package dungeonmania;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dungeonmania.allEntities.SwampTile;
import dungeonmania.util.Position;

/**
 * The Dijksta Interface handles all things Dijkstra-related for Mercenary and Assassin movement.
 */
public interface Dijkstra {

	/**
	 * This method returns the most optimal position for the Mercenary/Assassin to move onto in pursuit of the player.
	 * @param source the source position (position of the Mercenary/Assassin)
	 * @param currentDungeon the current Dungeon
	 * @return the most optimal position for the Mercenary/Assassin to move onto 
	 */
	public static Position move(Position source, Dungeon currentDungeon) {
		int minX = currentDungeon.getMinX() - 1;
		int maxX = currentDungeon.getMaxX() + 1;
		int minY = currentDungeon.getMinY() - 1;
		int maxY = currentDungeon.getMaxY() + 1;

		Map<Position, Map<Position, Integer>> dungeonMap = createGraph(currentDungeon, minX, minY, maxX, maxY);

		return traverse(source, currentDungeon.getPlayerPosition(), dungeonMap, minX, minY, maxX, maxY);
	}

	/**
	 * createGraph creates a weighted graph of the current dungeon. If the Mercenary/Assassin is unable to traverse through
	 * a cell during the the tick, an edge will not exist between the two cell. Edges only exist between adjacent 
	 * cell on the physical map. The weight of each edge represents twice the number of ticks required for the 
	 * Mercenary/Assassin to traverse from one cell to the other. The X and Y values are made to be most efficient for time complexity
	 * @param currentDungeon the current Dungeon
	 * @param minX the minimum X value to be considered by Dijkstra's algortihm
	 * @param minY the minimum Y value to be considered by Dijkstra's algortihm
	 * @param maxX the maximum X value to be considered by Dijkstra's algortihm
	 * @param maxY the maximum Y value to be considered by Dijkstra's algortihm
	 * @return a graph of the current grid, represented as a nested map
	 */
	public static Map<Position, Map<Position, Integer>> createGraph(Dungeon currentDungeon, int minX, int minY, int maxX, int maxY) {
		Map<Position, Map<Position, Integer>> dungeonMap = new HashMap<>();
		
		/*A list of entity types Mercenary/Assasins cannot coincide with. If at least one of these entities exist on a cell,
		there will be no edges between this cell and its adjacent cells in the weighted graph */
		List<String> mercIllegal = new ArrayList<>();
		
		mercIllegal.add("door");
		mercIllegal.add("door_1");
		mercIllegal.add("door_2");
		mercIllegal.add("wall");
		mercIllegal.add("boulder");
		mercIllegal.add("zombie_toast_spawner");
		mercIllegal.add("bomb");

		/* iterating through the current dungeon to check if each cell should be able traversed by the Merc/Assasin */
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				Position currPos = new Position(x, y);

				List<Entity> entOnCell = currentDungeon.getEntitiesOnCell(currPos);
				List<String> entTypesOnCell = new ArrayList<>();

				for (Entity ent : entOnCell) {
					entTypesOnCell.add(ent.getType());
				}

				/* If the cell contains entities the Merc/Assassin cannot coincide with, the current cell will be disregarded */
				if (!Collections.disjoint(entOnCell, mercIllegal)) {
					continue;
				} 
				
				/* A helper function returns a map with the key of the entries being positions of the adjacent cells 
				traversable by the Merc/Assasin during the current tick, and the values being the appropriate weight of the c
				orresponding edge as the value of each entry */
				Map<Position, Integer> outPaths = entHelper(currPos, currentDungeon, mercIllegal);

				/* If there exist at least one traversable adjacent cell for the current position, the information will be 
				put inside the dungeonMap */
				if (outPaths.size() != 0) {
					dungeonMap.put(currPos, outPaths);
				}
			}
		} return dungeonMap;
	}


	/**
	 * A helper function returns a map with the key of the entries being positions of the adjacent cells 
	 * traversable by the Merc/Assasin during the current tick, and the values being the appropriate weight of the
	 * corresponding edge as the value of each entry.
	 * @param currPos current position of the grid being analysed
	 * @param currentDungeon the current Dungeon
	 * @param mercIllegal a list of entity types the Mercenary/Assassin cannot collide with, thus they will avoid colliding with
	 * cells containg such entities in Dijkstra's algortihm, ie. an edge will not be created.
	 * @return the nested map for each entity
	 */
	public static Map<Position, Integer> entHelper(Position currPos, Dungeon currentDungeon, List<String> mercIllegal) {
		List<Position> adjPos = currPos.getCardinallyAdjPositions();
		
		Map<Position, Integer> outPaths = new HashMap<>();

		/* Iterating through the list of positions that adjacent to the current cell */
		for (Position pos : adjPos) {
			int traverseSpeed = 2;

			/* Obtaining a list of entities on the cell, and their types */
			List<Entity> entCell = currentDungeon.getEntitiesOnCell(pos);
			List<String> entTypesAdjCell = new ArrayList<>();

			for (Entity ent : entCell) {
				if (ent instanceof SwampTile) {
					SwampTile swampTile = (SwampTile) ent;
					traverseSpeed = swampTile.getMoveFactor();
				} entTypesAdjCell.add(ent.getType());
			}

			if (!Collections.disjoint(entTypesAdjCell, mercIllegal)) {
				continue;
			} outPaths.put(pos, traverseSpeed);
 		} return outPaths;
	}


	/**
	 * A Method that traverses through the above created weighted graph and executes Dijkstra's algortithm, returning the next
	 * position the Merc/Assasin should go to next tot maximise efficiency chasing the player. Pseudocode sourced from the 
	 * project specification: https://gitlab.cse.unsw.edu.au/COMP2511/21T3/project-specification/-/blob/master/M3.md.
	 * @param source the current position of the Mercenary/Assessin
	 * @param destination the current position of the player
	 * @param dungeonMap the graph of the dungeon
	 * @param minX the minimum X value to be considered by Dijkstra's algortihm
	 * @param minY the minimum Y value to be considered by Dijkstra's algortihm
	 * @param maxX the maximum X value to be considered by Dijkstra's algortihm
	 * @param maxY the maximum Y value to be considered by Dijkstra's algortihm
	 * @return the next position for the Mercenary/Assassin to move onto
	 */
	public static Position traverse(Position source, Position destination, Map<Position, Map<Position, Integer>> dungeonMap, int minX, int minY, int maxX, int maxY) {
		Map<Position, Double> dist = new HashMap<>();
		Map<Position, Position> prev = new HashMap<>();

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				Position pos = new Position(x, y);
				dist.put(pos, Double.POSITIVE_INFINITY);
				prev.put(pos, null);
			}
		} dist.put(source, 0.0);

		Map<Position, Double> dijkstraQueue = new HashMap<>();

		for (Position entry : dungeonMap.keySet()) {
			dijkstraQueue.put(entry, dist.get(entry));
		}

		while (!dijkstraQueue.isEmpty()) {
			Position u = null;
			double min = Double.POSITIVE_INFINITY;

			for (Position entry : dijkstraQueue.keySet()) {
				if (dist.get(entry) < min) {
					min = dist.get(entry);
					u = entry;
				}
			} 

			if (u == null) {
				break;
			}
			
			dijkstraQueue.remove(u);
			
			Map<Position, Integer> compPos = dungeonMap.get(u);
			
			for (Map.Entry<Position, Integer> entry : compPos.entrySet()) {
				Double v = Double.valueOf(entry.getValue());

				if (dist.get(entry.getKey()) != null) {
					if (dist.get(u) + v < dist.get(entry.getKey())) {	
						dist.put(entry.getKey(), dist.get(u) +  v);
						prev.put(entry.getKey(), u);
					}
				}
			}
		}
		
		if (dist.get(destination) != Double.POSITIVE_INFINITY) {
			return nextPos(destination, source, prev); 		
		} return null;
	}
	
	/**
	 * A helper function that ensures the most optimal next position for the Merc/Assassin is returned.
	 * @param currPos the current position of the player
	 * @param source the current position of the Mercenary/Assassin
	 * @param prev the map that holds the shortest path between the Mercenary and Player
	 * @return the next position for the Mercenary/Assassin to traverse onto
	*/
	public static Position nextPos(Position currPos, Position source, Map<Position, Position> prev) {
		Position returnPos = null;
		while (!prev.get(currPos).equals(source)) {
			currPos = prev.get(currPos);
		} if (prev.get(currPos).equals(source)) {
			returnPos = currPos;
		} return returnPos;
	}
}
