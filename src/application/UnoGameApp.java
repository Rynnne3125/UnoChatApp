package application;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class UnoGameApp extends Application {

    // --- RESOURCES ---
    private static final String IMG_YASUO = "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/Yasuo.png";
    private static final String IMG_AHRI = "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/Ahri.png";
    private static final String IMG_TEEMO = "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/Teemo.png";
    private static final String IMG_LEE = "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/LeeSin.png";
    private static final String BG_COLOR = "#091428";
    private static final String GOLD_COLOR = "#C8AA6E";

    // --- MODEL ---
    private enum CardColor { RED, GREEN, BLUE, YELLOW, BLACK }
    private enum CardType { NUMBER, SKIP, REVERSE, DRAW2, WILD, WILD_DRAW4 }

    private static class Card {
        CardColor color; CardType type; int number;
        public Card(CardColor color, CardType type, int number) {
            this.color = color; this.type = type; this.number = number;
        }
        public String toStringCode() { return color + "_" + type + "_" + number; }
        public static Card fromStringCode(String code) {
            try {
                String[] parts = code.split("_");
                return new Card(CardColor.valueOf(parts[0]), CardType.valueOf(parts[1]), Integer.parseInt(parts[2]));
            } catch (Exception e) { return null; }
        }
        public String getSymbol() {
            if (type == CardType.NUMBER) return String.valueOf(number);
            if (type == CardType.SKIP) return "⊘";
            if (type == CardType.REVERSE) return "⇄";
            if (type == CardType.DRAW2) return "+2";
            if (type == CardType.WILD) return "W";
            if (type == CardType.WILD_DRAW4) return "+4";
            return "";
        }
        public Color getFxColor() {
            switch (color) {
                case RED: return Color.web("#e74c3c");
                case GREEN: return Color.web("#2ecc71");
                case BLUE: return Color.web("#3498db");
                case YELLOW: return Color.web("#f1c40f");
                default: return Color.web("#2d3436");
            }
        }
    }

    // --- UI COMPONENTS ---
    private Stage mainStage;
    private Pane animationLayer;
    private StackPane rootStack;
    private HBox playerCardContainer;
    private StackPane centerPileStack, deckStack;
    private Label statusLabel, colorIndicator;
    private Text directionArrow;

    private PlayerProfileUI playerProfile; 
    private PlayerProfileUI leftProfile;   
    private PlayerProfileUI rightProfile;  
    
    private HBox leftHandContainer;
    private HBox rightHandContainer;

    // --- GAME STATE ---
    private List<Card> deck = new ArrayList<>();
    private List<Card> discardPile = new ArrayList<>();
    
    private List<Card> myHand = new ArrayList<>();     
    private List<Card> p1Hand = new ArrayList<>();     
    private List<Card> p2Hand = new ArrayList<>();     
    private List<Card> displayHand = new ArrayList<>(); 
    
    private java.util.Queue<NotificationTask> notificationQueue = new java.util.LinkedList<>();
    private boolean isShowingNotification = false;

    private int currentPlayerIndex = 0; 
    private int myIndex = 0; 
    private int direction = 1;
    private CardColor currentWildColor = null;
    private boolean isAnimating = false;

    // --- NETWORK ---
    private boolean isHost;
    private String playerName;

    // Host variables
    private List<Socket> clientSockets;
    private List<PrintWriter> clientWriters;
    private boolean[] isPlayerBot = {false, false, false}; 

    // Client variables
    private Socket clientSocket;
    private PrintWriter outToServer;
    private BufferedReader inFromServer;

    // Player Info
    private String myAvatar; // Avatar của bản thân
    private String hostAvatar;
    private String guest1Name;
    private String guest1Avatar;
    private String guest2Name;
    private String guest2Avatar;


    // 1. Constructor HOST
    public UnoGameApp(
            String hostName,
            boolean isHost,
            List<Socket> sockets,
            List<PrintWriter> writers,
            String hostAvatar,
            String guest1Name,
            String guest1Avatar,
            String guest2Name,
            String guest2Avatar
    ) {
        this.playerName = hostName;
        this.isHost = true;
        this.clientSockets = sockets;
        this.clientWriters = writers;
        this.myIndex = 0;
        this.myAvatar = hostAvatar; // Host cũng dùng myAvatar

        // Lưu thông tin
        this.hostAvatar = hostAvatar;
        this.guest1Name = guest1Name;
        this.guest1Avatar = guest1Avatar;
        this.guest2Name = guest2Name;
        this.guest2Avatar = guest2Avatar;

        if (sockets.size() < 1) isPlayerBot[1] = true;
        if (sockets.size() < 2) isPlayerBot[2] = true;
    }

    // 2. Constructor CLIENT (Đã sửa để nhận myAvatar)
    public UnoGameApp(String playerName, boolean isHost, Socket socket, PrintWriter out, BufferedReader in, String myAvatar) {
        this.playerName = playerName;
        this.isHost = false;
        this.clientSocket = socket;
        this.outToServer = out;
        this.inFromServer = in;
        this.myAvatar = myAvatar; // Lưu avatar bản thân để hiển thị
    }

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        stage.setTitle("UNO Online - " + playerName + (isHost ? " (HOST)" : " (CLIENT)"));
        
        // --- QUAN TRỌNG: UI PHẢI ĐƯỢC INIT TRƯỚC LOGIC HOST ---
        initializeUI();

        if (isHost) {
            initializeGameHost();
            startHostListener();
        } else {
            startClientListener();
        }
    }

 // ================= UI SETUP =================
    private void initializeUI() {
        BorderPane gameLayout = new BorderPane();
        gameLayout.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // --- TOP AREA ---
        HBox topArea = new HBox(150);
        topArea.setAlignment(Pos.CENTER);
        topArea.setPadding(new Insets(20));

        // 1. KHỞI TẠO Profile TRƯỚC (Sửa lỗi NPE)
        leftProfile = new PlayerProfileUI(
                (isHost && guest2Name != null) ? guest2Name : "Waiting...",
                (isHost && guest2Avatar != null) ? guest2Avatar : IMG_TEEMO,
                Pos.CENTER_LEFT
        );

        rightProfile = new PlayerProfileUI(
                (isHost && guest1Name != null) ? guest1Name : "Waiting...",
                (isHost && guest1Avatar != null) ? guest1Avatar : IMG_AHRI,
                Pos.CENTER_RIGHT
        );

        // 2. KHỞI TẠO Container chứa bài úp
        leftHandContainer = createCompactHandView();  
        rightHandContainer = createCompactHandView(); 

        // Hiển thị sẵn bài úp (Optional)
        renderBackCards(leftHandContainer, 7, Pos.CENTER_LEFT);
        renderBackCards(rightHandContainer, 7, Pos.CENTER_RIGHT);

        // 3. ĐƯA VÀO WRAPPER (Bây giờ leftProfile và rightProfile đã khác null)
        HBox leftWrapper = new HBox(10, leftProfile, leftHandContainer); 
        leftWrapper.setAlignment(Pos.CENTER_LEFT);
        
        HBox rightWrapper = new HBox(10, rightHandContainer, rightProfile); 
        rightWrapper.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region(); 
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        topArea.getChildren().addAll(leftWrapper, spacer, rightWrapper);

        // --- CENTER AREA ---
        StackPane tableCenter = new StackPane();
        directionArrow = new Text("⟳");
        directionArrow.setFont(Font.font(120));
        directionArrow.setFill(Color.web("#ffffff", 0.05));

        deckStack = createCardBack();
        deckStack.setStyle("-fx-cursor: hand;");
        deckStack.setOnMouseClicked(e -> handleDrawClick());

        centerPileStack = new StackPane();
        VBox piles = new VBox(10, new HBox(60, new VBox(5, deckStack, createLabel("RÚT", 12)), new VBox(5, centerPileStack, createLabel("BÀI ĐÁNH", 12))));
        piles.setAlignment(Pos.CENTER); ((HBox)piles.getChildren().get(0)).setAlignment(Pos.CENTER);

        colorIndicator = createLabel("", 20);
        statusLabel = createLabel("Connecting...", 18);
        statusLabel.setTextFill(Color.web(GOLD_COLOR));
        piles.getChildren().addAll(colorIndicator, statusLabel);
        
        tableCenter.getChildren().addAll(directionArrow, piles);

        // --- BOTTOM AREA ---
        // Sử dụng myAvatar được truyền từ Constructor
        playerProfile = new PlayerProfileUI(playerName, myAvatar != null ? myAvatar : IMG_YASUO, Pos.CENTER);
        playerCardContainer = new HBox(-30); playerCardContainer.setAlignment(Pos.CENTER_LEFT); playerCardContainer.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(playerCardContainer);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setFitToHeight(true); scroll.setPannable(true); scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStylesheets().add("data:text/css,.scroll-pane > .viewport { -fx-background-color: transparent; } .scroll-bar:horizontal { -fx-opacity: 0; }");

        HBox bottomArea = new HBox(20, playerProfile, scroll);
        bottomArea.setAlignment(Pos.CENTER_LEFT); bottomArea.setPadding(new Insets(10, 30, 20, 30));

        gameLayout.setTop(topArea); gameLayout.setCenter(tableCenter); gameLayout.setBottom(bottomArea);
        
        animationLayer = new Pane(); animationLayer.setPickOnBounds(false);
        rootStack = new StackPane(gameLayout, animationLayer);
        mainStage.setScene(new Scene(rootStack, 1200, 800));
        mainStage.show();
    }

    // ================= HOST LOGIC =================
    private void initializeGameHost() {
    	leftProfile.updateInfo(guest2Name, guest2Avatar);
    	rightProfile.updateInfo(guest1Name, guest1Avatar);
        initializeDeck();
        for (int i = 0; i < 7; i++) {
            myHand.add(drawOne()); p1Hand.add(drawOne()); p2Hand.add(drawOne());
        }
        displayHand.addAll(myHand);
        
        if (clientWriters.size() > 0) clientWriters.get(0).println("INIT:1");
        if (clientWriters.size() > 1) clientWriters.get(1).println("INIT:2");
        
        String setupMsg = "GAME_SETUP:" + 
                playerName + "|" + hostAvatar + "," +
                (guest1Name != null ? guest1Name : "Bot Ahri") + "|" + (guest1Avatar != null ? guest1Avatar : IMG_AHRI) + "," +
                (guest2Name != null ? guest2Name : "Bot Teemo") + "|" + (guest2Avatar != null ? guest2Avatar : IMG_TEEMO);
        
        broadcast(setupMsg);
        updateAllClients("START");
        updateUI(); 
    }

    private void startHostListener() {
        for (int i = 0; i < clientSockets.size(); i++) {
            final int pIndex = i + 1;
            Socket s = clientSockets.get(i);
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new java.io.InputStreamReader(s.getInputStream()));
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        handleClientMessage(pIndex, msg);
                    }
                } catch (IOException e) {
                    handleClientDisconnect(pIndex);
                }
            }).start();
        }
    }

    private void handleClientDisconnect(int pIndex) {
        if (isPlayerBot[pIndex]) return; 
        isPlayerBot[pIndex] = true; 
        
        // Fallback về Bot
        String botName = (pIndex == 1) ? "Bot Ahri" : "Bot Teemo";
        String botAva = (pIndex == 1) ? IMG_AHRI : IMG_TEEMO;

        // Cập nhật thông tin nội bộ Host để gửi Setup mới nếu có người khác out
        if(pIndex == 1) { guest1Name = botName; guest1Avatar = botAva; }
        else { guest2Name = botName; guest2Avatar = botAva; }

        Platform.runLater(() -> {
            statusLabel.setText("Player " + pIndex + " disconnect! Bot taking over.");
            
            // Cập nhật UI Host
            if (pIndex == 1) rightProfile.updateInfo(botName, botAva);
            else leftProfile.updateInfo(botName, botAva);

            if (currentPlayerIndex == pIndex) botAutoPlay(pIndex);
        });
    }

    private void handleClientMessage(int pIndex, String msg) {
        if (isPlayerBot[pIndex]) return;
        if (msg.startsWith("PLAY:")) {
            String code = msg.split(":")[1];
            Card c = Card.fromStringCode(code);
            Platform.runLater(() -> {
                if (currentPlayerIndex == pIndex) processCardPlayed(pIndex, c, false);
            });
        } 
        else if (msg.equals("DRAW")) {
            Platform.runLater(() -> {
                if (currentPlayerIndex == pIndex) {
                    Card c = drawOne();
                    getHandByIndex(pIndex).add(c);
                    broadcast("DRAW:" + pIndex);
                    updateAllClients("HandUpdate");
                    nextTurn();
                }
            });
        }
        else if (msg.startsWith("COLOR:")) {
            currentWildColor = CardColor.valueOf(msg.split(":")[1]);
            broadcast("COLOR:" + currentWildColor);
            Platform.runLater(() -> finishAction(new Card(CardColor.BLACK, CardType.WILD, -1)));
        }
    }

    private void botAutoPlay(int botIndex) {
    	double speed = myHand.isEmpty() ? 0.3 : 1.2; 
        
        PauseTransition pause = new PauseTransition(Duration.seconds(speed));
        pause.setOnFinished(e -> {
        	List<Card> hand = getHandByIndex(botIndex);
            Card top = discardPile.get(discardPile.size() - 1);
            Card play = null;
            for (Card c : hand) { if (isValidMove(c, top)) { play = c; break; } }

            if (play != null) processCardPlayed(botIndex, play, true);
            else {
                Card drawn = drawOne();
                if (drawn != null) {
                    hand.add(drawn);
                    broadcast("DRAW:" + botIndex);
                    updateUI();
                    if (isValidMove(drawn, top)) processCardPlayed(botIndex, drawn, true);
                    else nextTurn();
                } else nextTurn();
            }
        });
        pause.play();
    }

    private void processCardPlayed(int pIndex, Card c, boolean isBot) {
        Node fromNode = getAvatarNodeByIndex(pIndex);
        StackPane animatedCard = createCardView(c, true);

        animateCardMovement(animatedCard, fromNode, centerPileStack, () -> {
            List<Card> hand = getHandByIndex(pIndex);
            hand.removeIf(card -> card.toStringCode().equals(c.toStringCode())); // Xóa bài logic
            discardPile.add(c);

            // QUAN TRỌNG: Đồng bộ bài hiển thị của User ngay tại đây
            if (pIndex == myIndex) {
                displayHand.clear();
                displayHand.addAll(myHand);
            }
            
            updateUI(); // Làm mới giao diện

            Runnable continueLogic = () -> {
                if (c.color == CardColor.BLACK) {
                    if (isBot) {
                        currentWildColor = CardColor.values()[new Random().nextInt(4)];
                        finishAction(c);
                    } else showColorPickerDialog(c);
                } else finishAction(c);
            };

            if (hand.size() == 1) {
                String name = (pIndex == myIndex ? "BẠN" : getProfileByIndex(pIndex).getName().toUpperCase());
                showStatusOverlay(name + " HÔ: UNO!", 1.5, continueLogic);
            } else {
                continueLogic.run();
            }
        });
    }
 // Trong class UnoGameApp, cập nhật các phương thức xử lý logic

    private void finishAction(Card c) {
        if (checkWinCondition()) return;

        // Định nghĩa hành động chuyển lượt
        Runnable nextTurnStep = () -> {
            if (mainStage.isShowing()) nextTurn();
        };

        switch (c.type) {
            case SKIP:
                String targetName = getProfileByIndex(getNextIndex()).getName().toUpperCase();
                showStatusOverlay("CẤM LƯỢT: " + targetName, 1.2, () -> {
                    currentPlayerIndex = getNextIndex();
                    nextTurnStep.run();
                });
                break;

            case REVERSE:
                direction *= -1;
                showStatusOverlay(direction == 1 ? "ĐỔI CHIỀU: THUẬN ⟳" : "ĐỔI CHIỀU: NGƯỢC ⟲", 1.2, nextTurnStep);
                break;

            case DRAW2:
            case WILD_DRAW4:
                int victim = getNextIndex();
                int amount = (c.type == CardType.DRAW2) ? 2 : 4;
                String victimName = getProfileByIndex(victim).getName().toUpperCase();

                // 1. Hiện thông báo phạt
                showStatusOverlay(victimName + " BỊ PHẠT +" + amount, 1.5, () -> {
                    // 2. Sau khi thông báo ẩn, chạy bài bay về tay
                    animatePenaltyDraw(victim, amount); 
                    drawN(victim, amount);
                    refreshUserHand();
                    currentPlayerIndex = getNextIndex(); // Bỏ qua lượt nạn nhân
                    
                    // 3. Đợi bài bay xong một chút rồi mới chuyển lượt
                    PauseTransition delay = new PauseTransition(Duration.millis(amount * 300));
                    delay.setOnFinished(e -> nextTurnStep.run());
                    delay.play();
                });
                break;

            default:
                // Bài số hoặc Wild thường: chuyển lượt ngay
                nextTurnStep.run();
                break;
        }
    }
    private void checkUno(int pIndex) {
        List<Card> hand = getHandByIndex(pIndex);
        if (hand.size() == 1) {
            String name = (pIndex == 0) ? "BẠN" : "PLAYER " + pIndex;
            showStatusOverlay(name + " HÔ: UNO!", 2);
            // Hiệu ứng Glow cho profile khi đang ở trạng thái UNO
            getProfileByIndex(pIndex).setUnoGlow(true);
        } else {
            getProfileByIndex(pIndex).setUnoGlow(false);
        }
    }
    private PlayerProfileUI getProfileByIndex(int idx) {
        if (idx == myIndex) return playerProfile;
        // Dựa trên logic hiển thị của bạn
        if (isHost) {
            return (idx == 1) ? rightProfile : leftProfile;
        } else {
            if (myIndex == 1) return (idx == 0) ? rightProfile : leftProfile;
            if (myIndex == 2) return (idx == 0) ? rightProfile : leftProfile;
        }
        return playerProfile;
    }
    // Gọi checkUno(pIndex) ngay trong hàm processCardPlayed sau khi remove bài
    private void skipNextPlayer() {
        currentPlayerIndex = getNextIndex(); // Nhảy qua người tiếp theo
        showStatusOverlay("BỊ CẤM LƯỢT!", 1);
    }

    private boolean checkWinCondition() {
        String winner = null;
        if (myHand.isEmpty()) winner = playerName;
        else if (p1Hand.isEmpty()) winner = "Player 1";
        else if (p2Hand.isEmpty()) winner = "Player 2";

        if (winner != null) {
            String finalWinner = winner;
            broadcast("WIN:" + finalWinner);
            Platform.runLater(() -> showWinScreen(finalWinner + " CHIẾN THẮNG!"));
            return true; // Có người thắng
        }
        return false; // Chưa ai thắng
    }

    private void nextTurn() {
        currentPlayerIndex = getNextIndex();
        broadcast("TURN:" + currentPlayerIndex);
        updateUI();

        String turnOwner;
        if (currentPlayerIndex == myIndex) {
            turnOwner = "LƯỢT CỦA BẠN!";
            statusLabel.setTextFill(Color.web(GOLD_COLOR));
        } else {
            // Lấy profile để lấy tên hiển thị
            PlayerProfileUI profile = getProfileByIndex(currentPlayerIndex);
            turnOwner = "LƯỢT CỦA: " + profile.getName().toUpperCase();
            statusLabel.setTextFill(Color.LIGHTGRAY);
        }
        
        statusLabel.setText(turnOwner);
        showStatusOverlay(turnOwner, 1.0); // Hiển thị thông báo giữa màn hình cho rõ
        
        // Logic cho Bot chơi tiếp
        if (currentPlayerIndex != myIndex && isPlayerBot[currentPlayerIndex]) {
            double speed = myHand.isEmpty() ? 0.5 : 1.5;
            PauseTransition pt = new PauseTransition(Duration.seconds(speed));
            pt.setOnFinished(e -> botAutoPlay(currentPlayerIndex));
            pt.play();
        }
    }

    private void broadcast(String msg) {
        for (int i = 0; i < clientWriters.size(); i++) {
            if (!isPlayerBot[i + 1]) { try { clientWriters.get(i).println(msg); } catch (Exception e) {} }
        }
    }

    private void updateAllClients(String reason) {
        if (clientWriters.size() > 0 && !isPlayerBot[1]) {
            StringBuilder sb = new StringBuilder("HAND:");
            for(Card c : p1Hand) sb.append(c.toStringCode()).append(",");
            clientWriters.get(0).println(sb.toString());
            clientWriters.get(0).println("COUNTS:" + myHand.size() + ":" + p2Hand.size());
            if (!discardPile.isEmpty()) clientWriters.get(0).println("TOP:" + discardPile.get(discardPile.size()-1).toStringCode());
        }
        if (clientWriters.size() > 1 && !isPlayerBot[2]) {
            StringBuilder sb = new StringBuilder("HAND:");
            for(Card c : p2Hand) sb.append(c.toStringCode()).append(",");
            clientWriters.get(1).println(sb.toString());
            clientWriters.get(1).println("COUNTS:" + myHand.size() + ":" + p1Hand.size());
            if (!discardPile.isEmpty()) clientWriters.get(1).println("TOP:" + discardPile.get(discardPile.size()-1).toStringCode());
        }
        broadcast("COUNTS:" + myHand.size() + ":" + p1Hand.size() + ":" + p2Hand.size());
    }

    // ================= CLIENT LOGIC =================
    private void startClientListener() {
        new Thread(() -> {
            try {
                String msg;
                while ((msg = inFromServer.readLine()) != null) {
                    String finalMsg = msg;
                    Platform.runLater(() -> processServerMessage(finalMsg));
                }
            } catch (IOException e) {
                Platform.runLater(() -> showWinScreen("Host Disconnected!"));
            }
        }).start();
    }

    private void processServerMessage(String msg) {
        if (msg.startsWith("INIT:")) {
            myIndex = Integer.parseInt(msg.split(":")[1]);
        }
        else if (msg.startsWith("GAME_SETUP:")) {
            // GAME_SETUP:Name0|Ava0,Name1|Ava1,Name2|Ava2
            String data = msg.substring(11);
            String[] parts = data.split(",");
            
            String[] hData = parts[0].split("\\|");
            String[] g1Data = parts[1].split("\\|");
            String[] g2Data = parts[2].split("\\|");

            // Logic cập nhật UI Client
            // Nếu tôi là P1: Phải là Host, Trái là P2
            // Nếu tôi là P2: Phải là Host, Trái là P1
            if (myIndex == 1) {
                rightProfile.updateInfo(hData[0], hData[1]);
                leftProfile.updateInfo(g2Data[0], g2Data[1]);
            } else if (myIndex == 2) {
                rightProfile.updateInfo(hData[0], hData[1]);
                leftProfile.updateInfo(g1Data[0], g1Data[1]);
            }
        }
        else if (msg.startsWith("HAND:")) {
            displayHand.clear();
            if (msg.length() > 5) {
                String[] parts = msg.substring(5).split(",");
                for (String code : parts) if (!code.isEmpty()) displayHand.add(Card.fromStringCode(code));
            }
            updateUI();
        }
        else if (msg.startsWith("PLAYED:")) {
            String[] parts = msg.split(":");
            int who = Integer.parseInt(parts[1]);
            Card c = Card.fromStringCode(parts[2]);
            discardPile.add(c);
            Node fromNode = getAvatarNodeByIndex(who);
            animateCard(createCardView(c, true), fromNode, centerPileStack, () -> updateUI());
        }
        else if (msg.startsWith("TURN:")) {
            currentPlayerIndex = Integer.parseInt(msg.split(":")[1]);
            updateUI();
            statusLabel.setText(currentPlayerIndex == myIndex ? "YOUR TURN!" : "Player " + currentPlayerIndex + "'s turn");
        }
        else if (msg.startsWith("COUNTS:")) {
            String[] p = msg.split(":");
            int hCount = Integer.parseInt(p[1]);
            int p1Count = Integer.parseInt(p[2]);
            int p2Count = Integer.parseInt(p[3]);
            
            // Cập nhật đúng vị trí dựa trên Index của mình
            Platform.runLater(() -> {
                String[] pi = msg.split(":");
                int h = Integer.parseInt(pi[1]);
                int p1 = Integer.parseInt(pi[2]);
                int p2 = Integer.parseInt(pi[3]);

                if (myIndex == 1) {
                    // Nếu tôi là P1: Bên phải là Host (index 0), bên trái là P2 (index 2)
                    renderBackCards(rightHandContainer, h, Pos.CENTER_RIGHT);
                    renderBackCards(leftHandContainer, p2, Pos.CENTER_LEFT);
                } else if (myIndex == 2) {
                    // Nếu tôi là P2: Bên phải là Host (index 0), bên trái là P1 (index 1)
                    renderBackCards(rightHandContainer, h, Pos.CENTER_RIGHT);
                    renderBackCards(leftHandContainer, p1, Pos.CENTER_LEFT);
                }
            });
        }
        else if (msg.startsWith("TOP:")) {
            Card c = Card.fromStringCode(msg.split(":")[1]);
            if(discardPile.isEmpty() || !discardPile.get(discardPile.size()-1).toStringCode().equals(c.toStringCode())) {
                discardPile.add(c); updateUI();
            }
        }
        else if (msg.startsWith("WIN:")) {
            showWinScreen(msg.split(":")[1] + " WINS!");
        }
        else if (msg.startsWith("COLOR:")) {
            currentWildColor = CardColor.valueOf(msg.split(":")[1]);
            updateUI();
        }
        else if (msg.startsWith("DRAW:")) {
            int who = Integer.parseInt(msg.split(":")[1]);
            Node from = deckStack;
            Node to = getAvatarNodeByIndex(who);
            animateCard(createCardBack(), from, to, null);
        }
    }

    private void handleDrawClick() {
        if (isAnimating || currentPlayerIndex != myIndex) return;

        if (isHost) {
            Card c = drawOne();
            if (c == null) return;

            // Chạy animation rút bài
            animateCardMovement(createCardBack(), deckStack, playerProfile, () -> {
                myHand.add(c);
                displayHand.add(c);
                broadcast("DRAW:0");
                updateUI();
                
                // QUAN TRỌNG: Chỉ chuyển lượt sau khi rút xong và cập nhật UI
                nextTurn(); 
            });
        } else {
            // Client chỉ gửi yêu cầu, không tự ý chạy logic rút ở đây để tránh lặp
            outToServer.println("DRAW");
        }
    }

    private void clientPlayCard(Card c) {
        if (currentPlayerIndex != myIndex) return;
        outToServer.println("PLAY:" + c.toStringCode());
    }

    // ================= HELPERS =================
    private void updateUI() {
        playerProfile.setActive(currentPlayerIndex == myIndex);
        
        int leftIdx = -1, rightIdx = -1;
        if (isHost) { rightIdx = 1; leftIdx = 2; } 
        else if (myIndex == 1) { rightIdx = 0; leftIdx = 2; }
        else if (myIndex == 2) { rightIdx = 0; leftIdx = 1; }
        
        leftProfile.setActive(currentPlayerIndex == leftIdx);
        rightProfile.setActive(currentPlayerIndex == rightIdx);

        centerPileStack.getChildren().clear();
        if (!discardPile.isEmpty()) centerPileStack.getChildren().add(createCardView(discardPile.get(discardPile.size()-1), true));

        if (currentWildColor != null) {
            colorIndicator.setText("Color: " + currentWildColor);
            colorIndicator.setTextFill(getColor(currentWildColor));
        } else { colorIndicator.setText(""); }
        directionArrow.setText(direction == 1 ? "⟳" : "⟲");

        playerCardContainer.getChildren().clear();
        Card top = discardPile.isEmpty() ? null : discardPile.get(discardPile.size()-1);
        boolean isMyTurn = (currentPlayerIndex == myIndex);

        for (Card c : new ArrayList<>(displayHand)) {
            Node node = createCardView(c, true);
            node.setOnMouseEntered(e -> { if(!isAnimating) { node.setTranslateY(-20); node.setViewOrder(-10); }});
            node.setOnMouseExited(e -> { if(!isAnimating) { node.setTranslateY(0); node.setViewOrder(0); }});
            node.setOnMouseClicked(e -> {
                // Thêm điều kiện !isShowingNotification
                if (isMyTurn && isValidMove(c, top) && !isAnimating && !isShowingNotification) {
                    if (isHost) processCardPlayed(0, c, false);
                    else clientPlayCard(c);
                }
            });
            playerCardContainer.getChildren().add(node);
        }
        playerCardContainer.setOpacity(isMyTurn ? 1.0 : 0.6);
        if (isHost) {
            updateOpponentCounts(p1Hand.size(), p2Hand.size());
            broadcast("COUNTS:" + myHand.size() + ":" + p1Hand.size() + ":" + p2Hand.size());
        }
    }

    private void updateOpponentCounts(int n1, int n2) {
        // n1, n2 là số lượng bài nhận được từ gói tin COUNTS
        // Giả định: n1 là người bên phải, n2 là người bên trái
        if (isHost) {
            renderBackCards(rightHandContainer, n1, Pos.CENTER_RIGHT);
            renderBackCards(leftHandContainer, n2, Pos.CENTER_LEFT);
        } else {
            // Client logic tương tự dựa trên myIndex
            if (myIndex == 1) { // Tôi là P1, Host bên phải, P2 bên trái
                renderBackCards(rightHandContainer, n1, Pos.CENTER_RIGHT);
                renderBackCards(leftHandContainer, n2, Pos.CENTER_LEFT);
            } else { // Tôi là P2, Host bên phải, P1 bên trái
                renderBackCards(rightHandContainer, n1, Pos.CENTER_RIGHT);
                renderBackCards(leftHandContainer, n2, Pos.CENTER_LEFT);
            }
        }
    }

    private void renderBackCards(HBox box, int count, Pos alignment) {
        box.getChildren().clear();
        box.setSpacing(-50); // Khoảng cách âm để bài xếp chồng (Overlap)
        box.setAlignment(alignment);

        int maxDisplay = 7; 
        for (int i = 0; i < Math.min(count, maxDisplay); i++) {
            StackPane cardBack = createCardBack(0.5); // Bài Bot nhỏ bằng 50% bài User
            box.getChildren().add(cardBack);
        }

        if (count > maxDisplay) {
            Label more = new Label("+" + (count - maxDisplay));
            more.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
            // Đẩy label ra ngoài một chút để không bị lá bài cuối che mất
            HBox.setMargin(more, new Insets(0, 0, 0, 55)); 
            box.getChildren().add(more);
        }
    }
    private void updateCompactHand(HBox box, int count) {
        box.getChildren().clear();
        int maxDisplay = 6; // Hiển thị tối đa 6 lá để tránh tràn màn hình
        for (int i = 0; i < Math.min(count, maxDisplay); i++) {
            StackPane backCard = createCardBack(0.4); // Tạo mặt sau lá bài với tỉ lệ nhỏ
            // Hiệu ứng xếp chồng (overlapping)
            if (i > 0) HBox.setMargin(backCard, new Insets(0, 0, 0, -25)); 
            box.getChildren().add(backCard);
        }
        // Nếu Bot có nhiều bài hơn maxDisplay, hiển thị thêm số lượng
        if (count > maxDisplay) {
            Label extra = new Label("+" + (count - maxDisplay));
            extra.setTextFill(Color.WHITE);
            extra.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            box.getChildren().add(extra);
        }
    }

    private void showColorDialog(Card playedCard) {
        ChoiceDialog<CardColor> d = new ChoiceDialog<>(CardColor.RED, CardColor.RED, CardColor.GREEN, CardColor.BLUE, CardColor.YELLOW);
        d.setTitle("Chọn màu"); d.setHeaderText("Hasagi! Chọn màu đi:");
        currentWildColor = d.showAndWait().orElse(CardColor.RED);
        broadcast("COLOR:" + currentWildColor);
        finishAction(playedCard);
    }

    private Node getAvatarNodeByIndex(int idx) {
        if (idx == myIndex) return playerProfile.getAvatarNode();
        if (isHost) return (idx == 1) ? rightProfile.getAvatarNode() : leftProfile.getAvatarNode();
        if (myIndex == 1) return (idx == 0) ? rightProfile.getAvatarNode() : leftProfile.getAvatarNode();
        if (myIndex == 2) return (idx == 0) ? rightProfile.getAvatarNode() : leftProfile.getAvatarNode();
        return playerProfile.getAvatarNode();
    }

    private List<Card> getHandByIndex(int idx) {
        if (idx == 0) return myHand; if (idx == 1) return p1Hand; return p2Hand;
    }
    private void initializeDeck() {
        deck.clear(); discardPile.clear();
        for(CardColor c : CardColor.values()) {
            if(c == CardColor.BLACK) continue;
            deck.add(new Card(c, CardType.NUMBER, 0));
            for(int i=1; i<=9; i++) { deck.add(new Card(c, CardType.NUMBER, i)); deck.add(new Card(c, CardType.NUMBER, i)); }
            for(int i=0; i<2; i++) { deck.add(new Card(c, CardType.SKIP, -1)); deck.add(new Card(c, CardType.REVERSE, -1)); deck.add(new Card(c, CardType.DRAW2, -1)); }
        }
        for(int i=0; i<4; i++) { deck.add(new Card(CardColor.BLACK, CardType.WILD, -1)); deck.add(new Card(CardColor.BLACK, CardType.WILD_DRAW4, -1)); }
        Collections.shuffle(deck); discardPile.add(deck.remove(0));
    }
    private Card drawOne() {
        if (deck.isEmpty()) { 
            if (discardPile.size() <= 1) return null;
            Card top = discardPile.remove(discardPile.size()-1); deck.addAll(discardPile); discardPile.clear(); discardPile.add(top); Collections.shuffle(deck);
        }
        return deck.remove(0);
    }
    private void drawN(int idx, int n) {
        List<Card> h = getHandByIndex(idx); for(int i=0; i<n; i++) h.add(drawOne());
        broadcast("DRAW:" + idx); updateAllClients("DrawN");
    }
    private int getNextIndex() {
        int n = currentPlayerIndex + direction;
        if (n > 2) n = 0;
        if (n < 0) n = 2;
        return n;
    }
    private boolean isValidMove(Card c, Card top) {
        if (top == null) return true;
        CardColor active = (top.color == CardColor.BLACK && currentWildColor != null) ? currentWildColor : top.color;
        return c.color == CardColor.BLACK || c.color == active || (c.type == top.type && c.type != CardType.NUMBER) || (c.type == CardType.NUMBER && top.type == CardType.NUMBER && c.number == top.number);
    }
    private StackPane createCardView(Card c, boolean front) {
        StackPane s = new StackPane();
        s.setPrefSize(90, 135);

        Rectangle border = new Rectangle(90, 135);
        border.setArcWidth(20); border.setArcHeight(20);
        border.setFill(Color.WHITE);
        border.setStroke(Color.web("#333333"));

        Rectangle bg = new Rectangle(82, 127);
        bg.setArcWidth(18); bg.setArcHeight(18);

        if (front && c != null) {
            Color mainColor = c.getFxColor();
            // Hiệu ứng Gradient cho nền lá bài
            bg.setFill(new LinearGradient(0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, mainColor),
                new javafx.scene.paint.Stop(1, mainColor.darker())));

            // Hình Oval trắng đặc trưng ở giữa
            Ellipse oval = new Ellipse(32, 48);
            oval.setFill(Color.web("#ffffff", 0.25));
            oval.setRotate(25);

            // Ký hiệu chính ở giữa
            Node symbolNode;
            switch (c.type) {
                case SKIP: symbolNode = createSkipSymbol(); break;
                case REVERSE: symbolNode = createReverseSymbol(); break;
                case DRAW2: symbolNode = createDrawSymbol("+2"); break;
                case WILD_DRAW4: symbolNode = createDrawSymbol("+4"); break;
                default:
                    Text t = new Text(c.getSymbol());
                    t.setFont(Font.font("Arial Black", FontWeight.BOLD, 50));
                    t.setFill(Color.WHITE);
                    t.setStroke(Color.BLACK); t.setStrokeWidth(0.5);
                    symbolNode = t;
            }

            // Ký hiệu nhỏ ở góc
            Text smallSymbol = new Text(c.getSymbol());
            smallSymbol.setFont(Font.font("Arial Black", 16));
            smallSymbol.setFill(Color.WHITE);
            StackPane.setAlignment(smallSymbol, Pos.TOP_LEFT);
            StackPane.setMargin(smallSymbol, new Insets(8));

            s.getChildren().addAll(border, bg, oval, symbolNode, smallSymbol);
        } else {
            // Mặt sau lá bài chuẩn UNO
        	bg.setFill(new LinearGradient(0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, Color.web("#D72600")),
                    new javafx.scene.paint.Stop(1, Color.web("#000000"))));
                Text logo = new Text("UNO");
                logo.setFont(Font.font("Arial Black", FontWeight.BOLD, 20));
                logo.setFill(Color.YELLOW);
                logo.setRotate(-30);
                s.getChildren().addAll(border, bg, logo);
        }

        s.setEffect(new DropShadow(8, Color.web("#000000", 0.5)));
        return s;
    }
    private void animateCardMovement(Node cardNode, Node fromNode, Node toNode, Runnable onFinish) {
        isAnimating = true;
        animationLayer.getChildren().add(cardNode);

        // Lấy tọa độ tuyệt đối trên Scene
        Bounds fromBounds = fromNode.localToScene(fromNode.getBoundsInLocal());
        Bounds toBounds = toNode.localToScene(toNode.getBoundsInLocal());

        cardNode.setLayoutX(fromBounds.getMinX());
        cardNode.setLayoutY(fromBounds.getMinY());

        TranslateTransition tt = new TranslateTransition(Duration.millis(600), cardNode);
        tt.setByX(toBounds.getMinX() - fromBounds.getMinX());
        tt.setByY(toBounds.getMinY() - fromBounds.getMinY());
        
        // Thêm hiệu ứng xoay nhẹ khi bay cho đẹp
        cardNode.setRotate(new Random().nextInt(20) - 10);

        tt.setOnFinished(e -> {
            animationLayer.getChildren().remove(cardNode);
            isAnimating = false;
            if (onFinish != null) onFinish.run();
        });
        tt.play();
    }
 // Vẽ ký hiệu Đổi chiều (Hai mũi tên ngược nhau)
    private Node createReverseSymbol() {
        StackPane container = new StackPane();
        container.setAlignment(Pos.CENTER);

        // Mũi tên trên hướng sang trái
        Path arrow1 = new Path();
        arrow1.getElements().addAll(
            new MoveTo(0, 5), new LineTo(30, 5), // Thân
            new MoveTo(0, 5), new LineTo(10, 0), // Cạnh đầu 1
            new MoveTo(0, 5), new LineTo(10, 10) // Cạnh đầu 2
        );
        arrow1.setStroke(Color.WHITE);
        arrow1.setStrokeWidth(4);

        // Mũi tên dưới hướng sang phải (Xoay 180 độ)
        Path arrow2 = new Path();
        arrow2.getElements().addAll(
            new MoveTo(0, 5), new LineTo(30, 5),
            new MoveTo(0, 5), new LineTo(10, 0),
            new MoveTo(0, 5), new LineTo(10, 10)
        );
        arrow2.setStroke(Color.WHITE);
        arrow2.setStrokeWidth(4);
        arrow2.setRotate(180);
        arrow2.setTranslateY(15);

        container.getChildren().addAll(arrow1, arrow2);
        return container;
    }

    // Vẽ ký hiệu Cộng bài (+2 hoặc +4 với các lá bài nhỏ xếp chồng)
    private Node createSkipSymbol() {
        StackPane stack = new StackPane();
        Circle circle = new Circle(22);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(6);
        Rectangle bar = new Rectangle(38, 6);
        bar.setFill(Color.WHITE);
        bar.setRotate(45);
        stack.getChildren().addAll(circle, bar);
        return stack;
    }

    private Node createDrawSymbol(String val) {
        VBox box = new VBox(-8);
        box.setAlignment(Pos.CENTER);
        Text t = new Text(val);
        t.setFont(Font.font("Arial Black", 30));
        t.setFill(Color.WHITE);
        
        HBox cards = new HBox(-12);
        cards.setAlignment(Pos.CENTER);
        for(int i=0; i<2; i++) {
            Rectangle r = new Rectangle(18, 26, Color.WHITE);
            r.setArcWidth(4); r.setArcHeight(4);
            r.setStroke(Color.GRAY);
            r.setRotate(i * 15);
            cards.getChildren().add(r);
        }
        box.getChildren().addAll(t, cards);
        return box;
    }
    private StackPane createCardBack() { return createCardView(null, false); }
    private StackPane createCardBack(double scale) { StackPane s = createCardBack(); s.setScaleX(scale); s.setScaleY(scale); return s; }
    private Label createLabel(String text, int size) { Label l = new Label(text); l.setFont(Font.font("Arial", FontWeight.BOLD, size)); l.setTextFill(Color.LIGHTGRAY); return l; }
    private HBox createCompactHandView() { HBox box = new HBox(-40); box.setAlignment(Pos.CENTER); return box; }
    private Color getColor(CardColor c) { switch(c) { case RED: return Color.RED; case GREEN: return Color.GREEN; case BLUE: return Color.CYAN; case YELLOW: return Color.YELLOW; default: return Color.WHITE; } }
    private void animateCard(Node node, Node from, Node to, Runnable onFinish) {
        isAnimating = true; animationLayer.getChildren().add(node);
        Bounds bFrom = from.localToScene(from.getBoundsInLocal()); Bounds bTo = to.localToScene(to.getBoundsInLocal());
        node.setLayoutX(bFrom.getMinX()); node.setLayoutY(bFrom.getMinY());
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), node);
        tt.setByX(bTo.getMinX() - bFrom.getMinX()); tt.setByY(bTo.getMinY() - bFrom.getMinY());
        tt.setOnFinished(e -> { animationLayer.getChildren().remove(node); isAnimating = false; if (onFinish != null) onFinish.run(); });
        tt.play();
    }
 // Cách 1: Chỉ hiện thông báo (tự động điền null cho hành động tiếp theo)
    private void showStatusOverlay(String msg, double seconds) {
        showStatusOverlay(msg, seconds, null);
    }

    // Cách 2: Hiện thông báo và chạy hành động sau khi ẩn
    private void showStatusOverlay(String msg, double seconds, Runnable onComplete) {
        Platform.runLater(() -> {
            notificationQueue.add(new NotificationTask(msg, seconds, onComplete));
            processNotificationQueue();
        });
    }

