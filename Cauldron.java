import java.awt.*;
import java.awt.image.*;

/**
 * A cauldron is actively being heated, so the contents should move and bubble
 * over time.
 **/
public class Cauldron extends Solid {

	private static final long serialVersionUID = 1L;
	
	final private static Color STEW = new Color(90, 60, 0);
	final private static Color BUBBLE = new Color(150, 150, 50);
	/**
	 * All cauldrons use the same set of images, forming a looped animation
	 **/
	private static BufferedImage[] images = setUp();

	// i is the index number for the animation
	private int i = 0;

	public Cauldron(double x, double y, double width) {
		super(width, width, Game.CIRCLE);
		moveTo(x, y, Game.getIntersectingTiles(this, x, y));
	}

	public ScaledImage draw() {
		ScaledImage s = ScaledImage.get(images[i / 2]);
		for (double j = 0; j < Game.timeSpeed; j += 0.5) {
			i++;
		}
		i %= images.length * 2;
		return s;
	}

	private static BufferedImage[] setUp() {
		// l is the number of images
		int l = 160;
		// images is the actual array of images
		images = new BufferedImage[l];
		// "image" is the neutral image to show with no bubble
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.fillOval(0, 0, 100, 100);
		g.setColor(STEW);
		g.fillOval(10, 10, 80, 80);
		for (int j = 0; j < l; j++) {
			// now iterate and make each image
			if (j < 16) {
				images[j] = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
				g = images[j].createGraphics();
				g.setColor(Color.BLACK);
				g.fillOval(0, 0, 100, 100);
				g.setColor(STEW);
				g.fillOval(10, 10, 80, 80);
				int diam = j;
				g.setColor(BUBBLE);
				g.drawOval(30 - (diam / 2), 40 - (diam / 2), diam, diam);
			} else if (j > 31 && j < 48) {
				images[j] = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
				g = images[j].createGraphics();
				g.setColor(Color.BLACK);
				g.fillOval(0, 0, 100, 100);
				g.setColor(STEW);
				g.fillOval(10, 10, 80, 80);
				int diam = j - 32;
				g.setColor(BUBBLE);
				g.drawOval(60 - (diam / 2), 50 - (diam / 2), diam, diam);
			} else if (j > 63 && j < 80) {
				images[j] = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
				g = images[j].createGraphics();
				g.setColor(Color.BLACK);
				g.fillOval(0, 0, 100, 100);
				g.setColor(STEW);
				g.fillOval(10, 10, 80, 80);
				int diam = j - 64;
				g.setColor(BUBBLE);
				g.drawOval(40 - (diam / 2), 60 - (diam / 2), diam, diam);
			} else if (j > 95 && j < 112) {
				images[j] = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
				g = images[j].createGraphics();
				g.setColor(Color.BLACK);
				g.fillOval(0, 0, 100, 100);
				g.setColor(STEW);
				g.fillOval(10, 10, 80, 80);
				int diam = j - 96;
				g.setColor(BUBBLE);
				g.drawOval(45 - (diam / 2), 65 - (diam / 2), diam, diam);
			} else if (j > 127 && j < 144) {
				images[j] = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
				g = images[j].createGraphics();
				g.setColor(Color.BLACK);
				g.fillOval(0, 0, 100, 100);
				g.setColor(STEW);
				g.fillOval(10, 10, 80, 80);
				int diam = j - 128;
				g.setColor(BUBBLE);
				g.drawOval(55 - (diam / 2), 40 - (diam / 2), diam, diam);
			} else {
				images[j] = image;
			}
		}
		return images;
	}

	public void interact() {
		Game.getResponse("A cauldron. Something is cooking.");
	}
}