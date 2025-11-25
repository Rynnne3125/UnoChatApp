module UnoChatApp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // Có thể cần dòng này
    requires javafx.media;
	requires jakarta.mail;
	requires jakarta.activation;
	requires com.google.gson;
	requires java.desktop;
	requires java.net.http;
    opens model to com.google.gson;
	opens application to javafx.graphics, javafx.fxml;
	requires WaifUPnP;
    exports control;
    opens control to javafx.graphics, javafx.fxml;
}