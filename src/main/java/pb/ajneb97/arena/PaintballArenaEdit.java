package pb.ajneb97.arena;

import org.bukkit.entity.Player;

public class PaintballArenaEdit {

	private Player jugador;
	private PaintballArena paintballArena;
	private String paso;
	public PaintballArenaEdit(Player jugador, PaintballArena paintballArena) {
		this.jugador = jugador;
		this.paintballArena = paintballArena;
		this.paso = "";
	}
	public Player getJugador() {
		return jugador;
	}
	public void setJugador(Player jugador) {
		this.jugador = jugador;
	}
	public PaintballArena getPartida() {
		return paintballArena;
	}
	public void setPartida(PaintballArena paintballArena) {
		this.paintballArena = paintballArena;
	}
	public void setPaso(String paso) {
		this.paso = paso;
	}
	public String getPaso() {
		return this.paso;
	}
}
