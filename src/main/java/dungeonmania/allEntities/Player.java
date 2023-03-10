package dungeonmania.allEntities;

import java.util.ArrayList;
import java.util.List;

import dungeonmania.Battle;
import dungeonmania.CollectableEntity;
import dungeonmania.Dungeon;
import dungeonmania.Entity;
import dungeonmania.MovingEntity;
import dungeonmania.util.Direction;
import dungeonmania.util.Position;


public class Player extends Entity {
    private int health;
	private int attack;
    private boolean visible;
	private Direction currentDir;
	private boolean haveKey;
	private boolean hasSunStone;
	private int invincibleTickDuration;
	private final boolean enemyAttack;
	private final int initialHealth;
	private final int invincibleAmount;
	private final int initialAttack = 2;
	private List<String> controlled = new ArrayList<>();
	private List<Direction> traceList = new ArrayList<>();

	public Player(String id, Position position, int health, boolean enemyAttack, int invincibleAmount) {
        super(id, position, "player");
		this.attack = initialAttack;
		this.visible = true;
		this.invincibleTickDuration = 0;
		this.health = health;
		this.initialHealth = health;
		this.enemyAttack = enemyAttack;
		this.invincibleAmount = invincibleAmount;
    }

    public void setHealth(int newHealth) {
        health = newHealth;
    }

	public int getAttack() {
		return attack;
	}

	public void setAttack(int newAttack) {
        attack = newAttack;
    }

    public int getHealth() {
        return health;
    }

	public int getInitialHealth() {
		return initialHealth;
	}

	public Direction getCurrentDir() {
		return currentDir;
	}

	public int getInvincibleAmount() {
		return invincibleAmount;
	}

    public boolean isVisible() {
        return visible;
    }

	public boolean enemyAttack() {
		return enemyAttack;
	}

	public void setVisibility(boolean canBeSeen) {
		visible = canBeSeen;
	} 

	public int getInvincibleTickDuration() {
		return invincibleTickDuration; 
	}

	public void setInvincibleTickDuration(int durationInTicks) {
		invincibleTickDuration = (durationInTicks >= 0) ? durationInTicks : 0;
	}
	
	public void setCurrentDir(Direction currentDir) {
		this.currentDir = currentDir;
	}
	
	public void setHaveKey(boolean haveKey) {
		this.haveKey = haveKey;
	}

	public boolean getSunstoneStatus() {
		return hasSunStone;
	}

	public int getInitialAttack() {
		return initialAttack;
	}

	public List<String> getControlled() {
		return controlled;
	}

	public List<Direction> getTraceList() {
		return traceList;
	}

	public void addTrace(Direction direction) {
		this.traceList.add(direction);
	}

	public void setTraceList(List<Direction> traceList) {
		this.traceList = traceList;
	}

	public void act(Direction moveDir, Dungeon dungeon) {
		setCurrentDir(moveDir);
		
		// make sure invincibility wears off
		setInvincibleTickDuration(invincibleTickDuration - 1);

		// If there are mercs being controlled
		if (!controlled.isEmpty()) {
			for (Entity ent : dungeon.getEntities()) {
				if (ent instanceof Mercenary) {
					Mercenary merc = (Mercenary) ent;
					merc.sceptreTick(dungeon);
				}
			}
		}

		// move player
		move(dungeon, moveDir);
	
		// store the player's step
		addTrace(moveDir);		
	}

