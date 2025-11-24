package coreapp.view.CustomComponents;

import coreapp.util.constants.FontConstants;
import coreapp.util.constants.UITexts;
import coreapp.util.ui.UIUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;

/**
 * A custom Button component with text overlay for use in menu interfaces.
 * Logic is kept similar to Swing version.
 */
public class MenuTextButton extends StackPane {

    private final String buttonText;
    private final int textX;
    private final int textY;
    private final int fontSize;
    private final Font textFont;
    private final Canvas canvas;
    private final ImageView imageView;

    /**
     * Constructor with only icon, width, and height.
     */
    public MenuTextButton(Image icon, int width, int height) {
        this(icon, width, height, UITexts.STRING_EMPTY, 0, 0, 20);
    }

    /**
     * Full constructor with icon, size, text, text position, and font size.
     */
    public MenuTextButton(Image icon, int width, int height, String buttonText, int textX, int textY, int fontSize) {
        this.buttonText = buttonText;
        this.textX = textX;
        this.textY = textY;
        this.fontSize = fontSize;
        this.textFont = UIUtils.loadCustomFont(FontConstants.RechargeFontPath);

        // Image
        imageView = new ImageView(icon);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        // Canvas for text overlay
        canvas = new Canvas(width, height);
        drawText();

        getChildren().addAll(imageView, canvas);

        // Mouse cursor change
        setOnMouseEntered(e -> setStyle("-fx-cursor: hand;"));
        setOnMouseExited(e -> setStyle("-fx-cursor: default;"));
    }

    /**
     * Draws text overlay on the canvas.
     */
    private void drawText() {
        if (buttonText != null && !buttonText.isEmpty()) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFont(textFont);
            gc.setFill(Color.WHITE);
            gc.fillText(buttonText, textX, textY);
        }
    }
}
