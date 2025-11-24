package com.p2pframework.core;

import java.io.*;
import java.net.Socket;
import com.p2pframework.utils.JsonUtil;
import com.p2pframework.utils.LoggerUtil;

public class PeerConnection {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Peer peer;

    public PeerConnection(Socket socket, Peer peer) throws IOException {
        this.socket = socket;
        this.peer = peer;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    Message msg = JsonUtil.fromJson(line, Message.class);
                    peer.handleIncomingMessage(msg, this);
                }
            } catch (Exception e) {
                LoggerUtil.error("Connection closed: " + e.getMessage());
            }
        }).start();
    }

    public void send(Message msg) {
        out.println(JsonUtil.toJson(msg));
    }
}
