package utils;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import control.LoginController;
import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
public class showCustomAlert {
	
	// ... Các biến và hàm khác ...
	public static void showCustomAlert(String title, String message) {
		boolean isSuccess = true;
	    Stage alertStage = new Stage();
	    alertStage.initModality(Modality.APPLICATION_MODAL);
	    alertStage.initStyle(StageStyle.TRANSPARENT); // Cửa sổ không viền

	    // Màu sắc chủ đạo (Xanh nếu thành công, Đỏ nếu lỗi)
	    String headerColor = isSuccess ? "#379711" : "#D72600"; // UNO Green / Red
	    String iconText = isSuccess ? "✔" : "✖";

	    // --- Layout chính ---
	    VBox root = new VBox(15);
	    root.setAlignment(Pos.CENTER);
	    root.setPadding(new Insets(20, 30, 20, 30));
	    // Style CSS inline cho box
	    root.setStyle(
	        "-fx-background-color: #1a1a1a;" + // Nền tối
	        "-fx-border-color: " + headerColor + ";" +
	        "-fx-border-width: 2;" +
	        "-fx-background-radius: 15;" +
	        "-fx-border-radius: 15;"
	    );
	    // Hiệu ứng bóng đổ
	    root.setEffect(new DropShadow(10, Color.BLACK));

	    // --- Tiêu đề ---
	    Label lblTitle = new Label(iconText + "  " + title.toUpperCase());
	    lblTitle.setStyle("-fx-text-fill: " + headerColor + "; -fx-font-weight: bold; -fx-font-size: 18px;");

	    // --- Nội dung ---
	    Label lblMessage = new Label(message);
	    lblMessage.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
	    lblMessage.setWrapText(true);
	    lblMessage.setMaxWidth(300);

	    // --- Nút OK ---
	    Button btnOk = new Button("OK");
	    btnOk.setPrefWidth(100);
	    btnOk.setStyle(
	        "-fx-background-color: " + headerColor + ";" +
	        "-fx-text-fill: white;" +
	        "-fx-font-weight: bold;" +
	        "-fx-background-radius: 20;" +
	        "-fx-cursor: hand;"
	    );
	    // Hiệu ứng hover cho nút
	    btnOk.setOnMouseEntered(e -> btnOk.setStyle("-fx-background-color: white; -fx-text-fill: " + headerColor + "; -fx-font-weight: bold; -fx-background-radius: 20;"));
	    btnOk.setOnMouseExited(e -> btnOk.setStyle("-fx-background-color: " + headerColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;"));
	    
	    btnOk.setOnAction(e -> alertStage.close());

	    root.getChildren().addAll(lblTitle, lblMessage, btnOk);

	    // --- Scene ---
	    Scene scene = new Scene(root);
	    scene.setFill(Color.TRANSPARENT); // Nền scene trong suốt để bo tròn đẹp
	    alertStage.setScene(scene);
	    alertStage.showAndWait();
	}
}
