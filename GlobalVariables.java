import java.io.Serializable;

/**
 * This class stores global variables (primitives).
 * Putting them in one place makes serialization easier.
 */
public class GlobalVariables implements Serializable{

	//Whether you can fight
	public boolean haveSword;
	
	//Affects old man's speech
	public boolean monsterDead;
	
	//Affects game messages regarding shards
	public boolean knowAboutGoal;
	
	//Affects junkie and dealer sister
	public int junkieState;
	
	private static final long serialVersionUID = 1L;
	
	public GlobalVariables() {
		haveSword = false;
		monsterDead = false;
		knowAboutGoal = false;
		junkieState = Junkie.UNAWARE;
	}

}
