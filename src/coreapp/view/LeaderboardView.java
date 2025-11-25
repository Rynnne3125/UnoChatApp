package coreapp.view;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import application.Main;
import coreapp.data.UserRepository;
import coreapp.data.UserStatisticRepository;
import coreapp.model.user.User;
import coreapp.model.user.UserStatistic;
import coreapp.util.constants.ErrorConstants;
import coreapp.util.constants.FileConstants;
import coreapp.util.constants.FontConstants;
import coreapp.util.constants.ImagePath;
import coreapp.util.constants.UIColors;
import coreapp.util.constants.UITexts;
import coreapp.util.constants.WindowConstants;
import coreapp.util.helpers.Logger;
import coreapp.util.session.CurrentUserManager;
import coreapp.util.ui.UIUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

/**
 * The leaderboard page view of the application (JavaFX version).
 * This class represents the graphical user interface for the leaderboard functionality.
 */
public class LeaderboardView extends BaseFrame {
    
    private VBox leaderboardPanel;
    private final Font customFont = UIUtils.loadCustomFont(FontConstants.RechargeFontPath);

    public LeaderboardView() {
        super(WindowConstants.LEADERBOARD_WINDOW_TITLE);
        initializeFrame();
    }

    @Override
    void initializeFrame() {
        BorderPane backgroundPanel = new BorderPane();
        backgroundPanel.setPrefSize(WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);

        // Create gradient background
        Stop[] stops = new Stop[] { 
            new Stop(0, Color.web("#1488CC")), 
            new Stop(1, Color.web("#2B32B2"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        BackgroundFill bgFill = new BackgroundFill(gradient, null, null);
        backgroundPanel.setBackground(new Background(bgFill));

        leaderboardPanel = new VBox();

        // Create title panel with back button
        HBox titlePanel = new HBox(20);
        titlePanel.setPadding(new Insets(10, 20, 10, 20));
        titlePanel.setAlignment(Pos.CENTER_LEFT);

        // Back button
        Image backIcon = new Image(ImagePath.CLOSE_ICON);
        ImageView backImageView = new ImageView(backIcon);
        backImageView.setFitWidth(50);
        backImageView.setFitHeight(50);

        Button backButton = new Button();
        backButton.setGraphic(backImageView);
        backButton.setStyle("-fx-background-color: transparent;");
        backButton.setPrefSize(50, 50);
        backButton.setOnAction(e -> {
            dispose();
            new Main();
        });

        // Title label
        Label titleLabel = new Label(UITexts.LEADERBOARD_HEADLINE.toUpperCase());
        titleLabel.setFont(Font.font(customFont.getFamily(), 32));
        titleLabel.setTextFill(UIColors.OFFWHITE_FX);
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 370));

        titlePanel.getChildren().addAll(backButton, titleLabel);
        backgroundPanel.setTop(titlePanel);

        try {
            displayLeaderboard();
            backgroundPanel.setCenter(leaderboardPanel);
        } catch (IOException e) {
            Logger.log(e.getMessage(), FileConstants.ERROR_LOGS_FILE_PATH);
        }

        Scene scene = new Scene(backgroundPanel, WindowConstants.DEFAULT_WINDOW_WIDTH, WindowConstants.DEFAULT_WINDOW_HEIGHT);
        setScene(scene);
        show();
    }

    private void displayLeaderboard() throws IOException {
        leaderboardPanel.getChildren().clear();

        List<UserStatistic> userStatisticsList = UserStatisticRepository.getUserStatistics();
        Collections.sort(userStatisticsList, Comparator.comparingInt(UserStatistic::getTotalScore).reversed());

        var currentUser = CurrentUserManager.getInstance().getCurrentUser();
        int currentUserRow = -1;

        // Create table
        TableView<LeaderboardEntry> table = new TableView<>();
        table.setStyle("-fx-background-color: transparent;");

        // Rank column
        TableColumn<LeaderboardEntry, Integer> rankColumn = new TableColumn<>(UITexts.LEADERBOARD_COLUMN_RANK);
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        rankColumn.setPrefWidth(200);
        rankColumn.setStyle("-fx-alignment: CENTER;");

        // Username column
        TableColumn<LeaderboardEntry, String> usernameColumn = new TableColumn<>(UITexts.LEADERBOARD_COLUMN_USERNAME);
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setPrefWidth(400);
        usernameColumn.setStyle("-fx-alignment: CENTER;");

        // Score column
        TableColumn<LeaderboardEntry, Integer> scoreColumn = new TableColumn<>(UITexts.LEADERBOARD_COLUMN_SCORE);
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreColumn.setPrefWidth(200);
        scoreColumn.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(rankColumn, usernameColumn, scoreColumn);

        // Populate table
        ObservableList<LeaderboardEntry> data = FXCollections.observableArrayList();
        int rank = 1;
        for (UserStatistic userStatistic : userStatisticsList) {
            var user = UserRepository.getUserById(userStatistic.getUserId());
            String username = user != null ? user.getUsername() : UITexts.UNKNOWN;

            data.add(new LeaderboardEntry(rank, username, userStatistic.getTotalScore(), user.getId()));

            if (user.getId().equals(currentUser.getId())) {
                currentUserRow = rank - 1;
            }
            rank++;
        }

        table.setItems(data);

        // Custom row factory for coloring
        final int finalCurrentUserRow = currentUserRow;
        table.setRowFactory(tv -> new TableRow<LeaderboardEntry>() {
            @Override
            protected void updateItem(LeaderboardEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    int rowIndex = getIndex();
                    if (rowIndex == finalCurrentUserRow) {
                        setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); -fx-text-fill: black;");
                    } else if (rowIndex == 0) {
                        setStyle("-fx-text-fill: #F9A114;"); // Gold
                    } else if (rowIndex == 1) {
                        setStyle("-fx-text-fill: #C0C0C0;"); // Silver
                    } else if (rowIndex == 2) {
                        setStyle("-fx-text-fill: #CD7F32;"); // Bronze
                    } else {
                        setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });

        // Double-click to view profile
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                LeaderboardEntry selectedEntry = table.getSelectionModel().getSelectedItem();
                if (selectedEntry != null) {
                    try {
                        User selectedUser = UserRepository.getUserById(selectedEntry.getUserId());
                        dispose();
                        new UserProfileView(selectedUser, LeaderboardView.class);
                    } catch (IOException ex) {
                        Logger.log(ErrorConstants.USER_DOES_NOT_EXIST, FileConstants.ERROR_LOGS_FILE_PATH);
                    }
                }
            }
        });

        // Style table
        table.setStyle("-fx-background-color: transparent; -fx-font-size: 20px;");
        table.setPrefHeight(500);
        table.setPrefWidth(800);

        VBox tableContainer = new VBox(table);
        tableContainer.setPadding(new Insets(20, 50, 20, 50));
        tableContainer.setAlignment(Pos.CENTER);

        leaderboardPanel.getChildren().add(tableContainer);
    }

    @Override
    void initializeStage() {
        // Not used for this view
    }

    // Inner class for table entries
    public static class LeaderboardEntry {
        private final Integer rank;
        private final String username;
        private final Integer score;
        private final String userId;

        public LeaderboardEntry(int rank, String username, int score, String userId) {
            this.rank = rank;
            this.username = username;
            this.score = score;
            this.userId = userId;
        }

        public Integer getRank() { return rank; }
        public String getUsername() { return username; }
        public Integer getScore() { return score; }
        public String getUserId() { return userId; }
    }
}