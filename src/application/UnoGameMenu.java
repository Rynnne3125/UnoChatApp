package application;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
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
    
    // --- ĐƯA NAMEFIELD LÊN THÀNH BIẾN TOÀN CỤC ---
    private TextField nameField; 

    // Màu sắc chuẩn UNO
    private final Color UNO_RED = Color.web("#D72600");
    private final Color UNO_YELLOW = Color.web("#F9C700");
    private final Color UNO_GREEN = Color.web("#379711");
    private final Color UNO_BLUE = Color.web("#0956BF");

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

        Pane backgroundParticles = createFloatingCards();
        root.getChildren().add(backgroundParticles);

        // --- 2. MAIN LAYOUT ---
        VBox mainContent = new VBox(20);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(40));

        StackPane logo = createUnoLogo();
        VBox playerSetupBox = createPlayerSetup();

        // Menu Buttons
        HBox actionButtons = new HBox(30);
        actionButtons.setAlignment(Pos.CENTER);

        Button btnPlay = createMenuButton("PLAY NOW", UNO_GREEN, "▶");
        Button btnCommunity = createMenuButton("CỘNG ĐỒNG", UNO_BLUE, "💬");
        Button btnNews = createMenuButton("NEWS",UNO_RED, "📰");

        btnCommunity.setOnAction(e -> switchToChatApp());
        
        // --- SỬA: GỌI HÀM SWITCH TO LOBBY ---
        btnPlay.setOnAction(e -> switchToLobby()); 
        
        btnNews.setOnAction(e -> switchToNews());

        actionButtons.getChildren().addAll(btnPlay, btnCommunity, btnNews);

        mainContent.getChildren().addAll(logo, playerSetupBox, actionButtons);
        
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

    // --- LOGIC CHUYỂN SANG UNO LOBBY ---
    private void switchToLobby() {
        // Hiệu ứng Fade Out
        FadeTransition ft = new FadeTransition(Duration.millis(300), primaryStage.getScene().getRoot());
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            try {
                // 1. Lấy tên người chơi nhập ở Menu
                String playerName = nameField.getText();
                if (playerName == null || playerName.trim().isEmpty()) {
                    playerName = "Player";
                }

                // 2. Truyền tên sang Lobby Screen
                UnoLobbyScreen.setInitialName(playerName);

                // 3. Khởi tạo và chuyển cảnh
                UnoLobbyScreen lobby = new UnoLobbyScreen();
                lobby.start(primaryStage); // Dùng lại Stage hiện tại để không bật cửa sổ mới

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        ft.play();
    }

    private void switchToChatApp() {
        FadeTransition ft = new FadeTransition(Duration.millis(300), primaryStage.getScene().getRoot());
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            try {
                UnoChatApp chatApp = new UnoChatApp();
                chatApp.start(primaryStage);
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        ft.play();
    }

    private void switchToNews() {
        FadeTransition ft = new FadeTransition(Duration.millis(300), primaryStage.getScene().getRoot());
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/NewsView.fxml"));
                Parent root = loader.load();
                Object controller = loader.getController();
                if (controller instanceof NewsController) {
                    ((NewsController) controller).initData();
                }
                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                primaryStage.setFullScreen(true);
            } catch (IOException ex) { ex.printStackTrace(); }
        });
        ft.play();
    }

    // ... (Giữ nguyên createUnoLogo) ...
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

        StackPane logoContainer = new StackPane(textGroup);
        Rotate rotate = new Rotate(-10, 0, 0);
        logoContainer.getTransforms().add(rotate);
        
        ScaleTransition st = new ScaleTransition(Duration.millis(1000), logoContainer);
        st.setFromX(1); st.setFromY(1);
        st.setToX(1.05); st.setToY(1.05);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);
        st.play();

        return logoContainer;
    }

    // --- CẬP NHẬT HÀM NÀY ĐỂ KHỞI TẠO BIẾN nameField TOÀN CỤC ---
    private VBox createPlayerSetup() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        
        Label lbl = new Label("THÔNG TIN NGƯỜI CHƠI");
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Khởi tạo biến toàn cục nameField tại đây
        nameField = new TextField();
        nameField.setMaxWidth(300);
        nameField.setStyle("-fx-background-radius: 20; -fx-padding: 10; -fx-font-size: 14px; -fx-background-color: rgba(255,255,255,0.9);");

        if (Main.CurrentUser != null) {
            nameField.setText(Main.CurrentUser.getUsername());
            nameField.setEditable(false);
        } else {
            nameField.setPromptText("Nhập tên hiển thị...");
        }

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

    // ... (Giữ nguyên createMenuButton, createFloatingCards, toHexString) ...
    private Button createMenuButton(String text, Color baseColor, String icon) {
        Button btn = new Button(icon + " " + text);
        btn.setFont(Font.font("System", FontWeight.BOLD, 16));
        btn.setTextFill(Color.WHITE);
        btn.setPrefSize(200, 60);
        
        String cssColor = toHexString(baseColor);
        btn.setStyle(String.format(
            "-fx-background-color: linear-gradient(to bottom, %s, derive(%s, -30%%)); " +
            "-fx-background-radius: 30; " +
            "-fx-border-color: white; -fx-border-radius: 30; -fx-border-width: 2; " +
            "-fx-cursor: hand;", cssColor, cssColor
        ));

        DropShadow shadow = new DropShadow();
        shadow.setColor(baseColor.darker().darker());
        shadow.setOffsetY(5);
        btn.setEffect(shadow);

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
            
            Circle c = new Circle(20, Color.WHITE);
            c.setCenterX(30); c.setCenterY(45);
            
            Pane cardGroup = new Pane(card, c);
            
            cardGroup.setLayoutX(rand.nextInt(1000));
            cardGroup.setLayoutY(rand.nextInt(700));
            cardGroup.setOpacity(0.3);
            cardGroup.setRotate(rand.nextInt(360));
            
            cardGroup.setEffect(new GaussianBlur(5));

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