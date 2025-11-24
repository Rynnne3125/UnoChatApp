package coreapp.view;

import coreapp.data.UserRepository;
import coreapp.model.user.User;
import coreapp.util.constants.*;
import coreapp.util.helpers.Logger;
import coreapp.util.session.CurrentUserManager;
import coreapp.util.ui.UIUtils;
import coreapp.util.ui.toaster.Toaster;
import coreapp.view.CustomComponents.GradientPanel;
import coreapp.view.CustomComponents.HyperlinkText;
import coreapp.view.CustomComponents.TextField;
import coreapp.view.CustomComponents.TextFieldPassword;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * The login page view of the application. This class represents the graphical
 * user interface for the login functionality.
 */
public class LoginPageView extends BaseFrame {

    /**
     * The toaster object for displaying notifications.
     */
    private final Toaster toaster;

    /**
     * The main JPanel of the login page.
     */
    Pane mainJPanel = getMainJPanel();

    /**
     * The password field for the login page.
     */
    private TextFieldPassword passwordField;

    /**
     * The username field for the login page.
     */
    private TextField usernameField;

    /**
     * Constructs a new LoginPageView.
     *
     */
    public LoginPageView() {
        super(WindowConstants.LOGIN_WINDOW_TITLE);
        toaster = new Toaster(mainJPanel);
        initializeFrame();
    }

