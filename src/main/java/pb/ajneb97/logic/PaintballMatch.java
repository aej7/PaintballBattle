package pb.ajneb97.logic;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import pb.ajneb97.enums.MatchState;

import java.util.ArrayList;
import java.util.Random;



public class PaintballMatch {

	private final Team team1;
	private final Team team2;
	private final String matchNumber;
	private int maximumPlayerAmount;
	private int minimumPlayerAmount;
	private int playerAmount;
	private MatchState matchState;
	private Location lobby;
	private int time;
	private int maximumTime;
	private int initialLives;
	private boolean isNuke;
	
	public PaintballMatch(String matchNumber, int maximumTime, String team1, String team2, int initialLives) {
		//por defecto
		this.team1 = new Team(team1);
		this.team2 = new Team(team2);
		this.matchNumber = matchNumber;
		this.maximumPlayerAmount = 16;
		this.minimumPlayerAmount = 4;
		this.playerAmount = 0;
		this.matchState = MatchState.OFF;
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
		if(team1.addPlayer(player)) {
			this.playerAmount++;
		}	
	}
	
	public void changePlayerToTeam2(PaintballPlayer player) {
		this.team1.removePlayer(player.getJugador().getName());
		this.team2.addPlayer(player);
	}
	
	public void removePlayer(String player) {
		if(team1.removePlayer(player) || team2.removePlayer(player)) {
			this.playerAmount--;
		}	
	}
	
	public ArrayList<PaintballPlayer> getPlayers(){
		ArrayList<PaintballPlayer> players = new ArrayList<PaintballPlayer>();

    players.addAll(team1.getPlayers());
    players.addAll(team2.getPlayers());
		
		return players;
	}
	
	public PaintballPlayer getPlayer(String playerName) {
		for (PaintballPlayer player : getPlayers()) {
			if (player.getJugador().getName().equals(playerName)) {
				return player;
			}
		}
		return null;
	}
	
	public Team getEquipoJugador(String jugador) {
		ArrayList<PaintballPlayer> jugadoresTeam1 = team1.getPlayers();
		for(int i=0;i<jugadoresTeam1.size();i++) {
			if(jugadoresTeam1.get(i).getJugador().getName().equals(jugador)) {
				return this.team1;
			}
		}
		ArrayList<PaintballPlayer> jugadoresTeam2 = team2.getPlayers();
		for(int i=0;i<jugadoresTeam2.size();i++) {
			if(jugadoresTeam2.get(i).getJugador().getName().equals(jugador)){
				return this.team2;
			}
		}
		
		return null;
	}
	
	public Team getTeam1() {
		return this.team1;
	}
	
	public Team getTeam2() {
		return this.team2;
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
	
	public MatchState getState() {
		return this.matchState;
	}
	
	public void setState(MatchState estado) {
		this.matchState = estado;
	}
	
	public boolean estaIniciada() {
		if(!this.matchState.equals(MatchState.WAITING) && !this.matchState.equals(MatchState.STARTING)) {
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
	
	public boolean estaActivada() {
		if(this.matchState.equals(MatchState.OFF)) {
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
	
	public Team getGanador() {
		if(team1.getPlayers().size() == 0) {
			return team2;
		}
		if(team2.getPlayers().size() == 0) {
			return team1;
		}
		
		int vidasTeam1 = team1.getVidas();
		int vidasTeam2 = team2.getVidas();
		if(vidasTeam1 > vidasTeam2) {
			return team1;
		}else if(vidasTeam2 > vidasTeam1) {
			return team2;
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
				if(nuevo.get(i).getAsesinatos() < nuevo.get(c).getAsesinatos()) {
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
		if(equipo.equals(this.team1.getTipo())) {
			int cantidadPreferenciaTeam1 = 0;
			for(PaintballPlayer j : this.getPlayers()) {
				if(j.getPreferenciaTeam() != null && j.getPreferenciaTeam().equals(this.team1.getTipo())) {
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
				if(j.getPreferenciaTeam() != null &&  j.getPreferenciaTeam().equals(this.team2.getTipo())) {
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
		Team team1 = this.team1;
		Team team2 = this.team2;
		String nTeam1 = team1.getTipo();
		String nTeam2 = team2.getTipo();
		Random r = new Random();
		ArrayList<String> nombres = new ArrayList<String>();
		for(String key : config.getConfigurationSection("teams").getKeys(false)) {
			nombres.add(key);
		}
		
		int max = nombres.size();
		if(team1.esRandom() && !team2.esRandom()) {
			int num = r.nextInt(max);
			nTeam1 = nombres.get(num);
			while(nTeam1.equals(nTeam2)) {
				num = r.nextInt(max);
				nTeam1 = nombres.get(num);
			}
			team1.setTipo(nTeam1);
		}else if(!team1.esRandom() && team2.esRandom()) {
			int num = r.nextInt(max);
			nTeam2 = nombres.get(num);
			while(nTeam2.equals(nTeam1)) {
				num = r.nextInt(max);
				nTeam2 = nombres.get(num);
			}
			team2.setTipo(nTeam2);
		}else if(team1.esRandom() && team2.esRandom()) {
			int num = r.nextInt(max);
			nTeam1 = nombres.get(num);
			num = r.nextInt(max);
			nTeam2 = nombres.get(num);
			while(nTeam2.equals(nTeam1)) {
				num = r.nextInt(max);
				nTeam2 = nombres.get(num);
			}
			team1.setTipo(nTeam1);
			team2.setTipo(nTeam2);
		}
	}
}
