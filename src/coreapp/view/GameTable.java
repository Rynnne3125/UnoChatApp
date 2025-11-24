package coreapp.view;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
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
import coreapp.util.constants.FileConstants;
import coreapp.util.constants.FontConstants;
import coreapp.util.constants.ImagePath;
import coreapp.util.constants.UnoStatusMessages;
import coreapp.util.constants.WarningConstants;
import coreapp.util.constants.WindowConstants;
import coreapp.util.helpers.Logger;
import coreapp.util.session.CurrentUserManager;
import coreapp.util.ui.GameTableLayoutHelper;
import coreapp.util.ui.UIUtils;
import coreapp.util.ui.toaster.Toaster;
import coreapp.view.Popups.ColorSelectionPopup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the main game table where the Uno game is played.
 */
public class GameTable extends BaseFrame {

    /**
     * Toaster object for displaying notifications.
     */
    private Toaster toaster;

    /**
     * GameSession object representing the current game session.
     */
    private GameSession gameSession;

    /**
     * 2D array of Panes representing the cells of the game table.
     */
    private Pane[][] cells;

    /**
     * 2D array representing the main cells of the game table.
     */
    private int[][] mainCells;

    /**
     * Number of players in the game.
     */
    private int numberOfPlayers;

    /**
     * ImageView representing the discard pile in the game table.
     */
    private ImageView discardPileLabel;

    /**
     * Button representing the draw pile in the game table.
     */
    private Button drawPileButton;

    /**
     * Label representing the card count in the draw pile.
     */
    private Label cardCountInDrawPile;

    /**
     * Font object for custom fonts in the game table.
     */
    private final Font customFont = Font.loadFont(getClass().getResourceAsStream(FontConstants.RechargeFontPath), 12);

    /**
     * Boolean indicating whether the draw pile button has been clicked.
     */
    private boolean drawPileButtonClicked = false;

    /**
     * TextArea representing the area for displaying game status.
     */
    private TextArea gameStatusArea;

    /**
     * Delay time for bot actions in milliseconds.
     */
    private int botDelay = 3000;

    /**
     * Constructs a new GameTable object with the specified number of players and
     * game session name.
     *
     * @param numberOfPlayers The number of players in the game.
     * @param gameSessionName The name of the game session.
     */
    public GameTable(int numberOfPlayers, String gameSessionName) {
        super(WindowConstants.GAME_TABLE_WINDOW + " : " + gameSessionName);
        this.numberOfPlayers = numberOfPlayers;
        gameSession = new GameSession(numberOfPlayers, gameSessionName);
        gameSession.initializeGameSession();
        gameSession.setSessionName(gameSessionName);
        initializeFrame();
        mainCells = GameTableLayoutHelper.getPlayerCells(numberOfPlayers);
        addBotPlayerElements();
        paintUserCell();
        addCenterElements();
        paintUserCell();
    }

