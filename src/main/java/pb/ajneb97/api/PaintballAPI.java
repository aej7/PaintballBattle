package pb.ajneb97.api;

import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.database.PaintballPlayer;
import pb.ajneb97.database.MySql;
import pb.ajneb97.enums.ArenaState;
import pb.ajneb97.logic.PaintballArena;

public class PaintballAPI {

	private static PaintballBattle plugin;
	
	public PaintballAPI(PaintballBattle plugin) {
		this.plugin = plugin;
	}
	
//	public static JugadorDatos getPaintballDatos(Player player) {
//		if(!MySQL.isEnabled(plugin.getConfig())) {
//			JugadorDatos j = plugin.getJugador(player.getName());
//			if(j != null) {
//				return new JugadorPaintballDatos(j.getWins(),j.getLoses(),j.getTies(),j.getKills(),j.getHats(),j.getPerks());
//			}else {
//				return new JugadorPaintballDatos(0,0,0,0,new ArrayList<Hat>(),new ArrayList<Perk>());
//			}
//		}else {
//			return MySQL.getStatsTotales(plugin, player.getName(),"Coins");
//		}
//	}
	
	public static int getCoins(org.bukkit.entity.Player player) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.getCoins();
			}else {
				return 0;
			}
		}else {
			return MySql.getStatsTotales(plugin, player.getName(),"Coins");
		}
	}
	
	public static void addCoins(org.bukkit.entity.Player player, int coins) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				j.increaseCoinsByAmount(coins);
			}
		}else {
			MySql.agregarCoinsJugadorAsync(plugin, player.getName(), coins);
		}
	}
	
	public static void removeCoins(org.bukkit.entity.Player player, int coins) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				j.decreaseCoinsByAmount(coins);
			}
		}else {
			MySql.removerCoinsJugadorAsync(plugin, player.getName(), coins);
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
			return MySql.getStatsTotales(plugin, player.getName(),"Win");
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
			return MySql.getStatsTotales(plugin, player.getName(),"Lose");
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
			return MySql.getStatsTotales(plugin, player.getName(),"Tie");
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
			return MySql.getStatsTotales(plugin, player.getName(),"Kills");
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
			return MySql.jugadorTieneHat(plugin, player.getName(), hat);
		}
	}
	
	public static boolean hasHatSelected(org.bukkit.entity.Player player, String hat) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.hasHatEquipped(hat);
			}else {
				return false;
			}
		}else {
			return MySql.jugadorTieneHatSeleccionado(plugin, player.getName(), hat);
		}
	}
	
	public static ArrayList<Perk> getPerks(org.bukkit.entity.Player player) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null) {
				return j.getNamesAndLevelsOfPerks();
			}else {
				return new ArrayList<Perk>();
			}
		}else {
			return MySql.getPerksJugador(plugin, player.getName());
		}
	}
	
	public static ArrayList<Hat> getHats(org.bukkit.entity.Player player) {
		if(!MySql.isEnabled(plugin.getConfig())) {
			PaintballPlayer j = plugin.getPlayer(player.getName());
			if(j != null && j.getNamesAndLevelsOfHats() != null) {
				return j.getNamesAndLevelsOfHats();
			}else {
				return new ArrayList<Hat>();
			}
		}else {
			return MySql.getHatsJugador(plugin, player.getName());
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