	public boolean shouldTimeTravel(Dungeon dungeon) {
		Position currPlayerPos = super.getPosition();
		List<Entity> entOnPlayerCell = dungeon.getEntitiesOnCell(currPlayerPos);
		for (Entity ent: entOnPlayerCell) {
			if (ent instanceof TimeTravellingPortal) {
				move(dungeon, currentDir);
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the player is able to collide with entity<p>
	 * Collide means if they are able to be on the same square<p>
	 * If the colliding entity is a MovableEntity, then a battle occurs
	 * 
	 * @param	entity	The entity in question
	 * @param	dungeon	The given dungeon where this collision is taking place
	 * @return	true - If player is able to collide<p>
	 * 			false - If player is not able to collide
	 */
	public boolean collide(Entity entity, Dungeon dungeon) {
		// If empty space
		if (entity == null) {
			return true;
		}
		
		if (entity instanceof Wall || entity instanceof ZombieToastSpawner) {
			return false;
		} if (entity instanceof Bomb) {
			Bomb bomb = (Bomb) entity;
			if (bomb.isActive()) {
				return false;
			} else {
				dungeon.removeEntity(entity);
				dungeon.addItemToInventory((CollectableEntity)entity);
			return true;
			}
		} else if (entity instanceof Door) {
			Door door = (Door) entity;
			if (door.isOpen() || hasSunStone) {
				return true;
			}
			if (haveKey) {
				CollectableEntity removed = null;
				for (CollectableEntity item : dungeon.getInventory()) {
					if (item instanceof Key) {
						Key key = (Key) item;
						if (key.getKey() == door.getKey()) {
							removed = item;
							break;
						} else {
							return false;
						}
						
					}
				}
				dungeon.getInventory().remove(removed);
				haveKey = false;
				door.unlock();
				return true;
			}

			return false;
			
		} else if (entity instanceof Boulder) {
			Boulder boulder = (Boulder) entity;

			Position newPos = boulder.getPosition().translateBy(currentDir);
			return boulder.collide(dungeon.getEntity(newPos));		

		} else if (entity instanceof Portal) {
			Portal portal1 = (Portal) entity;

			for (Entity currEnt : dungeon.getEntities()) {
				if (!(currEnt instanceof Portal)) {
					continue;
				}
				// Check if same entity
				if (!currEnt.getId().equals(portal1.getId())) {
					Portal portal2 = (Portal) currEnt;
					if (portal1.getColour().equals(portal2.getColour())) {
						Entity nextTo = dungeon.getEntity(portal2.getPosition().translateBy(currentDir));
						return collide(nextTo, dungeon);
					}
				}
			}
			return false;

		} else if (entity instanceof CollectableEntity) {
			// can't have 2 keys in inv
			if (entity instanceof Key) {
				if (haveKey) {
					return true;
				}			
				haveKey = true;
			} else if (entity instanceof OneRing) {
				for (CollectableEntity item : dungeon.getInventory()) {
					if (item.getType().equals("one_ring")) {
						return true;
					}
				}	
			} else if (entity instanceof SunStone) {
				hasSunStone = true;
			} else if (entity instanceof Anduril) {
				Anduril anduril = (Anduril) entity;
				attack *= anduril.getDmgMultiplier();
			}
			// Remove entity from map
			dungeon.removeEntity(entity);
			// Add to player inv
			dungeon.addItemToInventory((CollectableEntity)entity);

		} else if (entity instanceof MovingEntity) {
			int check = 0;
			if (entity instanceof Mercenary) {
				Mercenary merc = (Mercenary) entity;
				//Should not fight mercenary;
				if (merc.getIsAlly()) {
					check = 1;
				}
			}
			if (enemyAttack() && check == 0) {
				Battle.battle(entity, dungeon);
			}
		} else if (entity instanceof TimeTravellingPortal) {
			Position landPos = entity.getPosition().translateBy(currentDir);
			List<Entity> entsNext = dungeon.getEntitiesOnCell(landPos);
			boolean canLand = true;

			for (Entity ent : entsNext) {
				if (!collide(ent, dungeon)) {
					canLand = false;
					break;
				}
			} return canLand;
		}

		return true;
	}

	/**
	 * Move through a portal, if the current player is standing on a portal tile
	 * @param dungeon	Current dungeon of player
	 */
	public void portalMove(Dungeon dungeon) {
		Position pos = getPosition();
		Portal portal1 = (Portal) dungeon.getEntity("portal", pos);
		Position posPortal2 = new Position(0, 0);
		if (portal1 != null) {
			// Find other portal
			for (Entity currEnt : dungeon.getEntities()) {
				if (currEnt instanceof Portal) {
					Portal portal2 = (Portal) currEnt;
					if (portal2.getColour().equals(portal1.getColour()) && !portal2.equals(portal1)) {
						posPortal2 = portal2.getPosition();
						break;
					}
				}
			} setPosition(posPortal2.translateBy(getCurrentDir()));
		}
	}

	/**
	 * Translate a player by a "direction", if possible
	 * @param dungeon	Current dungeon of player
	 * @param direction	Desired direction of movement of player
	 */
	public void move(Dungeon dungeon, Direction direction) {
		// Check if the direction is able to be moved into
		Position newPos = getPosition().translateBy(direction);

		for (Entity entity : dungeon.getEntitiesOnCell(newPos)) {
			if (!collide(entity, dungeon)) {
				return;
			}
		}
		setPosition(newPos);
		setCurrentDir(direction);
		portalMove(dungeon);
	}
}