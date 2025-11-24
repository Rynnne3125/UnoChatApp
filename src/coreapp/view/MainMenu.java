package coreapp.view;

import coreapp.data.UserStatisticRepository;
import coreapp.util.constants.*;
import coreapp.util.helpers.Logger;
import coreapp.util.session.CurrentUserManager;
import coreapp.util.ui.UIUtils;
import coreapp.util.ui.toaster.Toaster;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

import java.io.IOException;

/**
 * Represents the main menu interface of the application (JavaFX version).
 */
public class MainMenu extends BaseFrame {
    
    private Toaster toaster;
    private final Font customFont = UIUtils.loadCustomFont(FontConstants.RechargeFontPath);

    public MainMenu() {
        super(WindowConstants.MAIN_MENU_WINDOW_TITLE_PREFIX);
        initializeFrame();
    }

    @Override
    void initializeFrame() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPrefSize(1200, 800);

        // Create gradient background
        Stop[] stops = new Stop[] { 
            new Stop(0, Color.web("#2c3e50")), 
            new Stop(1, Color.web("#3498db"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        BackgroundFill bgFill = new BackgroundFill(gradient, null, null);
        mainLayout.setBackground(new Background(bgFill));

        // Initialize toaster after mainLayout is created
        toaster = new Toaster(mainLayout);

        // TOP PANEL
        HBox topPanel = createTopPanel();
        mainLayout.setTop(topPanel);

        // MIDDLE PANEL
        HBox middlePanel = createMiddlePanel();
        mainLayout.setCenter(middlePanel);

        // BOTTOM PANEL
        HBox bottomPanel = createBottomPanel();
        mainLayout.setBottom(bottomPanel);

        Scene scene = new Scene(mainLayout, 1200, 800);
        setScene(scene);
        show();
    }

    private HBox createTopPanel() {
        HBox topPanel = new HBox();
        topPanel.setPrefHeight(100);
        topPanel.setPadding(new Insets(10));

        HBox topPanelLeft = createUserInfoSection();
        Region middleSpacer = new Region();
        HBox.setHgrow(middleSpacer, Priority.ALWAYS);
        HBox topPanelRight = createActionButtonsSection();

        topPanel.getChildren().addAll(topPanelLeft, middleSpacer, topPanelRight);
        return topPanel;
    }

    private HBox createUserInfoSection() {
        HBox userInfoSection = new HBox(10);
        userInfoSection.setAlignment(Pos.CENTER_LEFT);

        ImageView avatarImage = new ImageView(new Image(ImagePath.AVATAR));
        avatarImage.setFitWidth(90);
        avatarImage.setFitHeight(90);

        VBox textFieldsPanel = new VBox(5);
        textFieldsPanel.setAlignment(Pos.CENTER_LEFT);

        var currentUser = CurrentUserManager.getInstance().getCurrentUser();
        Label upperTextField = new Label(UITexts.CURRENT_USER + currentUser.getUsername());
        upperTextField.setFont(Font.font(customFont.getFamily(), 17));
        upperTextField.setTextFill(UIColors.OFFWHITE_FX);

        Label lowerTextField = new Label();
        try {
            var userStatistic = UserStatisticRepository.getUserStatisticById(currentUser.getId());
            lowerTextField.setText(UITexts.CURRENT_USER_SCORE.toUpperCase() + userStatistic.getTotalScore());
        } catch (IOException e) {
            Logger.log(e.getMessage(), FileConstants.ERROR_LOGS_FILE_PATH);
            lowerTextField.setText(UITexts.CURRENT_USER_SCORE.toUpperCase() + "0");
        }
        lowerTextField.setFont(Font.font(customFont.getFamily(), 10));
        lowerTextField.setTextFill(UIColors.OFFWHITE_FX);

        textFieldsPanel.getChildren().addAll(upperTextField, lowerTextField);
        userInfoSection.getChildren().addAll(avatarImage, textFieldsPanel);

        return userInfoSection;
    }

    private HBox createActionButtonsSection() {
        HBox actionButtonsSection = new HBox(15);
        actionButtonsSection.setAlignment(Pos.CENTER_RIGHT);
        actionButtonsSection.setPadding(new Insets(0, 25, 0, 0));

        Button menuListButton = createIconButton(ImagePath.MENU_LIST);
        menuListButton.setOnAction(e -> openMenuList());

        Button infoButton = createIconButton(ImagePath.MENU_INFO);
        infoButton.setOnAction(e -> showInformationDialog());

        Button logoutButton = createIconButton(ImagePath.MENU_LOGOUT);
        logoutButton.setOnAction(e -> logout());

        actionButtonsSection.getChildren().addAll(menuListButton, infoButton, logoutButton);
        return actionButtonsSection;
    }

    private Button createIconButton(String iconPath) {
        ImageView icon = new ImageView(new Image(iconPath));
        icon.setFitWidth(50);
        icon.setFitHeight(50);

        Button button = new Button();
        button.setGraphic(icon);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        button.setPrefSize(50, 50);
        button.setCursor(Cursor.HAND);

        return button;
    }

    private HBox createMiddlePanel() {
        HBox middlePanel = new HBox();
        middlePanel.setPadding(new Insets(40, 0, 0, 0));

        VBox leftPanel = new VBox();
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(0, 0, 0, 230));

        ImageView gameImage = new ImageView(new Image(ImagePath.MENU_GAME_ICON));
        gameImage.setFitWidth(400);
        gameImage.setFitHeight(400);
        leftPanel.getChildren().add(gameImage);

        VBox rightPanel = new VBox(20);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPadding(new Insets(100, 100, 0, 0));

        Button playOfflineButton = createImageButton(ImagePath.MENU_PLAY_OFFLINE_BUTTON_IMAGE, 280, 100);
        playOfflineButton.setOnAction(e -> {
            dispose();
            new NumberOfPlayersView();
        });

        Button playOnlineButton = createImageButton(ImagePath.MENU_PLAY_ONLINE_BUTTON_IMAGE, 280, 100);
        playOnlineButton.setOnAction(e -> toaster.warn(UITexts.I_DO_NOT_WORK));

        rightPanel.getChildren().addAll(playOfflineButton, playOnlineButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        middlePanel.getChildren().addAll(leftPanel, spacer, rightPanel);

        return middlePanel;
    }

    private Button createImageButton(String imagePath, double width, double height) {
        ImageView image = new ImageView(new Image(imagePath));
        image.setFitWidth(width);
        image.setFitHeight(height);

        Button button = new Button();
        button.setGraphic(image);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        button.setPrefSize(width, height);
        button.setCursor(Cursor.HAND);

        return button;
    }

    private HBox createBottomPanel() {
        HBox bottomPanel = new HBox();
        bottomPanel.setPrefHeight(100);
        bottomPanel.setPadding(new Insets(10));

        HBox bottomPanelLeft = createMenuButtonsSection();
        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        bottomPanel.getChildren().addAll(bottomPanelLeft, bottomSpacer);
        return bottomPanel;
    }

    private HBox createMenuButtonsSection() {
        HBox menuButtonsSection = new HBox(20);
        menuButtonsSection.setAlignment(Pos.CENTER_LEFT);
        menuButtonsSection.setPadding(new Insets(0, 0, 0, 15));

        Button settingsButton = createMenuButton(
            ImagePath.SETTINGS, 
            UITexts.MENU_BUTTON_SETTINGS.toUpperCase(), 
            210, 70
        );
        settingsButton.setOnAction(e -> openSettingsWindow());

        Button shareButton = createMenuButton(
            ImagePath.SHARE, 
            UITexts.MENU_BUTTON_SHARE.toUpperCase(), 
            210, 70
        );
        shareButton.setOnAction(e -> shareContent());

        Button leaderboardButton = createMenuButton(
            ImagePath.LEADERBOARD, 
            UITexts.MENU_BUTTON_LEADERBOARD.toUpperCase(), 
            250, 70
        );
        leaderboardButton.setOnAction(e -> showLeaderboard());

        menuButtonsSection.getChildren().addAll(settingsButton, shareButton, leaderboardButton);
        return menuButtonsSection;
    }

    private Button createMenuButton(String iconPath, String text, double width, double height) {
        Button button = new Button(text);

        ImageView icon = new ImageView(new Image(iconPath));
        icon.setFitWidth(30);
        icon.setFitHeight(30);
        button.setGraphic(icon);

        button.setPrefSize(width, height);
        button.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: white;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;"
        );

        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(10);
        button.setCursor(Cursor.HAND);

        return button;
    }

    private void logout() {
        CurrentUserManager.getInstance().setCurrentUser(null);
        dispose();
        new LoginPageView();
    }

    private void openMenuList() {
        toaster.warn(UITexts.I_DO_NOT_WORK);
    }

    private void showInformationDialog() {
        toaster.warn(UITexts.I_DO_NOT_WORK);
    }

    private void openSettingsWindow() {
        toaster.warn(UITexts.I_DO_NOT_WORK);
    }

    private void shareContent() {
        toaster.warn(UITexts.I_DO_NOT_WORK);
    }

    private void showLeaderboard() {
        dispose();
        new LeaderboardView();
    }

    @Override
    void initializeStage() {
        // Not used for this view
    }
}