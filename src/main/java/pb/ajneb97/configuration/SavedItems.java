package pb.ajneb97.configuration;


import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;


public class SavedItems {

	private ItemStack[] inventarioGuardado;
	private ItemStack[] equipamientoGuardado;
	private GameMode gamemodeGuardado;
	private float experienciaGuardada;
	private int levelGuardado;
	private int hambreGuardada;
	private double vidaGuardada;
	private double maxVidaGuardada;
	private boolean allowFlight;
	private boolean isFlying;
	
	public SavedItems(ItemStack[] inventarioGuardado, ItemStack[] equipamientoGuardado, GameMode gamemodeGuardado, float experienciaGuardada, int levelGuardado, int hambreGuardada,
										double vidaGuardada, double maxVidaGuardada, boolean allowFlight, boolean isFlying) {
		this.inventarioGuardado = inventarioGuardado;
		this.equipamientoGuardado = equipamientoGuardado;
		this.gamemodeGuardado = gamemodeGuardado;
		this.experienciaGuardada = experienciaGuardada;
		this.levelGuardado = levelGuardado;
		this.hambreGuardada = hambreGuardada;
		this.vidaGuardada = vidaGuardada;
		this.maxVidaGuardada = maxVidaGuardada;
		this.allowFlight = allowFlight;
		this.isFlying = isFlying;
	}

	public boolean isAllowFlight() {
		return allowFlight;
	}

	public boolean isFlying() {
		return isFlying;
	}

	public ItemStack[] getInventory() {
		return inventarioGuardado;
	}
	
	public ItemStack[] getEquipment() {
		return equipamientoGuardado;
	}

	public GameMode getGamemode() {
		return gamemodeGuardado;
	}
	
	public float getXp() {
		return experienciaGuardada;
	}
	
	public int getLevel() {
		return this.levelGuardado;
	}
	
	public int GetHunger() {
		return this.hambreGuardada;
	}

	public double getHealth() {
		return vidaGuardada;
	}

	public double getMaxHealth() {
		return maxVidaGuardada;
	}
}
