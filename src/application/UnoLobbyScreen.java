package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

public class UnoLobbyScreen extends Application {

    // --- RESOURCES ---
    private static final String IMG_BOT_AHRI = "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/Ahri.png";
    private static final String IMG_BOT_TEEMO = "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/Teemo.png";
    private static final String IMG_YASUO = "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/Yasuo.png";
    private static final String IMG_LEE = "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/LeeSin.png";
    private static final String IMG_JINX = "https://ddragon.leagueoflegends.com/cdn/13.24.1/img/champion/Jinx.png";

    private static final String[] PLAYER_AVATARS = { IMG_YASUO, IMG_LEE, IMG_JINX };
    private int currentAvatarIndex = 0;

    private static final String BG_COLOR = "#091428";
    private static final String GOLD_COLOR = "#C8AA6E";
    private static final String GREY_COLOR = "#5c5b57";

    private static final int PORT = 8888;
    private static String initialPlayerName = "Hasagi"; 
    public static void setInitialName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            initialPlayerName = name;
        }
    }
    private Stage primaryStage;
    private TextField nameField;
    private ImageView myAvatarView;
    private Button btnPrevAvatar, btnNextAvatar, startBtn;
    private VBox connectionControlsBox, hostInfoBox;
    private Label roomInfoLabel;

    private Label rightNameLbl, leftNameLbl;
    private ImageView rightAvatar, leftAvatar;
    private Circle rightBorder, leftBorder;
    private Button rightBtn, leftBtn;

    // --- NETWORK VARIABLES ---
    private ServerSocket serverSocket;
    private List<Socket> clientSockets = new ArrayList<>();
    private List<PrintWriter> clientWriters = new ArrayList<>();
    
    private PrintWriter clientOut;
    private BufferedReader clientIn;

    // STATE
    private String currentHostName = "Host";
    private String currentHostAvatar = IMG_YASUO;
    private String currentGuest1Name = null;
    private String currentGuest1Avatar = null;
    private String currentGuest2Name = null;
    private String currentGuest2Avatar = null;
    
    // Trạng thái nút Bot (True = Bot Active, False = Empty/Inactive)
    private boolean isLeftBotActive = true; 
    private boolean isRightBotActive = true;
    
    private boolean isHost = false;
    private boolean isRoomLocked = false;
    private boolean isGameStarted = false;
    
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("UNO - Lobby Avatar Sync");
        stage.setOnCloseRequest(e -> cleanUpNetwork());
        showLobby();
    }

    private void showLobby() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: " + BG_COLOR + ";");
        layout.setPadding(new Insets(30));

        Text title = new Text("UNO - LOBBY");
        title.setFont(Font.font("Beaufort for LOL", FontWeight.BOLD, 50));
        title.setFill(Color.web(GOLD_COLOR));
        title.setEffect(new DropShadow(10, Color.BLACK));

        HBox avatarContainer = new HBox(40);
        avatarContainer.setAlignment(Pos.CENTER);
        avatarContainer.getChildren().addAll(
            createSlot("Bot Ahri", IMG_BOT_AHRI, "LEFT"),
            createPlayerSlot(),
            createSlot("Bot Teemo", IMG_BOT_TEEMO, "RIGHT")
        );

        startBtn = new Button("VÀO TRẬN");
        styleButton(startBtn, 200, GOLD_COLOR);
        
        startBtn.setOnAction(e -> {
            if (isHost) {
                isRoomLocked = true; 
                try { if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close(); } 
                catch (IOException ex) {}
                broadcast("START_GAME");
                launchGameApp();
            }
        });

        layout.getChildren().addAll(title, avatarContainer, startBtn);
        primaryStage.setScene(new Scene(layout, 1000, 700));
        primaryStage.show();
    }

    private VBox createSlot(String defaultName, String imgUrl, String side) {
        VBox root = new VBox(10); root.setAlignment(Pos.CENTER); root.setPadding(new Insets(120, 0, 0, 0));
        Label lbl = new Label(defaultName); lbl.setFont(Font.font("Arial", FontWeight.BOLD, 14)); lbl.setTextFill(Color.WHITE);
        ImageView avatar = new ImageView(new Image(imgUrl, true)); avatar.setFitWidth(100); avatar.setFitHeight(100); avatar.setClip(new Circle(50, 50, 50));
        Circle border = new Circle(54, Color.TRANSPARENT); border.setStroke(Color.web(GOLD_COLOR)); border.setStrokeWidth(3);
        StackPane stack = new StackPane(border, avatar);
        
        Button btn = new Button("BOT"); 
        styleSmallButton(btn, GREY_COLOR); 
        styleToggleButton(btn, true);
        
        // --- LOGIC BẤM NÚT (SỬA ĐỔI) ---
        btn.setOnAction(e -> {
            // Chỉ Host mới được bấm
            if (!isHost) return; 
            
            // Cập nhật trạng thái
            if (side.equals("LEFT")) isLeftBotActive = !isLeftBotActive;
            else isRightBotActive = !isRightBotActive;
            
            // Đồng bộ trạng thái mới cho mọi người
            broadcastRoomState();
        });

        if (side.equals("LEFT")) { leftNameLbl = lbl; leftAvatar = avatar; leftBorder = border; leftBtn = btn; } 
        else { rightNameLbl = lbl; rightAvatar = avatar; rightBorder = border; rightBtn = btn; }
        root.getChildren().addAll(new VBox(6, lbl, stack), btn);
        return root;
    }

    // ... (createPlayerSlot giữ nguyên) ...
    private VBox createPlayerSlot() {
        VBox slot = new VBox(10); slot.setAlignment(Pos.BOTTOM_CENTER);
        VBox topControls = new VBox(10); topControls.setAlignment(Pos.CENTER); topControls.setMinHeight(120);

        nameField = new TextField(initialPlayerName); 
        nameField.setPromptText("Tên của bạn");
        nameField.setStyle("-fx-background-color: #1e2328; -fx-text-fill: white; -fx-border-color: " + GREY_COLOR + "; -fx-alignment: center;"); nameField.setMinWidth(250);

        Button btnHost = new Button("Tạo Phòng"); styleSmallButton(btnHost, GOLD_COLOR);
        Button btnJoin = new Button("Vào Phòng"); styleSmallButton(btnJoin, "#3498db");

        connectionControlsBox = new VBox(10, nameField, new HBox(10, btnHost, btnJoin) {{ setAlignment(Pos.CENTER); }}); connectionControlsBox.setAlignment(Pos.CENTER);
        
        hostInfoBox = new VBox(5); hostInfoBox.setAlignment(Pos.CENTER); hostInfoBox.setVisible(false); hostInfoBox.setManaged(false);
        roomInfoLabel = new Label(); roomInfoLabel.setTextFill(Color.web("#2ecc71")); roomInfoLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        Button btnCancel = new Button("Hủy"); styleSmallButton(btnCancel, "#e74c3c");
        hostInfoBox.getChildren().addAll(roomInfoLabel, btnCancel);

        myAvatarView = new ImageView(new Image(PLAYER_AVATARS[0])); myAvatarView.setFitWidth(140); myAvatarView.setFitHeight(140); myAvatarView.setClip(new Circle(70,70,70));
        StackPane imgStack = new StackPane(new Circle(75, Color.TRANSPARENT) {{ setStroke(Color.web(GOLD_COLOR)); setStrokeWidth(4); setEffect(new DropShadow(15, Color.web(GOLD_COLOR))); }}, myAvatarView);

        btnPrevAvatar = new Button("<"); btnNextAvatar = new Button(">");
        String arrowStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand;";
        btnPrevAvatar.setStyle(arrowStyle); btnNextAvatar.setStyle(arrowStyle);
        btnPrevAvatar.setOnAction(e -> changeAvatar(-1)); btnNextAvatar.setOnAction(e -> changeAvatar(1));

        HBox avatarSelector = new HBox(10, btnPrevAvatar, imgStack, btnNextAvatar); avatarSelector.setAlignment(Pos.CENTER);

        btnHost.setOnAction(e -> startHost()); btnJoin.setOnAction(e -> showJoinDialog());
        btnCancel.setOnAction(e -> { cleanUpNetwork(); resetLobbyUI(); });

        topControls.getChildren().addAll(connectionControlsBox, hostInfoBox);
        slot.getChildren().addAll(topControls, avatarSelector, new Label("BẠN") {{ setTextFill(Color.web(GOLD_COLOR)); setFont(Font.font(18)); }});
        return slot;
    }

    private void changeAvatar(int delta) {
        currentAvatarIndex += delta;
        if (currentAvatarIndex < 0) currentAvatarIndex = PLAYER_AVATARS.length - 1;
        if (currentAvatarIndex >= PLAYER_AVATARS.length) currentAvatarIndex = 0;
        myAvatarView.setImage(new Image(PLAYER_AVATARS[currentAvatarIndex]));
    }
    private void lockAvatarSelection() { btnPrevAvatar.setVisible(false); btnNextAvatar.setVisible(false); nameField.setDisable(true); }
    private void unlockAvatarSelection() { btnPrevAvatar.setVisible(true); btnNextAvatar.setVisible(true); nameField.setDisable(false); }

    // --- NETWORK LOGIC ---

    private void startHost() {
        currentHostName = nameField.getText().isEmpty() ? "Host" : nameField.getText();
        currentHostAvatar = PLAYER_AVATARS[currentAvatarIndex];
        isHost = true; 
        
        lockAvatarSelection();
        
        connectionControlsBox.setVisible(false); connectionControlsBox.setManaged(false);
        hostInfoBox.setVisible(true); hostInfoBox.setManaged(true);
        roomInfoLabel.setText("IP: " + getLocalIpAddress());
        startBtn.setDisable(false);
        
        // Host được phép bấm nút BOT
        leftBtn.setDisable(false);
        rightBtn.setDisable(false);

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                while (true) {
                    if (isRoomLocked) break;
                    Socket client = serverSocket.accept();
                    if (clientSockets.size() >= 2) { client.close(); continue; }
                    
                    clientSockets.add(client);
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    clientWriters.add(out);
                    new Thread(() -> handleClient(client, out)).start();
                }
            } catch (IOException e) { }
        }).start();
    }

    private void handleClient(Socket client, PrintWriter out) {
        String registeredRole = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line = in.readLine();
            String gName = "Unknown", gAvatar = IMG_BOT_TEEMO;

            if (line != null && line.startsWith("LOGIN:")) {
                String[] parts = line.split(":", 3);
                if (parts.length >= 2) gName = parts[1];
                if (parts.length >= 3) gAvatar = parts[2];
            }

            synchronized (this) {
                if (currentGuest1Name == null) {
                    currentGuest1Name = gName; currentGuest1Avatar = gAvatar; registeredRole = "GUEST1";
                } else if (currentGuest2Name == null) {
                    currentGuest2Name = gName; currentGuest2Avatar = gAvatar; registeredRole = "GUEST2";
                }
            }
            broadcastRoomState();

            String msg;
            while ((msg = in.readLine()) != null) {} // Keep-alive loop

        } catch (IOException e) { 
        } finally {
            handleDisconnect(client, out, registeredRole);
        }
    }

    private void handleDisconnect(Socket client, PrintWriter out, String registeredRole) {
        try { client.close(); } catch (IOException e) {}
        clientSockets.remove(client); clientWriters.remove(out);
        synchronized (this) {
            if ("GUEST1".equals(registeredRole)) { currentGuest1Name = null; currentGuest1Avatar = null; } 
            else if ("GUEST2".equals(registeredRole)) { currentGuest2Name = null; currentGuest2Avatar = null; }
        }
        broadcastRoomState();
    }

    private void broadcast(String message) {
        for (PrintWriter writer : clientWriters) { try { writer.println(message); } catch (Exception e) { } }
    }

    private void broadcastRoomState() {
        String g1Name = (currentGuest1Name == null) ? "null" : currentGuest1Name;
        String g1Ava = (currentGuest1Avatar == null) ? "null" : currentGuest1Avatar;
        String g2Name = (currentGuest2Name == null) ? "null" : currentGuest2Name;
        String g2Ava = (currentGuest2Avatar == null) ? "null" : currentGuest2Avatar;

        // Cập nhật Protocol: Gửi thêm trạng thái isLeftBotActive và isRightBotActive
        // STATE:Host|Ava,G1|Ava,G2|Ava,LeftActive,RightActive
        String message = "STATE:" + 
                currentHostName + "|" + currentHostAvatar + "," + 
                g1Name + "|" + g1Ava + "," + 
                g2Name + "|" + g2Ava + "," +
                isLeftBotActive + "," + isRightBotActive;

        Platform.runLater(() -> updateLobbyUI(message, true));
        broadcast(message);
    }

    private void showJoinDialog() {
        TextInputDialog dialog = new TextInputDialog("192.168.1.");
        dialog.setTitle("Tìm Phòng"); dialog.setHeaderText("Nhập IP Host:");
        dialog.getDialogPane().setStyle("-fx-background-color: #091428;");
        dialog.getDialogPane().getStylesheets().add("data:text/css,.dialog-pane .label { -fx-text-fill: white; } .text-field { -fx-text-fill: white; -fx-background-color: #5c5b57; }");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(ip -> connectToHost(ip.trim()));
    }

    private void connectToHost(String ip) {
        String myName = nameField.getText().isEmpty() ? "Client" : nameField.getText();
        String myAvatar = PLAYER_AVATARS[currentAvatarIndex];
        isHost = false;
        lockAvatarSelection();
        connectionControlsBox.setVisible(false); connectionControlsBox.setManaged(false);
        hostInfoBox.setVisible(true); hostInfoBox.setManaged(true);
        roomInfoLabel.setText("Đang kết nối...");
        startBtn.setDisable(true);
        
        // --- QUAN TRỌNG: Client không được bấm nút BOT ---
        leftBtn.setDisable(true);
        rightBtn.setDisable(true);

        new Thread(() -> {
            try {
                Socket socket = new Socket(ip, PORT);
                clientSockets.add(socket); 
                this.clientOut = new PrintWriter(socket.getOutputStream(), true);
                this.clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                clientOut.println("LOGIN:" + myName + ":" + myAvatar);

                String msg;
                while ((msg = clientIn.readLine()) != null) {
                    if (msg.startsWith("STATE:")) {
                        String finalMsg = msg;
                        Platform.runLater(() -> {
                            roomInfoLabel.setText("Connected!");
                            updateLobbyUI(finalMsg, false);
                        });
                    }
                    else if (msg.equals("START_GAME")) {
                        Platform.runLater(this::launchGameApp);
                        break; 
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    resetLobbyUI();
                    new Alert(Alert.AlertType.ERROR, "Mất kết nối tới Host!").show();
                });
            }
        }).start();
    }

    private void updateLobbyUI(String stateMsg, boolean isHostView) {
        try {
            // STATE:H,G1,G2,LeftActive,RightActive
            String data = stateMsg.substring(6);
            String[] parts = data.split(","); // Tách thành 5 phần
            
            String[] hData = parts[0].split("\\|");
            String[] g1Data = parts[1].split("\\|");
            String[] g2Data = parts[2].split("\\|");
            
            // Đọc trạng thái nút (Nếu có)
            boolean lActive = true, rActive = true;
            if (parts.length >= 5) {
                lActive = Boolean.parseBoolean(parts[3]);
                rActive = Boolean.parseBoolean(parts[4]);
            }

            // Đồng bộ biến toàn cục nếu là Client (để giữ trạng thái)
            if (!isHost) {
                isLeftBotActive = lActive;
                isRightBotActive = rActive;
            }

            if (isHostView) {
                // Host: Phải=G1, Trái=G2
                updateSlot(rightNameLbl, rightAvatar, rightBorder, rightBtn, g1Data[0], g1Data[1], isRightBotActive);
                updateSlot(leftNameLbl, leftAvatar, leftBorder, leftBtn, g2Data[0], g2Data[1], isLeftBotActive);
            } else {
                // Client: Phải=Host, Trái=Người còn lại
                // Note: Host luôn là người, không có nút Bot
                updateSlot(rightNameLbl, rightAvatar, rightBorder, rightBtn, hData[0] + " (Chủ)", hData[1], true);
                
                String myName = nameField.getText();
                String otherName = "null", otherAva = "null";
                boolean otherBtnStatus = true;

                if (myName.equals(g1Data[0])) { 
                    otherName = g2Data[0]; otherAva = g2Data[1]; otherBtnStatus = lActive; // Tôi là G1 -> Thấy G2 (bên trái Host)
                } else if (myName.equals(g2Data[0])) { 
                    otherName = g1Data[0]; otherAva = g1Data[1]; otherBtnStatus = rActive; // Tôi là G2 -> Thấy G1 (bên phải Host)
                }
                
                updateSlot(leftNameLbl, leftAvatar, leftBorder, leftBtn, otherName, otherAva, otherBtnStatus);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateSlot(Label lbl, ImageView view, Circle border, Button btn, String name, String imgUrl, boolean isBotActive) {
        // CASE 1: KHÔNG CÓ NGƯỜI CHƠI (HIỆN BOT / EMPTY)
        if (name == null || name.equals("null") || name.equals("Unknown")) {
            boolean isLeft = (btn == leftBtn);
            
            // Cập nhật text và hình ảnh dựa trên trạng thái Active/Inactive
            if (isBotActive) {
                btn.setText("BOT");
                lbl.setText(isLeft ? "Bot Ahri" : "Bot Teemo");
                view.setImage(new Image(isLeft ? IMG_BOT_AHRI : IMG_BOT_TEEMO));
                view.setOpacity(1.0);
                styleToggleButton(btn, true);
            } else {
                btn.setText("EMPTY");
                lbl.setText("Trống");
                view.setImage(new Image(isLeft ? IMG_BOT_AHRI : IMG_BOT_TEEMO)); // Vẫn hiện ảnh nhưng mờ
                view.setOpacity(0.3);
                styleToggleButton(btn, false);
            }
            
            lbl.setTextFill(Color.WHITE);
            border.setStroke(Color.web(GOLD_COLOR));
            
            // Luôn hiện nút, nhưng chỉ Host bấm được (Client disable ở connectToHost rồi)
            btn.setVisible(true);
            btn.setManaged(true);
            return; 
        }

        // CASE 2: CÓ NGƯỜI CHƠI
        lbl.setText(name); 
        lbl.setTextFill(Color.web("#2ecc71")); 
        if (imgUrl != null && !imgUrl.equals("null")) view.setImage(new Image(imgUrl));
        view.setOpacity(1.0); 
        border.setStroke(Color.web("#2ecc71"));
        
        // Ẩn nút Bot khi đã có người
        btn.setVisible(false); 
        btn.setManaged(false);
    }

    private void resetLobbyUI() {
        hostInfoBox.setVisible(false); hostInfoBox.setManaged(false);
        connectionControlsBox.setVisible(true); connectionControlsBox.setManaged(true);
        startBtn.setDisable(false); unlockAvatarSelection();
        currentGuest1Name = null; currentGuest1Avatar = null; currentGuest2Name = null; currentGuest2Avatar = null;
        
        // Reset trạng thái
        isLeftBotActive = true;
        isRightBotActive = true;
        
        // Reset 2 Slot
        updateSlot(leftNameLbl, leftAvatar, leftBorder, leftBtn, "null", null, true);
        updateSlot(rightNameLbl, rightAvatar, rightBorder, rightBtn, "null", null, true);
        
        // Host được quyền bấm lại
        if(isHost) { leftBtn.setDisable(false); rightBtn.setDisable(false); }
    }

    private void launchGameApp() {
        isGameStarted = true;
        primaryStage.close();
        UnoGameApp game;
        
        if (isHost) {
            currentHostName = nameField.getText();
            currentHostAvatar = PLAYER_AVATARS[currentAvatarIndex];
            game = new UnoGameApp(
                currentHostName, true, clientSockets, clientWriters, currentHostAvatar,
                currentGuest1Name, currentGuest1Avatar, currentGuest2Name, currentGuest2Avatar
            );
        } else {
            game = new UnoGameApp(
                nameField.getText(), false, clientSockets.get(0), clientOut, clientIn, 
                PLAYER_AVATARS[currentAvatarIndex]
            ); 
        }
        try { game.start(new Stage()); } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void cleanUpNetwork() {
        if (isGameStarted) return; 
        try {
            if (serverSocket != null) serverSocket.close();
            for (Socket s : clientSockets) s.close();
            clientSockets.clear(); clientWriters.clear();
        } catch (IOException e) {}
    }

    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) return addr.getHostAddress();
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) { return "Unknown"; }
    }
    
    private void styleButton(Button btn, double w, String c) {
        btn.setStyle("-fx-background-color: "+c+"; -fx-text-fill: #091428; -fx-font-weight: bold; -fx-font-size: 18px;");
        btn.setPrefWidth(w); btn.setOnMouseEntered(e->btn.setEffect(new Glow(0.5))); btn.setOnMouseExited(e->btn.setEffect(null));
    }
    private void styleSmallButton(Button btn, String c) {
        btn.setStyle("-fx-background-color: "+c+"; -fx-text-fill: #091428; -fx-font-weight: bold;"); btn.setPrefWidth(120);
    }
    private void styleToggleButton(Button btn, boolean active) {
        if(active) btn.setStyle("-fx-background-color: #1e2328; -fx-text-fill: #C8AA6E; -fx-border-color: #C8AA6E;");
        else btn.setStyle("-fx-background-color: black; -fx-text-fill: #555; -fx-border-color: #555;");
    }

    public static void main(String[] args) { launch(args); }
}