package p2pskeleton.protocol;

import p2pskeleton.core.Message;

public interface ProtocolHandler {
    void onMessage(Message message);
}

