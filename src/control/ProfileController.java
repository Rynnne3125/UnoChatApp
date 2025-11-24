package control;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.User;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import dao.FirebaseProfileRest;

public class ProfileController {

    @FXML private Circle avatarCircle;
    @FXML private Label usernameLabel;
    @FXML private Label rankLabel;
    @FXML private Label rankTextBig;
    @FXML private Label levelLabel;
    @FXML private Label statusLabel;
    @FXML private TextField statusInput;
    @FXML private Label pointLabel;
    @FXML private ProgressBar rankprogressbar;
    @FXML private VBox matchHistoryContainer;

    private User currentUser;
 
    // --- KHỞI TẠO DỮ LIỆU ---
    public void initData(User user) {
        this.currentUser = user;
        loadUserInfo();
        loadMockMatchHistory();
        
        // Setup listener cho status input
        statusInput.setOnAction(e -> {
            String newStatus = statusInput.getText();
            if(!newStatus.isEmpty()) {
                updateStatus(newStatus);
            }
        });
    }

    private void loadUserInfo() {
        if (currentUser == null) return;

        usernameLabel.setText(currentUser.getUsername());
        
        // Avatar
        try {
            if (currentUser.getImageAvatar() != null) {
                avatarCircle.setFill(new ImagePattern(new Image(currentUser.getImageAvatar())));
            }
        } catch (Exception e) {
            avatarCircle.setFill(Color.GRAY);
        }

        // Rank (Giả sử User có field getRank(), nếu chưa thì dùng default)
        // Lưu ý: Bạn cần thêm getter getRank() và getPoint() vào User.java nếu chưa có
        // Ở đây tôi dùng phương pháp kiểm tra an toàn
        String rank = "Unranked";
        int point = 0;
        
        // Giả lập lấy rank từ User object (bạn cần bổ sung getter vào User.java)
        // rank = user.getRank();
        // point = user.getPoint();
        
        // Tạm thời hardcode nếu class User chưa update
        rankLabel.setText(currentUser.getEmail());
        rankTextBig.setText(currentUser.getRank());
        
        pointLabel.setText(String.valueOf(point));
     // 2. Tính progress (0.0 → 1.0)
        double progress = Math.min(1.0, point / 100.0);

        // 3. Gán vào progress bar
        rankprogressbar.setProgress(progress);
        // Status mặc định
        statusLabel.setText("Online");
    }

    private void updateStatus(String msg) {
        statusLabel.setText(msg);
        statusInput.clear();
        // Update lên Firebase
        new Thread(() -> {
            FirebaseProfileRest.updateUserStatus(currentUser.getUsername(), msg);
        }).start();
    }

    // --- TẠO DANH SÁCH TRẬN ĐẤU GIẢ LẬP (STYLE RIOT) ---
    private void loadMockMatchHistory() {
        matchHistoryContainer.getChildren().clear();
        
        // Tạo 10 trận đấu ngẫu nhiên
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            boolean isWin = rand.nextBoolean();
            String champName = isWin ? "PLAY WITH FRIENDS" : "PLAY RANDOM";
            List<String> kdaDates = List.of(
            	    "12/11/2025",
            	    "23/11/2025",
            	    "14/11/2025",
            	    "20/11/2025",
            	    "05/11/2025",
            	    "08/11/2025",
            	    "18/11/2025"
            	);

            Random rnd = new Random();
            String kda = kdaDates.get(rnd.nextInt(kdaDates.size()));
            
            matchHistoryContainer.getChildren().add(createMatchItem(isWin, champName, kda));
        }
    }

    private HBox createMatchItem(boolean isWin, String champ, String kda) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 20, 10, 20));
        
        // Style tùy theo thắng thua
        // Thắng: Xanh (#28344E), Border Trái Xanh Dương (#5383E8)
        // Thua: Đỏ (#59343B), Border Trái Đỏ (#E84057)
        String bgColor = isWin ? "#28344E" : "#59343B";
        String accentColor = isWin ? "#5383E8" : "#E84057";
        String resultText = isWin ? "VICTORY" : "DEFEAT";
        
        item.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + accentColor + "; -fx-border-width: 0 0 0 4;");
        
        // 1. Kết quả & Mode
        VBox resultBox = new VBox(2);
        Label resLbl = new Label(resultText);
        resLbl.setTextFill(Color.web(isWin ? "#5383E8" : "#E84057"));
        resLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label modeLbl = new Label("Ranked Solo");
        modeLbl.setTextFill(Color.web("#9e9eb1"));
        modeLbl.setStyle("-fx-font-size: 10px;");
        
        resultBox.getChildren().addAll(resLbl, modeLbl);
        resultBox.setPrefWidth(80);

        // 2. Champion Icon (Placeholder Circle)
        Circle champIcon = new Circle(24);
        champIcon.setFill(Color.web("#1E2328"));
        champIcon.setStroke(Color.web(isWin ? "#5383E8" : "#E84057"));
        
        // 3. Info (Champ Name & KDA)
        VBox statsBox = new VBox(2);
        Label champLbl = new Label(champ);
        champLbl.setTextFill(Color.WHITE);
        champLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label kdaLbl = new Label("DATE: "+kda);
        kdaLbl.setTextFill(Color.web("#9e9eb1"));
        
        statsBox.getChildren().addAll(champLbl, kdaLbl);
        
        // 4. Spacer
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // 5. Time
        Label timeLbl = new Label("2 days ago");
        timeLbl.setTextFill(Color.GRAY);

        item.getChildren().addAll(resultBox, champIcon, statsBox, spacer, timeLbl);
        return item;
    }

    // --- NAVIGATION ---
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/NewsView.fxml"));
            Parent root = loader.load();
            
            // Truyền User ngược lại NewsController để giữ session
            NewsController controller = loader.getController();
            controller.initData();

            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setFullScreen(true);
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}