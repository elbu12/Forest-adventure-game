import java.awt.Color;

public class MonsterMan extends Talker {

	private static final long serialVersionUID = 1L;

	public MonsterMan(double x, double y, double direction, Color color, Solid target) {
		super(x, y, direction, color, target);
	}

	boolean haveBeenAlerted = false;

	public ScaledImage draw() {
		if (!haveBeenAlerted && Game.distanceBetween2(x, y, target.x, target.y) < Game.screenWidth * Game.screenWidth * 0.2) {
			haveBeenAlerted = true;
			Game.getResponse("You see an old man standing outside a cabin.\nHe seems friendly.");
		}
		return super.draw();
	}

	public void interact() {
		if (Game.globals.monsterDead) {
			Game.getResponse("'I see you killed the monster. Thank you!'");
		} else {
			Game.getResponse("'Beware of the monster that lurks west of here.\nDon't go there unarmed!'");
		}
	}

	public void hit(int damage) {
		Game.getResponse("To talk with the old man, press space when not in combat mode.");
	}
}
