**Assumptions Milestone 1**

Movable Entities - Mercenary
Keeping consistent with cardinality of interaction between spawner and character, we are assuming that interacting with the mercenary can only be done from 2 tiles of (up down left right)  movement

Collectable Entities - General
Everything the player can obtain is a collectibleEntity (collectible entities, rare collectible entities, buildable entities)

Collectable Entity - OneRing
The rare collectible is spawned at the location of battle rather than automatically added to the inventory




**Assumptions Milestone 2**

Player
Player spawns with a max health of 100


Static Entities - Boulder
Boulders cannot be pushed onto Collectable, or Movable Entities


Static Entities - Portal
Boulders cannot be pushed THROUGH a portal, rather it will stay in place and the world will tick

Static Entities - Spawner
The spawner will not spawn a Zombie on a Wall, Boulder or other Moving Entity

Moving Entities - General
With the exception of spiders, movable entities cannot coincide with the exit
Enemies cannot collide with each other (i.e. can’t be on the same square simultaneously)

Moving Entities - Spider
Spider reverse direction when they hit a boulder; it rests for one tick before moving in the opposite direction
If there are more than maximum spiders attempted to be added, exception is thrown: InvalidActionException


Moving Entities - Mercenary
Mercenaries spawn every 10 ticks
In accordance with the project specification, mercenaries will spawn at entry location (the spawn position of the player).
Mercenary prioritises movement in the following order: up, down, left, right
Mercenary can be bribed with 1 gold
When mercenary becomes ally and starts following player, his is in the same tile as player


Moving Entities - Zombie
Zombies cannot walk through walls
Zombies cannot walk through boulders
Zombies cannot push boulders

Collectable Entities - Potions
Invincibility lasts for 5 ticks

Controller
List<String> buildable: List of buildables will include the list of items that can be crafted from the set inventory, does not duplicate buildable items such as [bow, bow, shield]
List<String> buildable: Does not take in account whether the total inventory can cover all the buildable items, i.e if there are 2 wood, 1 treasure and 3 arrows, both bow and shield will be added to list even though the 2 wood cannot cover for both the bow and shield

Battles
The player get bonus attack damage if it has a weapon
Multiple weapons stack attack damage and the player use all weapons against enemies
Arrows don't add on any attack ability, can only used to be form bows
If player has 2 base damage, having a sword (+10), and bow (+5), will stack to result in  17 total attack damage
Total player attack = base attack + sumAll(items.getAttack)

Misc
Default layer with most entities will be layer 0
Floor layer with example switch, will be layer -1

GameMode
Peaceful:
Zombie spawn 20 ticks
Enemy don’t attack player
Initialise 100 health
One ring gives 100 health
Invincibility pot last 8 secs

Standard:
Zombie spawn 20 ticks
Enemy attacks player
Initialise 100 health
One ring gives 100 health
Invincibility pot last 8 secs

Hard:
Zombie spawn 15 ticks
Enemy attacks player
Initialise 60 health
One ring gives 60 health
Invincibility pot last 0 secs


**Assumptions Milestone 3**

Bosses - Assassin bribe requires treasure AND one ring

Bosses - Hydra will spawn in the spawnpoint, every 50 ticks

Bosses - Hydra will not spawn on the 50th if the spawn point is currently taken by a collideable entity, needs to wait another 50ticks before trying again

Pathfinding - Dijkstra, if the player can’t collide into the particular square at that tick, no edge will be formed

Collectable Entity - Sun stone can be used as a key or treasure but has infinite uses

Rare Collectable Entity - Anduril has a durability of 15, and triples the player’s initial BASE attack damage

Buildable Entity - Sceptre can only be triggered through interact not through use

Buildable Entity - Sceptres only have a durability of 1, when it controls either a mercenary or assassin, it gets removed after interaction 

SaveLoad Game - If you load a game and don't save it, the game will be lost, even if it was previously saved

Moving Entity - Spider will spawn randomly throughout the dungeon every 20 ticks
On hard mode, spiders will spawn every 15 ticks

Moving Entity - Spawn points for all moving entities (mercenary, spider and zombie toast) depending on the gamemode, they will not spawn if it it’s spawn point is taken by a collideable entity.

Dijkstra - If the current grid contains an entity that the mercenary cannot collide into during the current tick, an edge will not be formed in the graphical representation of the dungeon.

Anduril - Has a durability of 15, and triples the player’s initial BASE attack damage

Sun Stone -  Can be used as a key or treasure but has infinite uses

Assassin - Bribe requires treasure AND one ring

Hydra - Will spawn in the spawnpoint, every 50 ticks
Hydra - Doesn’t spawn if cannot at the 50th tick at spawnpoint
Same with ZombieToast (15/20th tick) and Mercenary (10/20th tick)

Sceptre - Can only be triggered through interact not through use
Sceptre - Only has a durability of 1, when it controls either a mercenary or assassin, it gets removed after interaction 

Dijkstra - If the player can’t collide into the particular square at that tick, no edge will be formed

Spider - will spawn randomly throughout the dungeon every 20 ticks; on hard mode, every 15
