package pb.ajneb97.logic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.arena.ArenaManager;
import pb.ajneb97.arena.PaintballArena;
import pb.ajneb97.enums.ArenaState;
import pb.ajneb97.player.PaintballKillstreak;

public class CooldownKillstreaks {

	int taskID;
	int tiempo;
	private PaintballPlayer jugador;
	private PaintballArena paintballArena;
	private PaintballBattle plugin;
	public CooldownKillstreaks(PaintballBattle plugin){		
		this.plugin = plugin;		
	}
	
	public void cooldownKillstreak(final PaintballPlayer jugador, final PaintballArena paintballArena, final String nombre, int tiempo){
		this.jugador = jugador;
		this.paintballArena = paintballArena;
		this.tiempo = tiempo;
		if(nombre.equalsIgnoreCase("fury")) {
			CooldownKillstreaks c = new CooldownKillstreaks(plugin);
			c.cooldownParticulasFury(jugador, paintballArena);
		}
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
		public void run(){
			if(!ejecutarCooldownKillstreak(nombre)){
				FileConfiguration messages = plugin.getMessages();
				FileConfiguration config = plugin.getConfig();
				if(!paintballArena.getState().equals(ArenaState.ENDING)) {
					String name = ChatColor.translateAlternateColorCodes('&', config.getString("killstreaks_items."+nombre+".name"));
					jugador.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("killstreakExpired").replace("%killstreak%", name)));
					String[] separados = config.getString("expireKillstreakSound").split(";");
					try {
						Sound sound = Sound.valueOf(separados[0]);
						jugador.getPlayer().playSound(jugador.getPlayer().getLocation(), sound, Float.valueOf(separados[1]), Float.valueOf(separados[2]));
					}catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
					}
				}
				
				Bukkit.getScheduler().cancelTask(taskID);
				return;
			}	
 		   }
 	   }, 0L, 20L);
	}

	protected boolean ejecutarCooldownKillstreak(String nombre) {
		if(paintballArena != null && paintballArena.getState().equals(ArenaState.PLAYING)) {
			if(tiempo <= 0) {
				jugador.removerKillstreak(nombre);
				return false;
			}else {
				PaintballKillstreak k = jugador.getKillstreak(nombre);
				if(k != null) {
					tiempo--;
					k.setTime(tiempo);
					return true;
				}else {
					jugador.removerKillstreak(nombre);
					return false;
				}
			}
		}else {
			jugador.removerKillstreak(nombre);
			return false;
		}
	}
	
	public void cooldownParticulasFury(final PaintballPlayer jugador, final PaintballArena paintballArena){
		this.jugador = jugador;
		this.paintballArena = paintballArena;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
		public void run(){
			if(!ejecutarParticulasFury()){
				Bukkit.getScheduler().cancelTask(taskID);
				return;
			}
 		   }
 	   }, 0L, 5L);
	}
	
	protected boolean ejecutarParticulasFury() {
		if(paintballArena != null && paintballArena.getState().equals(ArenaState.PLAYING)) {
			if(jugador != null) {
				if(jugador.getKillstreak("fury") != null) {
					Location l = jugador.getPlayer().getLocation().clone();
					l.setY(l.getY()+1.5);
					if(Bukkit.getVersion().contains("1.8")) {
						l.getWorld().playEffect(l, Effect.valueOf("VILLAGER_THUNDERCLOUD"),1);	
					}else {
						l.getWorld().spawnParticle(Particle.ANGRY_VILLAGER,l,1);
					}
					return true;
				}else {
					return false;
				}
			}else {
				return false;
			}
		}else {
			return false;
		}
	}
	
	public void cooldownNuke(final PaintballPlayer jugador, final PaintballArena paintballArena, final String[] separados1, final String[] separados2){
		this.jugador = jugador;
		this.paintballArena = paintballArena;
		this.tiempo = 5;
		final FileConfiguration messages = plugin.getMessages();
		for(PaintballPlayer player : paintballArena.getPlayers()) {
			player.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("nukeImpact").replace("%time%", tiempo+"")));
		}
		tiempo--;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
		public void run(){
			if(!ejecutarNuke(separados1,separados2,messages)){
				Bukkit.getScheduler().cancelTask(taskID);
				return;
			}
 		   }
 	   }, 20L, 20L);
	}

	protected boolean ejecutarNuke(String[] separados1,String[] separados2,FileConfiguration messages) {
		if(paintballArena != null && paintballArena.getState().equals(ArenaState.PLAYING)) {
			if(jugador != null) {
				if(tiempo <= 0) {
					try {
						Sound sound = Sound.valueOf(separados2[0]);
						if(separados2.length >= 4) {
							if(separados2[3].equalsIgnoreCase("global")) {
								for(PaintballPlayer player : paintballArena.getPlayers()) {
									player.getPlayer().playSound(player.getPlayer().getLocation(), sound, Float.valueOf(separados2[1]), Float.valueOf(separados2[2]));
								}
							}else {
								jugador.getPlayer().playSound(jugador.getPlayer().getLocation(), sound, Float.valueOf(separados2[1]), Float.valueOf(separados2[2]));
							}
						}else {
							jugador.getPlayer().playSound(jugador.getPlayer().getLocation(), sound, Float.valueOf(separados2[1]), Float.valueOf(separados2[2]));
						}
					}catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados2[0]+" &7is not valid."));
					}
					for(PaintballPlayer player : paintballArena.getPlayers()) {
						ArenaManager.muereJugador(paintballArena, jugador, player, plugin, false, true);
					}
					paintballArena.setNuke(false);
					return false;
				}else {
					try {
						Sound sound = Sound.valueOf(separados1[0]);
						if(separados1.length >= 4) {
							if(separados1[3].equalsIgnoreCase("global")) {
								for(PaintballPlayer player : paintballArena.getPlayers()) {
									player.getPlayer().playSound(player.getPlayer().getLocation(), sound, Float.valueOf(separados1[1]), Float.valueOf(separados1[2]));
								}
							}else {
								jugador.getPlayer().playSound(jugador.getPlayer().getLocation(), sound, Float.valueOf(separados1[1]), Float.valueOf(separados1[2]));
							}
						}else {
							jugador.getPlayer().playSound(jugador.getPlayer().getLocation(), sound, Float.valueOf(separados1[1]), Float.valueOf(separados1[2]));
						}
					}catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados1[0]+" &7is not valid."));
					}
					for(PaintballPlayer player : paintballArena.getPlayers()) {
						player.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("nukeImpact").replace("%time%", tiempo+"")));
					}
					tiempo--;
					return true;
				}
			}else {
				return false;
			}
		}else {
			return false;
		}
	}
}
