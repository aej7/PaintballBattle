package pb.ajneb97.logic;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.enums.MatchState;

public class CooldownHats {

	int taskID;
	int tiempo;
	private PaintballPlayer jugador;
	private PaintballMatch paintballMatch;
	private PaintballBattle plugin;
	public CooldownHats(PaintballBattle plugin){		
		this.plugin = plugin;		
	}
	
	public void cooldownHat(final PaintballPlayer jugador, final PaintballMatch paintballMatch, int tiempo){
		this.jugador = jugador;
		this.tiempo = tiempo;
		this.paintballMatch = paintballMatch;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
		public void run(){
			if(!ejecutarCooldownHat()){
				FileConfiguration messages = plugin.getMessages();
				if(!paintballMatch.getState().equals(MatchState.ENDING)) {
					jugador.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("hatCooldownFinished")));
				}
				
				Bukkit.getScheduler().cancelTask(taskID);
				return;
			}	
 		   }
 	   }, 0L, 20L);
	}

	protected boolean ejecutarCooldownHat() {
		if(paintballMatch != null && paintballMatch.getState().equals(MatchState.PLAYING)) {
			if(tiempo <= 0) {
				jugador.setEfectoHatEnCooldown(false);
				return false;
			}else {
				tiempo--;
				jugador.setTiempoEfectoHat(tiempo);
				return true;
			}
		}else {
			jugador.setEfectoHatEnCooldown(false);
			return false;
		}
	}
	
	public void durationHat(final PaintballPlayer jugador, final PaintballMatch paintballMatch, int tiempo){
		this.jugador = jugador;
		this.tiempo = tiempo;
		this.paintballMatch = paintballMatch;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
		public void run(){
			if(!ejecutarDurationHat()){
				Bukkit.getScheduler().cancelTask(taskID);
				return;
			}	
 		   }
 	   }, 0L, 20L);
	}
	
	protected boolean ejecutarDurationHat() {
		if(paintballMatch != null && paintballMatch.getState().equals(MatchState.PLAYING)) {
			if(tiempo <= 0) {
				jugador.setEfectoHatActivado(false);
				return false;
			}else {
				tiempo--;
				return true;
			}
		}else {
			jugador.setEfectoHatActivado(false);
			return false;
		}
	}
}
