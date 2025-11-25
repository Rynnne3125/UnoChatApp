package control;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import model.User;
import utils.EmailService;
import utils.showCustomAlert;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import application.Main;
import application.UnoGameMenu;
import dao.FirebaseNewsRest;
import dao.FirebaseUserRest;

public class LoginController implements Initializable {

    // --- KHAI BÁO CÁC FORM ---
    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    @FXML private VBox otpForm;
    @FXML private VBox forgotForm;
    @FXML private VBox loadingOverlay; // Màn hình loading

    // --- CÁC TRƯỜNG NHẬP LIỆU REGISTER ---
    @FXML private TextField regEmail;
    @FXML private TextField regUser;
    @FXML private PasswordField regPass;

    // --- CÁC TRƯỜNG NHẬP LIỆU OTP ---
    @FXML private Label otpMessageLabel;
    @FXML private TextField otpInput;

    // --- CÁC TRƯỜNG NHẬP LIỆU FORGOT PASSWORD ---
    @FXML private TextField forgotEmail;

    // --- VIDEO BACKGROUND ---
    @FXML private StackPane rightPane;
    @FXML private MediaView bgMediaView;
    private MediaPlayer mediaPlayer;

    // --- BIẾN LƯU TRỮ TẠM THỜI ---
    private String serverOtpCode; // Mã OTP do hệ thống sinh ra
    private User tempUser;        // Thông tin user chờ đăng ký
    
