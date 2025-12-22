package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Beautiful Post Card Component for News Feed
 * Supports: Author avatar, content, comments, likes
 */
public class PostCardUI extends VBox {

    public PostCardUI(String authorName, String authorAvatar, String postContent, String timestamp) {
        this.setPrefWidth(400);
        this.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.08); " +
                        "-fx-border-color: #d13639; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-padding: 16; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 8, 0, 0, 2);"
        );
        this.setSpacing(12);

        // Header: Author Info + Timestamp
        HBox headerBox = createHeaderBox(authorName, authorAvatar, timestamp);

        // Content
        Label contentLabel = new Label(postContent);
        contentLabel.setWrapText(true);
        contentLabel.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-padding: 8;"
        );

        // Action Buttons
        HBox actionsBox = createActionsBox();

        this.getChildren().addAll(headerBox, contentLabel, actionsBox);
    }

    private HBox createHeaderBox(String authorName, String authorAvatar, String timestamp) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);

        // Avatar
        Circle avatar = new Circle(20);
        avatar.setStyle("-fx-fill: #3498db; -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.2), 4, 0, 0, 1);");
        try {
            if (authorAvatar != null && !authorAvatar.isEmpty()) {
                Image img = new Image(authorAvatar, 40, 40, true, true);
                avatar.setFill(new ImagePattern(img));
            }
        } catch (Exception ignored) {}

        // Author Name + Time
        VBox authorInfo = new VBox();
        authorInfo.setSpacing(2);

        Label nameLabel = new Label(authorName);
        nameLabel.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #d13639;"
        );

        Label timeLabel = new Label(timestamp != null ? timestamp : "Just now");
        timeLabel.setStyle(
                "-fx-font-size: 11px; " +
                        "-fx-text-fill: #7a7d82;"
        );

        authorInfo.getChildren().addAll(nameLabel, timeLabel);

        header.getChildren().addAll(avatar, authorInfo);
        return header;
    }

    private HBox createActionsBox() {
        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setSpacing(15);
        actions.setStyle("-fx-padding: 8 0 0 0; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-width: 1 0 0 0;");

        // Like Button
        Button likeBtn = new Button("❤️ Like");
        likeBtn.setStyle(createActionButtonStyle("#e74c3c"));

        // Comment Button
        Button commentBtn = new Button("💬 Comment");
        commentBtn.setStyle(createActionButtonStyle("#3498db"));

        // Share Button
        Button shareBtn = new Button("↗️ Share");
        shareBtn.setStyle(createActionButtonStyle("#2ecc71"));

        actions.getChildren().addAll(likeBtn, commentBtn, shareBtn);
        return actions;
    }

    private String createActionButtonStyle(String color) {
        return "-fx-font-size: 11px; " +
                "-fx-padding: 6 12 6 12; " +
                "-fx-background-color: transparent; " +
                "-fx-text-fill: " + color + "; " +
                "-fx-border-color: " + color + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand; " +
                "-fx-font-weight: bold;";
    }
}
