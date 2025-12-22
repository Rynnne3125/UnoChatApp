package control;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import application.Main;
import application.UnoChatApp;
import application.UnoGameMenu;
import dao.FirebaseNewsRest;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import model.Post;
import model.User;
import utils.CatboxUploader;
import utils.NavigationHelper;

public class NewsController {

    // Static reference để cleanup khi exit app
    private static NewsController currentInstance = null;

    // --- FXML UI COMPONENTS ---
    @FXML private TextField searchField;
    @FXML private VBox menuContainer;
    @FXML private VBox feedContainer;
    @FXML private Circle userAvatarCircle;
    @FXML private Circle notificationBadge;
    @FXML private Label currentUserName;
    // --- DATA & STATE ---
    private User currentUser;
    private Button goHomeButton;
    private ScheduledExecutorService pollingScheduler;
    private ExecutorService backgroundExecutor; // Quản lý tất cả background threads

    // TỐI ƯU: Map để quản lý Node bạn bè, tránh xóa đi vẽ lại
    private Map<String, Node> friendNodeMap = new HashMap<>();

    // Biến tạm khi tạo post
    private File tempMediaFile = null;
    private String tempMediaType = null;

    // Container cho friends list
    private VBox friendsListContainer;

    // ==================== INITIALIZATION ====================