    /**
     * Adds center elements to the game table interface.
     */
    void addCenterElements() {
        var centerPanel = getCenterPanel();
        centerPanel.setPadding(Insets.EMPTY);

        gameStatusArea = new TextArea();
        gameStatusArea.setEditable(false);
        gameStatusArea.setWrapText(true);
        gameStatusArea.setStyle("-fx-control-inner-background: transparent; -fx-background-color: transparent;");
        gameStatusArea.setPrefSize(295, 255);

        ScrollPane scrollPane = new ScrollPane(gameStatusArea);
        scrollPane.setLayoutX(10);
        scrollPane.setLayoutY(70);
        scrollPane.setPrefSize(295, 255);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        gameStatusArea.textProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                gameStatusArea.setScrollTop(Double.MAX_VALUE);
            });
        });

        centerPanel.getChildren().add(scrollPane);

        Separator verticalLine = new Separator();
        verticalLine.setOrientation(javafx.geometry.Orientation.VERTICAL);
        verticalLine.setLayoutX(375);
        verticalLine.setPrefHeight(centerPanel.getPrefHeight());
        verticalLine.setStyle("-fx-background-color: black;");
        centerPanel.getChildren().add(verticalLine);

        drawPileButton = new Button();
        drawPileButton.setLayoutX(425);
        drawPileButton.setLayoutY(90);
        drawPileButton.setPrefSize(130, 150);

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

        discardPileLabel = new ImageView();
        discardPileLabel.setLayoutX(565);
        discardPileLabel.setLayoutY(90);
        discardPileLabel.setFitWidth(103);
        discardPileLabel.setFitHeight(150);
        discardPileLabel.setStyle("-fx-border-color: black;");
        centerPanel.getChildren().add(discardPileLabel);

        Image icon = new Image(ImagePath.BACK_ICON);
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        Button leaveGameBtn = new Button();
        leaveGameBtn.setGraphic(imageView);
        leaveGameBtn.setOnAction(e -> leaveGame());
        leaveGameBtn.setPrefSize(50, 50);
        leaveGameBtn.setLayoutX(5);
        leaveGameBtn.setLayoutY(5);
        leaveGameBtn.setStyle("-fx-background-color: transparent;");
        centerPanel.getChildren().add(leaveGameBtn);

        cardCountInDrawPile = new Label();
        cardCountInDrawPile.setFont(Font.font(customFont.getFamily(), 22));
        cardCountInDrawPile.setTextFill(Color.WHITE);
        cardCountInDrawPile.setLayoutX(465);
        cardCountInDrawPile.setLayoutY(245);
        centerPanel.getChildren().add(cardCountInDrawPile);

        updateCardCountInDrawPile(gameSession.getDrawPileCardCount());
        updateDrawPileImage();

        addStatusMessage(UnoStatusMessages.getGameStartMessage(gameSession.getSessionName()));
        addStatusMessage(UnoStatusMessages.getPlayerTurnMessage(gameSession.getCurrentPlayer()));
        setCellBorders();
    }

    /**
     * Handles the selection of a card by a player.
     *
     * @param btn  The button representing the selected card.
     * @param card The card that was selected.
     */
    void handleCardSelection(Button btn, Card card) {
        if (gameSession.getCurrentPlayerIndex() == 0) {
            gameSession.setCurrentPlayerIndex(-1);
            var played = false;

            gameSession.setCurrentPlayerIndex(0);
            played = gameSession.playCard(card);
            gameSession.setCurrentPlayerIndex(-1);
            if (played) {
                gameSession.setCurrentPlayerIndex(1);
                Pane parent = (Pane) btn.getParent();
                if (parent != null) {
                    parent.getChildren().remove(btn);
                }
                updateDiscardPileImage(card.getImagePath());
                gameSession.setCurrentPlayerIndex(0);
                if (card instanceof WildCard) {
                    ColorSelectionPopup colorSelectionPopup = new ColorSelectionPopup(this);
                    colorSelectionPopup.show();
                    coreapp.model.enums.Color selectedColor = colorSelectionPopup.getSelectedColor();
                    ((WildCard) card).setColor(selectedColor);
                    gameSession.setColorToPlay(selectedColor);
                    gameSession.setCurrentPlayerIndex(0);
                    addStatusMessage(UnoStatusMessages.getWildCardPlayedMessage(gameSession.getCurrentPlayer(),
                            card.getName(), selectedColor));
                    handleWildCard(gameSession.getCurrentPlayer(), (WildCard) card);
                    if (((WildCard) card).getWildType() == WildType.WILD_DRAW_4) {
                        gameSession.setCurrentPlayerIndex(0);
                        skipNextPlayer();
                    } else {
                        gameSession.setCurrentPlayerIndex(0);
                    }
                } else if (card instanceof ActionCard) {
                    handleActionCard(gameSession.getCurrentPlayer(), (ActionCard) card);
                } else {
                    addStatusMessage(
                            UnoStatusMessages.getPlayerPlayCardMessage(gameSession.getCurrentPlayer(), card.getName()));
                }
                drawPileButtonClicked = true;
                if (gameSession.getPlayers().get(0).getCardCount() == 1) {
                    addStatusMessage(UnoStatusMessages.getPlayerCalledUnoMessage(gameSession.getCurrentPlayer()));
                }

                if (gameSession.getPlayers().get(0).hasWon()) {
                    addStatusMessage(UnoStatusMessages.getPlayerWinMessage(gameSession.getPlayers().get(0)));
                    var totalScore = getTotalScoreOfLosers(0);

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

                    close();

                    new YouWonWindow();
                } else {
                    invokeBotTurn();
                }
            } else {
                gameSession.setCurrentPlayerIndex(0);
                toaster.warn(WarningConstants.YOU_CANNOT_PLAY_THIS_CARD);
            }
        }
    }

    /**
     * Initiates a bot player's turn.
     */
    void invokeBotTurn() {
        Player currentPlayer = gameSession.nextPlayer();
        setCellBorders();
        addStatusMessage(UnoStatusMessages.getPlayerTurnMessage(gameSession.getCurrentPlayer()));

        PauseTransition initialTimer = new PauseTransition(Duration.millis(botDelay));
        initialTimer.setOnFinished(e -> {
            if (currentPlayer instanceof Bot bot) {
                var obj = gameSession.playBotTurn(bot);
                var playedCardByBot = (Card) obj[0];
                gameSession.playCard(playedCardByBot);
                var drewCard = (boolean) obj[1];
                var playerIndex = gameSession.getCurrentPlayerIndex();

                if (drewCard) {
                    var drawCount = (int) obj[2];
                    addStatusMessage(UnoStatusMessages.getPlayerDrawCardMessage(currentPlayer, drawCount));
                }
                if (playedCardByBot instanceof WildCard w) {
                    handleWildCard(bot, w);
                } else if (playedCardByBot instanceof ActionCard a) {
                    handleActionCard(bot, a);
                } else {
                    addStatusMessage(
                            UnoStatusMessages.getPlayerPlayCardMessage(currentPlayer, playedCardByBot.getName()));
                }
                updateDiscardPileImage(playedCardByBot.getImagePath());
                paintBotCell(mainCells[playerIndex], bot, bot.getUser());

                if (currentPlayer.getCardCount() == 1) {
                    addStatusMessage(UnoStatusMessages.getPlayerCalledUnoMessage(currentPlayer));
                }

                if (currentPlayer.hasWon()) {
                    addStatusMessage(UnoStatusMessages.getPlayerWinMessage(currentPlayer));

                    close();

                    new YouLostWindow(UnoStatusMessages.getPlayerRoundWinMessage(currentPlayer));
                } else {
                    invokeBotTurn();
                }
            } else {
                drawPileButtonClicked = false;
            }
            setCellBorders();
            updateCardCountInDrawPile(gameSession.getDrawPileCardCount());
        });
        initialTimer.play();
    }

    /**
     * Calculates the total score of losing players.
     *
     * @param index The index of the current player.
     * @return The total score of losing players.
     */
    int getTotalScoreOfLosers(int index) {
        var totalScore = 0;
        for (int x = 0; x < gameSession.getPlayers().size(); x++) {
            if (x != index) {
                var p = gameSession.getPlayers().get(x);
                var cards = p.getHand();
                for (var c : cards) {
                    totalScore += c.getScore();
                }
            }
        }
        return totalScore;
    }

    /**
     * Handles the effect of playing a Wild Card.
     *
     * @param player   The player who played the Wild Card.
     * @param wildCard The Wild Card that was played.
     */
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

    /**
     * Handles the effect of playing an Action Card.
     *
     * @param player     The player who played the Action Card.
     * @param actionCard The Action Card that was played.
     */
    private void handleActionCard(Player player, ActionCard actionCard) {
        switch (actionCard.getAction()) {
            case SKIP:
                skipNextPlayer();
                addStatusMessage(UnoStatusMessages.getSkipCardPlayedMessage(player, actionCard.getName()));
                break;
            case REVERSE:
                gameSession.reverseGameDirection();
                addStatusMessage(UnoStatusMessages.getReverseCardPlayedMessage(player, actionCard.getName()));
                break;
            case DRAW_2:
                addStatusMessage(UnoStatusMessages.getActionCardPlayedMessage(player, actionCard.getName()));
                draw2();
                skipNextPlayer();
            default:
                break;
        }
    }

    /**
     * Draws a specified number of cards from the draw pile.
     *
     * @param count The number of cards to draw.
     */
    void drawCards(int count) {
        int currentPlayerIndex = gameSession.getCurrentPlayerIndex();
        int gameDirection = gameSession.getGameDirection();
        int nextPlayerIndex = getNextIndex(currentPlayerIndex, gameDirection, gameSession.getPlayers().size());
        var nextPlayer = gameSession.getPlayers().get(nextPlayerIndex);
        for (int x = 0; x < count; x++) {
            var card = gameSession.drawCard();
            nextPlayer.addCard(card);
        }
        addStatusMessage(UnoStatusMessages.getDrawPenaltyMessage(nextPlayer, count));
        if (CurrentUserManager.getInstance().getCurrentUser().getId() != nextPlayer.getUser().getId())
            paintBotCell(mainCells[nextPlayerIndex], nextPlayer, nextPlayer.getUser());
        else
            paintUserCell();
    }

    /**
     * Skips the turn of the next player.
     */
    void skipNextPlayer() {
        int currentPlayerIndex = gameSession.getCurrentPlayerIndex();
        int gameDirection = gameSession.getGameDirection();
        int playerCount = gameSession.getPlayers().size();
        int nextPlayerIndex = getNextIndex(currentPlayerIndex, gameDirection, playerCount);
        gameSession.setCurrentPlayerIndex(nextPlayerIndex);
    }

    /**
     * Gets the index of the next player in the game session.
     *
     * @param currentIndex The index of the current player.
     * @param direction    The direction of the game (forward or backward).
     * @param playerCount  The total number of players in the game.
     * @return The index of the next player.
     */
    int getNextIndex(int currentIndex, int direction, int playerCount) {
        int nextIndex = currentIndex + direction;
        if (nextIndex < 0) {
            nextIndex += playerCount;
        }
        return (nextIndex + playerCount) % playerCount;
    }

    /**
     * Initiates drawing two cards from the draw pile.
     */
    void draw2() {
        drawCards(2);
    }

    /**
     * Initiates drawing four cards from the draw pile.
     */
    void draw4() {
        drawCards(4);
    }

    /**
     * Adds a status message to the game status area.
     *
     * @param message The status message to add.
     */
    void addStatusMessage(String message) {
        Platform.runLater(() -> {
            gameStatusArea.appendText(message + "\n");
        });

        Logger.log(message, FileConstants.GAME_LOGS_FILE_PATH);
    }

    /**
     * Updates the displayed count of cards in the draw pile.
     *
     * @param count The new count of cards in the draw pile.
     */
    void updateCardCountInDrawPile(int count) {
        cardCountInDrawPile.setText(Integer.toString(count));
    }

    /**
     * Updates the image of the draw pile button based on the number of cards in the
     * draw pile.
     */
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

    /**
     * Updates the image of the discard pile based on the image path of the played
     * card.
     *
     * @param imagePath The image path of the played card.
     */
    void updateDiscardPileImage(String imagePath) {
        Image discardPileIcon = new Image(imagePath);
        discardPileLabel.setImage(discardPileIcon);
        discardPileLabel.setFitWidth(100);
        discardPileLabel.setFitHeight(150);
    }

    /**
     * Adds elements representing the current player to the specified cell panel.
     *
     * @param cellPanel The panel representing the cell for the current player.
     */
    void addCurrentPlayerElements(Pane cellPanel) {
        try {
            cellPanel.getChildren().clear();
            var currentPlayer = gameSession.getPlayers().get(0);
            VBox currentPlayerPanel = new VBox();
            currentPlayerPanel.setAlignment(Pos.CENTER);
            currentPlayerPanel.setStyle("-fx-background-color: transparent;");

            HBox cardPanel = new HBox();
            cardPanel.setAlignment(Pos.CENTER);
            cardPanel.setStyle("-fx-background-color: transparent;");

            int marginLeft = 0;
            if (currentPlayer.getCardCount() >= 20) {
                marginLeft = currentPlayer.getCardCount() * 33;
            } else if (currentPlayer.getCardCount() >= 14) {
                marginLeft = currentPlayer.getCardCount() * 24;
            } else if (currentPlayer.getCardCount() >= 10) {
                marginLeft = currentPlayer.getCardCount() * 21;
            } else if (currentPlayer.getCardCount() >= 6) {
                marginLeft = currentPlayer.getCardCount() * 17;
            }
            cardPanel.setPadding(new Insets(15, 0, 0, marginLeft));

            var cardWidth = 70;
            var cardHeight = 110;
            boolean isFirstCard = true;
            for (Card card : currentPlayer.getHand()) {
                Image cardImage = new Image(card.getImagePath());
                ImageView imageView = new ImageView(cardImage);
                imageView.setFitWidth(cardWidth);
                imageView.setFitHeight(cardHeight);

                Button cardButton = new Button();
                cardButton.setGraphic(imageView);
                cardButton.setStyle("-fx-background-color: transparent;");
                cardButton.setAlignment(Pos.CENTER);

                int marginToLeft = -(int) (currentPlayer.getCardCount() * 1.4);
                if (isFirstCard) {
                    marginToLeft = 0;
                    isFirstCard = false;
                }

                HBox.setMargin(cardButton, new Insets(0, marginToLeft, 0, 0));
                cardButton.setOnAction(e -> handleCardSelection(cardButton, card));
                cardPanel.getChildren().add(cardButton);
            }
            currentPlayerPanel.getChildren().add(cardPanel);

            // Add UNO button with image
            Image unoImage = new Image(ImagePath.UNO_BUTTON_IMAGE_PATH);
            ImageView unoImageView = new ImageView(unoImage);
            Button unoButton = new Button();
            unoButton.setGraphic(unoImageView);
            unoButton.setStyle("-fx-background-color: transparent;");
            if (currentPlayer.getCardCount() == 1) {
                unoButton.setVisible(true);
            } else {
                unoButton.setVisible(false);
            }
            unoButton.setOnAction(e -> {
                // handleUnoAction();
            });

            VBox.setMargin(unoButton, new Insets(10, 0, 0, 0));
            currentPlayerPanel.getChildren().add(unoButton);

            cellPanel.getChildren().add(currentPlayerPanel);
        } catch (Exception e) {
            Logger.log(e.getMessage(), FileConstants.ERROR_LOGS_FILE_PATH);
        }
    }

    /**
     * Adds elements representing bot players to the game table interface.
     */
    void addBotPlayerElements() {
        var players = gameSession.getPlayers();
        List<Dimension> initialCellSizes = new ArrayList<>();
        for (int i = 0; i < mainCells.length; i++) {
            var cellCoordinates = mainCells[i];
            var cellPanel = getCell(cellCoordinates[0], cellCoordinates[1]);
            initialCellSizes.add(new Dimension((int) cellPanel.getPrefWidth(), (int) cellPanel.getPrefHeight()));
        }
        for (int x = 1; x < mainCells.length; x++) {
            var cellCoordinates = mainCells[x];
            var player = players.get(x);
            var user = player.getUser();
            paintBotCell(cellCoordinates, player, user);
        }
        for (int i = 0; i < mainCells.length; i++) {
            var cellCoordinates = mainCells[i];
            var cellPanel = getCell(cellCoordinates[0], cellCoordinates[1]);
            Dimension size = initialCellSizes.get(i);
            cellPanel.setPrefSize(size.width, size.height);
        }
    }

    /**
     * Paints the cell representing the current user's player information.
     */
    void paintUserCell() {
        var playerCell = getCell(mainCells[0][0], mainCells[0][1]);
        addCurrentPlayerElements(playerCell);
    }

    /**
     * Sets borders for the cells representing players in the game session.
     */
    void setCellBorders() {
        var currentPlayerIndex = gameSession.getCurrentPlayerIndex();
        var currentPlayerCellCoordinates = mainCells[currentPlayerIndex];
        var currentPlayerCellPanel = getCell(currentPlayerCellCoordinates[0], currentPlayerCellCoordinates[1]);
        currentPlayerCellPanel.setStyle("-fx-border-color: green; -fx-border-width: 3;");

        for (var cellCoordinates : mainCells) {
            var cellPanel = getCell(cellCoordinates[0], cellCoordinates[1]);
            if (cellCoordinates != currentPlayerCellCoordinates) {
                cellPanel.setStyle("-fx-border-color: black; -fx-border-width: 1;");
            }
        }
    }

    /**
     * Paints the cell representing bot player information.
     *
     * @param cellCoordinates The coordinates of the cell.
     * @param player          The bot player to paint.
     * @param user            The user associated with the bot player.
     */
    void paintBotCell(int[] cellCoordinates, Player player, User user) {
        var cellPanel = getCell(cellCoordinates[0], cellCoordinates[1]);
        cellPanel.getChildren().clear();

        VBox botCardPanel = new VBox();
        botCardPanel.setAlignment(Pos.CENTER);
        botCardPanel.setStyle("-fx-background-color: transparent;");

        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setFont(Font.font(customFont.getFamily(), 20));
        usernameLabel.setTextFill(Color.WHITE);
        usernameLabel.setAlignment(Pos.CENTER);
        VBox.setMargin(usernameLabel, new Insets(25, 0, 25, 0));
        botCardPanel.getChildren().add(usernameLabel);

        HBox cardPanel = new HBox();
        cardPanel.setAlignment(Pos.CENTER);
        cardPanel.setStyle("-fx-background-color: transparent;");

        boolean isFirstCard = true;
        for (int x = 0; x < player.getHand().size(); x++) {
            Image cardImage = Card.getDefaultCardImage(35, 60);
            ImageView cardLabel = new ImageView(cardImage);

            if (!isFirstCard) {
                var margin = -(int) (player.getHand().size() * 2.5);
                if (player.getHand().size() >= 13) {
                    margin = -25;
                }
                cardPanel.getChildren().add(createSpacer(margin));
            } else {
                isFirstCard = false;
            }

            cardPanel.getChildren().add(cardLabel);
        }

        botCardPanel.getChildren().add(cardPanel);

        Label lbl = new Label(Integer.toString(player.getHand().size()));
        lbl.setFont(Font.font(customFont.getFamily(), 20));
        lbl.setTextFill(Color.WHITE);
        lbl.setAlignment(Pos.CENTER);
        VBox.setMargin(lbl, new Insets(10, 0, 0, 0));
        botCardPanel.getChildren().add(lbl);

        cellPanel.getChildren().add(botCardPanel);
    }

    private Region createSpacer(int width) {
        Region spacer = new Region();
        spacer.setPrefWidth(width);
        return spacer;
    }

    /**
     * Initializes the frame of the game table.
     */
    @Override
    void initializeFrame() {
        int rows = GameTableLayoutHelper.rows;
        int columns = GameTableLayoutHelper.columns;
        GridPane mainPanel = new GridPane();
        toaster = new Toaster(mainPanel);
        cells = new Pane[rows][columns];
        GridBagConstraints[][] gbcArray = GameTableLayoutHelper.generateLayout(numberOfPlayers);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                cells[i][j] = new Pane();
                cells[i][j].setStyle("-fx-border-color: black;");
                GridBagConstraints gbc = gbcArray[i][j];

                // Convert GridBagConstraints to GridPane constraints
                GridPane.setRowIndex(cells[i][j], i);
                GridPane.setColumnIndex(cells[i][j], j);
                if (gbc.gridwidth > 1) {
                    GridPane.setColumnSpan(cells[i][j], gbc.gridwidth);
                }
                if (gbc.gridheight > 1) {
                    GridPane.setRowSpan(cells[i][j], gbc.gridheight);
                }
                GridPane.setHgrow(cells[i][j], gbc.weightx > 0 ? Priority.ALWAYS : Priority.NEVER);
                GridPane.setVgrow(cells[i][j], gbc.weighty > 0 ? Priority.ALWAYS : Priority.NEVER);

                mainPanel.getChildren().add(cells[i][j]);
            }
        }

        setScene(new Scene(mainPanel));
        show();
    }

    /**
     * Gets the center panel of the game table interface.
     *
     * @return The center panel of the game table.
     */
    public Pane getCenterPanel() {
        return getCell(1, 1);
    }

    /**
     * Gets the cell panel at the specified row and column indices.
     *
     * @param row    The row index of the cell.
     * @param column The column index of the cell.
     * @return The cell panel at the specified indices.
     */
    public Pane getCell(int row, int column) {
        return cells[row][column];
    }

    /**
     * Leaves the current game session and returns to the main menu.
     */
    void leaveGame() {
        close();
        new MainMenu();
    }

    // Inner class for Dimension since JavaFX doesn't have it
    private static class Dimension {
        public int width;
        public int height;

        public Dimension(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}