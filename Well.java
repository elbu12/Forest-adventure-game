import java.awt.*;
import java.awt.image.*;

public class Well extends Solid {

	private static final long serialVersionUID = 1L;
	transient BufferedImage image;
	// color of water within well
	private static Color color = new Color(0, 0, 60);

	public ScaledImage draw() {
		if (image == null) {
			image = new BufferedImage((int) (width * Game.tileWidth), (int) (width * Game.tileWidth),
					BufferedImage.TYPE_INT_ARGB);
			int w = image.getWidth();
			Graphics g = image.createGraphics();
			g.setColor(Color.GRAY); // the stone
			g.fillOval(0, 0, w, w);
			g.setColor(Color.BLACK);
			for (double i = 0; i < 2 * Math.PI; i += Math.PI / 10.0) {
				// the creases between stones
				g.drawLine(w / 2, w / 2, (w / 2) + (int) (w * Math.cos(i) / 2), (w / 2) + (int) (w * Math.sin(i) / 2));
			}
			g.setColor(color);
			g.fillOval(w / 7, w / 7, w * 5 / 7, w * 5 / 7); // the hole
		}
		return ScaledImage.get(image);
	}

	public Well(double width) {
		super(width, width, Game.CIRCLE);
	}

	public void interact() {
		Game.getResponse("This is a well.");
	}
}