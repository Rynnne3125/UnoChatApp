package model;

import java.util.Random;
import java.util.UUID;
import com.google.gson.annotations.SerializedName;

public class User {
    
    private String id; 
    private String email;
    private String username;
    private String password;

    @SerializedName("image_avatar")
    private String imageAvatar = "";

    @SerializedName("online_status")
    private boolean onlineStatus;

    private int point;
    private String rank;

    private static final String[] AVATAR_LIST = {
        "https://i.pinimg.com/736x/d5/c0/fc/d5c0fc0734cb465b16affe739be62c52.jpg",
        "https://cdn.dribbble.com/userupload/23839376/file/original-f6a79767815644ade14c04c8b7b80a9e.png",
        "https://img.freepik.com/premium-photo/vibrant-men-s-cricket-world-cup-2024-illustration-featuring-dynamic-cricket-illustration-with-fast-hits_719166-4508.jpg"
    };

    // --- 1. THÊM CONSTRUCTOR RỖNG (ĐỂ SỬA LỖI BOTREPOSITORY) ---
    public User() {
        // Khởi tạo các giá trị mặc định tránh null pointer exception
        this.point = 0;
        this.rank = "Bot";
        this.onlineStatus = true; // Bot thì thường luôn tính là online
        this.setIMGforUser(this); // Random avatar cho Bot luôn
    }
    // -----------------------------------------------------------

    /**
     * Constructor dùng khi ĐĂNG KÝ MỚI (LoginController)
     */
    public User(String email, String username, String password) {
        this.id = UUID.randomUUID().toString(); 
        this.email = email;
        this.username = username;
        this.password = password;
        
        this.setOnlineStatus(false);
        this.point = 0;              
        this.rank = "Tân Binh";      
        
        this.setIMGforUser(this);
    }

    // Hàm random avatar
    public void setIMGforUser(User user) {
        if (user.imageAvatar == null || user.imageAvatar.isEmpty()) {
            int randomIndex = new Random().nextInt(AVATAR_LIST.length);
            user.imageAvatar = AVATAR_LIST[randomIndex];
        }
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; } // BotRepository sẽ dùng hàm này để gán ID từ file

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImageAvatar() { return imageAvatar; }
    public void setImageAvatar(String imageAvatar) { this.imageAvatar = imageAvatar; }

    public boolean isOnlineStatus() { return onlineStatus; }
    public void setOnlineStatus(boolean onlineStatus) { this.onlineStatus = onlineStatus; }

    public int getPoint() { return point; }
    public void setPoint(int point) { this.point = point; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    // --- JSON Converter ---
    public String toJson() {
        return String.format(
            "{" +
            "\"id\":\"%s\"," +
            "\"email\":\"%s\"," +
            "\"username\":\"%s\"," +
            "\"password\":\"%s\"," +
            "\"image_avatar\":\"%s\"," +
            "\"online_status\":%b," +
            "\"point\":%d," +
            "\"rank\":\"%s\"" +
            "}",
            id, email, username, password, imageAvatar, isOnlineStatus(), point, rank
        );
    }
    
    @Override
    public String toString() {
        return "User [id=" + id + ", email=" + email + ", username=" + username + "]";
    }
}