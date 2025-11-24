package coreapp.view.CustomComponents;

import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import coreapp.util.constants.UIColors;
import coreapp.util.constants.UIFonts;
import coreapp.util.ui.UIUtils;

/**
 * A custom JavaFX TextField component with rounded corners and custom styling.
 * This class extends TextField to provide enhanced visual appearance and
 * functionality.
 */
public class TextField extends javafx.scene.control.TextField {
    
    /**
     * The color of the border.
     */
    private Color borderColor = UIColors.COLOR_INTERACTIVE_FX;
    
    /**
     * Constructs a new TextField with default settings.
     */
    public TextField() {
        super();
        initializeStyle();
    }
    
    /**
     * Constructs a new TextField with initial text.
     * 
     * @param text The initial text for the text field.
     */
    public TextField(String text) {
        super(text);
        initializeStyle();
    }
    
    /**
     * Initializes the styling for the text field.
     */
    private void initializeStyle() {
        // Set font
        setFont(Font.font(UIFonts.FONT_GENERAL_UI.getFamily(), UIFonts.FONT_GENERAL_UI.getSize()));
        
        // Set padding (equivalent to margin in Swing)
        setPadding(new Insets(5, 10, 5, 10));
        
        // Apply initial style
        updateStyle();
        
        // Remove default focus indicator
        setFocusTraversable(true);
    }
    
    /**
     * Generates the CSS style string based on current properties.
     * 
     * @return The CSS style string.
     */
    private String generateStyle() {
        return String.format(
            "-fx-background-color: %s;" +
            "-fx-text-fill: %s;" +
            "-fx-background-radius: %d;" +
            "-fx-border-radius: %d;" +
            "-fx-border-color: %s;" +
            "-fx-border-width: 1;" +
            "-fx-prompt-text-fill: %s;" +
            "-fx-highlight-fill: %s;" +
            "-fx-highlight-text-fill: white;",
            toHex(UIColors.COLOR_BACKGROUND_FX),
            toHex(UIColors.OFFBLACK_FX),
            UIUtils.ROUNDNESS,
            UIUtils.ROUNDNESS,
            toHex(borderColor),
            toHex(UIColors.COLOR_OUTLINE_FX),
            toHex(UIColors.COLOR_INTERACTIVE_FX)
        );
    }
    
    /**
     * Updates the style of the text field.
     */
    private void updateStyle() {
        setStyle(generateStyle());
    }
    
    /**
     * Converts Color to hex string (#RRGGBB).
     * 
     * @param color The color to convert.
     * @return The hex string representation of the color.
     */
    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
    
    /**
     * Sets the color of the border.
     * 
     * @param color The color to set for the border.
     */
    public void setBorderColor(Color color) {
        this.borderColor = color;
        updateStyle();
    }
    
    /**
     * Gets the current border color.
     * 
     * @return The current border color.
     */
    public Color getBorderColor() {
        return borderColor;
    }
}