package pb.ajneb97.logic;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import pb.ajneb97.PaintballBattle;
import pb.ajneb97.player.PaintballHat;
import pb.ajneb97.database.PaintballPlayerDAO;
import pb.ajneb97.database.MySql;
import pb.ajneb97.player.PaintballPlayer;
import pb.ajneb97.utils.ItemsUtils;

public class HatsInventory implements Listener{

	PaintballBattle plugin;

	public HatsInventory(PaintballBattle plugin) {
		this.plugin = plugin;
	}
	
	public static void createInventory(Player player, PaintballBattle plugin) {
		FileConfiguration config = plugin.getConfig();
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', config.getString("hats_inventory_title")));
		ArrayList<PaintballHat> paintballHats = PaintballPlayerDAO.getHats(player);
		int slot = 0;
		if(paintballHats.isEmpty()) {
			ItemStack item = ItemsUtils.creaItem(config, "hats_items.no_hats");
			inv.setItem(13, item);
		}else {
			FileConfiguration messages = plugin.getMessages();
			for(PaintballHat h : paintballHats) {
				String name = h.getName();
				ItemStack item = ItemsUtils.creaItem(config, "hats_items."+name);
				ItemMeta meta = item.getItemMeta();
				List<String> lore = meta.getLore();
				String status = "";
				if(h.isEquipped()) {
					status = messages.getString("hatStatusSelected");
				}else {
					status = messages.getString("hatStatusNotSelected");
				}
				for(int i=0;i<lore.size();i++) {
					lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i).replace("%status%", status)));
				}
				meta.setLore(lore);
				item.setItemMeta(meta);
				
				if(config.contains("hats_items."+name+".skull_id")) {
					String id = config.getString("hats_items."+name+".skull_id");
					String textura = config.getString("hats_items."+name+".skull_texture");
					item = ItemsUtils.getCabeza(item, id, textura);
				}
				
				inv.setItem(slot, item);
				slot++;
			}
			
			ItemStack item = ItemsUtils.creaItem(config, "hats_items.remove_hat");
			inv.setItem(26, item);
		}
		
		paintballPlayer.openInventory(inv);
	}
	
	@EventHandler
	public void clickInventario(InventoryClickEvent event){
		FileConfiguration config = plugin.getConfig();
		String pathInventory = ChatColor.translateAlternateColorCodes('&', config.getString("hats_inventory_title"));
		String pathInventoryM = ChatColor.stripColor(pathInventory);
		FileConfiguration messages = plugin.getMessages();
		String prefix = ChatColor.translateAlternateColorCodes('&', messages.getString("prefix"))+" ";
		if(ChatColor.stripColor(event.getView().getTitle()).equals(pathInventoryM)){
			if(event.getCurrentItem() == null){
				event.setCancelled(true);
				return;
			}
			if((event.getSlotType() == null)){
				event.setCancelled(true);
				return;
			}else{
				final org.bukkit.entity.Player jugador = (org.bukkit.entity.Player) event.getWhoClicked();
				event.setCancelled(true);
				if(event.getClickedInventory().equals(jugador.getOpenInventory().getTopInventory())) {
					ArrayList<PaintballHat> paintballHats = PaintballPlayerDAO.getHats(jugador);
					ItemStack item = event.getCurrentItem();
					if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
						if(event.getSlot() == 26) {
							if(MySql.isEnabled(config)) {
								MySql.unequipHats(plugin, jugador.getName());
							}else {
								PaintballPlayer jDatos = plugin.getPlayer(jugador.getName());
								jDatos.unequipHat();
							}
							jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("hatRemoved")));
							Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
								public void run() {
									HatsInventory.createInventory(jugador, plugin);
								}
							}, 5L);
							return;
						}
						for(PaintballHat h : paintballHats) {
							ItemStack itemConfig = ItemsUtils.creaItem(config, "hats_items."+h.getName());
							ItemMeta meta = item.getItemMeta();
							ItemMeta metaConfig = itemConfig.getItemMeta();
							if(item.getType().equals(itemConfig.getType()) && meta.getDisplayName().equals(metaConfig.getDisplayName())) {
								//Seleccionar hat
								if(PaintballPlayerDAO.hasHatSelected(jugador, h.getName())) {
									jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("hatAlreadySelected")));
									return;
								}
								if(MySql.isEnabled(config)) {
									MySql.equipHatAsync(plugin, jugador.getName(), h.getName());
								}else {
									PaintballPlayer jDatos = plugin.getPlayer(jugador.getName());
									jDatos.toggleHatEquipped(h.getName());
								}
								jugador.sendMessage(prefix+ChatColor.translateAlternateColorCodes('&', messages.getString("hatSelected").replace("%name%", config.getString("hats_items."+h.getName()+".name"))));
								Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
									public void run() {
										HatsInventory.createInventory(jugador, plugin);
									}
								}, 5L);
								return;
							}
							
						}
					}
				}
			}
		}
	}
}
