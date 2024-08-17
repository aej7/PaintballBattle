package pb.ajneb97.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pb.ajneb97.api.Hat;
import pb.ajneb97.api.Perk;

public class PaintballPlayer {

	private int wins;
	private int losses;
	private int ties;
	private int kills;
	private int coins;
	private final String name;
	private final String uuid;
	private final Map<String, Perk> namesAndLevelsOfPerks;
	private final Map<String, Hat> namesAndLevelsOfHats;
	
	public PaintballPlayer(String name, String uuid, int wins, int losses, int ties, int kills, int coins, List<Perk> perks, List<Hat> hats) {
		this.wins = wins;
		this.losses = losses;
		this.ties = ties;
		this.kills = kills;
		this.coins = coins;
		this.name = name;
		this.uuid = uuid;
		namesAndLevelsOfPerks = new HashMap<>();
		namesAndLevelsOfHats = new HashMap<>();

		for (Perk perk : perks) {
			namesAndLevelsOfPerks.put(perk.getName() + ";" + perk.getLevel(), perk);
		}

		for (Hat hat : hats) {
			namesAndLevelsOfHats.put(hat.getName() + ";" + hat.isSelected(), hat);
		}
	}

	public List<Hat> getNamesAndLevelsOfHats() {
		return List.copyOf(namesAndLevelsOfHats.values());
	}
	
	public void addHat(String hatName) {
		if (!namesAndLevelsOfHats.containsKey(hatName)) {
			namesAndLevelsOfHats.put(hatName, new Hat(hatName, false));
		}
	}
	
	public boolean hasHat(String hatName) {
    return namesAndLevelsOfHats.containsKey(hatName);
	}
	
	public boolean hasHatEquipped(String hatName) {
    Hat hat = namesAndLevelsOfHats.get(hatName);
		return hat != null && hat.isSelected();
	}
	
	public void unequipHat() {
    namesAndLevelsOfHats.values().forEach(hat -> hat.setSelected(false));
	}

	public void equipHat(String hatName) {
    namesAndLevelsOfHats.values().forEach(hat -> hat.setSelected(hat.getName().equals(hatName)));
	}
	
	public List<Perk> getNamesAndLevelsOfPerks() {
		return List.copyOf(namesAndLevelsOfPerks.values());
	}
	
	public void setPerk(String perkName, int level) {
    Perk perk = namesAndLevelsOfPerks.get(perkName);

		if (perk != null) {
			perk.setLevel(level);
		} else {
			namesAndLevelsOfPerks.put(perkName, new Perk(perkName, level));
		}
	}
	
	public int getPerkLevel(String perkName) {
    Perk perk = namesAndLevelsOfPerks.get(perkName);

		if (perk != null) {
			return perk.getLevel();
		} else {
			return 0;
		}
	}
	
	public String getUUID() {
		return uuid;
	}

	public int getWins() {
		return wins;
	}

	public int getLosses() {
		return losses;
	}

	public int getTies() {
		return ties;
	}

	public int getKills() {
		return kills;
	}
	
	public int getCoins() {
		return coins;
	}

	public String getName() {
		return name;
	}
	
	public void increaseWinAmount() {
		wins++;
	}
	
	public void increaseLossAmount() {
		losses++;
	}
	
	public void increaseTieAmount() {
		ties++;
	}
	
	public void increaseCoinsByAmount(int amount) {
		coins += amount;
	}
	
	public void decreaseCoinsByAmount(int amount) {
		coins += amount;
	}
	
	public void increaseKillsByAmount(int amount) {
		kills += amount;
	}
}
