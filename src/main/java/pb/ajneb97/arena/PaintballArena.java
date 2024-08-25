package pb.ajneb97.arena;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import pb.ajneb97.enums.ArenaState;
import pb.ajneb97.player.PaintballTeam;

import java.util.ArrayList;
import java.util.Random;



public class PaintballArena {

	private final PaintballTeam paintballTeam1;
	private final PaintballTeam paintballTeam2;
	private final String matchNumber;
	private int maximumPlayerAmount;
	private int minimumPlayerAmount;
	private int playerAmount;
	private ArenaState arenaState;
	private Location lobby;
	private int time;
	private int maximumTime;
	private int initialLives;
	private boolean isNuke;
	
	public PaintballArena(String matchNumber, int maximumTime, String team1, String team2, int initialLives) {
		//por defecto
		this.paintballTeam1 = new PaintballTeam(team1);
		this.paintballTeam2 = new PaintballTeam(team2);
		this.matchNumber = matchNumber;
		this.maximumPlayerAmount = 16;
		this.minimumPlayerAmount = 4;
		this.playerAmount = 0;
		this.arenaState = ArenaState.OFF;
		this.time = 0;
		this.maximumTime = maximumTime;
		this.initialLives = initialLives;
		this.isNuke = false;
	}
	
	public boolean isNuke() {
		return isNuke;
	}

	public void setNuke(boolean isNuke) {
		this.isNuke = isNuke;
	}

	public void setInitialLives(int amount) {
		this.initialLives = amount;
	}
	
	public int getInitialLives() {
		return this.initialLives;
	}
	
	public void setMaximumTime(int time) {
		this.maximumTime = time;
	}
	
	public int getMaximumTime() {
		return this.maximumTime;
	}
	
	public void decreaseTime() {
		this.time--;
	}
	
