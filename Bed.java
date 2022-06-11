import java.awt.image.*;

/**
 * A bed, with one of multiple possible blanket states. Remember to set the
 * direction appropriately! [direction == 0 ] => Pillow is at northern end
 * 
 * Note that "direction" must be a multiple of PI/2, else, it will be rounded
 * down to such.
 * 
 * Beds are usually 2x3.7
 **/
public class Bed extends Solid {

	private static final long serialVersionUID = 1L;
	private static BufferedImage[] images = setUp();
	private transient BufferedImage image = null;
	// this determines the blanket state of the bed
	private static int blanketState = 0;

	public Bed(double x, double y, double width, double height, double direction) {
		super(width, height, Game.RECTANGLE);
		moveTo(x, y, Game.getIntersectingTiles(this, x, y));
		blanketState++;
		this.direction = direction - (direction % (Math.PI * 0.5));
	}

	public ScaledImage draw() {
		if (image == null) {
			blanketState %= images.length;
			image = images[blanketState];
		}
		return ScaledImage.get(image);
	}

	public void interact() {
		Game.getResponse("This is a bed.");
	}

	private static BufferedImage[] setUp() {
		BufferedImage[] images = new BufferedImage[5];
		for (int i = 1; i <= images.length; i++) {
			images[i - 1] = ImageMaker.get("bed" + i + ".png");
		}
		return images;
	}

	public void hit(int damage) {
		Game.getResponse("Your sword punctures the mattress.");
	}
}