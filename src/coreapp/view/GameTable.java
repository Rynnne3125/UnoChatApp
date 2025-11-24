package coreapp.view;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

// Giả định các import model/data vẫn giữ nguyên
import coreapp.data.UserStatisticRepository;
import coreapp.model.GameSession;
import coreapp.model.cards.ActionCard;
import coreapp.model.cards.Card;
import coreapp.model.cards.WildCard;
import coreapp.model.enums.ActionType;
import coreapp.model.enums.WildType;
import coreapp.model.player.Bot;
import coreapp.model.player.Player;
import coreapp.model.user.User;
import coreapp.util.constants.*;
import coreapp.util.helpers.Logger;
import coreapp.util.session.CurrentUserManager;
import coreapp.util.ui.GameTableLayoutHelper; // Cần điều chỉnh helper này trả về index thay vì GBC
import coreapp.util.ui.toaster.Toaster;
import coreapp.view.Popups.ColorSelectionPopup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameTable extends Stage {

    private Toaster toaster;
    private GameSession gameSession;
    private Pane[][] cells;
    private int[][] mainCells;
    private int numberOfPlayers;
    private ImageView discardPileLabel;
    private Button drawPileButton;
    private Label cardCountInDrawPile;
    
    // Load font an toàn hơn
    private Font customFont; 
    
    private boolean drawPileButtonClicked = false;
    private TextArea gameStatusArea;
    private int botDelay = 3000;

    // Kích thước mặc định nếu không có trong constants
    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 720;

    public GameTable(int numberOfPlayers, String gameSessionName) {
        this.setTitle(WindowConstants.GAME_TABLE_WINDOW + " : " + gameSessionName);
        this.numberOfPlayers = numberOfPlayers;

        // Load font
        try {
            customFont = Font.loadFont(getClass().getResourceAsStream(FontConstants.RechargeFontPath), 12);
        } catch (Exception e) {
            customFont = Font.font("Arial", 12); // Fallback font
        }

        gameSession = new GameSession(numberOfPlayers, gameSessionName);
        gameSession.initializeGameSession();
        gameSession.setSessionName(gameSessionName);

        initializeFrame();
        
        // Logic lấy vị trí cell người chơi
        mainCells = GameTableLayoutHelper.getPlayerCells(numberOfPlayers);
        
        addBotPlayerElements();
        paintUserCell();
        addCenterElements();
        paintUserCell();
    }

    void initializeFrame() {
        int rows = GameTableLayoutHelper.rows;
        int columns = GameTableLayoutHelper.columns;
        
        GridPane mainPanel = new GridPane();
        // Cấu hình Grid để fill màn hình
        mainPanel.setAlignment(Pos.CENTER);
        
        // Thiết lập Constraints để Grid co giãn đều (Thay thế cho GridBagConstraints weightx/y)
        for (int i = 0; i < columns; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / columns);
            colConst.setHgrow(Priority.ALWAYS);
            mainPanel.getColumnConstraints().add(colConst);
        }
        for (int i = 0; i < rows; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / rows);
            rowConst.setVgrow(Priority.ALWAYS);
            mainPanel.getRowConstraints().add(rowConst);
        }

        toaster = new Toaster(mainPanel);
        cells = new Pane[rows][columns];

        // Tạo các cell rỗng
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                cells[i][j] = new Pane();
                cells[i][j].setStyle("-fx-border-color: black; -fx-background-color: #2c3e50;"); // Màu nền ví dụ
                
                // Add vào grid
                mainPanel.add(cells[i][j], j, i);
            }
        }

        // Setup Scene
        Scene scene = new Scene(mainPanel, WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setScene(scene);
        
        // Xử lý đóng cửa sổ
        this.setOnCloseRequest(e -> leaveGame());
    }

    void addCenterElements() {
        var centerPanel = getCenterPanel();
        // Sử dụng AnchorPane hoặc StackPane cho center để dễ absolute positioning hơn Pane thường
        // Tuy nhiên giữ nguyên Pane theo code cũ nhưng chỉnh lại coordinate tương đối
        
        centerPanel.setPadding(Insets.EMPTY);
        centerPanel.setStyle("-fx-background-color: #34495e;"); // Màu bàn chơi giữa

        gameStatusArea = new TextArea();
        gameStatusArea.setEditable(false);
        gameStatusArea.setWrapText(true);
        // CSS cho TextArea trong suốt
        gameStatusArea.setStyle("-fx-control-inner-background: rgba(0,0,0,0.5); -fx-background-color: transparent; -fx-text-fill: white;");
        gameStatusArea.setPrefSize(295, 255);
        gameStatusArea.setFont(Font.font("Arial", 14));

        ScrollPane scrollPane = new ScrollPane(gameStatusArea);
        scrollPane.setLayoutX(10);
        scrollPane.setLayoutY(70);
        scrollPane.setPrefSize(295, 255);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Auto scroll down
        gameStatusArea.textProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> gameStatusArea.setScrollTop(Double.MAX_VALUE));
        });

        centerPanel.getChildren().add(scrollPane);

        Separator verticalLine = new Separator();
        verticalLine.setOrientation(javafx.geometry.Orientation.VERTICAL);
        verticalLine.setLayoutX(375);
        verticalLine.setPrefHeight(400); // Set chiều cao cố định hoặc bind theo centerPanel
        ((Property<Number>) verticalLine.heightProperty()).bind(centerPanel.heightProperty());
        verticalLine.setStyle("-fx-background-color: black;");
        centerPanel.getChildren().add(verticalLine);

        // Draw Pile
        drawPileButton = new Button();
        drawPileButton.setLayoutX(425);
        drawPileButton.setLayoutY(90);
        drawPileButton.setPrefSize(130, 150);
        drawPileButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        drawPileButton.setOnAction(e -> {
            if (!drawPileButtonClicked) {
                var drawnCard = gameSession.drawCard();
                gameSession.getPlayers().get(0).addCard(drawnCard);
                paintUserCell();

                if (gameSession.getDrawPileCardCount() == 0) {
                    gameSession.reshuffleDiscardPile();
                }

                updateCardCountInDrawPile(gameSession.getDrawPileCardCount());
                updateDrawPileImage();
            }
        });
        centerPanel.getChildren().add(drawPileButton);

        // Discard Pile
        discardPileLabel = new ImageView();
        discardPileLabel.setLayoutX(565);
        discardPileLabel.setLayoutY(90);
        discardPileLabel.setFitWidth(103);
        discardPileLabel.setFitHeight(150);
        // ImageView không có border trực tiếp, bọc trong StackPane nếu cần border
        centerPanel.getChildren().add(discardPileLabel);

        // Leave Button
        Button leaveGameBtn = new Button();
        try {
            Image icon = new Image(ImagePath.BACK_ICON);
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(40);
            imageView.setFitHeight(40);
            leaveGameBtn.setGraphic(imageView);
        } catch (Exception e) {
            leaveGameBtn.setText("X"); // Fallback
        }
        
        leaveGameBtn.setOnAction(e -> leaveGame());
        leaveGameBtn.setLayoutX(10);
        leaveGameBtn.setLayoutY(10);
        leaveGameBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        centerPanel.getChildren().add(leaveGameBtn);

        cardCountInDrawPile = new Label();
        cardCountInDrawPile.setFont(Font.font(customFont.getFamily(), 22));
        cardCountInDrawPile.setTextFill(Color.WHITE);
        cardCountInDrawPile.setLayoutX(465);
        cardCountInDrawPile.setLayoutY(245);
        // Chặn sự kiện chuột để bấm xuyên qua vào nút draw pile
        cardCountInDrawPile.setMouseTransparent(true); 
        centerPanel.getChildren().add(cardCountInDrawPile);

        updateCardCountInDrawPile(gameSession.getDrawPileCardCount());
        updateDrawPileImage();

        addStatusMessage(UnoStatusMessages.getGameStartMessage(gameSession.getSessionName()));
        addStatusMessage(UnoStatusMessages.getPlayerTurnMessage(gameSession.getCurrentPlayer()));
        setCellBorders();
    }

    void handleCardSelection(Button btn, Card card) {
        if (gameSession.getCurrentPlayerIndex() == 0) {
            gameSession.setCurrentPlayerIndex(-1); // Lock input
            boolean played = false;

            gameSession.setCurrentPlayerIndex(0);
            played = gameSession.playCard(card);
            gameSession.setCurrentPlayerIndex(-1);

            if (played) {
                gameSession.setCurrentPlayerIndex(1);
                
                // Animation/UI Removal logic
                Pane parent = (Pane) btn.getParent(); // HBox
                if (parent != null) {
                    parent.getChildren().remove(btn);
                }
                
                updateDiscardPileImage(card.getImagePath());
                gameSession.setCurrentPlayerIndex(0);
                
                if (card instanceof WildCard) {
                    handleWildCardUI(card); // Tách logic Wildcard ra hàm riêng cho gọn
                } else if (card instanceof ActionCard) {
                    handleActionCard(gameSession.getCurrentPlayer(), (ActionCard) card);
                } else {
                    addStatusMessage(UnoStatusMessages.getPlayerPlayCardMessage(gameSession.getCurrentPlayer(), card.getName()));
                }
                
                drawPileButtonClicked = true;
                checkWinCondition();
                
                if (!gameSession.getPlayers().get(0).hasWon()) {
                     invokeBotTurn();
                }
            } else {
                gameSession.setCurrentPlayerIndex(0);
                toaster.warn(WarningConstants.YOU_CANNOT_PLAY_THIS_CARD);
            }
        }
    }
    
    // Tách logic UI Wildcard
    private void handleWildCardUI(Card card) {
        ColorSelectionPopup colorSelectionPopup = new ColorSelectionPopup(this); // Pass stage this
        // Lưu ý: show() trong JavaFX thường non-blocking, nếu ColorSelectionPopup dùng showAndWait thì ok.
        // Giả sử nó là blocking dialog
        colorSelectionPopup.showAndWait(); 
        
        coreapp.model.enums.Color selectedColor = colorSelectionPopup.getSelectedColor();
        ((WildCard) card).setColor(selectedColor);
        gameSession.setColorToPlay(selectedColor);
        
        addStatusMessage(UnoStatusMessages.getWildCardPlayedMessage(gameSession.getCurrentPlayer(), card.getName(), selectedColor));
        handleWildCard(gameSession.getCurrentPlayer(), (WildCard) card);
        
        if (((WildCard) card).getWildType() == WildType.WILD_DRAW_4) {
             skipNextPlayer();
        }
    }

    // Logic kiểm tra thắng thua
    private void checkWinCondition() {
        Player p = gameSession.getPlayers().get(0);
        if (p.getCardCount() == 1) {
            addStatusMessage(UnoStatusMessages.getPlayerCalledUnoMessage(gameSession.getCurrentPlayer()));
        }
        if (p.hasWon()) {
             // Logic thắng game (giữ nguyên logic tính điểm)
             addStatusMessage(UnoStatusMessages.getPlayerWinMessage(p));
             updateUserStatistics(getTotalScoreOfLosers(0));
             this.close();
             // new YouWonWindow(); // Mở cửa sổ thắng
        }
    }

    private void updateUserStatistics(int totalScore) {
        // Logic database giữ nguyên
        var currentUser = CurrentUserManager.getInstance().getCurrentUser();
        try {
            var currentUserStatistic = UserStatisticRepository.getUserStatisticById(currentUser.getId());
            var statistics = UserStatisticRepository.getUserStatistics();
            for (var statistic : statistics) {
                if (statistic.getId().equals(currentUserStatistic.getId())) {
                    statistic.setTotalScore(currentUserStatistic.getTotalScore() + totalScore);
                    statistic.setNumberOfWins(currentUserStatistic.getNumberOfWins() + 1);
                    break;
                }
            }
            UserStatisticRepository.updateUserStatistics(statistics);
        } catch (IOException ex) {
            Logger.log(ex.getMessage(), FileConstants.ERROR_LOGS_FILE_PATH);
        }
    }

    void invokeBotTurn() {
        Player currentPlayer = gameSession.nextPlayer();
        setCellBorders();
        addStatusMessage(UnoStatusMessages.getPlayerTurnMessage(gameSession.getCurrentPlayer()));

        PauseTransition initialTimer = new PauseTransition(Duration.millis(botDelay));
        initialTimer.setOnFinished(e -> {
            if (currentPlayer instanceof Bot bot) {
                // Logic bot giữ nguyên
                Object[] obj = gameSession.playBotTurn(bot);
                Card playedCardByBot = (Card) obj[0];
                gameSession.playCard(playedCardByBot);
                boolean drewCard = (boolean) obj[1];
                int playerIndex = gameSession.getCurrentPlayerIndex();

                if (drewCard) {
                    int drawCount = (int) obj[2];
                    addStatusMessage(UnoStatusMessages.getPlayerDrawCardMessage(currentPlayer, drawCount));
                }
                
                // Handle bot card types
                if (playedCardByBot instanceof WildCard w) {
                    handleWildCard(bot, w);
                } else if (playedCardByBot instanceof ActionCard a) {
                    handleActionCard(bot, a);
                } else {
                    addStatusMessage(UnoStatusMessages.getPlayerPlayCardMessage(currentPlayer, playedCardByBot.getName()));
                }
                
                updateDiscardPileImage(playedCardByBot.getImagePath());
                paintBotCell(mainCells[playerIndex], bot, bot.getUser());

                if (currentPlayer.getCardCount() == 1) {
                    addStatusMessage(UnoStatusMessages.getPlayerCalledUnoMessage(currentPlayer));
                }

                if (currentPlayer.hasWon()) {
                    addStatusMessage(UnoStatusMessages.getPlayerWinMessage(currentPlayer));
                    this.close();
                    // new YouLostWindow(...);
                } else {
                    invokeBotTurn(); // Đệ quy gọi lượt tiếp theo
                }
            } else {
                // Lượt người chơi
                drawPileButtonClicked = false;
            }
            setCellBorders();
            updateCardCountInDrawPile(gameSession.getDrawPileCardCount());
        });
        initialTimer.play();
    }

    // Các logic game logic (drawCards, skipNextPlayer, handleActionCard...) giữ nguyên vì không phụ thuộc UI Swing
    // ... (Giữ nguyên các hàm logic game)
    
    int getTotalScoreOfLosers(int index) {
        var totalScore = 0;
        for (int x = 0; x < gameSession.getPlayers().size(); x++) {
            if (x != index) {
                var p = gameSession.getPlayers().get(x);
                for (var c : p.getHand()) totalScore += c.getScore();
            }
        }
        return totalScore;
    }

    private void handleWildCard(Player player, WildCard wildCard) {
        if (player instanceof Bot bot) {
            coreapp.model.enums.Color selectedColor = bot.chooseRandomColor();
            wildCard.setColor(selectedColor);
            addStatusMessage(UnoStatusMessages.getWildCardPlayedMessage(player, wildCard.getName(), selectedColor));
        }
        if (wildCard.getWildType() == WildType.WILD_DRAW_4) {
            draw4();
            skipNextPlayer();
        }
    }

    private void handleActionCard(Player player, ActionCard actionCard) {
        switch (actionCard.getAction()) {
            case SKIP -> {
                skipNextPlayer();
                addStatusMessage(UnoStatusMessages.getSkipCardPlayedMessage(player, actionCard.getName()));
            }
            case REVERSE -> {
                gameSession.reverseGameDirection();
                addStatusMessage(UnoStatusMessages.getReverseCardPlayedMessage(player, actionCard.getName()));
            }
            case DRAW_2 -> {
                addStatusMessage(UnoStatusMessages.getActionCardPlayedMessage(player, actionCard.getName()));
                draw2();
                skipNextPlayer();
            }
        }
    }

    void drawCards(int count) {
        int currentPlayerIndex = gameSession.getCurrentPlayerIndex();
        int gameDirection = gameSession.getGameDirection();
        int nextPlayerIndex = getNextIndex(currentPlayerIndex, gameDirection, gameSession.getPlayers().size());
        var nextPlayer = gameSession.getPlayers().get(nextPlayerIndex);
        for (int x = 0; x < count; x++) {
            nextPlayer.addCard(gameSession.drawCard());
        }
        addStatusMessage(UnoStatusMessages.getDrawPenaltyMessage(nextPlayer, count));
        
        if (CurrentUserManager.getInstance().getCurrentUser().getId() != nextPlayer.getUser().getId())
            paintBotCell(mainCells[nextPlayerIndex], nextPlayer, nextPlayer.getUser());
        else
            paintUserCell();
    }

    void skipNextPlayer() {
        int currentPlayerIndex = gameSession.getCurrentPlayerIndex();
        int gameDirection = gameSession.getGameDirection();
        int playerCount = gameSession.getPlayers().size();
        int nextPlayerIndex = getNextIndex(currentPlayerIndex, gameDirection, playerCount);
        gameSession.setCurrentPlayerIndex(nextPlayerIndex);
    }

    int getNextIndex(int currentIndex, int direction, int playerCount) {
        int nextIndex = currentIndex + direction;
        if (nextIndex < 0) nextIndex += playerCount;
        return (nextIndex + playerCount) % playerCount;
    }

    void draw2() { drawCards(2); }
    void draw4() { drawCards(4); }

    void addStatusMessage(String message) {
        Platform.runLater(() -> gameStatusArea.appendText(message + "\n"));
        Logger.log(message, FileConstants.GAME_LOGS_FILE_PATH);
    }

    void updateCardCountInDrawPile(int count) {
        Platform.runLater(() -> cardCountInDrawPile.setText(Integer.toString(count)));
    }

    void updateDrawPileImage() {
        String imagePath;
        var cardCount = gameSession.getDrawPileCardCount();
        imagePath = switch (cardCount) {
            case 4 -> ImagePath.DRAW_PILE_IMAGE_4;
            case 3 -> ImagePath.DRAW_PILE_IMAGE_3;
            case 2 -> ImagePath.DRAW_PILE_IMAGE_2;
            default -> cardCount > 4 ? ImagePath.DRAW_PILE_IMAGE_4 : ImagePath.DEFAULT_CARD_IMAGE_PATH;
        };
        Image drawPileIcon = new Image(imagePath);
        ImageView imageView = new ImageView(drawPileIcon);
        imageView.setFitWidth(130);
        imageView.setFitHeight(150);
        drawPileButton.setGraphic(imageView);
    }

    void updateDiscardPileImage(String imagePath) {
        Image discardPileIcon = new Image(imagePath);
        discardPileLabel.setImage(discardPileIcon);
        discardPileLabel.setFitWidth(100); // Điều chỉnh lại tỉ lệ cho đẹp
        discardPileLabel.setFitHeight(150);
    }

    void addCurrentPlayerElements(Pane cellPanel) {
        try {
            cellPanel.getChildren().clear();
            var currentPlayer = gameSession.getPlayers().get(0);
            
            VBox currentPlayerPanel = new VBox();
            currentPlayerPanel.setAlignment(Pos.CENTER);
            // Binding size để panel luôn ở giữa cell
            currentPlayerPanel.prefWidthProperty().bind(cellPanel.widthProperty());
            currentPlayerPanel.prefHeightProperty().bind(cellPanel.heightProperty());

            HBox cardPanel = new HBox();
            cardPanel.setAlignment(Pos.CENTER);
            
            // Xử lý các lá bài chồng lên nhau (Negative spacing thay vì margin thủ công phức tạp)
            // Giá trị spacing sẽ âm để tạo hiệu ứng xếp bài
            double spacing = -40; // Mặc định chồng lên nhau
            if (currentPlayer.getCardCount() > 20) spacing = -50;
            else if (currentPlayer.getCardCount() < 5) spacing = -10;
            
            cardPanel.setSpacing(spacing);

            for (Card card : currentPlayer.getHand()) {
                Image cardImage = new Image(card.getImagePath());
                ImageView imageView = new ImageView(cardImage);
                imageView.setFitWidth(70);
                imageView.setFitHeight(110);
                // Giữ tỷ lệ ảnh
                imageView.setPreserveRatio(true);

                Button cardButton = new Button();
                cardButton.setGraphic(imageView);
                cardButton.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-cursor: hand;");
                
                // Hiệu ứng hover cho bài người chơi
                cardButton.setOnMouseEntered(e -> {
                    cardButton.setTranslateY(-20); // Bài nhảy lên khi trỏ chuột vào
                });
                cardButton.setOnMouseExited(e -> {
                    cardButton.setTranslateY(0);
                });

                cardButton.setOnAction(e -> handleCardSelection(cardButton, card));
                cardPanel.getChildren().add(cardButton);
            }
            
            currentPlayerPanel.getChildren().add(cardPanel);

            // UNO Button
            if (currentPlayer.getCardCount() == 1) {
                Image unoImage = new Image(ImagePath.UNO_BUTTON_IMAGE_PATH);
                ImageView unoImageView = new ImageView(unoImage);
                unoImageView.setFitWidth(80); // Chỉnh lại size
                unoImageView.setPreserveRatio(true);
                
                Button unoButton = new Button();
                unoButton.setGraphic(unoImageView);
                unoButton.setStyle("-fx-background-color: transparent;");
                unoButton.setOnAction(e -> { /* handleUnoAction */ });
                
                VBox.setMargin(unoButton, new Insets(10, 0, 0, 0));
                currentPlayerPanel.getChildren().add(unoButton);
            }

            cellPanel.getChildren().add(currentPlayerPanel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addBotPlayerElements() {
        var players = gameSession.getPlayers();
        // Không cần lưu Dimension nữa vì GridPane tự handle resize
        for (int x = 1; x < mainCells.length; x++) {
            if (x < players.size()) { // Check bounds
                var cellCoordinates = mainCells[x];
                var player = players.get(x);
                paintBotCell(cellCoordinates, player, player.getUser());
            }
        }
    }

    void paintUserCell() {
        // Giả sử người chơi luôn ở cell đầu tiên được định nghĩa
        var playerCell = getCell(mainCells[0][0], mainCells[0][1]);
        addCurrentPlayerElements(playerCell);
    }

    void setCellBorders() {
        // Xóa border cũ và set border mới
        int currentPlayerIndex = gameSession.getCurrentPlayerIndex();
        
        // Loop qua tất cả các cells để reset style, sau đó highlight current
        // Vì logic map từ index -> cell coordinate hơi phức tạp, ta dùng mainCells
        
        for (int i=0; i < mainCells.length; i++) {
             var coords = mainCells[i];
             Pane cell = getCell(coords[0], coords[1]);
             if (i == currentPlayerIndex) {
                 cell.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 4; -fx-background-color: #2c3e50;"); // Green highlight
             } else {
                 cell.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: #2c3e50;");
             }
        }
    }

    void paintBotCell(int[] cellCoordinates, Player player, User user) {
        var cellPanel = getCell(cellCoordinates[0], cellCoordinates[1]);
        cellPanel.getChildren().clear();

        VBox botPanel = new VBox();
        botPanel.setAlignment(Pos.CENTER);
        botPanel.prefWidthProperty().bind(cellPanel.widthProperty());
        botPanel.prefHeightProperty().bind(cellPanel.heightProperty());

        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setFont(Font.font(customFont.getFamily(), 16));
        usernameLabel.setTextFill(Color.WHITE);
        botPanel.getChildren().add(usernameLabel);

        HBox cardPanel = new HBox();
        cardPanel.setAlignment(Pos.CENTER);
        // Spacing âm để bài bot chồng lên nhau gọn gàng
        cardPanel.setSpacing(-20); 

        int cardCount = player.getHand().size();
        // Giới hạn số lượng bài hiển thị để không vỡ layout nếu bot cầm quá nhiều bài
        int maxDisplay = Math.min(cardCount, 10); 

        for (int x = 0; x < maxDisplay; x++) {
            try {
                // ImagePath.DEFAULT_CARD_IMAGE_PATH thay vì gọi static method có thể gây lỗi
                Image cardImage = new Image(ImagePath.DEFAULT_CARD_IMAGE_PATH); 
                ImageView cardLabel = new ImageView(cardImage);
                cardLabel.setFitWidth(40);
                cardLabel.setFitHeight(60);
                cardPanel.getChildren().add(cardLabel);
            } catch (Exception e) {
                // Fallback nếu ảnh lỗi: vẽ hình chữ nhật
                Rectangle rect = new Rectangle(40, 60, Color.DARKRED);
                rect.setStroke(Color.WHITE);
                cardPanel.getChildren().add(rect);
            }
        }
        
        // Nếu cầm nhiều hơn hiển thị, hiện dấu +
        if (cardCount > maxDisplay) {
            Label more = new Label("+" + (cardCount - maxDisplay));
            more.setTextFill(Color.WHITE);
            cardPanel.getChildren().add(more);
        }

        VBox.setMargin(cardPanel, new Insets(10, 0, 10, 0));
        botPanel.getChildren().add(cardPanel);

        Label countLbl = new Label("Cards: " + cardCount);
        countLbl.setFont(Font.font(customFont.getFamily(), 14));
        countLbl.setTextFill(Color.LIGHTGRAY);
        botPanel.getChildren().add(countLbl);

        cellPanel.getChildren().add(botPanel);
    }
    
    // Helper để lấy Pane từ grid
    public Pane getCenterPanel() {
        return getCell(1, 1); // Giả định layout 3x3 và center là 1,1
    }

    public Pane getCell(int row, int column) {
        if (row >= 0 && row < cells.length && column >= 0 && column < cells[0].length) {
            return cells[row][column];
        }
        return new Pane(); // Tránh null pointer
    }

    void leaveGame() {
        this.close();
        // new MainMenu().show();
    }
}