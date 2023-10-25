module com.example.whatsup_server {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.whatsup_server to javafx.fxml;
    exports com.example.whatsup_server;
}