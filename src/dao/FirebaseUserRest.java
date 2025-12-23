package dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.gson.Gson;
import model.User;

public class FirebaseUserRest {

    // Thay YOUR_PROJECT_ID bằng ID thật của bạn
    private static final String BASE_URL = "https://unofirebase-19555-default-rtdb.firebaseio.com/users/";
    private static final Gson gson = new Gson();
    private final static List<String> RANDOM_AVATARS = List.of(
            "https://media.makeameme.org/created/uno-reverse-lol.jpg",
            "https://img.freepik.com/premium-photo/vibrant-men-s-cricket-world-cup-2024-illustration-featuring-dynamic-cricket-illustration-with-fast-hits_719166-4508.jpg",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRGcCnNGEL4TaYHxQEzSfIpnQlz3VcW9TAsKQ&s",
            "https://i.pinimg.com/736x/d5/c0/fc/d5c0fc0734cb465b16affe739be62c52.jpg",
            "https://image.spreadshirtmedia.net/image-server/v1/products/T949A2PA2009PT25X7Y0D320877709W4629H7023/views/3,width=550,height=550,appearanceId=2,backgroundColor=F2F2F2,modelId=11689,crop=list/uno-4-design-four-draw-card-mug.jpg",
            "https://cdn.dribbble.com/userupload/23839376/file/original-f6a79767815644ade14c04c8b7b80a9e.png"
    );
    private static String getRandomAvatar() {
        try {
			return RANDOM_AVATARS.get(
			    new java.util.Random().nextInt(RANDOM_AVATARS.size())
			);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
    }

    // --- 1. ĐĂNG KÝ (Dùng PUT để lưu theo Username) ---
    public static boolean registerUser(User user) {
    	// Nếu user chưa có avatar → gán ngẫu nhiên
        if (user.getImageAvatar() == null || user.getImageAvatar().isBlank()) {
            user.setImageAvatar(getRandomAvatar());
        }
        try {
            // Lưu trực tiếp vào node /users/USERNAME.json
            String path = BASE_URL + user.getUsername() + ".json";
            URL url = new URL(path);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT"); // Dùng PUT để ghi đè/tạo mới tại vị trí cụ thể
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            String jsonInputString = user.toJson();
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 2. ĐĂNG NHẬP & CẬP NHẬT TRẠNG THÁI ---
    public static boolean checkLoginAndUpdateStatus(String username, String password) {
        try {
            // BƯỚC 1: Lấy thông tin User về (GET)
            String path = BASE_URL + username + ".json";
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) return false; // Không tìm thấy user hoặc lỗi mạng

            // Đọc dữ liệu trả về
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            String jsonStr = response.toString();

            // Nếu trả về "null" nghĩa là username không tồn tại
            if (jsonStr.equals("null")) return false;

            // BƯỚC 2: Check Password thủ công (Parse chuỗi JSON đơn giản)
            // Tìm đoạn "password":"..."
            String searchStr = "\"password\":\"" + password + "\"";
            if (!jsonStr.contains(searchStr)) {
                return false; // Sai mật khẩu
            }

            updateOnlineStatus(username, true);
            
            return true; // Login thành công

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm phụ để update trạng thái
    public static void updateOnlineStatus(String username, boolean isOnline) {
        try {
            String path = BASE_URL + username + ".json";
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            // JSON body chỉ chứa field cần sửa
            String jsonUpdate = String.format("{\"online_status\": %b}", isOnline);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonUpdate.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            conn.getResponseCode(); // Thực thi request
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Lấy thông tin User từ Firebase theo username
     */
    public static User getUserInfo(String username) {
        try {
            String path = BASE_URL + username + ".json";
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) return null;
            
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            String jsonStr = response.toString();
            
            if (jsonStr.equals("null") || jsonStr.isEmpty()) return null;
            
            // Parse JSON thành User object
            User user = gson.fromJson(jsonStr, User.class);
            if (user != null && (user.getUsername() == null || user.getUsername().isEmpty())) {
                user.setUsername(username);
            }
            
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}