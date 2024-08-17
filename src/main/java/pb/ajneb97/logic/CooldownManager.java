package pb.ajneb97.logic;

import java.util.ArrayList;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.enums.ArenaState;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;


public class CooldownManager {

	int taskID;
	int time;
	private PaintballArena paintballArena;
	private final PaintballBattle plugin;
	public CooldownManager(PaintballBattle plugin){		
		this.plugin = plugin;		
	}
	
	public void setMatchStartCooldown(PaintballArena paintballArena, int cooldown) {
		this.paintballArena = paintballArena;
		this.time = cooldown;
		paintballArena.setTime(time);
		final FileConfiguration messages = plugin.getMessages();
		final FileConfiguration config = plugin.getConfig();
		final String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
		ArrayList<PaintballPlayer> players = paintballArena.getPlayers();
    for (PaintballPlayer player : players) {
      player.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("arenaStartingMessage").replace("%time%", time + "")));
    }
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
		public void run(){
			if(!ejecutarComenzarJuego(messages,config,prefix)){
				Bukkit.getScheduler().cancelTask(taskID);
				return;
			}	
 		   }
 	   }, 0L, 20L);
	}
	
	protected boolean ejecutarComenzarJuego(FileConfiguration messages,FileConfiguration config,String prefix) {
		if(paintballArena != null && paintballArena.getState().equals(ArenaState.STARTING)) {
			if(time <= 5 && time > 0) {
				ArrayList<PaintballPlayer> jugadores = paintballArena.getPlayers();
				for(int i=0;i<jugadores.size();i++) {
					jugadores.get(i).getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("arenaStartingMessage").replace("%time%", time +"")));
					String[] separados = config.getString("startCooldownSound").split(";");
					try {
						Sound sound = Sound.valueOf(separados[0]);
						jugadores.get(i).getPlayer().playSound(jugadores.get(i).getPlayer().getLocation(), sound, Float.valueOf(separados[1]), Float.valueOf(separados[2]));
					}catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
					}
				}
				paintballArena.decreaseTime();
				time--;
				return true;
			}else if(time <= 0) {
				ArenaManager.startArena(paintballArena,plugin);
				return false;
			}else {
				paintballArena.decreaseTime();
				time--;
				return true;
			}
		}else {
			ArrayList<PaintballPlayer> jugadores = paintballArena.getPlayers();
			for(int i=0;i<jugadores.size();i++) {
				jugadores.get(i).getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("gameStartingCancelled")));
			}
			return false;
		}
	}
	
	public void cooldownJuego(PaintballArena paintballArena){
		this.paintballArena = paintballArena;
		this.time = paintballArena.getMaximumTime();
		paintballArena.setTime(time);
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
		public void run(){
			if(!ejecutarJuego()){
				Bukkit.getScheduler().cancelTask(taskID);
				return;
			}	
 		   }
 	   }, 0L, 20L);
	}
	
	protected boolean ejecutarJuego() {
		if(paintballArena != null && paintballArena.getState().equals(ArenaState.PLAYING)) {
			paintballArena.decreaseTime();
			if(time == 0) {
				ArenaManager.setArenaStateToEnding(paintballArena, plugin);
				return false;
			}else {
				time--;
				return true;
			}
		}else {
			return false;
		}
	}
	
	public void cooldownFaseFinalizacion(PaintballArena paintballArena, int cooldown, final PaintballTeam ganador){
		this.paintballArena = paintballArena;
		this.time = cooldown;
		paintballArena.setTime(time);
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
		public void run(){
			if(!ejecutarComenzarFaseFinalizacion(ganador)){
				Bukkit.getScheduler().cancelTask(taskID);
				return;
			}	
 		   }
 	   }, 0L, 20L);
	}
	
	protected boolean ejecutarComenzarFaseFinalizacion(PaintballTeam winnerPaintballTeam) {
		if(paintballArena != null && paintballArena.getState().equals(ArenaState.ENDING)) {
			paintballArena.decreaseTime();
			if(time == 0) {
				ArenaManager.finalizarPartida(paintballArena,plugin,false, winnerPaintballTeam);
				return false;
			}else {
				time--;
				if(winnerPaintballTeam != null) {
					ArenaManager.lanzarFuegos(winnerPaintballTeam.getPlayers().values().stream().toList());
				}
				return true;
			}
		}else {
			return false;
		}
	}
}
