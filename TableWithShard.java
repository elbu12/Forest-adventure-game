import java.awt.*;
import java.awt.image.*;

public class TableWithShard extends Table {

	private static final long serialVersionUID = 1L;
	// images[0] is no shard; images[1] is shard
	protected transient BufferedImage[] images = null;
	public boolean shard = true;

	public TableWithShard(double x, double y, double width, double height) {
		super(x, y, width, height);
	}

	public void obtainShard() {
		shard = false;
	}

	public void interact() {
		if (shard) {
			Game.getResponse(
					"The table has a colorful piece of stone."
							+ (Game.globals.knowAboutGoal ? "\nA shard of the Widow's Heart." : ""),
					new Game.DialogButton("Take it") {
						public void press() {
							obtainShard();
						}
					}, new Game.DialogButton("Leave it"));
		} else {
			Game.getResponse("The table is empty.");
		}
	}

	public ScaledImage draw() {
		if (images == null) {
			makeImage();
			images = new BufferedImage[2];
			// no shard image
			images[0] = image;
			int w; // actual pixel width of image
			int h; // actual pixel height of image
			Graphics g;
			// shard image
			images[1] = new BufferedImage((int) (Game.tileWidth * width), (int) (Game.tileHeight * height),
					BufferedImage.TYPE_INT_ARGB);
			w = images[1].getWidth(); // actual pixel width of image
			h = images[1].getHeight(); // actual pixel height of image
			g = images[1].createGraphics();
			// the table
			g.setColor(TABLE);
			g.fillRoundRect(0, 0, w, h, w / 5, h / 5);
			// the shard
			g.setColor(Color.MAGENTA);
			g.fillOval(w / 2, h / 2, 6, 6);
		}
		return ScaledImage.get(images[(shard ? 1 : 0)]);
	}
}