    @FXML private TextField loginUser;
    @FXML private PasswordField loginPass;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupVideo();
    }
    public TextField getloginUserTextField() {
    	return this.loginUser;
    }
    // 1. THIẾT LẬP VIDEO NỀN
    private void setupVideo() {
        try {
            File file = new File("img/uno_banner.mp4");
            if (file.exists()) {
                Media media = new Media(file.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                bgMediaView.setMediaPlayer(mediaPlayer);
                
                // Cấu hình Loop và Mute
                mediaPlayer.setAutoPlay(true);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setMute(true);
                
                // Resize video theo khung
                bgMediaView.setPreserveRatio(false);
                bgMediaView.fitWidthProperty().bind(rightPane.widthProperty());
                bgMediaView.fitHeightProperty().bind(rightPane.heightProperty());
                
                mediaPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("Video load failed: " + e.getMessage());
        }
    }

    // 2. LOGIC ĐĂNG KÝ (BƯỚC 1: GỬI OTP)
    @FXML
    private void handlePreRegister() {
        String email = regEmail.getText().trim();
        String user = regUser.getText().trim();
        String pass = regPass.getText();

        if (email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            showCustomAlert.showCustomAlert("Error", "Please fill all fields!");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showCustomAlert.showCustomAlert("Error", "Invalid email address!");
            return;
        }

        // Lưu tạm thông tin
        tempUser = new User(email, user, pass);
        
        // Bật chế độ Loading
        setLoading(true);

        // Chạy tiến trình gửi mail trong Thread riêng
        new Thread(() -> {
            try {
                serverOtpCode = EmailService.generateOTP();
                EmailService.sendOTP(email, serverOtpCode);

                // Cập nhật UI khi thành công (phải dùng Platform.runLater)
                Platform.runLater(() -> {
                    setLoading(false);
                    otpMessageLabel.setText("Code sent to: " + email);
                    switchToOtpForm();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setLoading(false);
                    showCustomAlert.showCustomAlert("Error", "Cannot send Email. Check internet or App Password.");
                });
            }
        }).start();
    }

    // 3. LOGIC XÁC THỰC OTP VÀ LƯU LÊN FIREBASE (BƯỚC 2)
    @FXML
    private void handleVerifyAndCreate() {
        String userCode = otpInput.getText().trim();

        if (serverOtpCode != null && serverOtpCode.equals(userCode)) {
            // OTP Đúng -> Bắt đầu lưu lên Server
            setLoading(true);

            new Thread(() -> {
                boolean success = FirebaseUserRest.registerUser(tempUser);
                
                Platform.runLater(() -> {
                    setLoading(false);
                    if (success) {
                        showCustomAlert.showCustomAlert("Success", "Account created successfully on Firebase!");
                        // Reset form
                        regEmail.clear(); regUser.clear(); regPass.clear(); otpInput.clear();
                        switchToLogin();
                    } else {
                        showCustomAlert.showCustomAlert("Error", "OTP Correct, but failed to save to Firebase.");
                    }
                });
            }).start();
        } else {
            showCustomAlert.showCustomAlert("Error", "Wrong OTP Code! Please check again.");
        }
    }

    // 4. LOGIC QUÊN MẬT KHẨU (GỬI OTP)
    @FXML
    private void handleSendForgotPasswordOTP() {
        String email = forgotEmail.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            showCustomAlert.showCustomAlert("Error", "Please enter a valid email.");
            return;
        }

        setLoading(true);
        new Thread(() -> {
            try {
                String otp = EmailService.generateOTP();
                EmailService.sendOTP(email, otp);
                // (Lưu ý: Ở đây bạn cần logic để lưu OTP này lại và xử lý đổi mật khẩu tiếp theo)
                
                Platform.runLater(() -> {
                    setLoading(false);
                    showCustomAlert.showCustomAlert("Sent", "OTP sent to " + email + ". (Reset password logic needs implementation)");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showCustomAlert.showCustomAlert("Error", "Failed to send email.");
                });
            }
        }).start();
    }
    @FXML
    private void handleLogin() {
        String username = loginUser.getText().trim();
        String password = loginPass.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showCustomAlert.showCustomAlert("Error", "Please enter username and password.");
            return;
        }

        setLoading(true); // Hiện loading

        new Thread(() -> {
            // Gọi hàm check login mới viết bên Service
            boolean success = FirebaseUserRest.checkLoginAndUpdateStatus(username, password);

            Platform.runLater(() -> {
                setLoading(false);
                if (success) {
                    try {
                        // Lấy thông tin user đầy đủ từ Firebase (bao gồm avatar)
                        User loggedInUser = FirebaseNewsRest.findUser(username);
                        if (loggedInUser == null) {
                            showCustomAlert.showCustomAlert("Error", "Failed to load user information.");
                            return;
                        }
                        
                        // Nếu avatar rỗng thì mới random (chỉ cho user mới đăng ký)
                        if (loggedInUser.getImageAvatar() == null || loggedInUser.getImageAvatar().isEmpty()) {
                            loggedInUser.setIMGforUser(loggedInUser);
                        }
                        
                    	showCustomAlert.showCustomAlert("Welcome, Gamer", "Login Successful! You are now Online.");
                    	Main.CurrentUser = loggedInUser;
                    	Stage stage = (Stage) loginUser.getScene().getWindow();
                        // Khởi tạo Menu và chạy trên Stage hiện tại
                        UnoGameMenu gameMenu = new UnoGameMenu();
                        stage.setFullScreen(true);
                        gameMenu.start(stage);
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        showCustomAlert.showCustomAlert("Error", "Failed to load application.");
                    }
                }
                else {
                    showCustomAlert.showCustomAlert("Failed", "Invalid Username or Password.");
                }
            });
        }).start();
    }
    // --- CÁC HÀM CHUYỂN ĐỔI GIAO DIỆN ---
    @FXML private void switchToLogin() { setVisibleForm(true, false, false, false); }
    @FXML private void switchToRegister() { setVisibleForm(false, true, false, false); }
    @FXML private void switchToForgot() { setVisibleForm(false, false, false, true); }
    private void switchToOtpForm() { setVisibleForm(false, false, true, false); }

    private void setVisibleForm(boolean login, boolean reg, boolean otp, boolean forgot) {
        loginForm.setVisible(login);   loginForm.setManaged(login);
        registerForm.setVisible(reg);  registerForm.setManaged(reg);
        otpForm.setVisible(otp);       otpForm.setManaged(otp);
        forgotForm.setVisible(forgot); forgotForm.setManaged(forgot);
    }

    // --- HÀM TIỆN ÍCH ---
    
    // Bật/Tắt lớp loading và vô hiệu hóa các nút bấm
    private void setLoading(boolean isLoading) {
        loadingOverlay.setVisible(isLoading);
        loginForm.setDisable(isLoading);
        registerForm.setDisable(isLoading);
        otpForm.setDisable(isLoading);
        forgotForm.setDisable(isLoading);
    }

}