    public void initData() {
        this.currentUser = Main.CurrentUser;
        User user = this.currentUser ;
        currentUserName.setText(user.getUsername());
        // 1. Load Avatar User hiện tại (Resize nhỏ 100x100 để nhẹ)
        userAvatarCircle.setFill(Color.GRAY); // Default color
        try {
            Image img = ImageCache.get(user.getImageAvatar(), 100, 100);
            if (img != null) {
                if (img.getProgress() >= 1.0 && !img.isError()) {
                    // Image đã load xong
                    try {
                        userAvatarCircle.setFill(new ImagePattern(img));
                    } catch (Exception e) {
                        // Giữ màu gray nếu lỗi
                    }
                } else {
                    // Đợi image load xong
                    img.progressProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.doubleValue() >= 1.0 && !img.isError()) {
                            Platform.runLater(() -> {
                                try {
                                    userAvatarCircle.setFill(new ImagePattern(img));
                                } catch (Exception e) {
                                    // Nếu vẫn lỗi, giữ màu gray
                                }
                            });
                        }
                    });
                }
            }
        } catch (Exception e) {
            // Giữ màu gray nếu có lỗi
        }

        // Thêm event handler cho avatar: click để mở profile
        userAvatarCircle.setCursor(javafx.scene.Cursor.HAND);
        userAvatarCircle.setOnMouseClicked(e -> handleOpenProfile());

        // 2. Tạo nút "Go Home"
        goHomeButton = new Button("🏠  Go Home");
        goHomeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #d13639; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;");
        goHomeButton.setMaxWidth(Double.MAX_VALUE);
        goHomeButton.setOnAction(e -> handleRefresh());

        // 3. Tạo cấu trúc Friends List
        HBox friendsHeader = new HBox(10);
        friendsHeader.setAlignment(Pos.CENTER_LEFT);

        Label friendsTitle = new Label("Friends");
        friendsTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        friendsTitle.setTextFill(Color.web("#65676b"));

        Label onlineCount = new Label("(0 online)");
        onlineCount.setId("onlineCount");
        onlineCount.setStyle("-fx-font-size: 11px; -fx-text-fill: #31A24C;");

        friendsHeader.getChildren().addAll(friendsTitle, onlineCount);

        friendsListContainer = new VBox(8);
        friendsListContainer.setStyle("-fx-padding: 10 0 0 0;");

        VBox friendsSection = new VBox(8);
        friendsSection.getChildren().addAll(friendsHeader, friendsListContainer);

        menuContainer.getChildren().add(friendsSection);

        // 4. Khởi tạo ExecutorService để quản lý threads
        backgroundExecutor = Executors.newFixedThreadPool(5); // Giới hạn 5 threads

        // 5. Lưu instance hiện tại để cleanup khi exit
        currentInstance = this;


        loadFriendsList(); // Load lần đầu
        // 6. Load dữ liệu ban đầu
        loadNewsFeed();
        // 7. Bắt đầu polling (check tin nhắn/online status)
        startNotificationPolling();
    }

    /**
     * Static method để cleanup khi exit app
     */
    public static void cleanupAll() {
        if (currentInstance != null) {
            currentInstance.cleanup();
            currentInstance = null;
        }
        ImageCache.clear();
    }

    // ==================== CLEANUP & SHUTDOWN ====================

    /**
     * Cleanup tất cả resources khi thoát app
     */
    public void cleanup() {
        try {
            // Stop polling
            stopPolling();

            // Shutdown background executor - không chờ quá lâu để tránh hang
            if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
                backgroundExecutor.shutdownNow();
                try {
                    // Chỉ chờ tối đa 1 giây, sau đó force shutdown
                    if (!backgroundExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                        System.err.println("Background executor did not terminate gracefully, forcing shutdown");
                    }
                } catch (InterruptedException e) {
                    backgroundExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        } finally {
            // Clear friend node map
            friendNodeMap.clear();

            // Clear image cache
            ImageCache.clear();

            // Clear temp files
            tempMediaFile = null;
            tempMediaType = null;
        }
    }

    // ==================== OPTIMIZED FRIENDS LIST ====================

    private void loadFriendsList() {
        if (backgroundExecutor == null || backgroundExecutor.isShutdown()) {
            return;
        }
        Task<List<User>> loadFriendsTask = new Task<>() {
            @Override
            protected List<User> call() {
                return FirebaseNewsRest.getFriendsList(currentUser.getUsername());
            }
        };

        loadFriendsTask.setOnSucceeded(e -> {
            List<User> friends = loadFriendsTask.getValue();
            if (friends == null) return;

            // Update text số lượng online
            long onlineCount = friends.stream().filter(User::isOnlineStatus).count();
            updateOnlineCount((int) onlineCount, friends.size());

            if (friends.isEmpty()) {
                friendsListContainer.getChildren().clear();
                friendNodeMap.clear();
                Label noFriends = new Label("No friends yet");
                noFriends.setStyle("-fx-font-size: 12px; -fx-text-fill: #BCC0C4;");
                friendsListContainer.getChildren().add(noFriends);
                return;
            }

            // TỐI ƯU 1: Xóa các friend không còn trong list khỏi UI và Map
            List<String> currentFriendUsernames = friends.stream().map(User::getUsername).collect(Collectors.toList());
            friendsListContainer.getChildren().removeIf(node -> {
                if (node instanceof Label) return true; // Xóa label "No friends" cũ
                String id = (String) node.getUserData();
                boolean exists = currentFriendUsernames.contains(id);
                if (!exists) friendNodeMap.remove(id);
                return !exists;
            });

            // TỐI ƯU 2: Cập nhật status hoặc Thêm mới (Không xóa đi vẽ lại toàn bộ)
            for (User friend : friends) {
                if (friendNodeMap.containsKey(friend.getUsername())) {
                    // Node đã tồn tại -> Chỉ update chấm xanh/đỏ
                    HBox card = (HBox) friendNodeMap.get(friend.getUsername());
                    updateFriendStatusUI(card, friend.isOnlineStatus());
                } else {
                    // Node chưa có -> Tạo mới và thêm vào UI
                    HBox newCard = createFriendCard(friend);
                    newCard.setUserData(friend.getUsername()); // Key để quản lý
                    friendNodeMap.put(friend.getUsername(), newCard);
                    friendsListContainer.getChildren().add(newCard);
                }
            }

        });

        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.execute(loadFriendsTask);
        }
    }

    private void updateFriendStatusUI(HBox card, boolean isOnline) {
        // Tìm circle status trong cấu trúc node và đổi màu
        try {
            StackPane stack = (StackPane) card.getChildren().get(0);
            if (stack.getChildren().size() > 1) {
                Circle statusBadge = (Circle) stack.getChildren().get(1);
                Color targetColor = isOnline ? Color.web("#31A24C") : Color.web("#D13639");
                if (!statusBadge.getFill().equals(targetColor)) {
                    statusBadge.setFill(targetColor);
                }
            }
        } catch (Exception ignored) { }
    }

    private void updateOnlineCount(int online, int total) {
        Label onlineLabel = (Label) menuContainer.lookup("#onlineCount");
        if (onlineLabel != null) {
            onlineLabel.setText("(" + online + "/" + total + " online)");
        }
    }

    private HBox createFriendCard(User friend) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: transparent; -fx-padding: 8 10; -fx-background-radius: 8; -fx-cursor: hand;");
        card.setMaxWidth(Double.MAX_VALUE);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F0F2F5; -fx-padding: 8 10; -fx-background-radius: 8; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: transparent; -fx-padding: 8 10; -fx-background-radius: 8; -fx-cursor: hand;"));

        StackPane avatarStack = new StackPane();
        Circle avatar = new Circle(18);
        avatar.setFill(Color.GRAY); // Default color
        avatar.setStroke(Color.web("#E4E6EB"));
        avatar.setStrokeWidth(2);

        // TỐI ƯU: Load ảnh resize 40x40 và đợi load xong
        Image img = ImageCache.get(friend.getImageAvatar(), 40, 40);
        if (img != null) {
            if (img.getProgress() >= 1.0) {
                // Image đã load xong
                avatar.setFill(new ImagePattern(img));
            } else {
                // Đợi image load xong
                img.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() >= 1.0 && !img.isError()) {
                        Platform.runLater(() -> {
                            try {
                                avatar.setFill(new ImagePattern(img));
                            } catch (Exception e) {
                                // Nếu vẫn lỗi, giữ màu gray
                            }
                        });
                    }
                });
            }
        }

        Circle statusBadge = new Circle(6);
        statusBadge.setFill(friend.isOnlineStatus() ? Color.web("#31A24C") : Color.web("#D13639"));
        statusBadge.setStroke(Color.WHITE);
        statusBadge.setStrokeWidth(2);
        statusBadge.setTranslateX(12);
        statusBadge.setTranslateY(12);

        avatarStack.getChildren().addAll(avatar, statusBadge);

        Label nameLabel = new Label(friend.getUsername());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e60026;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        card.getChildren().addAll(avatarStack, nameLabel);

        // Click vào friend card để hiện dialog với options
        card.setOnMouseClicked(e -> showFriendOptionsDialog(friend));

        return card;
    }

    // ==================== POLLING (5s/lần) ====================

    private void startNotificationPolling() {
        pollingScheduler = Executors.newSingleThreadScheduledExecutor();
        pollingScheduler.scheduleAtFixedRate(() -> {
            try {
                // Nếu executor chính đã tắt thì dừng polling ngay
                if (backgroundExecutor == null || backgroundExecutor.isShutdown()) {
                    return;
                }

                Map<String, String> requests = FirebaseNewsRest.getFriendRequests(currentUser.getUsername());
                boolean hasRequests = requests != null && !requests.isEmpty();

                Platform.runLater(() -> {
                    // --- THÊM KIỂM TRA TẠI ĐÂY ---
                    if (backgroundExecutor == null || backgroundExecutor.isShutdown()) return;

                    if (notificationBadge != null) notificationBadge.setVisible(hasRequests);
                    loadFriendsList();
                });
            } catch (Exception e) {
                System.err.println("Polling silent error: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
    private void stopPolling() {
        if (pollingScheduler != null && !pollingScheduler.isShutdown()) {
            pollingScheduler.shutdownNow();
            try {
                // Chỉ chờ tối đa 1 giây để tránh hang
                if (!pollingScheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.err.println("Polling scheduler did not terminate gracefully");
                }
            } catch (InterruptedException e) {
                pollingScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // ==================== FEED & POST ====================

    @FXML private void handleRefresh() { loadNewsFeed(); }
    @FXML
    private void loadNewsFeed() {
        feedContainer.getChildren().clear();
        if (menuContainer.getChildren().contains(goHomeButton)) {
            menuContainer.getChildren().remove(goHomeButton);
        }
        searchField.clear();

        Label loadingLabel = new Label("Loading posts...");
        loadingLabel.setTextFill(Color.GRAY);
        feedContainer.getChildren().add(loadingLabel);

        Task<List<Post>> loadTask = new Task<>() {
            @Override protected List<Post> call() { return FirebaseNewsRest.getAllPosts(); }
        };

        loadTask.setOnSucceeded(e -> {
            feedContainer.getChildren().clear();
            List<Post> posts = loadTask.getValue();
            if (posts.isEmpty()) {
                Label emptyLabel = new Label("No posts yet. Be the first to share! 🎮");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #65676b;");
                feedContainer.getChildren().add(emptyLabel);
            } else {
                for (Post post : posts) {
                    feedContainer.getChildren().add(createPostView(post));
                }
            }
        });

        loadTask.setOnFailed(e -> {
            feedContainer.getChildren().clear();
            feedContainer.getChildren().add(new Label("Failed to load feed."));
        });

        backgroundExecutor.execute(loadTask);
    }

    private VBox createPostView(Post post) {
        VBox postBox = new VBox(12);
        postBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1e2328 0%, #0f1923 100%); -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 12, 0, 0, 4); -fx-border-color: #3c3c41; -fx-border-width: 1; -fx-border-radius: 12;");
        postBox.setPadding(new Insets(18));
        postBox.setMaxWidth(650);

        // --- Header ---
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle avt = new Circle(22);
        avt.setFill(Color.GRAY); // Default color
        // TỐI ƯU: Avatar bài viết resize 50x50 và đợi load xong
        Image authorImg = ImageCache.get(post.getAuthorAvatar(), 50, 50);
        if (authorImg != null) {
            if (authorImg.getProgress() >= 1.0) {
                // Image đã load xong
                try {
                    avt.setFill(new ImagePattern(authorImg));
                } catch (Exception e) {
                    // Giữ màu gray nếu lỗi
                }
            } else {
                // Đợi image load xong
                authorImg.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() >= 1.0 && !authorImg.isError()) {
                        Platform.runLater(() -> {
                            try {
                                avt.setFill(new ImagePattern(authorImg));
                            } catch (Exception e) {
                                // Nếu vẫn lỗi, giữ màu gray
                            }
                        });
                    }
                });
            }
        }

        VBox info = new VBox(2);
        Label name = new Label(post.getAuthorUsername());
        name.setFont(Font.font("System", FontWeight.BOLD, 15));

        long diff = System.currentTimeMillis() - post.getTimestamp();
        String timeStr = (diff < 60000) ? "Just now" : (diff / 60000) + " mins ago";
        if (diff > 3600000) timeStr = (diff / 3600000) + " hours ago";
        Label time = new Label(timeStr);
        time.setTextFill(Color.GRAY);
        time.setFont(Font.font(12));
        info.getChildren().addAll(name, time);

        header.getChildren().addAll(avt, info);

        // --- Content ---
        Label content = new Label(post.getContent());
        content.setWrapText(true);
        content.setFont(Font.font(14));
        content.setTextFill(Color.web("#f0e6d2"));
        // --- Media ---
        Node mediaNode = null;
        if (post.getMediaUrl() != null && !post.getMediaUrl().isEmpty()) {
            if ("IMAGE".equals(post.getMediaType())) {
                ImageView iv = new ImageView();
                iv.setFitWidth(620);
                iv.setPreserveRatio(true);
                iv.setStyle("-fx-background-radius: 8;");

                // TỐI ƯU: Load ảnh post resize chiều ngang 800px (chiều cao tự tính)
                // Ảnh sẽ tự load async, không cần Task wrapper
                iv.setImage(ImageCache.get(post.getMediaUrl(), 800, 0));

                mediaNode = iv;
            } else if ("VIDEO".equals(post.getMediaType())) {
                Label videoLabel = new Label("🎥 Video Content");
                videoLabel.setStyle("-fx-background-color: #F0F2F5; -fx-padding: 20; -fx-background-radius: 8;");
                mediaNode = videoLabel;
            }
        }

        // --- Actions (Like/Comment) ---
        HBox actions = new HBox(25);
        Button likeBtn = new Button("❤ " + post.getLikeCount());
        likeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676b; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
        likeBtn.setOnAction(e -> {
            post.addLike();
            likeBtn.setText("❤ " + post.getLikeCount());
            likeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d13639; -fx-font-weight: bold; -fx-font-size: 14px;");
            backgroundExecutor.execute(() -> FirebaseNewsRest.updatePostLikes(post.getId(), post.getLikeCount()));
        });

        Button commentBtn = new Button("💬 Comment");
        commentBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #7289DA; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
        actions.getChildren().addAll(likeBtn, commentBtn);

        // --- Comments ---
        VBox commentSection = new VBox(8);
        commentSection.setStyle("-fx-background-color: rgba(31, 35, 40, 0.6); -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #3c3c41; -fx-border-width: 1;");
        if (post.getComments() != null) {
            for (String oldCmt : post.getComments()) {
                Label l = new Label(oldCmt);
                l.setWrapText(true);
                l.setStyle("-fx-background-color: linear-gradient(to right, #0ac8b9, #0a9fa6); -fx-text-fill: #f0e6d2; -fx-padding: 8 12; -fx-background-radius: 10; -fx-font-size: 12;");
                commentSection.getChildren().add(l);
            }
        }
        TextField cmtInput = new TextField();
        cmtInput.setPromptText("Write a comment...");
        cmtInput.setStyle("-fx-background-color: #36393F; -fx-background-radius: 15;");
        cmtInput.setOnAction(e -> {
            String txt = cmtInput.getText().trim();
            if (!txt.isEmpty()) {
                String full = currentUser.getUsername() + ": " + txt;
                Label newL = new Label(full);
                newL.setWrapText(true);
                newL.setStyle("-fx-background-color: white; -fx-padding: 8 12; -fx-background-radius: 12;");
                commentSection.getChildren().add(commentSection.getChildren().size() - 1, newL);
                cmtInput.clear();
                post.addComment(full);
                backgroundExecutor.execute(() -> FirebaseNewsRest.addCommentToPost(post.getId(), full));
            }
        });
        commentSection.getChildren().add(cmtInput);

        postBox.getChildren().addAll(header, content);
        if (mediaNode != null) postBox.getChildren().add(mediaNode);
        postBox.getChildren().addAll(new Separator(), actions, commentSection);
        return postBox;
    }

    // ==================== CREATE POST DIALOG ====================

    @FXML
    private void handleCreatePost() {
        if (feedContainer == null || feedContainer.getScene() == null) return;

        Parent root = feedContainer.getScene().getRoot();
        StackPane sceneRoot;
        if (root instanceof StackPane) {
            sceneRoot = (StackPane) root;
        } else {
            sceneRoot = new StackPane(root);
            feedContainer.getScene().setRoot(sceneRoot);
        }
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.65);");
        overlay.setPickOnBounds(true);

        VBox card = new VBox(12);
        card.setMaxWidth(520);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");

        HBox header = new HBox();
        Label title = new Label("Create New Post");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✖");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-text-fill: #666;");
        header.getChildren().addAll(title, spacer, closeBtn);

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("What's on your mind?");
        contentArea.setWrapText(true);

        VBox previewBox = new VBox(10);
        previewBox.setAlignment(Pos.CENTER);

        HBox mediaButtons = new HBox(10);
        Button selectImageBtn = new Button("📷 Image");
        Button removeMediaBtn = new Button("❌ Remove");
        removeMediaBtn.setDisable(true);

        selectImageBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            File file = fc.showOpenDialog(feedContainer.getScene().getWindow());
            if (file != null) {
                tempMediaFile = file;
                tempMediaType = "IMAGE";
                removeMediaBtn.setDisable(false);
                ImageView preview = new ImageView(new Image(file.toURI().toString()));
                preview.setFitWidth(360);
                preview.setPreserveRatio(true);
                previewBox.getChildren().setAll(preview);
            }
        });

        removeMediaBtn.setOnAction(e -> {
            tempMediaFile = null;
            tempMediaType = null;
            removeMediaBtn.setDisable(true);
            previewBox.getChildren().clear();
        });
        mediaButtons.getChildren().addAll(selectImageBtn, removeMediaBtn);

        Button postBtn = new Button("Post");
        postBtn.setStyle("-fx-background-color: #d13639; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 16; -fx-background-radius: 8;");
        postBtn.setOnAction(e -> {
            if (contentArea.getText().trim().isEmpty() && tempMediaFile == null) return;
            postBtn.setDisable(true);
            postBtn.setText("Posting...");

            Task<Void> createPostTask = new Task<>() {
                @Override protected Void call() throws Exception {
                    String mediaUrl = "";
                    if (tempMediaFile != null) mediaUrl = CatboxUploader.uploadFile(tempMediaFile);
                    Post newPost = new Post(currentUser.getUsername(), currentUser.getImageAvatar(), contentArea.getText(), mediaUrl, tempMediaFile != null ? tempMediaType : "NONE");
                    FirebaseNewsRest.createPost(newPost);
                    return null;
                }
            };
            createPostTask.setOnSucceeded(ev -> {
                sceneRoot.getChildren().remove(overlay);
                handleRefresh();
            });
            createPostTask.setOnFailed(ev -> {
                postBtn.setDisable(false);
                showCustomAlert(Alert.AlertType.ERROR, "Error", "Failed to post.");
            });
            backgroundExecutor.execute(createPostTask);
        });

        closeBtn.setOnAction(e -> {
            sceneRoot.getChildren().remove(overlay);
            // If we wrapped a BorderPane, unwrap to original root to avoid further casts
            if (sceneRoot.getChildren().size() == 1 && sceneRoot.getChildren().get(0) == root && feedContainer.getScene().getRoot() == sceneRoot) {
                feedContainer.getScene().setRoot(root);
            }
        });

        card.getChildren().addAll(header, contentArea, mediaButtons, previewBox, postBtn);
        overlay.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);
        sceneRoot.getChildren().add(overlay);
    }

    // ==================== SEARCH & FRIEND SEARCH ====================

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty() || query.equalsIgnoreCase(currentUser.getUsername())) return;

        feedContainer.getChildren().clear();
        if (!menuContainer.getChildren().contains(goHomeButton)) {
            menuContainer.getChildren().add(0, goHomeButton);
        }
        feedContainer.getChildren().add(new ProgressIndicator());

        Task<User> searchTask = new Task<>() {
            @Override protected User call() { return FirebaseNewsRest.findUser(query); }
        };

        searchTask.setOnSucceeded(e -> {
            feedContainer.getChildren().clear();
            User found = searchTask.getValue();
            if (found != null) feedContainer.getChildren().add(createSearchResultCard(found));
            else feedContainer.getChildren().add(new Label("User not found."));
        });
        backgroundExecutor.execute(searchTask);
    }

    private VBox createSearchResultCard(User user) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);

        StackPane avatarStack = new StackPane();
        Circle avatar = new Circle(50);
        avatar.setFill(Color.GRAY); // Default color
        avatar.setStroke(Color.web("#E4E6EB"));
        avatar.setStrokeWidth(3);

        // TỐI ƯU: Resize 100x100 và đợi load xong
        Image searchImg = ImageCache.get(user.getImageAvatar(), 100, 100);
        if (searchImg != null) {
            if (searchImg.getProgress() >= 1.0) {
                // Image đã load xong
                try {
                    avatar.setFill(new ImagePattern(searchImg));
                } catch (Exception e) {
                    // Giữ màu gray nếu lỗi
                }
            } else {
                // Đợi image load xong
                searchImg.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() >= 1.0 && !searchImg.isError()) {
                        Platform.runLater(() -> {
                            try {
                                avatar.setFill(new ImagePattern(searchImg));
                            } catch (Exception e) {
                                // Nếu vẫn lỗi, giữ màu gray
                            }
                        });
                    }
                });
            }
        }

        Circle statusBadge = new Circle(12);
        statusBadge.setFill(user.isOnlineStatus() ? Color.web("#31A24C") : Color.web("#D13639"));
        statusBadge.setStroke(Color.WHITE);
        statusBadge.setStrokeWidth(3);
        statusBadge.setTranslateX(35);
        statusBadge.setTranslateY(35);
        avatarStack.getChildren().addAll(avatar, statusBadge);

        Label name = new Label(user.getUsername());
        name.setFont(Font.font(20));

        Button actionBtn = new Button("Add Friend");
        actionBtn.setStyle("-fx-background-color: #d13639; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        // Logic kiểm tra friend (như cũ)
        backgroundExecutor.execute(() -> {
            List<User> friends = FirebaseNewsRest.getFriendsList(currentUser.getUsername());
            boolean isFriend = friends.stream().anyMatch(f -> f.getUsername().equals(user.getUsername()));
            Platform.runLater(() -> {
                if (isFriend) {
                    actionBtn.setText("Already Friends");
                    actionBtn.setDisable(true);
                } else {
                    actionBtn.setOnAction(ev -> {
                        actionBtn.setDisable(true);
                        backgroundExecutor.execute(() -> {
                            FirebaseNewsRest.sendFriendRequest(currentUser.getUsername(), user.getUsername());
                            Platform.runLater(() -> actionBtn.setText("Sent ✓"));
                        });
                    });
                }
            });
        });

        card.getChildren().addAll(avatarStack, name, actionBtn);
        return card;
    }

    // ==================== NOTIFICATIONS & LOGOUT ====================

    @FXML
    private void handleNotifications() {
        Task<Map<String, String>> checkTask = new Task<>() {
            @Override protected Map<String, String> call() {
                return FirebaseNewsRest.getFriendRequests(currentUser.getUsername());
            }
        };
        checkTask.setOnSucceeded(e -> {
            Map<String, String> reqs = checkTask.getValue();
            if (reqs != null && !reqs.isEmpty()) showFriendRequestsDialog(reqs);
            else showCustomAlert(Alert.AlertType.INFORMATION, "Notifications", "No new notifications.");
        });
        backgroundExecutor.execute(checkTask);
    }

    public void showFriendRequestsDialog(Map<String, String> requests) {
        Stage dialog = new Stage();
        Window owner = Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        // --- Root Pane ---
        StackPane rootPane = new StackPane();
        rootPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0f1923, #0a0e27);" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: linear-gradient(to right, #d13639, #f05a5a);" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 20;"
        );
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#d13639"));
        glow.setRadius(20);
        glow.setSpread(0.1);
        rootPane.setEffect(glow);
        rootPane.setPadding(new Insets(2));

        // --- Content ---
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(25));
        mainContent.setStyle("-fx-background-radius: 18; -fx-background-color: transparent;");

        // Header
        Label titleLbl = new Label("PENDING REQUESTS");
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLbl.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 2);");
        HBox headerBox = new HBox(titleLbl);
        headerBox.setAlignment(Pos.CENTER);

        // List Container
        VBox requestsContainer = new VBox(12);
        requestsContainer.setStyle("-fx-background-color: transparent;");

        if (requests == null || requests.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(40, 0, 40, 0));
            Label emptyIcon = new Label("📭");
            emptyIcon.setStyle("-fx-font-size: 40px;");
            Label emptyText = new Label("No new friend requests.");
            emptyText.setStyle("-fx-text-fill: #7a7d82; -fx-font-size: 14px; -fx-font-weight: bold;");
            emptyState.getChildren().addAll(emptyIcon, emptyText);
            requestsContainer.getChildren().add(emptyState);
        } else {
            for (Map.Entry<String, String> entry : requests.entrySet()) {
                HBox row = createRequestRow(entry.getKey(), dialog);
                requestsContainer.getChildren().add(row);
            }
        }

        // --- ScrollPane (Nguyên nhân gây lỗi) ---
        ScrollPane scrollPane = new ScrollPane(requestsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(350);

        // Style cơ bản cho ScrollPane
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 0;" +
                        "-fx-hbar-policy: never;" +
                        "-fx-vbar-policy: as_needed;"
        );

        // Close Button
        Button closeBtn = new Button("CLOSE");
        closeBtn.setPrefWidth(Double.MAX_VALUE);
        closeBtn.setStyle("-fx-background-color: #1e2328; -fx-text-fill: #7a7d82; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 12;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #2a2e35; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 12;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: #1e2328; -fx-text-fill: #7a7d82; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 12;"));
        closeBtn.setOnAction(e -> dialog.close());

        mainContent.getChildren().addAll(headerBox, scrollPane, closeBtn);
        rootPane.getChildren().add(mainContent);

        Scene scene = new Scene(rootPane, 420, 550);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);

        // === KHẮC PHỤC LỖI TẠI ĐÂY ===
        // Chỉ chạy lệnh lookup khi cửa sổ đã hiển thị (Shown)
        dialog.setOnShown(e -> {
            javafx.scene.Node viewport = scrollPane.lookup(".viewport");
            if (viewport != null) {
                viewport.setStyle("-fx-background-color: transparent;");
            }
        });
        // ============================

        dialog.show();
    }
    // --- Hàm hỗ trợ tạo từng dòng (Row) ---
    private HBox createRequestRow(String senderUsername, Stage dialog) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 15, 12, 15));
        // Style cho thẻ: Nền tối hơn, bo góc
        row.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;"
        );

        // 1. Avatar Placeholder (Hình tròn)
        Circle avatar = new Circle(22);
        // Tạo gradient cho avatar
        Stop[] stops = new Stop[] { new Stop(0, Color.web("#d13639")), new Stop(1, Color.web("#f05a5a"))};
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        avatar.setFill(gradient);
        avatar.setStroke(Color.web("#1e2328"));
        avatar.setStrokeWidth(2);
        avatar.setEffect(new DropShadow(5, Color.BLACK));

        StackPane avatarPane = new StackPane(avatar);
        // Thêm chữ cái đầu của tên vào avatar
        Label initialLabel = new Label(senderUsername.substring(0, 1).toUpperCase());
        initialLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        avatarPane.getChildren().add(initialLabel);


        // 2. Tên người gửi
        Label nameLbl = new Label(senderUsername);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        HBox.setHgrow(nameLbl, Priority.ALWAYS); // Để tên chiếm khoảng trống còn lại

        // 3. Các nút hành động (Dạng icon tròn)
        HBox actionsBox = new HBox(10);

        // Nút Accept (Xanh lá)
        Button accBtn = createActionButton("✔", "#379711");
        accBtn.setOnAction(e -> {
            // Hiệu ứng khi click: disable dòng và đổi màu
            row.setDisable(true);
            row.setStyle(row.getStyle() + "; -fx-opacity: 0.5;");

            // Logic cũ của bạn
            if (backgroundExecutor != null && currentUser != null) {
                backgroundExecutor.execute(() -> {
                    FirebaseNewsRest.acceptFriendRequest(currentUser.getUsername(), senderUsername);
                    // Sau khi xong có thể update UI thêm nếu cần, ví dụ: hiện thông báo nhỏ
                    Platform.runLater(() -> {
                        // Có thể remove row khỏi list nếu muốn: ((VBox)row.getParent()).getChildren().remove(row);
                        // Hoặc giữ nguyên trạng thái disabled như hiện tại.
                    });
                });
            } else {
                System.err.println("Error: backgroundExecutor or currentUser is null in showFriendRequestsDialog");
            }
        });

        // Nút Decline (Đỏ) - (Thêm vào cho đủ bộ, logic có thể làm sau)
        Button declineBtn = createActionButton("✖", "#D72600");
        declineBtn.setOnAction(e -> {
            row.setDisable(true);
            row.setStyle(row.getStyle() + "; -fx-opacity: 0.5;");
            // TODO: Gọi hàm từ chối kết bạn ở đây
            System.out.println("Declined request from: " + senderUsername);
        });

        actionsBox.getChildren().addAll(accBtn, declineBtn);

        row.getChildren().addAll(avatarPane, nameLbl, actionsBox);
        return row;
    }

    // Hàm hỗ trợ tạo nút tròn icon
    private Button createActionButton(String iconText, String colorHex) {
        Button btn = new Button(iconText);
        btn.setPrefSize(36, 36);
        btn.setMinSize(36, 36);
        btn.setMaxSize(36, 36);

        String baseStyle =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + colorHex + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 16px;" +
                        "-fx-border-color: " + colorHex + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 18;" + // Bán kính bằng một nửa kích thước để tròn
                        "-fx-background-radius: 18;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0;";

        String hoverStyle =
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 16px;" +
                        "-fx-border-color: " + colorHex + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 18;" +
                        "-fx-background-radius: 18;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0;";

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));

        return btn;
    }

    @FXML
    private void handleLogout() {
        // Cleanup tất cả resources trước khi logout
        cleanup();

        try {
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setMaximized(true);
            // Khởi tạo Menu và chạy trên Stage hiện tại
            UnoGameMenu gameMenu = new UnoGameMenu();
            gameMenu.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hiện dialog với options khi click vào friend
     */
    private void showFriendOptionsDialog(User friend) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Friend Options");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(friend.getUsername());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        // Button Direct Message
        Button messageBtn = new Button("💬 Direct Message");
        messageBtn.setStyle("-fx-background-color: #d13639; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        messageBtn.setOnAction(e -> {
            dialog.close(); // Đóng dialog option
            try {
                // 1. Cleanup resource của NewsController hiện tại
                cleanup();

                // 2. Truyền tên người muốn chat sang UnoChatApp
                UnoChatApp.setTargetFriend(friend.getUsername());

                // 3. Chuyển cảnh sang UnoChatApp
                Stage stage = (Stage) searchField.getScene().getWindow();
                stage.setMaximized(true);

                UnoChatApp chatApp = new UnoChatApp();
                chatApp.start(stage);

            } catch (Exception ex) {
                ex.printStackTrace();
                showCustomAlert(Alert.AlertType.ERROR, "Error", "Cannot open Chat App.");
            }
        });

        // Button Unfriend
        Button unfriendBtn = new Button("❌ Unfriend");
        unfriendBtn.setStyle("-fx-background-color: #65676b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        unfriendBtn.setOnAction(e -> {
            dialog.close();
            handleUnfriend(friend);
        });

        // Button Cancel
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #E4E6EB; -fx-text-fill: #050505; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        buttonsBox.getChildren().addAll(messageBtn, unfriendBtn, cancelBtn);
        root.getChildren().addAll(titleLabel, buttonsBox);

        dialog.setScene(new Scene(root, 400, 150));
        dialog.show();
    }

    /**
     * Xử lý unfriend
     */
    private void handleUnfriend(User friend) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Unfriend");
        confirmAlert.setHeaderText("Unfriend " + friend.getUsername() + "?");
        confirmAlert.setContentText("Are you sure you want to remove this friend?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Disable UI trong khi xử lý
                backgroundExecutor.execute(() -> {
                    boolean success = FirebaseNewsRest.unfriend(currentUser.getUsername(), friend.getUsername());
                    Platform.runLater(() -> {
                        if (success) {
                            showCustomAlert(Alert.AlertType.INFORMATION, "Success", "You have unfriended " + friend.getUsername());
                            // Reload friends list
                            loadFriendsList();
                        } else {
                            showCustomAlert(Alert.AlertType.ERROR, "Error", "Failed to unfriend. Please try again.");
                        }
                    });
                });
            }
        });
    }

    private void showCustomAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- NAVIGATION BACK ---
    @FXML
    private void handleBack() {
        if (menuContainer == null || menuContainer.getScene() == null) return;
        Stage stage = (Stage) menuContainer.getScene().getWindow();
        NavigationHelper.backToGameMenu(stage);
    }

    @FXML
    private void handleOpenProfile() {
        if (searchField == null || searchField.getScene() == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/profileView.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.initData(Main.CurrentUser);

            Stage stage = (Stage) searchField.getScene().getWindow();
            Scene profileScene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());

            root.setOpacity(0);
            stage.setScene(profileScene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } catch (IOException ex) {
            ex.printStackTrace();
            showCustomAlert(Alert.AlertType.ERROR, "Error", "Failed to load profile view.");
        }
    }
}

