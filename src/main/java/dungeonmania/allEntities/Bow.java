package dungeonmania.allEntities;

import dungeonmania.CollectableEntity;
import dungeonmania.util.Position;


public class Bow extends CollectableEntity {

	private int durability;
	private int extraDamage = 5;
   
    public Bow(String id, Position position) {
        super(id, position, "bow");
		this.durability = 5;
    }
	public int getExtraDamage() {
        return extraDamage;
    }
	public int getDurability () {
		return durability;
	}
	public void setDurability(int durability) {
		this.durability = durability;
	}

}
