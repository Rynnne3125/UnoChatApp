package utils;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * UI styling utility for consistent, modern look across all views
 * Provides pre-built components with UNO theme colors
 */
public class UIThemeManager {

    // ==================== COLOR PALETTE ====================
    public static final String PRIMARY_RED = "#d13639";
    public static final String PRIMARY_DARK = "#0a0e27";
    public static final String SECONDARY_DARK = "#0f1923";
    public static final String TEXT_PRIMARY = "#ffffff";
    public static final String TEXT_SECONDARY = "#7a7d82";
    public static final String ACCENT_GOLD = "#c8aa6e";
    public static final String SUCCESS_GREEN = "#2ecc71";
    public static final String ERROR_RED = "#e74c3c";
    public static final String WARNING_ORANGE = "#f39c12";

    // ==================== BUTTONS ====================
    public static Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 25 10 25; " +
                        "-fx-background-color: linear-gradient(to bottom, " + PRIMARY_RED + " 0%, #b91c23 100%); " +
                        "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 8, 0, 0, 2);"
        );
        return btn;
    }

    public static Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-padding: 8 18 8 18; " +
                        "-fx-background-color: rgba(255, 255, 255, 0.1); " +
                        "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                        "-fx-border-color: " + TEXT_SECONDARY + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
        );
        return btn;
    }

    public static Button createIconButton(String icon, String color) {
        Button btn = new Button(icon);
        btn.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-padding: 10 12 10 12; " +
                        "-fx-background-color: rgba(" + hexToRgb(color) + ", 0.2); " +
                        "-fx-border-color: " + color + "; " +
                        "-fx-border-width: 1.5; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        return btn;
    }

    // ==================== LABELS & TEXT ====================
    public static Label createTitleLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + PRIMARY_RED + ";"
        );
        return lbl;
    }

    public static Label createSubtitleLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + TEXT_PRIMARY + ";"
        );
        return lbl;
    }

    public static Label createBodyLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-text-fill: " + TEXT_SECONDARY + ";"
        );
        return lbl;
    }

    // ==================== INPUT FIELDS ====================
    public static TextField createStyledTextField(String promptText) {
        TextField tf = new TextField();
        tf.setPromptText(promptText);
        tf.setStyle(
                "-fx-padding: 10 15 10 15; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-color: rgba(255, 255, 255, 0.05); " +
                        "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                        "-fx-prompt-text-fill: " + TEXT_SECONDARY + "; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.1); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6;"
        );
        return tf;
    }

    // ==================== CONTAINERS ====================
    public static VBox createCardContainer() {
        VBox card = new VBox();
        card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.05); " +
                        "-fx-border-color: " + PRIMARY_RED + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-padding: 20; " +
                        "-fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 5, 0, 0, 1);"
        );
        return card;
    }

    public static HBox createButtonBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER);
        bar.setStyle("-fx-spacing: 15; -fx-padding: 15 0 15 0;");
        return bar;
    }

    public static VBox createCenteredContent() {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setStyle("-fx-padding: 30 50 30 50;");
        return vbox;
    }

    // ==================== SEPARATORS ====================
    public static Separator createStyledSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + TEXT_SECONDARY + "; -fx-opacity: 0.2;");
        return sep;
    }

    // ==================== HELPER METHODS ====================
    private static String hexToRgb(String hex) {
        hex = hex.substring(1);
        int r = Integer.valueOf(hex.substring(0, 2), 16);
        int g = Integer.valueOf(hex.substring(2, 4), 16);
        int b = Integer.valueOf(hex.substring(4, 6), 16);
        return r + "," + g + "," + b;
    }

    // ==================== DROPDOWN MENU ====================
    public static ComboBox<String> createStyledComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setStyle(
                "-fx-padding: 8 12 8 12; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-color: rgba(255, 255, 255, 0.05); " +
                        "-fx-text-fill: " + TEXT_PRIMARY + "; " +
                        "-fx-border-color: " + TEXT_SECONDARY + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6;"
        );
        return combo;
    }

    // ==================== TOAST NOTIFICATION ====================
    public static void showToastNotification(String message, String type) {
        System.out.println("[" + type + "] " + message);
        // Can be extended to show visual toast later
    }

    // ==================== GRADIENT BACKGROUNDS ====================
    public static String getRandomGameBackground() {
        String[] backgrounds = {
                "linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)",
                "linear-gradient(135deg, #2d1b00 0%, #3d2817 50%, #4a3a2a 100%)",
                "linear-gradient(135deg, #1b1b1b 0%, #2d2d2d 50%, #404040 100%)",
                "linear-gradient(135deg, #0a0a1a 0%, #1a0a2e 50%, #16213e 100%)",
                "linear-gradient(135deg, #0f1419 0%, #1a1f2e 50%, #2d3561 100%)",
                "linear-gradient(135deg, #1a0f1f 0%, #2d1b3d 50%, #3d2a4a 100%)",
                "linear-gradient(135deg, #0f1f1f 0%, #1a3f3f 50%, #2a5f5f 100%)"
        };
        return backgrounds[(int) (Math.random() * backgrounds.length)];
    }
}
