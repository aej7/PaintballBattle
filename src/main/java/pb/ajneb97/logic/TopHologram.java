package pb.ajneb97.logic;

import java.util.ArrayList;
import java.util.List;


import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

/* Change in HologramsDisplay API usage
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
 */
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.database.MySql;
import pb.ajneb97.database.MySqlCallback;
import pb.ajneb97.utils.HologramsUtils;


public class TopHologram {
	
	private String name;
	private String type; 
	private Hologram hologram;
	private final double y;
	private String period; //global,montly,weekly
	//Si el period es monthly: 
	//Se comprueba mediante los millis que MES y AÑO es. Se obtienen los millis de cada registro en mysql
	//y se comprueba a que MES y AÑO corresponde. Si el mes y el año son iguales, es un registro valido 
	//para el holograma
	
	public TopHologram(String name,String type,Location location,PaintballBattle plugin,String period) {
		this.type = type;
		this.name = name;
		this.period = period;
		this.y = location.getY();
		Location nuevaLoc = location.clone();
		nuevaLoc.setY(nuevaLoc.getY()+ HologramsUtils.getY(nuevaLoc, HologramsUtils.getNumberOfHologramLines(plugin))+1.4);
		/* Change in HologramsDisplay API usage
		this.hologram = HologramsAPI.createHologram(plugin, nuevaLoc);
		 */
		this.hologram = HolographicDisplaysAPI.get(plugin).createHologram(nuevaLoc);
	}
	
	public String getPeriod() {
		return this.period;
	}
	
	public double getY() {
		return y;
	}

	public void removeHologram() {
		this.hologram.delete();
	}

	public void spawnHologram(PaintballBattle plugin) {
//		Bukkit.getConsoleSender().sendMessage("MySQL: Obteniendo datos para "+type+" "+period);
		//final long millisAntes = System.currentTimeMillis();
		
		FileConfiguration messages = plugin.getMessages();
		FileConfiguration config = plugin.getConfig();
		final int topPlayersMax = Integer.parseInt(config.getString("top_hologram_number_of_players"));
		List<String> lineas = messages.getStringList("topHologramFormat");
		/* Change in HologramsDisplay API usage
		VisibilityManager visibility = hologram.getVisibilityManager();
		visibility.setVisibleByDefault(true);
		 */
		VisibilitySettings visibility = hologram.getVisibilitySettings();
		visibility.setGlobalVisibility(VisibilitySettings.Visibility.VISIBLE);
		String typeName = "";
		String periodName = "";
		if(type.equals("kills")) {
			typeName = messages.getString("topHologramTypeKills");
		}else {
			typeName = messages.getString("topHologramTypeWins");
		}
		if(period.equals("monthly")) {
			periodName = messages.getString("topHologramPeriodMonthly");
		}else if(period.equals("weekly")) {
			periodName = messages.getString("topHologramPeriodWeekly");
		}else {
			periodName = messages.getString("topHologramPeriodGlobal");
		}
		
		final String lineaMessage = messages.getString("topHologramScoreboardLine");
    	for(int i=0;i<lineas.size();i++){
			String linea = lineas.get(i).replace("%type%",typeName).replace("%period%", periodName);
			if(linea.contains("%scoreboard_lines%")) {
				if(MySql.isEnabled(config) && !period.equals("global")) {
					HologramsUtils.getTopPlayersFromDatabase(plugin, type, period, playersList -> {
            // TODO Auto-generated method stub
            for(int c=0;c<topPlayersMax;c++) {
              int num = c+1;
              try {
                String[] separados = playersList.get(c).split(";");
                /* Change in HologramsDisplay API usage
                hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', lineaMessage.replace("%position%", num+"")
                    .replace("%name%", separados[0]).replace("%points%", separados[1])));
                 */
                hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', lineaMessage.replace("%position%", num+"")
                    .replace("%name%", separados[0]).replace("%points%", separados[1])));
              } catch(Exception e) {
                break;
              }
            }
            //long millisDespues = System.currentTimeMillis();
            //long espera = millisDespues-millisAntes;
//							Bukkit.getConsoleSender().sendMessage("MySQL: Datos obtenidos para "+type+" "+period+ " en: "+espera+" ms");
          });
				} else {
					HologramsUtils.getTopPlayers(plugin,plugin.getJugadores(), type, (MySqlCallback) playersList -> {
            for(int c=0;c<topPlayersMax;c++) {
              int num = c+1;
              try {
                String[] separados = playersList.get(c).split(";");
                /* Change in HologramsDisplay API usage
                hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', lineaMessage.replace("%position%", num+"")
                    .replace("%name%", separados[0]).replace("%points%", separados[1])));
                 */
                hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', lineaMessage.replace("%position%", num+"")
                    .replace("%name%", separados[0]).replace("%points%", separados[1])));
              } catch(Exception e) {
                break;
              }
            }
            //long millisDespues = System.currentTimeMillis();
            //long espera = millisDespues-millisAntes;
//							Bukkit.getConsoleSender().sendMessage("MySQL: Datos obtenidos para "+type+" "+period+ " en: "+espera+" ms");
          });
				}

			} else {
				/* Change in HologramsDisplay API usage
				hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', linea));
				 */
				hologram.getLines().appendText(ChatColor.translateAlternateColorCodes('&', linea));
			}
		}
	}
	
	public void update(PaintballBattle plugin) {
		Location loc = this.hologram.getPosition().toLocation().clone();
		removeHologram();
		loc.setY(y);
		Location nuevaLoc = loc.clone();
		nuevaLoc.setY(nuevaLoc.getY()+ HologramsUtils.getY(nuevaLoc, HologramsUtils.getNumberOfHologramLines(plugin))+1.4);
		this.hologram = HolographicDisplaysAPI.get(plugin).createHologram(nuevaLoc);
		spawnHologram(plugin);
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public Hologram getHologram() {
		return hologram;
	}
	
	
}
