package p2pskeleton.core;
public class Message {
    public MessageType type;
    public String fromPeer;
    public long seq;
    public String roomId;
    public String payload;

    public Message() {}

    public Message(MessageType type, String fromPeer, long seq, String roomId, String payload) {
        this.type = type;
        this.fromPeer = fromPeer;
        this.seq = seq;
        this.roomId = roomId;
        this.payload = payload;
    }
}

