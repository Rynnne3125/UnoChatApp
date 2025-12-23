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
                if (code == null || code.trim().isEmpty()) return null;
                String[] parts = code.split("_");
                if (parts.length < 3) return null;
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
            if (color == null) return Color.BLACK;
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
    private boolean isSelectingColor = false; // Chặn đánh bài khi đang chọn màu

    // Host variables: Lưu bài Wild đang chờ xử lý
    private Card pendingWildCard = null;

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
    private String myAvatar, hostAvatar, guest1Name, guest1Avatar, guest2Name, guest2Avatar;

    // 1. Constructor HOST
    public UnoGameApp(String hostName, boolean isHost, List<Socket> sockets, List<PrintWriter> writers,
            String hostAvatar, String guest1Name, String guest1Avatar, String guest2Name, String guest2Avatar) {
        this.playerName = (hostName != null) ? hostName : "Host";
        this.isHost = true;
        this.clientSockets = sockets;
        this.clientWriters = writers;
        this.myIndex = 0;
        this.myAvatar = hostAvatar;
        this.hostAvatar = (hostAvatar != null) ? hostAvatar : IMG_YASUO;
        this.guest1Name = (guest1Name != null) ? guest1Name : "Bot Ahri";
        this.guest1Avatar = (guest1Avatar != null) ? guest1Avatar : IMG_AHRI;
        this.guest2Name = (guest2Name != null) ? guest2Name : "Bot Teemo";
        this.guest2Avatar = (guest2Avatar != null) ? guest2Avatar : IMG_TEEMO;

        if (sockets.size() < 1) isPlayerBot[1] = true;
        if (sockets.size() < 2) isPlayerBot[2] = true;
    }

    // 2. Constructor CLIENT
    public UnoGameApp(String playerName, boolean isHost, Socket socket, PrintWriter out, BufferedReader in, String myAvatar) {
        this.playerName = (playerName != null) ? playerName : "Player";
        this.isHost = false;
        this.clientSocket = socket;
        this.outToServer = out;
        this.inFromServer = in;
        this.myAvatar = myAvatar;
    }

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        stage.setTitle("UNO Online - " + playerName + (isHost ? " (HOST)" : " (CLIENT)"));
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

        HBox topArea = new HBox(150);
        topArea.setAlignment(Pos.CENTER);
        topArea.setPadding(new Insets(20));

        leftProfile = new PlayerProfileUI((isHost) ? guest2Name : "Waiting...", (isHost) ? guest2Avatar : IMG_TEEMO, Pos.CENTER_LEFT);
        rightProfile = new PlayerProfileUI((isHost) ? guest1Name : "Waiting...", (isHost) ? guest1Avatar : IMG_AHRI, Pos.CENTER_RIGHT);

        leftHandContainer = createCompactHandView();  
        rightHandContainer = createCompactHandView(); 

        renderBackCards(leftHandContainer, 7, Pos.CENTER_LEFT);
        renderBackCards(rightHandContainer, 7, Pos.CENTER_RIGHT);

        HBox leftWrapper = new HBox(10, leftProfile, leftHandContainer); leftWrapper.setAlignment(Pos.CENTER_LEFT);
        HBox rightWrapper = new HBox(10, rightHandContainer, rightProfile); rightWrapper.setAlignment(Pos.CENTER_RIGHT);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        topArea.getChildren().addAll(leftWrapper, spacer, rightWrapper);

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
        
        String setupMsg = "GAME_SETUP:" + playerName + "|" + hostAvatar + "," + guest1Name + "|" + guest1Avatar + "," + guest2Name + "|" + guest2Avatar;
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
        String botName = (pIndex == 1) ? "Bot Ahri" : "Bot Teemo";
        String botAva = (pIndex == 1) ? IMG_AHRI : IMG_TEEMO;
        if(pIndex == 1) { guest1Name = botName; guest1Avatar = botAva; } else { guest2Name = botName; guest2Avatar = botAva; }
        Platform.runLater(() -> {
            broadcastMessage("Player " + pIndex + " thoát! Bot thay thế.", 2.0);
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
            if (c != null) {
                Platform.runLater(() -> {
                    if (currentPlayerIndex == pIndex) processCardPlayed(pIndex, c, false);
                });
            }
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
            // Nhận màu từ Client
            try {
                CardColor color = CardColor.valueOf(msg.split(":")[1]);
                currentWildColor = color;
                
                // 1. Thông báo cho mọi người biết Client đã chọn màu gì
                broadcastMessage("ĐÃ CHỌN MÀU: " + color, 1.5);
                broadcast("COLOR:" + color);
                
                // 2. Kích hoạt hiệu ứng lá bài pending (ví dụ +4)
                Platform.runLater(() -> {
                    if (pendingWildCard != null) {
                        finishAction(pendingWildCard);
                        pendingWildCard = null; // Xóa trạng thái pending sau khi xử lý
                    } else {
                        // Fallback nếu lỗi
                        finishAction(new Card(CardColor.BLACK, CardType.WILD, -1));
                    }
                });
            } catch (Exception e) {}
        }
    }

    private void botAutoPlay(int botIndex) {
        double speed = myHand.isEmpty() ? 0.3 : 1.2; 
        PauseTransition pause = new PauseTransition(Duration.seconds(speed));
        pause.setOnFinished(e -> {
            List<Card> hand = getHandByIndex(botIndex);
            Card top = discardPile.isEmpty() ? new Card(CardColor.RED, CardType.NUMBER, 0) : discardPile.get(discardPile.size() - 1);
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
        if (c == null) return;
        Node fromNode = getAvatarNodeByIndex(pIndex);
        StackPane animatedCard = createCardView(c, true);

        // Gửi tin nhắn PLAYED để Client đồng bộ
        broadcast("PLAYED:" + pIndex + ":" + c.toStringCode());

        animateCardMovement(animatedCard, fromNode, centerPileStack, () -> {
            List<Card> hand = getHandByIndex(pIndex);
            hand.removeIf(card -> card.toStringCode().equals(c.toStringCode())); 
            discardPile.add(c);

            if (pIndex == myIndex) refreshUserHand();
            updateUI(); 

            // LOGIC XỬ LÝ LÁ BÀI
            Runnable continueLogic = () -> {
                if (c.color == CardColor.BLACK) {
                    if (isBot) {
                        // Bot tự chọn màu
                        currentWildColor = CardColor.values()[new Random().nextInt(4)];
                        broadcastMessage("BOT CHỌN MÀU: " + currentWildColor, 1.5);
                        broadcast("COLOR:" + currentWildColor);
                        finishAction(c);
                    } else if (pIndex == myIndex) { 
                        // Host đánh -> Host tự chọn màu
                        showColorPickerDialog(c);
                    } else {
                        // Client đánh -> Host chờ tin nhắn COLOR từ Client
                        // QUAN TRỌNG: Lưu lá bài lại và CHƯA gọi finishAction
                        pendingWildCard = c;
                    }
                } else {
                    // Bài thường hoặc bài chức năng có màu -> Kết thúc luôn
                    finishAction(c);
                }
            };

            if (hand.size() == 1) {
                String name = (pIndex == myIndex ? "BẠN" : getProfileByIndex(pIndex).getNameSafe().toUpperCase());
                broadcastMessage(name + " HÔ: UNO!", 1.5);
                continueLogic.run();
            } else {
                continueLogic.run();
            }
        });
    }

    private void broadcastMessage(String text, double seconds) {
        showStatusOverlay(text, seconds); // Host hiện
        broadcast("MSG:" + seconds + ":" + text); // Client hiện
    }

    private void finishAction(Card c) {
        if (checkWinCondition()) return;

        Runnable nextTurnStep = () -> {
            if (mainStage.isShowing()) nextTurn();
        };

        switch (c.type) {
            case SKIP:
                String targetName = getProfileByIndex(getNextIndex()).getNameSafe(); 
                broadcastMessage("CẤM LƯỢT: " + targetName.toUpperCase(), 1.2);
                currentPlayerIndex = getNextIndex();
                PauseTransition delaySkip = new PauseTransition(Duration.seconds(1.2));
                delaySkip.setOnFinished(e -> nextTurnStep.run());
                delaySkip.play();
                break;

            case REVERSE:
                direction *= -1;
                broadcastMessage(direction == 1 ? "ĐỔI CHIỀU: THUẬN ⟳" : "ĐỔI CHIỀU: NGƯỢC ⟲", 1.2);
                PauseTransition delayRev = new PauseTransition(Duration.seconds(1.2));
                delayRev.setOnFinished(e -> nextTurnStep.run());
                delayRev.play();
                break;

            case DRAW2:
            case WILD_DRAW4:
                int victim = getNextIndex();
                int amount = (c.type == CardType.DRAW2) ? 2 : 4;
                String victimName = getProfileByIndex(victim).getNameSafe();

                broadcastMessage(victimName.toUpperCase() + " BỊ PHẠT +" + amount, 1.5);
                
                Platform.runLater(() -> {
                    animatePenaltyDraw(victim, amount); 
                    drawN(victim, amount);
                    refreshUserHand();
                    currentPlayerIndex = getNextIndex(); 
                    
                    PauseTransition delay = new PauseTransition(Duration.millis(amount * 300 + 1500));
                    delay.setOnFinished(e -> nextTurnStep.run());
                    delay.play();
                });
                break;

            default:
                nextTurnStep.run();
                break;
        }
    }

    private void checkUno(int pIndex) {
        List<Card> hand = getHandByIndex(pIndex);
        if (hand.size() == 1) {
            getProfileByIndex(pIndex).setUnoGlow(true);
        } else {
            getProfileByIndex(pIndex).setUnoGlow(false);
        }
    }

    private PlayerProfileUI getProfileByIndex(int idx) {
        if (idx == myIndex) return playerProfile;
        if (isHost) return (idx == 1) ? rightProfile : leftProfile;
        else {
            if (myIndex == 1) return (idx == 0) ? rightProfile : leftProfile;
            if (myIndex == 2) return (idx == 0) ? rightProfile : leftProfile;
        }
        return playerProfile;
    }

    private boolean checkWinCondition() {
        String winner = null;
        
        // Kiểm tra bài của bản thân (My Index)
        if (myHand.isEmpty()) {
            winner = playerName; // Hoặc getProfileByIndex(myIndex).getNameSafe();
        } 
        // Kiểm tra bài của Player 1 (Index 1)
        else if (p1Hand.isEmpty()) {
            // CŨ: winner = "Player 1";
            // MỚI: Lấy tên từ Profile của Index 1
            winner = getProfileByIndex(1).getNameSafe(); 
        } 
        // Kiểm tra bài của Player 2 (Index 2)
        else if (p2Hand.isEmpty()) {
            // CŨ: winner = "Player 2";
            // MỚI: Lấy tên từ Profile của Index 2
            winner = getProfileByIndex(2).getNameSafe();
        }

        if (winner != null) {
            String finalWinner = winner;
            // Gửi thông báo thắng cho tất cả các client
            broadcast("WIN:" + finalWinner);
            // Hiển thị màn hình thắng
            Platform.runLater(() -> showWinScreen(finalWinner + " Winner!"));
            return true; 
        }
        return false; 
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
            PlayerProfileUI profile = getProfileByIndex(currentPlayerIndex);
            turnOwner = "LƯỢT CỦA: " + profile.getNameSafe().toUpperCase();
            statusLabel.setTextFill(Color.LIGHTGRAY);
        }
        statusLabel.setText(turnOwner);
        // Hiện overlay thông báo lượt cho cả Host (ở đây) và Client (qua message TURN)
        showStatusOverlay(turnOwner, 0.8);
        
        if (currentPlayerIndex != myIndex && isPlayerBot[currentPlayerIndex]) {
            double speed = myHand.isEmpty() ? 0.5 : 1.5;
            PauseTransition pt = new PauseTransition(Duration.seconds(speed));
            pt.setOnFinished(e -> botAutoPlay(currentPlayerIndex));
            pt.play();
        }
    }

    private void broadcast(String msg) {
        if (clientWriters == null) return;
        for (int i = 0; i < clientWriters.size(); i++) {
            if (!isPlayerBot[i + 1]) { 
                try { 
                    PrintWriter pw = clientWriters.get(i);
                    pw.println(msg);
                    pw.flush(); // BẮT BUỘC: Đẩy dữ liệu đi ngay lập tức
                } catch (Exception e) {} 
            }
        }
    }

    private void updateAllClients(String reason) {
        if (clientWriters.size() > 0 && !isPlayerBot[1]) {
            StringBuilder sb = new StringBuilder("HAND:");
            for(Card c : p1Hand) sb.append(c.toStringCode()).append(",");
            clientWriters.get(0).println(sb.toString());
            clientWriters.get(0).flush();
            clientWriters.get(0).println("COUNTS:" + myHand.size() + ":" + p2Hand.size());
            clientWriters.get(0).flush();
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
            String data = msg.substring(11);
            String[] parts = data.split(",");
            String[] hData = parts[0].split("\\|");
            String[] g1Data = parts[1].split("\\|");
            String[] g2Data = parts[2].split("\\|");
            if (myIndex == 1) {
                rightProfile.updateInfo(hData[0], hData[1]); leftProfile.updateInfo(g2Data[0], g2Data[1]);
            } else if (myIndex == 2) {
                rightProfile.updateInfo(hData[0], hData[1]); leftProfile.updateInfo(g1Data[0], g1Data[1]);
            }
        }
        else if (msg.startsWith("HAND:")) {
            myHand.clear(); displayHand.clear();
            if (msg.length() > 5) {
                String[] parts = msg.substring(5).split(",");
                for (String code : parts) {
                    Card c = Card.fromStringCode(code);
                    if (c != null) { myHand.add(c); displayHand.add(c); }
                }
            }
            updateUI();
        }
        else if (msg.startsWith("PLAYED:")) {
            String[] parts = msg.split(":");
            int who = Integer.parseInt(parts[1]);
            Card c = Card.fromStringCode(parts[2]);
            if (c == null) return;

            discardPile.add(c);
            if (who == myIndex) {
                 for (int i = 0; i < myHand.size(); i++) {
                     if (myHand.get(i).toStringCode().equals(c.toStringCode())) {
                         myHand.remove(i); break;
                     }
                 }
                 refreshUserHand();
            }

            Node fromNode = getAvatarNodeByIndex(who);
            animateCard(createCardView(c, true), fromNode, centerPileStack, () -> {
                updateUI();
                // Client hiển thị bảng chọn màu nếu là bài Wild của mình
                if (c.color == CardColor.BLACK && who == myIndex) {
                    showColorPickerDialog(c);
                }
            });
        }
        else if (msg.startsWith("TURN:")) {
            currentPlayerIndex = Integer.parseInt(msg.split(":")[1]);
            updateUI();
            
            // FIX: Hiển thị Overlay thông báo lượt bên Client
            String turnOwner = (currentPlayerIndex == myIndex) ? "LƯỢT CỦA BẠN!" : "LƯỢT CỦA: " + getProfileByIndex(currentPlayerIndex).getNameSafe().toUpperCase();
            statusLabel.setText(currentPlayerIndex == myIndex ? "YOUR TURN!" : "LƯỢT CỦA: " + getProfileByIndex(currentPlayerIndex).getNameSafe().toUpperCase());
            showStatusOverlay(turnOwner, 0.8);
        }
        else if (msg.startsWith("COUNTS:")) {
            String[] p = msg.split(":");
            if (p.length >= 4) {
                int h = Integer.parseInt(p[1]);
                int p1 = Integer.parseInt(p[2]);
                int p2 = Integer.parseInt(p[3]);
                Platform.runLater(() -> {
                    if (myIndex == 1) {
                        renderBackCards(rightHandContainer, h, Pos.CENTER_RIGHT);
                        renderBackCards(leftHandContainer, p2, Pos.CENTER_LEFT);
                    } else if (myIndex == 2) {
                        renderBackCards(rightHandContainer, h, Pos.CENTER_RIGHT);
                        renderBackCards(leftHandContainer, p1, Pos.CENTER_LEFT);
                    }
                });
            }
        }
        else if (msg.startsWith("TOP:")) {
            Card c = Card.fromStringCode(msg.split(":")[1]);
            if (c != null && (discardPile.isEmpty() || !discardPile.get(discardPile.size()-1).toStringCode().equals(c.toStringCode()))) {
                discardPile.add(c); updateUI();
            }
        }
        else if (msg.startsWith("MSG:")) {
            // Nhận thông báo đồng bộ từ Host (Màu đỏ, Cấm lượt...)
            String[] p = msg.split(":", 3);
            if (p.length >= 3) {
                double sec = Double.parseDouble(p[1]);
                showStatusOverlay(p[2], sec);
            }
        }
        else if (msg.startsWith("WIN:")) {
            showWinScreen(msg.split(":")[1] + " WINS!");
        }
        else if (msg.startsWith("COLOR:")) {
            try {
                currentWildColor = CardColor.valueOf(msg.split(":")[1]);
                updateUI();
            } catch(Exception e){}
        }
        else if (msg.startsWith("DRAW:")) {
            int who = Integer.parseInt(msg.split(":")[1]);
            Node from = deckStack;
            Node to = getAvatarNodeByIndex(who);
            animateCard(createCardBack(), from, to, null);
        }
    }

    private void handleDrawClick() {
        if (isAnimating || currentPlayerIndex != myIndex || isSelectingColor) return; // Chặn khi đang chọn màu

        if (isHost) {
            Card c = drawOne();
            if (c == null) return;

            animateCardMovement(createCardBack(), deckStack, playerProfile, () -> {
                myHand.add(c);
                displayHand.add(c);
                broadcast("DRAW:0");
                updateUI();
                nextTurn(); 
            });
        } else {
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
        
        Card top = discardPile.isEmpty() ? null : discardPile.get(discardPile.size()-1);
        if (top != null) centerPileStack.getChildren().add(createCardView(top, true));

        // FIX: Chỉ hiện Text màu khi lá trên cùng là Wild
        if (currentWildColor != null && top != null && top.color == CardColor.BLACK) {
            colorIndicator.setText("MÀU CHỌN: " + currentWildColor);
            colorIndicator.setTextFill(getColor(currentWildColor));
        } else { 
            colorIndicator.setText(""); 
        }
        
        directionArrow.setText(direction == 1 ? "⟳" : "⟲");

        playerCardContainer.getChildren().clear();
        boolean isMyTurn = (currentPlayerIndex == myIndex);

        for (Card c : new ArrayList<>(displayHand)) {
            Node node = createCardView(c, true);
            node.setOnMouseEntered(e -> { if(!isAnimating && !isSelectingColor) { node.setTranslateY(-20); node.setViewOrder(-10); }});
            node.setOnMouseExited(e -> { if(!isAnimating && !isSelectingColor) { node.setTranslateY(0); node.setViewOrder(0); }});
            node.setOnMouseClicked(e -> {
                // FIX: Chặn click khi đang chọn màu
                if (isMyTurn && isValidMove(c, top) && !isAnimating && !isShowingNotification && !isSelectingColor) {
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
        if (isHost) {
            renderBackCards(rightHandContainer, n1, Pos.CENTER_RIGHT);
            renderBackCards(leftHandContainer, n2, Pos.CENTER_LEFT);
        } else {
            if (myIndex == 1) { 
                renderBackCards(rightHandContainer, n1, Pos.CENTER_RIGHT);
                renderBackCards(leftHandContainer, n2, Pos.CENTER_LEFT);
            } else { 
                renderBackCards(rightHandContainer, n1, Pos.CENTER_RIGHT);
                renderBackCards(leftHandContainer, n2, Pos.CENTER_LEFT);
            }
        }
    }

    private void renderBackCards(HBox box, int count, Pos alignment) {
        box.getChildren().clear();
        box.setSpacing(-50); 
        box.setAlignment(alignment);
        int maxDisplay = 7; 
        for (int i = 0; i < Math.min(count, maxDisplay); i++) {
            StackPane cardBack = createCardBack(0.5); 
            box.getChildren().add(cardBack);
        }
        if (count > maxDisplay) {
            Label more = new Label("+" + (count - maxDisplay));
            more.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16;");
            HBox.setMargin(more, new Insets(0, 0, 0, 55)); 
            box.getChildren().add(more);
        }
    }
    
    private HBox createCompactHandView() { HBox box = new HBox(-40); box.setAlignment(Pos.CENTER); return box; }

    private void showColorPickerDialog(Card playedCard) {
        Platform.runLater(() -> {
            isSelectingColor = true; // Bật cờ chặn click
            
            VBox container = new VBox(20);
            container.setAlignment(Pos.CENTER);
            container.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-background-radius: 20; -fx-padding: 40;");
            container.setMaxSize(500, 300);
            Label title = new Label("CHỌN MÀU MUỐN ĐỔI");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 24; -fx-font-weight: bold;");
            HBox colorsHand = new HBox(15);
            colorsHand.setAlignment(Pos.CENTER);
            for (CardColor color : new CardColor[]{CardColor.RED, CardColor.GREEN, CardColor.BLUE, CardColor.YELLOW}) {
                StackPane colorCard = createColorSelectionCard(color);
                colorCard.setOnMouseClicked(e -> {
                    currentWildColor = color;
                    rootStack.getChildren().remove(container); 
                    isSelectingColor = false; // Tắt cờ chặn click
                    
                    if (isHost) {
                        broadcastMessage("MÀU MỚI: " + currentWildColor, 1.5);
                        broadcast("COLOR:" + currentWildColor);
                        finishAction(playedCard);
                    } else {
                        // Client gửi màu lên Server, không tự ý finishAction
                        outToServer.println("COLOR:" + currentWildColor);
                    }
                });
                colorsHand.getChildren().add(colorCard);
            }
            container.getChildren().addAll(title, colorsHand);
            rootStack.getChildren().add(container); 
        });
    }

    private StackPane createColorSelectionCard(CardColor color) {
        StackPane s = new StackPane();
        Rectangle rect = new Rectangle(80, 120);
        rect.setArcWidth(15); rect.setArcHeight(15);
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
        s.setOnMouseEntered(e -> s.setScaleX(1.1));
        s.setOnMouseExited(e -> s.setScaleX(1.0));
        return s;
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
        if (n > 2) n = 0; if (n < 0) n = 2;
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
        Rectangle border = new Rectangle(90, 135); border.setArcWidth(20); border.setArcHeight(20); border.setFill(Color.WHITE); border.setStroke(Color.web("#333333"));
        Rectangle bg = new Rectangle(82, 127); bg.setArcWidth(18); bg.setArcHeight(18);

        if (front && c != null) {
            Color mainColor = c.getFxColor();
            bg.setFill(new LinearGradient(0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE, new javafx.scene.paint.Stop(0, mainColor), new javafx.scene.paint.Stop(1, mainColor.darker())));
            Ellipse oval = new Ellipse(32, 48); oval.setFill(Color.web("#ffffff", 0.25)); oval.setRotate(25);
            Node symbolNode;
            switch (c.type) {
                case SKIP: symbolNode = createSkipSymbol(); break;
                case REVERSE: symbolNode = createReverseSymbol(); break;
                case DRAW2: symbolNode = createDrawSymbol("+2"); break;
                case WILD_DRAW4: symbolNode = createDrawSymbol("+4"); break;
                default:
                    Text t = new Text(c.getSymbol()); t.setFont(Font.font("Arial Black", FontWeight.BOLD, 50));
                    t.setFill(Color.WHITE); t.setStroke(Color.BLACK); t.setStrokeWidth(0.5); symbolNode = t;
            }
            Text smallSymbol = new Text(c.getSymbol()); smallSymbol.setFont(Font.font("Arial Black", 16)); smallSymbol.setFill(Color.WHITE);
            StackPane.setAlignment(smallSymbol, Pos.TOP_LEFT); StackPane.setMargin(smallSymbol, new Insets(8));
            s.getChildren().addAll(border, bg, oval, symbolNode, smallSymbol);
        } else {
            bg.setFill(new LinearGradient(0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE, new javafx.scene.paint.Stop(0, Color.web("#D72600")), new javafx.scene.paint.Stop(1, Color.web("#000000"))));
            Text logo = new Text("UNO"); logo.setFont(Font.font("Arial Black", FontWeight.BOLD, 20)); logo.setFill(Color.YELLOW); logo.setRotate(-30);
            s.getChildren().addAll(border, bg, logo);
        }
        s.setEffect(new DropShadow(8, Color.web("#000000", 0.5)));
        return s;
    }
    private void animateCardMovement(Node cardNode, Node fromNode, Node toNode, Runnable onFinish) {
        isAnimating = true; animationLayer.getChildren().add(cardNode);
        Bounds fromBounds = fromNode.localToScene(fromNode.getBoundsInLocal()); Bounds toBounds = toNode.localToScene(toNode.getBoundsInLocal());
        cardNode.setLayoutX(fromBounds.getMinX()); cardNode.setLayoutY(fromBounds.getMinY());
        TranslateTransition tt = new TranslateTransition(Duration.millis(600), cardNode);
        tt.setByX(toBounds.getMinX() - fromBounds.getMinX()); tt.setByY(toBounds.getMinY() - fromBounds.getMinY());
        cardNode.setRotate(new Random().nextInt(20) - 10);
        tt.setOnFinished(e -> { animationLayer.getChildren().remove(cardNode); isAnimating = false; if (onFinish != null) onFinish.run(); });
        tt.play();
    }
    private Node createReverseSymbol() {
        StackPane container = new StackPane(); container.setAlignment(Pos.CENTER);
        Path arrow1 = new Path(); arrow1.getElements().addAll(new MoveTo(0, 5), new LineTo(30, 5), new MoveTo(0, 5), new LineTo(10, 0), new MoveTo(0, 5), new LineTo(10, 10));
        arrow1.setStroke(Color.WHITE); arrow1.setStrokeWidth(4);
        Path arrow2 = new Path(); arrow2.getElements().addAll(new MoveTo(0, 5), new LineTo(30, 5), new MoveTo(0, 5), new LineTo(10, 0), new MoveTo(0, 5), new LineTo(10, 10));
        arrow2.setStroke(Color.WHITE); arrow2.setStrokeWidth(4); arrow2.setRotate(180); arrow2.setTranslateY(15);
        container.getChildren().addAll(arrow1, arrow2); return container;
    }
    private Node createSkipSymbol() {
        StackPane stack = new StackPane();
        Circle circle = new Circle(22); circle.setFill(Color.TRANSPARENT); circle.setStroke(Color.WHITE); circle.setStrokeWidth(6);
        Rectangle bar = new Rectangle(38, 6); bar.setFill(Color.WHITE); bar.setRotate(45);
        stack.getChildren().addAll(circle, bar); return stack;
    }
    private Node createDrawSymbol(String val) {
        VBox box = new VBox(-8); box.setAlignment(Pos.CENTER);
        Text t = new Text(val); t.setFont(Font.font("Arial Black", 30)); t.setFill(Color.WHITE);
        HBox cards = new HBox(-12); cards.setAlignment(Pos.CENTER);
        for(int i=0; i<2; i++) { Rectangle r = new Rectangle(18, 26, Color.WHITE); r.setArcWidth(4); r.setArcHeight(4); r.setStroke(Color.GRAY); r.setRotate(i * 15); cards.getChildren().add(r); }
        box.getChildren().addAll(t, cards); return box;
    }
    private StackPane createCardBack() { return createCardView(null, false); }
    private StackPane createCardBack(double scale) { StackPane s = createCardBack(); s.setScaleX(scale); s.setScaleY(scale); return s; }
    private Label createLabel(String text, int size) { Label l = new Label(text); l.setFont(Font.font("Arial", FontWeight.BOLD, size)); l.setTextFill(Color.LIGHTGRAY); return l; }
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

    private void showStatusOverlay(String msg, double seconds) { showStatusOverlay(msg, seconds, null); }
    private void showStatusOverlay(String msg, double seconds, Runnable onComplete) {
        Platform.runLater(() -> { notificationQueue.add(new NotificationTask(msg, seconds, onComplete)); processNotificationQueue(); });
    }
    private static class NotificationTask { String msg; double seconds; Runnable onComplete; NotificationTask(String m, double s, Runnable c) { msg = m; seconds = s; onComplete = c; } }
    private void processNotificationQueue() {
        if (isShowingNotification || notificationQueue.isEmpty()) return;
        isShowingNotification = true;
        NotificationTask task = notificationQueue.poll();
        Label label = new Label(task.msg);
        label.setStyle("-fx-background-color: rgba(9, 20, 40, 0.9); -fx-text-fill: #C8AA6E; -fx-font-size: 35; -fx-padding: 25 50; -fx-background-radius: 50; -fx-border-color: #C8AA6E; -fx-border-width: 3;");
        label.setEffect(new DropShadow(20, Color.BLACK));
        StackPane.setAlignment(label, Pos.CENTER);
        rootStack.getChildren().add(label);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.4), label); fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0);
        PauseTransition stay = new PauseTransition(Duration.seconds(task.seconds));
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.4), label); fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);
        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition(fadeIn, stay, fadeOut);
        seq.setOnFinished(e -> { rootStack.getChildren().remove(label); isShowingNotification = false; if (task.onComplete != null) task.onComplete.run(); processNotificationQueue(); });
        seq.play();
    }
    
    private void animatePenaltyDraw(int victimIdx, int remaining) {
        if (remaining <= 0) { isAnimating = false; return; }
        isAnimating = true;
        Node targetNode = getAvatarNodeByIndex(victimIdx);
        StackPane cardBack = createCardBack(0.5);
        animateCardMovement(cardBack, deckStack, targetNode, () -> {
            Platform.runLater(() -> { if (victimIdx == myIndex) { refreshUserHand(); } updateUI(); animatePenaltyDraw(victimIdx, remaining - 1); });
        });
    }
    private void refreshUserHand() { Platform.runLater(() -> { displayHand.clear(); displayHand.addAll(myHand); updateUI(); }); }
    private void showWinScreen(String msg) {
        Platform.runLater(() -> {
            StackPane winOverlay = new StackPane(); winOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
            VBox content = new VBox(30); content.setAlignment(Pos.CENTER);
            Text winText = new Text(msg.toUpperCase()); winText.setFont(Font.font("Arial Rounded MT Bold", FontWeight.EXTRA_BOLD, 70));
            winText.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#F9C700")), new Stop(1, Color.web("#D72600"))));
            winText.setStroke(Color.WHITE); winText.setStrokeWidth(3); winText.setEffect(new DropShadow(20, Color.GOLD));
            ScaleTransition st = new ScaleTransition(Duration.millis(800), winText); st.setFromX(0.5); st.setFromY(0.5); st.setToX(1.1); st.setToY(1.1); st.setCycleCount(Animation.INDEFINITE); st.setAutoReverse(true); st.play();
            Button btnBackToMenu = new Button("QUAY LẠI MENU"); btnBackToMenu.setFont(Font.font("System", FontWeight.BOLD, 20));
            btnBackToMenu.setTextFill(Color.WHITE); btnBackToMenu.setPrefSize(250, 60);
            btnBackToMenu.setStyle("-fx-background-color: linear-gradient(#2ecc71, #27ae60); -fx-background-radius: 30; -fx-border-color: white; -fx-border-radius: 30; -fx-border-width: 2;");
            btnBackToMenu.setCursor(javafx.scene.Cursor.HAND); btnBackToMenu.setOnAction(e -> backToMenu());
            btnBackToMenu.setOnMouseEntered(e -> btnBackToMenu.setScaleX(1.1)); btnBackToMenu.setOnMouseExited(e -> btnBackToMenu.setScaleX(1.0));
            content.getChildren().addAll(winText, btnBackToMenu); winOverlay.getChildren().add(content);
            FadeTransition ft = new FadeTransition(Duration.millis(1000), winOverlay); ft.setFromValue(0.0); ft.setToValue(1.0);
            rootStack.getChildren().add(winOverlay); ft.play();
        });
    }
    private void backToMenu() {
        try { if (isHost) { for (PrintWriter pw : clientWriters) pw.println("HOST_DISCONNECTED"); } else if (clientSocket != null) { clientSocket.close(); }
            UnoGameMenu menu = new UnoGameMenu(); menu.start(mainStage);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private class PlayerProfileUI extends VBox {
        private Circle border; private ImageView img; private Label nameLabel;;
        public PlayerProfileUI(String n, String url, Pos align) {
            setAlignment(align); setSpacing(5);
            border = new Circle(42, Color.TRANSPARENT); border.setStroke(Color.web("#5c5b57")); border.setStrokeWidth(3);
            String safeUrl = (url != null) ? url : IMG_TEEMO;
            img = new ImageView(new Image(safeUrl, true)); img.setFitWidth(80); img.setFitHeight(80); img.setClip(new Circle(40,40,40));
            String safeName = (n != null) ? n : "Unknown";
            nameLabel = new Label(safeName); nameLabel.setTextFill(Color.WHITE); nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            getChildren().addAll(new StackPane(border, img), nameLabel);
        }
        public String getNameSafe() { String txt = nameLabel.getText(); return (txt != null) ? txt : "Unknown"; }
        public void updateInfo(String n, String url) { if (n != null) this.nameLabel.setText(n); if (url != null) this.img.setImage(new Image(url)); }        
        public void setActive(boolean a) {
            if(a) { border.setStroke(Color.web(GOLD_COLOR)); border.setEffect(new Glow(0.8)); nameLabel.setTextFill(Color.web(GOLD_COLOR)); }
            else { border.setStroke(Color.web("#5c5b57")); border.setEffect(null); nameLabel.setTextFill(Color.WHITE); }
        }
        public Node getAvatarNode() { return border; }
        public void setUnoGlow(boolean active) {
            if (active) { border.setStroke(Color.GOLD); border.setStrokeWidth(5); border.setEffect(new Glow(1.0)); } else { border.setStroke(Color.web("#5c5b57")); border.setStrokeWidth(3); border.setEffect(null); }
        }
    }
    public static void main(String[] args) { launch(args); }
}