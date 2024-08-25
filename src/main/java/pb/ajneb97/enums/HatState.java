package pb.ajneb97.enums;

public enum HatState {

  EQUIPPED(true),
  UNEQUIPPED(false);

  private final boolean value;

  HatState(boolean value) {
    this.value = value;
  }

  public boolean getValue() {
    return value;
  }
}
