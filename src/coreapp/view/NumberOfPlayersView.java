package coreapp.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import coreapp.util.constants.FontConstants;
import coreapp.util.constants.ImagePath;
import coreapp.util.constants.UITexts;
import coreapp.util.constants.WarningConstants;
import coreapp.util.constants.WindowConstants;
import coreapp.util.ui.UIUtils;
import coreapp.util.ui.toaster.Toaster;

// IMPORT MENU MỚI
import application.UnoGameMenu;

/**
 * The NumberOfPlayersView class represents a stage for selecting the number of
 * players in a game.
 */
public class NumberOfPlayersView extends BaseFrame {

    private Label numberOfPlayersLabel;
    private int numberOfPlayers = 2;
    private final Font customFont;
    private Toaster toaster;
    private Pane mainPanel;
    public NumberOfPlayersView() {
        super(WindowConstants.NUMBER_OF_PLAYERS_WINDOW_TITLE);
        customFont = UIUtils.loadCustomFont(FontConstants.RechargeFontPath);
        initializeStage();
    }
    private Image loadImage(String path) {
        try {
            // Dòng này giúp tìm ảnh trong thư mục src/images
            return new Image(getClass().getResource(path).toExternalForm());
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh: " + path);
            return null; // Trả về null để không crash app
        }
    }
    @Override
    void initializeStage() {
        mainPanel = createGradientPanel();
        toaster = new Toaster(mainPanel);
        
        VBox mainContainer = new VBox(20); // Tăng khoảng cách giữa các phần tử
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));

        // --- HEADER SECTION (BACK BUTTON + TITLE) ---
        HBox headerPanel = new HBox(20);
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        headerPanel.setPadding(new Insets(10));

        // 1. Back Button
        ImageView backImageView = new ImageView(loadImage(ImagePath.BACK_ICON));
        backImageView.setFitWidth(40);
        backImageView.setFitHeight(40);

        Button goBackButton = new Button();
        goBackButton.setGraphic(backImageView);
        goBackButton.setStyle("-fx-background-color: transparent;");
        goBackButton.setPrefSize(50, 50);
        goBackButton.setCursor(Cursor.HAND);

        // --- LOGIC QUAY LẠI MENU MỚI ---
        goBackButton.setOnAction(e -> {
            dispose(); // Đóng cửa sổ hiện tại
            try {
                // Mở lại UnoGameMenu
                new UnoGameMenu().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 2. Title Label
        Label playAgainstBotsLabel = new Label(UITexts.PLAY_AGAINST_BOTS.toUpperCase());
        playAgainstBotsLabel.setFont(Font.font(customFont.getFamily(), 40));
        playAgainstBotsLabel.setTextFill(Color.CYAN);
        
        // Căn giữa tiêu đề
        Region spacerLeft = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        Region spacerRight = new Region(); // Spacer ảo để đẩy title vào giữa
        HBox.setHgrow(spacerRight, Priority.ALWAYS);
        spacerRight.setPrefWidth(50); // Bù trừ cho nút back bên trái

        headerPanel.getChildren().addAll(goBackButton, spacerLeft, playAgainstBotsLabel, spacerRight);
        mainContainer.getChildren().add(headerPanel);

        // --- NUMBER SELECTION SECTION ---
        VBox selectionBox = new VBox(10);
        selectionBox.setAlignment(Pos.CENTER);

        Label numberOfBotsTextLabel = new Label(UITexts.NUMBER_OF_PLAYERS);
        numberOfBotsTextLabel.setFont(Font.font(customFont.getFamily(), 25));
        numberOfBotsTextLabel.setTextFill(Color.WHITE);
        
        HBox numberControlPanel = new HBox(35);
        numberControlPanel.setAlignment(Pos.CENTER);

        ImageView decreaseImageView = new ImageView(loadImage(ImagePath.NUMBER_OF_PLAYERS_DECREMENT));
        decreaseImageView.setFitWidth(60); decreaseImageView.setFitHeight(60);

        Button decreaseButton = new Button();
        decreaseButton.setGraphic(decreaseImageView);
        decreaseButton.setStyle("-fx-background-color: transparent;");
        decreaseButton.setCursor(Cursor.HAND);
        decreaseButton.setOnAction(e -> {
            if (numberOfPlayers > 2) {
                numberOfPlayers--;
                updatePlayerCountLabel();
            }
        });

        // Number Display
        numberOfPlayersLabel = new Label(String.valueOf(numberOfPlayers));
        numberOfPlayersLabel.setFont(Font.font(customFont.getFamily(), 40));
        numberOfPlayersLabel.setTextFill(Color.YELLOW); // Màu vàng cho nổi bật
        numberOfPlayersLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        // Increase Button
        ImageView increaseImageView = new ImageView(loadImage(ImagePath.NUMBER_OF_PLAYERS_INCREMENT));
        increaseImageView.setFitWidth(60); increaseImageView.setFitHeight(60);

        Button increaseButton = new Button();
        increaseButton.setGraphic(increaseImageView);
        increaseButton.setStyle("-fx-background-color: transparent;");
        increaseButton.setCursor(Cursor.HAND);
        increaseButton.setOnAction(e -> {
            if (numberOfPlayers < 10) {
                numberOfPlayers++;
                updatePlayerCountLabel();
            }
        });

        numberControlPanel.getChildren().addAll(decreaseButton, numberOfPlayersLabel, increaseButton);
        selectionBox.getChildren().addAll(numberOfBotsTextLabel, numberControlPanel);
        mainContainer.getChildren().add(selectionBox);

        // --- SESSION NAME SECTION ---
        VBox sessionBox = new VBox(10);
        sessionBox.setAlignment(Pos.CENTER);
        sessionBox.setPadding(new Insets(20, 0, 0, 0));

        Label nameOfSessionLabel = new Label(UITexts.NAME_OF_SESSION);
        nameOfSessionLabel.setFont(Font.font(customFont.getFamily(), 25));
        nameOfSessionLabel.setTextFill(Color.WHITE);

        TextField sessionNameField = new TextField();
        sessionNameField.setFont(Font.font(customFont.getFamily(), 20));
        sessionNameField.setAlignment(Pos.CENTER);
        sessionNameField.setPrefSize(400, 60);
        sessionNameField.setMaxWidth(400);
        // Style đẹp hơn cho TextField
        sessionNameField.setStyle(
            "-fx-text-fill: white;" +
            "-fx-background-color: rgba(0, 0, 0, 0.5);" +
            "-fx-border-color: cyan;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 15;" +
            "-fx-background-radius: 15;" +
            "-fx-prompt-text-fill: gray;"
        );
        sessionNameField.setPromptText("Enter room name...");

        sessionBox.getChildren().addAll(nameOfSessionLabel, sessionNameField);
        mainContainer.getChildren().add(sessionBox);

        // --- START BUTTON ---
        ImageView startButtonImageView = new ImageView(loadImage(ImagePath.START_BUTTON));
        startButtonImageView.setFitWidth(200); startButtonImageView.setFitHeight(80);

        Button startGameButton = new Button();
        startGameButton.setGraphic(startButtonImageView);
        startGameButton.setStyle("-fx-background-color: transparent;");
        startGameButton.setCursor(Cursor.HAND);
        
        // Hiệu ứng hover cho nút Start
        startGameButton.setOnMouseEntered(e -> startGameButton.setScaleX(1.1));
        startGameButton.setOnMouseExited(e -> startGameButton.setScaleX(1.0));

        startGameButton.setOnAction(e -> {
            String sessionName = sessionNameField.getText();
            if (sessionName.trim().isEmpty()) {
                toaster.warn(WarningConstants.FILL_SESSION_NAME_WARNING);
            } else {
                dispose();
                // Vào bàn chơi chính
                new GameTable(numberOfPlayers, sessionName);
            }
        });

        VBox.setMargin(startGameButton, new Insets(30, 0, 0, 0));
        mainContainer.getChildren().add(startGameButton);

        mainPanel.getChildren().add(mainContainer);

        Scene scene = new Scene(mainPanel, WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        setScene(scene);
        show();
    }

    private void updatePlayerCountLabel() {
        numberOfPlayersLabel.setText(String.valueOf(numberOfPlayers));
    }

    private Pane createGradientPanel() {
        Pane panel = new Pane();
        panel.setPrefSize(WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        
        // Thay đổi background sang màu tối (Dark/Space theme) giống UnoGameMenu
        Stop[] stops = new Stop[] { 
            new Stop(0, Color.web("#2c3e50")), 
            new Stop(1, Color.web("#000000"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        BackgroundFill bgFill = new BackgroundFill(gradient, null, null);
        panel.setBackground(new Background(bgFill));
        
        return panel;
    }

    @Override
    void initializeFrame() {
        // Not used
    }
}