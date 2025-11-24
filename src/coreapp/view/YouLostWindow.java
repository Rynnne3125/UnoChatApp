package coreapp.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import coreapp.util.constants.FontConstants;
import coreapp.util.constants.ImagePath;
import coreapp.util.constants.WindowConstants;
import coreapp.util.ui.UIUtils;

class YouLostWindow extends BaseFrame {
    
    private String message;
    private final Font customFont = UIUtils.loadCustomFont(FontConstants.RechargeFontPath);

    public YouLostWindow(String message) {
        super(WindowConstants.YOU_LOST_WINDOW_TITLE);
        this.message = message;
        initializeFrame();
    }

    @Override
    void initializeFrame() {
        Pane mainPanel = createGradientPanel();
        BorderPane mainContainer = new BorderPane();
        mainContainer.setPrefSize(WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);

        // Create button panel
        StackPane buttonPanel = new StackPane();
        buttonPanel.setPadding(new Insets(20, 0, 0, 20));
        buttonPanel.setAlignment(Pos.TOP_LEFT);

        Image backImage = new Image(ImagePath.BACK_ICON);
        ImageView backImageView = new ImageView(backImage);
        backImageView.setFitWidth(50);
        backImageView.setFitHeight(50);

        Button backButton = new Button();
        backButton.setGraphic(backImageView);
        backButton.setStyle("-fx-background-color: transparent;");
        backButton.setPrefSize(50, 50);
        backButton.setCursor(Cursor.HAND);
        
        backButton.setOnAction(e -> {
            dispose();
            new MainMenu();
        });

        buttonPanel.getChildren().add(backButton);
        mainContainer.setTop(buttonPanel);

        // Load and scale you lost image
        Image youLostImage = new Image(ImagePath.YOU_LOST_IMAGE);
        ImageView youLostImageView = new ImageView(youLostImage);
        youLostImageView.setFitWidth(400);
        youLostImageView.setFitHeight(200);
        youLostImageView.setPreserveRatio(true);

        StackPane imageContainer = new StackPane(youLostImageView);
        imageContainer.setAlignment(Pos.CENTER);
        mainContainer.setCenter(imageContainer);

        // Create message label
        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font(customFont.getFamily(), 22));
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setAlignment(Pos.CENTER);
        
        StackPane messageContainer = new StackPane(messageLabel);
        messageContainer.setAlignment(Pos.CENTER);
        messageContainer.setPadding(new Insets(0, 0, 70, 0));
        mainContainer.setBottom(messageContainer);

        mainPanel.getChildren().add(mainContainer);

        Scene scene = new Scene(mainPanel, WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        setScene(scene);
        show();
    }

    private Pane createGradientPanel() {
        Pane panel = new Pane();
        panel.setPrefSize(WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        
        Stop[] stops = new Stop[] { 
            new Stop(0, Color.web("#1e3c72")), 
            new Stop(1, Color.web("#2a5298"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        BackgroundFill bgFill = new BackgroundFill(gradient, null, null);
        panel.setBackground(new Background(bgFill));
        
        return panel;
    }

    @Override
    void initializeStage() {
        // Not used for this view
    }
}