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
 */
public class TextFieldPassword extends PasswordField {

    /**
     * Border color of the text field.
     */
    private Color borderColor = (Color) UIColors.COLOR_OUTLINE_FX; // <-- bạn cần thêm mã màu FX trong UIColors

    /**
     * Constructs a custom TextFieldPassword with default styling.
     */
    public TextFieldPassword() {
        super();

        setFont(new Font(UIFonts.FONT_GENERAL_UI.getFontName(), UIFonts.FONT_GENERAL_UI.getSize()));
        setPadding(new Insets(5, 10, 5, 10));

        // Bỏ hiệu ứng focus mặc định
        setStyle(generateStyle());
    }

    /**
     * Generates the CSS style string based on current borderColor.
     */
    private String generateStyle() {
        return String.format(
                "-fx-background-color: %s;" +
                        "-fx-text-fill: %s;" +
                        "-fx-background-radius: %d;" +
                        "-fx-border-radius: %d;" +
                        "-fx-border-color: %s;" +
                        "-fx-border-width: 1;",
                UIColors.COLOR_BACKGROUND_HEX,
                UIColors.OFFBLACK_HEX,
                UIUtils.ROUNDNESS,
                UIUtils.ROUNDNESS,
                toHex(borderColor)
        );
    }

    /**
     * Convert JavaFX Color to Hex (#RRGGBB).
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

    public void setEchoChar(char c) {
    }
}
