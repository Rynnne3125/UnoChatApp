package coreapp.util.ui.toaster;

import coreapp.util.constants.UIFonts;
import coreapp.util.ui.UIUtils;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Represents the body of a toaster in JavaFX.
 */
public class ToasterBody extends StackPane {

    private static final int TOAST_PADDING = 15;
    private final Pane panelToToastOn;
    private final Label messageLabel;
    private volatile boolean stopDisplaying = false;

    public ToasterBody(Pane panelToToastOn, String message, Color bgColor, int targetY) {
        this.panelToToastOn = panelToToastOn;

        // Background rectangle with rounded corners
        Rectangle bgRect = new Rectangle();
        bgRect.setArcWidth(UIUtils.ROUNDNESS);
        bgRect.setArcHeight(UIUtils.ROUNDNESS);
        bgRect.setFill(bgColor);

        // Label
        messageLabel = new Label(message);
        messageLabel.setFont(UIFonts.FONT_GENERAL_UI);
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setAlignment(Pos.CENTER);

        this.getChildren().addAll(bgRect, messageLabel);
        this.setAlignment(Pos.CENTER);

        // Compute width/height based on text
        messageLabel.applyCss();
        messageLabel.layout();
        double textWidth = messageLabel.getWidth();
        double textHeight = messageLabel.getHeight();

        double toastWidth = textWidth + TOAST_PADDING * 2;
        double toastHeight = textHeight + TOAST_PADDING;

        bgRect.setWidth(toastWidth);
        bgRect.setHeight(toastHeight);

        this.setPrefSize(toastWidth, toastHeight);

        // Initial Y position (above screen)
        this.setLayoutX((panelToToastOn.getWidth() - toastWidth) / 2);
        this.setLayoutY(-toastHeight);

        // Animate moving down to targetY
        Timeline slideIn = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(this.layoutYProperty(), targetY))
        );
        slideIn.play();
    }

    /**
     * Animates the toast sliding up and removes it from the pane.
     */
    public void dismiss() {
        if (stopDisplaying) return;
        stopDisplaying = true;

        Timeline slideOut = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(this.layoutYProperty(), -this.getHeight()))
        );
        slideOut.setOnFinished(e -> panelToToastOn.getChildren().remove(this));
        slideOut.play();
    }

    public synchronized boolean getStopDisplaying() {
        return stopDisplaying;
    }

    public synchronized void setStopDisplaying(boolean stopDisplaying) {
        this.stopDisplaying = stopDisplaying;
    }
}
