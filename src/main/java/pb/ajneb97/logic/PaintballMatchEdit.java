package pb.ajneb97.logic;

import org.bukkit.entity.Player;

public class PaintballMatchEdit {

	private Player jugador;
	private PaintballMatch paintballMatch;
	private String paso;
	public PaintballMatchEdit(Player jugador, PaintballMatch paintballMatch) {
		this.jugador = jugador;
		this.paintballMatch = paintballMatch;
		this.paso = "";
	}
	public Player getJugador() {
		return jugador;
	}
	public void setJugador(Player jugador) {
		this.jugador = jugador;
	}
	public PaintballMatch getPartida() {
		return paintballMatch;
	}
	public void setPartida(PaintballMatch paintballMatch) {
		this.paintballMatch = paintballMatch;
	}
	public void setPaso(String paso) {
		this.paso = paso;
	}
	public String getPaso() {
		return this.paso;
	}
}
