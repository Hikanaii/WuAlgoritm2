module com.example.wualgoritm {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.wualgoritm to javafx.fxml;
    exports com.example.wualgoritm;
}