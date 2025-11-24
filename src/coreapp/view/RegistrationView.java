package coreapp.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import coreapp.data.UserRepository;
import coreapp.data.UserStatisticRepository;
import coreapp.model.user.User;
import coreapp.model.user.UserStatistic;
import coreapp.util.constants.*;
import coreapp.util.constants.WindowConstants;
import coreapp.util.helpers.Logger;
import coreapp.util.session.CurrentUserManager;
import coreapp.util.ui.UIUtils;
import coreapp.util.ui.toaster.Toaster;
import coreapp.view.CustomComponents.HyperlinkText;
import coreapp.view.CustomComponents.TextField;
import coreapp.view.CustomComponents.TextFieldPassword;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * The registration page view of the application in JavaFX.
 * This class represents the graphical user interface for the registration functionality.
 */
public class RegistrationView extends BaseFrame {

    /**
     * The toaster object for displaying notifications.
     */
    private Toaster toaster;

    /**
     * The main Pane of the registration page.
     */
    private Pane mainPane;

    /**
     * The password field for the registration page.
     */
    private TextFieldPassword passwordField;

    /**
     * The username field for the registration page.
     */
    private TextField usernameField;

    /**
     * The email field for the registration page.
     */
    private TextField emailField;

    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Constructs a new RegistrationView.
     */
    public RegistrationView() {
        super(WindowConstants.REGISTRATION_WINDOW_TITLE);
        initializeFrame();
    }

    @Override
    void initializeStage() {

    }

