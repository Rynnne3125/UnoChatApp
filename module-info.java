module UnoChatApp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;

    exports community;
    exports coreapp.model.user;
    opens community to javafx.fxml;
}
