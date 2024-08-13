package pb.ajneb97.logic;

import org.bukkit.entity.Player;

public class PaintballInstanceEdit {

	private Player jugador;
	private PaintballInstance paintballInstance;
	private String paso;
	public PaintballInstanceEdit(Player jugador, PaintballInstance paintballInstance) {
		this.jugador = jugador;
		this.paintballInstance = paintballInstance;
		this.paso = "";
	}
	public Player getJugador() {
		return jugador;
	}
	public void setJugador(Player jugador) {
		this.jugador = jugador;
	}
	public PaintballInstance getPartida() {
		return paintballInstance;
	}
	public void setPartida(PaintballInstance paintballInstance) {
		this.paintballInstance = paintballInstance;
	}
	public void setPaso(String paso) {
		this.paso = paso;
	}
	public String getPaso() {
		return this.paso;
	}
}
