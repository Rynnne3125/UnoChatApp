package utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CatboxUploader {

    private static final String CATBOX_URL = "https://catbox.moe/user/api.php";
    private static final String POMF_URL = "https://pomf.lain.la/upload.php";

    public static String uploadFile(File file) {
        String fileName = file.getName().toLowerCase();
        
        // Logic chọn server
        if (fileName.endsWith(".mp4") || fileName.endsWith(".mp3")) {
            // Pomf dùng param 'files[]'
            return postFile(file, POMF_URL, "files[]"); 
        } else {
            // Catbox dùng param 'fileToUpload'
            return postFile(file, CATBOX_URL, "fileToUpload");
        }
    }

    private static String postFile(File file, String urlString, String fileParamName) {
        String boundary = Long.toHexString(System.currentTimeMillis());
        String LINE_FEED = "\r\n";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            try (OutputStream output = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

                if (urlString.equals(CATBOX_URL)) {
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"reqtype\"").append(LINE_FEED);
                    writer.append(LINE_FEED).append("fileupload").append(LINE_FEED);
                }

                writer.append("--" + boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"" + fileParamName + "\"; filename=\"" + file.getName() + "\"").append(LINE_FEED);
                writer.append("Content-Type: application/octet-stream").append(LINE_FEED);
                writer.append(LINE_FEED).flush();

                try (FileInputStream inputStream = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                    output.flush();
                }

                writer.append(LINE_FEED).flush();
                writer.append("--" + boundary + "--").append(LINE_FEED).flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    
                    String rawResponse = response.toString();
                    
                    // --- PHẦN XỬ LÝ QUAN TRỌNG MỚI THÊM ---
                    if (urlString.equals(POMF_URL)) {
                        // Pomf trả về JSON, cần bóc tách lấy URL
                        return extractUrlFromJson(rawResponse);
                    } else {
                        // Catbox trả về URL trực tiếp, dùng luôn
                        return rawResponse;
                    }
                }
            } else {
                System.err.println("Upload failed code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Hàm bóc tách URL từ JSON bằng Regex (không cần thư viện ngoài)
    private static String extractUrlFromJson(String json) {
        // Mẫu JSON: ... "url": "https:\/\/pomf2.lain.la\/f\/cavwufye.mp4" ...
        // Regex tìm chuỗi nằm sau "url": " và trước dấu " tiếp theo
        Pattern pattern = Pattern.compile("\"url\"\\s*:\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(json);
        
        if (matcher.find()) {
            String url = matcher.group(1);
            // JSON thường escape dấu gạch chéo (ví dụ \/), cần thay thế lại thành /
            return url.replace("\\/", "/");
        }
        
        // Nếu không tìm thấy (lỗi server hoặc format đổi), trả về null hoặc chuỗi gốc để debug
        System.err.println("Không tìm thấy URL trong JSON Pomf: " + json);
        return null;
    }
}