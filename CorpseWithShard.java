
public class CorpseWithShard extends Corpse{
	
	private static final long serialVersionUID = 1L;
	boolean received;

	public CorpseWithShard(double width, double height) {
		super(width, height);
		received = false;
	}
	public void interact() {
		if (received) {
			Game.getResponse("You search the body but find nothing else.");
		} else {
			Game.getResponse("This corpse is so thin, it must have died of starvation.\n"
					+ "You search it and find a colorful piece of stone."
					+ (Game.globals.knowAboutGoal ? "\nA shard of the Widow's Heart." : ""));
			received = true;
			shape = Game.NULL;
		}
	}
}
