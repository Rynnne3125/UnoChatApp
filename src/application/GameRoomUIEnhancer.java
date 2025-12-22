package application;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Utility class to enhance UnoGameApp UI with:
 * - UNO button (race to press first when 1 card left)
 * - Turn timer (10 seconds)
 * - Chat interaction
 * - Friend request & block buttons
 * - Theme backgrounds
 */
public class GameRoomUIEnhancer {

    // ==================== UNO BUTTON ====================
    public static VBox createUNOButton(Runnable onUNOPressed) {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-spacing: 8;");

        Button unoBtn = new Button("🎉 UNO!");
        unoBtn.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 15 30 15 30; " +
                        "-fx-background-color: linear-gradient(to bottom, #FFD700 0%, #FFA500 100%); " +
                        "-fx-text-fill: #000000; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.5), 8, 0, 0, 3);"
        );

        unoBtn.setOnAction(e -> {
            onUNOPressed.run();
            unoBtn.setStyle(
                    "-fx-font-size: 18px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 15 30 15 30; " +
                            "-fx-background-color: #32CD32; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-radius: 12; " +
                            "-fx-background-radius: 12; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.5), 8, 0, 0, 3);"
            );
        });

        unoBtn.setVisible(false); // Hidden until 1 card left
        container.getChildren().add(unoBtn);
        return container;
    }

    // ==================== TURN TIMER ====================
    public static VBox createTurnTimer(int secondsLimit) {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.6); " +
                        "-fx-border-color: #d13639; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10; " +
                        "-fx-padding: 12; " +
                        "-fx-spacing: 8;"
        );

        Label timerLabel = new Label("10s");
        timerLabel.setStyle(
                "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #00FF00;"
        );

        Label timerTitle = new Label("TIME LEFT");
        timerTitle.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-text-fill: #7f8c8d; " +
                        "-fx-font-weight: bold;"
        );

        ProgressBar progressBar = new ProgressBar(1.0);
        progressBar.setPrefWidth(80);
        progressBar.setStyle(
                "-fx-accent: #32CD32;"
        );

        container.getChildren().addAll(timerTitle, timerLabel, progressBar);

        // Timer logic
        int[] remainingSeconds = {secondsLimit};
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000) { // 1 second
                    remainingSeconds[0]--;
                    timerLabel.setText(remainingSeconds[0] + "s");
                    progressBar.setProgress((double) remainingSeconds[0] / secondsLimit);

                    if (remainingSeconds[0] <= 3) {
                        timerLabel.setStyle(
                                "-fx-font-size: 24px; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-text-fill: #FF4444;"
                        );
                    }

                    if (remainingSeconds[0] <= 0) {
                        stop();
                    }
                    lastUpdate = now;
                }
            }
        };
        timer.start();

        return container;
    }

    // ==================== GAME ROOM INTERACTIONS ====================
    public static HBox createGameRoomActions() {
        HBox actionsBox = new HBox();
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setStyle("-fx-spacing: 15; -fx-padding: 15;");

        // Chat Button
        Button chatBtn = new Button("💬 Chat");
        chatBtn.setStyle(createButtonStyle("#3498db"));
        chatBtn.setOnAction(e -> showChatPanel());

        // Add Friend Button
        Button friendBtn = new Button("👥 Add Friend");
        friendBtn.setStyle(createButtonStyle("#2ecc71"));
        friendBtn.setOnAction(e -> showFriendRequest());

        // Block User Button
        Button blockBtn = new Button("🚫 Block");
        blockBtn.setStyle(createButtonStyle("#e74c3c"));
        blockBtn.setOnAction(e -> showBlockConfirm());

        // Emotes/Quick Messages
        Button emoteBtn = new Button("😊 Emotes");
        emoteBtn.setStyle(createButtonStyle("#9b59b6"));
        emoteBtn.setOnAction(e -> showEmotes());

        actionsBox.getChildren().addAll(chatBtn, friendBtn, blockBtn, emoteBtn);
        return actionsBox;
    }

    private static String createButtonStyle(String color) {
        return "-fx-font-size: 12px; " +
                "-fx-padding: 10 15 10 15; " +
                "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-border-radius: 6; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 2);";
    }

    private static void showChatPanel() {
        // TODO: Implement chat UI
        System.out.println("Chat panel opened");
    }

    private static void showFriendRequest() {
        // TODO: Implement friend request UI
        System.out.println("Friend request UI opened");
    }

    private static void showBlockConfirm() {
        // TODO: Implement block confirmation
        System.out.println("Block confirmation opened");
    }

    private static void showEmotes() {
        // TODO: Implement emotes panel
        System.out.println("Emotes panel opened");
    }

    // ==================== BACKGROUND THEMES ====================
    public static String getRandomGameTheme() {
        String[] themes = {
                "linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)", // Dark Blue
                "linear-gradient(135deg, #2d1b00 0%, #3d2817 50%, #4a3a2a 100%)", // Brown
                "linear-gradient(135deg, #1b1b1b 0%, #2d2d2d 50%, #404040 100%)", // Dark Gray
                "linear-gradient(135deg, #0a0a1a 0%, #1a0a2e 50%, #16213e 100%)", // Purple-ish Dark
                "linear-gradient(135deg, #0f1419 0%, #1a1f2e 50%, #2d3561 100%)", // Blue-Gray
                "linear-gradient(135deg, #1a0f1f 0%, #2d1b3d 50%, #3d2a4a 100%)", // Purple
                "linear-gradient(135deg, #0f1f1f 0%, #1a3f3f 50%, #2a5f5f 100%)"  // Teal-ish
        };
        return themes[(int) (Math.random() * themes.length)];
    }

    // ==================== CHAT PANEL COMPONENT ====================
    public static VBox createChatPanel() {
        VBox chatPanel = new VBox();
        chatPanel.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.85); " +
                        "-fx-border-color: #d13639; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8; " +
                        "-fx-padding: 12;"
        );
        chatPanel.setPrefWidth(250);
        chatPanel.setPrefHeight(300);

        // Title
        Label chatTitle = new Label("💬 Game Chat");
        chatTitle.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #d13639;"
        );

        // Messages Area (Scrollable)
        TextArea messagesArea = new TextArea();
        messagesArea.setStyle(
                "-fx-control-inner-background: #1a1a1a; " +
                        "-fx-text-fill: #00FF00; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 11px; " +
                        "-fx-padding: 8;"
        );
        messagesArea.setWrapText(true);
        messagesArea.setEditable(false);
        messagesArea.setPrefRowCount(10);

        // Input
        HBox inputBox = new HBox();
        inputBox.setStyle("-fx-spacing: 8; -fx-padding: 8 0 0 0;");

        TextField messageInput = new TextField();
        messageInput.setPromptText("Type message...");
        messageInput.setStyle(
                "-fx-padding: 8; " +
                        "-fx-background-color: #2a2a2a; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #666666; " +
                        "-fx-border-color: #444444; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 4;"
        );

        Button sendBtn = new Button("Send");
        sendBtn.setStyle(
                "-fx-background-color: #d13639; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8 15 8 15; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4;"
        );

        inputBox.getChildren().addAll(messageInput, sendBtn);
        HBox.setHgrow(messageInput, javafx.scene.layout.Priority.ALWAYS);

        chatPanel.getChildren().addAll(chatTitle, messagesArea, inputBox);
        VBox.setVgrow(messagesArea, javafx.scene.layout.Priority.ALWAYS);

        return chatPanel;
    }

    // ==================== PLAYER QUICK ACTIONS ====================
    public static VBox createPlayerQuickActions(String playerName) {
        VBox actionBox = new VBox();
        actionBox.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.7); " +
                        "-fx-border-color: #3498db; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-padding: 10;"
        );
        actionBox.setAlignment(Pos.TOP_CENTER);

        Label playerLabel = new Label(playerName);
        playerLabel.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white;"
        );

        Button addFriendBtn = new Button("Add Friend");
        addFriendBtn.setStyle(createButtonStyle("#2ecc71"));
        addFriendBtn.setPrefWidth(120);

        Button blockBtn = new Button("Block");
        blockBtn.setStyle(createButtonStyle("#e74c3c"));
        blockBtn.setPrefWidth(120);

        actionBox.getChildren().addAll(playerLabel, addFriendBtn, blockBtn);
        return actionBox;
    }
}
