package dungeonmania;

import dungeonmania.exceptions.InvalidActionException;
import dungeonmania.response.models.DungeonResponse;
import dungeonmania.response.models.EntityResponse;
import dungeonmania.response.models.ItemResponse;
import dungeonmania.util.Direction;
import dungeonmania.util.FileLoader;
import dungeonmania.util.Position;
import dungeonmania.allEntities.*;
import dungeonmania.GameInOut;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;


import java.util.HashMap;

public class DungeonManiaController {

	/**
	 * ArrayList games: each game is stored as a map of existing entities, with their unique id as the key (stored as an int).
	 */
	private List<Dungeon> games =  new ArrayList<>();
	private int lastUsedDungeonId = 0;

	private Dungeon currentDungeon;

    public DungeonManiaController() {

	}


	public String getSkin() {
		return "default";		
	}

	public String getLocalisation() {
		return "en_US";
	
	}

	public List<String> getGameModes() {
		return Arrays.asList("Standard", "Peaceful", "Hard");
	}

	/**
	 * /dungeons
	 * 
	 * Done for you.
	*/
	public static List<String> dungeons() {
		try {
			return FileLoader.listFileNamesInResourceDirectory("/dungeons");
		} catch (IOException e) {
			return new ArrayList<>();
		}
	}

	public static List<String> getDungeons() {

		String[] dungeons;

        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        File f = new File("src/main/resources/dungeons");

        // Populates the array with names of files and directories
        dungeons = f.list();

		List<String> returnList = new ArrayList<>();

        // Put every file name into a list
        for (String dungeonFile : dungeons) {
            returnList.add(dungeonFile.replace(".json", ""));
		}

		return returnList;
	}

	/**
	 * Create a new game, and store it into a file inside /resources/savedGames
	 * @param dungeonName		fileName of the dungeon
	 * @param gameMode			gameMode of the dungeon (Peaceful, Standard or Hard)
	 * @return DungeonResponse	the dungeon which is being created
	 * @throws IllegalArgumentException
	 */
	public DungeonResponse newGame(String dungeonName, String gameMode) throws IllegalArgumentException {
		checkValidNewGame(dungeonName, gameMode);
		Dungeon newDungeon = jsonExporter.makeDungeon(lastUsedDungeonId, dungeonName + ".json", gameMode);
				
		List<EntityResponse> entities = new ArrayList<EntityResponse>();
		for (Map.Entry<String, Entity> entry : newDungeon.getEntities().entrySet()) {
			Entity currentEntity = entry.getValue();
			EntityResponse er = new EntityResponse(entry.getKey(), currentEntity.getType(), currentEntity.getPosition(), currentEntity.isInteractable());
			entities.add(er);
		}

		// Check if switch is coincided with boulder
		for (Map.Entry<String, Entity> entry : newDungeon.getEntities().entrySet()) {
			Entity currentEntity = entry.getValue();
			if (currentEntity instanceof Switch) {
				Position pos = currentEntity.getPosition();
				Position newPos = new Position(pos.getX(), pos.getY(), 0);
				Boulder boulder = (Boulder) newDungeon.getEntity("boulder", newPos);
				if (boulder != null) {
					Switch sw = (Switch) currentEntity;
					sw.setStatus(true);
				}
				
			}
		}
		DungeonResponse result = new DungeonResponse(
			String.valueOf(newDungeon.getId()), 
			newDungeon.getName(), 
			entities, 
			new ArrayList<ItemResponse>(), 
			newDungeon.getBuildables(),             
			newDungeon.getGoals() 
		);

		lastUsedDungeonId++;

		currentDungeon = newDungeon;
		games.add(newDungeon);
		return result;
	}
		