// Class phụ để lưu Task
private static class NotificationTask {
    String msg; double seconds; Runnable onComplete;
    NotificationTask(String m, double s, Runnable c) { msg = m; seconds = s; onComplete = c; }
}

private void processNotificationQueue() {
    if (isShowingNotification || notificationQueue.isEmpty()) return;

    isShowingNotification = true;
    NotificationTask task = notificationQueue.poll();

    Label label = new Label(task.msg);
    label.setStyle("-fx-background-color: rgba(9, 20, 40, 0.9); -fx-text-fill: #C8AA6E; " +
                   "-fx-font-size: 35; -fx-padding: 25 50; -fx-background-radius: 50; " +
                   "-fx-border-color: #C8AA6E; -fx-border-width: 3;");
    label.setEffect(new DropShadow(20, Color.BLACK));
    StackPane.setAlignment(label, Pos.CENTER);
    
    rootStack.getChildren().add(label);

    FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.4), label);
    fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);
    
    PauseTransition stay = new PauseTransition(Duration.seconds(task.seconds));
    
    FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.4), label);
    fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);

    javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition(fadeIn, stay, fadeOut);
    seq.setOnFinished(e -> {
        rootStack.getChildren().remove(label);
        isShowingNotification = false;
        if (task.onComplete != null) task.onComplete.run(); // Chạy hành động tiếp theo (ví dụ nextTurn)
        processNotificationQueue(); // Kiểm tra thông báo tiếp theo
    });
    seq.play();
}
private void animatePenaltyDraw(int victimIdx, int remaining) {
    if (remaining <= 0) {
        isAnimating = false; 
        return;
    }

    isAnimating = true;
    Node targetNode = getAvatarNodeByIndex(victimIdx);
    StackPane cardBack = createCardBack(0.5);

    animateCardMovement(cardBack, deckStack, targetNode, () -> {
        Platform.runLater(() -> {
            // Nếu nạn nhân là chính mình, phải cập nhật danh sách bài hiển thị
            if (victimIdx == myIndex) {
                displayHand.clear();
                displayHand.addAll(myHand);
            }
            
            updateUI(); 
            animatePenaltyDraw(victimIdx, remaining - 1);
        });
    });
}
private void refreshUserHand() {
    Platform.runLater(() -> {
        displayHand.clear();
        displayHand.addAll(myHand);
        updateUI();
    });
}
private void showWinScreen(String msg) {
    Platform.runLater(() -> {
        // --- 1. OVERLAY NỀN MỜ ---
        StackPane winOverlay = new StackPane();
        winOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        // --- 2. HIỆU ỨNG TEXT CHIẾN THẮNG ---
        Text winText = new Text(msg.toUpperCase());
        winText.setFont(Font.font("Arial Rounded MT Bold", FontWeight.EXTRA_BOLD, 70));
        winText.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#F9C700")), // Vàng sáng
                new Stop(1, Color.web("#D72600")))); // Đỏ cam
        
        winText.setStroke(Color.WHITE);
        winText.setStrokeWidth(3);
        
        DropShadow ds = new DropShadow(20, Color.GOLD);
        winText.setEffect(ds);

        // Animation phóng to thu nhỏ cho chữ
        ScaleTransition st = new ScaleTransition(Duration.millis(800), winText);
        st.setFromX(0.5); st.setFromY(0.5);
        st.setToX(1.1);   st.setToY(1.1);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        st.play();

        // --- 3. NÚT QUAY LẠI MENU ---
        Button btnBackToMenu = new Button("QUAY LẠI MENU");
        btnBackToMenu.setFont(Font.font("System", FontWeight.BOLD, 20));
        btnBackToMenu.setTextFill(Color.WHITE);
        btnBackToMenu.setPrefSize(250, 60);
        btnBackToMenu.setStyle("-fx-background-color: linear-gradient(#2ecc71, #27ae60); " +
                               "-fx-background-radius: 30; -fx-border-color: white; -fx-border-radius: 30; -fx-border-width: 2;");
        
        btnBackToMenu.setCursor(javafx.scene.Cursor.HAND);
        btnBackToMenu.setOnAction(e -> backToMenu());

        // Hiệu ứng hover cho nút
        btnBackToMenu.setOnMouseEntered(e -> btnBackToMenu.setScaleX(1.1));
        btnBackToMenu.setOnMouseExited(e -> btnBackToMenu.setScaleX(1.0));

        content.getChildren().addAll(winText, btnBackToMenu);
        winOverlay.getChildren().add(content);
        
        // Hiệu ứng Fade In cho toàn bộ màn hình win
        FadeTransition ft = new FadeTransition(Duration.millis(1000), winOverlay);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        
        rootStack.getChildren().add(winOverlay);
        ft.play();
    });
}

