public class Junkie extends Fencer{

	private static final long serialVersionUID = 1L;

	public Junkie(Solid target) {
		super(target);
		behavior = Navigator.DEFEND_TERRITORY;
		setDetectionRange(4);
		fightingStyle = SolidActor.IMMEDIATE_ATTACK;
	}
	public boolean place(double x, double y) {
		// set the point the junkie will defend
		tx = x;
		ty = y;
		return super.place(x, y);
	}
	public void act() {
		if (Game.globals.junkieState == AWARE) {
			// the junkie faces you
			direction = Angle.get(Game.player.x - this.x, Game.player.y - this.y);
		} else if (Game.globals.junkieState== CHOOSING && dieWhenFar(15)) {
			// You left.
			Game.globals.junkieState = GONE;
		} else {
			super.act();
		}
	}
	public void reset() {
		if (action == DIE && Game.globals.junkieState == CHOOSING) {
			// You just killed the junkie!
			Game.globals.junkieState = KILLED;
			Game.getResponse(
					"You kill the addict. It's ugly. He's crying, bleeding, trying to hold in his guts.\n"
							+ "When it's over, you take his head.");
		}
		super.reset();
	}

	public ScaledImage draw() {
		if (Game.globals.junkieState == AWARE && Game.distanceBetween2(this.x, this.y, target.x, target.y) < 5 * 5) {
			// you got close enough. Time for the discussion!
			Game.globals.junkieState = CHOOSING;
			talk(0);
		}
		return super.draw();
	}
	public void talk(int i) {
		if (i == 0) {
			Game.getResponse(
					"You find a man matching the description of the addict who killed\n"
							+ "the woman's sister. He is clearly anxious.",
					new Game.DialogButton("Confront him about the murder") {
						public void press() {
							talk(1);
						}
					}, new Game.DialogButton("Leave") {
						public void press() {
							talk(2);
						}
					});
		} else if (i == 1) {
			Game.getResponse(
					"'It was an accident! I did not mean to kill her! But she attacked\n"
							+ "me and I had to defend myself. Please don't kill me!'",
					new Game.DialogButton("'Why did she attack you?'") {
						public void press() {
							talk(3);
						}
					}, new Game.DialogButton("Ok"));
		} else if (i == 2) {
			Game.getResponse("The man watches you carefully.");
		} else if (i == 3) {
			Game.getResponse("'I think she was afraid I would hurt her and she tried to strike first.'",
					new Game.DialogButton("'Why did she think you would hurt her?'") {
						public void press() {
							talk(4);
						}
					}, new Game.DialogButton("Ok"));
		} else if (i == 4) {
			Game.getResponse("'I had drawn my sword.'", new Game.DialogButton("'Why?'") {
				public void press() {
					talk(5);
				}
			}, new Game.DialogButton("Ok"));
		} else if (i == 5) {
			Game.getResponse("'I just needed another hit. I was desperate.'",
					new Game.DialogButton("'So you tried to rob her?'") {
						public void press() {
							talk(6);
						}
					}, new Game.DialogButton("Ok"));
		} else if (i == 6) {
			Game.getResponse("'She raised her prices. I could not afford it.\nI would have paid the old price.'");
		}
	}
	final public static int UNAWARE = 0;
	final public static int AWARE = 1;
	final public static int CHOOSING = 2;
	final public static int GONE = 3;
	final public static int KILLED = 4;
}
