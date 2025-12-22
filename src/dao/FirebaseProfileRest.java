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

    private static final String BASE_URL = "https://unofirebase-19555-default-rtdb.firebaseio.com/"; // Thay ID dự án của bạn
    private static final Gson gson = new GsonBuilder().create();

    // Lấy thông tin user theo username
    public static User getUserProfile(String username) {
        try {
            // Giả định bạn lưu user theo key là username
            String path = BASE_URL + "users/" + username + ".json";
            String json = sendRequest(path, "GET", null);

            if (json != null && !json.equals("null")) {
                User user = gson.fromJson(json, User.class);
                return user;
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
            // Chỉ update trường specific thì dùng PATCH
            String jsonBody = String.format("{\"status_message\": \"%s\"}", statusMsg);

            // Firebase Rest API hỗ trợ PATCH để update partial data
            return sendRequest(path, "PATCH", jsonBody) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật ảnh đại diện (image_avatar)
    public static boolean updateUserAvatar(String username, String avatarUrl) {
        try {
            String path = BASE_URL + "users/" + username + ".json";
            String jsonBody = String.format("{\"image_avatar\": \"%s\"}", avatarUrl.replace("\\", "\\\\").replace("\"", "\\\""));
            return sendRequest(path, "PATCH", jsonBody) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật bio
    public static boolean updateUserBio(String username, String bio) {
        try {
            String path = BASE_URL + "users/" + username + ".json";
            String jsonBody = String.format("{\"bio\": \"%s\"}", bio.replace("\\", "\\\\").replace("\"", "\\\""));
            return sendRequest(path, "PATCH", jsonBody) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm gửi request chung (Tái sử dụng)
    private static String sendRequest(String urlStr, String method, String jsonBody) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);

            if (jsonBody != null) {
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }
            }

            if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) response.append(line);
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}