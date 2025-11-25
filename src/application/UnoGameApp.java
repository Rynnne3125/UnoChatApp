package application;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UnoGameApp extends Application {
    // UNO Colors
    private final Color UNO_RED = Color.web("#D72600");
    private final Color UNO_YELLOW = Color.web("#F9C700");
    private final Color UNO_GREEN = Color.web("#379711");
    private final Color UNO_BLUE = Color.web("#0956BF");
    private final Color UNO_BLACK = Color.web("#1a1a1a");

    // Game State
    private String username = "Player1";
    private String myLanIP;
    private Stage primaryStage;
    private GameServer gameServer;
    private GameClient gameClient;
    private boolean isHost = false;
    private boolean isSinglePlayer = false; // Chế độ chơi đơn

    // Player Slots
    private PlayerSlot[] playerSlots = new PlayerSlot[3];
    private VBox[] playerCardContainers = new VBox[3];
    private Label[] playerNameLabels = new Label[3];
    
    // Game Data
    private List<UnoCard> deck = new ArrayList<>();
    private List<UnoCard> discardPile = new ArrayList<>();
    private List<UnoCard>[] playerHands = new List[3];
    private int currentPlayerIndex = 0;
    private UnoCard topCard;
    
    // UI Components
    private TextField roomIPField;
    private Button startButton;
    private StackPane centerCardArea;
    private HBox myHandContainer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        myLanIP = getRealLanIP();
        primaryStage.setTitle("UNO Game - 3 Players");
        
        showLobbyScene();
        primaryStage.show();
        primaryStage.setFullScreen(true);
    }

    // ============ LOBBY SCENE ============
    private void showLobbyScene() {
        StackPane root = new StackPane();
        root.setBackground(createGradientBackground());
        root.getChildren().add(createFloatingCards());

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setMaxWidth(800);
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Header
        Label title = new Label("🎮 UNO GAME - 3 PLAYERS");
        title.setFont(Font.font("System", FontWeight.BOLD, 40));
        title.setTextFill(Color.WHITE);
        title.setEffect(createDropShadow());

        // Player Slots UI
        HBox slotsContainer = new HBox(15);
        slotsContainer.setAlignment(Pos.CENTER);
        
        for (int i = 0; i < 3; i++) {
            playerSlots[i] = new PlayerSlot(i);
            VBox slotBox = createSlotBox(i);
            slotsContainer.getChildren().add(slotBox);
        }

        // --- BUTTONS CONTROL ---
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        // Nút chơi ngay với Bot (MỚI)
        Button btnSingle = createStyledButton("CHƠI VỚI MÁY", UNO_YELLOW, "🤖");
        btnSingle.setPrefWidth(220);
        btnSingle.setOnAction(e -> startSinglePlayerGame());

        Button createRoomBtn = createStyledButton("TẠO PHÒNG", UNO_GREEN, "🏠");
        createRoomBtn.setOnAction(e -> createGameRoom());
        
        Button joinRoomBtn = createStyledButton("VÀO PHÒNG", UNO_BLUE, "🔗");
        joinRoomBtn.setOnAction(e -> showJoinRoomDialog());
        
        startButton = createStyledButton("BẮT ĐẦU (Multi)", UNO_RED, "▶");
        startButton.setDisable(true); // Chỉ sáng khi đủ người trong chế độ tạo phòng
        startButton.setOnAction(e -> startGame());
        
        buttonBox.getChildren().addAll(btnSingle, createRoomBtn, joinRoomBtn, startButton);

        // IP Info (chỉ hiện cho đẹp hoặc khi tạo phòng)
        Label ipLabel = new Label("IP Của Bạn: " + myLanIP);
        ipLabel.setTextFill(Color.LIGHTGRAY);

        mainLayout.getChildren().addAll(title, slotsContainer, buttonBox, ipLabel);

        StackPane glassWrapper = new StackPane(mainLayout);
        glassWrapper.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 20;");
        
        root.getChildren().add(glassWrapper);
        primaryStage.setScene(new Scene(root, 900, 700));
    }

    private void startSinglePlayerGame() {
        isHost = true;
        isSinglePlayer = true;
        
        // Setup Slot 0 là Mình
        playerSlots[0].type = SlotType.PLAYER;
        playerSlots[0].playerName = "Bạn";
        
        // Setup Slot 1 & 2 là Bot
        playerSlots[1].type = SlotType.BOT;
        playerSlots[1].playerName = "Bot 1";
        
        playerSlots[2].type = SlotType.BOT;
        playerSlots[2].playerName = "Bot 2";
        
        // Vào game ngay
        startGame();
    }

    private VBox createSlotBox(int index) {
        VBox slotBox = new VBox(10);
        slotBox.setAlignment(Pos.CENTER);
        slotBox.setPadding(new Insets(20));
        slotBox.setPrefSize(180, 220);
        slotBox.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 15; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 15;");
        slotBox.setEffect(createDropShadow());

        Label slotLabel = new Label("SLOT " + (index + 1));
        slotLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        slotLabel.setTextFill(Color.WHITE);

        Circle statusCircle = new Circle(15);
        statusCircle.setFill(Color.GRAY);
        playerSlots[index].statusCircle = statusCircle;

        Label statusLabel = new Label("Trống");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.LIGHTGRAY);
        playerSlots[index].statusLabel = statusLabel;

        // Chỉ hiện nút thêm bot nếu đang ở chế độ tạo phòng
        Button toggleBtn = new Button("Thêm Bot");
        toggleBtn.setStyle("-fx-background-color: " + toHex(UNO_YELLOW) + "; -fx-text-fill: black; -fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand;");
        toggleBtn.setVisible(false); // Mặc định ẩn, chỉ hiện khi Tạo Phòng
        
        toggleBtn.setOnAction(e -> {
            PlayerSlot slot = playerSlots[index];
            if (slot.type == SlotType.EMPTY) {
                slot.type = SlotType.BOT;
                slot.playerName = "Bot " + (index + 1);
                statusCircle.setFill(UNO_YELLOW);
                statusLabel.setText("Bot");
                toggleBtn.setText("Xóa Bot");
            } else if (slot.type == SlotType.BOT) {
                slot.type = SlotType.EMPTY;
                slot.playerName = null;
                statusCircle.setFill(Color.GRAY);
                statusLabel.setText("Trống");
                toggleBtn.setText("Thêm Bot");
            }
            updateStartButton();
        });
        
        playerSlots[index].toggleButton = toggleBtn;

        slotBox.getChildren().addAll(slotLabel, statusCircle, statusLabel, toggleBtn);
        return slotBox;
    }

    private void updateStartButton() {
        int filledSlots = 0;
        for (PlayerSlot slot : playerSlots) {
            if (slot.type != SlotType.EMPTY) filledSlots++;
        }
        startButton.setDisable(filledSlots < 2 || !isHost);
    }

    // ============ GAME SCENE ============
    private void showGameScene() {
        StackPane root = new StackPane();
        root.setBackground(createGradientBackground());
        root.getChildren().add(createFloatingCards());

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        // Center - Playing Area
        centerCardArea = new StackPane();
        centerCardArea.setPrefSize(400, 400);
        layout.setCenter(centerCardArea);

        // Top - Player 2 (Đối thủ 1)
        playerCardContainers[1] = createPlayerCardContainer(1);
        layout.setTop(playerCardContainers[1]);

        // Left - Player 3 (Đối thủ 2)
        playerCardContainers[2] = createPlayerCardContainer(2);
        layout.setLeft(playerCardContainers[2]);

        // Bottom - My Hand
        myHandContainer = new HBox(-40); // Overlap bài một chút cho đẹp
        myHandContainer.setAlignment(Pos.CENTER);
        myHandContainer.setPadding(new Insets(20));
        myHandContainer.setPrefHeight(200);
        
        VBox myArea = new VBox(10);
        myArea.setAlignment(Pos.CENTER);
        playerNameLabels[0] = new Label(playerSlots[0].playerName + " (Bạn)");
        playerNameLabels[0].setFont(Font.font("System", FontWeight.BOLD, 20));
        playerNameLabels[0].setTextFill(Color.WHITE);
        myArea.getChildren().addAll(playerNameLabels[0], myHandContainer);
        layout.setBottom(myArea);
        
        // Button Menu nhỏ góc trái trên
        Button backBtn = new Button("⬅ Thoát");
        backBtn.setOnAction(e -> {
            // Reset game state
            currentPlayerIndex = 0;
            showLobbyScene();
        });
        layout.setLeft(new VBox(10, backBtn, playerCardContainers[2])); // Hack layout

        root.getChildren().add(layout);
        primaryStage.setScene(new Scene(root, 1200, 800));
        
        Platform.runLater(() -> dealInitialCards());
    }

    private VBox createPlayerCardContainer(int playerIndex) {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));
        
        playerNameLabels[playerIndex] = new Label(playerSlots[playerIndex].playerName);
        playerNameLabels[playerIndex].setFont(Font.font("System", FontWeight.BOLD, 16));
        playerNameLabels[playerIndex].setTextFill(Color.WHITE);
        playerNameLabels[playerIndex].setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5 15; -fx-background-radius: 15;");
        
        HBox cardBox = new HBox(-30); // Overlap bài đối thủ
        cardBox.setAlignment(Pos.CENTER);
        
        container.getChildren().addAll(playerNameLabels[playerIndex], cardBox);
        return container;
    }

    // ============ GAME LOGIC ============
    private void initializeDeck() {
        deck.clear();
        Color[] colors = {UNO_RED, UNO_BLUE, UNO_GREEN, UNO_YELLOW};
        String[] values = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "Skip", "Reverse", "+2"};
        
        for (Color color : colors) {
            for (String value : values) {
                deck.add(new UnoCard(color, value));
                if (!value.equals("0")) {
                    deck.add(new UnoCard(color, value));
                }
            }
        }
        
        // Wild cards
        for (int i = 0; i < 4; i++) {
            deck.add(new UnoCard(UNO_BLACK, "Wild"));
            deck.add(new UnoCard(UNO_BLACK, "+4"));
        }
        
        Collections.shuffle(deck);
    }

    private void dealInitialCards() {
        initializeDeck();
        discardPile.clear();
        
        for (int i = 0; i < 3; i++) {
            playerHands[i] = new ArrayList<>();
        }
        
        Timeline dealTimeline = new Timeline();
        int delay = 0;
        
        // Chia 7 lá (UNO chuẩn là 7)
        for (int round = 0; round < 7; round++) {
            for (int player = 0; player < 3; player++) {
                final int p = player;
                
                KeyFrame kf = new KeyFrame(Duration.millis(delay), e -> {
                    if (!deck.isEmpty()) {
                        UnoCard card = deck.remove(0);
                        playerHands[p].add(card);
                        animateDealCard(p, card);
                    }
                });
                dealTimeline.getKeyFrames().add(kf);
                delay += 100; // Tốc độ chia bài
            }
        }
        
        dealTimeline.setOnFinished(e -> {
            if (!deck.isEmpty()) {
                topCard = deck.remove(0);
                // Nếu bốc trúng bài đen đầu tiên thì bốc lại cho đơn giản
                while(topCard.color == UNO_BLACK && !deck.isEmpty()) {
                    deck.add(topCard);
                    topCard = deck.remove(0);
                }
                discardPile.add(topCard);
                showTopCard();
            }
            startTurn();
        });
        
        dealTimeline.play();
    }

    private void animateDealCard(int playerIndex, UnoCard card) {
        Pane cardPane = createCardPane(card, playerIndex == 0);
        
        if (playerIndex == 0) {
            // My hand
            cardPane.setOnMouseClicked(e -> playCard(card));
            addHoverEffect(cardPane);
            myHandContainer.getChildren().add(cardPane);
        } else {
            // Other players
            HBox cardBox = (HBox) ((VBox) playerCardContainers[playerIndex]).getChildren().get(1);
            cardBox.getChildren().add(cardPane);
        }
    }

    private Pane createCardPane(UnoCard card, boolean faceUp) {
        StackPane pane = new StackPane();
        pane.setPrefSize(70, 100); // Kích thước bài
        
        Rectangle cardRect = new Rectangle(70, 100);
        cardRect.setArcWidth(10);
        cardRect.setArcHeight(10);
        
        if (faceUp) {
            cardRect.setFill(card.color);
            Label valueLabel = new Label(card.value);
            valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            valueLabel.setTextFill(Color.WHITE);
            // Xử lý bài đen
            if(card.color == UNO_BLACK) {
                cardRect.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE, 
                    new Stop(0, UNO_RED), new Stop(0.5, UNO_BLUE), new Stop(1, UNO_YELLOW)));
            }
            
            Circle centerCircle = new Circle(25, Color.TRANSPARENT);
            centerCircle.setStroke(Color.WHITE); centerCircle.setStrokeWidth(2);
            
            pane.getChildren().addAll(cardRect, centerCircle, valueLabel);
        } else {
            // Mặt úp
            cardRect.setFill(Color.BLACK);
            Label uno = new Label("UNO");
            uno.setTextFill(UNO_RED);
            uno.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            uno.setRotate(-30);
            
            Rectangle border = new Rectangle(66, 96, Color.TRANSPARENT);
            border.setStroke(Color.WHITE); border.setArcWidth(8); border.setArcHeight(8);
            
            pane.getChildren().addAll(cardRect, border, uno);
        }

        pane.setEffect(createDropShadow());
        return pane;
    }

    private void showTopCard() {
        centerCardArea.getChildren().clear();
        Pane cardPane = createCardPane(topCard, true);
        
        // Hiệu ứng scale khi bài ra giữa
        ScaleTransition st = new ScaleTransition(Duration.millis(300), cardPane);
        st.setFromX(0.5); st.setFromY(0.5); st.setToX(1.2); st.setToY(1.2);
        st.play();
        
        centerCardArea.getChildren().add(cardPane);
        
        // Update màu nền center theo màu bài (cho đẹp)
        centerCardArea.setStyle("-fx-effect: dropshadow(three-pass-box, " + toHex(topCard.color) + ", 20, 0, 0, 0);");
    }

    private void startTurn() {
        highlightCurrentPlayer();
        
        // Kiểm tra xem lượt này có phải là BOT không
        if (playerSlots[currentPlayerIndex].type == SlotType.BOT) {
            // Bot turn
            PauseTransition pause = new PauseTransition(Duration.seconds(1.2)); // Bot nghĩ 1.2s
            pause.setOnFinished(e -> botPlayCard());
            pause.play();
        } else {
            // Player turn -> chờ click
            // Có thể thêm logic kiểm tra xem Player có bài đánh được không, nếu không thì tự bốc
            boolean canPlay = false;
            for(UnoCard c : playerHands[currentPlayerIndex]) {
                if(canPlayCard(c, topCard)) { canPlay = true; break; }
            }
            if(!canPlay) {
                // Tự động bốc nếu không có bài (Auto draw)
                // Trong thực tế nên hiện nút "Bốc bài", nhưng ở đây làm auto cho nhanh
                Platform.runLater(() -> {
                     showAlert("Bạn không có bài! Đang bốc...");
                     drawCardForPlayer(currentPlayerIndex);
                     nextTurn();
                });
            }
        }
    }

    private void highlightCurrentPlayer() {
        for (int i = 0; i < 3; i++) {
            if (playerNameLabels[i] != null) {
                if (i == currentPlayerIndex) {
                    playerNameLabels[i].setTextFill(UNO_YELLOW);
                    playerNameLabels[i].setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-padding: 5 15; -fx-background-radius: 15; -fx-border-color: yellow; -fx-border-radius: 15;");
                } else {
                    playerNameLabels[i].setTextFill(Color.WHITE);
                    playerNameLabels[i].setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5 15; -fx-background-radius: 15;");
                }
            }
        }
    }

    private void playCard(UnoCard card) {
        if (currentPlayerIndex != 0) return; // Không phải lượt mình
        
        if (canPlayCard(card, topCard)) {
            // Animation bài bay ra (Simulated)
            playerHands[0].remove(card);
            handleCardEffect(card);
            
            refreshMyHand();
            showTopCard();
            
            if (playerHands[0].isEmpty()) {
                showWinner(0);
                return;
            }
            
            nextTurn();
        } else {
            // Rung lắc bài báo lỗi
            // (Code đơn giản thì show alert)
            // showAlert("Không đánh được bài này!");
        }
    }
    
    private void handleCardEffect(UnoCard card) {
        topCard = card;
        discardPile.add(card);
        
        // Logic đơn giản cho action cards
        if(card.value.equals("Skip")) {
            currentPlayerIndex = (currentPlayerIndex + 1) % 3; // Nhảy cóc
        }
        else if(card.value.equals("+2")) {
            int nextP = (currentPlayerIndex + 1) % 3;
            drawCardForPlayer(nextP);
            drawCardForPlayer(nextP);
        }
        else if(card.value.equals("+4")) {
             int nextP = (currentPlayerIndex + 1) % 3;
             for(int k=0; k<4; k++) drawCardForPlayer(nextP);
        }
        // Chọn màu cho Wild (Bot thì random, Người thì mặc định Đỏ cho nhanh demo)
        if(card.color == UNO_BLACK) {
            topCard.color = UNO_RED; // Demo: luôn set về đỏ
            if(playerSlots[currentPlayerIndex].type == SlotType.PLAYER) {
                showAlert("Bạn chọn màu Đỏ (Mặc định)");
            }
        }
    }

    private void botPlayCard() {
        List<UnoCard> hand = playerHands[currentPlayerIndex];
        UnoCard playableCard = null;
        
        // Bot tìm bài đánh được
        for (UnoCard card : hand) {
            if (canPlayCard(card, topCard)) {
                playableCard = card;
                break; // Bot đánh lá đầu tiên tìm thấy
            }
        }
        
        if (playableCard != null) {
            hand.remove(playableCard);
            handleCardEffect(playableCard);
            
            refreshPlayerHand(currentPlayerIndex);
            showTopCard();
            
            if (hand.isEmpty()) {
                showWinner(currentPlayerIndex);
                return;
            }
        } else {
            drawCardForPlayer(currentPlayerIndex);
        }
        
        nextTurn();
    }
    
    private void drawCardForPlayer(int pIdx) {
        if (deck.isEmpty()) {
            // Reshuffle discard pile (Trừ top card)
            if(discardPile.size() > 1) {
                deck.addAll(discardPile.subList(0, discardPile.size()-1));
                discardPile.subList(0, discardPile.size()-1).clear();
                Collections.shuffle(deck);
            } else {
                return; // Hết bài
            }
        }
        UnoCard c = deck.remove(0);
        playerHands[pIdx].add(c);
        if(pIdx == 0) refreshMyHand();
        else refreshPlayerHand(pIdx);
    }

    private boolean canPlayCard(UnoCard card, UnoCard top) {
        if (card.color == UNO_BLACK) return true; // Wild luôn đánh được
        if (top.color == UNO_BLACK) return true; // Nếu top là Wild (đã set màu) thì phải check màu (trong code demo này topCard.color sẽ được set khi đánh)
        return card.color.equals(top.color) || card.value.equals(top.value);
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % 3;
        startTurn();
    }

    private void refreshMyHand() {
        myHandContainer.getChildren().clear();
        for (UnoCard card : playerHands[0]) {
            Pane cardPane = createCardPane(card, true);
            cardPane.setOnMouseClicked(e -> playCard(card));
            addHoverEffect(cardPane);
            myHandContainer.getChildren().add(cardPane);
        }
    }
    
    private void addHoverEffect(Pane p) {
        p.setOnMouseEntered(e -> {
            p.setViewOrder(-1); // Nổi lên trên cùng
            TranslateTransition tt = new TranslateTransition(Duration.millis(100), p);
            tt.setToY(-20);
            tt.play();
        });
        p.setOnMouseExited(e -> {
            p.setViewOrder(0);
            TranslateTransition tt = new TranslateTransition(Duration.millis(100), p);
            tt.setToY(0);
            tt.play();
        });
    }

    private void refreshPlayerHand(int playerIndex) {
        if (playerIndex == 0) return;
        HBox cardBox = (HBox) ((VBox) playerCardContainers[playerIndex]).getChildren().get(1);
        cardBox.getChildren().clear();
        // Vẽ lại lưng bài dựa trên số lượng
        for (int i = 0; i < playerHands[playerIndex].size(); i++) {
            cardBox.getChildren().add(createCardPane(new UnoCard(Color.GRAY, "?"), false));
        }
    }

    private void showWinner(int playerIndex) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("KẾT THÚC");
        alert.setHeaderText(playerSlots[playerIndex].playerName + " CHIẾN THẮNG! 🏆");
        alert.setContentText("Bấm OK để về sảnh.");
        alert.showAndWait();
        showLobbyScene();
    }

    // ============ NETWORKING (Giữ nguyên cho tính năng mở rộng) ============
    private void createGameRoom() {
        isHost = true;
        isSinglePlayer = false;
        playerSlots[0].type = SlotType.PLAYER;
        playerSlots[0].playerName = username;
        playerSlots[0].statusCircle.setFill(UNO_GREEN);
        playerSlots[0].statusLabel.setText("Bạn (Host)");
        
        // Hiện nút toggle bot cho các slot còn lại
        for(int i=1; i<3; i++) {
             playerSlots[i].toggleButton.setVisible(true);
        }

        gameServer = new GameServer(5555);
        new Thread(gameServer).start();
        updateStartButton();
    }

    private void showJoinRoomDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Vào Phòng");
        dialog.setHeaderText("Nhập IP:Port của Host");
        dialog.setContentText("Ví dụ: 192.168.1.5:5555");
        dialog.showAndWait().ifPresent(addr -> {
            String[] parts = addr.split(":");
            if (parts.length == 2) {
                joinGameRoom(parts[0], Integer.parseInt(parts[1]));
            }
        });
    }

    private void joinGameRoom(String ip, int port) {
        gameClient = new GameClient(ip, port);
        new Thread(gameClient).start();
    }

    private void startGame() {
        if (isHost) {
            showGameScene();
        }
    }

    // ============ HELPERS ============
    private Background createGradientBackground() {
        return new Background(new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2c3e50")),
                new Stop(1, Color.web("#000000"))),
            CornerRadii.EMPTY, Insets.EMPTY));
    }

    private Pane createFloatingCards() {
        Pane pane = new Pane();
        pane.setPickOnBounds(false);
        Random rand = new Random();
        Color[] colors = {UNO_RED, UNO_BLUE, UNO_GREEN, UNO_YELLOW};

        for (int i = 0; i < 15; i++) {
            Rectangle card = new Rectangle(40, 60);
            card.setArcWidth(8); card.setArcHeight(8);
            card.setFill(colors[rand.nextInt(colors.length)]);
            card.setStroke(Color.WHITE); card.setStrokeWidth(1);
            card.setOpacity(0.15);

            Pane cardGroup = new Pane(card);
            cardGroup.setLayoutX(rand.nextInt(1000));
            cardGroup.setLayoutY(rand.nextInt(700));
            
            RotateTransition rt = new RotateTransition(Duration.seconds(10 + rand.nextInt(10)), cardGroup);
            rt.setByAngle(360); rt.setCycleCount(Animation.INDEFINITE); rt.play();

            pane.getChildren().add(cardGroup);
        }
        return pane;
    }

    private Button createStyledButton(String text, Color color, String icon) {
        Button btn = new Button(icon + " " + text);
        btn.setPrefHeight(45);
        btn.setFont(Font.font("System", FontWeight.BOLD, 14));
        btn.setTextFill(Color.WHITE);

        String hex = toHex(color);
        btn.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 25; -fx-cursor: hand;");
        btn.setEffect(createDropShadow());

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + toHex(color.brighter()) + "; -fx-background-radius: 25; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 25; -fx-cursor: hand;"));

        return btn;
    }

    private DropShadow createDropShadow() {
        DropShadow ds = new DropShadow();
        ds.setColor(Color.rgb(0, 0, 0, 0.3));
        ds.setOffsetY(3);
        return ds;
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
            (int) (c.getRed() * 255),
            (int) (c.getGreen() * 255),
            (int) (c.getBlue() * 255));
    }

    private String getRealLanIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) { return "127.0.0.1"; }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.show();
    }

    // ============ INNER CLASSES ============
    enum SlotType { EMPTY, BOT, PLAYER }

    class PlayerSlot {
        int index;
        SlotType type = SlotType.EMPTY;
        String playerName;
        Circle statusCircle;
        Label statusLabel;
        Button toggleButton;
        PlayerSlot(int index) { this.index = index; }
    }

    class UnoCard {
        Color color;
        String value;
        UnoCard(Color color, String value) { this.color = color; this.value = value; }
    }

    // Dummy classes for Server/Client (Giữ lại để ko báo lỗi nếu bạn muốn phát triển sau)
    class GameServer implements Runnable {
        GameServer(int port) {}
        @Override public void run() {}
    }
    class GameClient implements Runnable {
        GameClient(String ip, int port) {}
        @Override public void run() {}
    }
}