package coreapp.util.ui;

import java.awt.Color;
import java.io.File;

import coreapp.util.constants.FileConstants;
import coreapp.util.helpers.Logger;
import javafx.scene.text.Font;

/**
 * Utility methods for UI-related tasks (JavaFX version).
 */
public class UIUtils {

    /**
     * The roundness value used for drawing rounded shapes.
     */
    public static final int ROUNDNESS = 8;

    /**
     * Converts java.awt.Color to hex string for JavaFX CSS.
     * Example: Color(255,0,0) -> "FF0000"
     *
     * @param color The AWT color.
     * @return Hex string without '#'.
     */
    public static String toHex(Color color) {
        return String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Loads a custom font for JavaFX from a file path.
     *
     * @param path The path to the font file.
     * @return JavaFX Font instance.
     */
    public static Font loadCustomFont(String path) {
        try {
            File fontFile = new File(path);
            return Font.loadFont(fontFile.toURI().toString(), 14);
        } catch (Exception e) {
            Logger.log(e.getMessage(), FileConstants.ERROR_LOGS_FILE_PATH);
            return Font.getDefault();
        }
    }
}