	/**
	 * Gets the dungeon from dungeonId and returns a DungeonResponse class
	 * @param dungeonId		- Identifier of the dungeon in the list in controller
	 * @return DungeonResponse
	 */
	public DungeonResponse getDungeonInfo(int dungeonId) {
		Dungeon target = null;
		for (Dungeon dungeon : games) {
			if (dungeon.getId() == dungeonId) {
				target = dungeon;
			}
		}

		List<EntityResponse> entities = new ArrayList<EntityResponse>();
		for (Map.Entry<String, Entity> entry : target.getEntities().entrySet()) {
			Entity currentEntity = entry.getValue();
			EntityResponse er = new EntityResponse(entry.getKey(), currentEntity.getType(), currentEntity.getPosition(), currentEntity.isInteractable());
			entities.add(er);
		}

		List<ItemResponse> inventory = new ArrayList<ItemResponse>();

		for (CollectibleEntity collectibleEntity : target.getInventory()) {
			inventory.add(new ItemResponse(collectibleEntity.getId(), collectibleEntity.getType()));
		}
		
		return new DungeonResponse(
			String.valueOf(target.getId()), 
			target.getName(), 
			entities, 
			inventory, 
			target.getBuildables(),             
			target.getGoals()
		);
	}
	
	/**
	 * Checks if dungeonName is a real file
	 * Checks if gameMode is Peaceful, Standard or Hard
	 * @param dungeonName	File name of desired file
	 * @param gameMode		Desired gamemode of dungeon
	 * @throws IllegalArgumentException	If not a real file, or not a real gamemode
	 */
	public void checkValidNewGame(String dungeonName, String gameMode) throws IllegalArgumentException {
		boolean gameExists = false;
		if (getDungeons().contains(dungeonName)) {
			gameExists = true;
		}
		
		if (gameExists == false) {
			throw new IllegalArgumentException("Invalid Dungeon Map Passed; Requested Dungeon Does Not Exist");
		}

		if (!this.getGameModes().contains(gameMode)) {
			throw new IllegalArgumentException("Invalid Game Mode Passed; Supported Game Modes: Standard, Peaceful, Hard");
		}
	}
	
	public Dungeon getDungeon(int dungeonId) {
		return games.get(dungeonId);
	}

