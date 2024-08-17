package pb.ajneb97.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.enums.ArenaState;
import pb.ajneb97.logic.PaintballArena;

public class SignAdmin {
	
	private int taskID;
	private PaintballBattle plugin;
	public SignAdmin(PaintballBattle plugin){
		this.plugin = plugin;		
	}
	
	public int getTaskID() {
		return this.taskID;
	}
	
	public void reloadSigns() {
	    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
 	    taskID = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() { 
            	ejecutarActualizarCarteles();
            }
        },0, 30L);
	}

	protected void ejecutarActualizarCarteles() {
		FileConfiguration config = plugin.getConfig();
		FileConfiguration messages = plugin.getMessages();
		if(config.contains("Signs")) {
			for(String arena : config.getConfigurationSection("Signs").getKeys(false)) {
				PaintballArena paintballArena = plugin.getMatch(arena);
				if(paintballArena != null) {
					List<String> listaCarteles = new ArrayList<String>();
					if(config.contains("Signs."+arena)) {
						listaCarteles = config.getStringList("Signs."+arena);
					}
					for(int i=0;i<listaCarteles.size();i++) {
						String[] separados = listaCarteles.get(i).split(";");
						int x = Integer.valueOf(separados[0]);
						int y = Integer.valueOf(separados[1]);
						int z = Integer.valueOf(separados[2]);
						World world = Bukkit.getWorld(separados[3]);
						if(world != null) {
							int chunkX = x >> 4;
							int chunkZ = z >> 4;
							if(!world.isChunkLoaded(chunkX, chunkZ)) {
								continue;
							}
							Block block = world.getBlockAt(x,y,z);
							
							if(block.getType().name().contains("SIGN")) {
								Sign sign = (Sign) block.getState();
								String estado = "";
								if(paintballArena.getState().equals(ArenaState.PLAYING)) {
									estado = messages.getString("signStatusIngame");
								}else if(paintballArena.getState().equals(ArenaState.STARTING)) {
									estado = messages.getString("signStatusStarting");
								}else if(paintballArena.getState().equals(ArenaState.WAITING)) {
									estado = messages.getString("signStatusWaiting");
								}else if(paintballArena.getState().equals(ArenaState.OFF)) {
									estado = messages.getString("signStatusDisabled");
								}else if(paintballArena.getState().equals(ArenaState.ENDING)) {
									estado = messages.getString("signStatusFinishing");
								}
								
								List<String> lista = messages.getStringList("signFormat");
								for(int c=0;c<lista.size();c++) {
									sign.setLine(c, ChatColor.translateAlternateColorCodes('&', lista.get(c).replace("%arena%", arena).replace("%current_players%", paintballArena.getPlayerAmount()+"")
											.replace("%max_players%", paintballArena.getMaximumPlayerAmount()+"").replace("%status%", estado)));
								}

								sign.update();
							}
						}
					}
					
				}
			}
		}
		
	}
}
