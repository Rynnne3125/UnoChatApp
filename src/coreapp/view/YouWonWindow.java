package coreapp.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import coreapp.util.constants.ImagePath;
import coreapp.util.constants.WindowConstants;

import static jdk.internal.net.http.common.Utils.close;

/**
 * Represents a window displayed when the player wins the game. Extends the
 * BaseStage class.
 */
public class YouWonWindow extends BaseFrame {

    /**
     * Constructs a new YouWonWindow.
     */
    public YouWonWindow() {
        super(WindowConstants.YOU_WON_WINDOW_TITLE);
        initializeStage();
    }

    /**
     * Initializes the stage layout.
     */
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

    @Override
    void initializeFrame() {

    }

    /**
     * Creates the back button with image and action handler
     */
    private Button createBackButton() {
        Image backImage = new Image(ImagePath.BACK_ICON);
        ImageView backImageView = new ImageView(backImage);
        backImageView.setFitWidth(50);
        backImageView.setFitHeight(50);

        Button backButton = new Button();
        backButton.setGraphic(backImageView);
        backButton.setStyle("-fx-background-color: transparent;");
        backButton.setPrefSize(50, 50);

        backButton.setOnAction(e -> {
            close();
            new MainMenu();
        });

        return backButton;
    }

    /**
     * Creates a gradient panel (placeholder - needs actual gradient implementation)
     */
    private Pane createGradientPanel() {
        Pane panel = new Pane();
        // Set gradient background (example - adjust colors as needed)
        panel.setStyle("-fx-background-color: linear-gradient(to bottom, #1e3c72, #2a5298);");
        return panel;
    }
}