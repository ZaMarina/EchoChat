module com.example.echochat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.echochat to javafx.fxml;
    exports com.example.echochat;
}