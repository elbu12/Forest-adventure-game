
public class HidingWanderingMonster extends WanderingMonster{

	private static final long serialVersionUID = 1L;

	public HidingWanderingMonster(double x, double y, double wx, double wy, double ww, double wh, Solid target) {
		super(x, y, wx, wy, ww, wh, target);
		hiding = true;
	}
	protected boolean canDetect(Solid s) {
		// is the target within the "visibility" area?
		if (isVisible()) {
			// yes; are they within normal detection range?
			return super.canDetect(s);
		} else {
			// target outside of specified area; will not detect
			return false;
		}
	}
}