    /**
     * Initializes the frame components.
     */
    @Override
    public void initializeFrame() {
        mainPane = getMainPane();
        this.toaster = new Toaster(mainPane);
        addLogo();
        addRegistrationText();
        addSeparator();
        addEmailTextField();
        addUsernameTextField();
        addPasswordTextField();
        addRegisterButton();
        addLoginButton();

        Scene scene = new Scene(mainPane, WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        scene.setFill(Color.TRANSPARENT);

        this.setScene(scene);
        this.initStyle(StageStyle.TRANSPARENT);
        this.setResizable(false);

        this.show();

        mainPane.requestFocus();
    }

    /**
     * Creates the main panel for the registration page.
     *
     * @return The main Pane for the registration page.
     */
    private Pane getMainPane() {
        Pane pane = new Pane();
        pane.setPrefSize(WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        pane.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);");
        pane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        pane.setOnMouseDragged(event -> {
            this.setX(event.getScreenX() - xOffset);
            this.setY(event.getScreenY() - yOffset);
        });
        this.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        return pane;
    }
    /**
     * Adds the registration text label to the registration page.
     */
    private void addRegistrationText() {
        Label registrationLabel = new Label(UITexts.REGISTRATION_PAGE_TEXT);
        registrationLabel.setFont(Font.font(UIFonts.FONT_GENERAL_UI.getFamily(), UIFonts.FONT_GENERAL_UI.getSize()));
        registrationLabel.setTextFill(UIColors.OFFWHITE_FX);
        registrationLabel.setAlignment(Pos.CENTER);
        registrationLabel.setLayoutX(670);
        registrationLabel.setLayoutY(155);
        registrationLabel.setPrefSize(300, 50);
        mainPane.getChildren().add(registrationLabel);
    }

    /**
     * Adds the separator to the registration page.
     */
    private void addSeparator() {
        Line separator = new Line();
        separator.setStartX(640);
        separator.setStartY(154);
        separator.setEndX(640);
        separator.setEndY(494);
        separator.setStroke(UIColors.COLOR_OUTLINE_FX);
        mainPane.getChildren().add(separator);
    }

    /**
     * Adds the logo to the registration page.
     */
    private void addLogo() {
        try {
            Image backgroundImage = new Image(new File(ImagePath.LOGO_PATH).toURI().toString());

            int originalWidth = (int) backgroundImage.getWidth();
            int originalHeight = (int) backgroundImage.getHeight();

            int targetWidth = 500;
            int targetHeight = 250;

            int newWidth = originalWidth;
            int newHeight = originalHeight;

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
            imageView.setFocusTraversable(false);

            mainPane.getChildren().add(imageView);
        } catch (Exception e) {
            Logger.log(ErrorConstants.BACKGROUND_IMAGE_ERROR, FileConstants.ERROR_LOGS_FILE_PATH);
        }
    }

    /**
     * Adds the email text field to the registration page.
     */
    private void addEmailTextField() {
        TextField emailField = new TextField();

        emailField.setLayoutX(670);
        emailField.setLayoutY(204);
        emailField.setPrefSize(300, 50);

        emailField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (emailField.getText().equals(UITexts.PLACEHOLDER_TEXT_EMAIL)) {
                    emailField.setText(UITexts.STRING_EMPTY);
                }
                emailField.setStyle("-fx-text-fill: " + toHexString(UIColors.OFFBLACK_FX) + ";");
                emailField.setBorderColor(UIColors.COLOR_INTERACTIVE_FX);
            } else {
                if (emailField.getText().isEmpty()) {
                    emailField.setText(UITexts.PLACEHOLDER_TEXT_EMAIL);
                }
                emailField.setStyle("-fx-text-fill: " + toHexString(UIColors.COLOR_OUTLINE_FX) + ";");
                emailField.setBorderColor(UIColors.COLOR_OUTLINE_FX);
            }
        });

        emailField.setStyle("-fx-text-fill: " + toHexString(UIColors.COLOR_OUTLINE_FX) + ";");
        emailField.setText(UITexts.PLACEHOLDER_TEXT_EMAIL);
        emailField.setBorderColor(UIColors.COLOR_OUTLINE_FX);

        mainPane.getChildren().add(emailField);

        this.emailField = emailField;
    }

    /**
     * Adds the username text field to the registration page.
     */
    private void addUsernameTextField() {
        TextField usernameField = new TextField();

        usernameField.setLayoutX(670);
        usernameField.setLayoutY(269);
        usernameField.setPrefSize(300, 50);

        usernameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (usernameField.getText().equals(UITexts.PLACEHOLDER_TEXT_USERNAME)) {
                    usernameField.setText(UITexts.STRING_EMPTY);
                }
                usernameField.setStyle("-fx-text-fill: " + toHexString(UIColors.OFFBLACK_FX) + ";");
                usernameField.setBorderColor(UIColors.COLOR_INTERACTIVE_FX);
            } else {
                if (usernameField.getText().isEmpty()) {
                    usernameField.setText(UITexts.PLACEHOLDER_TEXT_USERNAME);
                }
                usernameField.setStyle("-fx-text-fill: " + toHexString(UIColors.COLOR_OUTLINE_FX) + ";");
                usernameField.setBorderColor(UIColors.COLOR_OUTLINE_FX);
            }
        });

        usernameField.setStyle("-fx-text-fill: " + toHexString(UIColors.COLOR_OUTLINE_FX) + ";");
        usernameField.setText(UITexts.PLACEHOLDER_TEXT_USERNAME);
        usernameField.setBorderColor(UIColors.COLOR_OUTLINE_FX);

        mainPane.getChildren().add(usernameField);

        this.usernameField = usernameField;
    }

    /**
     * Adds the password text field to the registration page.
     */
    private void addPasswordTextField() {
        TextFieldPassword passwordField = new TextFieldPassword();

        passwordField.setLayoutX(670);
        passwordField.setLayoutY(334);
        passwordField.setPrefSize(300, 50);
        passwordField.setText(UITexts.PLACEHOLDER_TEXT_PASSWORD);

        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (passwordField.getText().equals(UITexts.PLACEHOLDER_TEXT_PASSWORD)) {
                    passwordField.setText(UITexts.STRING_EMPTY);
                }
                passwordField.setTextFill(UIColors.OFFBLACK_FX);
                passwordField.setBorderColor(UIColors.COLOR_INTERACTIVE_FX);
                passwordField.setMaskText(true);
            } else {
                if (passwordField.getText().isEmpty()) {
                    passwordField.setText(UITexts.PLACEHOLDER_TEXT_PASSWORD);
                    passwordField.setMaskText(false);
                }
                passwordField.setTextFill(UIColors.COLOR_OUTLINE_FX);
                passwordField.setBorderColor(UIColors.COLOR_OUTLINE_FX);
            }
        });

        passwordField.setOnKeyPressed(event -> {
            passwordField.setMaskText(true);

            if (event.getCode().toString().equals("ENTER")) {
                registerEventHandler();
            }
        });

        passwordField.setText(UITexts.PLACEHOLDER_TEXT_PASSWORD);
        passwordField.setMaskText(false);
        passwordField.setTextFill(UIColors.COLOR_OUTLINE_FX);
        passwordField.setBorderColor(UIColors.COLOR_OUTLINE_FX);

        mainPane.getChildren().add(passwordField);

        this.passwordField = passwordField;
    }

    /**
     * Adds the register button to the registration page.
     */
    private void addRegisterButton() {
        Button registerButton = new Button(UITexts.BUTTON_TEXT_REGISTER);

        registerButton.setLayoutX(670);
        registerButton.setLayoutY(403);
        registerButton.setPrefSize(300, 50);

        registerButton.setStyle("-fx-background-color: " + toHexString(UIColors.COLOR_INTERACTIVE_FX) + "; " +
                "-fx-background-radius: " + UIUtils.ROUNDNESS + "; " +
                "-fx-text-fill: white; " +
                "-fx-font: " + UIFonts.FONT_GENERAL_UI.getSize() + " '" + UIFonts.FONT_GENERAL_UI.getFamily() + "';");

        registerButton.setOnMousePressed(event -> {
            registerEventHandler();
        });

        registerButton.setOnMouseEntered(event -> {
            registerButton.setStyle("-fx-background-color: " + toHexString(UIColors.COLOR_INTERACTIVE_DARKER_FX) + "; " +
                    "-fx-background-radius: " + UIUtils.ROUNDNESS + "; " +
                    "-fx-text-fill: " + toHexString(UIColors.OFFWHITE_FX) + "; " +
                    "-fx-font: " + UIFonts.FONT_GENERAL_UI.getSize() + " '" + UIFonts.FONT_GENERAL_UI.getFamily() + "';");
            registerButton.setCursor(Cursor.HAND);
        });

        registerButton.setOnMouseExited(event -> {
            registerButton.setStyle("-fx-background-color: " + toHexString(UIColors.COLOR_INTERACTIVE_FX) + "; " +
                    "-fx-background-radius: " + UIUtils.ROUNDNESS + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-font: " + UIFonts.FONT_GENERAL_UI.getSize() + " '" + UIFonts.FONT_GENERAL_UI.getFamily() + "';");
            registerButton.setCursor(Cursor.DEFAULT);
        });

        mainPane.getChildren().add(registerButton);
    }

    /**
     * Adds the login link to the registration page.
     */
    private void addLoginButton() {
        HyperlinkText loginLink = new HyperlinkText(UITexts.BUTTON_TEXT_LOGIN, 940, 469, () -> {
            try {
                this.close();
                new LoginPageView();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

        loginLink.setTextFill(UIColors.OFFWHITE_FX);

        loginLink.setOnMouseEntered(event -> {
            loginLink.setTextFill(UIColors.COLOR_INTERACTIVE_FX);
        });

        loginLink.setOnMouseExited(event -> {
        });

        mainPane.getChildren().add(loginLink);
    }

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    /**
     * Handles the registration event.
     */
    private void registerEventHandler() {
        try {
            var email = emailField.getText().trim();
            var username = usernameField.getText().trim();
            var password = passwordField.getText().trim();

            if (email.equals(UITexts.PLACEHOLDER_TEXT_EMAIL)
                    || username.equals(UITexts.PLACEHOLDER_TEXT_USERNAME)
                    || password.equals(UITexts.BUTTON_TEXT_FORGOT_PASS)) {
                toaster.warn(WarningConstants.FILL_INPUTS_WARNING);
                return;
            }

            if (email.isEmpty() ||
                    username.isEmpty() ||
                    password.isEmpty()) {
                toaster.error(ErrorConstants.EMPTY_FIELD);
                return;
            }

            if (!Pattern.matches(EMAIL_REGEX, email)) {
                toaster.error(ErrorConstants.INVALID_EMAIL_FORMAT);
                return;
            }

            if (email.contains(FileConstants.USER_DATA_SEPARATOR)
                    || username.contains(FileConstants.USER_DATA_SEPARATOR)
                    || password.contains(FileConstants.USER_DATA_SEPARATOR)) {
                toaster.error(ErrorConstants.INVALID_CHARACTER);
                return;
            }

            if (UserRepository.emailExists(email)) {
                toaster.error(ErrorConstants.EMAIL_USED);
                return;
            }

            if (UserRepository.usernameTaken(username)) {
                toaster.error(ErrorConstants.USERNAME_TAKEN);
                return;
            }

            var newUser = new User(username, email, password);
            UserRepository.addUser(newUser);
            CurrentUserManager.getInstance().setCurrentUser(newUser);

            var newUserStatistic = new UserStatistic(newUser.getId());
            UserStatisticRepository.addUserStatistic(newUserStatistic);

            toaster.success(UITexts.WELCOME);

            this.close();

            new LeaderboardView();
        } catch (IOException e) {
            toaster.error(ErrorConstants.UNKNOWN_ERROR);
            Logger.log(e.getMessage(), FileConstants.ERROR_LOGS_FILE_PATH);
        }
    }

    /**
     * Converts Color to hex string for CSS
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}