package pb.ajneb97.logic;



import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.arena.PaintballArena;
import pb.ajneb97.lib.actionbarapi.ActionBarAPI;
import pb.ajneb97.player.PaintballKillstreak;

public class CooldownKillstreaksActionbar {
	
	int taskID;
	private PaintballBattle plugin;
	public CooldownKillstreaksActionbar(PaintballBattle plugin){		
		this.plugin = plugin;		
	}
	
	public void createActionbars() {
	    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
	    final FileConfiguration messages = plugin.getMessages();
    	final FileConfiguration config = plugin.getConfig();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() { 
            	for(Player player : Bukkit.getOnlinePlayers()) {
            		actualizarActionbars(player,messages,config);
                }
            }
        },0, 20L);
	}
	
	protected void actualizarActionbars(final Player player,final FileConfiguration messages,final FileConfiguration config) {
		PaintballArena paintballArena = plugin.getPlayersMatch(player.getName());
		if(paintballArena != null) {
			PaintballPlayer jugador = paintballArena.getPlayer(player.getName());
			PaintballKillstreak ultima = jugador.getUltimaKillstreak();
			if(ultima != null) {
				String name = config.getString("killstreaks_items."+ultima.getType()+".name");
				int tiempo = ultima.getTime();
				ActionBarAPI.sendActionBar(jugador.getPlayer(), ChatColor.translateAlternateColorCodes('&', messages.getString("killstreakActionbar")
						.replace("%killstreak%", name).replace("%time%", tiempo+"")));
			}
		}
	}
}
