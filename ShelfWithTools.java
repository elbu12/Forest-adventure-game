
public class ShelfWithTools extends Shelf {

	private static final long serialVersionUID = 1L;

	public ShelfWithTools(double x, double y, double width, double height) {
		super(x, y, width, height);
	}

	public void interact() {
		Game.getResponse("These shelves have various tools and nails and such,\n" + "but nothing relevant to you.");
	}
}
