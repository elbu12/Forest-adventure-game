public class TutorialMonster extends Monster {
	
	private static final long serialVersionUID = 1L;
	
	int[] tutorialState;

	public TutorialMonster(Solid target, int[] tutorialState) {
		super(target);
		this.tutorialState = tutorialState;
	}

	public void interact() {
		Game.tutorialEvent(2);
		Game.getResponse("The monster does not react.");
	}

	public void die() {
		if (tutorialState[0] > 4) {
			super.die();
		} else {
			// Just relocate it
			Game.tutorialEvent(3);
			double tempy = (Game.player.y < 10 ? Game.player.y + 5 : Game.player.y - 5);
			moveTo(Game.player.x, tempy, Game.getIntersectingTiles(this, Game.player.x, tempy));
		}
	}

	public int getDamage() {
		// When this hits, just relocate it
		double tempy = (Game.player.y < 10 ? Game.player.y + 5 : Game.player.y - 5);
		moveTo(Game.player.x, tempy, Game.getIntersectingTiles(this, Game.player.x, tempy));
		Game.player.health = 1;
		return -1;
	}
}