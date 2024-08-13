package pb.ajneb97.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
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
import pb.ajneb97.api.Hat;
import pb.ajneb97.api.PaintballAPI;
import pb.ajneb97.api.Perk;
import pb.ajneb97.database.Player;
import pb.ajneb97.database.MySql;
import pb.ajneb97.enums.MatchStatus;
import pb.ajneb97.lib.titleapi.TitleAPI;
import pb.ajneb97.utils.ItemsUtils;
import pb.ajneb97.utils.OthersUtils;

public class PartidaManager {

	@SuppressWarnings("deprecation")
	public static void jugadorEntra(PaintballInstance paintballInstance, org.bukkit.entity.Player jugador, PaintballBattle plugin) {
		PaintballPlayer paintballPlayer = new PaintballPlayer(jugador);
		FileConfiguration messages = plugin.getMessages();
		paintballInstance.agregarJugador(paintballPlayer);
		ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
		for(int i=0;i<jugadores.size();i++) {
			jugadores.get(i).getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("playerJoin").replace("%player%", jugador.getName())
					.replace("%current_players%", paintballInstance.getCantidadActualJugadores()+"").replace("%max_players%", paintballInstance.getCantidadMaximaJugadores()+"")));
		}
		
		jugador.getInventory().clear();
		jugador.getEquipment().clear();
		jugador.getEquipment().setArmorContents(null);
		jugador.updateInventory();
		
		jugador.setGameMode(GameMode.SURVIVAL);
		jugador.setExp(0);
		jugador.setLevel(0);
		jugador.setFoodLevel(20);
		jugador.setMaxHealth(20);
		jugador.setHealth(20);
		jugador.setFlying(false);
		jugador.setAllowFlight(false);
		for(PotionEffect p : jugador.getActivePotionEffects()) {
			jugador.removePotionEffect(p.getType());
		}
		
		jugador.teleport(paintballInstance.getLobby());
		
		FileConfiguration config = plugin.getConfig();
		if(config.getString("leave_item_enabled").equals("true")) {
			ItemStack item = ItemsUtils.crearItem(config, "leave_item");
			jugador.getInventory().setItem(8, item);
		}
		if(config.getString("hats_item_enabled").equals("true")) {
			ItemStack item = ItemsUtils.crearItem(config, "hats_item");
			jugador.getInventory().setItem(7, item);
		}
		if(config.getString("choose_team_system").equals("true")) {
			ItemStack item = ItemsUtils.crearItem(config, "teams."+ paintballInstance.getTeam1().getTipo());
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', messages.getString("teamChoose").replace("%team%", config.getString("teams."+ paintballInstance.getTeam1().getTipo()+".name"))));
			item.setItemMeta(meta);
			jugador.getInventory().setItem(0, item);
			item = ItemsUtils.crearItem(config, "teams."+ paintballInstance.getTeam2().getTipo());
			meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', messages.getString("teamChoose").replace("%team%", config.getString("teams."+ paintballInstance.getTeam2().getTipo()+".name"))));
			item.setItemMeta(meta);
			jugador.getInventory().setItem(1, item);
		}
		
		if(paintballInstance.getCantidadActualJugadores() >= paintballInstance.getCantidadMinimaJugadores()
				&& paintballInstance.getEstado().equals(MatchStatus.ESPERANDO)) {
			cooldownIniciarPartida(paintballInstance,plugin);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void jugadorSale(PaintballInstance paintballInstance, org.bukkit.entity.Player jugador, boolean finalizaPartida,
																 PaintballBattle plugin, boolean cerrandoServer) {
		PaintballPlayer paintballPlayer = paintballInstance.getJugador(jugador.getName());
		FileConfiguration messages = plugin.getMessages();
		ItemStack[] inventarioGuardado = paintballPlayer.getGuardados().getInventarioGuardado();
		ItemStack[] equipamientoGuardado = paintballPlayer.getGuardados().getEquipamientoGuardado();
		GameMode gamemodeGuardado = paintballPlayer.getGuardados().getGamemodeGuardado();
		float xpGuardada = paintballPlayer.getGuardados().getXPGuardada();
		int levelGuardado = paintballPlayer.getGuardados().getLevelGuardado();
		int hambreGuardada = paintballPlayer.getGuardados().getHambreGuardada();
		double vidaGuardada = paintballPlayer.getGuardados().getVidaGuardada();
		double maxVidaGuardada = paintballPlayer.getGuardados().getMaxVidaGuardada();
		boolean allowFligth = paintballPlayer.getGuardados().isAllowFlight();
		boolean isFlying = paintballPlayer.getGuardados().isFlying();

		paintballInstance.removerJugador(jugador.getName());
		
		if(!finalizaPartida) {
			ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
			for(int i=0;i<jugadores.size();i++) {
				jugadores.get(i).getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("playerLeave").replace("%player%", jugador.getName())
						.replace("%current_players%", paintballInstance.getCantidadActualJugadores()+"").replace("%max_players%", paintballInstance.getCantidadMaximaJugadores()+"")));
			}
		}
		
		FileConfiguration config = plugin.getConfig();
		double x = Double.valueOf(config.getString("MainLobby.x"));
		double y = Double.valueOf(config.getString("MainLobby.y"));
		double z = Double.valueOf(config.getString("MainLobby.z"));
		String world = config.getString("MainLobby.world");
		float yaw = Float.valueOf(config.getString("MainLobby.yaw"));
		float pitch = Float.valueOf(config.getString("MainLobby.pitch"));
		Location mainLobby = new Location(Bukkit.getWorld(world),x,y,z,yaw,pitch);
		jugador.teleport(mainLobby);
		
		jugador.getInventory().setContents(inventarioGuardado);
		jugador.getEquipment().setArmorContents(equipamientoGuardado);
		jugador.setGameMode(gamemodeGuardado);
		jugador.setLevel(levelGuardado);
		jugador.setExp(xpGuardada);
		jugador.setFoodLevel(hambreGuardada);
		jugador.setMaxHealth(maxVidaGuardada);
		jugador.setHealth(vidaGuardada);
		for(PotionEffect p : jugador.getActivePotionEffects()) {
			jugador.removePotionEffect(p.getType());
		}
		jugador.updateInventory();

		jugador.setAllowFlight(allowFligth);
		jugador.setFlying(isFlying);
		
		if(!cerrandoServer) {
			if(paintballInstance.getCantidadActualJugadores() < paintballInstance.getCantidadMinimaJugadores()
					&& paintballInstance.getEstado().equals(MatchStatus.COMENZANDO)){
				paintballInstance.setEstado(MatchStatus.ESPERANDO);
			}else if(paintballInstance.getCantidadActualJugadores() <= 1 && (paintballInstance.getEstado().equals(MatchStatus.JUGANDO))) {
				//fase finalizacion
				PartidaManager.iniciarFaseFinalizacion(paintballInstance, plugin);
			}else if((paintballInstance.getTeam1().getCantidadJugadores() == 0 || paintballInstance.getTeam2().getCantidadJugadores() == 0) && paintballInstance.getEstado().equals(MatchStatus.JUGANDO)) {
				//fase finalizacion
				PartidaManager.iniciarFaseFinalizacion(paintballInstance, plugin);
			}
		}
	}
	
	public static void cooldownIniciarPartida(PaintballInstance paintballInstance, PaintballBattle plugin) {
		paintballInstance.setEstado(MatchStatus.COMENZANDO);
		FileConfiguration config = plugin.getConfig();
		FileConfiguration messages = plugin.getMessages();
		int time = Integer.valueOf(config.getString("arena_starting_cooldown"));
		
		CooldownManager cooldown = new CooldownManager(plugin);
		cooldown.cooldownComenzarJuego(paintballInstance,time);
		
		String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
		
		if(config.getString("broadcast_starting_arena.enabled").equals("true")) {
			List<String> worlds = config.getStringList("broadcast_starting_arena.worlds");
			for(String world : worlds) {
				for(org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
					if(player.getWorld().getName().equals(world)) {
						player.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("arenaStartingBroadcast")
								.replace("%arena%", paintballInstance.getNombre())));
					}
				}
			}
		}
	}
	
	public static void iniciarPartida(PaintballInstance paintballInstance, PaintballBattle plugin) {
		paintballInstance.setEstado(MatchStatus.JUGANDO);
		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();
		//String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
		
		if(plugin.getConfig().getString("choose_team_system").equals("true")) {
			setTeams(paintballInstance);
		}else {
			setTeamsAleatorios(paintballInstance);
		}

		darItems(paintballInstance,plugin.getConfig(),plugin.getShop(),plugin.getMessages());
		teletransportarJugadores(paintballInstance);
		setVidas(paintballInstance,plugin.getShop());
		
		ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
		String[] separados = config.getString("startGameSound").split(";");
		Sound sound = null;
		float volume = 0;
		float pitch = 0;
		try {
			sound = Sound.valueOf(separados[0]);
			volume = Float.valueOf(separados[1]);
			pitch = Float.valueOf(separados[2]);
		}catch(Exception ex) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
			sound = null;
		}
		for(int i=0;i<jugadores.size();i++) {
			String nombreTeam = paintballInstance.getEquipoJugador(jugadores.get(i).getJugador().getName()).getTipo();
			jugadores.get(i).getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("gameStarted")));
			jugadores.get(i).getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("teamInformation").replace("%team%", plugin.getConfig().getString("teams."+nombreTeam+".name"))));
			jugadores.get(i).getJugador().closeInventory();
			if(sound != null) {
				jugadores.get(i).getJugador().playSound(jugadores.get(i).getJugador().getLocation(), sound, volume, pitch);
			}
		}
		
		CooldownManager cooldown = new CooldownManager(plugin);
		cooldown.cooldownJuego(paintballInstance);
	}
	
	

	public static void setVidas(PaintballInstance paintballInstance, FileConfiguration shop) {
		paintballInstance.getTeam1().setVidas(paintballInstance.getVidasIniciales());
		paintballInstance.getTeam2().setVidas(paintballInstance.getVidasIniciales());
		
		ArrayList<PaintballPlayer> jugadoresTeam1 = paintballInstance.getTeam1().getJugadores();
		for(PaintballPlayer j : jugadoresTeam1) {
			//comprobar perk extralives
			int nivelExtraLives = PaintballAPI.getPerkLevel(j.getJugador(), "extra_lives");
			if(nivelExtraLives != 0) {
				String linea = shop.getStringList("perks_upgrades.extra_lives").get(nivelExtraLives-1);
				String[] sep = linea.split(";");
				int cantidad = Integer.valueOf(sep[0]);
				paintballInstance.getTeam1().aumentarVidas(cantidad);
			}
		}
		ArrayList<PaintballPlayer> jugadoresTeam2 = paintballInstance.getTeam2().getJugadores();
		for(PaintballPlayer j : jugadoresTeam2) {
			//comprobar perk extralives
			int nivelExtraLives = PaintballAPI.getPerkLevel(j.getJugador(), "extra_lives");
			if(nivelExtraLives != 0) {
				String linea = shop.getStringList("perks_upgrades.extra_lives").get(nivelExtraLives-1);
				String[] sep = linea.split(";");
				int cantidad = Integer.valueOf(sep[0]);
				paintballInstance.getTeam2().aumentarVidas(cantidad);
			}
		}
	}
	
	public static void killstreakInstantanea(String key, org.bukkit.entity.Player jugador, PaintballInstance paintballInstance, PaintballBattle plugin) {
		FileConfiguration config = plugin.getConfig();
		if(key.equalsIgnoreCase("3_lives")) {
			Team team = paintballInstance.getEquipoJugador(jugador.getName());
			team.aumentarVidas(3);
		}else if(key.equalsIgnoreCase("teleport")) {
			PaintballPlayer j = paintballInstance.getJugador(jugador.getName());
			if(j.getDeathLocation() != null) {
				j.getJugador().teleport(j.getDeathLocation());
			}else {
				Team team = paintballInstance.getEquipoJugador(jugador.getName());
				j.getJugador().teleport(team.getSpawn());
			}
		}else if(key.equalsIgnoreCase("more_snowballs")) {
			PaintballPlayer j = paintballInstance.getJugador(jugador.getName());
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
				jugador.getInventory().addItem(item);
			}
		}else if(key.equalsIgnoreCase("lightning")) {
			PaintballPlayer jugadorAtacante = paintballInstance.getJugador(jugador.getName());
			int radio = Integer.valueOf(config.getString("killstreaks_items."+key+".radius"));
			Collection<Entity> entidades = jugador.getWorld().getNearbyEntities(jugador.getLocation(), radio, radio, radio);
			for(Entity e : entidades) {
				if(e != null && e.getType().equals(EntityType.PLAYER)) {
					org.bukkit.entity.Player player = (org.bukkit.entity.Player) e;
					PaintballPlayer jugadorDañado = paintballInstance.getJugador(player.getName());
					if(jugadorDañado != null) {
						PartidaManager.muereJugador(paintballInstance, jugadorAtacante, jugadorDañado, plugin, true, false);
					}
				}
			}
		}else if(key.equalsIgnoreCase("nuke")) {
			paintballInstance.setEnNuke(true);
			PaintballPlayer jugadorAtacante = paintballInstance.getJugador(jugador.getName());
			CooldownKillstreaks c = new CooldownKillstreaks(plugin);
			String[] separados1 = config.getString("killstreaks_items."+key+".activateSound").split(";");
			String[] separados2 = config.getString("killstreaks_items."+key+".finalSound").split(";");
			c.cooldownNuke(jugadorAtacante, paintballInstance, separados1, separados2);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void setTeamsAleatorios(PaintballInstance paintballInstance) {
		ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
		ArrayList<PaintballPlayer> jugadoresCopia = (ArrayList<PaintballPlayer>) paintballInstance.getJugadores().clone();
		//Si son 4 se seleccionan 2, Si son 5 tambien 2, Si son 6, 3, Si son 7, tambien 3
		Random r = new Random();
		int num = jugadores.size()/2;
		for(int i=0;i<num;i++) {
			int pos = r.nextInt(jugadoresCopia.size());
			PaintballPlayer jugadorSelect = jugadoresCopia.get(pos);
			jugadoresCopia.remove(pos);
			
			paintballInstance.repartirJugadorTeam2(jugadorSelect);
		}
	}
	
	private static void setTeams(PaintballInstance paintballInstance) {
		//Falta comprobar lo siguiente:
		//Si 2 usuarios seleccionan team y uno se va, los 2 usuarios estaran en el mismo team al
		//iniciar la partida y seran solo ellos 2.
		
		ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
		for(PaintballPlayer j : jugadores) {
			paintballInstance.getEquipoJugador(j.getJugador().getName()).removerJugador(j.getJugador().getName());
			String preferenciaTeam = j.getPreferenciaTeam();
			if(preferenciaTeam == null) {
				if(paintballInstance.puedeSeleccionarEquipo(paintballInstance.getTeam1().getTipo())) {
					j.setPreferenciaTeam(paintballInstance.getTeam1().getTipo());
				}else {
					j.setPreferenciaTeam(paintballInstance.getTeam2().getTipo());
				}
			}
			preferenciaTeam = j.getPreferenciaTeam();
			if(preferenciaTeam.equals(paintballInstance.getTeam2().getTipo())) {
				paintballInstance.getTeam2().agregarJugador(j);
			}else {
				paintballInstance.getTeam1().agregarJugador(j);
			}
		}
		
		//Balanceo final
		Team team1 = paintballInstance.getTeam1();
		Team team2 = paintballInstance.getTeam2();
		for(PaintballPlayer j : jugadores) {
			Team team = paintballInstance.getEquipoJugador(j.getJugador().getName());
			if(team1.getCantidadJugadores() > team2.getCantidadJugadores()+1) {
				if(team.getTipo().equals(team1.getTipo())) {
					//Mover al jugador del equipo1 al equipo2
					team1.removerJugador(j.getJugador().getName());
					team2.agregarJugador(j);
				}
			}else if(team2.getCantidadJugadores() > team1.getCantidadJugadores()+1) {
				if(team.getTipo().equals(team2.getTipo())) {
					//Mover al jugador del equipo2 al equipo1
					team2.removerJugador(j.getJugador().getName());
					team1.agregarJugador(j);
				}
			}
		}
	}
	
	public static void darItems(PaintballInstance paintballInstance, FileConfiguration config, FileConfiguration shop, FileConfiguration messages) {
		ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
		for(PaintballPlayer j : jugadores) {
			org.bukkit.entity.Player p = j.getJugador();
			p.getInventory().setItem(8, null);
			
			Team team = paintballInstance.getEquipoJugador(p.getName());
			if(config.contains("teams."+ team.getTipo())) {
				darEquipamientoJugador(p,Integer.valueOf(config.getString("teams."+ team.getTipo()+".color")));
			}else {
				darEquipamientoJugador(p,0);
			}
			//comprobar perk initial killcoins
			int nivelInitialKillcoins = PaintballAPI.getPerkLevel(j.getJugador(), "initial_killcoins");
			if(nivelInitialKillcoins != 0) {
				String linea = shop.getStringList("perks_upgrades.initial_killcoins").get(nivelInitialKillcoins-1);
				String[] sep = linea.split(";");
				int cantidad = Integer.valueOf(sep[0]);
				j.agregarCoins(cantidad);
			}
			ItemsUtils.crearItemKillstreaks(j,config);
			ponerHat(paintballInstance,j,config,messages);
			setBolasDeNieve(j,config);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void ponerHat(PaintballInstance paintballInstance, PaintballPlayer jugador, FileConfiguration config, FileConfiguration messages) {
		ArrayList<Hat> hats = PaintballAPI.getHats(jugador.getJugador());
		for(Hat h : hats) {
			if(h.isSelected()) {
				jugador.setSelectedHat(h.getName());
				ItemStack item = ItemsUtils.crearItem(config, "hats_items."+h.getName());
				ItemMeta meta = item.getItemMeta();
				meta.setLore(null);
				item.setItemMeta(meta);
				if(config.contains("hats_items."+h.getName()+".skull_id")) {
					String id = config.getString("hats_items."+h.getName()+".skull_id");
					String textura = config.getString("hats_items."+h.getName()+".skull_texture");
					item = ItemsUtils.getCabeza(item, id, textura);
				}
				jugador.getJugador().getEquipment().setHelmet(item);
				
				if(h.getName().equals("speed_hat")) {
					jugador.getJugador().addPotionEffect(new PotionEffect(PotionEffectType.SPEED,9999999,0,false,false));
				}else if(h.getName().equals("present_hat")) {
					Team team = paintballInstance.getEquipoJugador(jugador.getJugador().getName());
					ArrayList<PaintballPlayer> jugadoresCopy = (ArrayList<PaintballPlayer>) team.getJugadores().clone();
					jugadoresCopy.remove(jugador);
					if(!jugadoresCopy.isEmpty()) {
						Random r = new Random();
						int pos = r.nextInt(jugadoresCopy.size());
						String jName = jugadoresCopy.get(pos).getJugador().getName();
						PaintballPlayer j = paintballInstance.getJugador(jName);
						j.agregarCoins(3);
						jugador.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("presentHatGive").replace("%player%", j.getJugador().getName())));
						j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("presentHatReceive").replace("%player%", jugador.getJugador().getName())));
					}	
				}
				return;
			}
		}
	}
	
	public static void darEquipamientoJugador(org.bukkit.entity.Player jugador, int color) {
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
	
	public static void setBolasDeNieve(PaintballPlayer j, FileConfiguration config) {
		for(int i=0;i<=7;i++) {
			j.getJugador().getInventory().setItem(i, null);
		}
		for(int i=9;i<=35;i++) {
			j.getJugador().getInventory().setItem(i, null);
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
			j.getJugador().getInventory().addItem(item);
		}
	}
	
	public static void lanzarFuegos(ArrayList<PaintballPlayer> jugadores) {
		for(PaintballPlayer j : jugadores) {
			Firework fw = (Firework) j.getJugador().getWorld().spawnEntity(j.getJugador().getLocation(), EntityType.FIREWORK_ROCKET);
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
	
	public static void teletransportarJugadores(PaintballInstance paintballInstance) {
		ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
		for(PaintballPlayer j : jugadores) {
			org.bukkit.entity.Player p = j.getJugador();
			Team team = paintballInstance.getEquipoJugador(p.getName());
			p.teleport(team.getSpawn());
		}
	}
	
	public static void iniciarFaseFinalizacion(PaintballInstance paintballInstance, PaintballBattle plugin) {
		paintballInstance.setEstado(MatchStatus.TERMINANDO);
		Team ganador = paintballInstance.getGanador();
		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();
		
		String nameTeam1 = config.getString("teams."+ paintballInstance.getTeam1().getTipo()+".name");
		String nameTeam2 = config.getString("teams."+ paintballInstance.getTeam2().getTipo()+".name");
		
		String status = "";
		if(ganador == null) {
			//empate
			status = messages.getString("gameFinishedTieStatus");
		}else {
			String ganadorTexto = plugin.getConfig().getString("teams."+ganador.getTipo()+".name");
			status = messages.getString("gameFinishedWinnerStatus").replace("%winner_team%", ganadorTexto);
		}	
				
		ArrayList<PaintballPlayer> jugadoresKillsOrd = paintballInstance.getJugadoresKills();
		String top1 = "";
		String top2 = "";
		String top3 = "";
		int top1Kills = 0;
		int top2Kills = 0;
		int top3Kills = 0;
		
		if(jugadoresKillsOrd.size() == 2) {
			top1 = jugadoresKillsOrd.get(0).getJugador().getName();
			top1Kills = jugadoresKillsOrd.get(0).getAsesinatos();
			top2 = jugadoresKillsOrd.get(1).getJugador().getName();
			top2Kills = jugadoresKillsOrd.get(1).getAsesinatos();
			top3 = messages.getString("topKillsNone");
		}else if(jugadoresKillsOrd.size() == 1) {
			top1 = jugadoresKillsOrd.get(0).getJugador().getName();
			top1Kills = jugadoresKillsOrd.get(0).getAsesinatos();
			top3 = messages.getString("topKillsNone");
			top2 = messages.getString("topKillsNone");
		}else {
			top1 = jugadoresKillsOrd.get(0).getJugador().getName();
			top1Kills = jugadoresKillsOrd.get(0).getAsesinatos();
			top2 = jugadoresKillsOrd.get(1).getJugador().getName();
			top3 = jugadoresKillsOrd.get(2).getJugador().getName();
			top2Kills = jugadoresKillsOrd.get(1).getAsesinatos();
			top3Kills = jugadoresKillsOrd.get(2).getAsesinatos();
		}
		ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
		List<String> msg = messages.getStringList("gameFinished");
		for(PaintballPlayer j : jugadores) {
			for(int i=0;i<msg.size();i++) {
				j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', msg.get(i).replace("%status_message%", status).replace("%team1%", nameTeam1)
						.replace("%team2%", nameTeam2).replace("%kills_team1%", paintballInstance.getTeam1().getAsesinatosTotales()+"")
						.replace("%kills_team2%", paintballInstance.getTeam2().getAsesinatosTotales()+"").replace("%player1%", top1).replace("%player2%", top2)
						.replace("%player3%", top3).replace("%kills_player1%", top1Kills+"").replace("%kills_player2%", top2Kills+"")
						.replace("%kills_player3%", top3Kills+"").replace("%kills_player%", j.getAsesinatos()+"")));
			}
			Team teamJugador = paintballInstance.getEquipoJugador(j.getJugador().getName());
			if(MySql.isEnabled(plugin.getConfig())) {
				int win = 0;
				int lose = 0;
				int tie = 0;
				if(teamJugador.equals(ganador)) {
					win = 1;
					TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("winnerTitleMessage"), "");
				}else if(ganador == null) {
					tie = 1;
					TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("tieTitleMessage"), "");
				}else {
					lose = 1;
					TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("loserTitleMessage"), "");
				}
				//Aqui se crea/modifica el registro global del jugador
				if(!MySql.jugadorExiste(plugin, j.getJugador().getName())) {
					MySql.crearJugadorPartidaAsync(plugin, j.getJugador().getUniqueId().toString(), j.getJugador().getName(), "", win, tie, lose, j.getAsesinatos(),0, 1);
				}else {
					Player player = MySql.getJugador(plugin, j.getJugador().getName());
					int kills = j.getAsesinatos()+player.getKills();
					int wins = player.getWins()+win;
					int loses = player.getLoses()+lose;
					int ties = player.getTies()+tie;
					MySql.actualizarJugadorPartidaAsync(plugin, j.getJugador().getUniqueId().toString(), j.getJugador().getName(), wins, loses, ties, kills);
				}				
				//Este registro es el que se crea para datos mensuales y semanales
				MySql.crearJugadorPartidaAsync(plugin, j.getJugador().getUniqueId().toString(), j.getJugador().getName(), paintballInstance.getNombre(), win, tie, lose, j.getAsesinatos(),0,0);
			}else {
				plugin.registerPlayer(j.getJugador().getUniqueId().toString()+".yml");
				if(plugin.getJugador(j.getJugador().getName()) == null) {
					plugin.agregarJugadorDatos(new Player(j.getJugador().getName(),j.getJugador().getUniqueId().toString(),0,0,0,0,0,new ArrayList<Perk>(),new ArrayList<Hat>()));
				}
				Player jugador = plugin.getJugador(j.getJugador().getName());
				if(paintballInstance.getEquipoJugador(j.getJugador().getName()).equals(ganador)) {
					jugador.aumentarWins();
					TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("winnerTitleMessage"), "");
				}else if(ganador == null) {
					jugador.aumentarTies();
					TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("tieTitleMessage"), "");
				}else {
					jugador.aumentarLoses();
					TitleAPI.sendTitle(j.getJugador(), 10, 40, 10, messages.getString("loserTitleMessage"), "");
				}
				
				jugador.aumentarKills(j.getAsesinatos());
			}
			j.getJugador().closeInventory();
			j.getJugador().getInventory().clear();
			
			
			if(config.getString("leave_item_enabled").equals("true")) {
				ItemStack item = ItemsUtils.crearItem(config, "leave_item");
				j.getJugador().getInventory().setItem(8, item);
			}
			if(config.getString("play_again_item_enabled").equals("true")) {
				ItemStack item = ItemsUtils.crearItem(config, "play_again_item");
				j.getJugador().getInventory().setItem(7, item);
			}
			
			if(config.getString("rewards_executed_after_teleport").equals("false")) {
				if(ganador != null) {
					if(ganador.getTipo().equals(teamJugador.getTipo())) {
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
		c.cooldownFaseFinalizacion(paintballInstance,time,ganador);
	}
	
	public static void ejecutarComandosRewards(List<String> commands, PaintballPlayer j) {
		CommandSender console = Bukkit.getServer().getConsoleSender();
		for(int i=0;i<commands.size();i++){	
			if(commands.get(i).startsWith("msg %player%")) {
				String mensaje = commands.get(i).replace("msg %player% ", "");
				j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', mensaje));
			}else {
				String comandoAEnviar = commands.get(i).replaceAll("%player%", j.getJugador().getName()); 
				if(comandoAEnviar.contains("%random")) {
					int pos = comandoAEnviar.indexOf("%random");
					int nextPos = comandoAEnviar.indexOf("%", pos+1);
					String variableCompleta = comandoAEnviar.substring(pos,nextPos+1);
					String variable = variableCompleta.replace("%random_", "").replace("%", "");
					String[] sep = variable.split("-");
					int cantidadMinima = 0;
					int cantidadMaxima = 0;
					
				    try {
				    	cantidadMinima = (int) OthersUtils.eval(sep[0].replace("kills", j.getAsesinatos()+""));
				    	cantidadMaxima = (int) OthersUtils.eval(sep[1].replace("kills", j.getAsesinatos()+""));
					} catch (Exception e) {
						
					}
				    int num = OthersUtils.getNumeroAleatorio(cantidadMinima, cantidadMaxima);
				    comandoAEnviar = comandoAEnviar.replace(variableCompleta, num+"");
				}
				Bukkit.dispatchCommand(console, comandoAEnviar);	
			}
		}
	}
	
	public static void finalizarPartida(PaintballInstance paintballInstance, PaintballBattle plugin, boolean cerrandoServer, Team ganadorTeam) {
		FileConfiguration config = plugin.getConfig();
		ArrayList<PaintballPlayer> jugadores = paintballInstance.getJugadores();
		for(PaintballPlayer j : jugadores) {
			String tipoFin = "";
			if(ganadorTeam != null) {
				Team teamJugador = paintballInstance.getEquipoJugador(j.getJugador().getName());
				if(ganadorTeam.getTipo().equals(teamJugador.getTipo())) {
					tipoFin = "ganador";
				}else {
					tipoFin = "perdedor";
				}
			}else {
				tipoFin = "empate";
			}
			jugadorSale(paintballInstance, j.getJugador(),true,plugin,cerrandoServer);
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
		paintballInstance.getTeam1().setVidas(0);
		paintballInstance.getTeam2().setVidas(0);
		paintballInstance.setEnNuke(false);
		paintballInstance.modificarTeams(config);
		
		paintballInstance.setEstado(MatchStatus.ESPERANDO);
	}
	
	public static void muereJugador(PaintballInstance paintballInstance, PaintballPlayer jugadorAtacante, final PaintballPlayer jugadorDañado, PaintballBattle plugin, boolean lightning, boolean nuke) {
		if(jugadorDañado.haSidoAsesinadoRecientemente()) {
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
		
		Team teamDañado = paintballInstance.getEquipoJugador(jugadorDañado.getJugador().getName());
		Team teamAtacante = paintballInstance.getEquipoJugador(jugadorAtacante.getJugador().getName());
		if(teamDañado.equals(teamAtacante)) {
			return;
		}
		
		if(lightning) {
			jugadorDañado.getJugador().getWorld().strikeLightningEffect(jugadorDañado.getJugador().getLocation());
		}
		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();
		jugadorDañado.aumentarMuertes();
		jugadorDañado.setDeathLocation(jugadorDañado.getJugador().getLocation().clone());
		jugadorDañado.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("killedBy").replace("%player%", jugadorAtacante.getJugador().getName())));
		String[] separados = config.getString("killedBySound").split(";");
		try {
			Sound sound = Sound.valueOf(separados[0]);
			jugadorDañado.getJugador().playSound(jugadorDañado.getJugador().getLocation(), sound, Float.valueOf(separados[1]), Float.valueOf(separados[2]));
		}catch(Exception ex) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
		}
		jugadorDañado.setAsesinadoRecientemente(true);
		jugadorDañado.setLastKilledBy(jugadorAtacante.getJugador().getName());
		teamDañado.disminuirVidas(1);
		
		Team team = paintballInstance.getEquipoJugador(jugadorDañado.getJugador().getName());
		if(jugadorDañado.getSelectedHat().equals("explosive_hat")) {
			Random r = new Random();
			int num = r.nextInt(100);
			if(num >= 80) {
				if(Bukkit.getVersion().contains("1.8")) {
					jugadorDañado.getJugador().getWorld().playEffect(jugadorDañado.getJugador().getLocation(), Effect.valueOf("EXPLOSION_LARGE"), 2);
				}else {
					jugadorDañado.getJugador().getWorld().spawnParticle(Particle.EXPLOSION,jugadorDañado.getJugador().getLocation(),2);
				}
				separados = config.getString("explosiveHatSound").split(";");
				try {
					Sound sound = Sound.valueOf(separados[0]);
					jugadorDañado.getJugador().getWorld().playSound(jugadorDañado.getJugador().getLocation(), sound, Float.valueOf(separados[1]), Float.valueOf(separados[2]));
				}catch(Exception ex) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
				}
				Collection<Entity> entidades = jugadorDañado.getJugador().getWorld().getNearbyEntities(jugadorDañado.getJugador().getLocation(), 5, 5, 5);
				for(Entity e : entidades) {
					if(e != null && e.getType().equals(EntityType.PLAYER)) {
						org.bukkit.entity.Player player = (org.bukkit.entity.Player) e;
						PaintballPlayer jugadorDañado2 = paintballInstance.getJugador(player.getName());
						if(jugadorDañado2 != null) {
							PartidaManager.muereJugador(paintballInstance, jugadorDañado, jugadorDañado2, plugin, false, false);
						}
					}
				}
			}
		}
		jugadorDañado.getJugador().teleport(team.getSpawn());
		if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15")
				|| Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.18")
				|| Bukkit.getVersion().contains("1.19")|| Bukkit.getVersion().contains("1.20")) {
			if(jugadorDañado.getSelectedHat().equals("chicken_hat")) {
				jugadorDañado.getJugador().getInventory().removeItem(new ItemStack(Material.EGG));
			}else {
				jugadorDañado.getJugador().getInventory().removeItem(new ItemStack(Material.SNOWBALL));
			}
		}else {
			if(jugadorDañado.getSelectedHat().equals("chicken_hat")) {
				jugadorDañado.getJugador().getInventory().removeItem(new ItemStack(Material.EGG));
			}else {
				jugadorDañado.getJugador().getInventory().removeItem(new ItemStack(Material.valueOf("SNOW_BALL")));
			}
		}
		PartidaManager.setBolasDeNieve(jugadorDañado,config);
			
		jugadorAtacante.aumentarAsesinatos();
		int cantidadCoinsGanados = OthersUtils.coinsGanados(jugadorAtacante.getJugador(), config);
		int nivelExtraKillCoins = PaintballAPI.getPerkLevel(jugadorAtacante.getJugador(), "extra_killcoins");
		if(nivelExtraKillCoins != 0) {
			String linea = plugin.getShop().getStringList("perks_upgrades.extra_killcoins").get(nivelExtraKillCoins-1);
			String[] sep = linea.split(";");
			int cantidad = Integer.valueOf(sep[0]);
			cantidadCoinsGanados = cantidadCoinsGanados+cantidad;
		}
		String lastKilledBy = jugadorAtacante.getLastKilledBy();
		if(lastKilledBy != null && lastKilledBy.equals(jugadorDañado.getJugador().getName())) {
			cantidadCoinsGanados = cantidadCoinsGanados+1;
		}
		jugadorAtacante.agregarCoins(cantidadCoinsGanados);
		ItemsUtils.crearItemKillstreaks(jugadorAtacante,config);
		
		if(nuke) {
			String equipoAtacanteName = config.getString("teams."+ teamAtacante.getTipo()+".name");
			String equipoDañadoName = config.getString("teams."+ teamDañado.getTipo()+".name");
			for(PaintballPlayer j : paintballInstance.getJugadores()) {
				if(!j.getJugador().getName().equals(jugadorAtacante.getJugador().getName())) {
					j.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("nukeKillMessage").replace("%team_player1%", equipoDañadoName)
							.replace("%player1%", jugadorDañado.getJugador().getName()).replace("%team_player2%", equipoAtacanteName)
							.replace("%player2%", jugadorAtacante.getJugador().getName())));
				}	
			}
		}
		jugadorAtacante.getJugador().sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("kill").replace("%player%", jugadorDañado.getJugador().getName())));
		if(!nuke) {
			separados = config.getString("killSound").split(";");
			try {
				Sound sound = Sound.valueOf(separados[0]);
				jugadorAtacante.getJugador().playSound(jugadorAtacante.getJugador().getLocation(), sound, Float.valueOf(separados[1]), Float.valueOf(separados[2]));
			}catch(Exception ex) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PaintballBattle.prefix+"&7Sound Name: &c"+separados[0]+" &7is not valid."));
			}
		}
		
		
		int snowballs = Integer.valueOf(config.getString("snowballs_per_kill"));
		if(Bukkit.getVersion().contains("1.13") || Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15")
				|| Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.18")
				|| Bukkit.getVersion().contains("1.19")|| Bukkit.getVersion().contains("1.20")) {
			if(jugadorAtacante.getSelectedHat().equals("chicken_hat")) {
				jugadorAtacante.getJugador().getInventory().addItem(new ItemStack(Material.EGG,snowballs));
			}else {
				jugadorAtacante.getJugador().getInventory().addItem(new ItemStack(Material.SNOWBALL,snowballs));
			}
			
		}else {
			if(jugadorAtacante.getSelectedHat().equals("chicken_hat")) {
				jugadorAtacante.getJugador().getInventory().addItem(new ItemStack(Material.EGG,snowballs));
			}else {
				jugadorAtacante.getJugador().getInventory().addItem(new ItemStack(Material.valueOf("SNOW_BALL"),snowballs));
			}
			
		}
		
		if(teamDañado.getVidas() <= 0) {
			//terminar partida
			PartidaManager.iniciarFaseFinalizacion(paintballInstance, plugin);
			return;
		}
			
		int invulnerability = Integer.valueOf(config.getString("respawn_invulnerability"));
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				jugadorDañado.setAsesinadoRecientemente(false);
			}
		}, invulnerability*20L);
	}
	
	public static PaintballInstance getPartidaDisponible(PaintballBattle plugin) {
		ArrayList<PaintballInstance> paintballInstances = plugin.getPartidas();
		ArrayList<PaintballInstance> disponibles = new ArrayList<PaintballInstance>();
		for(int i = 0; i< paintballInstances.size(); i++) {
			if(paintballInstances.get(i).getEstado().equals(MatchStatus.ESPERANDO) ||
					paintballInstances.get(i).getEstado().equals(MatchStatus.COMENZANDO)) {
				if(!paintballInstances.get(i).estaLlena()) {
					disponibles.add(paintballInstances.get(i));
				}
			}
		}
		
		if(disponibles.isEmpty()) {
			return null;
		}
		
		//Ordenar
		for(int i=0;i<disponibles.size();i++) {
			for(int c=i+1;c<disponibles.size();c++) {
				if(disponibles.get(i).getCantidadActualJugadores() < disponibles.get(c).getCantidadActualJugadores()) {
					PaintballInstance p = disponibles.get(i);
					disponibles.set(i, disponibles.get(c));
					disponibles.set(c, p);
				}
			}
		}
		return disponibles.get(0);
	}
}
