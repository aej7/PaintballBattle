package pb.ajneb97.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.database.Player;
import pb.ajneb97.database.MySql;
import pb.ajneb97.database.MySqlCallback;

public class HologramsUtils {

	public static int getNumberOfHologramLines(PaintballBattle plugin) {
		FileConfiguration config = plugin.getConfig();
		FileConfiguration messages = plugin.getMessages();
		int lines = messages.getStringList("topHologramFormat").size();
    return lines + Integer.parseInt(config.getString("top_hologram_number_of_players"));
	}
	
	public static double getY(Location location, int numberOfHologramLines) {
		//TODO check why location isnt used anymore
    return numberOfHologramLines*0.15;
	}
	
	//This method is only used weekly/monthly
		public static void getTopPlayersFromDatabase(final PaintballBattle plugin, final String type, final String frequency, final MySqlCallback callback) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				List<Player> players = getPlayerDataByFrequency(plugin, frequency);

				List<Player> sortedPlayers = players.stream()
					.sorted((player1, player2) -> Integer.compare(getTotal(player2, type), getTotal(player1, type)))
					.collect(Collectors.toList());

				List<String> formattedPlayers = sortedPlayers.stream()
					.map(player -> player.getName() + ";" + getTotal(player, type))
					.collect(Collectors.toList());

				Bukkit.getScheduler().runTask(plugin, () -> callback.onCompletion(formattedPlayers));
			});
		}

		private static int getTotal(Player player, String type) {
			return switch (type) {
				case "kills" -> player.getKills();
				case "wins" -> player.getWins();
				default -> 0;
			};
		}

		private static List<Player> getPlayerDataByFrequency(PaintballBattle plugin, String frequency) {
			return switch (frequency) {
				case "monthly" -> MySql.getPlayerDataMonthly(plugin);
				case "weekly" -> MySql.getPlayerDataWeekly(plugin);
				default -> MySql.getPlayerData(plugin);
			};
    }
		
		public static void getTopPlayers(final PaintballBattle plugin, final ArrayList<Player> jugadores, final String tipo, final MySqlCallback callback){
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	            @Override
	            public void run() {
	            	final ArrayList<String> playersList = new ArrayList<String>();
	            	if(!MySql.isEnabled(plugin.getConfig())) {
	            		for(Player j : jugadores) {
	            			String name = j.getName();
	            			int total = 0;
	            			if(tipo.equals("kills")) {
	            				total = j.getKills();
	            			}else if(tipo.equals("wins")) {
	            				total = j.getWins();
	            			}
	            			playersList.add(name+";"+total);
	            		}
	            	}else {
	            		ArrayList<Player> jugadores = MySql.getPlayerData(plugin);
	            		for(Player p : jugadores) {
	            			String name = p.getName();
	            			int total = 0;
	            			if(tipo.equals("kills")) {
	            				total = p.getKills();
	            			}else if(tipo.equals("wins")) {
	            				total = p.getWins();
	            			}
	            			playersList.add(name+";"+total);
	            		}
	            	}
	        		
	        		for(int i=0;i<playersList.size();i++) {
	        			for(int k=i+1;k<playersList.size();k++) {
	        				String[] separadosI = playersList.get(i).split(";");
	        				int totalI = Integer.parseInt(separadosI[1]);
	        				String[] separadosK = playersList.get(k).split(";");
	        				int totalK = Integer.parseInt(separadosK[1]);
	        				if(totalI < totalK) {
	        					String aux = playersList.get(i);
	        					playersList.set(i, playersList.get(k));
	        					playersList.set(k, aux);
	        				}
	        			}
	        		}
	            	Bukkit.getScheduler().runTask(plugin, new Runnable() {
	                    @Override
	                    public void run() {
	                        // call the callback with the result
	                        callback.onCompletion(playersList);
	                    }
	                });
	            }
			});
			
		}
}
