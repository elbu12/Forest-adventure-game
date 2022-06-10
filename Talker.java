import java.awt.*;
import java.awt.image.*;

public class Talker extends Solid {

	private static final long serialVersionUID = 1L;

	public transient BufferedImage image = null;

	// Where they face when the target is not close
	final public double idleDirection;

	// The abovementioned target
	public Solid target;

	// the maximum distance at which they still face you
	public double faceDistance = 4;

	Color color;

	public Talker(double x, double y, double direction, Color color, Solid target) {
		super(1, 1, Game.CIRCLE);
		moveTo(x, y, Game.getIntersectingTiles(this, x, y));
		this.color = color;
		idleDirection = direction;
		this.direction = direction;
		this.target = target;
	}

	public ScaledImage draw() {
		// is this close enough to face you?
		if (Game.distanceBetween2(x, y, target.x, target.y) < faceDistance * faceDistance) {
			direction = Angle.get(target.x - x, target.y - y);
		} else {
			direction = idleDirection;
		}
		if (image == null) {
			image = Fencer.getBallAndStick(color)[0];
		}
		return ScaledImage.get(image, 1, 1);
	}
}