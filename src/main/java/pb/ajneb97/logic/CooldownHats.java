package pb.ajneb97.logic;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.arena.PaintballArena;
import pb.ajneb97.enums.ArenaState;

public class CooldownHats {

	int taskID;
	int tiempo;
	private PaintballPlayer jugador;
	private PaintballArena paintballArena;
	private PaintballBattle plugin;
	public CooldownHats(PaintballBattle plugin){		
		this.plugin = plugin;		
	}
	
	public void cooldownHat(final PaintballPlayer jugador, final PaintballArena paintballArena, int tiempo){
		this.jugador = jugador;
		this.tiempo = tiempo;
		this.paintballArena = paintballArena;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
		public void run(){
			if(!ejecutarCooldownHat()){
				FileConfiguration messages = plugin.getMessages();
				if(!paintballArena.getState().equals(ArenaState.ENDING)) {
					jugador.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("hatCooldownFinished")));
				}
				
				Bukkit.getScheduler().cancelTask(taskID);
				return;
			}	
 		   }
 	   }, 0L, 20L);
	}

	protected boolean ejecutarCooldownHat() {
		if(paintballArena != null && paintballArena.getState().equals(ArenaState.PLAYING)) {
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
	
	public void durationHat(final PaintballPlayer jugador, final PaintballArena paintballArena, int tiempo){
		this.jugador = jugador;
		this.tiempo = tiempo;
		this.paintballArena = paintballArena;
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
		if(paintballArena != null && paintballArena.getState().equals(ArenaState.PLAYING)) {
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
