import java.util.ArrayList;

public class CastleSummoner extends Fencer {

	private static final long serialVersionUID = 1L;
	
	ArrayList <Navigator> reinforcements;
	Gardener gardener;
	ArrayList <Actor> actors;

	public CastleSummoner(Solid target, ArrayList <Navigator> reinforcements, Gardener gardener,
			ArrayList <Actor> actors) {
		super(target);
		this.reinforcements = reinforcements;
		this.gardener = gardener;
		this.actors = actors;
		behavior = Navigator.STAND;
		intention = SolidActor.STAND;
		hiding = true;
		slowSpeed = 0.1;
		speed = slowSpeed;
		setDetectionRange(4.5);
	}

	boolean sayThing = false;

	public void act() {
		if (action == DIE) {
			// he does not get to act if he is already dying
			return;
		}
		if (behavior == Navigator.STAND) {
			// summoner is not yet ready to fight
			if (intention == SolidActor.STAND) {
				// summoner is standing, waiting for you
				if (isVisible() && Game.distanceBetween2(Game.player.x, Game.player.y, this.x, this.y) < 4 * 4) {
					// if very close, talk
					hiding = false;
					Game.getResponse("'Whoa, whoa, whoa! Please, don't kill me!'", new Game.DialogButton("Spare him") {
						public void press() {
							summon(); // call the reinforcements!
						}
					}, new Game.DialogButton("No mercy") {
						public void press() {
							// you chose no mercy. Now he fights
							intendToAttack();
						}
					});
				} else if (canDetect(Game.player)) {
					// face you when you're close
					direction = Angle.get(Game.player.x - this.x, Game.player.y - this.y);
				}
			} else if (intention == MOVE) {
				// summoner is moving toward the reinforcements
				navigateToward(tx, ty);
				if (this.x != nextNode[0] || this.y != nextNode[1]) {
					direction = Angle.get(nextNode[0] - this.x, nextNode[1] - this.y);
				}
				if (this.x == tx && this.y == ty) {
					// summoner reaches reinforcements; ready to fight
					sayThing = true;
					direction = 1.5 * Math.PI;
					intention = SolidActor.STAND;
					behavior = STAND_UNTIL_DETECT_THEN_ATTACK;
					state = 0;
					resetActions();
				}
			}
		} else {
			super.act();
		}
	}

	public void intendToAttack() {
		if (sayThing) {
			// if he has made it to his "standing point," then you get this message
			Game.getResponse(
					"You see a familiar face: the man whose life you spared.\nHe does not seem eager to return the favor.");
		} else {
			// if this happens, either you chose "no mercy" or attacked his reinforcements
			// before he got there
			behavior = STAND_UNTIL_DETECT_THEN_ATTACK;
		}
		super.intendToAttack();
	}

	public void summon() {
		// this right here is the "summoning"
		for (Navigator nav : reinforcements) {
			nav.place(nav.x, nav.y);
		}
		friends = reinforcements;
		reinforcements.add(this);
		intention = SolidActor.MOVE;
		// the gardener, if alive, joins the reinforcements
		// this is implemented by him disappearing from his
		// usual position
		if (actors.contains(gardener)) {
			gardener.die();
		}
	}

	public void interact() {
		if (intention == MOVE) {
			Game.getResponse("'Please let me go!'");
		}
	}
}