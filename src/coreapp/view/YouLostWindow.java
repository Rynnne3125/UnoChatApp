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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import coreapp.util.constants.FontConstants;
import coreapp.util.constants.ImagePath;
import coreapp.util.ui.UIUtils;
import coreapp.view.CustomComponents.ButtonWithImage;

/**
 * Represents the window displayed when the player loses the game.
 */
public class YouLostWindow extends BaseFrame {

    /** The message to be displayed in the window. */
    private String _message;

    /** Font object for custom fonts in the window. */
    private final Font customFont = UIUtils.loadCustomFont(FontConstants.RechargeFontPath);

    /**
     * Constructs a new YouLostWindow with the specified message.
     *
     * @param message The message to be displayed.
     */
    public YouLostWindow(String message) {
        super(coreapp.util.constants.WindowConstants.YOU_LOST_WINDOW_TITLE);
        _message = message;
        initializeFrame();
    }

    @Override
    void initializeStage() {

    }

    /**
     * Initializes the frame of the YouLostWindow.
     */
    @Override
    void initializeFrame() {
        BorderPane mainPanel = new BorderPane();
        mainPanel.setPrefSize(coreapp.util.constants.WindowConstants.DEFAULT_WINDOW_WIDTH,
                coreapp.util.constants.WindowConstants.DEFAULT_WINDOW_HEIGHT);

        // Create button panel
        StackPane buttonPanel = new StackPane();
        buttonPanel.setPadding(new Insets(20, 0, 0, 0));
        buttonPanel.setAlignment(Pos.TOP_LEFT);

        // Load and scale back button image
        Image backImage = new Image(ImagePath.BACK_ICON);
        ImageView backImageView = new ImageView(backImage);
        backImageView.setFitWidth(50);
        backImageView.setFitHeight(50);

        Button backButton = new Button();
        backButton.setGraphic(backImageView);
        backButton.setStyle("-fx-background-color: transparent;");
        backButton.setOnAction(e -> {
            close();
            new MainMenu();
        });

        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(0, 0, 0, -1180));
        buttonPanel.getChildren().add(backButton);

        mainPanel.setTop(buttonPanel);

        // Load and scale you lost image
        Image youLostImage = new Image(ImagePath.YOU_LOST_IMAGE);
        ImageView youLostImageView = new ImageView(youLostImage);
        youLostImageView.setFitWidth(400);
        youLostImageView.setFitHeight(200);
        youLostImageView.setPreserveRatio(true);

        StackPane imageContainer = new StackPane(youLostImageView);
        imageContainer.setAlignment(Pos.CENTER);
        mainPanel.setCenter(imageContainer);

        // Create message label
        Label messageLabel = new Label(_message);
        // Note: Font handling in JavaFX is different. You'll need to adapt UIUtils.loadCustomFont for JavaFX
        // messageLabel.setFont(customFont.deriveFont(Font.PLAIN, 22));
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 22;");
        messageLabel.setAlignment(Pos.CENTER);
        StackPane messageContainer = new StackPane(messageLabel);
        messageContainer.setAlignment(Pos.CENTER);
        messageContainer.setPadding(new Insets(0, 0, 70, 0));
        mainPanel.setBottom(messageContainer);

        Scene scene = new Scene(mainPanel);
        setScene(scene);
        show();
    }

    // Helper method to close the window
    private void close() {
        Stage stage = (Stage) getScene().getWindow();
        stage.close();
    }
}