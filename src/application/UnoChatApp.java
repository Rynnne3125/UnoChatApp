package application;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.dosse.upnp.UPnP;
public class UnoChatApp extends Application {
    // --- UNO PALETTE ---
    private final Color UNO_RED = Color.web("#D72600");
    private final Color UNO_YELLOW = Color.web("#F9C700");
    private final Color UNO_GREEN = Color.web("#379711");
    private final Color UNO_BLUE = Color.web("#0956BF");
    private Label publicIpLabel;
    // --- TRẠNG THÁI ---
    private String username;
    private String myLanIP;
    private static String targetFriendName = null; // Biến lưu tên người bạn muốn chat
    public static void setTargetFriend(String name) {
        targetFriendName = name;
    }
    
    // Server & Connection
    private static VirtualHost activeVirtualHost; 
    private PeerConnection connectionToServer; 
    
    // DATA
    private final String DATA_FILE = "uno_groups.dat";
    private final String HISTORY_DIR = "uno_chat_history/";
    private ObservableList<GroupData> savedGroups = FXCollections.observableArrayList();
    private String currentActiveGroupName = ""; 

    // UI COMPONENTS
    private Stage primaryStage;
    private VBox msgBox;
    private ListView<GroupData> groupListView;
    private Scene mainScene;

    public static void main(String[] args) {
        launch(args);
    }

    
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        
        // 1. Xử lý dữ liệu User
        if (Main.CurrentUser != null) {
            this.username = Main.CurrentUser.getUsername();
        } else {
            if (username == null) username = "Player_" + new Random().nextInt(999);
        }
        
        myLanIP = getRealLanIP(); 
        
        new File(HISTORY_DIR).mkdirs();
        loadGroupData(); 
        
        primaryStage.setTitle("UNO Connect - " + username);

        // 2. --- QUAN TRỌNG: SETUP GIAO DIỆN TRƯỚC ---
        setupMainMenu(); 
        
        // 3. Hiển thị Stage
        primaryStage.show();
        
        // 4. --- QUAN TRỌNG: SET FULLSCREEN SAU CÙNG ---
        // Phải gọi sau khi đã có Scene và sau khi show() (hoặc ngay trước show)
        // nhưng bắt buộc phải SAU setupMainMenu()
        primaryStage.setFullScreen(true); 

