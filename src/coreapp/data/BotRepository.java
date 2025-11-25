package coreapp.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import coreapp.model.cards.Card;
import coreapp.model.player.Bot;
import model.User;
import coreapp.util.constants.FileConstants;

public class BotRepository {

    public static List<Bot> getBots() {
        List<Bot> botList = new ArrayList<>();
        
        // 1. Thử đọc từ file
        File file = new File(FileConstants.BOT_DATA_FILE_PATH);
        
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] userData = line.split(FileConstants.BOT_DATA_SEPARATOR);
                    if (userData.length >= 4) { // Kiểm tra dữ liệu đủ không
                        User user = new User();
                        user.setId(userData[0]);
                        user.setUsername(userData[1]);
                        user.setEmail(userData[2]);
                        user.setPassword(userData[3]);
                        user.setRank("Bot"); // Đảm bảo rank là Bot
                        botList.add(new Bot(user, new ArrayList<Card>()));
                    }
                }
            } catch (IOException e) {
                System.err.println("⚠️ Lỗi đọc file bot: " + e.getMessage());
            }
        } else {
            System.err.println("⚠️ Không tìm thấy file bot tại: " + file.getAbsolutePath());
        }

        // 2. QUAN TRỌNG: Nếu không đọc được bot nào (file lỗi/thiếu), tạo Bot giả
        // Để game không bị Crash
        if (botList.isEmpty()) {
            System.out.println("🔄 Đang tạo Bot mặc định...");
            botList.add(createDummyBot("101", "Bot_Easy"));
            botList.add(createDummyBot("102", "Bot_Medium"));
            botList.add(createDummyBot("103", "Bot_Hard"));
            botList.add(createDummyBot("104", "Bot_Master"));
            botList.add(createDummyBot("105", "Bot_Legend"));
        }

        return botList; // Luôn trả về list (có thể rỗng nhưng không bao giờ null)
    }
    
    private static Bot createDummyBot(String id, String name) {
        User u = new User();
        u.setId(id);
        u.setUsername(name);
        u.setEmail(name.toLowerCase() + "@uno.com");
        u.setPassword("123");
        u.setRank("Bot");
        return new Bot(u, new ArrayList<Card>());
    }
}