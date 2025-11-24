package coreapp.util.ui.toaster;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Represents a toaster utility for displaying notifications on a specified Pane in JavaFX.
 * The toaster can display error, success, info, and warning messages.
 */
public class Toaster {

    private static final int STARTING_Y_POS = 15;
    private static ToasterBody currentToast = null;
    private final Pane panelToToastOn;
    private boolean isShowingToast = false;

    public Toaster(Pane panelToToastOn) {
        this.panelToToastOn = panelToToastOn;
    }

    public void error(String... messages) {
        showToast(messages, Color.rgb(181, 59, 86));
    }

    public void success(String... messages) {
        showToast(messages, Color.rgb(33, 181, 83));
    }

    public void info(String... messages) {
        showToast(messages, Color.rgb(13, 116, 181));
    }

    public void warn(String... messages) {
        showToast(messages, Color.rgb(181, 147, 10));
    }

    private synchronized void showToast(String[] messages, Color bgColor) {
        if (!isShowingToast) {
            isShowingToast = true;

            String message = String.join(" ", messages);
            ToasterBody toasterBody = new ToasterBody(panelToToastOn, message, bgColor, STARTING_Y_POS);

            toasterBody.setOnMouseClicked(e -> removeToast(toasterBody));

            if (currentToast != null) {
                removeToast(currentToast);
            }
            currentToast = toasterBody;

            Platform.runLater(() -> panelToToastOn.getChildren().add(toasterBody));

            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(2500), e -> removeToast(toasterBody)));
            timeline.play();
        }
    }

    private synchronized void removeToast(ToasterBody toasterBody) {
        if (!toasterBody.getStopDisplaying()) {
            toasterBody.setStopDisplaying(true);
            isShowingToast = false;

            Platform.runLater(() -> panelToToastOn.getChildren().remove(toasterBody));
        }
    }
}
