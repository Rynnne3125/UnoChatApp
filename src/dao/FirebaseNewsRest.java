package dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Post;
import model.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FirebaseNewsRest {

    // QUAN TRỌNG: Project ID của bạn
    private static final String BASE_URL = "https://unofirebase-19555-default-rtdb.firebaseio.com/";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ==================== POSTS (Updated) ====================

    /**
     * Tạo bài post mới.
     * Lưu ý: Khởi tạo likeCount = 0 và comments = empty tại các node riêng biệt.
     */
    public static String createPost(Post post) {
        try {
            // 1. Lưu thông tin cơ bản vào node /posts
            String postPath = BASE_URL + "posts/" + post.getId() + ".json";
            String jsonBody = gson.toJson(post);
            String response = sendRequest(postPath, "PUT", jsonBody);

            // 2. Khởi tạo likeCount = 0 tại node /likeCount
            String likePath = BASE_URL + "likeCount/" + post.getId() + ".json";
            sendRequest(likePath, "PUT", "0");

            // 3. Khởi tạo comments rỗng tại node /comments (Optional, Firebase tự tạo khi có data)
            // Nhưng để chắc chắn, ta có thể bỏ qua bước này, comment sẽ được tạo khi user comment đầu tiên.

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy tất cả bài posts.
     * LOGIC MỚI: Phải lấy Post, LikeCount và Comments từ 3 nơi rồi ghép lại.
     */
    public static List<Post> getAllPosts() {
        List<Post> postsList = new ArrayList<>();
        try {
            // 1. Lấy thông tin Posts cơ bản
            String postsJson = sendRequest(BASE_URL + "posts.json", "GET", null);
            if (postsJson == null || postsJson.equals("null")) return postsList;

            // 2. Lấy toàn bộ Like Counts (Map<PostId, Integer>)
            String likesJson = sendRequest(BASE_URL + "likeCount.json", "GET", null);
            
            // 3. Lấy toàn bộ Comments (Map<PostId, List<String>>)
            String commentsJson = sendRequest(BASE_URL + "comments.json", "GET", null);

            // --- Parse Data ---
            Type postsType = new TypeToken<Map<String, Post>>(){}.getType();
            Map<String, Post> postsMap = gson.fromJson(postsJson, postsType);

            Type likesType = new TypeToken<Map<String, Integer>>(){}.getType();
            Map<String, Integer> likesMap = new HashMap<>();
            if (likesJson != null && !likesJson.equals("null")) {
                likesMap = gson.fromJson(likesJson, likesType);
            }

            Type commentsType = new TypeToken<Map<String, List<String>>>(){}.getType();
            Map<String, List<String>> commentsMap = new HashMap<>();
            if (commentsJson != null && !commentsJson.equals("null")) {
                commentsMap = gson.fromJson(commentsJson, commentsType);
            }

            // --- Merge Data (Ghép Like và Comment vào Post) ---
            if (postsMap != null) {
                for (Post p : postsMap.values()) {
                    // Set Likes
                    if (likesMap.containsKey(p.getId())) {
                        p.setLikeCount(likesMap.get(p.getId()));
                    } else {
                        p.setLikeCount(0);
                    }

                    // Set Comments
                    if (commentsMap.containsKey(p.getId())) {
                        p.setComments(commentsMap.get(p.getId()));
                    } else {
                        p.setComments(new ArrayList<>());
                    }
                    
                    postsList.add(p);
                }
            }

            // Sắp xếp theo timestamp giảm dần
            postsList.sort((p1, p2) -> {
                Long t1 = p1.getTimestamp();
                Long t2 = p2.getTimestamp();
                if (t1 == null && t2 == null) return 0;
                if (t1 == null) return 1;
                if (t2 == null) return -1;
                return t2.compareTo(t1);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return postsList;
    }

    /**
     * Lấy post theo ID (Cũng cần merge data)
     */
    public static Post getPostById(String postId) {
        try {
            // 1. Lấy Post Info
            String jsonResponse = sendRequest(BASE_URL + "posts/" + postId + ".json", "GET", null);
            if (jsonResponse == null || jsonResponse.equals("null")) return null;
            Post post = gson.fromJson(jsonResponse, Post.class);

            // 2. Lấy Like Count từ node riêng
            String likeJson = sendRequest(BASE_URL + "likeCount/" + postId + ".json", "GET", null);
            if (likeJson != null && !likeJson.equals("null")) {
                post.setLikeCount(Integer.parseInt(likeJson));
            }

            // 3. Lấy Comments từ node riêng
            String commentJson = sendRequest(BASE_URL + "comments/" + postId + ".json", "GET", null);
            if (commentJson != null && !commentJson.equals("null")) {
                Type listType = new TypeToken<List<String>>(){}.getType();
                List<String> comments = gson.fromJson(commentJson, listType);
                post.setComments(comments);
            }

            return post;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật số lượng like => Ghi vào node /likeCount
     */
    public static String updatePostLikes(String postId, int newLikeCount) {
        try {
            // URL MỚI: likeCount/{postId}
            String path = BASE_URL + "likeCount/" + postId + ".json";
            String jsonBody = gson.toJson(newLikeCount);
            return sendRequest(path, "PUT", jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Thêm comment => Ghi vào node /comments
     */
    public static String addCommentToPost(String postId, String comment) {
        try {
            // 1. Lấy danh sách comment hiện tại từ node /comments/{postId}
            String pathGet = BASE_URL + "comments/" + postId + ".json";
            String currentCommentsJson = sendRequest(pathGet, "GET", null);
            
            List<String> comments = new ArrayList<>();
            if (currentCommentsJson != null && !currentCommentsJson.equals("null")) {
                Type listType = new TypeToken<List<String>>(){}.getType();
                comments = gson.fromJson(currentCommentsJson, listType);
            }

            // 2. Thêm comment mới
            comments.add(comment);

            // 3. Update lại node /comments/{postId}
            String jsonBody = gson.toJson(comments);
            return sendRequest(pathGet, "PUT", jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Xóa post => Phải xóa ở cả 3 nơi (posts, likeCount, comments) để sạch data
     */
    public static boolean deletePost(String postId) {
        try {
            sendRequest(BASE_URL + "posts/" + postId + ".json", "DELETE", null);
            sendRequest(BASE_URL + "likeCount/" + postId + ".json", "DELETE", null);
            sendRequest(BASE_URL + "comments/" + postId + ".json", "DELETE", null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== USERS (Giữ nguyên) ====================
    // User info vẫn lưu ở /users vì đây là profile chính

    public static User findUser(String username) {
        try {
            String jsonResponse = sendRequest(BASE_URL + "users/" + username + ".json", "GET", null);
            if (jsonResponse != null && !jsonResponse.equals("null")) {
                User user = gson.fromJson(jsonResponse, User.class);
                
                if (user != null && (user.getUsername() == null || user.getUsername().isEmpty())) {
                    user.setUsername(username);
                }
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String createOrUpdateUser(User user) {
        try {
            String path = BASE_URL + "users/" + user.getUsername() + ".json";
            String jsonBody = gson.toJson(user);
            return sendRequest(path, "PUT", jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== FRIEND REQUESTS (Updated) ====================

    /**
     * Gửi friend request => Ghi vào node /friends_request
     */
    public static String sendFriendRequest(String fromUser, String toUser) {
        try {
            // URL MỚI: friends_request/{toUser}/{fromUser}
            String path = BASE_URL + "friends_request/" + toUser + "/" + fromUser + ".json";
            return sendRequest(path, "PUT", gson.toJson("PENDING"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy requests => Đọc từ node /friends_request/{username}
     */
    public static Map<String, String> getFriendRequests(String username) {
        try {
            // URL MỚI: friends_request/{username}
            String jsonResponse = sendRequest(BASE_URL + "friends_request/" + username + ".json", "GET", null);
            if (jsonResponse != null && !jsonResponse.equals("null")) {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                return gson.fromJson(jsonResponse, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    /**
     * Chấp nhận bạn bè => Xóa ở friends_request, thêm vào node /friends
     */
    public static String acceptFriendRequest(String username, String friendUsername) {
        try {
            // 1. Xóa request ở friends_request
            String deletePath = BASE_URL + "friends_request/" + username + "/" + friendUsername + ".json";
            sendRequest(deletePath, "DELETE", null);

            // 2. Thêm vào node /friends (quan hệ 2 chiều)
            // URL MỚI: friends/{user}/{friend}
            String path1 = BASE_URL + "friends/" + username + "/" + friendUsername + ".json";
            String path2 = BASE_URL + "friends/" + friendUsername + "/" + username + ".json";
            
            sendRequest(path1, "PUT", gson.toJson(true));
            return sendRequest(path2, "PUT", gson.toJson(true));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Từ chối kết bạn => Xóa ở node /friends_request
     */
    public static String declineFriendRequest(String username, String friendUsername) {
        try {
            // URL MỚI
            String path = BASE_URL + "friends_request/" + username + "/" + friendUsername + ".json";
            return sendRequest(path, "DELETE", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lấy danh sách bạn bè => Đọc từ node /friends/{username}
     */
    /**
     * Lấy danh sách bạn bè với đầy đủ thông tin User (bao gồm online status)
     */
    public static List<User> getFriendsList(String username) {
        List<User> friendsList = new ArrayList<>();
        try {
            // 1. Lấy danh sách username của friends
            String jsonResponse = sendRequest(BASE_URL + "friends/" + username + ".json", "GET", null);
            if (jsonResponse != null && !jsonResponse.equals("null")) {
                Type type = new TypeToken<Map<String, Boolean>>(){}.getType();
                Map<String, Boolean> friendsMap = gson.fromJson(jsonResponse, type);
                
                if (friendsMap != null) {
                    // 2. Với mỗi friend username, lấy thông tin User đầy đủ
                    for (String friendUsername : friendsMap.keySet()) {
                        User friend = findUser(friendUsername);
                        if (friend != null) {
                            friendsList.add(friend);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return friendsList;
    }
    
    /**
     * Hủy kết bạn - Xóa friend ở cả 2 phía (quan hệ 2 chiều)
     */
    public static boolean unfriend(String username, String friendUsername) {
        try {
            // Xóa ở cả 2 phía
            String path1 = BASE_URL + "friends/" + username + "/" + friendUsername + ".json";
            String path2 = BASE_URL + "friends/" + friendUsername + "/" + username + ".json";
            
            sendRequest(path1, "DELETE", null);
            sendRequest(path2, "DELETE", null);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== HELPER METHOD (Giữ nguyên) ====================

    private static String sendRequest(String urlStr, String method, String jsonBody) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);
            
            if (jsonBody != null) {
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            }

            int responseCode = conn.getResponseCode();
            
            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            if (responseCode >= 200 && responseCode < 300) {
                return response.toString();
            } else {
                System.err.println("HTTP Error " + responseCode + ": " + response.toString());
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}