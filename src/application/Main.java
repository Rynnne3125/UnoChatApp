package application;
	
import dao.FirebaseNewsRest;
import dao.FirebaseUserRest;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import model.User;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.util.concurrent.TimeUnit;

import control.NewsController;


public class Main extends Application {
	public static User CurrentUser;
	
	@Override
	public void start(Stage stage) {
		try {
			
			Parent root = FXMLLoader.load(getClass().getResource("LoginView.fxml"));
			   Scene scene = new Scene(root);
			   stage.setScene(scene);
			   stage.setTitle("UNO GAMES");
			   stage.setScene(scene);
		        
			   stage.show();
			   
			   // Cleanup khi đóng cửa sổ
			   stage.setOnCloseRequest(event -> {
			        cleanupAndExit();
			    });
			    
			// Shutdown hook để đảm bảo cleanup khi exit app (Ctrl+C, kill process, etc.)
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				cleanupAndExit();
			}));
			    
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Cleanup tất cả resources và kill threads trước khi exit
	 */
	private static void cleanupAndExit() {
		try {
			// 1. Cleanup NewsController (shutdown threads, clear cache)
			NewsController.cleanupAll();
			
			// 2. Update online status nếu có user đang login (timeout ngắn để tránh hang)
			if (CurrentUser != null && CurrentUser.getUsername() != null) {
				try {
					// Chạy trong thread riêng với timeout để tránh hang
					Thread updateThread = new Thread(() -> {
						try {
							FirebaseUserRest.updateOnlineStatus(CurrentUser.getUsername(), false);
						} catch (Exception e) {
							System.err.println("Failed to update online status: " + e.getMessage());
						}
					});
					updateThread.setDaemon(true);
					updateThread.start();
					updateThread.join(500); // Chờ tối đa 500ms
				} catch (Exception e) {
					System.err.println("Error updating online status: " + e.getMessage());
				}
			}
			
		} catch (Exception e) {
			System.err.println("Error during cleanup: " + e.getMessage());
		} finally {
			// Exit JavaFX application
			try {
				Platform.exit();
			} catch (Exception e) {
				// Ignore
			}
			
			// Force exit sau 500ms để đảm bảo kill tất cả threads
			new Thread(() -> {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				System.exit(0);
			}).start();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
