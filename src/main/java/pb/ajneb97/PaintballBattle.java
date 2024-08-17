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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import pb.ajneb97.api.ExpansionPaintballBattle;
import pb.ajneb97.api.Hat;
import pb.ajneb97.api.PaintballAPI;
import pb.ajneb97.api.Perk;
import pb.ajneb97.configuration.PlayerConfig;
import pb.ajneb97.database.DatabaseConnection;
import pb.ajneb97.database.PaintballPlayer;
import pb.ajneb97.database.MySql;
import pb.ajneb97.enums.ArenaState;
import pb.ajneb97.logic.PaintballArena;
import pb.ajneb97.logic.PaintballArenaEdit;
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
import pb.ajneb97.logic.ArenaManager;
import pb.ajneb97.admin.ScoreboardAdmin;
import pb.ajneb97.logic.TopHologram;
import pb.ajneb97.admin.TopHologramAdmin;


public class PaintballBattle extends JavaPlugin {

  PluginDescriptionFile pluginDescriptionFile = getDescription();
  public String version = pluginDescriptionFile.getVersion();
  public static String prefix = ChatColor.translateAlternateColorCodes('&', "&8[&b&lPaintball Battle&8] ");
  private List<PaintballArena> paintballArenas;
  private FileConfiguration arenas = null;
  private File arenasFile = null;
  private FileConfiguration messages = null;
  private File messagesFile = null;
  private FileConfiguration shop = null;
  private File shopFile = null;
  private PaintballArenaEdit paintballArenaEdit;
  private List<PlayerConfig> playerConfigs;
  private List<PaintballPlayer> paintballPlayers;
  private List<TopHologram> topHolograms;
  private FileConfiguration holograms = null;
  private File hologramsFile = null;
  private static Economy economy = null;
  public boolean isFirstTime = false;
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
    paintballPlayers = new ArrayList<>();
    topHolograms = new ArrayList<>();
    registerEvents();
    initializeArenas();
    registerConfig();
    registerHolograms();
    registerMessages();
    createPlayersFolder();
    registerPlayers();
    registerShop();
    registerPaintballArena();
    registerCommands();
    checkMessagesUpdate();
    loadPlayers();
    setupEconomy();

    if(MySql.isEnabled(getConfig())){
      databaseConnection = new DatabaseConnection(getConfig());
    }

    scoreboardAdmin = new ScoreboardAdmin(this);
    scoreboardAdmin.reloadScoreboards();
    signAdmin = new SignAdmin(this);
    signAdmin.reloadSigns();
    CooldownKillstreaksActionbar cooldownKillstreaksActionbar = new CooldownKillstreaksActionbar(this);
    cooldownKillstreaksActionbar.createActionbars();

    loadTopHolograms();
    topHologramAdmin = new TopHologramAdmin(this);
    topHologramAdmin.scheduledUpdateHolograms();

