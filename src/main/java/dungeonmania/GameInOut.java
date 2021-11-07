package dungeonmania;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dungeonmania.allEntities.Player;
import dungeonmania.allEntities.Portal;
import dungeonmania.allEntities.*;
import dungeonmania.util.Position;
import dungeonmania.util.Direction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.lang.String;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class GameInOut {

	public static void toJSON(String fileName, String path, Dungeon dungeon) throws IOException {

		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create(); 
			
			Writer writer = new FileWriter(path);
			gson.toJson(dungeon, writer);
			writer.flush(); 
        	writer.close();
		} catch (Exception e) {
			System.out.println("Game Doesn't Exist");
		}
	}

	public static Dungeon fromJSON(String expType, String path, String feed, int lastUsedDungeonId, String gameMode) throws IOException {
        List<Entity> entityList = new ArrayList<>();
		List<CollectableEntity> returnInv = new ArrayList<>();
		String goals = null;
		String playMode = null; 
		int height = 50;
		int width  = 50;

		try {
            Map<String, Object> jsonMap = new Gson().fromJson(path, Map.class);
			if (expType.equals("load")) {
				playMode = (String)jsonMap.get("gameMode"); 
				goals = (String)jsonMap.get("goals"); 
			} else if (expType.equals("new")) {
				playMode = gameMode;
				Map<String, Object> goalConditions = (Map<String, Object>)jsonMap.get("goal-condition");
				
				if (goalConditions != null) {
					if (!goalConditions.get("goal").equals("AND") && !goalConditions.get("goal").equals("OR")) {	
						goals = (String)goalConditions.get("goal");
					} 
					JSONObject goalCon = new JSONObject(goalConditions);
					goals = createGoals(goalCon).remainingString();
					goals = createGoals(goalCon).remainingString();
				}
			}

			Map<String, Object> goalConditions = (Map<String, Object>)jsonMap.get("goal-condition");

			if (jsonMap.get("height") != null) {
				Double expHeight = (Double)jsonMap.get("height");
				height = expHeight.intValue(); 
			}

			if (jsonMap.get("width") != null) {
				Double expWidth = (Double)jsonMap.get("width");
				width = expWidth.intValue();  
			}

			List<Map<String, Object>> parseList = (List<Map<String, Object>>)jsonMap.get("entities"); 

			for (int i = 0; i < parseList.size(); i++) {

                Map<String, Object> currentEntity = parseList.get(i);
				
				String entityType = (String)currentEntity.get("type");
				String entityId = null;
				Position exportPos = null;

				if (expType.equals("load")) {
					entityId = (String)currentEntity.get("id");
   
					Map<String, Double> entityPosition = (Map<String, Double>)currentEntity.get("position");

					Double xDouble = (Double)entityPosition.get("x");
					Double yDouble = (Double)entityPosition.get("y");
					Double zDouble = (Double)entityPosition.get("layer");

					int xCoord = xDouble.intValue();
					int yCoord = yDouble.intValue();
					int zCoord = zDouble.intValue();
					
					exportPos = new Position(xCoord, yCoord, zCoord);
				} else if (expType.equals("new")){
					entityId = String.valueOf(i);

					Double xDouble = (Double)currentEntity.get("x");
					Double yDouble = (Double)currentEntity.get("y");
					int xCoord = xDouble.intValue();
					int yCoord = yDouble.intValue();
	
					int zCoord = 0;
					if (entityType.contains("switch")) {
						zCoord = -1;
					}

					exportPos = new Position(xCoord, yCoord, zCoord);
				}

				if (entityType.contains("portal")) {
					String colour = (String)currentEntity.get("colour");
					Portal portal = new Portal(entityId, exportPos, colour);
					entityList.add(portal);
				} else if (entityType.contains("player")) {
					Player player = new Player(entityId, exportPos, playMode);
					if (expType.equals("load")) { 
						Double healthD = (Double)currentEntity.get("health");
						int health = healthD.intValue();
	
						Double attackD = (Double)currentEntity.get("attack");
						int attack = attackD.intValue();
	
						boolean visible = (boolean)currentEntity.get("visible");
						boolean haveKey = (boolean)currentEntity.get("haveKey");
	
						Double invinceD = (Double)currentEntity.get("invincibleTickDuration");
						int invincibleTickDuration = invinceD.intValue();
	
						player.setHealth(health);
						player.setAttack(attack);
						player.setVisibility(visible);
						player.setCurrentDir(Direction.UP);
						player.setHaveKey(haveKey);
						player.setInvincibleTickDuration(invincibleTickDuration);
					} 
					entityList.add(player);
				} else if (entityType.contains("swamp_tile")) {

					Double moveFD = (Double)currentEntity.get("movement_factor");
					int moveF = moveFD.intValue();
					
					SwampTile swampT = new SwampTile(entityId, exportPos, moveF);
					entityList.add(swampT);
				} else {
					Entity newEntity = EntityFactory.createEntity(entityId, entityType, exportPos);
					entityList.add(newEntity);
				}

			}

			if (expType.equals("load")) {
				List<Map<String,Object>> inventoryList = (List<Map<String,Object>> )jsonMap.get("inventory"); 
				for (Map<String, Object> currentItem : inventoryList) {
	
					String itemType = (String)currentItem.get("type");
					String itemId = (String)currentItem.get("id");
	
					Map<String, Double> entityPosition = (Map<String, Double>)currentItem.get("position");
	
					Double xDouble = (Double)entityPosition.get("x");
					Double yDouble = (Double)entityPosition.get("y");
					Double zDouble = (Double)entityPosition.get("layer");
	
					int xCoord = xDouble.intValue();
					int yCoord = yDouble.intValue();
					int zCoord = zDouble.intValue();
					
					Position itemPos = new Position(xCoord, yCoord, zCoord);
	
					if (itemType.equals("treasure")) {
						Treasure newTreasure = new Treasure(itemId, itemPos);
						returnInv.add(newTreasure);
					} else if (itemType.equals("key")){
						Key newKey = new Key(itemId, itemPos);
						returnInv.add(newKey);
					} else if (itemType.equals("health_potion")){
						HealthPotion newHP = new HealthPotion(itemId, itemPos);
						returnInv.add(newHP);
					} else if (itemType.equals("invincibility_potion")){
						InvincibilityPotion newINVCP = new InvincibilityPotion(itemId, itemPos);
						returnInv.add(newINVCP);
					} else if (itemType.equals("invisibility_potion")){
						InvisibilityPotion newINVSP = new InvisibilityPotion(itemId, itemPos);
						returnInv.add(newINVSP);
					} else if (itemType.equals("wood")){
						Wood newWood = new Wood(itemId, itemPos);
						returnInv.add(newWood);
					} else if (itemType.equals("arrow")){
						Arrow newArrow = new Arrow(itemId, itemPos);
						returnInv.add(newArrow);
					} else if (itemType.equals("bomb")){
						BombItem newBomb = new BombItem(itemId, itemPos);
						returnInv.add(newBomb);
					} else if (itemType.equals("sword")){
						Double durabilityD = (Double)currentItem.get("durability");
						int durability = durabilityD.intValue();
						Sword newSword = new Sword(itemId, itemPos);
						newSword.setDurability(durability);
						returnInv.add(newSword);
					} else if (itemType.equals("armour")){
						Double durabilityD = (Double)currentItem.get("durability");
						int durability = durabilityD.intValue();
						Armour newArmour = new Armour(itemId, itemPos);
						newArmour.setDurability(durability);
						returnInv.add(newArmour);
					} else if (itemType.equals("one_ring")){
						OneRing newRing = new OneRing(itemId, itemPos);
						returnInv.add(newRing);
					} else if (itemType.equals("bow")){
						Double durabilityD = (Double)currentItem.get("durability");
						int durability = durabilityD.intValue();
						Bow newBow = new Bow(itemId, itemPos);
						newBow.setDurability(durability);
						returnInv.add(newBow);
					} else if (itemType.equals("shield")){
						Double durabilityD = (Double)currentItem.get("durability");
						int durability = durabilityD.intValue();
						Shield newShield = new Shield(itemId, itemPos);
						newShield.setDurability(durability);
						returnInv.add(newShield);
					}
				}
			}
			
			Dungeon returnDungeon = new Dungeon(lastUsedDungeonId, feed, entityList, playMode, goals, height, width);

			if (expType.equals("load")) {
				Double tickD = (Double)jsonMap.get("tickNumber"); 
				int tickNumber = tickD.intValue();
	
				Double hisD = (Double)jsonMap.get("historicalEntCount");  
				int historicalEntCount = hisD.intValue();
	
				Map<String, Double> spawnPos = (Map<String, Double>)jsonMap.get("spawnpoint");
	
				Double xSD = (Double)spawnPos.get("x");
				Double ySD = (Double)spawnPos.get("y");
				Double zSD = (Double)spawnPos.get("layer");
	
				int xSC = xSD.intValue();
				int ySC= ySD.intValue();
				int zSC = zSD.intValue();
				
				Position spawnpoint = new Position(xSC, ySC, zSC);
				returnDungeon.setId(lastUsedDungeonId);
				returnDungeon.setName(feed);
				returnDungeon.setInventory(returnInv);
				returnDungeon.setEntities(entityList);
				returnDungeon.setGoals(goals);
				returnDungeon.setHistoricalEntCount(historicalEntCount);
				returnDungeon.setTickNumber(tickNumber);
				returnDungeon.setSpawnpoint(spawnpoint); 
			}

			return returnDungeon;

		} catch (Exception e) {
			System.out.println("Error Loading Game");
		}
		return null;
	}

	/**
	 * Extract the goals from a .json dungeon
	 * @param goal
	 * @return
	 */
	public static GoalNode createGoals (JSONObject goal) {
		String current = goal.getString("goal");
		if (current.equals("enemies")) {
			return new GoalEnemies(current);
		} else if (current.equals("exit")) {
			return new GoalExit(current);
		} else if (current.equals("treasure")) {
			return new GoalTreasure(current);
		} else if (current.equals("boulder")) {
			return new GoalBoulder(current);
		} else if (current.equals("AND")) {
			GoalAnd andGoal = new GoalAnd(current);
			JSONArray subGoals = goal.getJSONArray("subgoals");
			for (int i = 0; i < subGoals.length(); i++) {
				GoalNode subGoal = createGoals(subGoals.getJSONObject(i));
				andGoal.add(subGoal);
			}
			return andGoal;
		} else if (current.equals("OR")) {
			GoalOr orGoal = new GoalOr(current);
			JSONArray subGoals = goal.getJSONArray("subgoals");
			for (int i = 0; i < subGoals.length(); i++) {
				GoalNode subGoal = createGoals(subGoals.getJSONObject(i));
				orGoal.add(subGoal);
			}
			return orGoal;
		}  
		return null;
	}

}