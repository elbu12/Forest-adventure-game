import java.awt.Color;

public class OldWoman extends Talker{

	private static final long serialVersionUID = 1L;

	public OldWoman(double x, double y, double direction, Color color, Solid target) {
		super(x, y, direction, color, target);
	}
	boolean haveBeenAlerted = false;

	public ScaledImage draw() {
		if (!haveBeenAlerted && Game.distanceBetween2(x, y, target.x, target.y) < Game.screenWidth * Game.screenWidth * 0.2) {
			haveBeenAlerted = true;
			Game.getResponse("You see an old woman washing something in the river.\nShe seems friendly.");
		}
		return super.draw();
	}

	public void interact() {
		Game.getResponse("The old woman tells you about the legend of the Widow's Heart,\n"
				+ "a broken gemstone, pieces of which were scattered throughout\n"
				+ "the woods north of the river. Supposedly one shard is in the\n"
				+ "abandoned city to the northwest.\n\n" + "'But don't go there. It's full of monsters!'");
		Game.globals.knowAboutGoal = true;
	}

	public void hit(int damage) {
		Game.getResponse("To talk with the old woman, press space when not in combat mode.");
	}
}