    PaintballAPI api = new PaintballAPI(this);
    if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
      new ExpansionPaintballBattle(this).register();
    }

    Checks.checkAndModify(this, isFirstTime);
    Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "Has been enabled! " + ChatColor.WHITE + "Version: " + version);
    Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "Thanks for using my plugin!  " + ChatColor.WHITE + "~Ajneb97");
    updateChecker();
  }

  public void onDisable(){
    if(paintballArenas != null) {
      for (PaintballArena paintballArena : paintballArenas) {
        if (!paintballArena.getState().equals(ArenaState.OFF)) {
          ArenaManager.finalizarPartida(paintballArena, this, true, null);
        }
      }
    }
    saveMatches();
    savePlayers();
    saveTopHolograms();

    Bukkit.getConsoleSender().sendMessage(prefix+ChatColor.YELLOW + "Has been disabled! " + ChatColor.WHITE + "Version: " + version);
  }

  public void reloadScoreboard() {
    int taskId = scoreboardAdmin.getTaskID();
    Bukkit.getScheduler().cancelTask(taskId);
    scoreboardAdmin = new ScoreboardAdmin(this);
    scoreboardAdmin.reloadScoreboards();
  }

  public void reloadSigns() {
    int taskId = signAdmin.getTaskID();
    Bukkit.getScheduler().cancelTask(taskId);
    signAdmin = new SignAdmin(this);
    signAdmin.reloadSigns();
  }

  public void recargarHologramas() {
    int taskId = topHologramAdmin.getTaskID();
    Bukkit.getScheduler().cancelTask(taskId);
    topHologramAdmin = new TopHologramAdmin(this);
    topHologramAdmin.scheduledUpdateHolograms();
  }

  public void setPaintballMatchEdit(PaintballArenaEdit p) {
    this.paintballArenaEdit = p;
  }

  public void removePaintballMatchEdit() {
    this.paintballArenaEdit = null;
  }

  public PaintballArenaEdit getPaintballMatchEdit() {
    return this.paintballArenaEdit;
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

  public PaintballArena getPlayersMatch(String playerName) {
    for (PaintballArena paintballArena : paintballArenas) {
      ArrayList<pb.ajneb97.logic.PaintballPlayer> paintballPlayers = paintballArena.getPlayers();
      for (pb.ajneb97.logic.PaintballPlayer paintballPlayer : paintballPlayers) {
        if (paintballPlayer.getPlayer().getName().equals(playerName)) {
          return paintballArena;
        }
      }
    }
    return null;
  }

  public List<PaintballArena> getPaintballMatches() {
    return this.paintballArenas;
  }

  public PaintballArena getMatch(String number) {
    for (PaintballArena paintballArena : paintballArenas) {
      if (paintballArena.getMatchNumber().equals(number)) {
        return paintballArena;
      }
    }
    return null;
  }

  public void addPaintballMatch(PaintballArena paintballArena) {
    this.paintballArenas.add(paintballArena);
  }

  public void removePaintballMatch(String number) {
    paintballArenas.removeIf(match -> match.getMatchNumber().equals(number));
  }

  public void registerPaintballArena() {
    this.paintballArenas = new ArrayList<PaintballArena>();
    FileConfiguration arenasConfig = getArenas();

    if(arenasConfig.contains("Arenas")) {
      ConfigurationSection arenasSection = arenasConfig.getConfigurationSection("Arenas");
      if (arenasSection == null) return;

      for (String arenaKey : arenasSection.getKeys(false)) {
        String arenaPath = String.format("Arenas.%s.", arenaKey);
        int minPlayers = Integer.parseInt(arenasConfig.getString(arenaPath + "min_players"));
        int maxPlayers = Integer.parseInt(arenasConfig.getString(arenaPath + "max_players"));
        int time = Integer.parseInt(arenasConfig.getString(arenaPath + "time"));
        int lives = Integer.parseInt(arenasConfig.getString(arenaPath + "lives"));

        Location lobby = getLocationFromConfig(arenaPath + "Lobby");

        String team1Name = arenasConfig.getString(arenaPath + "Team1.name");
        Location team1Spawn = getLocationFromConfig(arenaPath + "Team1.Spawn");

        String team2Name = arenasConfig.getString(arenaPath + "Team2.name");
        Location team2Spawn = getLocationFromConfig(arenaPath + "Team2.Spawn");

        PaintballArena paintballArena = new PaintballArena(arenaKey, time, team1Name, team2Name, lives);

        if ("random".equalsIgnoreCase(team1Name)) {
          paintballArena.getTeam1().setRandom(true);
        }
        if ("random".equalsIgnoreCase(team2Name)) {
          paintballArena.getTeam2().setRandom(true);
        }

        paintballArena.modifyTeams(getConfig());
        paintballArena.setMaximumPlayerAmount(maxPlayers);
        paintballArena.setMinimumPlayerAmount(minPlayers);
        paintballArena.setLobby(lobby);
        paintballArena.getTeam1().setSpawn(team1Spawn);
        paintballArena.getTeam2().setSpawn(team2Spawn);

        String enabled = arenasConfig.getString(arenaPath + "enabled");
        paintballArena.setState("true".equalsIgnoreCase(enabled) ? ArenaState.WAITING : ArenaState.OFF);

        this.paintballArenas.add(paintballArena);
      }
    }
  }

  private Location getLocationFromConfig(String basePath) throws NullPointerException {
    FileConfiguration arenasConfig = getArenas();
    if (!arenasConfig.contains(basePath)) {
      return null;
    }
      double posX = Double.parseDouble(arenasConfig.getString(basePath + ".x"));
      double posY = Double.parseDouble(arenasConfig.getString(basePath + ".y"));
      double posZ = Double.parseDouble(arenasConfig.getString(basePath + ".z"));
      String worldName = arenasConfig.getString(basePath + ".world");
      float pitch = Float.parseFloat(arenasConfig.getString(basePath + ".pitch"));
      float yaw = Float.parseFloat(arenasConfig.getString(basePath + ".yaw"));

      World world = Bukkit.getWorld(worldName);

      return new Location(world, posX, posY, posZ, yaw, pitch);
  }

  public void saveMatches() {
    FileConfiguration arenas = getArenas();
    arenas.set("Arenas", null);

    for(PaintballArena match : this.paintballArenas) {
      String matchPath = String.format("Arenas.%s.", match.getMatchNumber());
      match.GetPlayerTeam()
      arenas.set("Arenas."+matchNumber+".min_players", match.getMinimumPlayerAmount()+"");
      arenas.set("Arenas."+matchNumber+".max_players", match.getMaximumPlayerAmount()+"");
      arenas.set("Arenas."+matchNumber+".time", match.getMaximumTime()+"");
      arenas.set("Arenas."+matchNumber+".lives", match.getInitialLives()+"");
      Location lLobby = match.getLobby();
      if(lLobby != null) {
        arenas.set("Arenas."+matchNumber+".Lobby.x", lLobby.getX()+"");
        arenas.set("Arenas."+matchNumber+".Lobby.y", lLobby.getY()+"");
        arenas.set("Arenas."+matchNumber+".Lobby.z", lLobby.getZ()+"");
        arenas.set("Arenas."+matchNumber+".Lobby.world", lLobby.getWorld().getName());
        arenas.set("Arenas."+matchNumber+".Lobby.pitch", lLobby.getPitch());
        arenas.set("Arenas."+matchNumber+".Lobby.yaw", lLobby.getYaw());
      }

      Location lSpawnTeam1 = match.getTeam1().getSpawn();
      if(lSpawnTeam1 != null) {
        arenas.set("Arenas."+matchNumber+".Team1.Spawn.x", lSpawnTeam1.getX()+"");
        arenas.set("Arenas."+matchNumber+".Team1.Spawn.y", lSpawnTeam1.getY()+"");
        arenas.set("Arenas."+matchNumber+".Team1.Spawn.z", lSpawnTeam1.getZ()+"");
        arenas.set("Arenas."+matchNumber+".Team1.Spawn.world", lSpawnTeam1.getWorld().getName());
        arenas.set("Arenas."+matchNumber+".Team1.Spawn.pitch", lSpawnTeam1.getPitch());
        arenas.set("Arenas."+matchNumber+".Team1.Spawn.yaw", lSpawnTeam1.getYaw());
      }
      if(match.getTeam1().isRandom()) {
        arenas.set("Arenas."+matchNumber+".Team1.name", "random");
      }else {
        arenas.set("Arenas."+matchNumber+".Team1.name", match.getTeam1().getColor());
      }


      Location lSpawnTeam2 = match.getTeam2().getSpawn();
      if(lSpawnTeam2 != null) {
        arenas.set("Arenas."+matchNumber+".Team2.Spawn.x", lSpawnTeam2.getX()+"");
        arenas.set("Arenas."+matchNumber+".Team2.Spawn.y", lSpawnTeam2.getY()+"");
        arenas.set("Arenas."+matchNumber+".Team2.Spawn.z", lSpawnTeam2.getZ()+"");
        arenas.set("Arenas."+matchNumber+".Team2.Spawn.world", lSpawnTeam2.getWorld().getName());
        arenas.set("Arenas."+matchNumber+".Team2.Spawn.pitch", lSpawnTeam2.getPitch());
        arenas.set("Arenas."+matchNumber+".Team2.Spawn.yaw", lSpawnTeam2.getYaw());
      }
      if(match.getTeam2().isRandom()) {
        arenas.set("Arenas."+matchNumber+".Team2.name", "random");
      }else {
        arenas.set("Arenas."+matchNumber+".Team2.name", match.getTeam2().getColor());
      }

      if(match.getState().equals(ArenaState.OFF)) {
        arenas.set("Arenas."+matchNumber+".enabled", "false");
      }else {
        arenas.set("Arenas."+matchNumber+".enabled", "true");
      }
    }
    this.saveArenas();
  }

  public void initializeArenas() {
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

    Reader defaultConfigStream;
    try {
      defaultConfigStream = new InputStreamReader(this.getResource("arenas.yml"), "UTF8");
      YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
      arenas.setDefaults(defConfig);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public void registerConfig(){
    File config = new File(this.getDataFolder(), "config.yml");
    configPath = config.getPath();
    if(!config.exists()){
      this.isFirstTime = true;
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

  public void loadPlayers() {
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


        this.addPlayer(new PaintballPlayer(jugador,playerConfig.getPath().replace(".yml", ""),wins,loses,ties,kills,coins,perks,hats));
      }
    }
  }

  public void savePlayers() {
    if(!MySql.isEnabled(getConfig())) {
      for(PaintballPlayer paintballPlayer : paintballPlayers) {
        String playerName = paintballPlayer.getName();
        PlayerConfig playerConfig = getPlayerConfig(paintballPlayer.getUUID()+".yml");
        FileConfiguration config = playerConfig.getConfig();

        config.set("name", playerName);
        config.set("kills", paintballPlayer.getKills());
        config.set("wins", paintballPlayer.getWins());
        config.set("ties", paintballPlayer.getTies());
        config.set("losses", paintballPlayer.getLosses());
        config.set("coins", paintballPlayer.getCoins());
        config.set("perks", paintballPlayer.getNamesAndLevelsOfPerks());
        config.set("hats", paintballPlayer.getNamesAndLevelsOfHats());
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

  public void addPlayer(PaintballPlayer paintballPlayer) {
    paintballPlayers.add(paintballPlayer);
  }

  public PaintballPlayer getPlayer(String playerName) {
    for(PaintballPlayer paintballPlayer : paintballPlayers) {
      if(paintballPlayer != null && paintballPlayer.getName() != null && paintballPlayer.getName().equals(playerName)) {
        return paintballPlayer;
      }
    }
    return null;
  }

  public List<PaintballPlayer> getJugadores(){
    return this.paintballPlayers;
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

  public void addTopHologram(TopHologram topHologram) {
    this.topHolograms.add(topHologram);
  }

  public void removeTopHologram(String topHologramName) {
    topHolograms.removeIf(topHologram -> {
      if (topHologram.getName().equals(topHologramName)) {
        topHologram.removeHologram();
        return true;
      }
      return false;
    });
  }

  public TopHologram getTopHologram(String nombre) {
    for (TopHologram topHologram : topHolograms) {
      if (topHologram.getName().equals(nombre)) {
        return topHologram;
      }
    }
    return null;
  }

  public void saveTopHolograms() {
    FileConfiguration holograms = getHolograms();
    holograms.set("Holograms", null);

    for (TopHologram topHologram : topHolograms) {
      Location topHologramLocation = topHologram.getHologram().getPosition().toLocation();
      String topHologramName = topHologram.getName();
      String basePath = String.format("Holograms.%s.", topHologramName);

      String topHologramType = topHologram.getType();
      String topHologramPeriod = topHologram.getPeriod();
      holograms.set(basePath + "type", topHologramType);
      holograms.set(basePath + "period", topHologramPeriod);
      holograms.set(basePath + "x", topHologramLocation.getX() + "");
      holograms.set(basePath + "y", topHologram.getY() + "");
      holograms.set(basePath + "z", topHologramLocation.getZ() + "");
      holograms.set(basePath+ "world", topHologramLocation.getWorld().getName());
    }
    saveHolograms();
  }

  public void loadTopHolograms() {
    FileConfiguration hologramConfig = getHolograms();
    ConfigurationSection hologramSection = hologramConfig.getConfigurationSection("Holograms");
    if(hologramSection != null) {
      for(String key : hologramSection.getKeys(false)) {
        String basePath = String.format("Holograms.%s.", key);

        String type = hologramConfig.getString(basePath + "type");
        double posX = Double.parseDouble(hologramConfig.getString(basePath + "x"));
        double posY = Double.parseDouble(hologramConfig.getString(basePath + "y"));
        double posZ = Double.parseDouble(hologramConfig.getString(basePath + "z"));
        String worldName = hologramConfig.getString(basePath + "world");
        World world = Bukkit.getWorld(worldName);

        Location location = new Location(world, posX, posY, posZ);

        String period = hologramConfig.contains(basePath + "period")
          ? hologramConfig.getString(basePath + "period") : "global";

        TopHologram topHologram = new TopHologram(key, type, location,this, period);
        topHologram.spawnHologram(this);

        this.addTopHologram(topHologram);
      }
    }
  }

  public List<TopHologram> getTopHolograms(){
    return List.copyOf(this.topHolograms);
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
