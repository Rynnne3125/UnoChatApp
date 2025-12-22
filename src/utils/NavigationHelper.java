package utils;

import javafx.application.Platform;
import javafx.stage.Stage;
import application.UnoGameMenu;

/**
 * Navigation utility for handling back buttons and view transitions
 */
public class NavigationHelper {

    public static void backToGameMenu(Stage stage) {
        Platform.runLater(() -> {
            try {
                UnoGameMenu gameMenu = new UnoGameMenu();
                stage.setScene(null); // Clear current scene
                gameMenu.start(stage);
            } catch (Exception e) {
                System.err.println("Navigation error: " + e.getMessage());
            }
        });
    }

    public static void backToPreviousView(Stage stage, String viewName) {
        Platform.runLater(() -> {
            try {
                if ("menu".equalsIgnoreCase(viewName)) {
                    backToGameMenu(stage);
                }
            } catch (Exception e) {
                System.err.println("Navigation error: " + e.getMessage());
            }
        });
    }
}
