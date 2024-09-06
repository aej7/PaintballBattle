package pb.ajneb97.arena;

import java.util.*;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.logic.CooldownKillstreaks;
import pb.ajneb97.logic.CooldownManager;
import pb.ajneb97.player.PaintballTeam;
import pb.ajneb97.player.PaintballHat;
import pb.ajneb97.database.PaintballPlayerRepository;
import pb.ajneb97.player.PaintballPerk;
import pb.ajneb97.configuration.PaintballPlayerSavedData;
import pb.ajneb97.database.MySql;
import pb.ajneb97.enums.ArenaState;
import pb.ajneb97.lib.titleapi.TitleAPI;
import pb.ajneb97.utils.ItemsUtils;
import pb.ajneb97.utils.OthersUtils;

public class ArenaManager {

	private static void broadcastPlayerJoinMessage(PaintballArena paintballArena, Player player, FileConfiguration messages) {
		String joinMessage = ChatColor.translateAlternateColorCodes('&',
			messages.getString("playerJoin")
				.replace("%player%", player.getName())
				.replace("%current_players%", String.valueOf(paintballArena.getPlayerAmount()))
				.replace("%max_players%", String.valueOf(paintballArena.getMaximumPlayerAmount()))
		);

		for (PaintballPlayer matchPlayer : paintballArena.getPlayers()) {
			matchPlayer.getPlayer().sendMessage(joinMessage);
		}
	}

	public static void onPlayerJoinsArena(PaintballArena paintballArena, Player player, PaintballBattle plugin) {
		PaintballPlayer paintballPlayer = new PaintballPlayer(player);
		FileConfiguration messages = plugin.getMessages();
		paintballArena.addPlayer(paintballPlayer);

		broadcastPlayerJoinMessage(paintballArena, player, messages);

		resetPlayerState(player);

		player.teleport(paintballArena.getLobby());

		FileConfiguration config = plugin.getConfig();
		giveSpecialItems(player, config, messages, paintballArena);

		if (paintballArena.getPlayers().size() >= paintballArena.getMinimumPlayerAmount() && paintballArena.getState().equals(ArenaState.WAITING)) {
			startArenaCooldown(paintballArena, plugin);
		}
	}

	private static void resetPlayerState(Player player) {
		player.getInventory().clear();
		player.getEquipment().clear();
		player.updateInventory();

		// Reset player status
		player.setGameMode(GameMode.SURVIVAL);
		player.setExp(0);
		player.setLevel(0);
		player.setFoodLevel(20);
		//TODO more health bug v
		player.setMaxHealth(20);
		//player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
		player.setHealth(20);
		player.setFlying(false);
		player.setAllowFlight(false);

		// Remove all potion effects
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
	}

	private static void giveSpecialItems(Player player, FileConfiguration config, FileConfiguration messages, PaintballArena paintballArena) {
		addItemToPlayerInventory(player, config, "leave_item", 8);
		addItemToPlayerInventory(player, config, "hats_item", 7);

		if (config.getBoolean("choose_team_system")) {
			giveTeamSelectionItems(player, config, messages, paintballArena);
		}
	}

	private static void addItemToPlayerInventory(Player player, FileConfiguration config, String configPath, int inventorySlot) {
		if (config.getBoolean(configPath + "_enabled")) {
			ItemStack item = ItemsUtils.creaItem(config, configPath);
			player.getInventory().setItem(inventorySlot, item);
		}
	}

	private static void giveTeamSelectionItems(Player player, FileConfiguration config, FileConfiguration messages, PaintballArena paintballArena) {
		String team1Path = "teams." + paintballArena.getTeam1().getColor();
		String team2Path = "teams." + paintballArena.getTeam2().getColor();

		ItemStack team1Item = createTeamItem(config, messages, team1Path);
		ItemStack team2Item = createTeamItem(config, messages, team2Path);

		player.getInventory().setItem(0, team1Item);
		player.getInventory().setItem(1, team2Item);
	}

	private static ItemStack createTeamItem(FileConfiguration config, FileConfiguration messages, String teamPath) {
		ItemStack item = ItemsUtils.creaItem(config, teamPath);
		ItemMeta itemMeta = item.getItemMeta();

		if (itemMeta != null) { // Always check for null to avoid potential NPEs
			String teamName = config.getString(teamPath + ".name");
			String displayName = ChatColor.translateAlternateColorCodes('&', messages.getString("teamChoose").replace("%team%", teamName));
			itemMeta.setDisplayName(displayName);
			item.setItemMeta(itemMeta);
		}

		return item;
	}

	public static void onPlayerLeavesArena(PaintballArena paintballArena, Player player, boolean hasMatchEnded,
																				 PaintballBattle plugin, boolean isServerClosing) {
		PaintballPlayer paintballPlayer = paintballArena.getPlayer(player.getName());
		restorePlayerState(player, paintballArena);

		paintballArena.removePlayer(player.getName());

		if (!hasMatchEnded) {
			broadcastPlayerLeft(paintballArena, player, plugin.getMessages());
		}

		teleportPlayerToMainLobby(player, plugin.getConfig());

		if (!isServerClosing) {
			updateArenaState(paintballArena, plugin);
		}
	}

	private static void restorePlayerState(Player player, PaintballArena paintballArena) {
		PaintballPlayerSavedData playerPaintballPlayerSavedData = paintballArena.getPlayer(player.getName()).getSavedData();

		player.getInventory().setContents(playerPaintballPlayerSavedData.getInventory());
		player.getEquipment().setArmorContents(playerPaintballPlayerSavedData.getEquipment());
		player.setGameMode(playerPaintballPlayerSavedData.getGamemode());
		player.setLevel(playerPaintballPlayerSavedData.getLevel());
		player.setExp(playerPaintballPlayerSavedData.getXp());
		player.setFoodLevel(playerPaintballPlayerSavedData.GetHunger());
		player.setMaxHealth(playerPaintballPlayerSavedData.getMaxHealth());
		player.setHealth(playerPaintballPlayerSavedData.getHealth());
		player.setAllowFlight(playerPaintballPlayerSavedData.isFlightAllowed());
		player.setFlying(playerPaintballPlayerSavedData.isFlying());

		for (PotionEffect potionEffect : player.getActivePotionEffects()) {
			player.removePotionEffect(potionEffect.getType());
		}
	}

