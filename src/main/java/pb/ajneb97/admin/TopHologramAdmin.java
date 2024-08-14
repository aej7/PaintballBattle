package pb.ajneb97.admin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import pb.ajneb97.PaintballBattle;

public class TopHologramAdmin {

	int taskID;
	private final PaintballBattle plugin;
	public TopHologramAdmin(PaintballBattle plugin){		
		this.plugin = plugin;		
	}
	
	public int getTaskID() {
		return this.taskID;
	}
	
	public void scheduledUpdateHolograms() {
		FileConfiguration config = plugin.getConfig();
		long topHologramUpdateTimeInTicks = Long.parseLong(config.getString("top_hologram_update_time"))*20;
	    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() { 
            	updateHolograms();
            }
        }, topHologramUpdateTimeInTicks, topHologramUpdateTimeInTicks);
	}

	protected void updateHolograms() {
		plugin.getTopHolograms().forEach(topHologram -> topHologram.update(plugin));
	}
}
