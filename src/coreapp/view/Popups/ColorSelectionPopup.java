package coreapp.view.Popups;

import coreapp.model.enums.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Popup dialog for selecting a color (JavaFX version).
 */
public class ColorSelectionPopup {

    private Color selectedColor;
    private final Stage dialog;

    /**
     * Constructs a color selection popup dialog.
     *
     * @param parent the parent Stage
     */
    public ColorSelectionPopup(Stage parent) {
        dialog = new Stage();
        dialog.initOwner(parent);
        dialog.setTitle("Choose Color");
        dialog.initModality(Modality.WINDOW_MODAL);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        Button redBtn = new Button(Color.RED.toString());
        Button blueBtn = new Button(Color.BLUE.toString());
        Button greenBtn = new Button(Color.GREEN.toString());
        Button yellowBtn = new Button(Color.YELLOW.toString());

        redBtn.setOnAction(e -> choose(Color.RED));
        blueBtn.setOnAction(e -> choose(Color.BLUE));
        greenBtn.setOnAction(e -> choose(Color.GREEN));
        yellowBtn.setOnAction(e -> choose(Color.YELLOW));

        grid.add(redBtn, 0, 0);
        grid.add(blueBtn, 1, 0);
        grid.add(greenBtn, 0, 1);
        grid.add(yellowBtn, 1, 1);

        Scene scene = new Scene(grid, 300, 150);
        dialog.setScene(scene);
    }

    private void choose(Color color) {
        selectedColor = color;
        dialog.close();
    }

    /**
     * Shows the dialog and waits for the user to choose a color.
     */
    public void showAndWait() {
        dialog.showAndWait();
    }

    /**
     * Gets the selected color.
     *
     * @return the selected color
     */
    public Color getSelectedColor() {
        return selectedColor;
    }
}
