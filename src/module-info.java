module UnoChatApp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // Có thể cần dòng này

    // Dòng quan trọng nhất: Mở package 'community' để JavaFX có thể truy cập
    exports community;
    
    // Hoặc dùng opens nếu exports không đủ (thường dùng cho FXML)
    opens community to javafx.graphics, javafx.fxml;
}