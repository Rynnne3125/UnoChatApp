package p2pskeleton.core;

import java.net.ServerSocket;
import java.net.Socket;
import p2pskeleton.utils.LoggerUtil;

public class PeerServer {
    private ServerSocket server;
    private Peer peer;

    public PeerServer(int port, Peer peer) throws Exception {
        this.peer = peer;
        server = new ServerSocket(port);

        new Thread(this::acceptLoop).start();
    }

    private void acceptLoop() {
        try {
            while (true) {
                Socket client = server.accept();
                peer.addConnection(client);
                LoggerUtil.info("Peer connected: " + client.getInetAddress());
            }
        } catch (Exception e) {
            LoggerUtil.error("Server stopped: " + e.getMessage());
        }
    }
}
