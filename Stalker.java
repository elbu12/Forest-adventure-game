class Stalker extends Fencer {

	private static final long serialVersionUID = 1L;

	public Stalker(Solid target, double x, double y, int areax, int areay, int w, int h) {
		super(target, x, y);
		wx = areax;
		wy = areay;
		ww = w;
		wh = h;
		tx = this.x;
		ty = this.y;
		behavior = STALK_UNTIL_DETECT_THEN_ATTACK;
		intention = STALK;
		setDetectionRange(4);
		slowSpeed = 0.1;
		pathFindingIterations = 60;
		speed = slowSpeed;
	}

	public void die() {
		friends.remove(this);
		super.die();
	}
}