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
import coreapp.util.constants.FontConstants;
import coreapp.util.constants.ImagePath;
import coreapp.util.constants.UITexts;
import coreapp.util.constants.WarningConstants;
import coreapp.util.constants.WindowConstants;
import coreapp.util.ui.UIUtils;
import coreapp.util.ui.toaster.Toaster;

/**
 * The NumberOfPlayersView class represents a stage for selecting the number of
 * players in a game (JavaFX version).
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

    @Override
    void initializeStage() {
        mainPanel = createGradientPanel();
        toaster = new Toaster(mainPanel);
        
        VBox mainContainer = new VBox(10);
        mainContainer.setAlignment(Pos.CENTER);

        // Play against bots label with back button
        Label playAgainstBotsLabel = new Label(UITexts.PLAY_AGAINST_BOTS.toUpperCase());
        playAgainstBotsLabel.setFont(Font.font(customFont.getFamily(), 50));
        playAgainstBotsLabel.setTextFill(Color.CYAN);
        playAgainstBotsLabel.setAlignment(Pos.CENTER);

        Image backImage = new Image(ImagePath.BACK_ICON);
        ImageView backImageView = new ImageView(backImage);
        backImageView.setFitWidth(50);
        backImageView.setFitHeight(50);

        Button goBackButton = new Button();
        goBackButton.setGraphic(backImageView);
        goBackButton.setStyle("-fx-background-color: transparent;");
        goBackButton.setPrefSize(50, 50);
        goBackButton.setCursor(Cursor.HAND);

        goBackButton.setOnAction(e -> {
            dispose();
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
        numberOfBotsTextLabel.setFont(Font.font(customFont.getFamily(), 30));
        numberOfBotsTextLabel.setTextFill(Color.WHITE);
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
        decreaseButton.setCursor(Cursor.HAND);

        decreaseButton.setOnAction(e -> {
            if (numberOfPlayers > 2) {
                numberOfPlayers--;
                numberOfPlayersLabel.setText(String.valueOf(numberOfPlayers));
            }
        });
        thirdPanel.getChildren().add(decreaseButton);

        numberOfPlayersLabel = new Label(String.valueOf(numberOfPlayers));
        numberOfPlayersLabel.setFont(Font.font(customFont.getFamily(), 23));
        numberOfPlayersLabel.setTextFill(Color.WHITE);
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
        increaseButton.setCursor(Cursor.HAND);

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
        nameOfSessionLabel.setFont(Font.font(customFont.getFamily(), 30));
        nameOfSessionLabel.setTextFill(Color.WHITE);
        nameOfSessionLabel.setAlignment(Pos.CENTER);
        nameOfSessionLabel.setMaxWidth(Double.MAX_VALUE);
        mainContainer.getChildren().add(nameOfSessionLabel);

        // Session name field
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER);
        TextField sessionNameField = new TextField();
        sessionNameField.setFont(Font.font(customFont.getFamily(), 23));
        sessionNameField.setStyle(
            "-fx-text-fill: white;" +
            "-fx-background-color: rgba(255, 255, 255, 0.2);" +
            "-fx-border-color: white;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );
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
        startGameButton.setCursor(Cursor.HAND);

        startGameButton.setOnAction(e -> {
            String sessionName = sessionNameField.getText();
            if (sessionName.length() == 0) {
                toaster.warn(WarningConstants.FILL_SESSION_NAME_WARNING);
            } else {
                dispose();
                new GameTable(numberOfPlayers, sessionName);
            }
        });

        VBox.setMargin(startGameButton, new Insets(0, 0, 10, 0));
        mainContainer.getChildren().add(startGameButton);

        mainPanel.getChildren().add(mainContainer);

        Scene scene = new Scene(mainPanel, WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        setScene(scene);
        show();
    }

    private Pane createGradientPanel() {
        Pane panel = new Pane();
        panel.setPrefSize(WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        
        // Create gradient background
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