import java.util.ArrayList;

class FestivalRaider extends Fencer {

	private static final long serialVersionUID = 1L;
	static ArrayList <SolidActor> villagers;
	static ArrayList <Actor> actors;
	static ArrayList <Fencer> raiders;

	public FestivalRaider(Solid target, double x, double y) {
		super(target);
		// note how they are not automatically added to "actors!"
		// You must do this when appropriate!
		this.x = x;
		this.y = y;
		setDetectionRange(7.1);
		pathFindingIterations = 60;
		behavior = WANDER_UNTIL_DETECT_THEN_ATTACK;
		intention = STAND;
	}

	public void act() {
		if (intention == WANDER || !canHit(target) || !actors.contains(target)) {
			/**
			 * If current target is dead or too far, find the closest acceptable one. If no
			 * target can be found, begin wandering.
			 **/
			Solid newTarget = Game.getClosestSolidInCollection(this, villagers, detectionRange);
			if (newTarget != this) {
				// found a new target
				target = newTarget;
				if (intention != ATTACK) {
					intendToAttack();
				}
			} else {
				// target died; begin wandering
				if (intention == ATTACK && !actors.contains(target)) {
					// was previously attacking; now wander
					intention = WANDER;
					resetActions();
					action = STAND;
					state = 0;
					speed = slowSpeed;
					wx = x - 8;
					wy = y - 8;
					ww = 16;
					wh = 16;
					tx = x;
					ty = y;
					// the next line exists to prevent this raider's dead target
					// from constantly alerting it out of wandering
					target = Game.player;
				}
			}
		}
		super.act();
	}

	public void interact() {
		// This is how the raiders are "summoned"
		if (intention == STAND) {
			intention = ATTACK;
			place(x, y);
		}
	}

	public void die() {
		// no need to track dead raiders
		raiders.remove(this);
		super.die();
	}
}