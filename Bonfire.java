import java.awt.*;
import java.awt.image.*;

public class Bonfire extends Solid {

	private static final long serialVersionUID = 1L;
	private transient BufferedImage image = null;

	public Bonfire(double x, double y, double width) {
		super(width, width, Game.CIRCLE);
		moveTo(x, y, Game.getIntersectingTiles(this, x, y));
	}

	public ScaledImage draw() {
		if (image == null) {
			image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.createGraphics();
			// The pit
			g.setColor(Color.BLACK);
			g.fillOval(5, 5, 90, 90);
			// The fire
			g.setColor(Color.RED);
			g.fillOval(20, 20, 60, 60);
			g.setColor(Color.ORANGE);
			g.fillOval(25, 25, 50, 50);
			g.setColor(Color.YELLOW);
			g.fillOval(30, 30, 40, 40);
			// The stones
			for (int i = 0; i < 18; i++) {
				int shade = 32 + (int) (Math.random() * 176);
				g.setColor(new Color(shade, shade, shade));
				g.fillOval(42 + (int) (42 * Math.sin(Math.PI * 2.0 * i / 18.0)),
						42 + (int) (42 * Math.cos(Math.PI * 2.0 * i / 18.0)), 16, 16);
			}
			// The spit
			g.setColor(Color.BLACK);
			g.fillRect(18, 47, 64, 6);
			// The meat
			g.setColor(new Color(100, 80, 0));
			g.fillOval(33, 39, 34, 22);
		}
		return ScaledImage.get(image);
	}

	public void interact() {
		Game.getResponse("The roasting meat looks delicious.");
	}
}
