import java.io.Serializable;
import java.util.ArrayList;

public class SavedGame implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	Tile[][] tiles;
	ArrayList <Actor> actors;
	ArrayList <SolidActor> villagers;
	ArrayList <Fencer> raiders;
	GlobalVariables globals;
	Player player;
	VillageDance dance;
	double timeSpeed;

	public SavedGame(Tile[][] tiles, ArrayList <Actor> actors,
			ArrayList <SolidActor> villagers, ArrayList <Fencer> raiders,
			GlobalVariables globals,
			Player player, VillageDance dance, double timeSpeed) {
		this.tiles = tiles;
		this.actors = actors;
		this.villagers = villagers;
		this.raiders = raiders;
		this.globals = globals;
		this.player = player;
		this.dance = dance;
		this.timeSpeed = timeSpeed;
	}
}
