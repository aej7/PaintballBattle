package pb.ajneb97.database;

import java.sql.PreparedStatement;

//TODO Use a more modern SQL library
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.enums.HatState;
import pb.ajneb97.player.PaintballHat;
import pb.ajneb97.player.PaintballPerk;
import pb.ajneb97.player.PaintballPlayer;
import pb.ajneb97.player.PaintballStats;


//TODO add a PaintballPlayerDAO object to hold the database manipulation and loading outside of the Mysql class
public class MySql {

	public static boolean isEnabled(final FileConfiguration config) {
    return config.getString("mysql-database.enabled").equals("true");
	}

		public static void createTablePlayers(final DatabaseConnection connection) {
	        try {
	        	PreparedStatement statement = connection.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + connection.getTablePlayers() + " (`UUID` varchar(200), `Name` varchar(40), `Date` varchar(100), `Year` INT(10), `Month` INT(5), `Week` INT(5), `Day` INT(5), `Arena` varchar(40), `Win` INT(2), `Tie` INT(2), `Lose` INT(2), `Kills` INT(5), `Coins` INT(10), `Global_Data` INT(2) )");
	            statement.executeUpdate();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
		
		public static void createTablePerks(final DatabaseConnection connection) {
	        try {
	        	PreparedStatement statement = connection.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + connection.getTablePerks() + " (`UUID` varchar(200), `Name` varchar(40), `Perk` varchar(40), `Level` INT(2) )");
	            statement.executeUpdate();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
		
		public static void createTableHats(final DatabaseConnection connection) {
	        try {
	        	PreparedStatement statement = connection.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + connection.getTableHats() + " (`UUID` varchar(200), `Name` varchar(40), `Hat` varchar(40), `Selected` INT(2) )");
	            statement.executeUpdate();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
		
		public static int getTotalStats(final PaintballBattle plugin, final String playerName, final String type){
			int amount = 0;
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM " + plugin.getDatabaseConnection().getTablePlayers() + " WHERE (Name=? AND Global_Data=1)");
				statement.setString(1, playerName);
				ResultSet result = statement.executeQuery();
				
				while(result.next()){
					amount = result.getInt(type);
				}
				
				return amount;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return amount;
		}
		
		//Comprueba solo el dato global
		public static boolean playerExists(final PaintballBattle plugin, final String playerName) {
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM " + plugin.getDatabaseConnection().getTablePlayers() + " WHERE (Name=? AND Global_Data=1)");
				statement.setString(1, playerName);
				ResultSet result = statement.executeQuery();
				if(result.next()) {
					return true;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
		public static void updatePlayerStatsAsync(final PaintballBattle plugin, final String playerName, final int wins, final int losses, final int ties, final int kills) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try {
          PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("UPDATE " + plugin.getDatabaseConnection().getTablePlayers() + " SET Win=?, Tie=?, Lose=?, Kills=? WHERE (Name=? AND Global_Data=1)");
      statement.setInt(1, wins);
      statement.setInt(2, ties);
      statement.setInt(3, losses);
      statement.setInt(4, kills);
      statement.setString(5, playerName);
      statement.executeUpdate();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      });
		}
		
		public static void addPlayerCoins(final PaintballBattle plugin, final String playerName, final int coins) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try {
          PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("UPDATE " + plugin.getDatabaseConnection().getTablePlayers() + " SET Coins=`Coins`+? WHERE (Name=? AND Global_Data=1)");
      statement.setInt(1, coins);
			statement.setString(2, playerName);
      statement.executeUpdate();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      });
		}
		
		public static void removePlayerCoins(final PaintballBattle plugin, final String playerName, final int coins) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try {
          PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("UPDATE " + plugin.getDatabaseConnection().getTablePlayers() + " SET Coins=`Coins`-? WHERE (Name=? AND Global_Data=1)");
      statement.setInt(1, coins);
      statement.setString(2, playerName);
      statement.executeUpdate();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      });
		}
		
		public static void createPlayerArenaAsync(final PaintballBattle plugin, final String playerUuid, final String playerName, final String arena, final int win, final int tie, final int lose, final int kills, final int coins, final int global){
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try {
          Calendar calendar = Calendar.getInstance();
          Date date = new Date();
          calendar.setTime(date);
          int month = calendar.get(Calendar.MONTH);
          int year = calendar.get(Calendar.YEAR);
          int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
          int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);

        PreparedStatement insert = plugin.getDatabaseConnection().getConnection()
            .prepareStatement("INSERT INTO "+plugin.getDatabaseConnection().getTablePlayers()+" (UUID,Name,Date,Year,Month,Week,Day,Arena,Win,Tie,Lose,Kills,Coins,Global_Data) VALUE (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        insert.setString(1, playerUuid);
        insert.setString(2, playerName);
        insert.setString(3, date.getTime()+"");
        insert.setInt(4, year);
        insert.setInt(5, month);
        insert.setInt(6, weekOfMonth);
        insert.setInt(7, dayOfMonth);
        insert.setString(8, arena);
        insert.setInt(9, win);
        insert.setInt(10, tie);
        insert.setInt(11, lose);
        insert.setInt(12, kills);
        insert.setInt(13, coins);
        insert.setInt(14, global);
        insert.executeUpdate();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      });
			
		}
		
		public static boolean playerHasHat(final PaintballBattle plugin, final String playerName, final String hatName) {
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM " + plugin.getDatabaseConnection().getTableHats() + " WHERE (Name=? AND Hat=?)");
				statement.setString(1, playerName);
				statement.setString(2, hatName);
				ResultSet result = statement.executeQuery();
				if(result.next()){
					return true;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
		public static void addPlayerHatAsync(final PaintballBattle plugin, final String playerUuid, final String playerName, final String hatName) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try{
        PreparedStatement insert = plugin.getDatabaseConnection().getConnection()
            .prepareStatement("INSERT INTO "+plugin.getDatabaseConnection().getTableHats()+" (UUID,Name,Hat,Selected) VALUE (?,?,?,?)");
        insert.setString(1, playerUuid);
        insert.setString(2, playerName);
        insert.setString(3, hatName);
        insert.setInt(4, 0);
        insert.executeUpdate();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      });
		}
		
		public static boolean playerHasHatSelected(final PaintballBattle plugin, final String playerName, final String hatName) {
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM "+plugin.getDatabaseConnection().getTableHats()+" WHERE (Name=? AND Hat=? AND Selected=1)");
				statement.setString(1, playerName);
				statement.setString(2, hatName);
				ResultSet result = statement.executeQuery();
				if(result.next()){
					return true;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
		public static Map<String, PaintballHat> getPlayerHats(final PaintballBattle plugin, final String playerName) {
			Map<String, PaintballHat> hats = new HashMap<>();

			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM "+plugin.getDatabaseConnection().getTableHats()+" WHERE (Name=?)");
				statement.setString(1, playerName);
				ResultSet result = statement.executeQuery();

				while(result.next()) {
					String hatName = result.getString("Hat");
					boolean isSelected = result.getInt("Selected") == 1;
					HatState hatState = isSelected ? HatState.EQUIPPED : HatState.UNEQUIPPED;


					PaintballHat hat = new PaintballHat(hatName, hatState);
					hats.put(hat.getName(), hat);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return hats;
		}
		
		public static void unequipHats(final PaintballBattle plugin, final String playerName) {
			try {
        		PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("UPDATE "+plugin.getDatabaseConnection().getTableHats()+" SET Selected=0 WHERE (Name=? AND Selected=1)");
				statement.setString(1, playerName);
				statement.executeUpdate();
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}	
		}
		
		public static void equipHatAsync(final PaintballBattle plugin, final String playerName, final String hatName) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	            @Override
	            public void run() {
	            	try {
	            		unequipHats(plugin,playerName);
	    				
	            		PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("UPDATE "+plugin.getDatabaseConnection().getTableHats()+" SET Selected=1 WHERE (Name=? AND Hat=?)");
	            		statement.setString(1, playerName);
	    				statement.setString(2, hatName);
	    				statement.executeUpdate();
	        		} catch (SQLException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}
	            }
			});	
		}
		
		public static void crearJugadorPerkAsync(final PaintballBattle plugin, final String uuid,final String name,final String perk){
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	            @Override
	            public void run() {
	            	try{
	        			PreparedStatement insert = plugin.getDatabaseConnection().getConnection()
	        					.prepareStatement("INSERT INTO "+plugin.getDatabaseConnection().getTablePerks()+" (UUID,Name,Perk,Level) VALUE (?,?,?,?)");
	        			insert.setString(1, uuid);
	        			insert.setString(2, name);
	        			insert.setString(3, perk);
	        			insert.setInt(4, 1);
	        			insert.executeUpdate();
	        		} catch (SQLException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}
	            }
			});
			
		}	
		
		public static int getNivelPerk(PaintballBattle plugin, String name, String perk){
			int level = 0;
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM "+plugin.getDatabaseConnection().getTablePerks()+" WHERE (Name=? AND Perk=?)");
				statement.setString(1, name);
				statement.setString(2, perk);
				ResultSet resultado = statement.executeQuery();
				while(resultado.next()){	
					level = resultado.getInt("Level");
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return level;
		}
		
		public static boolean jugadorPerkExiste(PaintballBattle plugin, String player, String perk){
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM "+plugin.getDatabaseConnection().getTablePerks()+" WHERE (Name=? AND Perk=?)");
				statement.setString(1, player);
				statement.setString(2, perk);
				ResultSet resultado = statement.executeQuery();
				if(resultado.next()){
					return true;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
		public static ArrayList<PaintballPerk> getPerksJugador(PaintballBattle plugin, String name){
			ArrayList<PaintballPerk> paintballPerks = new ArrayList<PaintballPerk>();
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM "+plugin.getDatabaseConnection().getTablePerks()+" WHERE (Name=?)");
				statement.setString(1, name);
				ResultSet resultado = statement.executeQuery();	
				while(resultado.next()){			
					String perk = resultado.getString("Perk");
					int level = resultado.getInt("Level");
					paintballPerks.add(new PaintballPerk(perk,level));
				}		
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return paintballPerks;
		}
		
		public static void setPerkJugadorAsync(final PaintballBattle plugin,final String uuid,final String player,final String perk,final int level){
			if(jugadorPerkExiste(plugin,player,perk)) {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
		            @Override
		            public void run() {
		            	try {
		            		PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("UPDATE "+plugin.getDatabaseConnection().getTablePerks()+" SET Level=? WHERE (Name=? AND Perk=?)");
		    				statement.setInt(1, level);
		    				statement.setString(2, player);
		    				statement.setString(3, perk);
		    				statement.executeUpdate();
		        		} catch (SQLException e) {
		        			// TODO Auto-generated catch block
		        			e.printStackTrace();
		        		}
		            }
				});
			}else {
				crearJugadorPerkAsync(plugin, uuid, player, perk);
			}
				
		}
		
		public static PaintballPlayer getJugador(PaintballBattle plugin, String name){
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM " + plugin.getDatabaseConnection().getTablePlayers() + " WHERE (Global_Data=1 AND Name=?)");
				statement.setString(1, name);
				ResultSet result = statement.executeQuery();
				//TODO wtf is this??? while(result.next()){
					int wins = result.getInt("Win");
					int losses = result.getInt("Lose");
					int ties = result.getInt("Tie");
					int kills = result.getInt("Kills");
					PaintballStats stats = new PaintballStats(wins, losses, ties, kills);
					int coins = result.getInt("Coins");
					PaintballPlayerRepository playerDAO = new PaintballPlayerRepository(name, stats);
					return p;
				//}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		public static ArrayList<PaintballPlayer> getPlayerDataMonthly(PaintballBattle plugin){
			ArrayList<PaintballPlayer> paintballPlayers = new ArrayList<PaintballPlayer>();
			Calendar calendar = Calendar.getInstance();
			Date date = new Date();
			calendar.setTime(date);
			int month = calendar.get(Calendar.MONTH);
			int year = calendar.get(Calendar.YEAR);

			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM " + plugin.getDatabaseConnection().getTablePlayers()+" WHERE (Year=" + year + " AND Month=" + month + " AND Global_Data=0)");
				ResultSet resultado = statement.executeQuery();	
				while(resultado.next()){
					String name = resultado.getString("Name");
					if(!containsPlayer(paintballPlayers,name)) {
						int[] stats = getStatsTotalesMonthly(plugin,name,month,year);
						PaintballPlayer p = new PaintballPlayer(name,"",stats[0],stats[1],stats[2],stats[3],0,null,null);
						paintballPlayers.add(p);
					}	
				}		
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return paintballPlayers;
		}
		
		public static ArrayList<PaintballPlayer> getPlayerDataWeekly(PaintballBattle plugin){
			
			ArrayList<PaintballPlayer> paintballPlayers = new ArrayList<PaintballPlayer>();
			Calendar calendar = Calendar.getInstance();
			Date date = new Date();
			calendar.setTime(date);
			int mes = calendar.get(Calendar.MONTH);
			int año = calendar.get(Calendar.YEAR);
			int semana = calendar.get(Calendar.WEEK_OF_MONTH);
			
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM "+plugin.getDatabaseConnection().getTablePlayers()+" WHERE (Year="+año+" AND Month="+mes+" AND Week="+semana+" AND Global_Data=0)");
				ResultSet resultado = statement.executeQuery();	
				while(resultado.next()){
					String name = resultado.getString("Name");
					if(!containsPlayer(paintballPlayers,name)) {
						int[] stats = getStatsTotalesWeekly(plugin,name,mes,año,semana);
						PaintballPlayer p = new PaintballPlayer(name,"",stats[0],stats[1],stats[2],stats[3],0,null,null);
						paintballPlayers.add(p);
					}	
				}		
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return paintballPlayers;
		}
		
		public static int[] getStatsTotalesWeekly(PaintballBattle plugin, String name, int mes, int año, int semana){
			int[] cantidades = {0,0,0,0}; //Wins,Loses,Ties,Kills
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM "+plugin.getDatabaseConnection().getTablePlayers()+" WHERE (Name=? AND Year="+año+" AND Month="+mes+" AND Week="+semana+" AND Global_Data=0)");
				statement.setString(1, name);
				ResultSet resultado = statement.executeQuery();
				
				while(resultado.next()){	
					cantidades[0] = cantidades[0]+resultado.getInt("Win");
					cantidades[1] = cantidades[2]+resultado.getInt("Lose");
					cantidades[2] = cantidades[2]+resultado.getInt("Tie");
					cantidades[3] = cantidades[3]+resultado.getInt("Kills");
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return cantidades;
		}
		
		public static int[] getStatsTotalesMonthly(PaintballBattle plugin, String name, int mes, int año){
			int[] cantidades = {0,0,0,0}; //Wins,Loses,Ties,Kills
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM "+plugin.getDatabaseConnection().getTablePlayers()+" WHERE (Name=? AND Year="+año+" AND Month="+mes+" AND Global_Data=0)");
				statement.setString(1, name);
				ResultSet resultado = statement.executeQuery();
				
				while(resultado.next()){	
					cantidades[0] = cantidades[0]+resultado.getInt("Win");
					cantidades[1] = cantidades[2]+resultado.getInt("Lose");
					cantidades[2] = cantidades[2]+resultado.getInt("Tie");
					cantidades[3] = cantidades[3]+resultado.getInt("Kills");
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return cantidades;
		}
		
		//Se cargan solo las globales
		public static Map<String, PaintballPlayer> getPlayerData(final PaintballBattle plugin){
			Map<String, PaintballPlayer> players = new HashMap<>();
			try {
				PreparedStatement statement = plugin.getDatabaseConnection().getConnection().prepareStatement("SELECT * FROM " + plugin.getDatabaseConnection().getTablePlayers() + " WHERE Global_Data=1");
				ResultSet result = statement.executeQuery();
				while(result.next()) {
					String playerName = result.getString("Name");
					if(!players.containsKey(playerName)) {
						int wins = result.getInt("Win");
						int loses = result.getInt("Lose");
						int ties = result.getInt("Tie");
						int kills = result.getInt("Kills");
						int coins = result.getInt("Coins");
						plugin.getPlayer("")
						PaintballPlayer player = new PaintballPlayer(playerName,"",wins,loses,ties,kills,coins,null,null);
						players.put(player.getName(), player);
					}	
				}		
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return paintballPlayers;
		}
}
