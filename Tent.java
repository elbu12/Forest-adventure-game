import java.awt.*;
import java.awt.image.*;
//this is aan empty tent

public class Tent extends Solid {

	private static final long serialVersionUID = 1L;
	private transient BufferedImage image = null;

	// the tent is not quite square; good dimenions are 93x85
	public Tent(double x, double y, double width, double height) {
		super(width, height, Game.RECTANGLE);
		moveTo(x, y, Game.getIntersectingTiles(this, x, y));
	}

	public ScaledImage draw() {
		if (image == null) {
			image = ImageMaker.get("tent.png");
		}
		// the tent's cords stick out beyond its edge, hence the scaling
		return ScaledImage.get(image, 1.15, 1.16);
	}

	public void interact() {
		Game.getResponse("A simple tent. You see nothing valuable in it.");
	}

	public void hit(int damage) {
		Game.getResponse("Your sword pokes a hole in the tent.");
	}
}