// ==================== HELPER CLASS: IMAGE CACHE (LRU) ====================
// Tối ưu RAM: LRU Cache với giới hạn để tránh tràn RAM
class ImageCache {
    private static final int MAX_CACHE_SIZE = 50; // Giới hạn tối đa 50 ảnh
    private static Map<String, Image> cache = new LinkedHashMap<String, Image>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
            // Xóa ảnh cũ nhất khi vượt quá giới hạn
            return size() > MAX_CACHE_SIZE;
        }
    };

    public static synchronized Image get(String url, double w, double h) {
        if (url == null || url.isEmpty()) return null;

        // Key bao gồm URL và kích thước để phân biệt ảnh thumbnail và full
        String key = url + "_" + w + "_" + h;

        if (!cache.containsKey(key)) {
            try {
                // backgroundLoading = true (load ngầm, không đơ UI)
                Image img = new Image(url, w, h, true, true, true);

                // Thêm error listener để xóa khỏi cache nếu load fail
                img.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        synchronized (ImageCache.class) {
                            cache.remove(key);
                        }
                    }
                });

                cache.put(key, img);
            } catch (Exception e) {
                // Nếu có lỗi khi tạo Image, return null
                System.err.println("Error creating image from URL: " + url);
                return null;
            }
        }

        Image cached = cache.get(key);
        // Kiểm tra nếu image bị lỗi thì xóa khỏi cache và return null
        if (cached != null && cached.isError()) {
            cache.remove(key);
            return null;
        }

        return cached;
    }

    /**
     * Clear toàn bộ cache để giải phóng RAM
     */
    public static synchronized void clear() {
        cache.clear();
    }

    /**
     * Lấy số lượng ảnh trong cache
     */
    public static synchronized int size() {
        return cache.size();
    }
}