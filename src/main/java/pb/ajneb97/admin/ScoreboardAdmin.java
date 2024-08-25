package pb.ajneb97.admin;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import me.clip.placeholderapi.PlaceholderAPI;
import pb.ajneb97.PaintballBattle;
import pb.ajneb97.player.PaintballTeam;
import pb.ajneb97.enums.ArenaState;
import pb.ajneb97.logic.PaintballPlayer;
import pb.ajneb97.arena.PaintballArena;
import pb.ajneb97.lib.fastboard.FastBoard;
import pb.ajneb97.utils.OthersUtils;

public class ScoreboardAdmin {
	
	private int taskID;
	private PaintballBattle plugin;
	private final Map<UUID, FastBoard> boards = new HashMap<>();
	public ScoreboardAdmin(PaintballBattle plugin){		
		this.plugin = plugin;		
	}
	
	public int getTaskID() {
		return this.taskID;
	}
	
	public void reloadScoreboards() {
	    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
	    final FileConfiguration messages = plugin.getMessages();
    	final FileConfiguration config = plugin.getConfig();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() { 
            	for(Player player : Bukkit.getOnlinePlayers()) {
            		actualizarScoreboard(player,messages,config);
                }
            }
        },0, 20L);
	}
	
	protected void actualizarScoreboard(final Player player,final FileConfiguration messages,final FileConfiguration config) {
		PaintballArena paintballArena = plugin.getPlayersMatch(player.getName());
		FastBoard board = boards.get(player.getUniqueId());
		if(paintballArena != null) {
			PaintballPlayer jugador = paintballArena.getPlayer(player.getName());
			if(board == null) {
				board = new FastBoard(player);
				board.updateTitle(ChatColor.translateAlternateColorCodes('&',messages.getString("gameScoreboardTitle")));
				boards.put(player.getUniqueId(), board);
			}
			
			List<String> lista = messages.getStringList("gameScoreboardBody");
			PaintballTeam paintballTeam1 = paintballArena.getTeam1();
			PaintballTeam paintballTeam2 = paintballArena.getTeam2();
			String equipo1Nombre = config.getString("teams."+ paintballTeam1.getColor()+".name");
			String equipo2Nombre = config.getString("teams."+ paintballTeam2.getColor()+".name");

			for(int i=0;i<lista.size();i++) {
				String message = ChatColor.translateAlternateColorCodes('&', lista.get(i).replace("%status%", getEstado(paintballArena,messages)).replace("%team_1%", equipo1Nombre)
						.replace("%team_2%", equipo2Nombre).replace("%team_1_lives%", paintballTeam1.getLives()+"").replace("%team_2_lives%", paintballTeam2.getLives()+"")
						.replace("%kills%", jugador.getKills()+"").replace("%arena%", paintballArena.getMatchNumber()).replace("%current_players%", paintballArena.getPlayerAmount()+"")
						.replace("%max_players%", paintballArena.getMaximumPlayerAmount()+""));
				if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI")!= null){
					message = PlaceholderAPI.setPlaceholders(player, message);
				}
				board.updateLine(i, message);
			}
		}else {
			if(board != null) {
				boards.remove(player.getUniqueId());
				board.delete();
			}
		}
	}
	
	private String getEstado(PaintballArena paintballArena, FileConfiguration messages) {
		//Remplazar variables del %time%
		if(paintballArena.getState().equals(ArenaState.WAITING)) {
			return messages.getString("statusWaiting");
		}else if(paintballArena.getState().equals(ArenaState.STARTING)) {
			int tiempo = paintballArena.getTime();
			return messages.getString("statusStarting").replace("%time%", OthersUtils.getTiempo(tiempo));
		}else if(paintballArena.getState().equals(ArenaState.ENDING)) {
			int tiempo = paintballArena.getTime();
			return messages.getString("statusFinishing").replace("%time%", OthersUtils.getTiempo(tiempo));
		}else {
			int tiempo = paintballArena.getTime();
			return messages.getString("statusIngame").replace("%time%", OthersUtils.getTiempo(tiempo));
		}
	}

}
