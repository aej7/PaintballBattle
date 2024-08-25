package pb.ajneb97.database;

import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.database.PaintballPlayer;
import pb.ajneb97.enums.ArenaState;
import pb.ajneb97.arena.PaintballArena;
import pb.ajneb97.player.PaintballHat;
import pb.ajneb97.player.PaintballPerk;
import pb.ajneb97.player.PaintballPlayer;

public class PaintballPlayerDAO {

	private static PaintballBattle plugin;
	
	public PaintballPlayerDAO(PaintballBattle plugin) {
		this.plugin = plugin;
	}
	
	public static int getCoins(org.bukkit.entity.Player player) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.getCoins();
			}else {
				return 0;
			}
		}else {
			return MySql.getTotalStats(plugin, player.getName(),"Coins");
		}
	}
	
	public static void addCoins(org.bukkit.entity.Player player, int coins) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				j.increaseCoinsByAmount(coins);
			}
		}else {
			MySql.addPlayerCoins(plugin, player.getName(), coins);
		}
	}
	
	public static void removeCoins(org.bukkit.entity.Player player, int coins) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				j.decreaseCoinsByAmount(coins);
			}
		}else {
			MySql.removePlayerCoins(plugin, player.getName(), coins);
		}
	}
	
	public static int getWins(org.bukkit.entity.Player player) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.getWins();
			}else {
				return 0;
			}
		}else {
			return MySql.getTotalStats(plugin, player.getName(),"Win");
		}
		
	}
	
	public static int getLoses(org.bukkit.entity.Player player) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.getLosses();
			}else {
				return 0;
			}
		}else {
			return MySql.getTotalStats(plugin, player.getName(),"Lose");
		}
		
	}
	
	public static int getTies(org.bukkit.entity.Player player) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.getTies();
			}else {
				return 0;
			}
		}else {
			return MySql.getTotalStats(plugin, player.getName(),"Tie");
		}
	}
	
	public static int getKills(org.bukkit.entity.Player player) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.getKills();
			}else {
				return 0;
			}
		}else {
			return MySql.getTotalStats(plugin, player.getName(),"Kills");
		}
		
	}
	
	public static int getPerkLevel(org.bukkit.entity.Player player, String perk) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.getPerkLevel(perk);
			}else {
				return 0;
			}
		}else {
			return MySql.getNivelPerk(plugin, player.getName(), perk);
		}
	}
	
	public static boolean hasHat(org.bukkit.entity.Player player, String hat) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.hasHat(hat);
			}else {
				return false;
			}
		}else {
			return MySql.playerHasHat(plugin, player.getName(), hat);
		}
	}
	
	public static boolean hasHatSelected(org.bukkit.entity.Player player, String hat) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.hasAnyHatEquipped(hat);
			}else {
				return false;
			}
		}else {
			return MySql.playerHasHatSelected(plugin, player.getName(), hat);
		}
	}
	
	public static ArrayList<PaintballPerk> getPerks(org.bukkit.entity.Player player) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.getNamesAndLevelsOfPerks();
			}else {
				return new ArrayList<PaintballPerk>();
			}
		}else {
			return MySql.getPerksJugador(plugin, player.getName());
		}
	}
	
	public static ArrayList<PaintballHat> getHats(org.bukkit.entity.Player player) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null && j.getNamesAndLevelsOfHats() != null) {
				return j.getNamesAndLevelsOfHats();
			}else {
				return new ArrayList<PaintballHat>();
			}
		}else {
			return MySql.getPlayerHats(plugin, player.getName());
		}
	}
	
	public static int getPlayersArena(String arena) {
		PaintballArena paintballArena = plugin.getMatch(arena);
		if(paintballArena != null) {
			return paintballArena.getPlayerAmount();
		}else {
			return 0;
		}
	}
	
	public static String getStatusArena(String arena) {
		PaintballArena paintballArena = plugin.getMatch(arena);
		FileConfiguration messages = plugin.getMessages();
		if(paintballArena != null) {
			if(paintballArena.getState().equals(ArenaState.STARTING)) {
				return messages.getString("signStatusStarting");
			}else if(paintballArena.getState().equals(ArenaState.WAITING)) {
				return messages.getString("signStatusWaiting");
			}else if(paintballArena.getState().equals(ArenaState.PLAYING)) {
				return messages.getString("signStatusIngame");
			}else if(paintballArena.getState().equals(ArenaState.ENDING)) {
				return messages.getString("signStatusFinishing");
			}else {
				return messages.getString("signStatusDisabled");
			}
		}else {
			return null;
		}
	}
}
