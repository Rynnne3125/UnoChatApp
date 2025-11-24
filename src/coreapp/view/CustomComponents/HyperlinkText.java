package coreapp.view.CustomComponents;

import coreapp.util.constants.UIColors;
import coreapp.util.constants.UIFonts;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * A custom Label component representing a hyperlink-like text in JavaFX.
 */
public class HyperlinkText extends Label {

    /**
     * Creates a new HyperlinkText instance with the specified text, position, and
     * action to perform when clicked.
     *
     * @param hyperlinkText   The text to be displayed as a hyperlink.
     * @param xPos            The x-coordinate of the hyperlink text.
     * @param yPos            The y-coordinate of the hyperlink text.
     * @param hyperlinkAction The action to be performed when the hyperlink is clicked.
     */
    public HyperlinkText(String hyperlinkText, double xPos, double yPos, Runnable hyperlinkAction) {
        super(hyperlinkText);

        // Set font and color
        setFont(Font.font(UIFonts.FONT_FORGOT_PASSWORD.getName(), UIFonts.FONT_FORGOT_PASSWORD.getSize()));
        setTextFill(Color.web(UIColors.COLOR_OUTLINE.toString().substring(2))); // convert Color to hex string

        // Set position
        setLayoutX(xPos);
        setLayoutY(yPos);

        // Set cursor
        setOnMouseEntered(e -> setStyle("-fx-text-fill: " + darkerHex(UIColors.COLOR_OUTLINE) + ";"));
        setOnMouseExited(e -> setTextFill(Color.web(UIColors.COLOR_OUTLINE.toString().substring(2))));
        setOnMouseClicked((MouseEvent e) -> hyperlinkAction.run());
    }

    /**
     * Returns a darker hex string of the given AWT color for hover effect.
     *
     * @param color The AWT color.
     * @return The hex string for JavaFX CSS color.
     */
    private String darkerHex(java.awt.Color color) {
        java.awt.Color darker = color.darker();
        return String.format("#%02x%02x%02x", darker.getRed(), darker.getGreen(), darker.getBlue());
    }
}
