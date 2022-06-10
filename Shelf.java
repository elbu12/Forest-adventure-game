import java.awt.*;
import java.awt.image.*;

/**
 * Shelves don't look like much Note that, when constructing them, you designate
 * their center, not their northwest corner
 **/
public class Shelf extends Solid {

	private static final long serialVersionUID = 1L;
	protected BufferedImage image = null;

	public Shelf(double x, double y, double width, double height) {
		super(width, height, Game.RECTANGLE);
		moveTo(x, y, Game.getIntersectingTiles(this, x, y));
	}

	public ScaledImage draw() {
		if (image == null) {
			image = new BufferedImage((int) (Game.tileWidth * width), (int) (Game.tileHeight * height),
					BufferedImage.TYPE_INT_ARGB);
			int w = image.getWidth(); // actual pixel width of image
			int h = image.getHeight(); // actual pixel height of image
			Graphics g = image.createGraphics();
			// the table
			g.setColor(new Color(80, 70, 0));
			g.fillRect(0, 0, w, h);
		}
		return ScaledImage.get(image);
	}

	public void interact() {
		Game.getResponse("Dusty shelves with some empty jars.");
	}

}