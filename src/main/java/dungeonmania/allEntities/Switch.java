package dungeonmania.allEntities;

import dungeonmania.Entity;
import dungeonmania.util.Position;


public class Switch extends Entity {

    private boolean isActivated;

    public Switch(String id, Position position) {
        super(id, new Position(position.getX(), position.getY(), -1), "switch");
        this.isActivated = false;
    }

    public boolean getStatus() {
        return this.isActivated;
    }

    public void setStatus(boolean status) {
        this.isActivated = status;
    }
}
