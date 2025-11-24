package coreapp.view;

import coreapp.data.UserStatisticRepository;
import coreapp.model.user.User;
import coreapp.model.user.UserStatistic;
import coreapp.util.constants.FileConstants;
import coreapp.util.constants.FontConstants;
import coreapp.util.constants.ImagePath;
import coreapp.util.constants.UITexts;
import coreapp.util.constants.WindowConstants;
import coreapp.util.helpers.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class UserProfileView {

    private Stage stage;
    private User user;
    private UserStatistic userStatistic;
    private Class<?> previousPage;
    private Font customFont;

    public UserProfileView(User user, Class<?> previousPage) {
        this.user = user;
        this.previousPage = previousPage;

        try {
            this.userStatistic = UserStatisticRepository.getUserStatisticById(user.getId());
        } catch (IOException e) {
            this.userStatistic = new UserStatistic(user.getId());
            Logger.log(e.getMessage(), FileConstants.ERROR_LOGS_FILE_PATH);
        }

        this.stage = new Stage();
        this.stage.setTitle(WindowConstants.USER_PROFILE_WINDOW_TITLE_PREFIX + user.getUsername());

        this.customFont = Font.loadFont(
                getClass().getResourceAsStream(FontConstants.RechargeFontPath), 20
        );

        initializeFrame();
    }

    private void initializeFrame() {

        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(20));
        mainPane.setStyle("-fx-background-color: linear-gradient(to bottom, #001133, #003366);");

        /* ================================================================
           USER IMAGE
        ================================================================= */
        Image img = new Image(ImagePath.DEFAULT_USER_PROFILE);
        ImageView userImage = new ImageView(img);
        userImage.setFitWidth(300);
        userImage.setFitHeight(300);
        userImage.setPreserveRatio(true);

        StackPane imagePane = new StackPane(userImage);
        imagePane.setPadding(new Insets(10));

        mainPane.setCenter(imagePane);

        /* ================================================================
           USER INFO TEXT
        ================================================================= */
        Label username = makeLabel(UITexts.USER_DETAILS_USERNAME + user.getUsername());
        Label email = makeLabel(UITexts.USER_DETAILS_EMAIL + user.getEmail());
        Label gamesPlayed = makeLabel(UITexts.USER_DETAILS_GAMES_PLAYED + userStatistic.getNumberOfGamesPlayed());
        Label wins = makeLabel(UITexts.USER_DETAILS_WINS + userStatistic.getNumberOfWins());
        Label losses = makeLabel(UITexts.USER_DETAILS_LOSSES + userStatistic.getNumberOfLosses());
        Label totalScore = makeLabel(UITexts.USER_DETAILS_TOTAL_SCORE + userStatistic.getTotalScore());
        Label avgScore = makeLabel(UITexts.USER_DETAILS_AVERAGE_SCORE + String.format("%.2f", userStatistic.getAverageScore()));
        Label ratio = makeLabel(UITexts.USER_DETAILS_WIN_LOSS_RATIO + String.format("%.2f", userStatistic.getWinLossRatio()));

        VBox infoBox = new VBox(
                username, email, gamesPlayed, wins, losses, totalScore, avgScore, ratio
        );
        infoBox.setSpacing(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(0, 0, 0, 50));

        mainPane.setRight(infoBox);

        /* ================================================================
           TITLE PANEL (NORTH)
        ================================================================= */
        Label title = new Label(UITexts.USER_DETAILS_TITLE.toUpperCase());
        title.setFont(Font.font(customFont.getFamily(), 30));
        title.setTextFill(Paint.valueOf("cyan"));

        HBox topPane = new HBox(title);
        topPane.setAlignment(Pos.CENTER);
        topPane.setPadding(new Insets(10));
        topPane.setStyle("-fx-background-color: #00008B;");

        mainPane.setTop(topPane);

        /* ================================================================
           BACK BUTTON (WEST)
        ================================================================= */
        Image backImg = new Image(ImagePath.BACK_ICON);
        ImageView backIcon = new ImageView(backImg);
        backIcon.setFitWidth(50);
        backIcon.setFitHeight(50);

        Button backButton = new Button();
        backButton.setGraphic(backIcon);
        backButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        backButton.setOnAction(e -> goBack());

        VBox btnWrap = new VBox(backButton);
        btnWrap.setPadding(new Insets(10));
        btnWrap.setAlignment(Pos.TOP_LEFT);

        mainPane.setLeft(btnWrap);

        /* ================================================================
           BOTTOM PANEL
        ================================================================= */
        HBox south = new HBox();
        south.setPrefHeight(50);
        south.setStyle("-fx-background-color: #00008B;");
        mainPane.setBottom(south);

        /* ================================================================
           SHOW WINDOW
        ================================================================= */
        Scene scene = new Scene(mainPane, 1200, 800);
        stage.setScene(scene);
        stage.show();
    }

    private Label makeLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font(customFont.getFamily(), 21));
        lbl.setTextFill(Paint.valueOf("white"));
        return lbl;
    }

    private void goBack() {
        stage.close();
        try {
            previousPage.getConstructor().newInstance();
        } catch (Exception e) {
            Logger.log(e.getMessage(), FileConstants.ERROR_LOGS_FILE_PATH);
        }
    }
}
