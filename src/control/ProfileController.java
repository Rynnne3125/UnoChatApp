package control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.User;
import dao.FirebaseProfileRest;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import application.Main;

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

    // --- DANH SÁCH ẢNH RANDOM ---
    private final List<String> RANDOM_AVATARS = List.of(
            "https://media.makeameme.org/created/uno-reverse-lol.jpg",
            "https://img.freepik.com/premium-photo/vibrant-men-s-cricket-world-cup-2024-illustration-featuring-dynamic-cricket-illustration-with-fast-hits_719166-4508.jpg",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRGcCnNGEL4TaYHxQEzSfIpnQlz3VcW9TAsKQ&s",
            "https://i.pinimg.com/736x/d5/c0/fc/d5c0fc0734cb465b16affe739be62c52.jpg",
            "https://image.spreadshirtmedia.net/image-server/v1/products/T949A2PA2009PT25X7Y0D320877709W4629H7023/views/3,width=550,height=550,appearanceId=2,backgroundColor=F2F2F2,modelId=11689,crop=list/uno-4-design-four-draw-card-mug.jpg",
            "https://cdn.dribbble.com/userupload/23839376/file/original-f6a79767815644ade14c04c8b7b80a9e.png"
    );

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
        
        // Load Avatar
        try {
            if (currentUser.getImageAvatar() != null && !currentUser.getImageAvatar().isEmpty()) {
                avatarCircle.setFill(new ImagePattern(new Image(currentUser.getImageAvatar())));
            } else {
                 avatarCircle.setFill(Color.GRAY);
            }
        } catch (Exception e) {
            avatarCircle.setFill(Color.GRAY);
        }

        // Mock Data / Load Data
        rankLabel.setText(currentUser.getEmail());
        rankTextBig.setText(currentUser.getRank());
        
        // Giả lập point nếu chưa có getter
        int point = 1250; 
        pointLabel.setText(String.valueOf(point));
        
        double progress = Math.min(1.0, point / 2000.0); // Ví dụ max point là 2000
        rankprogressbar.setProgress(progress);
        
        statusLabel.setText("Online");
    }

    // --- XỬ LÝ ĐỔI AVATAR (NEW) ---
    @FXML
    private void handleAvatarClick(MouseEvent event) {
        // Không hiện menu nữa, click là đổi luôn
        setRandomAvatar();
    }

    private void setRandomAvatar() {
        if (RANDOM_AVATARS.isEmpty()) return;

        Random rand = new Random();
        String randomUrl = RANDOM_AVATARS.get(rand.nextInt(RANDOM_AVATARS.size()));
        
        // Gọi hàm update
        updateAvatar(randomUrl);
    }

    private void updateAvatar(String imageUrl) {
        // 1. Cập nhật UI ngay lập tức (Optimistic UI)
        try {
            avatarCircle.setFill(new ImagePattern(new Image(imageUrl)));
        } catch (Exception e) {
            System.out.println("Error loading image for UI: " + e.getMessage());
        }

        // 2. Cập nhật Model local
        if (currentUser != null) {
            // 3. Cập nhật Database qua DAO (Chạy Thread riêng để không lag UI)
            String username = currentUser.getUsername();
            new Thread(() -> {
                boolean success = FirebaseProfileRest.updateUserAvatar(username, imageUrl);
                if (success) {
                    System.out.println("✅ Avatar updated successfully on Firebase: " + imageUrl);
                    avatarCircle.setFill(new ImagePattern(new Image(imageUrl)));
                    Main.CurrentUser.setImageAvatar(imageUrl);
                } else {
                    System.err.println("❌ Failed to update avatar on Firebase");
                }
            }).start();
        }
    }

    // --- CÁC HÀM CŨ (LOGIC STATUS & MATCH HISTORY) ---

    private void updateStatus(String msg) {
        statusLabel.setText(msg);
        statusInput.clear();
        new Thread(() -> {
            FirebaseProfileRest.updateUserStatus(currentUser.getUsername(), msg);
        }).start();
    }

    private void loadMockMatchHistory() {
        matchHistoryContainer.getChildren().clear();
        Random rand = new Random();
        List<String> kdaDates = List.of(
                "12/11/2025", "23/11/2025", "14/11/2025", 
                "20/11/2025", "05/11/2025", "08/11/2025", "18/11/2025"
        );

        for (int i = 0; i < 10; i++) {
            boolean isWin = rand.nextBoolean();
            String champName = isWin ? "PLAY WITH FRIENDS" : "PLAY RANDOM";
            String kda = kdaDates.get(rand.nextInt(kdaDates.size()));
            matchHistoryContainer.getChildren().add(createMatchItem(isWin, champName, kda));
        }
    }

    private HBox createMatchItem(boolean isWin, String champ, String kda) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 20, 10, 20));
        
        String bgColor = isWin ? "#28344E" : "#59343B";
        String accentColor = isWin ? "#5383E8" : "#E84057";
        String resultText = isWin ? "VICTORY" : "DEFEAT";
        
        item.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + accentColor + "; -fx-border-width: 0 0 0 4;");
        
        VBox resultBox = new VBox(2);
        Label resLbl = new Label(resultText);
        resLbl.setTextFill(Color.web(isWin ? "#5383E8" : "#E84057"));
        resLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label modeLbl = new Label("Ranked Solo");
        modeLbl.setTextFill(Color.web("#9e9eb1"));
        modeLbl.setStyle("-fx-font-size: 10px;");
        
        resultBox.getChildren().addAll(resLbl, modeLbl);
        resultBox.setPrefWidth(80);

        Circle champIcon = new Circle(24);
        champIcon.setFill(Color.web("#1E2328"));
        champIcon.setStroke(Color.web(isWin ? "#5383E8" : "#E84057"));
        
        VBox statsBox = new VBox(2);
        Label champLbl = new Label(champ);
        champLbl.setTextFill(Color.WHITE);
        champLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label kdaLbl = new Label("DATE: "+kda);
        kdaLbl.setTextFill(Color.web("#9e9eb1"));
        
        statsBox.getChildren().addAll(champLbl, kdaLbl);
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label timeLbl = new Label("2 days ago");
        timeLbl.setTextFill(Color.GRAY);

        item.getChildren().addAll(resultBox, champIcon, statsBox, spacer, timeLbl);
        return item;
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/NewsView.fxml"));
            Parent root = loader.load();
            
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