package com.example.wualgoritm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class WuLineDrawing extends Application {


    // Отрисовываем окно
    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(500, 500); // Canvas - это "холст", который располагается в открывшемся окне. На этом полотне, как раз, и рисует алгоритм
        GraphicsContext gc = canvas.getGraphicsContext2D();
        PixelWriter pw = gc.getPixelWriter(); //спользуется `GraphicsContext` для получения `PixelWriter`, который используется для записи цветных пикселей на полотно.

        double x0 = 10, y0 = 150, x1 = 490, y1 = 200; // Задаем координаты начал и конца линни
        Color startColor = Color.RED; // Задаем первый цвет
        Color endColor = Color.BLUE; // Задаем второй цвет

        drawWuLine(pw, x0, y0, x1, y1, startColor, endColor); // рисует отрезок с использованием алгоритма Ву и интерполирует цвет от начала до конца линии.

        StackPane root = new StackPane(canvas); //здесь создается объект `root` класса `StackPane`, который является контейнером для размещения элементов на сцене. В данном случае, `canvas` (холст) добавляется в `StackPane` как единственный элемент.
        primaryStage.setScene(new Scene(root)); //здесь создается объект `Scene` с корневым элементом `root`, который затем устанавливается в качестве сцены для объекта `primaryStage`. `Scene` представляет собой содержимое окна приложения, и в данном случае оно содержит `root` (содержащий холст).
        primaryStage.show(); //это вызов метода `show()`, который отображает главное окно приложения (`primaryStage`) на экране.
    }

    // Антиалиасинг - Способ визуального сглаживания, который встроен в алгоритм отрисовки By. Его суть заключается в том, чтобы не втупую отрисовать
    // наклонную линию попиксельно и получить "лесенку", а отрисовать "переходы", представляющие собой дополнительные пиксели в местах "лесенок" по сторонам от основных пикселе.
    // Чем дальше от основного пикселя находится этот антиалиасинговый пиксель, тем он более тусклый. Так создается эффект сглаживания.

    // Интерполяция, в этом алгоритме она Линейная -  это способ слияния несокльких цветов, для получения чего-то среднего. Проще говоря, это простой плавный переход между цветами.
    // Обязательно почитайте об этих алгоритмах, а лучше, попросите GPT подробно рассказать в чем заключается их суть и как они работают. Препод обязательно спросит.

    // Метод отрисовки By (Улучшенный алгоритм Брезенхерма) с антиалиасингом и интерполяцией
    private void drawWuLine(PixelWriter pw, double x0, double y0, double x1, double y1, Color startColor, Color endColor) {
        boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);

        // Те самые ифки, которые позволяют использовать написанный алгоритм для всевозможных вариантов отрисовки, исключая неверную работу алгоритма.

        //если вертикальный отрезок, то точки меняются местами
        //если линия диагональная, не сильно крутая, то антиалиасинг удет работать, но если под 45 градусов, то у нас будет много лесенок
        if (steep) {
            double temp = x0;
            x0 = y0;
            y0 = temp;
            temp = x1;
            x1 = y1;
            y1 = temp;
        }

        if (x0 > x1) { //алгоритм рисует слева направо, тут мы проверяем, если у нас начальная точка справа, то тогда код будет рисовать задом наперед, что нас не устраивает и поэтому мы их просто меняем местами
            double temp = x0;
            x0 = x1;
            x1 = temp;
            temp = y0;
            y0 = y1;
            y1 = temp;
            //проверяет, является ли начальная точка правее конечной на оси x. Если это так, то происходит обмен начальной и конечной точками отрезка,
            // а также их соответствующих координат y. Это делается для того, чтобы обеспечить правильное отображение отрезка, независимо от его направления.
        }

        double dx = x1 - x0;
        double dy = y1 - y0;
        double gradient = dy / dx;
        double intery = y0 + gradient;
        //Это вычисляет угловой коэффициент между двумя точками.
        //intery будет использоваться для определения точек пересечения линии с целочисленными координатами по оси Y в
        // процессе рисования линии.

        for (double x = x0; x <= x1; x++) {
            int xint = (int)x;
            int yint = (int)intery;
            //Этот кусок кода представляет из себя цикл по x-координатам от x0 до x1.
            // Для каждого значения x вычисляются целочисленные значения xint (округленное значение x) и yint (округленное значение intery).
            // Переменная intery изменяется в процессе, и используется для отслеживания точек пересечения линии с целочисленными координатами по оси Y.

            double brightnessMain = rfpart(intery);
            double brightnessSecondary = fpart(intery);
            //Затем вычисляются значения яркости brightnessMain и brightnessSecondary для текущей точки.
            // Эти значения используются для вычисления цвета пикселя на линии.

            Color colorMain = interpolateColor(startColor, endColor, (x - x0) / dx, brightnessMain);
            Color colorSecondary = interpolateColor(startColor, endColor, (x - x0) / dx, brightnessSecondary);
            //Далее, для каждого значения x вычисляется цвет colorMain и colorSecondary с помощью функции interpolateColor.
            // В качестве входных данных для interpolateColor используются начальный цвет startColor, конечный цвет endColor, коэффициент цвета (отношение (x - x0) / dx) и значения яркости.

            if (steep) {
                pw.setColor(yint, xint, colorMain);
                pw.setColor(yint + 1, xint, colorSecondary);// Рисуем попиксельно - пиксель основной линии + антиаллясинговый пиксель.

            } else {
                pw.setColor(xint, yint, colorMain);
                pw.setColor(xint, yint + 1, colorSecondary);
            }
            //Этот код отрисовывает пиксели для линии, и в зависимости от условия steep (которое описывает наклон линии)
            // устанавливает цвета пикселей на холсте. Если steep равно true, то цвета устанавливаются в yint и xint для основной линии и в yint + 1 и
            // xint для антиалиасинговой линии. Если steep равно false, то цвета устанавливаются в xint и yint для основной линии и в xint и yint + 1 для антиалиасинговой линии.
            intery = intery + gradient;
            System.out.println(intery);
        }
    }


    // Метод линейной интерполяции. ОБЯЗАТЕЛЬНО изучите его, препод спросит.
    private Color interpolateColor(Color startColor, Color endColor, double ratio, double brightness) {
        //На вход функции interpolateColor подается начальный цвет startColor (в формате Color),
        // конечный цвет endColor (в формате Color), коэффициент ratio (от 0.0 до 1.0) и яркость brightness (вещественное число от 0 до 1).
        if (ratio < 0.0) ratio = 0.0;
        if (ratio > 1.0) ratio = 1.0;
        //Затем проверяется, чтобы значение коэффициента ratio находилось в пределах от 0.0 до 1.0.
        // Если оно меньше 0.0, то оно устанавливается равным 0.0, а если больше 1.0, то оно устанавливается равным 1.0.
        double red = startColor.getRed() + ratio * (endColor.getRed() - startColor.getRed());
        double green = startColor.getGreen() + ratio * (endColor.getGreen() - startColor.getGreen());
        double blue = startColor.getBlue() + ratio * (endColor.getBlue() - startColor.getBlue());
//Дальше выполняется линейная интерполяция для каждой компоненты цвета (красной, зеленой и синей):
//red: вычисляется как сумма начального красного компонента startColor.getRed() и произведения ratio на разницу между красными компонентами конечного и начального цветов: ratio * (endColor.getRed() - startColor.getRed()).
//green и blue вычисляются аналогично для соответствующих компонент цвета.
        // Учитывая яркость, адаптируем альфа-канал
        return new Color(red, green, blue, brightness);
    }

    private double fpart(double x) {
        return x - Math.floor(x);
    }

    private double rfpart(double x) {
        return 1 - fpart(x);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
