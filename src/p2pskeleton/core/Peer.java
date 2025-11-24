package p2pskeleton.core;

import java.net.Socket;
import java.util.*;

import p2pskeleton.protocol.ProtocolHandler;
import p2pskeleton.utils.LoggerUtil;

public class Peer {
    private String peerId = UUID.randomUUID().toString();
    private long seq = 0;
    private HashMap<String, PeerConnection> connections = new HashMap<>();
    private ProtocolHandler protocolHandler;

    public String getPeerId() { return peerId; }

    public Peer(int port, ProtocolHandler handler) throws Exception {
        this.protocolHandler = handler;
        new PeerServer(port, this);
    }

    public void addConnection(Socket socket) throws Exception {
        PeerConnection conn = new PeerConnection(socket, this);
        connections.put(socket.getRemoteSocketAddress().toString(), conn);
    }

    public void connectToPeer(String host, int port) throws Exception {
        Socket socket = new Socket(host, port);
        addConnection(socket);
    }

    public void broadcast(Message msg) {
        for (PeerConnection c : connections.values()) {
            c.send(msg);
        }
    }

    public void handleIncomingMessage(Message msg, PeerConnection conn) {
        LoggerUtil.info("RECV " + msg.type + " FROM " + msg.fromPeer);
        protocolHandler.onMessage(msg);
    }

    public void send(MessageType type, String room, String payload) {
        Message msg = new Message(type, peerId, seq++, room, payload);
        broadcast(msg);
    }
}
