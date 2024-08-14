package pb.ajneb97;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.jetbrains.annotations.Nullable;
import pb.ajneb97.api.ExpansionPaintballBattle;
import pb.ajneb97.api.Hat;
import pb.ajneb97.api.PaintballAPI;
import pb.ajneb97.api.Perk;
import pb.ajneb97.configuration.PlayerConfig;
import pb.ajneb97.database.DatabaseConnection;
import pb.ajneb97.database.Player;
import pb.ajneb97.database.MySql;
import pb.ajneb97.enums.MatchStatus;
import pb.ajneb97.logic.PaintballPlayer;
import pb.ajneb97.logic.PaintballMatch;
import pb.ajneb97.logic.PaintballMatchEdit;
import pb.ajneb97.eventhandlers.OnPlayerJoinEventHandler;
import pb.ajneb97.admin.SignAdmin;
import pb.ajneb97.eventhandlers.SignInteractEventHandler;
import pb.ajneb97.configuration.Checks;
import pb.ajneb97.logic.CooldownKillstreaksActionbar;
import pb.ajneb97.admin.InventoryAdmin;
import pb.ajneb97.logic.InventarioHats;
import pb.ajneb97.eventhandlers.InventoryInteractEventHandler;
import pb.ajneb97.eventhandlers.PlayerActionsEventHandler;
import pb.ajneb97.eventhandlers.MatchEventHandlerNew;
import pb.ajneb97.logic.PartidaManager;
import pb.ajneb97.admin.ScoreboardAdmin;
import pb.ajneb97.logic.TopHologram;
import pb.ajneb97.admin.TopHologramAdmin;


public class PaintballBattle extends JavaPlugin {
  
	PluginDescriptionFile pluginDescriptionFile = getDescription();
	public String version = pluginDescriptionFile.getVersion();
	public static String prefix = ChatColor.translateAlternateColorCodes('&', "&8[&b&lPaintball Battle&8] ");
	private List<PaintballMatch> paintballMatches;
	private FileConfiguration arenas = null;
	private File arenasFile = null;
	private FileConfiguration messages = null;
	private File messagesFile = null;
	private FileConfiguration shop = null;
	private File shopFile = null;
	private PaintballMatchEdit paintballMatchEdit;
	private List<PlayerConfig> playerConfigs;
	private List<Player> players;
	private List<TopHologram> topHolograms;
	private FileConfiguration holograms = null;
	private File hologramsFile = null;
	private static Economy economy = null;
	public boolean firstTime = false;
	public String latestVersion;
	
	public String messagesPath;
	public String configPath;
	
	private ScoreboardAdmin scoreboardAdmin;
	private SignAdmin signAdmin;
	private TopHologramAdmin topHologramAdmin;
	
	private DatabaseConnection databaseConnection;

	@SuppressWarnings("unused")
	public void onEnable(){
	   playerConfigs = new ArrayList<>();
	   players = new ArrayList<>();
	   topHolograms = new ArrayList<>();
	   registerEvents();
	   registerArenas();
	   registerConfig();
	   registerHolograms();
	   registerMessages();
	   createPlayersFolder();
	   registerPlayers();
	   registerShop();
	   registerPaintballMatches();
	   registerCommands();
	   checkMessagesUpdate();
	   cargarJugadores();
	   setupEconomy();
	   
	   if(MySql.isEnabled(getConfig())){
		   databaseConnection = new DatabaseConnection(getConfig());
	   }
	   
	   scoreboardAdmin = new ScoreboardAdmin(this);
	   scoreboardAdmin.crearScoreboards();
	   signAdmin = new SignAdmin(this);
	   signAdmin.updateSigns();
	   CooldownKillstreaksActionbar cooldownKillstreaksActionbar = new CooldownKillstreaksActionbar(this);
	   cooldownKillstreaksActionbar.createActionbars();
	   
	   cargarTopHologramas();
	   topHologramAdmin = new TopHologramAdmin(this);
	   topHologramAdmin.updateHolograms();
	   
	   PaintballAPI api = new PaintballAPI(this);
	   if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
		   new ExpansionPaintballBattle(this).register();
	   }
	   
