package coreapp.util.ui;

import java.awt.Color;
import java.io.File;
import java.net.URL;

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
            URL url = UIUtils.class.getResource(path);
            if (url == null) {
                System.err.println("Font not found: " + path);
                return null;
            }

            Font font = Font.loadFont(url.toExternalForm(), 20);
            return font;
        } catch (Exception e) {
            System.err.println("Error loading font: " + e.getMessage());
            return null;
        }
    }

}
