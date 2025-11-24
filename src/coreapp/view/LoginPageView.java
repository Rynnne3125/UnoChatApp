package coreapp.view;

import coreapp.data.UserRepository;
import coreapp.model.user.User;
import coreapp.util.constants.*;
import coreapp.util.helpers.Logger;
import coreapp.util.session.CurrentUserManager;
import coreapp.util.ui.UIUtils;
import coreapp.util.ui.toaster.Toaster;
import coreapp.view.CustomComponents.HyperlinkText;
import coreapp.view.CustomComponents.TextField;
import coreapp.view.CustomComponents.TextFieldPassword;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;

/**
 * The login page view of the application (JavaFX version).
 * This class represents the graphical user interface for the login functionality.
 */
public class LoginPageView extends BaseFrame {

    private Toaster toaster;
    private Pane mainPane;
    private TextFieldPassword passwordField;
    private TextField usernameField;
    private double xOffset = 0;
    private double yOffset = 0;

    public LoginPageView() {
        super(WindowConstants.LOGIN_WINDOW_TITLE);
        initializeFrame();
    }

    @Override
    public void initializeFrame() {
        mainPane = createMainPane();
        toaster = new Toaster(mainPane);
        addLogo();
        addLoginText();
        addSeparator();
        addUsernameTextField();
        addPasswordTextField();
        addLoginButton();
        addForgotPasswordButton();
        addRegisterButton();

        Scene scene = new Scene(mainPane, WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        
        setScene(scene);
        initStyle(StageStyle.TRANSPARENT);
        setResizable(false);
        show();

        Platform.runLater(() -> mainPane.requestFocus());
    }

    private Pane createMainPane() {
        Pane pane = new Pane();
        pane.setPrefSize(WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);

        // Create gradient background
        Stop[] stops = new Stop[] { 
            new Stop(0, Color.web("#667eea")), 
            new Stop(1, Color.web("#764ba2"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        BackgroundFill bgFill = new BackgroundFill(gradient, null, null);
        pane.setBackground(new Background(bgFill));

        // Make window draggable
        pane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        pane.setOnMouseDragged(event -> {
            setX(event.getScreenX() - xOffset);
            setY(event.getScreenY() - yOffset);
        });

        setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        return pane;
    }

    private void addLoginText() {
        Label loginLabel = new Label(UITexts.LOGIN_PAGE_TEXT);
        loginLabel.setFont(Font.font(UIFonts.FONT_GENERAL_UI.getFamily(), UIFonts.FONT_GENERAL_UI.getSize()));
        loginLabel.setTextFill(UIColors.OFFWHITE_FX);
        loginLabel.setAlignment(Pos.CENTER);
        loginLabel.setLayoutX(670);
        loginLabel.setLayoutY(190);
        loginLabel.setPrefSize(300, 50);
        mainPane.getChildren().add(loginLabel);
    }

    private void addSeparator() {
        Line separator = new Line();
        separator.setStartX(640);
        separator.setStartY(184);
        separator.setEndX(640);
        separator.setEndY(484);
        separator.setStroke(UIColors.COLOR_OUTLINE_FX);
        separator.setStrokeWidth(2);
        mainPane.getChildren().add(separator);
    }

    private void addLogo() {
        try {
            Image backgroundImage = new Image(new File(ImagePath.LOGO_PATH).toURI().toString());
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
            imageView.setFocusTraversable(false);
            mainPane.getChildren().add(imageView);

        } catch (Exception e) {
            Logger.log(ErrorConstants.BACKGROUND_IMAGE_ERROR, FileConstants.ERROR_LOGS_FILE_PATH);
        }
    }

    private void addUsernameTextField() {
        usernameField = new TextField();
        usernameField.setLayoutX(670);
        usernameField.setLayoutY(239);
        usernameField.setPrefSize(300, 50);

        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
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
    }

    private void addPasswordTextField() {
        passwordField = new TextFieldPassword();
        passwordField.setLayoutX(670);
        passwordField.setLayoutY(304);
        passwordField.setPrefSize(300, 50);

        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
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
            if (!passwordField.getText().equals(UITexts.PLACEHOLDER_TEXT_PASSWORD)) {
                passwordField.setMaskText(true);
            }
            if (event.getCode() == KeyCode.ENTER) {
                loginEventHandler();
            }
        });

        passwordField.setText(UITexts.PLACEHOLDER_TEXT_PASSWORD);
        passwordField.setMaskText(false);
        passwordField.setTextFill(UIColors.COLOR_OUTLINE_FX);
        passwordField.setBorderColor(UIColors.COLOR_OUTLINE_FX);

        mainPane.getChildren().add(passwordField);
    }

    private void addLoginButton() {
        Button loginButton = new Button(UITexts.BUTTON_TEXT_LOGIN);
        loginButton.setLayoutX(670);
        loginButton.setLayoutY(373);
        loginButton.setPrefSize(300, 50);
        loginButton.setFont(Font.font(UIFonts.FONT_GENERAL_UI.getFamily(), UIFonts.FONT_GENERAL_UI.getSize()));
        loginButton.setTextFill(Color.WHITE);
        
        loginButton.setStyle(
            "-fx-background-color: " + toHexString(UIColors.COLOR_INTERACTIVE_FX) + ";" +
            "-fx-background-radius: " + UIUtils.ROUNDNESS + ";" +
            "-fx-cursor: hand;"
        );

        loginButton.setOnMouseEntered(e -> {
            loginButton.setStyle(
                "-fx-background-color: " + toHexString(UIColors.COLOR_INTERACTIVE_DARKER_FX) + ";" +
                "-fx-background-radius: " + UIUtils.ROUNDNESS + ";" +
                "-fx-cursor: hand;"
            );
        });

        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle(
                "-fx-background-color: " + toHexString(UIColors.COLOR_INTERACTIVE_FX) + ";" +
                "-fx-background-radius: " + UIUtils.ROUNDNESS + ";" +
                "-fx-cursor: hand;"
            );
        });

        loginButton.setOnAction(e -> loginEventHandler());
        mainPane.getChildren().add(loginButton);
    }

    private void addForgotPasswordButton() {
        HyperlinkText forgotPasswordLink = new HyperlinkText(
            UITexts.BUTTON_TEXT_FORGOT_PASS, 
            670, 
            439, 
            () -> toaster.error("There is nothing I can do.")
        );
        mainPane.getChildren().add(forgotPasswordLink);
    }

    private void addRegisterButton() {
        HyperlinkText registerLink = new HyperlinkText(
            UITexts.BUTTON_TEXT_REGISTER, 
            925, 
            439, 
            () -> {
                try {
                    dispose();
                    new RegistrationView();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        );
        mainPane.getChildren().add(registerLink);
    }

    private void loginEventHandler() {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

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

                    dispose();
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

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    @Override
    void initializeStage() {
        // Not used for this view
    }
}