package coreapp.view;

import coreapp.data.UserStatisticRepository;
import coreapp.util.constants.FileConstants;
import coreapp.util.constants.FontConstants;
import coreapp.util.constants.ImagePath;
import coreapp.util.constants.UIColors;
import coreapp.util.constants.UITexts;
import coreapp.util.constants.WindowConstants;
import coreapp.util.helpers.Logger;
import coreapp.util.session.CurrentUserManager;
import coreapp.util.ui.toaster.Toaster;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;

import static jdk.internal.net.http.common.Utils.close;

/**
 * Represents the main menu interface of the application, providing access
 * to various features and functionalities through a user-friendly interface.
 */
public class MainMenu extends BaseFrame {
    /**
     * The toaster object for displaying notifications.
     */
    private final Toaster toaster;

    /**
     * The custom font used in the main menu.
     */
    private final Font customFont = Font.loadFont(getClass().getResourceAsStream(FontConstants.RechargeFontPath), 12);

    /**
     * Constructs a new MainMenu object.
     */
    public MainMenu() {
        super(WindowConstants.MAIN_MENU_WINDOW_TITLE_PREFIX);
        toaster = new Toaster(new Pane()); // Temporary pane, will be replaced
        initializeFrame();
    }

    /**
     * Initializes the graphical components of the main menu.
     */
    @Override
    void initializeFrame() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #3498db);");

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

    /**
     * Creates the top panel with user info and navigation buttons
     */
    private HBox createTopPanel() {
        HBox topPanel = new HBox();
        topPanel.setPrefHeight(100);
        topPanel.setStyle("-fx-background-color: transparent;");
        topPanel.setPadding(new Insets(10));

        // LEFT SECTION - User info
        HBox topPanelLeft = createUserInfoSection();

        // MIDDLE SECTION - Spacer
        Region middleSpacer = new Region();
        HBox.setHgrow(middleSpacer, Priority.ALWAYS);

        // RIGHT SECTION - Action buttons
        HBox topPanelRight = createActionButtonsSection();

        topPanel.getChildren().addAll(topPanelLeft, middleSpacer, topPanelRight);
        return topPanel;
    }

    /**
     * Creates the user info section
     */
    private HBox createUserInfoSection() {
        HBox userInfoSection = new HBox(10);
        userInfoSection.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        ImageView avatarImage = new ImageView(new Image(ImagePath.AVATAR));
        avatarImage.setFitWidth(90);
        avatarImage.setFitHeight(90);

        // User info text
        VBox textFieldsPanel = new VBox(5);
        textFieldsPanel.setAlignment(Pos.CENTER_LEFT);

        var currentUser = CurrentUserManager.getInstance().getCurrentUser();
        Label upperTextField = new Label(UITexts.CURRENT_USER + currentUser.getUsername());
        upperTextField.setFont(Font.font(customFont.getFamily(), 17));
        upperTextField.setTextFill(Color.web(UIColors.OFFWHITE));

        Label lowerTextField = new Label();
        try {
            var userStatistic = UserStatisticRepository.getUserStatisticById(currentUser.getId());
            lowerTextField.setText(UITexts.CURRENT_USER_SCORE.toUpperCase() + userStatistic.getTotalScore());
        } catch (IOException e) {
            Logger.log(e.getMessage(), FileConstants.ERROR_LOGS_FILE_PATH);
            lowerTextField.setText(UITexts.CURRENT_USER_SCORE.toUpperCase() + "0");
        }
        lowerTextField.setFont(Font.font(customFont.getFamily(), 10));
        lowerTextField.setTextFill(Color.web(UIColors.OFFWHITE));

        textFieldsPanel.getChildren().addAll(upperTextField, lowerTextField);
        userInfoSection.getChildren().addAll(avatarImage, textFieldsPanel);

        return userInfoSection;
    }

    /**
     * Creates the action buttons section
     */
    private HBox createActionButtonsSection() {
        HBox actionButtonsSection = new HBox(15);
        actionButtonsSection.setAlignment(Pos.CENTER_RIGHT);
        actionButtonsSection.setPadding(new Insets(0, 25, 0, 0));

        // Menu List Button
        ImageView menuListIcon = new ImageView(new Image(ImagePath.MENU_LIST));
        menuListIcon.setFitWidth(50);
        menuListIcon.setFitHeight(50);
        Button menuListButton = createIconButton(menuListIcon);
        menuListButton.setOnAction(e -> openMenuList());

        // Info Button
        ImageView infoIcon = new ImageView(new Image(ImagePath.MENU_INFO));
        infoIcon.setFitWidth(50);
        infoIcon.setFitHeight(50);
        Button infoButton = createIconButton(infoIcon);
        infoButton.setOnAction(e -> showInformationDialog());

        // Logout Button
        ImageView logoutIcon = new ImageView(new Image(ImagePath.MENU_LOGOUT));
        logoutIcon.setFitWidth(50);
        logoutIcon.setFitHeight(50);
        Button logoutButton = createIconButton(logoutIcon);
        logoutButton.setOnAction(e -> logout());

        actionButtonsSection.getChildren().addAll(menuListButton, infoButton, logoutButton);
        return actionButtonsSection;
    }

    /**
     * Creates an icon button
     */
    private Button createIconButton(ImageView icon) {
        Button button = new Button();
        button.setGraphic(icon);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        button.setPrefSize(50, 50);
        return button;
    }

