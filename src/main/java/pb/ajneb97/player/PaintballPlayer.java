package pb.ajneb97.player;

import org.bukkit.entity.Player;
import pb.ajneb97.configuration.PaintballPlayerSavedData;
import pb.ajneb97.enums.HatState;

import java.util.Map;
import java.util.UUID;

public class PaintballPlayer implements IPaintballPlayer {

  private final Player player;
  private Map<String, PaintballHat> hats;
  private Map<String, PaintballPerk> perks;
  private Map<String, PaintballKillstreak> killstreaks;
  private Map<String, PaintballStats> stats;
  private final UUID uuid;
  private int coins;
  private String preferredTeam;
  private PaintballPlayerSavedData paintballPlayerSavedData;
  private boolean wasKilledRecently;

  public PaintballPlayer(Player player, Map<String, PaintballHat> hats, Map<String, PaintballPerk> perks, Map<String, PaintballKillstreak> killstreaks, Map<String, PaintballStats> stats) {
    this.player = player;
    uuid = player.getUniqueId();
  }

  public Player getPlayerEntity() {
    return player;
  }

  @Override
  public void addHat(PaintballHat hat) {
    hats.put(hat.getName(), hat);
  }

  @Override
  public void removeHat(PaintballHat hat) {
    hats.remove(hat.getName());
  }

  @Override
  public Map<String, PaintballHat> getHats() {
    return hats;
  }

  @Override
  public boolean hasAnyHatEquipped() {
    return hats.values().stream().anyMatch(PaintballHat::isEquipped);
  }

  @Override
  public boolean hasHatEquipped(PaintballHat hat) {
    return hats.get(hat.getName()).isEquipped();
  }

  @Override
  public void toggleHatEquipped(PaintballHat hat) {
    hats.get(hat.getName()).toggleEquipped();
  }

  @Override
  public void addPerk(PaintballPerk perk) {
    perks.put(perk.getName(), perk);
  }

  @Override
  public void removePerk(PaintballPerk perk) {
  perks.remove(perk.getName());
  }

  @Override
  public Map<String, PaintballPerk> getPerks() {
    return perks;
  }

  @Override
  public void setPerkLevel(PaintballPerk perk, int perkLevel) {
    perks.get(perk.getName()).setLevel(perkLevel);
  }

  @Override
  public int getPerkLevel(PaintballPerk perk) {
    return perks.get(perk.getName()).getLevel();
  }

  @Override
  public int getCoins() {
    return coins;
  }

  @Override
  public void setCoins(int coins) {
    this.coins = coins;
  }

  @Override
  public UUID getUUID() {
    return uuid;
  }

  @Override
  public String getName() {
    return player.getName();
  }

  @Override
  public void setPerks(Map<String, PaintballPerk> perks) {
    this.perks = perks;
  }

  @Override
  public void setHats(Map<String, PaintballHat> hats) {
    this.hats = hats;
  }

  @Override
  public String getPreferredTeam() {
    return preferredTeam;
  }

  @Override
  public void setPreferredTeam(String preferredTeam) {
    this.preferredTeam = preferredTeam;
  }

  @Override
  public PaintballPlayerSavedData getSavedItems() {
    return paintballPlayerSavedData;
  }

  @Override
  public boolean getWasKilledRecently() {
    return wasKilledRecently;
  }

  @Override
  public void setWasKilledRecently(boolean wasKilledRecently) {
    this.wasKilledRecently = wasKilledRecently;
  }
}
