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
import pb.ajneb97.logic.Team;
import pb.ajneb97.enums.MatchStatus;
import pb.ajneb97.logic.PaintballPlayer;
import pb.ajneb97.logic.PaintballMatch;
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
		PaintballMatch paintballMatch = plugin.getPlayersMatch(player.getName());
		FastBoard board = boards.get(player.getUniqueId());
		if(paintballMatch != null) {
			PaintballPlayer jugador = paintballMatch.getJugador(player.getName());
			if(board == null) {
				board = new FastBoard(player);
				board.updateTitle(ChatColor.translateAlternateColorCodes('&',messages.getString("gameScoreboardTitle")));
				boards.put(player.getUniqueId(), board);
			}
			
			List<String> lista = messages.getStringList("gameScoreboardBody");
			Team team1 = paintballMatch.getTeam1();
			Team team2 = paintballMatch.getTeam2();
			String equipo1Nombre = config.getString("teams."+ team1.getTipo()+".name");
			String equipo2Nombre = config.getString("teams."+ team2.getTipo()+".name");

			for(int i=0;i<lista.size();i++) {
				String message = ChatColor.translateAlternateColorCodes('&', lista.get(i).replace("%status%", getEstado(paintballMatch,messages)).replace("%team_1%", equipo1Nombre)
						.replace("%team_2%", equipo2Nombre).replace("%team_1_lives%", team1.getVidas()+"").replace("%team_2_lives%", team2.getVidas()+"")
						.replace("%kills%", jugador.getAsesinatos()+"").replace("%arena%", paintballMatch.getNumber()).replace("%current_players%", paintballMatch.getCantidadActualJugadores()+"")
						.replace("%max_players%", paintballMatch.getCantidadMaximaJugadores()+""));
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
	
	private String getEstado(PaintballMatch paintballMatch, FileConfiguration messages) {
		//Remplazar variables del %time%
		if(paintballMatch.getState().equals(MatchStatus.WAITING)) {
			return messages.getString("statusWaiting");
		}else if(paintballMatch.getState().equals(MatchStatus.STARTING)) {
			int tiempo = paintballMatch.getTiempo();
			return messages.getString("statusStarting").replace("%time%", OthersUtils.getTiempo(tiempo));
		}else if(paintballMatch.getState().equals(MatchStatus.ENDING)) {
			int tiempo = paintballMatch.getTiempo();
			return messages.getString("statusFinishing").replace("%time%", OthersUtils.getTiempo(tiempo));
		}else {
			int tiempo = paintballMatch.getTiempo();
			return messages.getString("statusIngame").replace("%time%", OthersUtils.getTiempo(tiempo));
		}
	}

}
