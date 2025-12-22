package application;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

import control.NewsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
public class UnoGameMenu extends Application {

    private Stage primaryStage;

    // Màu sắc chuẩn UNO
    private final Color UNO_RED = Color.web("#D72600");
    private final Color UNO_YELLOW = Color.web("#F9C700");
    private final Color UNO_GREEN = Color.web("#379711");
    private final Color UNO_BLUE = Color.web("#0956BF");
    private final Color BG_DARK = Color.web("#1a1a1a");

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // --- 1. BACKGROUND ---
        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#2c3e50")),
                        new Stop(1, Color.web("#000000"))),
                CornerRadii.EMPTY, Insets.EMPTY)));

        // Hiệu ứng các lá bài bay lơ lửng làm nền
        Pane backgroundParticles = createFloatingCards();
        root.getChildren().add(backgroundParticles);

        // --- 2. MAIN LAYOUT ---
        VBox mainContent = new VBox(20);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(40));

        // Logo UNO
        StackPane logo = createUnoLogo();

        // Phần nhập thông tin người chơi (Client Test)
        VBox playerSetupBox = createPlayerSetup();

        // Menu Buttons
        HBox actionButtons = new HBox(30);
        actionButtons.setAlignment(Pos.CENTER);

        Button btnPlay = createMenuButton("PLAY NOW", UNO_GREEN, "▶");
        Button btnCommunity = createMenuButton("CỘNG ĐỒNG", UNO_BLUE, "💬"); // Nút chuyển sang Chat
        Button btnNews = createMenuButton("NEWS",UNO_RED, "📰");

        // Xử lý sự kiện chuyển sang UnoChatApp
        btnCommunity.setOnAction(e -> switchToChatApp());
        btnPlay.setOnAction(e-> switchToGameApp());
        btnNews.setOnAction(e -> switchToNews());

        actionButtons.getChildren().addAll(btnPlay, btnCommunity, btnNews);

        mainContent.getChildren().addAll(logo, playerSetupBox, actionButtons);

        // Hiệu ứng mờ kính (Glassmorphism) cho box chính
        StackPane glassPanel = new StackPane(mainContent);
        glassPanel.setMaxSize(800, 600);
        glassPanel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 30; -fx-border-radius: 30; -fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1;");

        root.getChildren().add(glassPanel);

        Scene scene = new Scene(root, 1024, 768);
        stage.setTitle("UNO Game - Ultimate Edition");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }

    // --- LOGIC CHUYỂN CẢNH SANG CHAT APP ---
    private void switchToChatApp() {
        try {
            // Tạo hiệu ứng fade out
            FadeTransition ft = new FadeTransition(Duration.millis(300), primaryStage.getScene().getRoot());
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> {
                try {
                    // Gọi UnoChatApp từ file bạn đã gửi
                    UnoChatApp chatApp = new UnoChatApp();
                    chatApp.start(primaryStage); // Sử dụng lại Stage hiện tại
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            ft.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // --- LOGIC CHUYỂN CẢNH SANG CHAT APP ---
    // Tìm đến hàm này trong UnoGameMenu.java và thay thế:
    private void switchToGameApp() {
        try {
            // Lấy tên từ nameField (Bạn cần khai báo nameField là biến toàn cục hoặc lấy từ UI)
            // Ở đây tôi giả định lấy từ Main.CurrentUser hoặc mặc định
            String playerName = (Main.CurrentUser != null) ? Main.CurrentUser.getUsername() : "Player";

            // Lấy avatar mặc định hoặc từ logic chọn avatar của bạn
            String playerAvatar = "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/Yasuo.png";

            FadeTransition ft = new FadeTransition(Duration.millis(300), primaryStage.getScene().getRoot());
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> {
                try {
                    // Khởi tạo UnoGameApp với vai trò HOST (vì bắt đầu từ Menu)
                    // Các tham số: Tên, IsHost, Sockets list, Writers list, HostAvatar, Guest1Name, Guest1Avatar, Guest2Name, Guest2Avatar
                    UnoGameApp gameApp = new UnoGameApp(
                            playerName,
                            true,                       // isHost
                            new java.util.ArrayList<>(), // clientSockets (trống vì chưa có ai kết nối)
                            new java.util.ArrayList<>(), // clientWriters
                            playerAvatar,               // Avatar của bạn
                            "Bot Ahri",                 // Guest 1 (Bot)
                            "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/Ahri.png",
                            "Bot Teemo",                // Guest 2 (Bot)
                            "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/Teemo.png"
                    );

                    gameApp.start(primaryStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            ft.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // --- LOGIC CHUYỂN CẢNH SANG NEWS VIEW ---
    private void switchToNews() {
        // Tạo hiệu ứng mờ dần (Fade Out) trước khi chuyển cảnh cho mượt
        FadeTransition ft = new FadeTransition(Duration.millis(300), primaryStage.getScene().getRoot());
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            try {
                // 1. Load file FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/NewsView.fxml"));
                Parent root = loader.load();

                // 2. Lấy Controller và khởi tạo dữ liệu (Quan trọng để load tin tức/user)
                // Lưu ý: Cần đảm bảo class NewsController có hàm initData() public
                Object controller = loader.getController();
                if (controller instanceof NewsController) {
                    ((NewsController) controller).initData();
                }

                // 3. Set Scene mới
                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                primaryStage.setFullScreen(true);
                primaryStage.centerOnScreen();

                // (Tùy chọn) Fade In lại nếu muốn
                // FadeTransition ftIn = new FadeTransition(Duration.millis(300), root);
                // ftIn.setFromValue(0.0); ftIn.setToValue(1.0); ftIn.play();

            } catch (IOException ex) {
                ex.printStackTrace();
                // Có thể hiển thị Alert báo lỗi nếu không tìm thấy file
            }
        });
        ft.play();
    }

    // ============================================================
    // CÁC HÀM TẠO GIAO DIỆN (UI COMPONENTS)
    // ============================================================

    private StackPane createUnoLogo() {
        Text t1 = new Text("U"); t1.setFill(UNO_YELLOW);
        Text t2 = new Text("N"); t2.setFill(UNO_RED);
        Text t3 = new Text("O"); t3.setFill(UNO_GREEN);
        Text t4 = new Text("!"); t4.setFill(UNO_BLUE);

        HBox textGroup = new HBox(5, t1, t2, t3, t4);
        textGroup.setAlignment(Pos.CENTER);

        for (javafx.scene.Node n : textGroup.getChildren()) {
            Text t = (Text) n;
            t.setFont(Font.font("Arial Rounded MT Bold", FontWeight.EXTRA_BOLD, 120));
            t.setStroke(Color.WHITE);
            t.setStrokeWidth(4);
            DropShadow ds = new DropShadow();
            ds.setOffsetY(5); ds.setColor(Color.BLACK);
            t.setEffect(ds);
        }

        // Xoay logo một chút cho nghệ
        StackPane logoContainer = new StackPane(textGroup);
        Rotate rotate = new Rotate(-10, 0, 0);
        logoContainer.getTransforms().add(rotate);

        // Animation nhịp đập
        ScaleTransition st = new ScaleTransition(Duration.millis(1000), logoContainer);
        st.setFromX(1); st.setFromY(1);
        st.setToX(1.05); st.setToY(1.05);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);
        st.play();

        return logoContainer;
    }

    private VBox createPlayerSetup() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Label lbl = new Label("THÔNG TIN NGƯỜI CHƠI");
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 18));

        // --- SỬA ĐỔI Ở ĐÂY ---
        TextField nameField = new TextField();
        nameField.setMaxWidth(300);
        nameField.setStyle("-fx-background-radius: 20; -fx-padding: 10; -fx-font-size: 14px; -fx-background-color: rgba(255,255,255,0.9);");

        // Kiểm tra xem đã có người đăng nhập chưa (Biến Main.CurrentUser)
        if (Main.CurrentUser != null) {
            // Nếu đã đăng nhập, điền tên và khóa không cho sửa (hoặc để sửa tùy bạn)
            nameField.setText(Main.CurrentUser.getUsername()); // Giả sử getter là getUserName() hoặc getHoTen()
            nameField.setEditable(false); // Đã đăng nhập thì không sửa tên ở đây
        } else {
            nameField.setPromptText("Nhập tên hiển thị...");
        }
        // ---------------------

        HBox avatarBox = new HBox(10);
        avatarBox.setAlignment(Pos.CENTER);
        String[] avatars = {"🦊", "🐼", "🐯", "🐸", "🐙"};
        ToggleGroup group = new ToggleGroup();

        for (String icon : avatars) {
            RadioButton rb = new RadioButton(icon);
            rb.setToggleGroup(group);
            rb.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-cursor: hand;");
            avatarBox.getChildren().add(rb);
        }
        ((RadioButton)avatarBox.getChildren().get(0)).setSelected(true);

        box.getChildren().addAll(lbl, nameField, avatarBox);
        return box;
    }

    private Button createMenuButton(String text, Color baseColor, String icon) {
        Button btn = new Button(icon + " " + text);
        btn.setFont(Font.font("System", FontWeight.BOLD, 16));
        btn.setTextFill(Color.WHITE);
        btn.setPrefSize(200, 60);

        // CSS Style cho nút bấm bóng bẩy
        String cssColor = toHexString(baseColor);
        btn.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, derive(%s, -30%%)); " +
                        "-fx-background-radius: 30; " +
                        "-fx-border-color: white; -fx-border-radius: 30; -fx-border-width: 2; " +
                        "-fx-cursor: hand;", cssColor, cssColor
        ));

        // Hiệu ứng bóng
        DropShadow shadow = new DropShadow();
        shadow.setColor(baseColor.darker().darker());
        shadow.setOffsetY(5);
        btn.setEffect(shadow);

        // Animation khi di chuột
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.1); st.setToY(1.1);
            st.play();
            btn.setStyle(String.format(
                    "-fx-background-color: linear-gradient(to bottom, derive(%s, 20%%), %s); " +
                            "-fx-background-radius: 30; -fx-border-color: yellow; -fx-border-radius: 30; -fx-border-width: 2;", cssColor, cssColor
            ));
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setToX(1.0); st.setToY(1.0);
            st.play();
            btn.setStyle(String.format(
                    "-fx-background-color: linear-gradient(to bottom, %s, derive(%s, -30%%)); " +
                            "-fx-background-radius: 30; -fx-border-color: white; -fx-border-radius: 30; -fx-border-width: 2;", cssColor, cssColor
            ));
        });

        return btn;
    }

    // Hàm tạo các lá bài bay nền
    private Pane createFloatingCards() {
        Pane pane = new Pane();
        Random rand = new Random();
        Color[] colors = {UNO_RED, UNO_BLUE, UNO_GREEN, UNO_YELLOW};

        for (int i = 0; i < 15; i++) {
            Rectangle card = new Rectangle(60, 90);
            card.setArcWidth(10); card.setArcHeight(10);
            card.setFill(colors[rand.nextInt(colors.length)]);
            card.setStroke(Color.WHITE);
            card.setStrokeWidth(3);

            // Trang trí giữa lá bài
            Circle c = new Circle(20, Color.WHITE);
            c.setCenterX(30); c.setCenterY(45);

            // Gom lại thành 1 group
            Pane cardGroup = new Pane(card, c);

            // Vị trí ngẫu nhiên
            cardGroup.setLayoutX(rand.nextInt(1000));
            cardGroup.setLayoutY(rand.nextInt(700));
            cardGroup.setOpacity(0.3); // Mờ đi để làm nền
            cardGroup.setRotate(rand.nextInt(360));

            // Hiệu ứng mờ ảo
            cardGroup.setEffect(new GaussianBlur(5));

            // Animation trôi
            TranslateTransition tt = new TranslateTransition(Duration.seconds(10 + rand.nextInt(10)), cardGroup);
            tt.setByY(-100 - rand.nextInt(200));
            tt.setCycleCount(Animation.INDEFINITE);
            tt.setAutoReverse(true);
            tt.play();

            RotateTransition rt = new RotateTransition(Duration.seconds(5 + rand.nextInt(5)), cardGroup);
            rt.setByAngle(360);
            rt.setCycleCount(Animation.INDEFINITE);
            rt.play();

            pane.getChildren().add(cardGroup);
        }
        return pane;
    }

    private String toHexString(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }

    public static void main(String[] args) {
        launch(args);
    }
}