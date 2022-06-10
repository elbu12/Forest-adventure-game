import java.awt.*;
import java.awt.image.*;

/**
 * Note: this table is assumed to be vertically oriented! If horizontal, the
 * knives may need to be relocated
 **/
public class TableWithKnives extends Table {

	private static final long serialVersionUID = 1L;

	public TableWithKnives(double x, double y, double width, double height) {
		super(x, y, width, height);
	}

	public void interact() {
		Game.getResponse("This table has some dirty knives on it.");
	}

	protected void makeImage() {
		super.makeImage();
		int w = image.getWidth(); // actual pixel width of image
		int h = image.getHeight(); // actual pixel height of image
		Graphics g = image.createGraphics();
		// the knives
		g.setColor(Color.BLACK);
		g.fillRect(w - 22, (h / 2), 12, 3);
		g.fillRect(w - 25, (h / 2) + 15, 12, 3);
		g.setColor(Color.WHITE);
		g.fillRect(w - 37, (h / 2), 15, 4);
		g.fillRect(w - 43, (h / 2) + 15, 18, 3);
	}
}