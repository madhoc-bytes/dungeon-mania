package dungeonmania.allEntities;

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
	private int invincibleTickDuration;

    public Player(String id, Position position, String gameMode) {
        super(id, position, "player");
		this.attack = 2;
		// this.health = 100;
		this.visible = true;
		this.invincibleTickDuration = 0;
		if (gameMode.equals("Peaceful") || gameMode.equals("Standard")) {
			this.health = 100;
		} else if (gameMode.equals("Hard")) {
			this.health = 60;
		}
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

	public Direction getCurrentDir() {
		return currentDir;
	}

    public boolean isVisible() {
        return visible;
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
		
		if (entity instanceof Wall) {
			return false;
		} else if (entity instanceof BombStatic) {
			return false;
		} else if (entity instanceof ZombieToastSpawner) {
			return false;
		} else if (entity instanceof Door) {
			Door door = (Door) entity;
			if (door.isOpen()) {
				return true;
			}
			if (haveKey) {
				CollectableEntity removed = null;
				for (CollectableEntity item : dungeon.getInventory()) {
					if (item instanceof Key) {
						removed = item;
						break;
					}
				}
				dungeon.getInventory().remove(removed);
				haveKey = false;
				door.unlock();
				return true;
			} else {
				return false;
			}
			
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
						// Find position of p2
						// Move in direciton of currDir
						Entity nextTo = dungeon.getEntity(portal2.getPosition().translateBy(currentDir));
						return collide(nextTo, dungeon);
					}
				}
			}
			return false;
		}

		// PICKUP ITEM
		if (entity instanceof CollectableEntity) {
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
			} 
			// Remove entity
			dungeon.removeEntity(entity);

			// Add to player inv
			dungeon.addItemToInventory((CollectableEntity)entity);

			return true;
		}

		// BATTLE
		if (entity instanceof MovingEntity) {

			if (dungeon.getMode().enemyAttack()) {
				Battle.battle(entity, dungeon);
			}
			//One Ring Spawning
			// OneRing ring = new OneRing(getPosition(), 0);
			// if (ring.doesSpawn()) {
			// 	int check = 0;
			// 	for (CollectibleEntity item : dungeon.getInventory()) {
			// 		if (item instanceof OneRing) {
			// 			check = 1;
			// 		}
			// 	}
			// 	if (check == 0) {
			// 		int id = dungeon.getHistoricalEntCount();
			// 		ring.setId(String.valueOf(id));
			// 		dungeon.setHistoricalEntCount(id++);
			// 		dungeon.addItemToInventory(ring);
			// 	}
			// }
		}
		

		return true;
	}


}
