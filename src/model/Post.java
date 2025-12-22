package model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Post {
    private String id;
    private String authorUsername;
    private String authorAvatar;
    private String content;
    private String mediaUrl;
    private String mediaType; // "IMAGE", "VIDEO", "NONE"
    private int likeCount;
    private List<String> comments;
    private Long timestamp; // Thời gian tạo post

    // Constructor đầy đủ
    public Post(String authorUsername, String authorAvatar, String content, String mediaUrl, String mediaType) {
        this.id = UUID.randomUUID().toString();
        this.authorUsername = authorUsername;
        this.authorAvatar = authorAvatar;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType != null ? mediaType : "NONE";
        this.likeCount = 0;
        this.comments = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor rỗng cho Gson
    public Post() {
        this.comments = new ArrayList<>();
        this.likeCount = 0;
    }

    // Getters
    public String getId() { return id; }
    public String getAuthorUsername() { return authorUsername; }
    public String getAuthorAvatar() { return authorAvatar; }
    public String getContent() { return content; }
    public String getMediaUrl() { return mediaUrl; }
    public String getMediaType() { return mediaType; }
    public int getLikeCount() { return likeCount; }
    public List<String> getComments() { return comments; }
    public Long getTimestamp() { return timestamp; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }
    public void setContent(String content) { this.content = content; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setComments(List<String> comments) { this.comments = comments; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    // Methods
    public void addLike() { this.likeCount++; }
    public void addComment(String cmt) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(cmt);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", author='" + authorUsername + '\'' +
                ", content='" + content + '\'' +
                ", likes=" + likeCount +
                '}';
    }
}