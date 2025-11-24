package coreapp.view.CustomComponents;

import coreapp.util.constants.UIColors;
import coreapp.util.constants.UIFonts;
import coreapp.util.ui.UIUtils;
import javafx.geometry.Insets;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * A custom JavaFX PasswordField component with rounded corners and custom styling.
 * This class extends PasswordField to provide enhanced visual appearance with
 * support for masking/unmasking text.
 */
public class TextFieldPassword extends PasswordField {
    
    /**
     * Border color of the text field.
     */
    private Color borderColor = UIColors.COLOR_OUTLINE_FX;
    
    /**
     * Flag to control whether text should be masked.
     */
    private boolean maskText = true;
    
    /**
     * Constructs a custom TextFieldPassword with default styling.
     */
    public TextFieldPassword() {
        super();
        initializeStyle();
    }
    
    /**
     * Initializes the styling for the password field.
     */
    private void initializeStyle() {
        setFont(Font.font(UIFonts.FONT_GENERAL_UI.getFamily(), UIFonts.FONT_GENERAL_UI.getSize()));
        setPadding(new Insets(5, 10, 5, 10));
        
        // Remove default focus effect
        setStyle(generateStyle());
        
        setFocusTraversable(true);
    }
    
    /**
     * Generates the CSS style string based on current borderColor and other properties.
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
     * Convert JavaFX Color to Hex (#RRGGBB).
     * 
     * @param c The color to convert.
     * @return The hex string representation of the color.
     */
    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
            (int) (c.getRed() * 255),
            (int) (c.getGreen() * 255),
            (int) (c.getBlue() * 255));
    }
    
    /**
     * Updates the border color and refreshes style.
     * 
     * @param color new border color
     */
    public void setBorderColor(Color color) {
        this.borderColor = color;
        setStyle(generateStyle());
    }
    
    /**
     * Gets the current border color.
     * 
     * @return The current border color.
     */
    public Color getBorderColor() {
        return borderColor;
    }
    
    /**
     * Sets whether the text should be masked (shown as password characters).
     * Note: JavaFX PasswordField always masks by default. This method is provided
     * for API compatibility but has limited functionality.
     * 
     * @param mask true to mask text, false to show plain text
     */
    public void setMaskText(boolean mask) {
        this.maskText = mask;
        // Note: PasswordField in JavaFX doesn't support dynamic masking/unmasking
        // If you need this functionality, consider using a TextField that can switch
        // between password and text mode, or use a custom control
    }
    
    /**
     * Gets whether text masking is enabled.
     * 
     * @return true if text is masked, false otherwise
     */
    public boolean isMaskText() {
        return maskText;
    }
    
    /**
     * Sets the echo character (for API compatibility).
     * Note: JavaFX PasswordField uses a fixed bullet character and doesn't support
     * custom echo characters directly.
     * 
     * @param c The character to use for masking (ignored in JavaFX)
     */
    public void setEchoChar(char c) {
        // JavaFX PasswordField doesn't support custom echo characters
        // This method is kept for API compatibility but doesn't do anything
    }
    
    /**
     * Sets the text fill color.
     * 
     * @param color The color for the text.
     */
    public void setTextFill(Color color) {
        setStyle(getStyle() + String.format("-fx-text-fill: %s;", toHex(color)));
    }
}