package p2pskeleton;

import p2pskeleton.core.*;
import p2pskeleton.protocol.*;
import p2pskeleton.utils.JsonUtil;

public class UNOGAME {
    public static void main(String[] args) throws Exception {

        ProtocolHandler handler = (Message msg) -> {
            System.out.println("Handle protocol: " + msg.type);
        };

        Peer peer = new Peer(9001, handler);

        // nếu muốn kết nối peer 2 → peer 1
        // peer.connectToPeer("localhost", 9001);

        System.out.println("Peer started with ID: " + peer.getPeerId());
    }
}

