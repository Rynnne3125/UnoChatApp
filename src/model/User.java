package model;

import java.util.Random;

import com.google.gson.annotations.SerializedName;

public class User {
	private String email;
    private String username;
    private String password;

    @SerializedName("image_avatar")
    private String imageAvatar = "";

    @SerializedName("online_status")
    private boolean onlineStatus;

    private int point;
    private String rank;


    public int getPoint() {
        return point;
    }
    public void setPoint(int point) {
        this.point = point;
    }

    public String getRank() {
        return rank;
    }
    public void setRank(String rank) {
        this.rank = rank;
    }

    // Danh sách ảnh avatar để random
    private static final String[] AVATAR_LIST = {
        "https://i.pinimg.com/736x/d5/c0/fc/d5c0fc0734cb465b16affe739be62c52.jpg",
        "https://cdn.dribbble.com/userupload/23839376/file/original-f6a79767815644ade14c04c8b7b80a9e.png",
        "https://img.freepik.com/premium-photo/vibrant-men-s-cricket-world-cup-2024-illustration-featuring-dynamic-cricket-illustration-with-fast-hits_719166-4508.jpg"
    };

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
        
        // --- Cấu hình mặc định ---
        this.setOnlineStatus(false); // Mặc định offline
        this.point = 0;            // Mặc định 0 điểm
        this.rank = "Tân Binh";    // Mặc định rank
        
    }
    public void setIMGforUser(User user) {
    	if(user.imageAvatar == null || user.imageAvatar.isEmpty()) {
        	// Random 1 trong 3 ảnh
            int randomIndex = new Random().nextInt(AVATAR_LIST.length);
            user.imageAvatar = AVATAR_LIST[randomIndex];
    	}
    }
    // --- Getters ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; } // Cần để check login\
    // --- JSON Converter ---
    // Mapping tên biến Java sang tên key JSON (snake_case)
    public String toJson() {
        return String.format(
            "{" +
            "\"email\":\"%s\"," +
            "\"username\":\"%s\"," +
            "\"password\":\"%s\"," +
            "\"image_avatar\":\"%s\"," +
            "\"online_status\":%b," +
            "\"point\":%d," +
            "\"rank\":\"%s\"" +
            "}",
            email, username, password, imageAvatar, isOnlineStatus(), point, rank
        );
    }

	public String getImageAvatar() {
		// TODO Auto-generated method stub
		return imageAvatar;
	}
	public boolean isOnlineStatus() {
		return onlineStatus;
	}
	public void setOnlineStatus(boolean onlineStatus) {
		this.onlineStatus = onlineStatus;
	}
	public String getEmail() {
		// TODO Auto-generated method stub
		return email;
	}

}