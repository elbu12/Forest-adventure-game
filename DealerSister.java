import java.awt.Color;
import java.awt.image.BufferedImage;

public class DealerSister extends Navigator{
	
	private static final long serialVersionUID = 1L;
	transient BufferedImage image = null;
	TableWithShard table;
	Junkie junkie;

	public DealerSister(double width, double height, TableWithShard table, Junkie junkie) {
		super(width, height);
		behavior = Navigator.STAND;
		intention = SolidActor.STAND;
		slowSpeed = Game.player.slowSpeed;
		speed = slowSpeed;
		this.table = table;
		this.junkie = junkie;
	}
	
	public void hit(int damage) {
		Game.getResponse("To talk with the woman, press space when not in combat mode.");
	}
	
	public ScaledImage draw() {
		if (image == null) {
			image = Fencer.getBallAndStick(new Color(90, 90, 30))[0];
		}
		return ScaledImage.get(image);
	}

	public void act() {
		if (intention == MOVE) {
			if (dieWhenFar(15)) {
				// remove if out of sight
				Game.dealerSisterGone = true;
				// remove reference so Garbage Collector will take this
				Game.dealerSister = null;
			} else if (x == tx && y == ty) {
				// reached junkie's location; stare into space
				direction = 0;
			} else {
				// move toward the junkie's location, (tx,ty)
				navigateToward(tx, ty);
				if (nextNode[0] != this.x || nextNode[1] != this.y) {
					direction = Angle.get(nextNode[0] - this.x, nextNode[1] - this.y);
				}
			}
		}
	}
	
	public void interact() {
		if (Game.globals.junkieState == Junkie.UNAWARE || Game.globals.junkieState == Junkie.AWARE ||
				Game.globals.junkieState == Junkie.CHOOSING) {
			talk(0);
		} else if (Game.globals.junkieState == Junkie.KILLED) {
			if (Game.receivedRewardForJunkie) {
				talk(2);
			} else {
				talk(1);
			}
		} else if (Game.globals.junkieState == Junkie.GONE) {
			if (intention == MOVE && this.x == tx && this.y == ty) {
				talk(5);
			} else {
				talk(3);
			}
		}
	}
	public void talk(int i) {
		if (i == 0) {
			Game.getResponse("You talk to a woman. She mentions how her sister was recently killed by a crazy\n"
					+ "drug addict, who fled the area afterward. She describes the killer and promises\n"
					+ "a reward for his head.");
			// now that you have talked to the sister, you can meet the junkie.
			// Also, since you will see the junkie, you do not need to know that
			// his camp was recently occupied
			if (Game.globals.junkieState == Junkie.UNAWARE) {
				Game.globals.junkieState = Junkie.AWARE;
				junkie.place(junkie.x, junkie.y);
			}
		} else if (i == 1) {
			Game.getResponse("You present the killer's head. The woman doesn't know what to do with it.\n"
					+ "She has you throw it away, but thanks you sincerely and presents your reward:\n"
					+ "A colorful piece of stone." + (Game.globals.knowAboutGoal ? " A shard of the Widow's Heart." : ""));
			Game.receivedRewardForJunkie = true;
			table.obtainShard();
		} else if (i == 2) {
			Game.getResponse("You talk to the woman whose sister was killed. She thanks you again for your help.");
		} else if (i == 3) {
			Game.getResponse("This is the woman whose sister was killed by the addict.",
					new Game.DialogButton("Tell her you found the killer's camp") {
						public void press() {
							talk(4);
						}
					}, new Game.DialogButton("Don't mention it"));
		} else if (i == 4) {
			Game.getResponse("You describe the location of the killer's camp. The woman thanks you sincerely.");
			intention = SolidActor.MOVE;
			tx = junkie.x;
			ty = junkie.y;
			// we no longer need any information from the actual "junkie" object
			// so change it to "null" and let the Garbage Collector take it
			junkie = null;
		} else if (i == 5) {
			Game.getResponse("The camp is abandoned. The woman studies the area, looking for tracks.");
		}
	}
}
