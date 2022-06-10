import java.util.ArrayList;

public class StalkerTalker extends Fencer {

	private static final long serialVersionUID = 1L;
	
	ArrayList <Navigator> stalkers;

	public StalkerTalker(Solid target, double x, double y, ArrayList <Navigator> stalkers) {
		super(target, x, y);
		this.stalkers = stalkers;
	}

	public void act() {
		switch (behavior) {
		case STAND_UNTIL_DETECT_THEN_ATTACK:
			if (intention == STAND) {
				if (target.x >= vx && target.x < vx + vw && target.y >= vy && target.y < vy + vh) {
					// If you enter the area he "guards," he will approach
					intendToAttack();
				}
				// Else, just stand around
			} else if (Game.distanceBetween2(this.x, this.y, target.x, target.y) < 3.5 * 3.5) {
				// Close enough to talk
				Game.getResponse(stalkers.size() > 0
						? "'Hello! I see you have discovered our little growing operation.\n"
								+ "Unfortunately, we can't let you leave with this information.\n"
								+ "Please try not to disturb the plants as we kill you.'"
						: "'Please don't kill me! Please don't hurt the plants, either!'");
				for (Navigator s : stalkers) {
					s.intendToAttack();
					s.pathFindingIterations = 299;
				}
				behavior = STAND;
				intention = FLEE;
				speed = fastSpeed;
			} else {
				// Approach
				navigateToward(target);
				direction = Angle.get(target.x - this.x, target.y - this.y);
			}
			break;
		case STAND:
			if (intention == FLEE) {
				// Run to hiding spot
				if (this.x == tx && this.y == ty) {
					intention = STAND;
					direction = Angle.get(0, 1);
				} else {
					double oldx = this.x;
					double oldy = this.y;
					navigateToward(tx, ty);
					direction = Angle.get(this.x - oldx, this.y - oldy);
				}
			} else if (Game.distanceBetween2(this.x, this.y, target.x, target.y) < 1.5 * 1.5) {
				// face you if close enough
				direction = Angle.get(target.x - this.x, target.y - this.y);
			} else {
				direction = 0;
			}
			break;
		}
	}

	public void interact() {
		Game.getResponse("'Sorry! Please don't kill me!'");
	}
}