    /**
     * Initializes the frame components.
     */
    @Override
    public void initializeFrame() {
        addLogo();
        addLoginText();
        addSeparator();
        addUsernameTextField();
        addPasswordTextField();
        addLoginButton();
        addForgotPasswordButton();
        addRegisterButton();

        Scene scene = new Scene(mainJPanel, WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        getStage().setScene(scene);
        getStage().show();

        Platform.runLater(() -> mainJPanel.requestFocus());
    }

    /**
     * Creates the main panel for the login page.
     *
     * @return The main Pane for the login page.
     */
    private GradientPanel getMainJPanel() {
        GradientPanel panel1 = new GradientPanel();

        panel1.setPrefSize(WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);

        panel1.setOnMousePressed(e -> {
            getStage().getScene().setUserData(new double[]{e.getScreenX(), e.getScreenY()});
        });

        panel1.setOnMouseDragged(e -> {
            double[] last = (double[]) getStage().getScene().getUserData();
            getStage().setX(getStage().getX() + e.getScreenX() - last[0]);
            getStage().setY(getStage().getY() + e.getScreenY() - last[1]);
            last[0] = e.getScreenX();
            last[1] = e.getScreenY();
        });

        getStage().setOnCloseRequest((WindowEvent e) -> Platform.exit());

        return panel1;
    }

    /**
     * Adds the login text label to the login page.
     */
    private void addLoginText() {
        Label loginLabel = new Label(UITexts.LOGIN_PAGE_TEXT);
        loginLabel.setFont(UIFonts.FONT_GENERAL_UI);
        loginLabel.setTextFill(UIColors.OFFWHITE);
        loginLabel.setAlignment(Pos.CENTER);
        loginLabel.setLayoutX(670);
        loginLabel.setLayoutY(190);
        loginLabel.setPrefSize(300, 50);
        mainJPanel.getChildren().add(loginLabel);
    }

    /**
     * Adds the separator to the login page.
     */
    private void addSeparator() {
        Separator separator1 = new Separator();
        separator1.setLayoutX(640);
        separator1.setLayoutY(184);
        separator1.setPrefHeight(300);
        separator1.setStyle("-fx-background-color: " + UIColors.COLOR_OUTLINE.toString().replace("0x", "#") + ";");
        mainJPanel.getChildren().add(separator1);
    }

    /**
     * Adds the logo to the login page.
     */
    private void addLogo() {
        try {
            Image backgroundImage = new Image(new FileInputStream(ImagePath.LOGO_PATH));
            double originalWidth = backgroundImage.getWidth();
            double originalHeight = backgroundImage.getHeight();

            double targetWidth = 450;
            double targetHeight = 250;

            double newWidth = originalWidth;
            double newHeight = originalHeight;

            if (originalWidth > targetWidth) {
                newWidth = targetWidth;
                newHeight = (newWidth * originalHeight) / originalWidth;
            }

            if (newHeight > targetHeight) {
                newHeight = targetHeight;
                newWidth = (newHeight * originalWidth) / originalHeight;
            }

            ImageView imageView = new ImageView(backgroundImage);
            imageView.setFitWidth(newWidth);
            imageView.setFitHeight(newHeight);
            imageView.setLayoutX(250);
            imageView.setLayoutY(215);
            mainJPanel.getChildren().add(imageView);

        } catch (IOException e) {
            Logger.log(ErrorConstants.BACKGROUND_IMAGE_ERROR, FileConstants.ERROR_LOGS_FILE_PATH);
        }
    }

    /**
     * Adds the username text field to the login page.
     */
    private void addUsernameTextField() {
        TextField usernameField = new TextField();

        usernameField.setLayoutX(670);
        usernameField.setLayoutY(239);
        usernameField.setPrefSize(300, 50);
        usernameField.setText(UITexts.STRING_EMPTY);

        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (usernameField.getText().equals(UITexts.PLACEHOLDER_TEXT_USERNAME)) {
                    usernameField.setText(UITexts.STRING_EMPTY);
                }
                usernameField.setForeground(UIColors.OFFBLACK);
                usernameField.setBorderColor(UIColors.COLOR_INTERACTIVE);
            } else {
                if (usernameField.getText().isEmpty()) {
                    usernameField.setText(UITexts.PLACEHOLDER_TEXT_USERNAME);
                }
                usernameField.setForeground(UIColors.COLOR_OUTLINE);
                usernameField.setBorderColor(UIColors.COLOR_OUTLINE);
            }
        });

        usernameField.setForeground(UIColors.COLOR_OUTLINE);
        usernameField.setText(UITexts.PLACEHOLDER_TEXT_USERNAME);
        usernameField.setBorderColor(UIColors.COLOR_OUTLINE);

        mainJPanel.getChildren().add(usernameField);
        this.usernameField = usernameField;
    }

    /**
     * Adds the password text field to the login page.
     */
    private void addPasswordTextField() {
        TextFieldPassword passwordField = new TextFieldPassword();
        passwordField.setLayoutX(670);
        passwordField.setLayoutY(304);
        passwordField.setPrefSize(300, 50);
        passwordField.setText(UITexts.PLACEHOLDER_TEXT_PASSWORD);

        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (passwordField.getText().equals(UITexts.PLACEHOLDER_TEXT_PASSWORD)) {
                    passwordField.setText(UITexts.STRING_EMPTY);
                }
                passwordField.setForeground(UIColors.OFFBLACK);
                passwordField.setBorderColor(UIColors.COLOR_INTERACTIVE);
            } else {
                if (passwordField.getText().equals(UITexts.STRING_EMPTY))
                    passwordField.setText(UITexts.PLACEHOLDER_TEXT_PASSWORD);

                if (passwordField.getText().equals(UITexts.PLACEHOLDER_TEXT_PASSWORD))
                    passwordField.setEchoChar((char) 0);
                else
                    passwordField.setEchoChar('*');

                passwordField.setForeground(UIColors.COLOR_OUTLINE);
                passwordField.setBorderColor(UIColors.COLOR_OUTLINE);
            }
        });

        passwordField.setOnKeyTyped((KeyEvent e) -> {
            passwordField.setEchoChar('*');
            if (e.getCharacter().equals("\r") || e.getCode() == KeyCode.ENTER) {
                loginEventHandler();
            }
        });

        passwordField.setText(UITexts.PLACEHOLDER_TEXT_PASSWORD);
        passwordField.setEchoChar((char) 0);
        passwordField.setForeground(UIColors.COLOR_OUTLINE);
        passwordField.setBorderColor(UIColors.COLOR_OUTLINE);

        mainJPanel.getChildren().add(passwordField);
        this.passwordField = passwordField;
    }

    /**
     * Adds the login button to the login page.
     */
    private void addLoginButton() {
        Label loginButton = new Label(UITexts.BUTTON_TEXT_LOGIN);
        loginButton.setLayoutX(670);
        loginButton.setLayoutY(373);
        loginButton.setPrefSize(300, 50);
        loginButton.setFont(UIFonts.FONT_GENERAL_UI);
        loginButton.setTextFill(Color.WHITE);
        loginButton.setStyle("-fx-background-color: " + UIColors.COLOR_INTERACTIVE.toString().replace("0x", "#") +
                "; -fx-alignment: center; -fx-border-radius: " + UIUtils.ROUNDNESS +
                "; -fx-background-radius: " + UIUtils.ROUNDNESS + ";");
        loginButton.setOnMouseClicked(e -> loginEventHandler());
        mainJPanel.getChildren().add(loginButton);
    }

    /**
     * Adds the forgot password button to the login page.
     */
    private void addForgotPasswordButton() {
        HyperlinkText forgotPasswordLink = new HyperlinkText(UITexts.BUTTON_TEXT_FORGOT_PASS, 670, 439, () -> {
            toaster.error("There is nothing I can do.");
        });
        mainJPanel.getChildren().add(forgotPasswordLink);
    }

    /**
     * Adds the register button to the login page.
     */
    private void addRegisterButton() {
        HyperlinkText registerLink = new HyperlinkText(UITexts.BUTTON_TEXT_REGISTER, 925, 439, () -> {
            try {
                this.dispose();
                new RegistrationView();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        mainJPanel.getChildren().add(registerLink);
    }

    /**
     * Handles the login event.
     */
    private void loginEventHandler() {
        try {
            var username = usernameField.getText().trim();
            var password = new String(passwordField.getPassword()).trim();

            if (username.equals(UITexts.STRING_EMPTY) || username.equals(UITexts.PLACEHOLDER_TEXT_USERNAME)
                    || password.equals(UITexts.STRING_EMPTY) || password.equals(UITexts.PLACEHOLDER_TEXT_PASSWORD)) {
                toaster.warn(WarningConstants.FILL_INPUTS_WARNING);
                return;
            }

            var users = UserRepository.getUsers();
            boolean userExists = false;

            User currentUser = null;
            for (User user : users) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    currentUser = user;
                    CurrentUserManager.getInstance().setCurrentUser(currentUser);
                    userExists = true;
                    toaster.success(UITexts.WELCOME);

                    this.dispose();
                    new LeaderboardView();
                    break;
                }
            }

            if (!userExists) {
                toaster.error(ErrorConstants.USER_DOES_NOT_EXIST);
            }
        } catch (IOException e) {
            toaster.error(ErrorConstants.UNKNOWN_ERROR);
            Logger.log(e.getMessage(), FileConstants.ERROR_LOGS_FILE_PATH);
        }
    }
}
