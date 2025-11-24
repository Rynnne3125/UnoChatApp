package coreapp.view.CustomComponents;

import javafx.scene.layout.Region;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * A Region with a vertical gradient background.
 * JavaFX version of Swing's GradientPanel.
 */
public class GradientPanel extends Region {

    private Color color1;
    private Color color2;

    /**
     * Constructs a GradientPanel with specified gradient colors.
     *
     * @param color1 The starting color of the gradient.
     * @param color2 The ending color of the gradient.
     */
    public GradientPanel(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
        initGradient();
    }

    /**
     * Constructs a GradientPanel with default colors.
     */
    public GradientPanel() {
        this(Color.rgb(20, 136, 204), Color.rgb(43, 50, 178));
    }

    /**
     * Initializes the gradient background.
     */
    private void initGradient() {
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, // startX, startY, endX, endY (normalized)
                true, // proportional
                CycleMethod.NO_CYCLE,
                new Stop(0, color1),
                new Stop(1, color2)
        );
        this.setBackground(new Background(new BackgroundFill(gradient, null, null)));
    }
}
