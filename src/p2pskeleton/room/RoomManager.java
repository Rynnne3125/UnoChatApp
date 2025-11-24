package p2pskeleton.room;

import java.util.*;
import p2pskeleton.protocol.GameState;

public class RoomManager {
    private Map<String, Room> rooms = new HashMap<>();

    public Room createRoom(String hostPeer) {
        Room r = new Room(UUID.randomUUID().toString(), hostPeer);
        rooms.put(r.roomId, r);
        return r;
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }
}

