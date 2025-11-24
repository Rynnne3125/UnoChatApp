package coreapp.view.CustomComponents;

import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;

/**
 * A button with an image icon for JavaFX.
 */
public class ButtonWithImage extends Button {

    /**
     * Constructs a ButtonWithImageFX with the specified image, width, and height.
     *
     * @param image  The Image to be displayed on the button.
     * @param width  The width of the button.
     * @param height The height of the button.
     */
    public ButtonWithImage(Image image, double width, double height) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        this.setGraphic(imageView);

        // Remove default styling
        this.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // Set preferred size
        this.setPrefSize(width, height);

        // Change cursor on hover
        this.setOnMouseEntered(e -> this.setCursor(Cursor.HAND));
        this.setOnMouseExited(e -> this.setCursor(Cursor.DEFAULT));
    }
}
