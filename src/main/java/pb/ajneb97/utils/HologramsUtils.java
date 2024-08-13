package pb.ajneb97.utils;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.database.Player;
import pb.ajneb97.database.MySql;
import pb.ajneb97.database.MySqlCallback;

public class HologramsUtils {

	public static int getCantidadLineasHolograma(PaintballBattle plugin) {
		FileConfiguration config = plugin.getConfig();
		FileConfiguration messages = plugin.getMessages();
		int lineas = messages.getStringList("topHologramFormat").size();
		lineas = lineas+Integer.valueOf(config.getString("top_hologram_number_of_players"));
		return lineas;
	}
	
	public static double determinarY(Location location, int cantidadLineasHolograma) {
		double cantidad = cantidadLineasHolograma*0.15;
		return cantidad;
	}
	
	//Este metodo se usa solo para monthly o weekly
		public static void getTopPlayersSQL(final PaintballBattle plugin,final String tipo,final String periodo,final MySqlCallback callback){
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	            @Override
	            public void run() {
	            	final ArrayList<String> playersList = new ArrayList<String>();

	        		ArrayList<Player> jugadores = new ArrayList<Player>();
	        		if(periodo.equals("monthly")) {
	        			jugadores = MySql.getPlayerDataMonthly(plugin);
	        		}else if(periodo.equals("weekly")) {
	        			jugadores = MySql.getPlayerDataWeekly(plugin);
	        		}else {
	        			jugadores = MySql.getPlayerData(plugin);
	        		}
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
	        		for(int i=0;i<playersList.size();i++) {
	        			for(int k=i+1;k<playersList.size();k++) {
	        				String[] separadosI = playersList.get(i).split(";");
	        				int totalI = Integer.valueOf(separadosI[1]);
	        				String[] separadosK = playersList.get(k).split(";");
	        				int totalK = Integer.valueOf(separadosK[1]);
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
	                        callback.alTerminar(playersList);
	                    }
	                });
	            }
	        });
			
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
	        				int totalI = Integer.valueOf(separadosI[1]);
	        				String[] separadosK = playersList.get(k).split(";");
	        				int totalK = Integer.valueOf(separadosK[1]);
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
	                        callback.alTerminar(playersList);
	                    }
	                });
	            }
			});
			
		}
}
