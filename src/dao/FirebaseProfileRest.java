package dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FirebaseProfileRest {

    private static final String BASE_URL = "https://unofirebase-19555-default-rtdb.firebaseio.com/"; // ID dự án của bạn
    private static final Gson gson = new GsonBuilder().create();

    // Lấy thông tin user theo username
    public static User getUserProfile(String username) {
        try {
            String path = BASE_URL + "users/" + username + ".json";
            String json = sendRequest(path, "GET", null);

            if (json != null && !json.equals("null")) {
                return gson.fromJson(json, User.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Cập nhật trạng thái (Online/Offline/In-Game)
    public static boolean updateUserStatus(String username, String statusMsg) {
        try {
            String path = BASE_URL + "users/" + username + ".json";
            // Dùng Gson để tạo JSON string an toàn hơn String.format
            String jsonBody = "{\"status_message\": \"" + statusMsg + "\"}";
            
            return sendRequest(path, "PATCH", jsonBody) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật avatar user (URL ảnh)
    public static boolean updateUserAvatar(String username, String avatarUrl) {
        try {
            String path = BASE_URL + "users/" + username + ".json";

            // Tạo đối tượng tạm để Gson chuyển thành JSON (An toàn, tự động escape ký tự đặc biệt)
            // Hoặc build chuỗi thủ công nhưng cần cẩn thận
            String safeUrl = avatarUrl.replace("\"", "\\\""); 
            String jsonBody = String.format("{\"image_avatar\":\"%s\"}", safeUrl);

            return sendRequest(path, "PATCH", jsonBody) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- HÀM GỬI REQUEST (ĐÃ FIX LỖI PATCH) ---
    private static String sendRequest(String urlStr, String method, String jsonBody) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // FIX: HttpURLConnection không hỗ trợ PATCH trực tiếp.
            // Giải pháp: Gửi POST và thêm header X-HTTP-Method-Override: PATCH
            if ("PATCH".equals(method)) {
                conn.setRequestMethod("POST");
                conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            } else {
                conn.setRequestMethod(method);
            }

            conn.setConnectTimeout(5000); // Thêm timeout để không bị treo
            conn.setReadTimeout(5000);

            if (jsonBody != null) {
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }
            }

            int responseCode = conn.getResponseCode();
            // Firebase trả về 200 OK hoặc 204 No Content khi thành công
            if (responseCode >= 200 && responseCode < 300) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);
                return response.toString();
            } else {
                System.err.println("Request Failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}