package p2pskeleton.protocol;

public class GameAction {
    public String actionType;
    public String data;

    public GameAction() {}
    public GameAction(String actionType, String data) {
        this.actionType = actionType;
        this.data = data;
    }
}
