package coreapp.view;

import application.Main;
import coreapp.util.constants.ImagePath;
import coreapp.util.constants.WindowConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * Represents the window displayed when the player wins the game (JavaFX version).
 */
class YouWonWindow extends BaseFrame {
    
    public YouWonWindow() {
        super(WindowConstants.YOU_WON_WINDOW_TITLE);
        initializeStage();
    }

    @Override
    void initializeStage() {
        Pane mainPanel = createGradientPanel();
        BorderPane mainContainer = new BorderPane();
        mainContainer.setPrefSize(WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);

        // Back button panel
        Button backButton = createBackButton();
        StackPane buttonPanel = new StackPane();
        buttonPanel.setAlignment(Pos.TOP_LEFT);
        buttonPanel.setPadding(new Insets(20, 0, 0, 20));
        buttonPanel.getChildren().add(backButton);
        mainContainer.setTop(buttonPanel);

        // You won image
        Image youWonImage = new Image(ImagePath.YOU_WON_IMAGE);
        ImageView imageView = new ImageView(youWonImage);
        imageView.setFitWidth(400);
        imageView.setFitHeight(500);
        imageView.setPreserveRatio(true);
        
        StackPane imagePanel = new StackPane(imageView);
        imagePanel.setAlignment(Pos.CENTER);
        mainContainer.setCenter(imagePanel);

        mainPanel.getChildren().add(mainContainer);

        Scene scene = new Scene(mainPanel, WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        setScene(scene);
        show();
    }

    private Button createBackButton() {
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
            new Main();
        });

        return backButton;
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
    void initializeFrame() {
        // Not used for this view
    }
}