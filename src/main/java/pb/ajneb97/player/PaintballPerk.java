package pb.ajneb97.player;

public class PaintballPerk {

	private final String perkName;
	private int level;

	public PaintballPerk(String perkName, int level) {
		this.perkName = perkName;
		this.level = level;
	}

	public String getName() {
		return perkName;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	
}
