package coreapp.view;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;

/**
 * Base class for all JavaFX frames in the application.
 * Provides common functionality for window management.
 */
public abstract class BaseFrame extends Stage {
    
    /**
     * Constructor for BaseFrame
     * @param title The title of the window
     */
    public BaseFrame(String title) {
        super();
        setTitle(title);
        initStyle(StageStyle.DECORATED);
        setResizable(false);
    }
    
    /**
     * Initialize frame components (for views that need frame-style initialization)
     */
    abstract void initializeFrame();
    
    /**
     * Initialize stage components (for views that need stage-style initialization)
     */
    abstract void initializeStage();
    
    /**
     * Close and dispose of the window
     */
    public void dispose() {
        this.close();
    }
}