public class TrapSpider extends Spider {

	private static final long serialVersionUID = 1L;

	// When you enter a specified "trap" area, this and its friends
	// will all attack simultaneously
	// the "trap" area is represented by the wx/wy/ww/wh variables
	public TrapSpider(double x, double y, Solid target, double vx, double vy, double vw, double vh, double wx,
			double wy, double ww, double wh) {
		super(target, x, y);
		setVisibilityArea(vx, vy, vw, vh);
		this.wx = wx;
		this.wy = wy;
		this.ww = ww;
		this.wh = wh;
		hiding = true;
	}

	protected boolean canDetect(Solid s) {
		// is the target within the "trap" area?
		if (target.x >= wx && target.x < wx + ww && target.y >= wy && target.y < wy + wh) {
			return true;
		} else {
			// no; are they within normal detection range?
			return super.canDetect(s);
		}
	}
}