	/**
	 * Save the game into a file in /resources/savedGames
	 * @throws IllegalArgumentException	If the given file name is not a real file
	 * @return	DungeonResponse
	 */
	public DungeonResponse saveGame(String name) throws IllegalArgumentException {
		String feed = name.replaceFirst(".json", "");
		System.out.println(feed);
		String path = ("src/main/resources/savedGames/" + feed + ".json"); 

		try {
			GameInOut.toJSON(path, currentDungeon);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return getDungeonInfo(currentDungeon.getId());
	}

	public DungeonResponse loadGame(String name) throws IllegalArgumentException {
		checkValidLoadGame(name);
		String feed = name.replaceFirst(".json", "");
		String newFeed = feed.split("-")[0];
		String fileName = (newFeed + ".json"); 

		try {
			currentDungeon = GameInOut.fromJSON(fileName, feed, lastUsedDungeonId);
			System.out.println(currentDungeon);
			setLastUsedDungeonId(getLastUsedDungeonId() + 1);
			games.add(currentDungeon);
			return getDungeonInfo(currentDungeon.getId());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Checks if the file name is a valid file name for loading
	 * @param name	file name
	 */
	public void checkValidLoadGame(String name) throws IllegalArgumentException {
		if (!allGames().contains(name)) {
			throw new IllegalArgumentException("Invalid Dungeon Name Passed; Requested Dungeon Cannot Be Loaded As It Does Not Exist");
		}
	}


	public List<String> allGames() {
		String[] games;
		// Creates a new File instance by converting the given pathname string
		// into an abstract pathname
		File f = new File("src/main/resources/savedGames");

		// Populates the array with names of files and directories
		games = f.list();
		List<String> gamesList = new ArrayList<>();

		// Put every file name into a list
		for (String gameFile : games) {
			gamesList.add(gameFile.replace(".json", ""));
		}

		return gamesList;
	}

	/**
	 * Go to next tick in give dungeon <p>
	 * Checks and uses itemUsed if it is valid <p>
	 * Checks the desired movement for Player<p>
	 * 			- Checks if able to move there<p>
	 * 			- Checks if there is a boulder, then move the boulder<p>
	 * Checks and moves all the MovableEntities
	 * 			- Checks if they will battle with Player
	 * 			- Checks if collideable with desired direction
	 * @param itemUsed
	 * @param movementDirection
	 * @return
	 * @throws IllegalArgumentException
	 * @throws InvalidActionException
	 */
	public DungeonResponse tick(String itemUsed, Direction movementDirection) throws IllegalArgumentException, InvalidActionException {
		checkValidTick(itemUsed);

		// Use item
		currentDungeon.useItem(itemUsed);
		
		// First tick of game, some actions to do
		if (currentDungeon.getTickNumber() == 0) {
			// If player exists
			if (currentDungeon.getPlayer() != null) {
				currentDungeon.setSpawnpoint(currentDungeon.getPlayerPosition());
			}
		}

		// Spawn in new mercenary after 10 ticks
		if (currentDungeon.getTickNumber() % 10 == 0 && currentDungeon.getTickNumber() > 0) {
			// If there is a spawnpoint
			if (currentDungeon.getSpawnpoint() != null) {
				// Merc spawn every 10 ticks
				int newId = currentDungeon.getHistoricalEntCount();
				Mercenary merc = new Mercenary(String.valueOf(newId), currentDungeon.getSpawnpoint());
				currentDungeon.addEntity(merc);
				// currentDungeon.setHistoricalEntCount(newId + 1);
			}
		}

		List<ZombieToastSpawner> spawners = new ArrayList<ZombieToastSpawner>();
		for (Map.Entry<String, Entity> entry : currentDungeon.getEntities().entrySet()) {
			Entity currentEntity = entry.getValue();
			if (currentEntity.getType().equals("zombie_toast_spawner")) {
				ZombieToastSpawner foundSpawner = (ZombieToastSpawner)currentEntity;
				spawners.add(foundSpawner);
			}
		}

		Move moveStrategy = null;
		currentDungeon.tickOne();
		
		moveStrategy = new PlayerMove();
		// Move player
		if (currentDungeon.getPlayer() != null) {
			currentDungeon.getPlayer().setCurrentDir(movementDirection);
			// make sure invincibility wears off
			int invicibleTicksLeft = currentDungeon.getPlayer().getInvincibleTickDuration();
			currentDungeon.getPlayer().setInvincibleTickDuration(invicibleTicksLeft - 1);
			moveStrategy.move(currentDungeon.getPlayer(), currentDungeon, movementDirection);			
		}
		
		Switch switchFlick = null;
		boolean switchOn = false;

		Map<String, Entity> tempEnts = new HashMap<>();
		for (Map.Entry<String, Entity> entry : currentDungeon.getEntities().entrySet()) {
			tempEnts.put(entry.getKey(), entry.getValue());
		}
		// Move everything else
		for (Map.Entry<String, Entity> entry : tempEnts.entrySet()) {
			Entity currentEntity = entry.getValue();
			if (currentEntity instanceof Spider) {
				moveStrategy = new SpiderMove();
				moveStrategy.move(currentEntity, currentDungeon);
			} else if (currentEntity instanceof Mercenary) {
				moveStrategy = new MercenaryMove();
				moveStrategy.move(currentEntity, currentDungeon);
			} else if (currentEntity instanceof Boulder) {
				if (!currentEntity.getPosition().equals(currentDungeon.getPlayerPosition())) {
					continue;
				}
				moveStrategy = new StandardMove();
				// Get position of switch, layer -1
				Position prevPos = currentEntity.getPosition();
				Position prevPosSwitch = new Position(prevPos.getX(), prevPos.getY(), -1);
				moveStrategy.move(currentEntity, currentDungeon, movementDirection);

				Position currPos = currentEntity.getPosition();
				Position currPosSwitch = new Position(currPos.getX(), currPos.getY(), -1);

				// Check if switch is being activated
				if (currentDungeon.entityExists("switch", currPosSwitch)) {
					switchFlick = (Switch) currentDungeon.getEntity("switch", currPosSwitch);
					switchOn = true;
				}
				// Check if switch is being deactivated
				if (currentDungeon.entityExists("switch", prevPosSwitch)) {
					switchFlick = (Switch) currentDungeon.getEntity("switch", prevPosSwitch);
					switchOn = false;
				}

			} else if (currentEntity instanceof ZombieToast) {
				moveStrategy = new StandardMove();
				Random random = new Random();
				int dir = random.nextInt(4);
				Direction currDir = Direction.NONE;
				switch (dir) {
					case 0:
						currDir = Direction.UP;
						break;
					case 1:
						currDir = Direction.DOWN;
						break;
					case 2:
						currDir = Direction.LEFT;
						break;
					case 3:
						currDir = Direction.RIGHT;
						break;
				}
				moveStrategy.move(currentEntity, currentDungeon, currDir);
			}
		}

		if (switchFlick != null) {
			switchFlick.setStatus(switchOn);
		}		
		
		
		// find all entities that should be blown up	
		List<String> idsToBeRemoved = new ArrayList<String>();		
		for (Map.Entry<String, Entity> entry : currentDungeon.getEntities().entrySet()) {
			Entity currentEntity = entry.getValue();
			if (currentEntity instanceof BombStatic) {
				for (Position cardinal : currentEntity.getPosition().getCardinallyAdjPositions()) {
					Switch sw = (Switch)currentDungeon.getEntity("switch", cardinal);
					if (sw != null) {
						if (sw.getStatus()) {
							idsToBeRemoved.addAll(currentDungeon.toBeDetonated(currentEntity.getPosition()));							
						}
					}
				}
			}
		}
		currentDungeon.getEntities().keySet().removeAll(idsToBeRemoved);

		// Spawn in new zombietoast after 20 ticks
		if (currentDungeon.getTickNumber() % currentDungeon.getMode().getZombieTick() == 1 && currentDungeon.getTickNumber() > 1) {
			for (ZombieToastSpawner spawner : spawners) {
				spawner.spawnZombie(currentDungeon);
			}
		}

		return getDungeonInfo(currentDungeon.getId());
	}

	/**
	 * Checks if the itemUsed is able to be used
	 * @param itemUsed
	 */
	public void checkValidTick(String itemUsed) throws IllegalArgumentException, InvalidActionException {
		
		boolean itemInInventory = false;

		CollectibleEntity objectUsed = null; 
		if (itemUsed != null) {
			for (CollectibleEntity inv : currentDungeon.getInventory()) {
				if (inv.getId().equals(itemUsed)) {
					objectUsed = inv;
					itemInInventory = true;
					break;
				}
			}
		}

		if (itemUsed == null) {
			itemInInventory = true;
		}

		if (!itemInInventory) {
			throw new InvalidActionException("Cannot Use Requested Item; Item Does Not Exist In Inventory");
		}

		String itemType = null;
		
		if (objectUsed != null) {
			itemType = objectUsed.getType();
		}

		List<String> permittedItems = new ArrayList<String>();
		permittedItems.add("bomb");
		permittedItems.add("health_potion");
		permittedItems.add("invincibility_potion");
		permittedItems.add("invisibility_potion");
		
		if (!permittedItems.contains(itemType) && itemType != null) {
			throw new IllegalArgumentException("Cannot Use Requested Item; Ensure Item Is Either a Bomb, Health Potion, " +
			"Invincibility Potion, Invisibility Potion or null");
		}
		
	}
				
	
	/**
	 * Interacts with given entityId
	 * @param entityId	Id of entity to be interacted with
	 * @return	DungeonResponse dungeon of after interaction
	 * @throws IllegalArgumentException	If cannot interact with entity
	 * @throws InvalidActionException	If cannot bribe Mercenary
	 * 									If out of range for bribery
	 * 									If out of range for ZombieToastSpawner destruction
	 */
	public DungeonResponse interact(String entityId) throws IllegalArgumentException, InvalidActionException {
		checkValidInteract(entityId);
		Entity ent = currentDungeon.getEntity(entityId);
		// Attempt bribe if mercenary
		if (ent instanceof Mercenary) {
			Mercenary merc = (Mercenary) ent;
			merc.bribe(currentDungeon);
		}

		if (ent instanceof ZombieToastSpawner) {
			currentDungeon.removeEntity(ent);
		}
		
		return getDungeonInfo(currentDungeon.getId());
	}


	/**
	 * Checks if given entity is a valid interaction
	 * @param entityId
	 * @throws IllegalArgumentException
	 * @throws InvalidActionException
	 */
	public void checkValidInteract(String entityId) throws IllegalArgumentException, InvalidActionException{
		if (currentDungeon.getEntity(entityId) == null) {
			throw new IllegalArgumentException("Cannot Interact With Requested Entity; Entity Does Not Exist In The Map");
		}

		Entity interactEntity = currentDungeon.getEntity(entityId);

		List<CollectibleEntity> currentInventory = currentDungeon.getInventory();

		boolean hasGold = false;
		boolean hasWeapon = false;

		for (CollectibleEntity item : currentInventory) {
			if (item.getType().equals("treasure")) {
				hasGold = true;
			} else if (item.getType().equals("sword") || item.getType().equals("bow")) {
				hasWeapon = true;
			}
		}

		Position playerPosition = currentDungeon.getPlayerPosition();
		Position entityPosition = currentDungeon.getEntity(entityId).getPosition();


		if (interactEntity.getType().equals("mercenary")) {
			if (!Position.inBribingRange(playerPosition, entityPosition)) {
				throw new InvalidActionException("Player Out Of Bribing Range Of Mercenary");
			} else if (!hasGold) {
				throw new InvalidActionException("Player Does Not Have Sufficient Gold To Bribe Mercenary");
			}
		}

		if (interactEntity.getType().equals("zombie_toast_spawner")) {
			if (!Position.isCardinallyAdjacent(playerPosition, entityPosition)) {
				throw new InvalidActionException("Player Out Of Range To Destroy Zombie Toast Spawner");
			} else if (!hasWeapon) {
				throw new InvalidActionException("Player Does Not Have A Weapon To Destroy Spawner");
			}
		}
	}

	/**
	 * Build a given entity
	 * @param buildable	entity to be built
	 * @return		DungeonResponse after build
	 * @throws IllegalArgumentException	If entity cannot be used in build
	 * @throws InvalidActionException	If not enough items to build
	 */
	public DungeonResponse build(String buildable) throws IllegalArgumentException, InvalidActionException {
		checkValidBuild(buildable);
		List<CollectibleEntity> currentInventory = currentDungeon.getInventory();
		if (buildable.equals("bow")) {
			int newId = currentDungeon.getHistoricalEntCount();				
			CollectibleEntity bow = (CollectibleEntity) EntityFactory.createEntity(String.valueOf(newId),"bow", currentDungeon.getPlayerPosition());
			currentDungeon.setHistoricalEntCount(newId + 1);
			currentInventory.add(bow);
			int counterArrow = 0;
			int counterWood = 0;
			for (int i = 0; i < currentInventory.size(); i++) {
				CollectibleEntity found = currentInventory.get(i);
				if (found.getType().equals("arrow") && counterArrow < 3) {
					counterArrow++;
					currentInventory.remove(i);
					i = -1;
				} else if (found.getType().equals("wood") && counterWood < 1) {
					counterWood++;
					currentInventory.remove(i);
					i = -1;
				}
			}
		} else if (buildable.equals("shield")) {
			int newId = currentDungeon.getHistoricalEntCount();	
			CollectibleEntity shield = (CollectibleEntity) EntityFactory.createEntity(String.valueOf(newId), "shield", currentDungeon.getPlayerPosition());
			currentDungeon.setHistoricalEntCount(newId + 1);

			currentInventory.add(shield);
			int counterTreasure = 0;
			int counterKey = 0;
			int counterWood = 0;
			for (int i = 0; i < currentInventory.size(); i++) {
				CollectibleEntity found = currentInventory.get(i);
				if (found.getType().equals("wood") && counterWood < 2) {
					counterWood++;
					currentInventory.remove(i);
					i = -1;
				} else if ((found.getType().equals("treasure") && counterTreasure < 1) || (found.getType().equals("key") && counterKey < 1)) {
					counterTreasure++;
					counterKey++;
					currentInventory.remove(i);
					i = -1;
				}
			}
		}

		return getDungeonInfo(currentDungeon.getId());
	}

	public void checkValidBuild(String buildable) throws IllegalArgumentException, InvalidActionException{
		List<String> permittedBuild = new ArrayList<String>();
		permittedBuild.add("bow");
		permittedBuild.add("shield");

		if (!permittedBuild.contains(buildable)) {
			throw new IllegalArgumentException("Cannot Build The Desired Item; Only Bows and Shields Can Be Built");
		}

		List<String> currentBuildable = currentDungeon.getBuildables();
		if (!currentBuildable.contains(buildable)) {
			throw new InvalidActionException("Cannot Build The Desired Item; Not Enough Items To Complete The Recipe");
		}
	}

	public int getLastUsedDungeonId() {
		return lastUsedDungeonId;
	}

	public void setLastUsedDungeonId(int lastUsedDungeonId) {
		this.lastUsedDungeonId = lastUsedDungeonId;
	}
	
}