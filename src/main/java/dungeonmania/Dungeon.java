package dungeonmania;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import dungeonmania.allEntities.Player;
import dungeonmania.util.*;

import java.util.HashMap;

public class Dungeon {

	private int id;
	private String name;
	private List<Entity> inventory;
    private Map<String, Entity> entities;
    private String gameMode;
    private String goals;
	private int historicalEntCount;


    public Dungeon(int id, String name, Map<String, Entity> entities, String gameMode, String goals) {
		this.id = id;
		this.name = name;	
		this.inventory = new ArrayList<>();	
        this.entities = entities;
        this.gameMode = gameMode;
        this.goals = goals;
		this.historicalEntCount = entities.size();
    }

    public Map<String, Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity newEntity) {
        this.entities.put((String.valueOf(historicalEntCount)), newEntity);
		historicalEntCount++;
    }

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Entity> getInventory() {
		return inventory;
	}

    public String getGameMode() {
        return gameMode;
    }

    public String getGoals() {
        return goals;
    }

	public Entity getEntity(String id) {
		return entities.get(id);
	}

	// Check if type exists regardless of position
	public boolean entityExists(String type) {
		return entities.keySet().contains(type);
	}

	// Check if something exists in position
	public boolean entityExists(Position position) {
		for (Entity ent : entities.values()) {
			if (ent.getPosition().equals(position)) {
				return true;
			}
		}
		return false;
	}

	// Check if type exists in position
	public boolean entityExists(String type, Position position) {		
		for (Entity ent : entities.values()) {
			if (ent.getPosition().equals(position) && ent.getType().equals(type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return player's curr position in the dungeon
	 */
	public Position getPlayerPosition() {
		for (Map.Entry<String, Entity> entry : entities.entrySet()) {
			Entity currentEntity = entry.getValue();
			if (currentEntity instanceof Player) {
				return currentEntity.getPosition();
			}		
		}
		return null;
	}

	/**
	 * @return the player entity of a dungeon
	 */
	public Player getPlayer() {
		for (Map.Entry<String, Entity> entry : entities.entrySet()) {
			Entity currentEntity = entry.getValue();
			if (currentEntity instanceof Player) {
				return (Player)currentEntity;
			}		
		}
		return null;
	}

	public List<String> getBuildables() {
		List<String> result = new ArrayList<String>();

		int wood = 0;
		int arrow = 0;
		int treasure = 0;
		int key = 0;

		for (Entity item : inventory) {
			if (item.getType().equals("wood")) {
				wood++;
			}

			if (item.getType().equals("arrow")) {
				arrow++;
			}

			if (item.getType().equals("treasure")) {
				treasure++;
			}

			if (item.getType().equals("key")) {
				key++;
			}
		}

		if (wood >= 1 && arrow >= 3) {
			result.add("bow");
		}

		if (wood >= 2 && (treasure >= 1 || key >= 1)) {
			result.add("shield");
		}

		return result;
	}

	public boolean equals(Object obj) {
		if (this == null || obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		} 

		if (getClass() != obj.getClass()) {
			return false;
		}

		Dungeon other = (Dungeon) obj;

		if (id != other.getId()) {
			return false;
		}
		
		if (name == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!name.equals(other.getName())) {
			return false;
		}

		if (inventory == null) {
			if (other.getInventory() != null) {
				return false;
			}
		} else if (!inventory.equals(other.getInventory())) {
			return false;
		}

		if (entities == null) {
			if (other.getEntities() != null) {
				return false;
			}
		} else if (!entities.equals(other.getEntities())) {
			return false;
		}

		if (gameMode == null) {
			if (other.getGameMode() != null) {
				return false;
			}
		} else if (!gameMode.equals(other.getGameMode())) {
			return false;
		}

		if (goals == null) {
			if (other.getGoals() != null) {
				return false;
			}
		} else if (!goals.equals(other.getGoals())) {
			return false;
		}
		return true;
	}
}