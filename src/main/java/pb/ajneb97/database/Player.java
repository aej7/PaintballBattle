package pb.ajneb97.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pb.ajneb97.api.Hat;
import pb.ajneb97.api.Perk;

public class Player {

	private int wins;
	private int losses;
	private int ties;
	private int kills;
	private int coins;
	private final String name;
	private final String uuid;
	private final Map<String, Perk> perks;
	private final Map<String, Hat> hats;
	
	public Player(String name, String uuid, int wins, int losses, int ties, int kills, int coins, List<Perk> perkList, List<Hat> hatList) {
		this.wins = wins;
		this.losses = losses;
		this.ties = ties;
		this.kills = kills;
		this.coins = coins;
		this.name = name;
		this.uuid = uuid;
		perks = new HashMap<>();
		hats = new HashMap<>();

		for (Perk perk : perkList) {
			perks.put(perk.getName(), perk);
		}

		for (Hat hat : hatList) {
			hats.put(hat.getName(), hat);
		}
	}

	public List<Hat> getHats(){
		return List.copyOf(hats.values());
	}
	
	public void addHat(String hatName) {
		if (!hats.containsKey(hatName)) {
			hats.put(hatName, new Hat(hatName, false));
		}
	}
	
	public boolean hasHat(String hatName) {
    return hats.containsKey(hatName);
	}
	
	public boolean hasHatEquipped(String hatName) {
    Hat hat = hats.get(hatName);
		return hat != null && hat.isSelected();
	}
	
	public void unequipHat() {
    hats.values().forEach(hat -> hat.setSelected(false));
	}

	public void equipHat(String hatName) {
    hats.values().forEach(hat -> hat.setSelected(hat.getName().equals(hatName)));
	}
	
	public List<Perk> getPerks(){
		return List.copyOf(perks.values());
	}
	
	public void setPerk(String perkName, int level) {
    Perk perk = perks.get(perkName);

		if (perk != null) {
			perk.setLevel(level);
		} else {
			perks.put(perkName, new Perk(perkName, level));
		}
	}
	
	public int getPerkLevel(String perkName) {
    Perk perk = perks.get(perkName);

		if (perk != null) {
			return perk.getNivel();
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
