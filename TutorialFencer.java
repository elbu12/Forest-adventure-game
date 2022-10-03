public class TutorialFencer extends Fencer {

	private static final long serialVersionUID = 1L;

	int[] tutorialState;

	public TutorialFencer(Solid target, int[] tutorialState) {
		super(target);
		this.tutorialState = tutorialState;
	}

	public void interact() {
		Game.getResponse("'Hello.'");
		Game.tutorialEvent(5);
	}

	public void die() {
		if (tutorialState[0] > 11) {
			super.die();
		}
		// Just relocate it
		Game.resetters.add(this);
		Game.tutorialEvent(6);
	}

	public void act() {
		if (behavior == STAND) {
			direction = Angle.get(target.x - x, target.y - y);
		}
		super.act();
	}

	public void reset() {
		if (tutorialState[0] >= 12) {
			super.reset();
			return;
		}
		double tempy = (Game.player.y < 10 ? Game.player.y + 5 : Game.player.y - 5);
		moveTo(Game.player.x, tempy, Game.getIntersectingTiles(this, Game.player.x, tempy));
		if (tutorialState[0] == 10) {
			int r = (int) (Math.random() * 3);
			fightingStyle = (r == 0 ? SolidActor.IMMEDIATE_ATTACK
					: r == 1 ? SolidActor.COUNTERATTACK : SolidActor.FEINT_THEN_ATTACK);
		}
	}

	public int getDamage() {
		// When this hits, just relocate it
		Game.resetters.add(this);
		Game.player.health = 1;
		return -1;
	}
};