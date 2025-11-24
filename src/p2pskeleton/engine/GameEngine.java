package p2pskeleton.engine;

import p2pskeleton.protocol.GameState;
import p2pskeleton.protocol.GameAction;

public class GameEngine {
    private GameState state = new GameState();

    public GameEngine() {}

    public void applyAction(GameAction action, String playerId) {
        // TODO: xử lý luật UNO hoặc game bạn thiết kế
        // Ví dụ:
        // - Validate lượt
        // - Update state
        // - Tạo JSON để đồng bộ
    }

    public GameState getState() {
        return state;
    }
}

