import java.awt.*;
import java.awt.image.*;

/**
 * Note: this table is assumed to be horizontally oriented! If vertical, the
 * tools may need to be relocated
 **/
public class TableWithTools extends Table {

	private static final long serialVersionUID = 1L;

	public TableWithTools(double x, double y, double width, double height) {
		super(x, y, width, height);
		moveTo(x, y, Game.getIntersectingTiles(this, x, y));
	}

	public void interact() {
		Game.getResponse("This table has some rusty tools on it.");
	}
	
	protected void makeImage() {
		super.makeImage();
		int w = image.getWidth(); // actual pixel width of image
		int h = image.getHeight(); // actual pixel height of image
		Graphics g = image.createGraphics();
		// the tools:
		g.setColor(Color.BLACK);
		// the hammer
		g.fillRect(15, 15, 25, 6);
		g.fillRect(35, 10, 6, 16);
		// the chisel
		g.fillRect(50, 15, 5, 20);
		// the tongs
		g.fillRect(70, 11, 18, 15);
		g.fillOval(70, 10, 36, 16);
		g.fillOval(100, 10, 20, 16);
		g.setColor(TABLE);
		g.fillRect(70, 15, 18, 7);
		g.fillOval(74, 14, 28, 8);
		g.fillOval(104, 14, 12, 8);
		g.fillRect(110, 16, 10, 4);
		// the saw
		g.setColor(Color.BLACK);
		g.fillRect(135, 11, 10, 16);
		g.setColor(TABLE);
		g.fillRect(137, 16, 6, 6);
		g.setColor(Color.GRAY);
		g.fillRect(145, 12, 30, 14);
	}
}