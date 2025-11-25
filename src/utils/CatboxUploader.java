package utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class CatboxUploader {

    private static final String UPLOAD_URL = "https://catbox.moe/user/api.php";

    public static String uploadFile(File file) {
        String boundary = Long.toHexString(System.currentTimeMillis());
        String LINE_FEED = "\r\n";

        try {
            URL url = new URL(UPLOAD_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream output = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

                // Thêm reqtype
                writer.append("--" + boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"reqtype\"").append(LINE_FEED);
                writer.append(LINE_FEED).append("fileupload").append(LINE_FEED);

                // Thêm file
                writer.append("--" + boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"" + file.getName() + "\"").append(LINE_FEED);
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

            // Đọc phản hồi
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            } else {
                System.err.println("Upload failed. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
