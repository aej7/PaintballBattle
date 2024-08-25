package pb.ajneb97.player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

public class PaintballTeam {

	private final Map<String, PaintballPlayer> players;
	private String teamColor;
	//Tipos: blue,red,yellow,green,orange,purple,black,white
	//brown,magenta,light_blue,lime,pink,gray,light_gray,cyan
	private Location spawn;
	private int currentLives;
	private boolean random;
	
	
	public PaintballTeam(String teamColor) {
		this.players = new HashMap<>();
		this.teamColor = teamColor;
		this.currentLives = 0;
	}
	
	public boolean isRandom() {
		return this.random;
	}
	
	public void setRandom(boolean random) {
		this.random = random;
	}

	public int getLives() {
		return this.currentLives;
	}
	
	public void decreaseLives(int amount) {
		this.currentLives = this.currentLives - amount;
	}
	
	public void increaseLives(int amount) {
		this.currentLives = this.currentLives + amount;
	}
	
	public void setLives(int amount) {
		this.currentLives = amount;
	}
	
	public void setColor(String color) {
		this.teamColor = color;
	}
	
	public String getColor() {
		return this.teamColor;
	}
	
	public boolean containsPlayer(String playerName) {
    return players.containsKey(playerName);
  }
	
	public boolean addPlayer(PaintballPlayer paintballPlayer) {
		String playerName = paintballPlayer.getPlayer().getName();

		return players.put(playerName, paintballPlayer) != null;
	}
	
	public boolean removePlayer(String playerName) {
		return players.remove(playerName) != null;
	}
	
	public Map<String, PaintballPlayer> getPlayers() {
		return this.players;
	}
	
	public int getCurrentSize() {
		return this.players.size();
	}
	
	public Location getSpawn() {
		return this.spawn;
	}
	
	public void setSpawn(Location spawnLocation) {
		this.spawn = spawnLocation;
	}
	
	public int getKills() {
		return players.values().stream().mapToInt(PaintballPlayer::getKills).sum();
	}
}
