package pb.ajneb97.player;

public class PaintballStats implements IPaintballStats{

  private int wins;
  private int losses;
  private int ties;
  private int kills;

  public PaintballStats(int wins, int losses, int ties, int kills) {
    this.wins = wins;
    this.losses = losses;
    this.ties = ties;
    this.kills = kills;
  }

  @Override
  public int getWins() {
    return wins;
  }

  @Override
  public void setWins(int wins) {
    this.wins = wins;
  }

  @Override
  public int getLosses() {
    return losses;
  }

  @Override
  public void setLosses(int losses) {
    this.losses = losses;
  }

  @Override
  public int getTies() {
    return ties;
  }

  @Override
  public void setTies(int ties) {
    this.ties = ties;
  }

  @Override
  public int getKills() {
    return kills;
  }

  @Override
  public void setKills(int kills) {
    this.kills = kills;
  }
}
