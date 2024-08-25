package pb.ajneb97.player;

import org.bukkit.entity.Player;
import pb.ajneb97.configuration.PaintballPlayerSavedData;

import java.util.Map;
import java.util.UUID;

public interface IPaintballPlayer {

  UUID getUUID();
  String getName();
  Player getPlayerEntity();

  String getPreferredTeam();
  void setPreferredTeam(String preferredTeam);

  void addHat(PaintballHat hat);
  void removeHat(PaintballHat hat);
  Map<String, PaintballHat> getHats();
  void setHats(Map<String, PaintballHat> hats);
  boolean hasHatEquipped(PaintballHat hat);
  boolean hasAnyHatEquipped();
  void toggleHatEquipped(PaintballHat hat);

  void addPerk(PaintballPerk perk);
  void removePerk(PaintballPerk perk);
  Map<String, PaintballPerk> getPerks();
  void setPerks(Map<String, PaintballPerk> perks);
  void setPerkLevel(PaintballPerk perk, int perkLevel);
  int getPerkLevel(PaintballPerk perk);

  int getCoins();
  void setCoins(int coins);

  PaintballPlayerSavedData getSavedItems();

  boolean getWasKilledRecently();
  void setWasKilledRecently(boolean wasKilledRecently);
}
