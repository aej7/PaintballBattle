package pb.ajneb97.player;

import pb.ajneb97.enums.HatState;

public class PaintballHat {

	private final String hatName;
	private HatState hatState;

	public PaintballHat(String hatName, HatState hatState) {
		this.hatName = hatName;
		this.hatState = hatState;
	}

	public String getName() {
		return hatName;
	}

	public boolean isEquipped() {
		return hatState.getValue();
	}

	public void toggleEquipped() {
		hatState = isEquipped() ? HatState.UNEQUIPPED : HatState.EQUIPPED;
	}

}
