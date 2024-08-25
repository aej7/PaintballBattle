package pb.ajneb97.player;


public class PaintballKillstreak {

	private String type;
	private int time;
	
	public PaintballKillstreak(String type, int time) {
		this.type = type;
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
	
}
