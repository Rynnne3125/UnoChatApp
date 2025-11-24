package coreapp.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import coreapp.util.constants.FontConstants;
import coreapp.util.constants.ImagePath;
import coreapp.util.constants.UITexts;
import coreapp.util.constants.WarningConstants;
import coreapp.util.constants.WindowConstants;
import coreapp.util.ui.UIUtils;
import coreapp.util.ui.toaster.Toaster;

import static jdk.internal.net.http.common.Utils.close;

/**
 * The NumberOfPlayersView class represents a stage for selecting the number of
 * players in a game.
 */
public class NumberOfPlayersView extends BaseFrame {

    /** Label for displaying the number of players. */
    private Label numberOfPlayersLabel;

    /** The number of players selected. */
    private int numberOfPlayers = 2;

    /**
     * The main font of the leaderboard page.
     */
    private final javafx.scene.text.Font customFont;

    /**
     * The toaster object for displaying notifications.
     */
    private Toaster toaster;

    /**
     * The main panel containing the components of the NumberOfPlayersView stage.
     */
    private Pane mainPanel;

    /**
     * Constructs a new NumberOfPlayersView.
     */
    public NumberOfPlayersView() {
        super(WindowConstants.NUMBER_OF_PLAYERS_WINDOW_TITLE);
        customFont = UIUtils.loadCustomFont(FontConstants.RechargeFontPath);
        initializeStage();
    }

    /**
     * Initializes the stage components.
     */
    @Override
    void initializeStage() {
        mainPanel = createGradientPanel();
        VBox mainContainer = new VBox(10);
        mainContainer.setAlignment(Pos.CENTER);

        // Play against bots label with back button
        Label playAgainstBotsLabel = new Label(UITexts.PLAY_AGAINST_BOTS.toUpperCase());
        playAgainstBotsLabel.setFont(javafx.scene.text.Font.font(customFont.getFamily(), 50));
        playAgainstBotsLabel.setTextFill(javafx.scene.paint.Color.CYAN);
        playAgainstBotsLabel.setAlignment(Pos.CENTER);

        Image backImage = new Image(ImagePath.BACK_ICON);
        ImageView backImageView = new ImageView(backImage);
        backImageView.setFitWidth(50);
        backImageView.setFitHeight(50);

        Button goBackButton = new Button();
        goBackButton.setGraphic(backImageView);
        goBackButton.setStyle("-fx-background-color: transparent;");
        goBackButton.setPrefSize(50, 50);

        goBackButton.setOnAction(e -> {
            close();
            new MainMenu();
        });

        HBox labelPanel = new HBox();
        labelPanel.setAlignment(Pos.CENTER_LEFT);
        labelPanel.setPadding(new Insets(10));
        labelPanel.getChildren().addAll(goBackButton, playAgainstBotsLabel);
        HBox.setHgrow(playAgainstBotsLabel, Priority.ALWAYS);
        playAgainstBotsLabel.setAlignment(Pos.CENTER);

        mainContainer.getChildren().add(labelPanel);

        // Number of players text label
        Label numberOfBotsTextLabel = new Label(UITexts.NUMBER_OF_PLAYERS);
        numberOfBotsTextLabel.setFont(javafx.scene.text.Font.font(customFont.getFamily(), 30));
        numberOfBotsTextLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        numberOfBotsTextLabel.setAlignment(Pos.CENTER);
        numberOfBotsTextLabel.setMaxWidth(Double.MAX_VALUE);
        mainContainer.getChildren().add(numberOfBotsTextLabel);

        // Number selection panel
        HBox thirdPanel = new HBox(35);
        thirdPanel.setAlignment(Pos.CENTER);
        thirdPanel.setPadding(new Insets(20, 0, 20, 0));

        int buttonSize = 70;

        Image decreaseImage = new Image(ImagePath.NUMBER_OF_PLAYERS_DECREMENT);
        ImageView decreaseImageView = new ImageView(decreaseImage);
        decreaseImageView.setFitWidth(buttonSize);
        decreaseImageView.setFitHeight(buttonSize);

        Button decreaseButton = new Button();
        decreaseButton.setGraphic(decreaseImageView);
        decreaseButton.setStyle("-fx-background-color: transparent;");
        decreaseButton.setPrefSize(buttonSize, buttonSize);

        decreaseButton.setOnAction(e -> {
            if (numberOfPlayers > 2) {
                numberOfPlayers--;
                numberOfPlayersLabel.setText(String.valueOf(numberOfPlayers));
            }
        });
        thirdPanel.getChildren().add(decreaseButton);

        numberOfPlayersLabel = new Label(String.valueOf(numberOfPlayers));
        numberOfPlayersLabel.setFont(javafx.scene.text.Font.font(customFont.getFamily(), 23));
        numberOfPlayersLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        numberOfPlayersLabel.setAlignment(Pos.CENTER);
        thirdPanel.getChildren().add(numberOfPlayersLabel);

        Image increaseImage = new Image(ImagePath.NUMBER_OF_PLAYERS_INCREMENT);
        ImageView increaseImageView = new ImageView(increaseImage);
        increaseImageView.setFitWidth(buttonSize);
        increaseImageView.setFitHeight(buttonSize);

        Button increaseButton = new Button();
        increaseButton.setGraphic(increaseImageView);
        increaseButton.setStyle("-fx-background-color: transparent;");
        increaseButton.setPrefSize(buttonSize, buttonSize);

        increaseButton.setOnAction(e -> {
            if (numberOfPlayers < 10) {
                numberOfPlayers++;
                numberOfPlayersLabel.setText(String.valueOf(numberOfPlayers));
            }
        });

        thirdPanel.getChildren().add(increaseButton);
        mainContainer.getChildren().add(thirdPanel);

        // Session name label
        Label nameOfSessionLabel = new Label(UITexts.NAME_OF_SESSION);
        nameOfSessionLabel.setFont(javafx.scene.text.Font.font(customFont.getFamily(), 30));
        nameOfSessionLabel.setTextFill(javafx.scene.paint.Color.WHITE);
        nameOfSessionLabel.setAlignment(Pos.CENTER);
        nameOfSessionLabel.setMaxWidth(Double.MAX_VALUE);
        mainContainer.getChildren().add(nameOfSessionLabel);

        // Session name field
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER);
        TextField sessionNameField = new TextField();
        sessionNameField.setFont(javafx.scene.text.Font.font(customFont.getFamily(), 23));
        sessionNameField.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
        sessionNameField.setAlignment(Pos.CENTER);
        sessionNameField.setPrefSize(555, 90);
        wrapper.getChildren().add(sessionNameField);
        mainContainer.getChildren().add(wrapper);

