package coreapp.model.cards;

import coreapp.model.enums.Color;
import coreapp.util.constants.ImagePath;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Represents a card in the Uno game.
 */
public abstract class Card {

    private Color color; // The color of the card
    private int value; // The value of the card (for Number Cards)
    private int score; // The score of the card
    private String imagePath; // The path to the image associated with the card

    /**
     * Constructs a Card with the specified color, value, score, and image path.
     *
     * @param color     The color of the card.
     * @param value     The value of the card (for Number Cards). Use -1 for Action
     *                  Cards and Wild Cards.
     * @param score     The score of the card.
     * @param imagePath The path to the image associated with the card.
     */
    public Card(Color color, int value, int score, String imagePath) {
        this.color = color;
        this.value = value;
        this.score = score;
        this.imagePath = imagePath;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public int getValue() {
        return value;
    }

    public int getScore() {
        return score;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getName() {
        if (this instanceof NumberCard) {
            return color.toString() + " " + value;
        } else if (this instanceof ActionCard) {
            return color.toString() + " " + ((ActionCard) this).getAction().toString();
        } else if (this instanceof WildCard) {
            return ((WildCard) this).getWildType().toString();
        } else {
            return "Unknown Card";
        }
    }

    public abstract boolean isPlayableOn(Card otherCard);

    /**
     * Gets the default card image as a JavaFX ImageView with the specified width and height.
     *
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return ImageView containing the card image.
     */
    public static ImageView getDefaultCardImage(int width, int height) {
        Image image = new Image(ImagePath.DEFAULT_CARD_IMAGE_PATH);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        return imageView;
    }
}