	private static void broadcastPlayerLeft(PaintballArena paintballArena, Player player, FileConfiguration messages) {
		String leaveMessage = ChatColor.translateAlternateColorCodes('&',
			messages.getString("playerLeave")
				.replace("%player%", player.getName())
				.replace("%current_players%", String.valueOf(paintballArena.getPlayerAmount()))
				.replace("%max_players%", String.valueOf(paintballArena.getMaximumPlayerAmount()))
		);

		for (PaintballPlayer matchPlayer : paintballArena.getPlayers()) {
			matchPlayer.getPlayer().sendMessage(leaveMessage);
		}
	}

	private static void teleportPlayerToMainLobby(Player player, FileConfiguration config) {
		Location mainLobby = new Location(
			Bukkit.getWorld(config.getString("MainLobby.world")),
			config.getDouble("MainLobby.x"),
			config.getDouble("MainLobby.y"),
			config.getDouble("MainLobby.z"),
			(float) config.getDouble("MainLobby.yaw"),
			(float) config.getDouble("MainLobby.pitch")
		);

		player.teleport(mainLobby);
	}

	private static void updateArenaState(PaintballArena paintballArena, PaintballBattle plugin) {
		int playerCount = paintballArena.getPlayerAmount();

		if (playerCount < paintballArena.getMinimumPlayerAmount() && paintballArena.getState().equals(ArenaState.STARTING)) {
			paintballArena.setState(ArenaState.WAITING);
		} else if (playerCount <= 1 && paintballArena.getState().equals(ArenaState.PLAYING)) {
			ArenaManager.setArenaStateToEnding(paintballArena, plugin);
		} else if ((paintballArena.getTeam1().getCurrentSize() == 0 || paintballArena.getTeam2().getCurrentSize() == 0)
			&& paintballArena.getState().equals(ArenaState.PLAYING)) {
			ArenaManager.setArenaStateToEnding(paintballArena, plugin);
		}
	}

	public static void startArenaCooldown(PaintballArena paintballArena, PaintballBattle plugin) {
		paintballArena.setState(ArenaState.STARTING);

		FileConfiguration config = plugin.getConfig();
		FileConfiguration messages = plugin.getMessages();
		int cooldownTime = config.getInt("arena_starting_cooldown");

		CooldownManager cooldownManager = new CooldownManager(plugin);
		cooldownManager.setMatchStartCooldown(paintballArena, cooldownTime);

		String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix")) + " ";

		if (config.getBoolean("broadcast_starting_arena.enabled")) {
			broadcastArenaStarting(paintballArena, plugin, prefix);
		}
	}

