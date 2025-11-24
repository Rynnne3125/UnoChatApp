package p2pskeleton.room;

import java.util.*;

public class Room {
    public String roomId;
    public String hostPeer;
    public List<PlayerInfo> players = new ArrayList<>();

    public Room(String id, String hostPeer) {
        this.roomId = id;
        this.hostPeer = hostPeer;
    }
}

