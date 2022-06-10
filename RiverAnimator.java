import java.io.Serializable;

public class RiverAnimator implements Actor, Serializable{

	private static final long serialVersionUID = 1L;
	
	private static RiverAnimator instance = null;
	
	public static int index = 0;
	
	private RiverAnimator() {}
	
	public static RiverAnimator get() {
		if (instance == null) {
			instance = new RiverAnimator();
		}
		return instance;
	}

	public void act() {
		index++;
		if (Game.timeSpeed == 1) {
			index++;	// river runs faster at normal game speed
		}
		index %= Tile.riverImages.length;
	}
}
