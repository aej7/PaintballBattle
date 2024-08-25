package pb.ajneb97;

import java.util.ArrayList;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pb.ajneb97.player.PaintballHat;
import pb.ajneb97.player.PaintballPerk;
import pb.ajneb97.database.PaintballPlayer;
import pb.ajneb97.database.MySql;
import pb.ajneb97.enums.ArenaState;
import pb.ajneb97.arena.PaintballArena;
import pb.ajneb97.arena.PaintballArenaEdit;
import pb.ajneb97.configuration.Checks;
import pb.ajneb97.admin.InventoryAdmin;
import pb.ajneb97.eventhandlers.InventoryInteractEventHandler;
import pb.ajneb97.arena.ArenaManager;
import pb.ajneb97.logic.TopHologram;
import pb.ajneb97.utils.OthersUtils;

public class CommandHandler implements CommandExecutor {
	
	private final PaintballBattle plugin;

	public CommandHandler(PaintballBattle plugin) {
		this.plugin = plugin;
	}

	private void reloadPlugin() {
		plugin.reloadConfig();
		plugin.reloadMessages();
		plugin.reloadShop();
		plugin.reloadSigns();
		plugin.reloadScoreboard();
		plugin.recargarHologramas();
	}

	private boolean hasAdminPermission(CommandSender sender) {
		return sender.isOp() || sender.hasPermission("paintball.admin");
	}

