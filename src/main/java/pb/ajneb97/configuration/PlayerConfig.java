package pb.ajneb97.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pb.ajneb97.PaintballBattle;

public class PlayerConfig {

	private FileConfiguration config;
	private final File configFile;
	private final Path configFilePath;
  private static final Logger logger = LogManager.getLogger(PlayerConfig.class);
	
	public PlayerConfig(String filePath, PaintballBattle plugin) {
    this.configFilePath = Paths.get(plugin.getDataFolder().getPath(), "players", filePath);
		this.configFile = configFilePath.toFile();
	}

	public String getPath(){
		return this.configFilePath.toString();
	}
	
	public FileConfiguration getConfig() {
		 if (config == null) {
		        reloadPlayerConfig();
		    }
		return this.config;
	}

	private void createPlayerConfig() throws IOException {
		if (!configFile.exists()) {
			boolean fileCreated = configFile.createNewFile();
			if (fileCreated) {
				logger.info("Successfully created a new player config file at: {}", configFile.getPath());
			} else {
				logger.warn("This player's config file already exists at: {}", configFile.getPath());
			}
		}
	}
	
	public void registerPlayerConfig(){
		  try {
				createPlayerConfig();
				loadPlayerConfig();
		  } catch (IOException e) {
				logger.error("An error occurred while trying to create or load the player config file at: {}", configFile.getPath(), e);
			}
	}

	private void loadPlayerConfig() {
		config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (IOException e) {
			logger.error("An error occured while trying to load the player config file from: {}", configFile.getPath(), e);
		} catch (InvalidConfigurationException e) {
			logger.error("Invalid configuration found in the player config file at: {}", configFile.getPath(), e);
		}
	}
	
	public void savePlayerConfig() {
		 try {
			 config.save(configFile);
		 } catch (IOException e) {
			 logger.error("An error occured while trying to save the player config file at: {}", configFile.getPath(), e);
	 	}
	 }
	  
	private void reloadPlayerConfig() {
		config = YamlConfiguration.loadConfiguration(configFile);

		if (configFile.exists()) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(configFile);
			config.setDefaults(defConfig);
		}
	}
}
