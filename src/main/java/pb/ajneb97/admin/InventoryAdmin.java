package pb.ajneb97.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import pb.ajneb97.PaintballBattle;
import pb.ajneb97.logic.PaintballArena;
import pb.ajneb97.logic.PaintballArenaEdit;

public class InventoryAdmin implements Listener{

	private PaintballBattle plugin;
	public InventoryAdmin(PaintballBattle plugin) {
		this.plugin = plugin;
	}
	
	public static void createInventory(Player jugador, PaintballArena paintballArena, PaintballBattle plugin) {
		Inventory inv = Bukkit.createInventory(null, 36, ChatColor.translateAlternateColorCodes('&', "&2Editing Arena: &7"+ paintballArena.getMatchNumber()));
		ItemStack item = new ItemStack(Material.BEACON,1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lSet Lobby"));
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to define the arena Lobby in your"));
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7current position."));
		lore.add(ChatColor.translateAlternateColorCodes('&', ""));
		Location lobby = paintballArena.getLobby();
		if(lobby == null) {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Position: &7NONE"));
		}else {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Position:"));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eX: &7"+lobby.getX()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eY: &7"+lobby.getY()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eZ: &7"+lobby.getZ()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eWorld: &7"+lobby.getWorld().getName()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eYaw: &7"+lobby.getYaw()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&ePitch: &7"+lobby.getPitch()));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(10, item);
		
		item = new ItemStack(Material.QUARTZ_BLOCK,1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lSet Team 1 Spawn"));
		lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to define the arena team 1 Spawn"));
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7in your current position."));
		lore.add(ChatColor.translateAlternateColorCodes('&', ""));
		Location spawn = paintballArena.getTeam1().getSpawn();
		if(spawn == null) {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Position: &7NONE"));
		}else {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Position:"));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eX: &7"+spawn.getX()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eY: &7"+spawn.getY()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eZ: &7"+spawn.getZ()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eWorld: &7"+spawn.getWorld().getName()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eYaw: &7"+spawn.getYaw()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&ePitch: &7"+spawn.getPitch()));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(11, item);
		
		item = new ItemStack(Material.QUARTZ_BLOCK,1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lSet Team 2 Spawn"));
		lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to define the arena team 2 Spawn"));
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7in your current position."));
		lore.add(ChatColor.translateAlternateColorCodes('&', ""));
		spawn = paintballArena.getTeam2().getSpawn();
		if(spawn == null) {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Position: &7NONE"));
		}else {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Position:"));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eX: &7"+spawn.getX()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eY: &7"+spawn.getY()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eZ: &7"+spawn.getZ()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eWorld: &7"+spawn.getWorld().getName()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&eYaw: &7"+spawn.getYaw()));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&ePitch: &7"+spawn.getPitch()));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(12, item);
		
		item = new ItemStack(Material.GHAST_TEAR, paintballArena.getMinimumPlayerAmount());
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lSet Min Players"));
		lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to define the arena minimum number"));
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7of players."));
		lore.add(ChatColor.translateAlternateColorCodes('&', ""));
		lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Value: &7"+ paintballArena.getMinimumPlayerAmount()));
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(13, item);
		
		item = new ItemStack(Material.GHAST_TEAR, paintballArena.getMaximumPlayerAmount());
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lSet Max Players"));
		lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to define the arena maximum number"));
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7of players."));
		lore.add(ChatColor.translateAlternateColorCodes('&', ""));
		lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Value: &7"+ paintballArena.getMaximumPlayerAmount()));
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(14, item);
		
		item = new ItemStack(Material.NAME_TAG,1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lSet Team 1 Color"));
		lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to define the arena team 1 Color."));
		lore.add(ChatColor.translateAlternateColorCodes('&', ""));
		if(paintballArena.getTeam1().isRandom()) {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Value: &7random"));
		}else {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Value: &7"+ paintballArena.getTeam1().getColor()));
		}
			
		
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(15, item);
		
		item = new ItemStack(Material.NAME_TAG,1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lSet Team 2 Color"));
		lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to define the arena team 2 Color."));
		lore.add(ChatColor.translateAlternateColorCodes('&', ""));
		if(paintballArena.getTeam2().isRandom()) {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Value: &7random"));
		}else {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Value: &7"+ paintballArena.getTeam2().getColor()));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(16, item);
		
		if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15")
				|| Bukkit.getVersion().contains("1.16")|| Bukkit.getVersion().contains("1.17")|| Bukkit.getVersion().contains("1.18")
				|| Bukkit.getVersion().contains("1.19") || Bukkit.getVersion().contains("1.20")) {
			item = new ItemStack(Material.CLOCK,1);
		}else {
			item = new ItemStack(Material.valueOf("WATCH"),1);
		}
		
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lSet Time"));
		lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to define the arena time in seconds."));
		lore.add(ChatColor.translateAlternateColorCodes('&', ""));
		lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Value: &7"+ paintballArena.getMaximumTime()));
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(21, item);
		
		item = new ItemStack(Material.REDSTONE_BLOCK,1);
		meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lSet Starting Lives"));
		lore = new ArrayList<String>();
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to define the starting amount of lives"));
		lore.add(ChatColor.translateAlternateColorCodes('&', "&7for both teams."));
		lore.add(ChatColor.translateAlternateColorCodes('&', ""));
		lore.add(ChatColor.translateAlternateColorCodes('&', "&9Current Value: &7"+ paintballArena.getInitialLives()));
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(23, item);
		
		jugador.openInventory(inv);
		
		PaintballArenaEdit p = new PaintballArenaEdit(jugador, paintballArena);
		plugin.setPaintballMatchEdit(p);
	}
	
	@EventHandler
	public void alCerrarInventario(InventoryCloseEvent event) {
		Player jugador = (Player) event.getPlayer();
		String pathInventory = ChatColor.translateAlternateColorCodes('&', "&2Editing Arena:");
		String pathInventoryM = ChatColor.stripColor(pathInventory);
		PaintballArenaEdit partida = plugin.getPaintballMatchEdit();
		if(partida != null && partida.getJugador().getName().equals(jugador.getName())) {
			if(ChatColor.stripColor(event.getView().getTitle()).contains(pathInventoryM)){
				plugin.removePaintballMatchEdit();
			}
		}
	}
	
	@EventHandler
	public void alSalir(PlayerQuitEvent event) {
		PaintballArenaEdit partida = plugin.getPaintballMatchEdit();
		Player jugador = event.getPlayer();
		if(partida != null && partida.getJugador().getName().equals(jugador.getName())) {
			plugin.removePaintballMatchEdit();
		}
	}
	
	@EventHandler
	public void clickInventario(InventoryClickEvent event){
		String pathInventory = ChatColor.translateAlternateColorCodes('&', "&2Editing Arena:");
		String pathInventoryM = ChatColor.stripColor(pathInventory);
		FileConfiguration messages = plugin.getMessages();
		String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
		if(ChatColor.stripColor(event.getView().getTitle()).contains(pathInventoryM)){
			if(event.getCurrentItem() == null){
				event.setCancelled(true);
				return;
			}
			if((event.getSlotType() == null)){
				event.setCancelled(true);
				return;
			}else{
				Player jugador = (Player) event.getWhoClicked();
				event.setCancelled(true);
				if(event.getClickedInventory().equals(jugador.getOpenInventory().getTopInventory())) {
					PaintballArenaEdit partida = plugin.getPaintballMatchEdit();
					if(partida != null && partida.getJugador().getName().equals(jugador.getName())) {
						int slot = event.getSlot();
						FileConfiguration config = plugin.getConfig();
						if(slot == 10) {
							partida.getPartida().setLobby(jugador.getLocation().clone());
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("lobbyDefined").replace("%name%", partida.getPartida().getMatchNumber())));
							InventoryAdmin.createInventory(jugador, partida.getPartida(),plugin);
						}else if(slot == 11) {
							partida.getPartida().getTeam1().setSpawn(jugador.getLocation().clone());
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("spawnTeamDefined").replace("%number%", "1").replace("%name%", partida.getPartida().getMatchNumber())));
							InventoryAdmin.createInventory(jugador, partida.getPartida(),plugin);
						}else if(slot == 12) {
							partida.getPartida().getTeam2().setSpawn(jugador.getLocation().clone());
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("spawnTeamDefined").replace("%number%", "2").replace("%name%", partida.getPartida().getMatchNumber())));
							InventoryAdmin.createInventory(jugador, partida.getPartida(),plugin);
						}else if(slot == 13) {
							jugador.closeInventory();
							PaintballArenaEdit p = new PaintballArenaEdit(jugador,partida.getPartida());
							p.setPaso("min");
							plugin.setPaintballMatchEdit(p);
							jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aWrite an even number."));
						}else if(slot == 14) {
							jugador.closeInventory();
							PaintballArenaEdit p = new PaintballArenaEdit(jugador,partida.getPartida());
							p.setPaso("max");
							plugin.setPaintballMatchEdit(p);
							jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aWrite an even number."));
						}else if(slot == 15) {
							jugador.closeInventory();
							PaintballArenaEdit p = new PaintballArenaEdit(jugador,partida.getPartida());
							p.setPaso("team1name");
							plugin.setPaintballMatchEdit(p);
							String lista = "";
							for(String key : config.getConfigurationSection("teams").getKeys(false)) {
								lista=lista+key+" ";
							}
							jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aWrite one of these team names: &7random "+lista));
						}else if(slot == 16) {
							jugador.closeInventory();
							PaintballArenaEdit p = new PaintballArenaEdit(jugador,partida.getPartida());
							p.setPaso("team2name");
							plugin.setPaintballMatchEdit(p);
							String lista = "";
							for(String key : config.getConfigurationSection("teams").getKeys(false)) {
								lista=lista+key+" ";
							}
							jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aWrite one of these team names: &7random "+lista));
						}else if(slot == 21) {
							jugador.closeInventory();
							PaintballArenaEdit p = new PaintballArenaEdit(jugador,partida.getPartida());
							p.setPaso("time");
							plugin.setPaintballMatchEdit(p);
							jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aWrite a number. This will be the arena time in seconds."));
						}else if(slot == 23) {
							jugador.closeInventory();
							PaintballArenaEdit p = new PaintballArenaEdit(jugador,partida.getPartida());
							p.setPaso("lives");
							plugin.setPaintballMatchEdit(p);
							jugador.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aWrite a number. This will be the amount of starting lives for each team."));
						}
					}
					
				}
			}
		}
	}
	