	private void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
	}

	private void handleCreateArenaCommand(org.bukkit.entity.Player player, String[] args) {
		if (args.length < 2) {
			sendMessage(player, plugin.getMessages().getString("commandCreateErrorUse"));
		}

		String arenaName = args[1];
		if (plugin.getMatch(arenaName) != null) {
			sendMessage(player, plugin.getMessages().getString("arenaAlreadyExists"));
			return;
		}

		FileConfiguration config = plugin.getConfig();
		if (!config.contains("MainLobby")) {
			sendMessage(player, plugin.getMessages().getString("noMainLobby"));
		}

		String team1 = "";
		String team2 = "";
		int i = 0;

		for (String key : Objects.requireNonNull(config.getConfigurationSection("teams")).getKeys(false)) {
			if (i == 0) {
				team1 = key;
			} else {
				team2 = key;
				break;
			}

			i++;
		}
	}
	
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args){
		FileConfiguration messages = plugin.getMessages();
		String prefix = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("prefix"))) + " ";
	   if (!(sender instanceof org.bukkit.entity.Player)) {
		   if	(args.length >= 1) {
			   if	(args[0].equalsIgnoreCase("givecoins")) {
				   giveCoins(sender, args, messages, prefix);
				 } else if (args[0].equalsIgnoreCase("reload")) {
					 reloadPlugin();
				   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(messages.getString("configReloaded"))));
			   }
		   }
		   return false;   	
	   }

	   Player player = (Player) sender;
	   if(args.length >= 1) {
		   if(args[0].equalsIgnoreCase("create")) {
			   if(player.isOp() || player.hasPermission("paintball.admin")) {
				   if(args.length >= 2) {
					   if(plugin.getMatch(args[1]) == null) {
						   FileConfiguration config = plugin.getConfig();
						   if(!config.contains("MainLobby")) {
							   player.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', messages.getString("noMainLobby")));
							   return true;
						   }
						   String equipo1 = "";
						   String equipo2 = "";
						   int i=0;
						   for(String key : Objects.requireNonNull(config.getConfigurationSection("teams")).getKeys(false)) {
							   if(i==0) {
								   equipo1 = key;
							   } else {
								   equipo2 = key;
								   break;
							   }

							   i++;
						   }
						   
						   PaintballArena paintballArena = new PaintballArena(args[1],Integer.valueOf(config.getString("arena_time_default")),equipo1,equipo2,Integer.valueOf(config.getString("team_starting_lives_default")));
						   plugin.addPaintballMatch(paintballArena);
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaCreated").replace("%name%", args[1])));
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaCreatedExtraInfo").replace("%name%", args[1])));
					   } else {
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyExists")));
					   }
				   } else {
					   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateErrorUse")));
				   }
			   } else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   } else if(args[0].equalsIgnoreCase("delete")) {
			   // /paintball delete <nombre>
			   if(player.isOp() || player.hasPermission("paintball.admin")) {
				   if(args.length >= 2) {
					   if(plugin.getMatch(args[1]) != null) {
						   plugin.removePaintballMatch(args[1]);
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDeleted").replace("%name%", args[1])));
					   }else {
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists")));
					   }
				   }else {
					   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandDeleteErrorUse")));
				   }
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("reload")) {
			   // /paintball reload
			   if(player.isOp() || player.hasPermission("paintball.admin")) {
				   plugin.reloadConfig();
				   plugin.reloadMessages();
				   plugin.reloadShop();
				   plugin.reloadSigns();
				   plugin.reloadScoreboard();
				   plugin.recargarHologramas();
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("configReloaded")));
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("setmainlobby")) {
			   // /paintball setmainlobby
			   if(player.isOp() || player.hasPermission("paintball.admin")) {
				   FileConfiguration config = plugin.getConfig();
				   
				   Location l = player.getLocation();
				   config.set("MainLobby.x", l.getX()+"");
				   config.set("MainLobby.y", l.getY()+"");
				   config.set("MainLobby.z", l.getZ()+"");
				   config.set("MainLobby.world", l.getWorld().getName());
				   config.set("MainLobby.pitch", l.getPitch());
				   config.set("MainLobby.yaw", l.getYaw());
				   plugin.saveConfig();
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("mainLobbyDefined")));
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("join")) {
			   // /paintball join <arena>
			   if(!Checks.checkTodo(plugin, player)) {
				   return false;
			   }
			   if(args.length >= 2) {
				   PaintballArena paintballArena = plugin.getMatch(args[1]);
				   if(paintballArena != null) {
					   if(paintballArena.isActivated()) {
						   if(plugin.getPlayersMatch(player.getName()) == null) {
							   if(!paintballArena.estaIniciada()) {
								   if(!paintballArena.estaLlena()) {
									   if(!OthersUtils.pasaConfigInventario(player, plugin.getConfig())) {
										   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("errorClearInventory")));
										   return true;
									   }
									   ArenaManager.onPlayerJoinsArena(paintballArena, player, plugin);
								   }else {
									   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaIsFull")));
								   }
							   }else {
								   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyStarted")));
							   }
						   }else {
							   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("alreadyInArena")));
						   }
					   }else {
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDisabledError")));
					   }
				   }else {
					   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists")));
				   }
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandJoinErrorUse")));
			   }
		   } else if(args[0].equalsIgnoreCase("joinrandom")) {
			   // /paintball joinrandom
			   if(plugin.getPlayersMatch(player.getName()) == null) {
				    PaintballArena paintballArenaNueva = ArenaManager.getPartidaDisponible(plugin);
					if(paintballArenaNueva == null) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("noArenasAvailable")));
					}else {
						ArenaManager.onPlayerJoinsArena(paintballArenaNueva, player, plugin);
					}
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("alreadyInArena")));
			   }
		   }else if(args[0].equalsIgnoreCase("leave")) {
			   // /paintball leave
			   PaintballArena paintballArena = plugin.getPlayersMatch(player.getName());
			   if(paintballArena != null) {
				   ArenaManager.onPlayerLeavesArena(paintballArena, player, false, plugin, false);;
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("notInAGame")));
			   }
		   }else if(args[0].equalsIgnoreCase("shop")) {   
			   // /paintball shop
			   if(!Checks.checkTodo(plugin, player)) {
				   return false;
			   }
			   InventoryInteractEventHandler.crearInventarioPrincipal(player, plugin);
		   }else if(args[0].equalsIgnoreCase("enable")) {
			   // /paintball enable <arena>
			   //Para activar una arena todo debe estar definido
			   if(player.isOp() || player.hasPermission("paintball.admin")) {
				   if(args.length >= 2) {
					   PaintballArena paintballArena = plugin.getMatch(args[1]);
					   if(paintballArena != null) {
						   if(paintballArena.isActivated()) {
							   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyEnabled")));
						   }else {
							   if(paintballArena.getLobby() == null) {
								   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("enableArenaLobbyError")));
								   return true;
							   }
							   if(paintballArena.getTeam1().getSpawn() == null) {
								   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("enableArenaSpawnError").replace("%number%", "1")));
								   return true;
							   }
							   if(paintballArena.getTeam2().getSpawn() == null) {
								   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("enableArenaSpawnError").replace("%number%", "2")));
								   return true;
							   }
							   
							   paintballArena.setState(ArenaState.WAITING);
							   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaEnabled").replace("%name%", args[1])));
						   }
					   }else {
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists")));
					   }
				   }else {
					   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandEnableErrorUse")));
				   }
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("disable")) {
			   // /paintball disable <arena>
			   if(player.isOp() || player.hasPermission("paintball.admin")) {
				   if(args.length >= 2) {
					   PaintballArena paintballArena = plugin.getMatch(args[1]);
					   if(paintballArena != null) {
						   if(!paintballArena.isActivated()) {
							   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaAlreadyDisabled")));
						   }else {
							   paintballArena.setState(ArenaState.OFF);
							   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDisabled").replace("%name%", args[1])));
						   }
					   }else {
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists")));
					   }
				   }else {
					   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandDisableErrorUse")));
				   }
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("edit")) {
			   // /paintball edit <arena>  
			   if(player.isOp() || player.hasPermission("paintball.admin")) {
				   if(!Checks.checkTodo(plugin, player)) {
					   return false;
				   }
				   if(args.length >= 2) {
					   PaintballArena paintballArena = plugin.getMatch(args[1]);
					   if(paintballArena != null) {
						   if(!paintballArena.isActivated()) {
							   PaintballArenaEdit p = plugin.getPaintballMatchEdit();
							   if(p == null) {
								   
								   InventoryAdmin.createInventory(player, paintballArena,plugin);
							   }else {
								   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaModifyingError")));
							   }
						   }else {
							   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaMustBeDisabled")));
						   }
					   }else {
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaDoesNotExists")));
					   }
				   }else {
					   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandAdminErrorUse")));
				   }
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("createtophologram")) {
			   // /paintball createtophologram <name> kills/wins <global/monthly/weekly>
			   if(player.isOp() || player.hasPermission("paintball.admin")) {
				   if (plugin.getServer().getPluginManager().getPlugin("HolographicDisplays") == null) {
					   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', "&cYou need HolographicDisplays plugin to use this feature."));
				        return true;
				   }
				   if(args.length >= 3) {
					   if(args[2].equalsIgnoreCase("kills") || args[2].equalsIgnoreCase("wins")) {
						   TopHologram topHologram = plugin.getTopHologram(args[1]);
						   if(topHologram == null) {
							   String period = "global";
							   if(args.length >= 4) {
								   period = args[3];
							   }
							   if(period.equalsIgnoreCase("global") || period.equalsIgnoreCase("monthly") || period.equalsIgnoreCase("weekly")) {
								   if(!MySql.isEnabled(plugin.getConfig()) && (period.equalsIgnoreCase("monthly") || period.equalsIgnoreCase("weekly"))) {
									   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramPeriodSQLError")));
									   return true;
								   }
								   TopHologram hologram = new TopHologram(args[1],args[2],player.getLocation(),plugin,period);
								   plugin.addTopHologram(hologram);
								   hologram.spawnHologram(plugin);
								   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramCreated")));
							   }else {
								   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateHologramErrorUse")));
							   }					    
						   }else {
							   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramAlreadyExists")));
						   }
					   }else {
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateHologramErrorUse")));
					   }  
				   }else {
					   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandCreateHologramErrorUse")));
				   }
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("removetophologram")) {
			   // /paintball removetophologram <name>
			   if(player.isOp() || player.hasPermission("paintball.admin")) {
				   if (plugin.getServer().getPluginManager().getPlugin("HolographicDisplays") == null) {
					   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', "&cYou need HolographicDisplays plugin to use this feature."));
				        return true;
				   }
				   if(args.length >= 2) {
					   TopHologram topHologram = plugin.getTopHologram(args[1]);
					   if(topHologram != null) {
						   plugin.removeTopHologram(args[1]);
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramRemoved")));
					   }else {
						   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("topHologramDoesNotExists")));
					   }  
				   }else {
					   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandRemoveHologramErrorUse")));
				   }
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }else if(args[0].equalsIgnoreCase("givecoins")) {
			   // /paintball givecoins <player> <amount>
			   if(player.isOp() || player.hasPermission("paintball.admin")) {
				   giveCoins(sender,args,messages,prefix);
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
		   }
		   else {
			   // /paintball help /o cualquier otro comando
			   if(player.isOp() || player.hasPermission("paintball.admin")) {
				   sendHelp(player);
			   }else {
				   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
			   }
			   
		   }
	   }else {
		   if(player.isOp() || player.hasPermission("paintball.admin")) {
			   sendHelp(player);
		   }else {
			   player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("noPermissions")));
		   }
	   }
	   
	   return true;
	   
	}
	
	public void giveCoins(CommandSender sender, String[] args, FileConfiguration messages, String prefix) {
		if(args.length >= 3) {
			   String player = args[1];
			   try {
				   int amount = Integer.parseInt(args[2]);
				   //Si el jugador no esta en la base de datos, o en un archivo, DEBE estar conectado para darle coins.
				   if(MySql.isEnabled(plugin.getConfig())) {
					   if(MySql.playerExists(plugin, player)) {
						   MySql.addPlayerCoins(plugin, player, amount);
						   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("giveCoinsMessage").replace("%player%", player).replace("%amount%", amount+"")));
						   org.bukkit.entity.Player p = Bukkit.getPlayer(player);
						   if(p != null) {
							   p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("receiveCoinsMessage").replace("%amount%", amount+""))); 
						   } 
					   }else {
						   org.bukkit.entity.Player p = Bukkit.getPlayer(player);
						   if(p != null) {
							   MySql.createPlayerArenaAsync(plugin, p.getUniqueId().toString(), p.getName(), "", 0, 0, 0, 0, amount, 1);
							   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("giveCoinsMessage").replace("%player%", player).replace("%amount%", amount+"")));
							   p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("receiveCoinsMessage").replace("%amount%", amount+"")));
						   }else {
							   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("errorPlayerOnline")));
						   }
					   }
				   } else {
					   org.bukkit.entity.Player p = Bukkit.getPlayer(player);
					   if(p != null) {
						   plugin.registerPlayer(p.getUniqueId().toString()+".yml");
						   if(plugin.getPlayer(p.getName()) == null) {
								plugin.addPlayer(new PaintballPlayer(p.getName(),p.getUniqueId().toString(),0,0,0,0,0,new ArrayList<PaintballPerk>(),new ArrayList<PaintballHat>()));
						   }
						   PaintballPlayer jDatos = plugin.getPlayer(p.getName());
						   jDatos.increaseCoinsByAmount(amount);
						   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("giveCoinsMessage").replace("%player%", player).replace("%amount%", amount+"")));
						   p.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("receiveCoinsMessage").replace("%amount%", amount+""))); 
					   }else {
						   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("errorPlayerOnline")));
					   }
				   }
				   
			   }catch(NumberFormatException e) {
				   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("validNumberError")));
			   }
			   
		   }else {
			   sender.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("commandGiveCoinsErrorUse")));
		   }
	}
	
	public void sendHelp(Player player) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[ [ &4[&fPaintball Battle&4] &7] ]"));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',""));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball create <arena> &8Creates a new arena."));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball delete <arena> &8Deletes an arena."));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball join <arena> &8Joins an arena."));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball joinrandom &8Joins a random arena."));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball leave &8Leaves from the arena."));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball shop &8Opens the Paintball Shop."));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball givecoins <player> <amount>"));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball setmainlobby &8Defines the minigame main lobby."));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball enable <arena> &8Enables an arena."));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball disable <arena> &8Disables an arena."));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball edit <arena> &8Edit the properties of an arena."));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball createtophologram <name> <kills/wins> <global/monthly/weekly>"));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball removetophologram <name>"));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6/paintball reload &8Reloads the configuration files."));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',""));
		   player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7[ [ &4[&fPaintball Battle&4] &7] ]"));
	}
}