    /**
     * Creates the middle panel with game image and play buttons
     */
    private HBox createMiddlePanel() {
        HBox middlePanel = new HBox();
        middlePanel.setStyle("-fx-background-color: transparent;");
        middlePanel.setPadding(new Insets(40, 0, 0, 0));

        // LEFT SECTION - Game image
        VBox leftPanel = new VBox();
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(0, 0, 0, 230));
        leftPanel.setStyle("-fx-background-color: transparent;");

        ImageView gameImage = new ImageView(new Image(ImagePath.MENU_GAME_ICON));
        gameImage.setFitWidth(400);
        gameImage.setFitHeight(400);
        leftPanel.getChildren().add(gameImage);

        // RIGHT SECTION - Play buttons
        VBox rightPanel = new VBox(20);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPadding(new Insets(100, 100, 0, 0));
        rightPanel.setStyle("-fx-background-color: transparent;");

        var playButtonWidth = 280;
        var playButtonHeight = 100;

        // Play Offline Button
        ImageView playOfflineIcon = new ImageView(new Image(ImagePath.MENU_PLAY_OFFLINE_BUTTON_IMAGE));
        playOfflineIcon.setFitWidth(playButtonWidth);
        playOfflineIcon.setFitHeight(playButtonHeight);
        Button playOfflineButton = createImageButton(playOfflineIcon);
        playOfflineButton.setOnAction(e -> {
            close();
            new NumberOfPlayersView();
        });

        // Play Online Button
        ImageView playOnlineIcon = new ImageView(new Image(ImagePath.MENU_PLAY_ONLINE_BUTTON_IMAGE));
        playOnlineIcon.setFitWidth(playButtonWidth);
        playOnlineIcon.setFitHeight(playButtonHeight);
        Button playOnlineButton = createImageButton(playOnlineIcon);
        playOnlineButton.setOnAction(e -> toaster.warn(UITexts.I_DO_NOT_WORK));

        rightPanel.getChildren().addAll(playOfflineButton, playOnlineButton);

        // Add panels to middle panel
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        middlePanel.getChildren().addAll(leftPanel, spacer, rightPanel);

        return middlePanel;
    }

    /**
     * Creates an image button
     */
    private Button createImageButton(ImageView image) {
        Button button = new Button();
        button.setGraphic(image);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        button.setPrefSize(image.getFitWidth(), image.getFitHeight());
        return button;
    }

    /**
     * Creates the bottom panel with additional menu buttons
     */
    private HBox createBottomPanel() {
        HBox bottomPanel = new HBox();
        bottomPanel.setPrefHeight(100);
        bottomPanel.setStyle("-fx-background-color: transparent;");
        bottomPanel.setPadding(new Insets(10));

        // LEFT SECTION - Menu buttons
        HBox bottomPanelLeft = createMenuButtonsSection();

        // RIGHT SECTION - Spacer
        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        bottomPanel.getChildren().addAll(bottomPanelLeft, bottomSpacer);
        return bottomPanel;
    }

    /**
     * Creates the menu buttons section
     */
    private HBox createMenuButtonsSection() {
        HBox menuButtonsSection = new HBox(20);
        menuButtonsSection.setAlignment(Pos.CENTER_LEFT);
        menuButtonsSection.setPadding(new Insets(0, 0, 0, 15));

        var buttonWidth = 210;
        var buttonHeight = 70;

        // Settings Button
        Button settingsButton = createMenuButton(
                ImagePath.SETTINGS,
                UITexts.MENU_BUTTON_SETTINGS.toUpperCase(),
                buttonWidth,
                buttonHeight
        );
        settingsButton.setOnAction(e -> openSettingsWindow());

        // Share Button
        Button shareButton = createMenuButton(
                ImagePath.SHARE,
                UITexts.MENU_BUTTON_SHARE.toUpperCase(),
                buttonWidth,
                buttonHeight
        );
        shareButton.setOnAction(e -> shareContent());

        // Leaderboard Button
        Button leaderboardButton = createMenuButton(
                ImagePath.LEADERBOARD,
                UITexts.MENU_BUTTON_LEADERBOARD.toUpperCase(),
                buttonWidth + 40,
                buttonHeight
        );
        leaderboardButton.setOnAction(e -> showLeaderboard());

        menuButtonsSection.getChildren().addAll(settingsButton, shareButton, leaderboardButton);
        return menuButtonsSection;
    }

    /**
     * Creates a menu button with icon and text
     */
    private Button createMenuButton(String iconPath, String text, double width, double height) {
        Button button = new Button(text);

        // Create icon
        ImageView icon = new ImageView(new Image(iconPath));
        icon.setFitWidth(30);
        icon.setFitHeight(30);
        button.setGraphic(icon);

        // Set button style
        button.setPrefSize(width, height);
        button.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: white; " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 1; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px;");

        // Set graphic text gap
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(10);

        return button;
    }

    /**
     * Logs out the current user and navigates to the login page.
     */
    private void logout() {
        CurrentUserManager.getInstance().setCurrentUser(null);
        close();
        new LoginPageView();
    }

    /**
     * Opens the menu list.
     */
    private void openMenuList() {
        toaster.warn(UITexts.I_DO_NOT_WORK);
    }

    /**
     * Displays information dialog.
     */
    private void showInformationDialog() {
        toaster.warn(UITexts.I_DO_NOT_WORK);
    }

    /**
     * Opens the settings window.
     */
    private void openSettingsWindow() {
        toaster.warn(UITexts.I_DO_NOT_WORK);
    }

    /**
     * Shares content.
     */
    private void shareContent() {
        toaster.warn(UITexts.I_DO_NOT_WORK);
    }

    /**
     * Shows the leaderboard.
     */
    private void showLeaderboard() {
        close();
        new LeaderboardView();
    }
}