        // 5. Logic Direct Message
        if (targetFriendName != null) {
            String pattern1 = "DM-" + username + "-" + targetFriendName;
            String pattern2 = "DM-" + targetFriendName + "-" + username;

            GroupData existingGroup = null;
            for (GroupData g : savedGroups) {
                if (g.groupName.equals(pattern1) || g.groupName.equals(pattern2)) {
                    existingGroup = g;
                    break;
                }
            }

            if (existingGroup != null) {
                GroupData finalGroup = existingGroup;
                Platform.runLater(() -> handleRejoinGroup(finalGroup));
            } else {
                String friend = targetFriendName; 
                Platform.runLater(() -> showCreateGroupDialogDM(friend));
            }
            targetFriendName = null; 
        }
    }
    private void showCreateGroupDialogDM(String friendName) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Chat Riêng");
        dialog.setHeaderText("Tạo phòng chat với " + friendName);
        
        ButtonType createType = new ButtonType("Tạo Phòng", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        
        // Tự động đặt tên phòng
        TextField nameField = new TextField("DM-" + username + "-" + friendName);
        TextField portField = new TextField("6000"); // Port khác mặc định chút
        
        grid.add(new Label("Tên Phòng:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Cổng (Port):"), 0, 1); grid.add(portField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(b -> b == createType ? new String[]{nameField.getText(), portField.getText()} : null);

        dialog.showAndWait().ifPresent(result -> {
            // Reset target sau khi xử lý xong
            targetFriendName = null; 
            try { 
                startVirtualHostAndConnect(result[0], Integer.parseInt(result[1])); 
            } catch (Exception e) { 
                showAlert("Lỗi tạo phòng!"); 
            }
        });
    }

    
    @Override
    public void stop() {
    	if (activeVirtualHost != null) {
            activeVirtualHost.stop();
            // Xóa port mapping trên router để dọn dẹp
            new Thread(() -> UPnP.closePortTCP(activeVirtualHost.port)).start();
        }
    	if (connectionToServer != null) try { connectionToServer.socket.close(); } catch(Exception e){}
    }

    // =================================================================
    // PHẦN 1: GIAO DIỆN CHÍNH (LOBBY)
    // =================================================================
    private void setupMainMenu() {
        StackPane root = new StackPane();
        
        // Background Gradient
        root.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2c3e50")), new Stop(1, Color.web("#000000"))),
            CornerRadii.EMPTY, Insets.EMPTY)));

        // --- THÊM HIỆU ỨNG BÀI BAY CHO MENU ---
        root.getChildren().add(createFloatingCards());

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setMaxWidth(600);
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Header
        HBox headerCard = createHeaderCard();
        
        // Action Buttons
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);
        
        Button btnBackMenu = createStyledButton("VỀ MENU", UNO_RED, "🏠");
        btnBackMenu.setPrefWidth(150);
        btnBackMenu.setOnAction(e -> returnToGameMenu());
        
        Button btnCreate = createStyledButton("TẠO PHÒNG", UNO_GREEN, "➕");
        btnCreate.setPrefWidth(150);
        btnCreate.setOnAction(e -> showCreateGroupDialog());

        Button btnJoin = createStyledButton("NHẬP IP", UNO_BLUE, "🔗");
        btnJoin.setPrefWidth(150);
        btnJoin.setOnAction(e -> showJoinDialog());
        
        actions.getChildren().addAll(btnBackMenu, btnCreate, btnJoin);

        // List Rooms
        Label lblList = new Label("LỊCH SỬ PHÒNG CHAT");
        lblList.setTextFill(Color.WHITE);
        lblList.setFont(Font.font("System", FontWeight.BOLD, 14));

        groupListView = new ListView<>(savedGroups);
        groupListView.setCellFactory(param -> new GroupListCell());
        groupListView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        VBox.setVgrow(groupListView, Priority.ALWAYS);

        StackPane listContainer = new StackPane(groupListView);
        listContainer.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 20; -fx-padding: 10;");

        groupListView.setOnMouseClicked(e -> {
            GroupData selected = groupListView.getSelectionModel().getSelectedItem();
            if (selected != null && e.getClickCount() == 2) {
                handleRejoinGroup(selected);
            }
        });

        mainLayout.getChildren().addAll(headerCard, actions, lblList, listContainer);
        
        // Bọc mainLayout trong kính mờ để dễ nhìn hơn trên nền động
        StackPane glassWrapper = new StackPane(mainLayout);
        glassWrapper.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 0;");

        root.getChildren().add(glassWrapper);
        
        mainScene = new Scene(root, 600, 750);
        primaryStage.setScene(mainScene);
    }

    // =================================================================
    // PHẦN 2: GIAO DIỆN CHAT (ROOM) - CÓ BACKGROUND ĐỘNG
    // =================================================================
    private void setupChatRoomUI(String groupName, String connectedIP, int port) {
        StackPane root = new StackPane();

        // 1. Background nền tối (Gradient) thay vì màu xám
        root.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1a1a1a")), new Stop(1, Color.web("#2c3e50"))),
            CornerRadii.EMPTY, Insets.EMPTY)));

        // 2. Thêm hiệu ứng bài bay (Nằm dưới cùng)
        root.getChildren().add(createFloatingCards());

        BorderPane layout = new BorderPane();
        
        // Header Bar (Glassmorphism)
        HBox header = new HBox(15);
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: rgba(9, 86, 191, 0.85); -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 0, 4, 0, 0); -fx-background-radius: 0 0 15 15;");

        Button backBtn = new Button("⬅ RỜI PHÒNG");
        backBtn.setFont(Font.font("System", FontWeight.BOLD, 12));
        backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 8 15 8 15;");
        backBtn.setOnAction(e -> {
            if (connectionToServer != null) try { connectionToServer.socket.close(); } catch(Exception ex){}
            setupMainMenu();
            primaryStage.setFullScreen(true);
        });

        VBox titleBox = new VBox(2);
        Label roomName = new Label(groupName);
        roomName.setFont(Font.font("System", FontWeight.BOLD, 18));
        roomName.setTextFill(Color.WHITE);
        
        Label status = new Label(connectedIP.equals("127.0.0.1") ? "Đang làm Host (Local)" : "Kết nối tới: " + connectedIP);
        status.setFont(Font.font("System", 11));
        status.setTextFill(Color.web("#E0E0E0"));

        titleBox.getChildren().addAll(roomName, status);
        header.getChildren().addAll(backBtn, titleBox);
        layout.setTop(header);

        // Message Area
        msgBox = new VBox(15);
        msgBox.setPadding(new Insets(20));
        
        ScrollPane scroll = new ScrollPane(msgBox);
        scroll.setFitToWidth(true);
        // QUAN TRỌNG: Làm trong suốt ScrollPane để thấy background phía sau
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-viewport-border: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        msgBox.heightProperty().addListener((o, old, val) -> scroll.setVvalue(1.0));
        
        layout.setCenter(scroll);
        
        loadChatHistory(groupName);

        // Input Area (Glassmorphism)
        HBox inputArea = new HBox(10);
        inputArea.setPadding(new Insets(15));
        // Màu nền input bán trong suốt
        inputArea.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 15 15 0 0;");
        inputArea.setAlignment(Pos.CENTER);

        TextField input = new TextField();
        input.setPromptText("Nhập tin nhắn...");
        input.setStyle("-fx-background-radius: 25; -fx-background-color: #F0F2F5; -fx-padding: 12; -fx-text-fill: black; -fx-font-size: 14px;");
        HBox.setHgrow(input, Priority.ALWAYS);

        Button sendBtn = new Button("➤");
        sendBtn.setStyle("-fx-background-color: " + toHex(UNO_GREEN) + "; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 45px; -fx-min-height: 45px; -fx-font-size: 18px; -fx-cursor: hand;");
        
        Runnable sendAction = () -> {
            String text = input.getText().trim();
            if (!text.isEmpty()) {
                saveMessageToHistory(groupName, "CHAT", username, text);
                connectionToServer.out.println("CHAT:" + username + ":" + text);
                input.clear();
            }
        };
        sendBtn.setOnAction(e -> sendAction.run());
        input.setOnAction(e -> sendAction.run());
        
        inputArea.getChildren().addAll(input, sendBtn);
        layout.setBottom(inputArea);

        root.getChildren().add(layout);
        primaryStage.getScene().setRoot(root);
    }

    // =================================================================
    // PHẦN 3: HÀM TẠO HIỆU ỨNG LÁ BÀI BAY (MỚI THÊM)
    // =================================================================
    private Pane createFloatingCards() {
        Pane pane = new Pane();
        // Cho phép click xuyên qua pane này để bấm được nút bên dưới nếu cần
        pane.setPickOnBounds(false); 
        
        Random rand = new Random();
        Color[] colors = {UNO_RED, UNO_BLUE, UNO_GREEN, UNO_YELLOW};

        // Tạo 15 lá bài ngẫu nhiên
        for (int i = 0; i < 15; i++) {
            Rectangle card = new Rectangle(50, 75); // Kích thước nhỏ hơn chút cho Chat
            card.setArcWidth(8); card.setArcHeight(8);
            card.setFill(colors[rand.nextInt(colors.length)]);
            card.setStroke(Color.WHITE);
            card.setStrokeWidth(2);
            
            // Hình tròn giữa lá bài
            Circle c = new Circle(15, Color.WHITE);
            c.setCenterX(25); c.setCenterY(37.5);
            
            Pane cardGroup = new Pane(card, c);
            
            // Vị trí ngẫu nhiên
            cardGroup.setLayoutX(rand.nextInt(600)); // Theo chiều rộng scene
            cardGroup.setLayoutY(rand.nextInt(750)); // Theo chiều cao scene
            cardGroup.setOpacity(0.15); // Rất mờ để không rối mắt khi đọc tin nhắn
            cardGroup.setRotate(rand.nextInt(360));
            
            // Hiệu ứng mờ ảo (Blur)
            cardGroup.setEffect(new GaussianBlur(3));

            // 1. Animation Trôi (Move)
            TranslateTransition tt = new TranslateTransition(Duration.seconds(15 + rand.nextInt(15)), cardGroup);
            tt.setByY(-150 - rand.nextInt(150)); // Trôi lên trên
            tt.setByX(rand.nextInt(50) - 25);     // Lắc lư trái phải
            tt.setCycleCount(Animation.INDEFINITE);
            tt.setAutoReverse(true);
            tt.play();

            // 2. Animation Xoay (Rotate)
            RotateTransition rt = new RotateTransition(Duration.seconds(10 + rand.nextInt(10)), cardGroup);
            rt.setByAngle(360);
            rt.setCycleCount(Animation.INDEFINITE);
            rt.play();
            
            // 3. Animation Phóng to/nhỏ (Scale)
            ScaleTransition st = new ScaleTransition(Duration.seconds(5 + rand.nextInt(5)), cardGroup);
            st.setByX(0.2); st.setByY(0.2);
            st.setAutoReverse(true);
            st.setCycleCount(Animation.INDEFINITE);
            st.play();

            pane.getChildren().add(cardGroup);
        }
        return pane;
    }

    // =================================================================
    // PHẦN 4: LOGIC & HELPERS (GIỮ NGUYÊN)
    // =================================================================

    private void addMessageBubble(String text, boolean isMe, String senderName) {
        VBox container = new VBox(5);
        
        if (!isMe) {
            Label nameLbl = new Label(senderName);
            nameLbl.setFont(Font.font("System", FontWeight.BOLD, 11));
            nameLbl.setTextFill(Color.LIGHTGRAY); // Chữ sáng màu cho nổi trên nền tối
            container.getChildren().add(nameLbl);
            container.setAlignment(Pos.CENTER_LEFT);
        } else {
            container.setAlignment(Pos.CENTER_RIGHT);
        }

        Label msgLbl = new Label(text);
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(300);
        msgLbl.setPadding(new Insets(12, 16, 12, 16));
        msgLbl.setFont(Font.font("System", 14));
        
        DropShadow ds = new DropShadow();
        ds.setColor(Color.rgb(0,0,0,0.3));
        ds.setOffsetY(2);
        msgLbl.setEffect(ds);

        if (isMe) { 
            msgLbl.setStyle("-fx-background-color: linear-gradient(to bottom right, " + toHex(UNO_GREEN) + ", " + toHex(UNO_GREEN.brighter()) + "); -fx-background-radius: 18 18 4 18;");
            msgLbl.setTextFill(Color.WHITE);
        } else {
            msgLbl.setStyle("-fx-background-color: linear-gradient(to bottom right, " + toHex(UNO_GREEN) + ", " + toHex(UNO_GREEN.brighter()) + "); -fx-background-radius: 18 18 4 18;");
            msgLbl.setTextFill(Color.BLACK);
        }

        container.getChildren().add(msgLbl);
        
        HBox alignBox = new HBox();
        alignBox.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        alignBox.getChildren().add(container);
        
        ScaleTransition st = new ScaleTransition(Duration.millis(200), msgLbl);
        st.setFromX(0); st.setFromY(0); st.setToX(1); st.setToY(1);
        st.play();

        msgBox.getChildren().add(alignBox);
    }
    
    private void addSystemMessage(String text) {
        Label lbl = new Label(text);
        // System message nền kính mờ
        lbl.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-padding: 5 15 5 15; -fx-background-radius: 15;");
        lbl.setFont(Font.font("System", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.WHITE);
        HBox c = new HBox(lbl); c.setAlignment(Pos.CENTER); c.setPadding(new Insets(10));
        msgBox.getChildren().add(c);
    }

    private Button createStyledButton(String text, Color color, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(200);
        btn.setPrefHeight(45);
        btn.setFont(Font.font("System", FontWeight.BOLD, 14));
        btn.setTextFill(Color.WHITE);
        
        String hex = toHex(color);
        String styleNormal = "-fx-background-color: " + hex + "; -fx-background-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 3);";
        String styleHover = "-fx-background-color: " + toHex(color.brighter()) + "; -fx-background-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 5);";
        
        btn.setStyle(styleNormal);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleNormal));
        
        return btn;
    }
    
    private HBox createHeaderCard() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #D72600, #F9C700); -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        Circle avatar = new Circle(30, Color.WHITE);
        Label letter = new Label(username.substring(0,1).toUpperCase());
        letter.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        letter.setTextFill(UNO_RED);
        StackPane avatarStack = new StackPane(avatar, letter);

        VBox info = new VBox(5);
        Label nameLbl = new Label("Xin chào, " + username);
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLbl.setTextFill(Color.WHITE);
        
        // LAN IP (IP Nội bộ)
        Label ipLbl = new Label("LAN IP: " + myLanIP);
        ipLbl.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
        ipLbl.setTextFill(Color.WHITE);
        ipLbl.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));

        // PUBLIC IP (IP Internet - Thêm mới)
        publicIpLabel = new Label("Public IP: Đang check...");
        publicIpLabel.setStyle("-fx-background-color: rgba(55, 151, 17, 0.8); -fx-padding: 3 8 3 8; -fx-background-radius: 10;"); // Màu xanh lá
        publicIpLabel.setTextFill(Color.WHITE);
        publicIpLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        publicIpLabel.setVisible(false); // Ẩn đi khi chưa tạo phòng

        info.getChildren().addAll(nameLbl, ipLbl, publicIpLabel);
        header.getChildren().addAll(avatarStack, info);
        return header;
    }
    private String toHex(Color c) {
        return String.format("#%02X%02X%02X", (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    }

    private void processIncomingMessage(String line) {
        Platform.runLater(() -> {
            if (line.startsWith("CHAT:")) {
                String[] parts = line.split(":", 3); 
                if (parts.length == 3) {
                    String sender = parts[1];
                    String content = parts[2];
                    boolean isMe = sender.equals(username);
                    addMessageBubble(content, isMe, sender);
                    if (!isMe) saveMessageToHistory(currentActiveGroupName, "CHAT", sender, content);
                }
            } else if (line.startsWith("SYSTEM:")) {
                String msg = line.substring(7);
                addSystemMessage(msg);
                saveMessageToHistory(currentActiveGroupName, "SYSTEM", "System", msg);
            }
        });
    }
    
    private void showCreateGroupDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Tạo Phòng Mới");
        ButtonType createType = new ButtonType("Tạo Ngay", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        TextField nameField = new TextField("Phòng của " + username);
        TextField portField = new TextField("5000"); 
        grid.add(new Label("Tên Phòng:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Cổng (Port):"), 0, 1); grid.add(portField, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(b -> b == createType ? new String[]{nameField.getText(), portField.getText()} : null);

        dialog.showAndWait().ifPresent(result -> {
            try { startVirtualHostAndConnect(result[0], Integer.parseInt(result[1])); } 
            catch (Exception e) { showAlert("Lỗi nhập liệu!"); }
        });
    }

    private void showJoinDialog() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Tham Gia");
        dialog.setHeaderText("Nhập IP:Port của Host");
        dialog.setContentText("Ví dụ: 192.168.1.5:5000");
        dialog.showAndWait().ifPresent(addr -> {
            String[] parts = addr.split(":");
            if(parts.length == 2) connectToGroup("Nhóm Mới", parts[0], Integer.parseInt(parts[1]));
            else showAlert("Sai định dạng!");
        });
    }

    private void handleRejoinGroup(GroupData group) {
        if (group.hostIP.equals(myLanIP) || group.hostIP.equals("127.0.0.1")) {
            startVirtualHostAndConnect(group.groupName, group.port);
        } else {
            connectToGroup(group.groupName, group.hostIP, group.port);
        }
    }

    private void startVirtualHostAndConnect(String groupName, int port) {
        if (activeVirtualHost == null || !activeVirtualHost.isRunning || activeVirtualHost.port != port) {
            if (activeVirtualHost != null) activeVirtualHost.stop();
            activeVirtualHost = new VirtualHost(groupName, port);
            new Thread(activeVirtualHost).start();
            
            // --- LOGIC UPNP MỚI ---
            // Chạy trong Thread riêng vì UPnP check mất 1-2 giây
            new Thread(() -> {
                Platform.runLater(() -> {
                    publicIpLabel.setText("Đang mở Port " + port + "...");
                    publicIpLabel.setVisible(true);
                });

                if (UPnP.isUPnPAvailable()) {
                    if (UPnP.isMappedTCP(port)) {
                        System.out.println("Port " + port + " đã được map trước đó.");
                    } else if (UPnP.openPortTCP(port)) {
                        System.out.println("Đã mở Port " + port + " thành công qua UPnP!");
                    } else {
                        System.out.println("Không thể mở Port (có thể Router tắt UPnP).");
                    }
                    
                    String externalIP = UPnP.getExternalIP();
                    Platform.runLater(() -> {
                        publicIpLabel.setText("Public IP: " + externalIP + ":" + port);
                        showAlert("Đã Public lên Internet!\nBạn bè hãy nhập: " + externalIP + ":" + port);
                    });
                } else {
                    Platform.runLater(() -> publicIpLabel.setText("UPnP: Không hỗ trợ"));
                }
            }).start();
            // ----------------------
        }
        
        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            Platform.runLater(() -> connectToGroup(groupName, "127.0.0.1", port));
        }).start();
    }

    private void connectToGroup(String defaultName, String ip, int port) {
    	if (connectionToServer != null && connectionToServer.socket != null && !connectionToServer.socket.isClosed()) {
            try { 
                connectionToServer.socket.close(); // Ngắt socket cũ ngay lập tức
            } catch (IOException e) {}
        }
        new Thread(() -> {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 10000);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(username); 
                String realGroupName = in.readLine(); 
                currentActiveGroupName = realGroupName;
                String saveIP = (ip.equals("127.0.0.1")) ? myLanIP : ip;

                Platform.runLater(() -> {
                    saveOrUpdateGroup(realGroupName, saveIP, port);
                    setupChatRoomUI(realGroupName, ip, port);
                });

                connectionToServer = new PeerConnection("Virtual client", socket, out);
                String line;
                while ((line = in.readLine()) != null) processIncomingMessage(line);
            } catch (Exception e) {  
            }
        }).start();
    }

    private String getHistoryFileName(String groupName) {
        return HISTORY_DIR + "chat_" + groupName.replaceAll("[^a-zA-Z0-9]", "_") + ".log";
    }
    private void saveMessageToHistory(String groupName, String type, String sender, String content) {
        if (groupName == null || groupName.isEmpty()) return;
        try (PrintWriter writer = new PrintWriter(new FileWriter(getHistoryFileName(groupName), true))) {
            writer.println(type + "|" + sender + "|" + content);
        } catch (IOException e) { e.printStackTrace(); }
    }
    private void loadChatHistory(String groupName) {
        File file = new File(getHistoryFileName(groupName));
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) {
                    String type = parts[0]; String sender = parts[1]; String content = parts[2];
                    if (type.equals("CHAT")) addMessageBubble(content, sender.equals(username), sender);
                    else if (type.equals("SYSTEM")) addSystemMessage(content);
                }
            }
        } catch (IOException e) {}
    }

    private void saveOrUpdateGroup(String name, String ip, int port) {
        savedGroups.removeIf(g -> g.groupName.equals(name));
        savedGroups.add(0, new GroupData(name, ip, port, LocalDateTime.now()));
        saveDataToFile();
    }
    private void saveDataToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(new ArrayList<>(savedGroups));
        } catch (Exception e) {}
    }
    @SuppressWarnings("unchecked")
    private void loadGroupData() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            List<GroupData> list = (List<GroupData>) ois.readObject();
            savedGroups.setAll(list);
        } catch (Exception e) {}
    }

    private String getRealLanIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                String displayName = iface.getDisplayName().toLowerCase();
                if (displayName.contains("virtual") || displayName.contains("vmware") || displayName.contains("docker")) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                     // Thêm điều kiện: || ip.startsWith("26.")
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.") || ip.startsWith("26.")) {
                            return ip; // Trả về ngay khi thấy IP Radmin hoặc LAN
                        }                    }
                }
            }
        } catch (Exception e) {}
        return "127.0.0.1";
    }
    private void showAlert(String msg) { Alert a = new Alert(Alert.AlertType.INFORMATION, msg); a.show(); }

    private void returnToGameMenu() {
        try {
            stop(); 
            UnoGameMenu gameMenu = new UnoGameMenu(); 
            primaryStage.setFullScreen(true);
            gameMenu.start(primaryStage); 
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Không thể quay về Menu (Kiểm tra file UnoGameMenu.java)");
        }
    }

    // --- INNER CLASSES ---
    public static class GroupData implements Serializable {
        String groupName; String hostIP; int port; LocalDateTime lastAccess;
        public GroupData(String n, String i, int p, LocalDateTime t) { groupName = n; hostIP = i; port = p; lastAccess = t; }
    }
    
    private class GroupListCell extends ListCell<GroupData> {
        @Override
        protected void updateItem(GroupData item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                HBox root = new HBox(15);
                root.setAlignment(Pos.CENTER_LEFT);
                root.setPadding(new Insets(10));
                
                Rectangle cardIcon = new Rectangle(40, 56);
                cardIcon.setArcWidth(8); cardIcon.setArcHeight(8);
                cardIcon.setFill(UNO_RED); 
                cardIcon.setStroke(Color.WHITE); cardIcon.setStrokeWidth(2);
                
                StackPane iconStack = new StackPane(cardIcon, new Label("R") {{ setTextFill(Color.WHITE); setFont(Font.font("Arial", FontWeight.BOLD, 20)); }});
                
                VBox info = new VBox(5);
                Label name = new Label(item.groupName);
                name.setFont(Font.font("System", FontWeight.BOLD, 16));
                name.setTextFill(Color.WHITE);
                
                Label detail = new Label("Host: " + item.hostIP + ":" + item.port);
                detail.setFont(Font.font("System", 12));
                detail.setTextFill(Color.LIGHTGRAY);
                
                info.getChildren().addAll(name, detail);
                root.getChildren().addAll(iconStack, info);
                
                root.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");
                
                setGraphic(root);
                setStyle("-fx-background-color: transparent; -fx-padding: 5;");
            }
        }
    }

    class VirtualHost implements Runnable {
        private ServerSocket server;
        private String groupName;
        int port;
        boolean isRunning = false;
        private Map<String, PrintWriter> clients = new ConcurrentHashMap<>();
        public VirtualHost(String groupName, int port) { this.groupName = groupName; this.port = port; }
        public void stop() { isRunning = false; try { if(server != null) server.close(); } catch(Exception e){} }
        @Override public void run() {
            try {
                server = new ServerSocket(); server.setReuseAddress(true); server.bind(new InetSocketAddress(port));
                isRunning = true;
                while (isRunning) { Socket s = server.accept(); new Thread(() -> handleClient(s)).start(); }
            } catch (IOException e) { if (isRunning) Platform.runLater(() -> showAlert("Port " + port + " bận!")); isRunning = false; }
        }
        private void handleClient(Socket socket) {
            String id = socket.getInetAddress().toString() + ":" + socket.getPort();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                String name = in.readLine(); out.println(groupName);
                clients.put(id, out); broadcast("SYSTEM:➕ " + name + " vào phòng.");
                String line; while ((line = in.readLine()) != null) if(line.startsWith("CHAT:")) broadcast(line);
            } catch (IOException e) {} finally { clients.remove(id); }
        }
        private void broadcast(String msg) { for (PrintWriter w : clients.values()) w.println(msg); }
    }

    private static class PeerConnection {
        String name; Socket socket; PrintWriter out;
        PeerConnection(String n, Socket s, PrintWriter o) { name = n; socket = s; out = o; }
    }
}