	public void increaseTime() {
		this.time++;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	public int getTime() {
		return this.time;
	}
	
	public String getMatchNumber() {
		return this.matchNumber;
	}
	
	public void addPlayer(PaintballPlayer player) {
		//ANTES DE INICIAR LA PARTIDA TODOS ESTAN EN EL TEAM 1 Y LUEGO SE REPARTEN LOS DEMAS AL TEAM 2
		if(paintballTeam1.addPlayer(player)) {
			this.playerAmount++;
		}	
	}
	
	public void changePlayerToTeam2(PaintballPlayer paintballPlayer) {
		this.paintballTeam1.removePlayer(paintballPlayer.getPlayer().getName());
		this.paintballTeam2.addPlayer(paintballPlayer);
	}
	
	public void removePlayer(String player) {
		if(paintballTeam1.removePlayer(player) || paintballTeam2.removePlayer(player)) {
			this.playerAmount--;
		}	
	}
	
	public ArrayList<PaintballPlayer> getPlayers() {
		ArrayList<PaintballPlayer> players = new ArrayList<PaintballPlayer>();

    players.addAll(paintballTeam1.getPlayers());
    players.addAll(paintballTeam2.getPlayers());
		
		return players;
	}
	
	public PaintballPlayer getPlayer(String playerName) {
		for (PaintballPlayer paintballPlayer : getPlayers()) {
			if (paintballPlayer.getPlayer().getName().equals(playerName)) {
				return paintballPlayer;
			}
		}
		return null;
	}
	
	public PaintballTeam GetPlayerTeam(String playerName) {
		ArrayList<PaintballPlayer> jugadoresTeam1 = paintballTeam1.getPlayers();
		for(int i=0;i<jugadoresTeam1.size();i++) {
			if(jugadoresTeam1.get(i).getPlayer().getName().equals(playerName)) {
				return this.paintballTeam1;
			}
		}
		ArrayList<PaintballPlayer> jugadoresTeam2 = paintballTeam2.getPlayers();
		for(int i=0;i<jugadoresTeam2.size();i++) {
			if(jugadoresTeam2.get(i).getPlayer().getName().equals(playerName)){
				return this.paintballTeam2;
			}
		}
		
		return null;
	}
	
	public PaintballTeam getTeam1() {
		return this.paintballTeam1;
	}
	
	public PaintballTeam getTeam2() {
		return this.paintballTeam2;
	}
	
	public int getMaximumPlayerAmount() {
		return this.maximumPlayerAmount;
	}
	
	public void setMaximumPlayerAmount(int max) {
		this.maximumPlayerAmount = max;
	}
	
	public int getMinimumPlayerAmount() {
		return this.minimumPlayerAmount;
	}
	
	public void setMinimumPlayerAmount(int min) {
		this.minimumPlayerAmount = min;
	}
	
	public int getPlayerAmount() {
		return this.playerAmount;
	}
	
	public ArenaState getState() {
		return this.arenaState;
	}
	
	public void setState(ArenaState estado) {
		this.arenaState = estado;
	}
	
	public boolean estaIniciada() {
		if(!this.arenaState.equals(ArenaState.WAITING) && !this.arenaState.equals(ArenaState.STARTING)) {
			return true;
		}else {
			return false;
		}
	}
	
	public boolean estaLlena() {
		if(this.playerAmount == this.maximumPlayerAmount) {
			return true;
		}else {
			return false;
		}
	}
	
	public boolean isActivated() {
		if(this.arenaState.equals(ArenaState.OFF)) {
			return false;
		}else {
			return true;
		}
	}
	
	public void setLobby(Location l) {
		this.lobby = l;
	}
		
	public Location getLobby() {
		return this.lobby;
	}
	
	public PaintballTeam getGanador() {
		if(paintballTeam1.getPlayers().size() == 0) {
			return paintballTeam2;
		}
		if(paintballTeam2.getPlayers().size() == 0) {
			return paintballTeam1;
		}
		
		int vidasTeam1 = paintballTeam1.getLives();
		int vidasTeam2 = paintballTeam2.getLives();
		if(vidasTeam1 > vidasTeam2) {
			return paintballTeam1;
		}else if(vidasTeam2 > vidasTeam1) {
			return paintballTeam2;
		}else {
			return null; //empate
		}	
	}
	
	public ArrayList<PaintballPlayer> getJugadoresKills() {
		ArrayList<PaintballPlayer> nuevo = new ArrayList<PaintballPlayer>();
		for(int i = 0; i< getPlayers().size(); i++) {
			nuevo.add(getPlayers().get(i));
		}
		
		for(int i=0;i<nuevo.size();i++) {
			for(int c=i+1;c<nuevo.size();c++) {
				if(nuevo.get(i).getKills() < nuevo.get(c).getKills()) {
					PaintballPlayer j = nuevo.get(i);
					nuevo.set(i, nuevo.get(c));
					nuevo.set(c, j);
				}
			}
		}
		
		return nuevo;
	}
	
	public boolean puedeSeleccionarEquipo(String equipo) {
		int mitad = 0;
		if(this.playerAmount % 2 != 0) {
			mitad = ((int)this.playerAmount /2) + 1;
		}else {
			mitad = (int)this.playerAmount /2;
		}
		if(equipo.equals(this.paintballTeam1.getColor())) {
			int cantidadPreferenciaTeam1 = 0;
			for(PaintballPlayer j : this.getPlayers()) {
				if(j.getPreferenciaTeam() != null && j.getPreferenciaTeam().equals(this.paintballTeam1.getColor())) {
					cantidadPreferenciaTeam1++;
				}
			}

			if(this.playerAmount == 1) {
				return true;
			}
			
			
			if(cantidadPreferenciaTeam1 >= mitad) {
				return false;
			}
		}else {
			int cantidadPreferenciaTeam2 = 0;
			for(PaintballPlayer j : this.getPlayers()) {
				if(j.getPreferenciaTeam() != null &&  j.getPreferenciaTeam().equals(this.paintballTeam2.getColor())) {
					cantidadPreferenciaTeam2++;
				}
			}
			
			if(this.playerAmount == 1) {
				return true;
			}
			if(cantidadPreferenciaTeam2 >= mitad) {
				return false;
			}
		}
		
		return true;
	}
	
	public void modifyTeams(FileConfiguration config) {
		PaintballTeam paintballTeam1 = this.paintballTeam1;
		PaintballTeam paintballTeam2 = this.paintballTeam2;
		String nTeam1 = paintballTeam1.getColor();
		String nTeam2 = paintballTeam2.getColor();
		Random r = new Random();
		ArrayList<String> nombres = new ArrayList<String>();
		for(String key : config.getConfigurationSection("teams").getKeys(false)) {
			nombres.add(key);
		}
		
		int max = nombres.size();
		if(paintballTeam1.isRandom() && !paintballTeam2.isRandom()) {
			int num = r.nextInt(max);
			nTeam1 = nombres.get(num);
			while(nTeam1.equals(nTeam2)) {
				num = r.nextInt(max);
				nTeam1 = nombres.get(num);
			}
			paintballTeam1.setColor(nTeam1);
		}else if(!paintballTeam1.isRandom() && paintballTeam2.isRandom()) {
			int num = r.nextInt(max);
			nTeam2 = nombres.get(num);
			while(nTeam2.equals(nTeam1)) {
				num = r.nextInt(max);
				nTeam2 = nombres.get(num);
			}
			paintballTeam2.setColor(nTeam2);
		}else if(paintballTeam1.isRandom() && paintballTeam2.isRandom()) {
			int num = r.nextInt(max);
			nTeam1 = nombres.get(num);
			num = r.nextInt(max);
			nTeam2 = nombres.get(num);
			while(nTeam2.equals(nTeam1)) {
				num = r.nextInt(max);
				nTeam2 = nombres.get(num);
			}
			paintballTeam1.setColor(nTeam1);
			paintballTeam2.setColor(nTeam2);
		}
	}
}
