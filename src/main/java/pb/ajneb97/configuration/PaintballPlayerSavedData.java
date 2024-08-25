package pb.ajneb97.configuration;


import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;


public class PaintballPlayerSavedData {

	private final ItemStack[] savedInventory;
	private final ItemStack[] savedEquipment;
	private final GameMode savedGamemode;
	private final float savedExperience;
	private final int savedLevel;
	private final int savedHunger;
	private final double healthSaved;
	private final double maxHealthSaved;
	private final boolean isFlightAllowed;
	private final boolean isFlying;
	
	public PaintballPlayerSavedData(ItemStack[] savedInventory, ItemStack[] savedEquipment, GameMode savedGamemode, float savedExperience, int savedLevel, int savedHunger,
																	double healthSaved, double maxHealthSaved, boolean isFlightAllowed, boolean isFlying) {
		this.savedInventory = savedInventory;
		this.savedEquipment = savedEquipment;
		this.savedGamemode = savedGamemode;
		this.savedExperience = savedExperience;
		this.savedLevel = savedLevel;
		this.savedHunger = savedHunger;
		this.healthSaved = healthSaved;
		this.maxHealthSaved = maxHealthSaved;
		this.isFlightAllowed = isFlightAllowed;
		this.isFlying = isFlying;
	}

	public boolean isFlightAllowed() {
		return isFlightAllowed;
	}

	public boolean isFlying() {
		return isFlying;
	}

	public ItemStack[] getInventory() {
		return savedInventory;
	}
	
	public ItemStack[] getEquipment() {
		return savedEquipment;
	}

	public GameMode getGamemode() {
		return savedGamemode;
	}
	
	public float getXp() {
		return savedExperience;
	}
	
	public int getLevel() {
		return this.savedLevel;
	}
	
	public int GetHunger() {
		return this.savedHunger;
	}

	public double getHealth() {
		return healthSaved;
	}

	public double getMaxHealth() {
		return maxHealthSaved;
	}
}