// Logic quay trở lại Menu
private void backToMenu() {
    try {
        // Đóng các kết nối nếu là Host hoặc Client
        if (isHost) {
            for (PrintWriter pw : clientWriters) pw.println("HOST_DISCONNECTED");
        } else if (clientSocket != null) {
            clientSocket.close();
        }
        
        // Chuyển cảnh về UnoGameMenu
        UnoGameMenu menu = new UnoGameMenu();
        menu.start(mainStage);
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    private void showColorPickerDialog(Card playedCard) {
        Platform.runLater(() -> {
            VBox container = new VBox(20);
            container.setAlignment(Pos.CENTER);
            container.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-background-radius: 20; -fx-padding: 40;");
            container.setMaxSize(500, 300);

            Label title = new Label("CHỌN MÀU MUỐN ĐỔI");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 24; -fx-font-weight: bold;");

            HBox colorsHand = new HBox(15);
            colorsHand.setAlignment(Pos.CENTER);

            for (CardColor color : new CardColor[]{CardColor.RED, CardColor.GREEN, CardColor.BLUE, CardColor.YELLOW}) {
                // Tạo một lá bài giả đại diện cho màu
                StackPane colorCard = createColorSelectionCard(color);
                colorCard.setOnMouseClicked(e -> {
                    currentWildColor = color;
                    rootStack.getChildren().remove(container); // Đóng bảng chọn
                    broadcast("COLOR:" + currentWildColor);
                    showStatusOverlay("MÀU MỚI: " + currentWildColor, 1.5);
                    finishAction(playedCard); // Tiếp tục luồng sau khi chọn màu
                });
                colorsHand.getChildren().add(colorCard);
            }

            container.getChildren().addAll(title, colorsHand);
            rootStack.getChildren().add(container); // Thêm vào rootStack để hiển thị đè lên game
        });
    }

    private StackPane createColorSelectionCard(CardColor color) {
        StackPane s = new StackPane();
        Rectangle rect = new Rectangle(80, 120);
        rect.setArcWidth(15); rect.setArcHeight(15);
        
        // Gán màu theo CardColor
        Color fxColor;
        switch(color) {
            case RED: fxColor = Color.web("#e74c3c"); break;
            case GREEN: fxColor = Color.web("#2ecc71"); break;
            case BLUE: fxColor = Color.web("#3498db"); break;
            case YELLOW: fxColor = Color.web("#f1c40f"); break;
            default: fxColor = Color.GRAY;
        }
        rect.setFill(fxColor);
        rect.setStroke(Color.WHITE);
        rect.setStrokeWidth(3);

        Text t = new Text(color.toString());
        t.setFont(Font.font("Arial Black", 12));
        t.setFill(Color.WHITE);

        s.getChildren().addAll(rect, t);
        s.setStyle("-fx-cursor: hand;");
        
        // Hiệu ứng hover
        s.setOnMouseEntered(e -> s.setScaleX(1.1));
        s.setOnMouseExited(e -> s.setScaleX(1.0));
        
        return s;
    }
    private class PlayerProfileUI extends VBox {
        private Circle border; private ImageView img; private Label nameLabel;;
        public PlayerProfileUI(String n, String url, Pos align) {
            setAlignment(align); setSpacing(5);
            border = new Circle(42, Color.TRANSPARENT); 
            border.setStroke(Color.web("#5c5b57")); 
            border.setStrokeWidth(3);
            img = new ImageView(new Image(url, true)); 
            img.setFitWidth(80); img.setFitHeight(80); 
            img.setClip(new Circle(40,40,40));
            nameLabel = new Label(n); 
            nameLabel.setTextFill(Color.WHITE); 
            nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            getChildren().addAll(new StackPane(border, img), nameLabel);
        }
        public String getName() {
            return nameLabel.getText();
        }
        public void updateInfo(String n, String url) { 
            this.nameLabel.setText(n); 
            if(url != null) this.img.setImage(new Image(url)); 
        }        public void setActive(boolean a) {
            if(a) { border.setStroke(Color.web(GOLD_COLOR)); border.setEffect(new Glow(0.8)); nameLabel.setTextFill(Color.web(GOLD_COLOR)); }
            else { border.setStroke(Color.web("#5c5b57")); border.setEffect(null); nameLabel.setTextFill(Color.WHITE); }
        }
        public Node getAvatarNode() { return border; }
        public void setUnoGlow(boolean active) {
            if (active) {
                this.border.setStroke(Color.GOLD);
                this.border.setStrokeWidth(5);
                this.border.setEffect(new Glow(1.0));
            } else {
                this.border.setStroke(Color.web("#5c5b57"));
                this.border.setStrokeWidth(3);
                this.border.setEffect(null);
            }
        }
    }
    public static void main(String[] args) { launch(args); }
}