	   Checks.checkearYModificar(this, firstTime);
	   Bukkit.getConsoleSender().sendMessage(prefix+ChatColor.YELLOW + "Has been enabled! " + ChatColor.WHITE + "Version: " + version);
	   Bukkit.getConsoleSender().sendMessage(prefix+ChatColor.YELLOW + "Thanks for using my plugin!  " + ChatColor.WHITE + "~Ajneb97");
	   updateChecker();
	}

	public void onDisable(){
		if(paintballMatches != null) {
      for (PaintballMatch paintballMatch : paintballMatches) {
        if (!paintballMatch.getEstado().equals(MatchStatus.OFF)) {
          PartidaManager.finalizarPartida(paintballMatch, this, true, null);
        }
      }
		}
		saveMatches();
		savePlayers();
		guardarTopHologramas();
		
		Bukkit.getConsoleSender().sendMessage(prefix+ChatColor.YELLOW + "Has been disabled! " + ChatColor.WHITE + "Version: " + version);
	}
	
	public void reloadScoreboard() {
		int taskID = scoreboardAdmin.getTaskID();
		Bukkit.getScheduler().cancelTask(taskID);
		scoreboardAdmin = new ScoreboardAdmin(this);
		scoreboardAdmin.crearScoreboards();
	}
	
	public void reloadSigns() {
		int taskID = signAdmin.getTaskID();
		Bukkit.getScheduler().cancelTask(taskID);
		signAdmin = new SignAdmin(this);
		signAdmin.updateSigns();
	}
	
	public void reloadHolograms() {
		int taskID = topHologramAdmin.getTaskID();
		Bukkit.getScheduler().cancelTask(taskID);
		topHologramAdmin = new TopHologramAdmin(this);
		topHologramAdmin.updateHolograms();
	}
	
	public void setPartidaEditando(PaintballMatchEdit p) {
		this.paintballMatchEdit = p;
	}
	
	public void removerPartidaEditando() {
		this.paintballMatchEdit = null;
	}
	
	public PaintballMatchEdit getPaintballMatchEdit() {
		return this.paintballMatchEdit;
	}
	
	public DatabaseConnection getDatabaseConnection() {
		return this.databaseConnection;
	}	
	
	private void setupEconomy() {
		  if (getServer().getPluginManager().getPlugin("Vault") == null) {
	          return;
	      }
	      RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	      if (rsp == null) {
	          return;
	      }
	      economy = rsp.getProvider();
	}
	  
	public Economy getEconomy(){	
		return economy;
	}
	
	public void registerEvents(){
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerActionsEventHandler(this), this);
		if(!Bukkit.getVersion().contains("1.8")) {
			pm.registerEvents(new MatchEventHandlerNew(this), this);
		}
		pm.registerEvents(new SignInteractEventHandler(this), this);
		pm.registerEvents(new InventoryAdmin(this), this);
		pm.registerEvents(new InventoryInteractEventHandler(this), this);
		pm.registerEvents(new InventarioHats(this), this);
		pm.registerEvents(new OnPlayerJoinEventHandler(this), this);
	}
	
	public void registerCommands(){
		Objects.requireNonNull(this.getCommand("paintball")).setExecutor(new CommandHandler(this));
	}
	
	public PaintballMatch getPlayersMatch(String playerName) {
    for (PaintballMatch paintballMatch : paintballMatches) {
      ArrayList<PaintballPlayer> players = paintballMatch.getPlayers();
      for (PaintballPlayer player : players) {
        if (player.getJugador().getName().equals(playerName)) {
          return paintballMatch;
        }
      }
    }
		return null;
	}
	
	public List<PaintballMatch> getPaintballMatches() {
		return this.paintballMatches;
	}
	
	public PaintballMatch getMatch(String number) {
    for (PaintballMatch paintballMatch : paintballMatches) {
      if (paintballMatch.getNumber().equals(number)) {
        return paintballMatch;
      }
    }
		return null;
	}
	
	public void addPaintballMatch(PaintballMatch paintballMatch) {
		this.paintballMatches.add(paintballMatch);
	}
	
	public void removePaintballMatch(String number) {
		for(int i = 0; i < paintballMatches.size(); i++) {
			if(paintballMatches.get(i).getNumber().equals(number)) {
				paintballMatches.remove(i);
			}
		}
	}
	
	public void registerPaintballMatches() {
		  this.paintballMatches = new ArrayList<PaintballMatch>();
		  FileConfiguration arenasConfig = getArenas();
		  if(arenasConfig.contains("Arenas")) {
			  for(String key : Objects.requireNonNull(arenasConfig.getConfigurationSection("Arenas")).getKeys(false)) {
					String arenasWithKeySection = arenasConfig.getString(String.format("Arenas.%s.", key));
					int minPlayers = Integer.parseInt(arenasConfig.getString(arenasWithKeySection + "min_players"));
				  int maxPlayers = Integer.parseInt(arenasConfig.getString(arenasWithKeySection + "max_players"));
				  int time = Integer.parseInt(arenasConfig.getString(arenasWithKeySection + "time"));
				  int lives = Integer.parseInt(arenasConfig.getString(arenasWithKeySection + "lives"));

					//TODO continue under with same optimization
				  Location lobby = null;
				  if(arenasConfig.contains(arenasWithKeySection + "Lobby")) {
						String lobbyWithKeySection = arenasWithKeySection + "Lobby.";
					  double lobbyX = Double.parseDouble(lobbyWithKeySection + "x");
					  double lobbyY = Double.parseDouble(lobbyWithKeySection + "y");
					  double lobbyZ = Double.parseDouble(lobbyWithKeySection + "z");
					  String lobbyWorld = lobbyWithKeySection + "world";
					  float lobbyPitch = Float.parseFloat(lobbyWithKeySection + "pitch");
					  float lobbyYaw = Float.parseFloat(lobbyWithKeySection + "yaw");
					  lobby = new Location(Bukkit.getWorld(lobbyWorld), lobbyX, lobbyY, lobbyZ, lobbyYaw, lobbyPitch);
				  }

				  String team1 = Objects.requireNonNull(arenasConfig.getString("Arenas." + key + ".Team1.name"));
				  Location spawnTeam1 = null;
				  if(arenasConfig.contains("Arenas." + key + ".Team1.Spawn")) {
						String team1SpawnWithKeySection = arenasWithKeySection + "Team1.Spawn.";
					  double spawnTeam1PosX = Double.parseDouble(team1SpawnWithKeySection + "x");
					  double spawnTeam1PosY = Double.parseDouble(team1SpawnWithKeySection + "y");
					  double spawnTeam1PosZ = Double.parseDouble(team1SpawnWithKeySection + "z");
					  String spawnTeam1World = team1SpawnWithKeySection + "world";
					  float spawnTeam1Pitch = Float.parseFloat(team1SpawnWithKeySection + "pitch");
					  float spawnTeam1Yaw = Float.parseFloat(team1SpawnWithKeySection + "yaw");
            spawnTeam1 = new Location(Bukkit.getWorld(spawnTeam1World), spawnTeam1PosX, spawnTeam1PosY, spawnTeam1PosZ, spawnTeam1Yaw, spawnTeam1Pitch);
				  }

				  String team2 = Objects.requireNonNull(arenasConfig.getString("Arenas."+key+".Team2.name"));
				  Location spawnTeam2 = null;
				  if(arenasConfig.contains("Arenas."+key+".Team2.Spawn")) {
						String team2SpawnWithKeySection = arenasWithKeySection + "Team2.Spawn.";
						double spawnTeam2PosX = Double.parseDouble(team2SpawnWithKeySection + "x");
						double spawnTeam2PosY = Double.parseDouble(team2SpawnWithKeySection + "y");
						double spawnTeam2PosZ = Double.parseDouble(team2SpawnWithKeySection + "z");
						String spawnTeam2World = team2SpawnWithKeySection + "world";
						float spawnTeam2Pitch = Float.parseFloat(team2SpawnWithKeySection + "pitch");
						float spawnTeam2Yaw = Float.parseFloat(team2SpawnWithKeySection + "yaw");
						spawnTeam2 = new Location(Bukkit.getWorld(spawnTeam2World), spawnTeam2PosX, spawnTeam2PosY, spawnTeam2PosZ, spawnTeam2Yaw, spawnTeam2Pitch);
				  }
				  
				  PaintballMatch paintballMatch = new PaintballMatch(key,time,team1,team2,lives);
				  if(team1.equalsIgnoreCase("random")) {
					  paintballMatch.getTeam1().setRandom(true);
				  }
				  if(team2.equalsIgnoreCase("random")) {
					  paintballMatch.getTeam2().setRandom(true);
				  }
				  paintballMatch.modificarTeams(getConfig());
				  paintballMatch.setCantidadMaximaJugadores(maxPlayers);
				  paintballMatch.setCantidadMinimaJugadores(minPlayers);
				  paintballMatch.setLobby(lobby);
				  paintballMatch.getTeam1().setSpawn(spawnTeam1);
				  paintballMatch.getTeam2().setSpawn(spawnTeam2);
				  String enabled = arenasConfig.getString("Arenas." + key + ".enabled");
				  if(enabled.equals("true")) {
					  paintballMatch.setState(MatchStatus.WAITING);
				  } else {
					  paintballMatch.setState(MatchStatus.OFF);
				  }
				  
				  this.paintballMatches.add(paintballMatch);
			  }
		  }
		  
	  }
	
	public void saveMatches() {
		  FileConfiguration arenas = getArenas();
		  arenas.set("Arenas", null);
		  for(PaintballMatch p : this.paintballMatches) {
			  String nombre = p.getNumber();
			  arenas.set("Arenas."+nombre+".min_players", p.getCantidadMinimaJugadores()+"");
			  arenas.set("Arenas."+nombre+".max_players", p.getCantidadMaximaJugadores()+"");
			  arenas.set("Arenas."+nombre+".time", p.getTiempoMaximo()+"");
			  arenas.set("Arenas."+nombre+".lives", p.getVidasIniciales()+"");
			  Location lLobby = p.getLobby();
			  if(lLobby != null) {
				  arenas.set("Arenas."+nombre+".Lobby.x", lLobby.getX()+"");
				  arenas.set("Arenas."+nombre+".Lobby.y", lLobby.getY()+"");
				  arenas.set("Arenas."+nombre+".Lobby.z", lLobby.getZ()+"");
				  arenas.set("Arenas."+nombre+".Lobby.world", lLobby.getWorld().getName());
				  arenas.set("Arenas."+nombre+".Lobby.pitch", lLobby.getPitch());
				  arenas.set("Arenas."+nombre+".Lobby.yaw", lLobby.getYaw());
			  }
			  
			  Location lSpawnTeam1 = p.getTeam1().getSpawn();
			  if(lSpawnTeam1 != null) {
				  arenas.set("Arenas."+nombre+".Team1.Spawn.x", lSpawnTeam1.getX()+"");
				  arenas.set("Arenas."+nombre+".Team1.Spawn.y", lSpawnTeam1.getY()+"");
				  arenas.set("Arenas."+nombre+".Team1.Spawn.z", lSpawnTeam1.getZ()+"");
				  arenas.set("Arenas."+nombre+".Team1.Spawn.world", lSpawnTeam1.getWorld().getName());
				  arenas.set("Arenas."+nombre+".Team1.Spawn.pitch", lSpawnTeam1.getPitch());
				  arenas.set("Arenas."+nombre+".Team1.Spawn.yaw", lSpawnTeam1.getYaw());
			  }
			  if(p.getTeam1().esRandom()) {
				  arenas.set("Arenas."+nombre+".Team1.name", "random");
			  }else {
				  arenas.set("Arenas."+nombre+".Team1.name", p.getTeam1().getTipo()); 
			  }
			  
			  
			  Location lSpawnTeam2 = p.getTeam2().getSpawn();
			  if(lSpawnTeam2 != null) {
				  arenas.set("Arenas."+nombre+".Team2.Spawn.x", lSpawnTeam2.getX()+"");
				  arenas.set("Arenas."+nombre+".Team2.Spawn.y", lSpawnTeam2.getY()+"");
				  arenas.set("Arenas."+nombre+".Team2.Spawn.z", lSpawnTeam2.getZ()+"");
				  arenas.set("Arenas."+nombre+".Team2.Spawn.world", lSpawnTeam2.getWorld().getName());
				  arenas.set("Arenas."+nombre+".Team2.Spawn.pitch", lSpawnTeam2.getPitch());
				  arenas.set("Arenas."+nombre+".Team2.Spawn.yaw", lSpawnTeam2.getYaw());
			  }
			  if(p.getTeam2().esRandom()) {
				  arenas.set("Arenas."+nombre+".Team2.name", "random");
			  }else {
				  arenas.set("Arenas."+nombre+".Team2.name", p.getTeam2().getTipo()); 
			  }
			  
			  if(p.getEstado().equals(MatchStatus.OFF)) {
				  arenas.set("Arenas."+nombre+".enabled", "false");
			  }else {
				  arenas.set("Arenas."+nombre+".enabled", "true");
			  }
		  }
		  this.saveArenas();
	  }
	
	 public void registerArenas(){
		  arenasFile = new File(this.getDataFolder(), "arenas.yml");
		  if(!arenasFile.exists()){
		    	this.getArenas().options().copyDefaults(true);
				saveArenas();
		    }
	  }
	  public void saveArenas() {
		 try {
			 arenas.save(arenasFile);
		 } catch (IOException e) {
			 e.printStackTrace();
	 	}
	 }
	  
	  public FileConfiguration getArenas() {
		    if (arenas == null) {
		        reloadArenas();
		    }
		    return arenas;
		}
	  
	  public void reloadArenas() {
		    if (arenas == null) {
		    	arenasFile = new File(getDataFolder(), "arenas.yml");
		    }
		    arenas = YamlConfiguration.loadConfiguration(arenasFile);

		    Reader defConfigStream;
			try {
				defConfigStream = new InputStreamReader(this.getResource("arenas.yml"), "UTF8");
				if (defConfigStream != null) {
			        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			        arenas.setDefaults(defConfig);
			    }
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}	    
		}
	  
	  public void registerConfig(){	
			File config = new File(this.getDataFolder(), "config.yml");
			configPath = config.getPath();
		    if(!config.exists()){
		    	this.firstTime = true;
		    	this.getConfig().options().copyDefaults(true);
				saveConfig();  
		    }
	  }
	  
	  public void registerShop(){
		  shopFile = new File(this.getDataFolder(), "shop.yml");
			if(!shopFile.exists()){
				this.getShop().options().copyDefaults(true);
				saveShop();
			}
		}
		
		public void saveShop() {
			try {
				shop.save(shopFile);
			}catch (IOException e) {
				 e.printStackTrace();
		 	}
		}
		  
		public FileConfiguration getShop() {
			if (shop == null) {
			   reloadShop();
			}
			return shop;
		}
		  
		public void reloadShop() {
			if (shop == null) {
				shopFile = new File(getDataFolder(), "shop.yml");
			}
			shop = YamlConfiguration.loadConfiguration(shopFile);
			Reader defConfigStream;
			try {
				defConfigStream = new InputStreamReader(this.getResource("shop.yml"), "UTF8");
				if (defConfigStream != null) {
				     YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				     shop.setDefaults(defConfig);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}	    
		}
	  
	  public void registerMessages(){
		  messagesFile = new File(this.getDataFolder(), "messages.yml");
		  messagesPath = messagesFile.getPath();
			if(!messagesFile.exists()){
				this.getMessages().options().copyDefaults(true);
				saveMessages();
			}
		}
		
		public void saveMessages() {
			try {
				messages.save(messagesFile);
			}catch (IOException e) {
				 e.printStackTrace();
		 	}
		}
		  
		public FileConfiguration getMessages() {
			if (messages == null) {
			   reloadMessages();
			}
			return messages;
		}
		  
		public void reloadMessages() {
			if (messages == null) {
			    messagesFile = new File(getDataFolder(), "messages.yml");
			}
			messages = YamlConfiguration.loadConfiguration(messagesFile);
			Reader defConfigStream;
			try {
				defConfigStream = new InputStreamReader(this.getResource("messages.yml"), "UTF8");
				if (defConfigStream != null) {
				     YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				     messages.setDefaults(defConfig);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}	    
		}
	
		public void createPlayersFolder(){
			File folder;
	        try {
	            folder = new File(this.getDataFolder() + File.separator + "players");
	            if(!folder.exists()){
	                folder.mkdirs();
	            }
	        } catch(SecurityException e) {
	            folder = null;
	        }
		}
		
		public void registerPlayers(){
			String path = this.getDataFolder() + File.separator + "players";
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();
			for (int i=0;i<listOfFiles.length;i++) {
				if(listOfFiles[i].isFile()) {
			        String pathName = listOfFiles[i].getName();
			        PlayerConfig config = new PlayerConfig(pathName,this);
			        config.registerPlayerConfig();
			        playerConfigs.add(config);
			    }
			}
		}
		
		public ArrayList<PlayerConfig> getPlayerConfigs(){
			return this.playerConfigs;
		}
		
		public boolean archivoYaRegistrado(String pathName) {
			for(int i = 0; i< playerConfigs.size(); i++) {
				if(playerConfigs.get(i).getPath().equals(pathName)) {
					return true;
				}
			}
			return false;
		}
		
		public PlayerConfig getPlayerConfig(String pathName) {
			for(int i = 0; i< playerConfigs.size(); i++) {
				if(playerConfigs.get(i).getPath().equals(pathName)) {
					return playerConfigs.get(i);
				}
			}
			return null;
		}
		public ArrayList<PlayerConfig> getPlayerConfigs() {
			return this.playerConfigs;
		}
		
		public boolean registerPlayer(String pathName) {
			if(!archivoYaRegistrado(pathName)) {
				PlayerConfig config = new PlayerConfig(pathName,this);
		        config.registerPlayerConfig();
		        playerConfigs.add(config);
		        return true;
			}else {
				return false;
			}
		}
		
		public void removerConfigPlayer(String path) {
			for(int i = 0; i< playerConfigs.size(); i++) {
				if(playerConfigs.get(i).getPath().equals(path)) {
					playerConfigs.remove(i);
				}
			}
		}
	
		public void cargarJugadores() {
			if(!MySql.isEnabled(getConfig())) {
				for(PlayerConfig playerConfig : playerConfigs) {
					FileConfiguration players = playerConfig.getConfig();
					String jugador = players.getString("name");
					int kills = 0;
					int wins = 0;
					int loses = 0;
					int ties = 0;
					int coins = 0;
					
					if(players.contains("kills")) {
						kills = Integer.valueOf(players.getString("kills"));
					}
					if(players.contains("wins")) {
						wins = Integer.valueOf(players.getString("wins"));
					}
					if(players.contains("loses")) {
						loses = Integer.valueOf(players.getString("loses"));
					}
					if(players.contains("ties")) {
						ties = Integer.valueOf(players.getString("ties"));
					}
					if(players.contains("coins")) {
						coins = Integer.valueOf(players.getString("coins"));
					}
					ArrayList<Perk> perks = new ArrayList<Perk>();
					if(players.contains("perks")) {
						List<String> listaPerks = players.getStringList("perks");
						for(int i=0;i<listaPerks.size();i++) {
							String[] separados = listaPerks.get(i).split(";");
							Perk p = new Perk(separados[0],Integer.valueOf(separados[1]));
							perks.add(p);
						}	
					}
					ArrayList<Hat> hats = new ArrayList<Hat>();
					if(players.contains("hats")) {
						List<String> listaHats = players.getStringList("hats");
            for (String listaHat : listaHats) {
              String[] separados = listaHat.split(";");
              Hat h = new Hat(separados[0], Boolean.valueOf(separados[1]));
              hats.add(h);
            }
					}
					
						
					this.addPlayer(new Player(jugador,playerConfig.getPath().replace(".yml", ""),wins,loses,ties,kills,coins,perks,hats));
				}
			}
		}
		
		public void savePlayers() {
			if(!MySql.isEnabled(getConfig())) {
				for(Player player : players) {
					String playerName = player.getName();
					PlayerConfig playerConfig = getPlayerConfig(player.getUUID()+".yml");
					FileConfiguration config = playerConfig.getConfig();

					config.set("name", playerName);
					config.set("kills", player.getKills());
					config.set("wins", player.getWins());
					config.set("ties", player.getTies());
					config.set("losses", player.getLosses());
					config.set("coins", player.getCoins());
					config.set("perks", player.getNamesAndLevelsOfPerks());
					config.set("hats", player.getNamesAndLevelsOfHats());
				}

				try {
					for (PlayerConfig playerConfig : playerConfigs) {
						playerConfig.savePlayerConfig();
					}
				} catch (Exception e) {
					//TODO error logging if save fails
				}
			}
		}
		
		public void addPlayer(Player player) {
			players.add(player);
		}
		
		public Player getPlayer(String playerName) {
			for(Player player : players) {
				if(player != null && player.getName() != null && player.getName().equals(playerName)) {
					return player;
				}
			}
			return null;
		}
		
		public ArrayList<Player> getJugadores(){
			return this.players;
		}
		
		public void registerHolograms(){
			  hologramsFile = new File(this.getDataFolder(), "holograms.yml");
			  if(!hologramsFile.exists()){
			    	this.getHolograms().options().copyDefaults(true);
					saveHolograms();
			    }
		  }
		  public void saveHolograms() {
			 try {
				 holograms.save(hologramsFile);
			 } catch (IOException e) {
				 e.printStackTrace();
		 	}
		 }
		  
		  public FileConfiguration getHolograms() {
			    if (holograms == null) {
			        reloadHolograms();
			    }
			    return holograms;
			}
		  
		  public void reloadHolograms() {
			    if (holograms == null) {
			    	hologramsFile = new File(getDataFolder(), "holograms.yml");
			    }
			    holograms = YamlConfiguration.loadConfiguration(hologramsFile);

			    Reader defConfigStream;
				try {
					defConfigStream = new InputStreamReader(this.getResource("holograms.yml"), "UTF8");
					if (defConfigStream != null) {
				        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				        holograms.setDefaults(defConfig);
				    }
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}	    
			}
		  
		  public void agregarTopHolograma(TopHologram topHologram) {
				this.topHolograms.add(topHologram);
			}
			
			public boolean eliminarTopHologama(String nombre) {
				for(int i = 0; i< topHolograms.size(); i++) {
					if(topHolograms.get(i).getName().equals(nombre)) {
						topHolograms.get(i).removeHologram();
						topHolograms.remove(i);
						return true;
					}
				}
				return false;
			}
			
			public TopHologram getTopHologram(String nombre) {
				for(int i = 0; i< topHolograms.size(); i++) {
					if(topHolograms.get(i).getName().equals(nombre)) {
						return topHolograms.get(i);
					}
				}
				return null;
			}
			
			public void guardarTopHologramas() {
				FileConfiguration holograms = getHolograms();
				holograms.set("Holograms", null);
				for(int i = 0; i< topHolograms.size(); i++) {
					Location l = topHolograms.get(i).getHologram().getPosition().toLocation();
					String name = topHolograms.get(i).getName();
					String type = topHolograms.get(i).getType();
					String period = topHolograms.get(i).getPeriod();
					holograms.set("Holograms."+name+".type", type);
					holograms.set("Holograms."+name+".period", period);
					holograms.set("Holograms."+name+".x", l.getX()+"");
					holograms.set("Holograms."+name+".y", topHolograms.get(i).getyOriginal()+"");
					holograms.set("Holograms."+name+".z", l.getZ()+"");
					holograms.set("Holograms."+name+".world", l.getWorld().getName());
				}
				saveHolograms();
			}
			
			public void cargarTopHologramas() {
				FileConfiguration holograms = getHolograms();
				if(holograms.contains("Holograms")) {
					for(String name : holograms.getConfigurationSection("Holograms").getKeys(false)) {
						String type = holograms.getString("Holograms."+name+".type");
						double x = Double.valueOf(holograms.getString("Holograms."+name+".x"));
						double y = Double.valueOf(holograms.getString("Holograms."+name+".y"));
						double z = Double.valueOf(holograms.getString("Holograms."+name+".z"));
						World world = Bukkit.getWorld(holograms.getString("Holograms."+name+".world"));
						Location location = new Location(world,x,y,z);
						String period = "global";
						if(holograms.contains("Holograms."+name+".period")) {
							period = holograms.getString("Holograms."+name+".period");
						}
						TopHologram topHologram = new TopHologram(name,type,location,this,period);
						topHologram.spawnHologram(this);
						this.agregarTopHolograma(topHologram);
					}
				}
			}
			
			public ArrayList<TopHologram> getTopHolograms(){
				return this.topHolograms;
			}
			
			public void updateChecker(){
				  
				  try {
					  HttpURLConnection con = (HttpURLConnection) new URL(
			                  "https://api.spigotmc.org/legacy/update.php?resource=76676").openConnection();
			          int timed_out = 1250;
			          con.setConnectTimeout(timed_out);
			          con.setReadTimeout(timed_out);
			          latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
			          if (latestVersion.length() <= 7) {
			        	  if(!version.equals(latestVersion)){
			        		  Bukkit.getConsoleSender().sendMessage(ChatColor.RED +"There is a new version available. "+ChatColor.YELLOW+
			        				  "("+ChatColor.GRAY+ latestVersion +ChatColor.YELLOW+")");
			        		  Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"You can download it at: "+ChatColor.WHITE+"https://www.spigotmc.org/resources/76676/");  
			        	  }      	  
			          }
			      } catch (Exception ex) {
			    	  Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED +"Error while checking update.");
			      }
			  }
			
			public void checkMessagesUpdate(){
				  Path archivo = Paths.get(messagesPath);
				  Path archivoConfig = Paths.get(configPath);
				  try{
					  String texto = new String(Files.readAllBytes(archivo));
					  String textoConfig = new String(Files.readAllBytes(archivoConfig));
					  if(!textoConfig.contains("broadcast_starting_arena:")){
						  getConfig().set("broadcast_starting_arena.enabled", true);
						  List<String> lista = new ArrayList<String>();
						  lista.add("paintball");lista.add("lobby");
						  getConfig().set("broadcast_starting_arena.worlds", lista);
						  getConfig().set("rewards_executed_after_teleport", false);
						  getMessages().set("arenaStartingBroadcast", "&aArena &6&l%arena% &ais about to start! Use &b/pb join %arena% &ato join the game!");
						  saveConfig();	
						  saveMessages();
					  }
					  if(!textoConfig.contains("startCooldownSound:")){
						  getConfig().set("startCooldownSound", "BLOCK_NOTE_BLOCK_PLING;10;1");
						  getConfig().set("startGameSound", "BLOCK_NOTE_BLOCK_PLING;10;2");
						  getConfig().set("arena_chat_enabled", true);
						  saveConfig();	
					  }
					  if(!texto.contains("errorClearInventory:")){
							getMessages().set("errorClearInventory", "&c&lERROR! &7To join an arena clear your inventory first.");
							getConfig().set("empty_inventory_to_join", false);
							saveConfig();
							saveMessages();
					  }
					  if(!textoConfig.contains("snowball_particle:")){
						  getConfig().set("snowball_particle", "SNOW_SHOVEL");
						  saveConfig();	
					  }
					  if(!texto.contains("receiveCoinsMessage:")){
						  getMessages().set("receiveCoinsMessage", "&aYou received &e%amount% &acoins.");
						  saveMessages();	
					  }
					  if(!textoConfig.contains("losers_command_rewards:")) {
						  List<String> lista = new ArrayList<String>();
						  lista.add("msg %player% &aYou've lost! Here, take this compensation reward.");
						  lista.add("paintball givecoins %player% %random_2*kills-6*kills%");
						  getConfig().set("losers_command_rewards", lista);
						  lista = new ArrayList<String>();
						  lista.add("msg %player% &aIt's a tie! Here, take this reward.");
						  lista.add("paintball givecoins %player% %random_2*kills-6*kills%");
						  getConfig().set("tie_command_rewards", lista);
						  saveConfig();
					  }
				  }catch(IOException e){
					  e.printStackTrace();
				  }
			  }
}