	@EventHandler
	public void capturarChat(AsyncPlayerChatEvent event) {
		final PaintballArenaEdit partida = plugin.getPaintballMatchEdit();
		final Player jugador = event.getPlayer();
		String message = ChatColor.stripColor(event.getMessage());
		if(partida != null && partida.getJugador().getName().equals(jugador.getName())) {
			event.setCancelled(true);
			FileConfiguration messages = plugin.getMessages();
			String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
			String paso = partida.getPaso();
			if(paso.equals("min")) {
				try {
					int num = Integer.valueOf(message);
					if(num >= 2 && num % 2 == 0) {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("minPlayersDefined").replace("%name%", partida.getPartida().getMatchNumber())));
						partida.getPartida().setMinimumPlayerAmount(num);
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							@Override
							public void run() {
								InventoryAdmin.createInventory(jugador, partida.getPartida(), plugin);
							}
						}, 3L);
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("validNumberError")));
					}
				}catch(NumberFormatException e) {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("validNumberError")));
				}
			}else if(paso.equals("max")) {
				try {
					int num = Integer.valueOf(message);
					if(num >= 2 && num % 2 == 0) {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("maxPlayersDefined").replace("%name%", partida.getPartida().getMatchNumber())));
						partida.getPartida().setMaximumPlayerAmount(num);
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							@Override
							public void run() {
								InventoryAdmin.createInventory(jugador, partida.getPartida(), plugin);
							}
						}, 3L);
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("validNumberError")));
					}
				}catch(NumberFormatException e) {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("validNumberError")));
				}
			}else if(paso.equals("team1name")) {
				FileConfiguration config = plugin.getConfig();

				if(config.contains("teams."+message) || message.equalsIgnoreCase("random")) {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("typeDefined").replace("%number%", "1").replace("%name%", partida.getPartida().getMatchNumber())));
					partida.getPartida().getTeam1().setColor(message);
					if(message.equalsIgnoreCase("random")) {
						partida.getPartida().getTeam1().setRandom(true);
					}else {
						partida.getPartida().getTeam1().setRandom(false);
					}
					partida.getPartida().modifyTeams(config);
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							InventoryAdmin.createInventory(jugador, partida.getPartida(), plugin);
						}
					}, 3L);
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', "&cThat team name doesn't exists."));
				}
			}else if(paso.equals("team2name")) {
				FileConfiguration config = plugin.getConfig();
				
				if(config.contains("teams."+message) || message.equalsIgnoreCase("random")) {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("typeDefined").replace("%number%", "2").replace("%name%", partida.getPartida().getMatchNumber())));
					partida.getPartida().getTeam2().setColor(message);
					if(message.equalsIgnoreCase("random")) {
						partida.getPartida().getTeam2().setRandom(true);
					}else {
						partida.getPartida().getTeam2().setRandom(false);
					}
					partida.getPartida().modifyTeams(config);
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							InventoryAdmin.createInventory(jugador, partida.getPartida(), plugin);
						}
					}, 3L);
				}else {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', "&cThat team name doesn't exists."));
				}
			}else if(paso.equals("time")) {
				try {
					int num = Integer.valueOf(message);
					if(num > 0) {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("timeDefined").replace("%name%", partida.getPartida().getMatchNumber())));
						partida.getPartida().setMaximumTime(num);
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							@Override
							public void run() {
								InventoryAdmin.createInventory(jugador, partida.getPartida(), plugin);
							}
						}, 3L);
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("validNumberError")));
					}
				}catch(NumberFormatException e) {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("validNumberError")));
				}
			}else if(paso.equals("lives")) {
				try {
					int num = Integer.valueOf(message);
					if(num > 0) {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("livesDefined").replace("%name%", partida.getPartida().getMatchNumber())));
						partida.getPartida().setInitialLives(num);
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							@Override
							public void run() {
								InventoryAdmin.createInventory(jugador, partida.getPartida(), plugin);
							}
						}, 3L);
					}else {
						jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("validNumberError")));
					}
				}catch(NumberFormatException e) {
					jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("validNumberError")));
				}
			}
		}
	}
}
