public class Gardener extends Fencer {

	private static final long serialVersionUID = 1L;

	public Gardener(Solid target, double x, double y) {
		super(target, x, y);
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