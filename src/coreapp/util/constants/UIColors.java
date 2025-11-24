package coreapp.util.constants;

import java.awt.Color;

/**
 * Constants for UI colors used throughout the application.
 * Provides both Swing (AWT Color) and JavaFX (javafx.scene.paint.Color) versions.
 */
public class UIColors {
    
    /* ========================================================================
       SWING COLORS (java.awt.Color)
       ======================================================================== */
    
    /** Outline color (Swing) - RGB(103, 112, 120). */
    public static final Color COLOR_OUTLINE = new Color(103, 112, 120);
    
    /** Background color (Swing) - RGB(232, 232, 232). */
    public static final Color COLOR_BACKGROUND = new Color(232, 232, 232);
    
    /** Interactive color (Swing) - RGB(108, 216, 158). */
    public static final Color COLOR_INTERACTIVE = new Color(108, 216, 158);
    
    /** Darker interactive color (Swing) - RGB(87, 171, 127). */
    public static final Color COLOR_INTERACTIVE_DARKER = new Color(87, 171, 127);
    
    /** Off-white color (Swing) - RGB(229, 229, 229). */
    public static final Color OFFWHITE = new Color(229, 229, 229);
    
    /** Off-black color (Swing) - RGB(26, 26, 26). */
    public static final Color OFFBLACK = new Color(26, 26, 26);
    
    /* ========================================================================
       HEX STRING CONSTANTS (for CSS styling)
       ======================================================================== */
    
    /** Outline color hex - #677078. */
    public static final String COLOR_OUTLINE_HEX = "#677078";
    
    /** Background color hex - #E8E8E8. */
    public static final String COLOR_BACKGROUND_HEX = "#E8E8E8";
    
    /** Interactive color hex - #6CD89E. */
    public static final String COLOR_INTERACTIVE_HEX = "#6CD89E";
    
    /** Darker interactive color hex - #57AB7F. */
    public static final String COLOR_INTERACTIVE_DARKER_HEX = "#57AB7F";
    
    /** Off-white color hex - #E5E5E5. */
    public static final String OFFWHITE_HEX = "#E5E5E5";
    
    /** Off-black color hex - #1A1A1A. */
    public static final String OFFBLACK_HEX = "#1A1A1A";
    
    /* ========================================================================
       JAVAFX COLORS (javafx.scene.paint.Color)
       ======================================================================== */
    
    /** Outline color (JavaFX) - #677078. */
    public static final javafx.scene.paint.Color COLOR_OUTLINE_FX = 
            javafx.scene.paint.Color.web(COLOR_OUTLINE_HEX);
    
    /** Background color (JavaFX) - #E8E8E8. */
    public static final javafx.scene.paint.Color COLOR_BACKGROUND_FX = 
            javafx.scene.paint.Color.web(COLOR_BACKGROUND_HEX);
    
    /** Interactive color (JavaFX) - #6CD89E. */
    public static final javafx.scene.paint.Color COLOR_INTERACTIVE_FX = 
            javafx.scene.paint.Color.web(COLOR_INTERACTIVE_HEX);
    
    /** Darker interactive color (JavaFX) - #57AB7F. */
    public static final javafx.scene.paint.Color COLOR_INTERACTIVE_DARKER_FX = 
            javafx.scene.paint.Color.web(COLOR_INTERACTIVE_DARKER_HEX);
    
    /** Off-white color (JavaFX) - #E5E5E5. */
    public static final javafx.scene.paint.Color OFFWHITE_FX = 
            javafx.scene.paint.Color.web(OFFWHITE_HEX);
    
    /** Off-black color (JavaFX) - #1A1A1A. */
    public static final javafx.scene.paint.Color OFFBLACK_FX = 
            javafx.scene.paint.Color.web(OFFBLACK_HEX);
    
    /* ========================================================================
       JAVAFX PAINT (javafx.scene.paint.Paint) - for compatibility
       ======================================================================== */
    
    /** Outline color (JavaFX Paint). */
    public static final javafx.scene.paint.Paint COLOR_OUTLINE_PAINT = COLOR_OUTLINE_FX;
    
    /** Background color (JavaFX Paint). */
    public static final javafx.scene.paint.Paint COLOR_BACKGROUND_PAINT = COLOR_BACKGROUND_FX;
    
    /** Interactive color (JavaFX Paint). */
    public static final javafx.scene.paint.Paint COLOR_INTERACTIVE_PAINT = COLOR_INTERACTIVE_FX;
    
    /** Darker interactive color (JavaFX Paint). */
    public static final javafx.scene.paint.Paint COLOR_INTERACTIVE_DARKER_PAINT = COLOR_INTERACTIVE_DARKER_FX;
    
    /** Off-white color (JavaFX Paint). */
    public static final javafx.scene.paint.Paint OFFWHITE_PAINT = OFFWHITE_FX;
    
    /** Off-black color (JavaFX Paint). */
    public static final javafx.scene.paint.Paint OFFBLACK_PAINT = OFFBLACK_FX;
    
    /* ========================================================================
       UTILITY METHODS
       ======================================================================== */
    
    /**
     * Converts a Swing Color to JavaFX Color.
     * 
     * @param swingColor The Swing (AWT) Color to convert.
     * @return The equivalent JavaFX Color.
     */
    public static javafx.scene.paint.Color toFXColor(Color swingColor) {
        return javafx.scene.paint.Color.rgb(
            swingColor.getRed(),
            swingColor.getGreen(),
            swingColor.getBlue(),
            swingColor.getAlpha() / 255.0
        );
    }
    
    /**
     * Converts a JavaFX Color to Swing Color.
     * 
     * @param fxColor The JavaFX Color to convert.
     * @return The equivalent Swing (AWT) Color.
     */
    public static Color toSwingColor(javafx.scene.paint.Color fxColor) {
        return new Color(
            (int) (fxColor.getRed() * 255),
            (int) (fxColor.getGreen() * 255),
            (int) (fxColor.getBlue() * 255),
            (int) (fxColor.getOpacity() * 255)
        );
    }
    
    /**
     * Converts a Color to HEX string format (#RRGGBB).
     * 
     * @param color The Swing Color to convert.
     * @return The HEX string representation.
     */
    public static String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            color.getRed(),
            color.getGreen(),
            color.getBlue()
        );
    }
    
    /**
     * Converts a JavaFX Color to HEX string format (#RRGGBB).
     * 
     * @param color The JavaFX Color to convert.
     * @return The HEX string representation.
     */
    public static String toHexString(javafx.scene.paint.Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255)
        );
    }
    
    // Private constructor to prevent instantiation
    private UIColors() {
        throw new AssertionError("Cannot instantiate constants class");
    }
}