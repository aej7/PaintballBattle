package pb.ajneb97.logic;

import java.util.ArrayList;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.enums.MatchStatus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;


public class CooldownManager {

	int taskID;
	int tiempo;
	private PaintballInstance paintballInstance;
	private PaintballBattle plugin;
	public CooldownManager(PaintballBattle plugin){		
		this.plugin = plugin;		
	}
	
	public void cooldownComenzarJuego(PaintballInstance paintballInstance, int cooldown){
		this.paintballInstance = paintballInstance;
		this.tiempo = cooldown;
		paintballInstance.setTiempo(tiempo);
		final FileConfiguration messages = plugin.getMessages();
		final FileConfiguration config = plugin.getConfig();
		final String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
		ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
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
		if(paintballInstance != null && paintballInstance.getEstado().equals(MatchStatus.STARTING)) {
			if(tiempo <= 5 && tiempo > 0) {
				ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
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
				paintballInstance.disminuirTiempo();
				tiempo--;
				return true;
			}else if(tiempo <= 0) {
				PartidaManager.iniciarPartida(paintballInstance,plugin);
				return false;
			}else {
				paintballInstance.disminuirTiempo();
				tiempo--;
				return true;
			}
		}else {
			ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
			for(int i=0;i<jugadores.size();i++) {
				jugadores.get(i).getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("gameStartingCancelled")));
			}
			return false;
		}
	}
	
	public void cooldownJuego(PaintballInstance paintballInstance){
		this.paintballInstance = paintballInstance;
		this.tiempo = paintballInstance.getTiempoMaximo();
		paintballInstance.setTiempo(tiempo);
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
		if(paintballInstance != null && paintballInstance.getEstado().equals(MatchStatus.PLAYING)) {
			paintballInstance.disminuirTiempo();
			if(tiempo == 0) {
				PartidaManager.iniciarFaseFinalizacion(paintballInstance, plugin);
				return false;
			}else {
				tiempo--;
				return true;
			}
		}else {
			return false;
		}
	}
	
	public void cooldownFaseFinalizacion(PaintballInstance paintballInstance, int cooldown, final Team ganador){
		this.paintballInstance = paintballInstance;
		this.tiempo = cooldown;
		paintballInstance.setTiempo(tiempo);
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
		if(paintballInstance != null && paintballInstance.getEstado().equals(MatchStatus.ENDING)) {
			paintballInstance.disminuirTiempo();
			if(tiempo == 0) {
				PartidaManager.finalizarPartida(paintballInstance,plugin,false,ganador);
				return false;
			}else {
				tiempo--;
				if(ganador != null) {
					PartidaManager.lanzarFuegos(ganador.getJugadores());
				}
				return true;
			}
		}else {
			return false;
		}
	}
}
