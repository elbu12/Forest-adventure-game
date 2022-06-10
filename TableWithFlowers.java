import java.awt.*;
import java.awt.image.*;

public class TableWithFlowers extends Table {

	private static final long serialVersionUID = 1L;

	public TableWithFlowers(double x, double y, double width, double height) {
		super(x, y, width, height);
	}

	public void interact() {
		Game.getResponse("This table has some flowers and stems.");
	}

	private int r(int n) {
		return (int) (Math.random() * n);
	}

	protected void makeImage() {
		super.makeImage();
		int w = image.getWidth(); // actual pixel width of image
		int h = image.getHeight(); // actual pixel height of image
		Graphics g = image.createGraphics();
		// the flowers
		for (int i = 0; i < 9; i++) {
			// flower x
			int fx = 10 + (14 * i) + (i > 4 ? 12 : 0) + (int) (Math.random() * 6);
			// stalk length
			int sl = (i > 4 ? 6 + (int) (Math.random() * 6) : 0);
			// flower y
			int fy = (10 + sl + (int) (Math.random() * 10));
			// flower diameter
			int fd = 6 + (int) (Math.random() * 4);
			g.setColor(new Color(0, 128 + (int) (Math.random() * 64), 0));
			g.fillOval(fx - 2, fy - sl - (fd / 2), 4, sl + (fd / 2));
			g.setColor(SpecialBush.outerColor);
			g.fillOval(fx - (fd / 2), fy - (fd / 2), fd, fd);
			g.setColor(SpecialBush.innerColor);
			g.fillOval(fx - (fd / 2) + 2, fy - (fd / 2) + 2, fd - 4, fd - 4);
		}
		// the lone stalks
		for (int i = 0; i < 6; i++) {
			g.setColor(new Color(0, 128 + (int) (Math.random() * 64), 0));
			g.fillOval(4 * w / 5 + (int) (Math.random() * 20), 5 + (int) (Math.random() * 5), 4,
					9 + (int) (Math.random() * 5));
		}
	}
}