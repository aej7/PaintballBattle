package pb.ajneb97.eventhandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import pb.ajneb97.PaintballBattle;
import pb.ajneb97.logic.PaintballInstance;

public class MatchEventHandlerNew implements Listener{

	PaintballBattle plugin;
	public MatchEventHandlerNew(PaintballBattle plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void alCambiarDeMano(PlayerSwapHandItemsEvent event) {
		Player jugador = event.getPlayer();
		PaintballInstance paintballInstance = plugin.getPartidaJugador(jugador.getName());
		if(paintballInstance != null) {
			event.setCancelled(true);
		}
	}
}
