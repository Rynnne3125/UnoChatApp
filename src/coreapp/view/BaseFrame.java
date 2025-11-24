package coreapp.view;

import coreapp.util.constants.ImagePath;
import coreapp.util.constants.WindowConstants;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * The BaseFrameFX class represents a base stage for creating JavaFX-based frames.
 * It provides common functionality such as centering the window on the screen,
 * setting default size, making the stage not resizable, and setting a custom icon.
 * Subclasses can extend this class to create specific frames for their application.
 */
public abstract class BaseFrame {

    protected BaseFrame previousFrame;
    private final Stage stage;

    /**
     * Constructs a new BaseFrameFX with the specified title and previous frame.
     *
     * @param title         The title of the stage.
     * @param previousFrame The previous frame that this stage is associated with.
     *                      Can be null if there is no previous frame.
     */
    public BaseFrame(String title, BaseFrame previousFrame) {
        this.previousFrame = previousFrame;
        stage = new Stage();
        stage.setTitle(title);

        stage.setWidth(WindowConstants.DEFAULT_WINDOW_WIDTH);
        stage.setHeight(WindowConstants.DEFAULT_WINDOW_HEIGHT);
        stage.setResizable(false);

        centerStage();

        changeWindowIcon();

        // Khi đóng stage thì thoát app
        stage.setOnCloseRequest(e -> Platform.exit());
    }

    /**
     * Constructs a new BaseFrameFX with the specified title.
     *
     * @param title The title of the stage.
     */
    public BaseFrame(String title) {
        this(title, null);
    }

    protected BaseFrame(Stage stage) {
        this.stage = stage;
    }

    /**
     * Lấy Stage hiện tại để thao tác (setScene, show, hide, v.v.)
     *
     * @return Stage hiện tại
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Đóng stage này.
     */
    public void dispose() {
        stage.close();
    }

    /**
     * Center the stage on the screen.
     */
    private void centerStage() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    /**
     * Change the window icon to a custom image.
     */
    public void changeWindowIcon() {
        try {
            Image icon = new Image(ImagePath.WINDOW_ICON);
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Failed to load window icon: " + e.getMessage());
        }
    }

    abstract void initializeStage();

    /**
     * Abstract method to initialize the frame. Subclasses must implement this method
     * to set up the frame components.
     */
    abstract void initializeFrame();
}
