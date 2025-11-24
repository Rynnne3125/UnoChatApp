package coreapp.util.constants;

import java.awt.Color;
import javafx.scene.paint.Paint;

/**
 * Constants for UI colors.
 */
public class UIColors {

    /** Swing Colors -------------------------------------------------------- */

    /** Outline color (Swing). */
    public static final Color COLOR_OUTLINE = new Color(103, 112, 120);

    /** Background color (Swing). */
    public static final Color COLOR_BACKGROUND = new Color(232, 232, 232);

    /** Interactive color (Swing). */
    public static final javafx.scene.paint.Color COLOR_INTERACTIVE = new Color(108, 216, 158);

    /** Darker interactive color (Swing). */
    public static final Color COLOR_INTERACTIVE_DARKER = new Color(87, 171, 127);

    /** Off-white color (Swing). */
    public static final Color OFFWHITE = new Color(229, 229, 229);

    /** Off-black color (Swing). */
    public static final Color OFFBLACK = new Color(26, 26, 26);


    /** JavaFX Colors ------------------------------------------------------- */

    /** As HEX text for CSS (JavaFX). */
    public static final String COLOR_OUTLINE_HEX = "#677078";
    public static final String COLOR_BACKGROUND_HEX = "#E8E8E8";
    public static final String COLOR_INTERACTIVE_HEX = "#6CD89E";
    public static final String COLOR_INTERACTIVE_DARKER_HEX = "#57AB7F";
    public static final String OFFWHITE_HEX = "#E5E5E5";
    public static final String OFFBLACK_HEX = "#1A1A1A";

    /** JavaFX Paint colors (use for setStyle, setFill, borderColor, etc.) */
    public static final Paint COLOR_OUTLINE_FX = Paint.valueOf(COLOR_OUTLINE_HEX);
    public static final Paint COLOR_BACKGROUND_FX = Paint.valueOf(COLOR_BACKGROUND_HEX);
    public static final Paint COLOR_INTERACTIVE_FX = Paint.valueOf(COLOR_INTERACTIVE_HEX);
    public static final Paint COLOR_INTERACTIVE_DARKER_FX = Paint.valueOf(COLOR_INTERACTIVE_DARKER_HEX);
    public static final Paint OFFWHITE_FX = Paint.valueOf(OFFWHITE_HEX);
    public static final Paint OFFBLACK_FX = Paint.valueOf(OFFBLACK_HEX);
}