        // Start game button
        int buttonWidth = 240;
        int buttonHeight = 90;
        Image startButtonImage = new Image(ImagePath.START_BUTTON);
        ImageView startButtonImageView = new ImageView(startButtonImage);
        startButtonImageView.setFitWidth(buttonWidth);
        startButtonImageView.setFitHeight(buttonHeight);

        Button startGameButton = new Button();
        startGameButton.setGraphic(startButtonImageView);
        startGameButton.setStyle("-fx-background-color: transparent;");
        startGameButton.setPadding(Insets.EMPTY);

        startGameButton.setOnAction(e -> {
            String sessionName = sessionNameField.getText();
            if (sessionName.length() == 0) {
                // Toaster implementation would need to be adapted for JavaFX
                // toaster.warn(WarningConstants.FILL_SESSION_NAME_WARNING);
                System.out.println(WarningConstants.FILL_SESSION_NAME_WARNING);
            } else {
                close();
                new GameTable(numberOfPlayers, sessionName);
            }
        });

        VBox.setMargin(startGameButton, new Insets(0, 0, 10, 0));
        mainContainer.getChildren().add(startGameButton);

        // Initialize toaster (would need JavaFX implementation)
        // toaster = new Toaster(mainPanel);

        mainPanel.getChildren().add(mainContainer);

        Scene scene = new Scene(mainPanel);
        setScene(scene);
        show();
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

    @Override
    void initializeFrame() {

    }
}