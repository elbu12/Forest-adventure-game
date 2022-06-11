import java.awt.*;
import java.awt.image.*;

/**
 * You can dance with your lover. If so, you will temporarily lose the ability
 * to act as the game takes over.
 * 
 * Afterward, you can make out with Lover. This causes the raiders to arrive.
 **/
public class Lover extends Navigator {

	private static final long serialVersionUID = 1L;

	transient BufferedImage image = null;

	private boolean beginDance = false;
	private boolean summonRaiders = false;
	final private static int KISS = -96;
	final private Player player = Game.player;

	final private Game.DialogButton goSomePlacePrivate = new Game.DialogButton(
			"'Shall we go some place private to talk?'") {
		public void press() {
			Game.getResponse("Alex smiles.");
			action = MOVE;
			tx = wx + (ww * 0.75);
			ty = wy - (height * 0.51);
		}
	};

	public Lover(double x, double y, double direction, double tx, double ty, double wx, double wy, double ww,
			double wh) {
		super(1, 1);
		place(x, y);
		this.direction = direction;
		behavior = STAND_UNTIL_DETECT_THEN_FLEE;
		intention = STAND;
		action = STAND;
		state = 0;
		fastSpeed = 0.15;
		slowSpeed = 0.1;
		speed = slowSpeed;
		setDetectionRange(6);
		// here, tx and ty represent the starting position of Lover for the dance.
		// Later, it will become Lover's position behind the house
		this.tx = tx;
		this.ty = ty;
		// here, wx/wy/ww/wh refer to the northwest corner and dimensions of the house
		// by which Lover stands. Note that (wx,wy) is the northwest corner and not the
		// center!
		this.wx = wx;
		this.wy = wy;
		this.ww = ww;
		this.wh = wh;
		pathFindingIterations = 160;
	}

	public void interact() {
		if (intention == STAND) {
			if (!beginDance) {
				// intention is STAND either at the very beginning, or when the
				// raiders arrive
				Game.getResponse("You meet a beautiful stranger named Alex.",
						new Game.DialogButton("Ask Alex to dance") {
							public void press() {
								dance();
							}
						}, new Game.DialogButton("Ok"));
			} else if (summonRaiders) {
				Game.getResponse("'It's the raiders! Run!'");
			}
		} else if (intention == KISS) {
			if (action == STAND) {
				// another chance to go some place private
				Game.getResponse("You and Alex talk for some time.", goSomePlacePrivate, new Game.DialogButton("Ok"));
			} else if (action == KISS) {
				Game.getResponse("You and Alex talk a bit, then kiss."
						+ "\n\nThen you hear screaming coming from the village.\n\nThe music stops.");
				// set intention to STAND so lover will flee
				intention = STAND;
				// summon the raiders!
				summonRaiders = true;
				for (Solid r : enemies) {
					r.interact();
				}
			}
		}
	}

	public void hit(int damage) {
		if (damage == 0) {
			Game.getResponse("Do not attack this person!");
		} else {
			super.hit(damage);
		}
	}

	public void dance() {
		// called when you ask Alex to dance
		// begins the dance process
		intention = DANCE;
		action = DANCE;
		// While dancing, you can't act normally. The game takes over
		player.acts = false;
	}

	public ScaledImage draw() {
		if (image == null) {
			image = Fencer.getBallAndStick(new Color(120, 32, 32))[0];
		}
		return ScaledImage.get(image);
	}

	public void act() {
		if (summonRaiders && dieWhenFar(14)) {
			// after the raiders appear, Lover disappears when too far from you
		} else if (action == DANCE) {
			// determine where people should be
			double lx = tx + Game.dance.getx(true); // Lover x position
			double ly = ty + Game.dance.gety(true); // Lover y position
			double px = tx + Game.dance.getx(false); // player x position
			double py = ty + 1.1 + Game.dance.gety(false); // Player y position
			// move Lover
			navigateToward(lx, ly);
			// move the player
			// They may have to move around the house to avoid being stuck beside it
			if (player.x > wx - (player.width * 0.5)) {
				player.moveToward(wx - (player.width * 0.5), wy - (player.height * 0.5));
			} else {
				player.moveToward(px, py);
			}
			// also, everyone has to face appropriately
			if (x == lx && y == ly && player.x == px && player.y == py) {
				// if everyone is dancing, face appropriately
				direction = Game.dance.getDirection(true);
				player.direction = Game.dance.getDirection(false);
			} else {
				// otherwise, each just faces the other
				direction = Angle.get(player.x - x, player.y - y);
				player.direction = Angle.get(x - player.x, y - player.y);
			}
			if (Game.dance.i == 0) {
				if (beginDance) {
					// already did a full cycle; end of dance
					action = STAND;
					intention = KISS;
					// restore player's ability to act.
					// This must happen, even if Lover somehow dies during the dance!
					player.acts = true;
					Game.getResponse("You bow.");
				} else {
					// beginning of a full dance cycle
					beginDance = true;
				}
			}
		} else if (intention == KISS) {
			if (action == MOVE) {
				// move to private place
				navigateToward(tx, ty);
				if (x != nextNode[0] || y != nextNode[1]) {
					direction = Angle.get(nextNode[0] - x, nextNode[1] - y);
				}
				if (x == tx && y == ty) {
					// now wait for the kiss
					action = KISS;
				}
			} else {
				// face player when standing
				direction = Angle.get(player.x - x, player.y - y);
			}
		} else {
			super.act();
		}
	}
}