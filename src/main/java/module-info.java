module com.example.echochat {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.example.echochat.Client;
    opens com.example.echochat.Client to javafx.fxml;
}