	private static void broadcastArenaStarting(PaintballArena paintballArena, PaintballBattle plugin, String prefix) {
		FileConfiguration messages = plugin.getMessages();
		String broadcastMessage = ChatColor.translateAlternateColorCodes('&', messages.getString("arenaStartingBroadcast")
			.replace("%arena%", paintballArena.getMatchNumber()));

		List<String> broadcastWorlds = plugin.getConfig().getStringList("broadcast_starting_arena.worlds");
		for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
			if (broadcastWorlds.contains(player.getWorld().getName())) {
				player.sendMessage(prefix + broadcastMessage);
			}
		}
	}
	
	public static void startArena(PaintballArena paintballArena, PaintballBattle plugin) {
		paintballArena.setState(ArenaState.PLAYING);
		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();
		//String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
		
		if(plugin.getConfig().getString("choose_team_system").equals("true")) {
			setTeams(paintballArena);
		} else {
			setTeamsRandom(paintballArena);
		}

		giveItems(paintballArena, plugin.getConfig(), plugin.getShop(),p lugin.getMessages());
		teletransportarJugadores(paintballArena);
		setLives(paintballArena,plugin.getShop());
		
		ArrayList<pb.ajneb97.logic.PaintballPlayer> paintballPlayers = paintballArena.getPlayers();
		String[] sep = config.getString("startGameSound").split(";");
		Sound sound = null;
		float volume = 0;
		float pitch = 0;
		try {
			sound = Sound.valueOf(sep[0]);
			volume = Float.parseFloat(sep[1]);
			pitch = Float.parseFloat(sep[2]);
		}catch(Exception ex) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+sep[0]+" &7is not valid."));
			sound = null;
		}
    for (pb.ajneb97.logic.PaintballPlayer paintballPlayer : paintballPlayers) {
      String teamColor = paintballArena.GetPlayerTeam(paintballPlayer.getPlayer().getName()).getColor();
      paintballPlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("gameStarted")));
      paintballPlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("teamInformation").replace("%team%", plugin.getConfig().getString("teams." + teamColor + ".name"))));
      paintballPlayer.getPlayer().closeInventory();
      if (sound != null) {
        paintballPlayer.getPlayer().playSound(paintballPlayer.getPlayer().getLocation(), sound, volume, pitch);
      }
    }
		
		CooldownManager cooldown = new CooldownManager(plugin);
		cooldown.cooldownJuego(paintballArena);
	}



	public static void setLives(PaintballArena paintballArena, FileConfiguration shop) {
		// Set initial lives for both teams
		paintballArena.getTeam1().setLives(paintballArena.getInitialLives());
		paintballArena.getTeam2().setLives(paintballArena.getInitialLives());

		// Handle extra lives for both teams
		updateTeamLives(paintballArena.getTeam1(), shop);
		updateTeamLives(paintballArena.getTeam2(), shop);
	}

	private static void updateTeamLives(PaintballTeam paintballTeam, FileConfiguration shop) {
		for (pb.ajneb97.logic.PaintballPlayer paintballPlayer : paintballTeam.getPlayers().values()) {
			int extraLivesPerkLevel = PaintballPlayerRepository.getPerkLevel(paintballPlayer.getPlayer(), "extra_lives");
			if (extraLivesPerkLevel > 0) {
				String line = shop.getStringList("perks_upgrades.extra_lives").get(extraLivesPerkLevel - 1);
				String[] sep = line.split(";");
				int amount = Integer.parseInt(sep[0]);
				paintballTeam.increaseLives(amount);
			}
		}
	}
	
	public static void killstreakInstantanea(String key, pb.ajneb97.database.PaintballPlayer paintballPlayer, PaintballArena paintballArena, PaintballBattle plugin) {
		FileConfiguration config = plugin.getConfig();
		if(key.equalsIgnoreCase("3_lives")) {
			PaintballTeam paintballTeam = paintballArena.GetPlayerTeam(paintballPlayer.getName());
			paintballTeam.increaseLives(3);
		}else if(key.equalsIgnoreCase("teleport")) {
			pb.ajneb97.logic.PaintballPlayer j = paintballArena.getPlayer(paintballPlayer.getName());
			if(j.getDeathLocation() != null) {
				j.getPlayer().teleport(j.getDeathLocation());
			}else {
				PaintballTeam paintballTeam = paintballArena.GetPlayerTeam(paintballPlayer.getName());
				j.getPlayer().teleport(paintballTeam.getSpawn());
			}
		}else if(key.equalsIgnoreCase("more_snowballs")) {
			pb.ajneb97.logic.PaintballPlayer j = paintballArena.getPlayer(paintballPlayer.getName());
			int snowballs = Integer.valueOf(config.getString("killstreaks_items."+key+".snowballs"));
			ItemStack item = null;
			if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15")
					|| Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.18")
					|| Bukkit.getVersion().contains("1.19") || Bukkit.getVersion().contains("1.20")) {
				if(j.getSelectedHat().equals("chicken_hat")) {
					item = new ItemStack(Material.EGG,1);
				}else {
					item = new ItemStack(Material.SNOWBALL,1);
				}
				
			}else {
				if(j.getSelectedHat().equals("chicken_hat")) {
					item = new ItemStack(Material.EGG,1);
				}else {
					item = new ItemStack(Material.valueOf("SNOW_BALL"),1);
				}
				
			}
			for(int i=0;i<snowballs;i++) {
				paintballPlayer.getInventory().addItem(item);
			}
		} else if(key.equalsIgnoreCase("lightning")) {
			pb.ajneb97.logic.PaintballPlayer jugadorAtacante = paintballArena.getPlayer(paintballPlayer.getName());
			int radio = Integer.valueOf(config.getString("killstreaks_items."+key+".radius"));
			Collection<Entity> entidades = paintballPlayer.getWorld().getNearbyEntities(paintballPlayer.getLocation(), radio, radio, radio);
			for(Entity e : entidades) {
				if(e != null && e.getType().equals(EntityType.PLAYER)) {
					org.bukkit.entity.Player player = (org.bukkit.entity.Player) e;
					pb.ajneb97.logic.PaintballPlayer jugadorDañado = paintballArena.getPlayer(player.getName());
					if(jugadorDañado != null) {
						ArenaManager.muereJugador(paintballArena, jugadorAtacante, jugadorDañado, plugin, true, false);
					}
				}
			}
		}else if(key.equalsIgnoreCase("nuke")) {
			paintballArena.setNuke(true);
			pb.ajneb97.logic.PaintballPlayer jugadorAtacante = paintballArena.getPlayer(paintballPlayer.getName());
			CooldownKillstreaks c = new CooldownKillstreaks(plugin);
			String[] separados1 = config.getString("killstreaks_items."+key+".activateSound").split(";");
			String[] separados2 = config.getString("killstreaks_items."+key+".finalSound").split(";");
			c.cooldownNuke(jugadorAtacante, paintballArena, separados1, separados2);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void setTeamsRandom(PaintballArena paintballArena) {
		ArrayList<pb.ajneb97.logic.PaintballPlayer> jugadores = paintballArena.getPlayers();
		ArrayList<pb.ajneb97.logic.PaintballPlayer> jugadoresCopia = (ArrayList<pb.ajneb97.logic.PaintballPlayer>) paintballArena.getPlayers().clone();
		//Si son 4 se seleccionan 2, Si son 5 tambien 2, Si son 6, 3, Si son 7, tambien 3
		Random r = new Random();
		int num = jugadores.size()/2;
		for(int i=0;i<num;i++) {
			int pos = r.nextInt(jugadoresCopia.size());
			pb.ajneb97.logic.PaintballPlayer jugadorSelect = jugadoresCopia.get(pos);
			jugadoresCopia.remove(pos);
			
			paintballArena.changePlayerToTeam2(jugadorSelect);
		}
	}
	
	private static void setTeams(PaintballArena paintballArena) {
		//Falta comprobar lo siguiente:
		//Si 2 usuarios seleccionan team y uno se va, los 2 usuarios estaran en el mismo team al
		//iniciar la partida y seran solo ellos 2.
		
		ArrayList<pb.ajneb97.logic.PaintballPlayer> jugadores = paintballArena.getPlayers();
		for(pb.ajneb97.logic.PaintballPlayer j : jugadores) {
			paintballArena.GetPlayerTeam(j.getPlayer().getName()).removePlayer(j.getPlayer().getName());
			String preferenciaTeam = j.getPreferenciaTeam();
			if(preferenciaTeam == null) {
				if(paintballArena.puedeSeleccionarEquipo(paintballArena.getTeam1().getColor())) {
					j.setPreferenciaTeam(paintballArena.getTeam1().getColor());
				}else {
					j.setPreferenciaTeam(paintballArena.getTeam2().getColor());
				}
			}
			preferenciaTeam = j.getPreferenciaTeam();
			if(preferenciaTeam.equals(paintballArena.getTeam2().getColor())) {
				paintballArena.getTeam2().addPlayer(j);
			}else {
				paintballArena.getTeam1().addPlayer(j);
			}
		}
		
		//Balanceo final
		PaintballTeam paintballTeam1 = paintballArena.getTeam1();
		PaintballTeam paintballTeam2 = paintballArena.getTeam2();
		for(pb.ajneb97.logic.PaintballPlayer j : jugadores) {
			PaintballTeam paintballTeam = paintballArena.GetPlayerTeam(j.getPlayer().getName());
			if(paintballTeam1.getCurrentSize() > paintballTeam2.getCurrentSize()+1) {
				if(paintballTeam.getColor().equals(paintballTeam1.getColor())) {
					//Mover al jugador del equipo1 al equipo2
					paintballTeam1.removePlayer(j.getPlayer().getName());
					paintballTeam2.addPlayer(j);
				}
			}else if(paintballTeam2.getCurrentSize() > paintballTeam1.getCurrentSize()+1) {
				if(paintballTeam.getColor().equals(paintballTeam2.getColor())) {
					//Mover al jugador del equipo2 al equipo1
					paintballTeam2.removePlayer(j.getPlayer().getName());
					paintballTeam1.addPlayer(j);
				}
			}
		}
	}
	
	public static void giveItems(PaintballArena paintballArena, FileConfiguration config, FileConfiguration shop, FileConfiguration messages) {
		for(PaintballPlayer paintballPlayer : paintballArena.getPlayers()) {
			Player player = paintballPlayer.getPlayer();
			player.getInventory().setItem(8, null);
			PaintballTeam paintballTeam = paintballArena.GetPlayerTeam(player.getName());

			if(config.contains("teams."+ paintballTeam.getColor())) {
				givePlayerEquipment(player,Integer.parseInt(config.getString("teams." + paintballTeam.getColor() + ".color")));
			} else {
				givePlayerEquipment(player,0);
			}

			int initialKillcoinsPerkLevel = PaintballPlayerRepository.getPerkLevel(paintballPlayer.getPlayer(), "initial_killcoins");
			if(initialKillcoinsPerkLevel != 0) {
				String line = shop.getStringList("perks_upgrades.initial_killcoins").get(initialKillcoinsPerkLevel-1);
				String[] sep = line.split(";");
				int amount = Integer.parseInt(sep[0]);
				paintballPlayer.addCoins(amount);
			}
			ItemsUtils.createItemKillstreaks(paintballPlayer, config);
			putHat(paintballArena, paintballPlayer, config, messages);
			setSnowballs(paintballPlayer, config);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void putHat(PaintballArena paintballArena, PaintballPlayer paintballPlayer, FileConfiguration config, FileConfiguration messages) {
		ArrayList<PaintballHat> paintballHats = PaintballPlayerRepository.getHats(paintballPlayer.getPlayer());
		for(PaintballHat paintballHat : paintballHats) {
			if(paintballHat.isEquipped()) {
				paintballPlayer.setSelectedHat(paintballHat.getName());
				ItemStack item = ItemsUtils.creaItem(config, "hats_items."+ paintballHat.getName());
				ItemMeta meta = item.getItemMeta();
				meta.setLore(null);
				item.setItemMeta(meta);
				if(config.contains("hats_items."+ paintballHat.getName()+".skull_id")) {
					String id = config.getString("hats_items."+ paintballHat.getName()+".skull_id");
					String textura = config.getString("hats_items."+ paintballHat.getName()+".skull_texture");
					item = ItemsUtils.getCabeza(item, id, textura);
				}
				paintballPlayer.getPlayer().getEquipment().setHelmet(item);
				
				if(paintballHat.getName().equals("speed_hat")) {
					paintballPlayer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED,9999999,0,false,false));
				}else if(paintballHat.getName().equals("present_hat")) {
					PaintballTeam paintballTeam = paintballArena.GetPlayerTeam(paintballPlayer.getPlayer().getName());
					ArrayList<pb.ajneb97.logic.PaintballPlayer> jugadoresCopy = (ArrayList<pb.ajneb97.logic.PaintballPlayer>) paintballTeam.getPlayers().clone();
					jugadoresCopy.remove(paintballPlayer);
					if(!jugadoresCopy.isEmpty()) {
						Random r = new Random();
						int pos = r.nextInt(jugadoresCopy.size());
						String jName = jugadoresCopy.get(pos).getPlayer().getName();
						pb.ajneb97.logic.PaintballPlayer j = paintballArena.getPlayer(jName);
						j.addCoins(3);
						paintballPlayer.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("presentHatGive").replace("%player%", j.getPlayer().getName())));
						j.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("presentHatReceive").replace("%player%", paintballPlayer.getPlayer().getName())));
					}	
				}
				return;
			}
		}
	}
	
	public static void givePlayerEquipment(org.bukkit.entity.Player jugador, int color) {
		ItemStack item = new ItemStack(Material.LEATHER_HELMET,1);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(color));
		item.setItemMeta(meta);
		jugador.getInventory().setHelmet(item);
		
		item = new ItemStack(Material.LEATHER_CHESTPLATE,1);
		meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(color));
		item.setItemMeta(meta);
		jugador.getInventory().setChestplate(item);
		
		item = new ItemStack(Material.LEATHER_LEGGINGS,1);
		meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(color));
		item.setItemMeta(meta);
		jugador.getInventory().setLeggings(item);
		
		item = new ItemStack(Material.LEATHER_BOOTS,1);
		meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(color));
		item.setItemMeta(meta);
		jugador.getInventory().setBoots(item);
	}
	
	public static void setSnowballs(pb.ajneb97.logic.PaintballPlayer j, FileConfiguration config) {
		for(int i=0;i<=7;i++) {
			j.getPlayer().getInventory().setItem(i, null);
		}
		for(int i=9;i<=35;i++) {
			j.getPlayer().getInventory().setItem(i, null);
		}
		int amount = Integer.valueOf(config.getString("initial_snowballs"));
		ItemStack item = null;
		if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15")
				|| Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.18")
				|| Bukkit.getVersion().contains("1.19") || Bukkit.getVersion().contains("1.20")) {
			if(j.getSelectedHat().equals("chicken_hat")) {
				item = new ItemStack(Material.EGG,1);
			}else {
				item = new ItemStack(Material.SNOWBALL,1);
			}
		}else {
			if(j.getSelectedHat().equals("chicken_hat")) {
				item = new ItemStack(Material.EGG,1);
			}else {
				item = new ItemStack(Material.valueOf("SNOW_BALL"),1);
			}
		}

		for(int i=0;i<amount;i++) {
			j.getPlayer().getInventory().addItem(item);
		}
	}
	
	public static void lanzarFuegos(ArrayList<pb.ajneb97.logic.PaintballPlayer> jugadores) {
		for(pb.ajneb97.logic.PaintballPlayer j : jugadores) {
			Firework fw = (Firework) j.getPlayer().getWorld().spawnEntity(j.getPlayer().getLocation(), EntityType.FIREWORK_ROCKET);
	        FireworkMeta fwm = fw.getFireworkMeta();
	        Type type = Type.BALL;
	        Color c1 = Color.RED;
	        Color c2 = Color.AQUA;
	        FireworkEffect efecto = FireworkEffect.builder().withColor(c1).withFade(c2).with(type).build();
	        fwm.addEffect(efecto);
	        fwm.setPower(2);
	        fw.setFireworkMeta(fwm);
		}	
	}
	
	public static void teletransportarJugadores(PaintballArena paintballArena) {
		ArrayList<pb.ajneb97.logic.PaintballPlayer> jugadores = paintballArena.getPlayers();
		for(pb.ajneb97.logic.PaintballPlayer j : jugadores) {
			org.bukkit.entity.Player p = j.getPlayer();
			PaintballTeam paintballTeam = paintballArena.GetPlayerTeam(p.getName());
			p.teleport(paintballTeam.getSpawn());
		}
	}
	
	public static void setArenaStateToEnding(PaintballArena paintballArena, PaintballBattle plugin) {
		paintballArena.setState(ArenaState.ENDING);
		PaintballTeam ganador = paintballArena.getGanador();
		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();
		
		String nameTeam1 = config.getString("teams."+ paintballArena.getTeam1().getColor()+".name");
		String nameTeam2 = config.getString("teams."+ paintballArena.getTeam2().getColor()+".name");
		
		String status = "";
		if(ganador == null) {
			//empate
			status = messages.getString("gameFinishedTieStatus");
		}else {
			String ganadorTexto = plugin.getConfig().getString("teams."+ganador.getColor()+".name");
			status = messages.getString("gameFinishedWinnerStatus").replace("%winner_team%", ganadorTexto);
		}	
				
		ArrayList<pb.ajneb97.logic.PaintballPlayer> jugadoresKillsOrd = paintballArena.getJugadoresKills();
		String top1 = "";
		String top2 = "";
		String top3 = "";
		int top1Kills = 0;
		int top2Kills = 0;
		int top3Kills = 0;
		
		if(jugadoresKillsOrd.size() == 2) {
			top1 = jugadoresKillsOrd.get(0).getPlayer().getName();
			top1Kills = jugadoresKillsOrd.get(0).getKills();
			top2 = jugadoresKillsOrd.get(1).getPlayer().getName();
			top2Kills = jugadoresKillsOrd.get(1).getKills();
			top3 = messages.getString("topKillsNone");
		}else if(jugadoresKillsOrd.size() == 1) {
			top1 = jugadoresKillsOrd.get(0).getPlayer().getName();
			top1Kills = jugadoresKillsOrd.get(0).getKills();
			top3 = messages.getString("topKillsNone");
			top2 = messages.getString("topKillsNone");
		}else {
			top1 = jugadoresKillsOrd.get(0).getPlayer().getName();
			top1Kills = jugadoresKillsOrd.get(0).getKills();
			top2 = jugadoresKillsOrd.get(1).getPlayer().getName();
			top3 = jugadoresKillsOrd.get(2).getPlayer().getName();
			top2Kills = jugadoresKillsOrd.get(1).getKills();
			top3Kills = jugadoresKillsOrd.get(2).getKills();
		}
		ArrayList<pb.ajneb97.logic.PaintballPlayer> jugadores = paintballArena.getPlayers();
		List<String> msg = messages.getStringList("gameFinished");
		for(pb.ajneb97.logic.PaintballPlayer j : jugadores) {
			for(int i=0;i<msg.size();i++) {
				j.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', msg.get(i).replace("%status_message%", status).replace("%team1%", nameTeam1)
						.replace("%team2%", nameTeam2).replace("%kills_team1%", paintballArena.getTeam1().getKills()+"")
						.replace("%kills_team2%", paintballArena.getTeam2().getKills()+"").replace("%player1%", top1).replace("%player2%", top2)
						.replace("%player3%", top3).replace("%kills_player1%", top1Kills+"").replace("%kills_player2%", top2Kills+"")
						.replace("%kills_player3%", top3Kills+"").replace("%kills_player%", j.getKills()+"")));
			}
			PaintballTeam paintballTeamJugador = paintballArena.GetPlayerTeam(j.getPlayer().getName());
			if(MySql.isEnabled(plugin.getConfig())) {
				int win = 0;
				int lose = 0;
				int tie = 0;
				if(paintballTeamJugador.equals(ganador)) {
					win = 1;
					TitleAPI.sendTitle(j.getPlayer(), 10, 40, 10, messages.getString("winnerTitleMessage"), "");
				}else if(ganador == null) {
					tie = 1;
					TitleAPI.sendTitle(j.getPlayer(), 10, 40, 10, messages.getString("tieTitleMessage"), "");
				}else {
					lose = 1;
					TitleAPI.sendTitle(j.getPlayer(), 10, 40, 10, messages.getString("loserTitleMessage"), "");
				}
				//Aqui se crea/modifica el registro global del jugador
				if(!MySql.playerExists(plugin, j.getPlayer().getName())) {
					MySql.createPlayerArenaAsync(plugin, j.getPlayer().getUniqueId().toString(), j.getPlayer().getName(), "", win, tie, lose, j.getKills(),0, 1);
				}else {
					pb.ajneb97.database.PaintballPlayer paintballPlayer = MySql.getJugador(plugin, j.getPlayer().getName());
					int kills = j.getKills()+ paintballPlayer.getKills();
					int wins = paintballPlayer.getWins()+win;
					int loses = paintballPlayer.getLosses()+lose;
					int ties = paintballPlayer.getTies()+tie;
					MySql.updatePlayerStatsAsync(plugin, j.getPlayer().getUniqueId().toString(), j.getPlayer().getName(), wins, loses, ties, kills);
				}				
				//Este registro es el que se crea para datos mensuales y semanales
				MySql.createPlayerArenaAsync(plugin, j.getPlayer().getUniqueId().toString(), j.getPlayer().getName(), paintballArena.getMatchNumber(), win, tie, lose, j.getKills(),0,0);
			}else {
				plugin.registerPlayer(j.getPlayer().getUniqueId().toString()+".yml");
				if(plugin.getPlayer(j.getPlayer().getName()) == null) {
					plugin.addPlayer(new pb.ajneb97.database.PaintballPlayer(j.getPlayer().getName(),j.getPlayer().getUniqueId().toString(),0,0,0,0,0,new ArrayList<PaintballPerk>(),new ArrayList<PaintballHat>()));
				}
				pb.ajneb97.database.PaintballPlayer jugador = plugin.getPlayer(j.getPlayer().getName());
				if(paintballArena.GetPlayerTeam(j.getPlayer().getName()).equals(ganador)) {
					jugador.increaseWinAmount();
					TitleAPI.sendTitle(j.getPlayer(), 10, 40, 10, messages.getString("winnerTitleMessage"), "");
				}else if(ganador == null) {
					jugador.increaseTieAmount();
					TitleAPI.sendTitle(j.getPlayer(), 10, 40, 10, messages.getString("tieTitleMessage"), "");
				}else {
					jugador.increaseLossAmount();
					TitleAPI.sendTitle(j.getPlayer(), 10, 40, 10, messages.getString("loserTitleMessage"), "");
				}
				
				jugador.increaseKillsByAmount(j.getKills());
			}
			j.getPlayer().closeInventory();
			j.getPlayer().getInventory().clear();
			
			
			if(config.getString("leave_item_enabled").equals("true")) {
				ItemStack item = ItemsUtils.creaItem(config, "leave_item");
				j.getPlayer().getInventory().setItem(8, item);
			}
			if(config.getString("play_again_item_enabled").equals("true")) {
				ItemStack item = ItemsUtils.creaItem(config, "play_again_item");
				j.getPlayer().getInventory().setItem(7, item);
			}
			
			if(config.getString("rewards_executed_after_teleport").equals("false")) {
				if(ganador != null) {
					if(ganador.getColor().equals(paintballTeamJugador.getColor())) {
						List<String> commands = config.getStringList("winners_command_rewards");
						ejecutarComandosRewards(commands,j);
					}else {
						List<String> commands = config.getStringList("losers_command_rewards");
						ejecutarComandosRewards(commands,j);
					}
				}else {
					List<String> commands = config.getStringList("tie_command_rewards");
					ejecutarComandosRewards(commands,j);
				}
			}
		}
		
		int time = Integer.valueOf(config.getString("arena_ending_phase_cooldown"));
		CooldownManager c = new CooldownManager(plugin);
		c.cooldownFaseFinalizacion(paintballArena,time,ganador);
	}
	
	public static void ejecutarComandosRewards(List<String> commands, pb.ajneb97.logic.PaintballPlayer j) {
		CommandSender console = Bukkit.getServer().getConsoleSender();
		for(int i=0;i<commands.size();i++){	
			if(commands.get(i).startsWith("msg %player%")) {
				String mensaje = commands.get(i).replace("msg %player% ", "");
				j.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', mensaje));
			}else {
				String comandoAEnviar = commands.get(i).replaceAll("%player%", j.getPlayer().getName());
				if(comandoAEnviar.contains("%random")) {
					int pos = comandoAEnviar.indexOf("%random");
					int nextPos = comandoAEnviar.indexOf("%", pos+1);
					String variableCompleta = comandoAEnviar.substring(pos,nextPos+1);
					String variable = variableCompleta.replace("%random_", "").replace("%", "");
					String[] sep = variable.split("-");
					int cantidadMinima = 0;
					int cantidadMaxima = 0;
					
				    try {
				    	cantidadMinima = (int) OthersUtils.eval(sep[0].replace("kills", j.getKills()+""));
				    	cantidadMaxima = (int) OthersUtils.eval(sep[1].replace("kills", j.getKills()+""));
					} catch (Exception e) {
						
					}
				    int num = OthersUtils.getNumeroAleatorio(cantidadMinima, cantidadMaxima);
				    comandoAEnviar = comandoAEnviar.replace(variableCompleta, num+"");
				}
				Bukkit.dispatchCommand(console, comandoAEnviar);	
			}
		}
	}
	
	public static void finalizarPartida(PaintballArena paintballArena, PaintballBattle plugin, boolean cerrandoServer, PaintballTeam ganadorPaintballTeam) {
		FileConfiguration config = plugin.getConfig();
		ArrayList<pb.ajneb97.logic.PaintballPlayer> jugadores = paintballArena.getPlayers();
		for(pb.ajneb97.logic.PaintballPlayer j : jugadores) {
			String tipoFin = "";
			if(ganadorPaintballTeam != null) {
				PaintballTeam paintballTeamJugador = paintballArena.GetPlayerTeam(j.getPlayer().getName());
				if(ganadorPaintballTeam.getColor().equals(paintballTeamJugador.getColor())) {
					tipoFin = "ganador";
				}else {
					tipoFin = "perdedor";
				}
			}else {
				tipoFin = "empate";
			}
			onPlayerLeavesArena(paintballArena, j.getPlayer(),true,plugin,cerrandoServer);
			if(config.getString("rewards_executed_after_teleport").equals("true") && !cerrandoServer) {
				if(tipoFin.equals("ganador")) {
					List<String> commands = config.getStringList("winners_command_rewards");
					ejecutarComandosRewards(commands,j);
				}else if(tipoFin.equals("perdedor")) {
					List<String> commands = config.getStringList("losers_command_rewards");
					ejecutarComandosRewards(commands,j);
				}else {
					List<String> commands = config.getStringList("tie_command_rewards");
					ejecutarComandosRewards(commands,j);
				}
			}
		}
		paintballArena.getTeam1().setLives(0);
		paintballArena.getTeam2().setLives(0);
		paintballArena.setNuke(false);
		paintballArena.modifyTeams(config);
		
		paintballArena.setState(ArenaState.WAITING);
	}
	
	public static void muereJugador(PaintballArena paintballArena, pb.ajneb97.logic.PaintballPlayer jugadorAtacante, final pb.ajneb97.logic.PaintballPlayer jugadorDañado, PaintballBattle plugin, boolean lightning, boolean nuke) {
		if(jugadorDañado.getWasKilledRecently()) {
			return;
		}
		if(jugadorDañado.getSelectedHat().equals("guardian_hat") && jugadorDañado.isEfectoHatActivado()) {
			return;
		}
		if(jugadorDañado.getSelectedHat().equals("protector_hat")) {
			Random r = new Random();
			int num = r.nextInt(100);
			if(num >= 80) {
				return;
			}
		}
		
		PaintballTeam paintballTeamDañado = paintballArena.GetPlayerTeam(jugadorDañado.getPlayer().getName());
		PaintballTeam paintballTeamAtacante = paintballArena.GetPlayerTeam(jugadorAtacante.getPlayer().getName());
		if(paintballTeamDañado.equals(paintballTeamAtacante)) {
			return;
		}
		
		if(lightning) {
			jugadorDañado.getPlayer().getWorld().strikeLightningEffect(jugadorDañado.getPlayer().getLocation());
		}
		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();
		jugadorDañado.aumentarMuertes();
		jugadorDañado.setDeathLocation(jugadorDañado.getPlayer().getLocation().clone());
		jugadorDañado.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("killedBy").replace("%player%", jugadorAtacante.getPlayer().getName())));
		String[] separados = config.getString("killedBySound").split(";");
		try {
			Sound sound = Sound.valueOf(separados[0]);
			jugadorDañado.getPlayer().playSound(jugadorDañado.getPlayer().getLocation(), sound, Float.valueOf(separados[1]), Float.valueOf(separados[2]));
		}catch(Exception ex) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
		}
		jugadorDañado.setWasKilledRecetly(true);
		jugadorDañado.setLastKilledBy(jugadorAtacante.getPlayer().getName());
		paintballTeamDañado.decreaseLives(1);
		
		PaintballTeam paintballTeam = paintballArena.GetPlayerTeam(jugadorDañado.getPlayer().getName());
		if(jugadorDañado.getSelectedHat().equals("explosive_hat")) {
			Random r = new Random();
			int num = r.nextInt(100);
			if(num >= 80) {
				if(Bukkit.getVersion().contains("1.8")) {
					jugadorDañado.getPlayer().getWorld().playEffect(jugadorDañado.getPlayer().getLocation(), Effect.valueOf("EXPLOSION_LARGE"), 2);
				}else {
					jugadorDañado.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION,jugadorDañado.getPlayer().getLocation(),2);
				}
				separados = config.getString("explosiveHatSound").split(";");
				try {
					Sound sound = Sound.valueOf(separados[0]);
					jugadorDañado.getPlayer().getWorld().playSound(jugadorDañado.getPlayer().getLocation(), sound, Float.valueOf(separados[1]), Float.valueOf(separados[2]));
				}catch(Exception ex) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
				}
				Collection<Entity> entidades = jugadorDañado.getPlayer().getWorld().getNearbyEntities(jugadorDañado.getPlayer().getLocation(), 5, 5, 5);
				for(Entity e : entidades) {
					if(e != null && e.getType().equals(EntityType.PLAYER)) {
						org.bukkit.entity.Player player = (org.bukkit.entity.Player) e;
						pb.ajneb97.logic.PaintballPlayer jugadorDañado2 = paintballArena.getPlayer(player.getName());
						if(jugadorDañado2 != null) {
							ArenaManager.muereJugador(paintballArena, jugadorDañado, jugadorDañado2, plugin, false, false);
						}
					}
				}
			}
		}
		jugadorDañado.getPlayer().teleport(paintballTeam.getSpawn());
		if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15")
				|| Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.18")
				|| Bukkit.getVersion().contains("1.19")|| Bukkit.getVersion().contains("1.20")) {
			if(jugadorDañado.getSelectedHat().equals("chicken_hat")) {
				jugadorDañado.getPlayer().getInventory().removeItem(new ItemStack(Material.EGG));
			}else {
				jugadorDañado.getPlayer().getInventory().removeItem(new ItemStack(Material.SNOWBALL));
			}
		}else {
			if(jugadorDañado.getSelectedHat().equals("chicken_hat")) {
				jugadorDañado.getPlayer().getInventory().removeItem(new ItemStack(Material.EGG));
			}else {
				jugadorDañado.getPlayer().getInventory().removeItem(new ItemStack(Material.valueOf("SNOW_BALL")));
			}
		}
		ArenaManager.setSnowballs(jugadorDañado,config);
			
		jugadorAtacante.aumentarAsesinatos();
		int cantidadCoinsGanados = OthersUtils.coinsGanados(jugadorAtacante.getPlayer(), config);
		int nivelExtraKillCoins = PaintballPlayerRepository.getPerkLevel(jugadorAtacante.getPlayer(), "extra_killcoins");
		if(nivelExtraKillCoins != 0) {
			String linea = plugin.getShop().getStringList("perks_upgrades.extra_killcoins").get(nivelExtraKillCoins-1);
			String[] sep = linea.split(";");
			int cantidad = Integer.valueOf(sep[0]);
			cantidadCoinsGanados = cantidadCoinsGanados+cantidad;
		}
		String lastKilledBy = jugadorAtacante.getLastKilledBy();
		if(lastKilledBy != null && lastKilledBy.equals(jugadorDañado.getPlayer().getName())) {
			cantidadCoinsGanados = cantidadCoinsGanados+1;
		}
		jugadorAtacante.addCoins(cantidadCoinsGanados);
		ItemsUtils.createItemKillstreaks(jugadorAtacante,config);
		
		if(nuke) {
			String equipoAtacanteName = config.getString("teams."+ paintballTeamAtacante.getColor()+".name");
			String equipoDañadoName = config.getString("teams."+ paintballTeamDañado.getColor()+".name");
			for(pb.ajneb97.logic.PaintballPlayer j : paintballArena.getPlayers()) {
				if(!j.getPlayer().getName().equals(jugadorAtacante.getPlayer().getName())) {
					j.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("nukeKillMessage").replace("%team_player1%", equipoDañadoName)
							.replace("%player1%", jugadorDañado.getPlayer().getName()).replace("%team_player2%", equipoAtacanteName)
							.replace("%player2%", jugadorAtacante.getPlayer().getName())));
				}	
			}
		}
		jugadorAtacante.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("kill").replace("%player%", jugadorDañado.getPlayer().getName())));
		if(!nuke) {
			separados = config.getString("killSound").split(";");
			try {
				Sound sound = Sound.valueOf(separados[0]);
				jugadorAtacante.getPlayer().playSound(jugadorAtacante.getPlayer().getLocation(), sound, Float.valueOf(separados[1]), Float.valueOf(separados[2]));
			}catch(Exception ex) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
			}
		}
		
		
		int snowballs = Integer.valueOf(config.getString("snowballs_per_kill"));
		if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15")
				|| Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.18")
				|| Bukkit.getVersion().contains("1.19")|| Bukkit.getVersion().contains("1.20")) {
			if(jugadorAtacante.getSelectedHat().equals("chicken_hat")) {
				jugadorAtacante.getPlayer().getInventory().addItem(new ItemStack(Material.EGG,snowballs));
			}else {
				jugadorAtacante.getPlayer().getInventory().addItem(new ItemStack(Material.SNOWBALL,snowballs));
			}
			
		}else {
			if(jugadorAtacante.getSelectedHat().equals("chicken_hat")) {
				jugadorAtacante.getPlayer().getInventory().addItem(new ItemStack(Material.EGG,snowballs));
			}else {
				jugadorAtacante.getPlayer().getInventory().addItem(new ItemStack(Material.valueOf("SNOW_BALL"),snowballs));
			}
			
		}
		
		if(paintballTeamDañado.getLives() <= 0) {
			//terminar partida
			ArenaManager.setArenaStateToEnding(paintballArena, plugin);
			return;
		}
			
		int invulnerability = Integer.valueOf(config.getString("respawn_invulnerability"));
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				jugadorDañado.setWasKilledRecetly(false);
			}
		}, invulnerability*20L);
	}
	
	public static PaintballArena getPartidaDisponible(PaintballBattle plugin) {
		ArrayList<PaintballArena> paintballArenas = plugin.getPaintballMatches();
		ArrayList<PaintballArena> disponibles = new ArrayList<PaintballArena>();
		for(int i = 0; i< paintballArenas.size(); i++) {
			if(paintballArenas.get(i).getState().equals(ArenaState.WAITING) ||
					paintballArenas.get(i).getState().equals(ArenaState.STARTING)) {
				if(!paintballArenas.get(i).estaLlena()) {
					disponibles.add(paintballArenas.get(i));
				}
			}
		}
		
		if(disponibles.isEmpty()) {
			return null;
		}
		
		//Ordenar
		for(int i=0;i<disponibles.size();i++) {
			for(int c=i+1;c<disponibles.size();c++) {
				if(disponibles.get(i).getPlayerAmount() < disponibles.get(c).getPlayerAmount()) {
					PaintballArena p = disponibles.get(i);
					disponibles.set(i, disponibles.get(c));
					disponibles.set(c, p);
				}
			}
		}
		return disponibles.get(0);
	}
}
