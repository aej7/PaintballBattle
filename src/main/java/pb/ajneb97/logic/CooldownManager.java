package pb.ajneb97.logic;

import java.util.ArrayList;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.enums.MatchState;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;


public class CooldownManager {

	int taskID;
	int tiempo;
	private PaintballMatch paintballMatch;
	private PaintballBattle plugin;
	public CooldownManager(PaintballBattle plugin){		
		this.plugin = plugin;		
	}
	
	public void cooldownComenzarJuego(PaintballMatch paintballMatch, int cooldown){
		this.paintballMatch = paintballMatch;
		this.tiempo = cooldown;
		paintballMatch.setTime(tiempo);
		final FileConfiguration messages = plugin.getMessages();
		final FileConfiguration config = plugin.getConfig();
		final String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
		ArrayList<PaintballPlayer> jugadores = paintballMatch.getPlayers();
		for(int i=0;i<jugadores.size();i++) {
			jugadores.get(i).getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("arenaStartingMessage").replace("%time%", tiempo+"")));
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
		if(paintballMatch != null && paintballMatch.getState().equals(MatchState.STARTING)) {
			if(tiempo <= 5 && tiempo > 0) {
				ArrayList<PaintballPlayer> jugadores = paintballMatch.getPlayers();
				for(int i=0;i<jugadores.size();i++) {
					jugadores.get(i).getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("arenaStartingMessage").replace("%time%", tiempo+"")));
					String[] separados = config.getString("startCooldownSound").split(";");
					try {
						Sound sound = Sound.valueOf(separados[0]);
						jugadores.get(i).getJugador().playSound(jugadores.get(i).getJugador().getLocation(), sound, Float.valueOf(separados[1]), Float.valueOf(separados[2]));
					}catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
					}
				}
				paintballMatch.decreaseTime();
				tiempo--;
				return true;
			}else if(tiempo <= 0) {
				PartidaManager.iniciarPartida(paintballMatch,plugin);
				return false;
			}else {
				paintballMatch.decreaseTime();
				tiempo--;
				return true;
			}
		}else {
			ArrayList<PaintballPlayer> jugadores = paintballMatch.getPlayers();
			for(int i=0;i<jugadores.size();i++) {
				jugadores.get(i).getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("gameStartingCancelled")));
			}
			return false;
		}
	}
	
	public void cooldownJuego(PaintballMatch paintballMatch){
		this.paintballMatch = paintballMatch;
		this.tiempo = paintballMatch.getMaximumTime();
		paintballMatch.setTime(tiempo);
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
		if(paintballMatch != null && paintballMatch.getState().equals(MatchState.PLAYING)) {
			paintballMatch.decreaseTime();
			if(tiempo == 0) {
				PartidaManager.iniciarFaseFinalizacion(paintballMatch, plugin);
				return false;
			}else {
				tiempo--;
				return true;
			}
		}else {
			return false;
		}
	}
	
	public void cooldownFaseFinalizacion(PaintballMatch paintballMatch, int cooldown, final Team ganador){
		this.paintballMatch = paintballMatch;
		this.tiempo = cooldown;
		paintballMatch.setTime(tiempo);
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
	
	protected boolean ejecutarComenzarFaseFinalizacion(Team ganador) {
		if(paintballMatch != null && paintballMatch.getState().equals(MatchState.ENDING)) {
			paintballMatch.decreaseTime();
			if(tiempo == 0) {
				PartidaManager.finalizarPartida(paintballMatch,plugin,false,ganador);
				return false;
			}else {
				tiempo--;
				if(ganador != null) {
					PartidaManager.lanzarFuegos(ganador.getPlayers());
				}
				return true;
			}
		}else {
			return false;
		}
	}
}
