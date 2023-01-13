
//This is the file that is opened to run the game. It handles everything.
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.awt.geom.*;

/**
 * 
 * THIS WAS WRITTEN BY ELLIOT BURKART!
 * 
 * NEXT STEPS:
 * 
 * Fallen trees!
 * 
 * What if you see people working, like foraging or hunting?
 * 
 * CODE QUESTION: Pathfinding was not working. Stalkers would attempt to
 * navigate toward their starting locations if out of range. Then I changed it,
 * such that they now only try to navigate toward that point if they are not
 * already there. That fixed it. Why?
 * 
 * Should the northeast building in the abandoned city be a tower, with a second
 * level?
 * 
 * Include a "map" to show where things are?
 * 
 * What if someone has gray hair?
 * 
 * Should timeSpeed be changed to an int? Or a boolean?
 * 
 * Do something that brings back Alex
 * 
 * Abandoned city can have beds and tables?
 * 
 * Should you start at the festival?
 * 
 * Add some flowers somewhere
 * 
 * Use trails to guide, but not force, actions
 * 
 * why is initializing a textArea so slow? Do I need to hide the initialization
 * in a different thread?
 * 
 * Add rubble, tables, benches, other spider objects
 * 
 * Thoughts on pathfinding:
 * 
 * Perhaps, when pathfinding toward a target solid, rather than removing the
 * solid and pathfinding toward its center, keep the solid and track when an
 * intersection occurs with it. Note: tried this and it led to freezing?
 * 
 * How well should enemies coordinate to avoid one blocking another?
 * 
 * Should pathFindingIterations decrease as the number of navigators navigating
 * increases?
 * 
 * Some tiles get up to 23 nodes. Should this be decreased for optimization?
 * Perhaps circles' nodes should be decreased in certain cases?
 **/

public class Game implements ActionListener, KeyListener {
	// worldWidth and Height are the number of tiles across the world
	// change them to resize the world
	static final int worldWidth = 180;
	static final int worldHeight = 180;

	// screenWidth and Height are the number of tiles visible on the screen
	static final int screenWidth = 13;
	static final int screenHeight = 13;

	// frameWidth and Height are the pixel dimensions of the Game's main screen
	// (excluding the health bar and compass)
	// If they are set to (650,650), it seems to cause problems with the space
	// between tiles being visible (as a black line)
	static final int frameWidth = 649;
	static final int frameHeight = 649;
	static final double frameWidth075 = frameWidth * 0.75;
	static final double frameHeight075 = frameHeight * 0.75;

	// tileWidth and Height are the dimensions, in pixels, of a single tile
	static final int tileWidth = frameWidth / screenWidth;
	static final int tileHeight = frameHeight / screenHeight;

	// the width of the health bar
	static final int healthBarWidth = 40;
	// the height of the health bar
	static final int healthBarHeight = frameHeight - healthBarWidth;

	// drawDelay is the delay between drawing frames
	static final int drawDelay = 34;

	static javax.swing.Timer timer = null;

	// the "radius" infinitesimal points are assumed to have, to account
	// for rounding error in collision detection tests
	static final double pointRadius = 0.0001;

	// timeSpeed is the rate at which time passes
	// decrease it to "slow down" time
	// Realistically, it should be 0.5 or 1
	// -1 means it has not been set
	public static double timeSpeed = -1;

	// actors contains Actor objects to be called every game iteration
	static ArrayList<Actor> actors;
	// resetters is a list of objects, to be called to "reset" after
	// the game draws its screen. After calling each resetter, the
	// list is cleared.
	static ArrayList<Resetter> resetters = new ArrayList<Resetter>(10);

	public static Tile[][] tiles;

	// This is used when collecting Solids to draw
	static ArrayList<Solid> relevantSolids = new ArrayList<Solid>(20);

	// This is you!
	public static Player player = null;
	
	public static GlobalVariables globals = null;

	// Used for dancers in the village
	public static VillageDance dance;

	// temporary code for testing!
	static boolean narration = false;

	// Setting up the graphics stuff
	static JFrame frame = new JFrame("Forest Game");
	static JPanel panel = new JPanel() {
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(visibleImage, 0, 0, null);
			// draw the health bar
			g.setColor(Color.RED);
			g.fillRect(frameWidth, healthBarHeight * (Player.maxHealth - player.health) / Player.maxHealth,
					healthBarWidth,
					healthBarHeight - healthBarHeight * (Player.maxHealth - player.health) / Player.maxHealth);
			// draw the compass
			g.drawImage(compass1, frameWidth, healthBarHeight, null);
		}
	};

	// setting up the dialog window stuff
	static JDialog dialog;
	private static GridBagConstraints gbc = new GridBagConstraints();
	private static JTextArea dialogText;
	
	//For tutorial dialog
	private static DialogButton initialButton = null;

	static double[][] circleCorners; // treats a circle like a polygon

	public Game(boolean tutorialFirst) {
		initialize();

		// initialize tiles
		tiles = new Tile[worldWidth][worldHeight];

		actors = new ArrayList<Actor>(10);

		// This tells dancers where to go
		dance = new VillageDance();
		actors.add(dance);

		// This is for river animation
		actors.add(RiverAnimator.get());

		/**
		 * CREATING THE MAP: All tiles are initially made as empty "undesignated" tiles.
		 * Later, mapmaking methods will fill them in with the appropriate content.
		 **/
		for (int i = 0; i < worldWidth; i++) {
			for (int j = 0; j < worldHeight; j++) {
				tiles[i][j] = new Tile(i, j);
			}
		}

		if (tutorialFirst) {
			playTutorial(gbc);
		} else {
			makeRegularGame();
		}
	}

	public Game(SavedGame savedGame) {
		// Start from a save file
		tiles = savedGame.tiles;
		actors = savedGame.actors;
		FestivalRaider.actors = actors;
		FestivalRaider.villagers = savedGame.villagers;
		FestivalRaider.raiders = savedGame.raiders;
		globals = savedGame.globals;
		player = savedGame.player;
		dance = savedGame.dance;
		timeSpeed = savedGame.timeSpeed;
		initialize();
		//Solids need to be replaced into their tiles
		for (Tile[] t : tiles) {
			for (Tile tile : t) {
				for (Solid solid : tile.solids) {
					if (solid.tiles == null) {
						solid.tiles = new ArrayList <Tile> ();
					}
					solid.tiles.add(tile);
				}
			}
		}
		timer = new javax.swing.Timer(drawDelay, this);
		getResponse("Game loaded successfully.");
	}

	private void initialize() {
		globals = new GlobalVariables();
		// images for graphics
		visibleImage = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_RGB);
		invisibleImage = new BufferedImage(frameWidth * 3 / 2, frameHeight * 3 / 2, BufferedImage.TYPE_INT_RGB);
		// the image of the compass, after rotation
		compass1 = new BufferedImage(healthBarWidth, healthBarWidth, BufferedImage.TYPE_INT_ARGB);
		// dialog window
		dialog = new JDialog(frame) {
			public void setVisible(boolean b) {
				if (!b) {
					// Focus always goes back to the main frame, not another dialog window
					frame.requestFocus();
				}
				super.setVisible(b);
			}
		};
		dialog.setLayout(new GridBagLayout());
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// This JTextArea takes forever to load. Why?
		dialogText = new JTextArea(" ");
		dialogText.setEditable(false);
		dialogText.setOpaque(false);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridheight = 1;
		gbc.insets.bottom = 10;
		gbc.insets.top = 10;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		dialog.add(dialogText, gbc);
		gbc.insets.bottom = 0;
		gbc.insets.top = 0;
		gbc.insets.left = 0;
		gbc.insets.right = 0;

		// make the main game frame
		frame.setSize(frameWidth + healthBarWidth, 25 + frameHeight);
		frame.add(panel);
		frame.addKeyListener(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		// set up angle measurement
		Angle.setUp();

		// This is used for pathfinding
		// Note that the polygon must be expanded to fit around the circle
		circleCorners = new double[16][2];
		double polygonRadius = 1.0 / Math.cos(Math.PI * 0.25 * (circleCorners.length - 2) / circleCorners.length);
		for (int i = 0; i < circleCorners.length; i++) {
			circleCorners[i][0] = Math.cos(Math.PI * 2 * i / circleCorners.length) * polygonRadius;
			circleCorners[i][1] = Math.sin(Math.PI * 2 * i / circleCorners.length) * polygonRadius;
		}

		// These EditableIntArrays represent nodes (used for pathfinding) within
		// individual tiles
		for (int i = 0; i < worldWidth; i++) {
			for (int j = 0; j < worldHeight; j++) {
				tileNodes[i][j] = new EditableIntArray(10);
			}
		}
	}
	
	//When the player performs the designated action, a tutorial event 
	//is fired to tell the initial button to enable
	public static void tutorialEvent(int i) {
		if (tutorialState == null) {
			return;
		}
		if (i == tutorialState[0]) {
			initialButton.setEnabled(true);
		}
	}
	private static int[] tutorialState = null;

	public void playTutorial(GridBagConstraints gbc) {
		// Creates the tutorial. The initialWindow/TextArea are reused for instructions.
		// now set any undesignated tiles to forest floor
		player = new Player();
		timeSpeed = 1;
		final JDialog initialDialog = new JDialog(frame);
		initialDialog.setLayout(new GridBagLayout());
		initialDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		JTextArea initialTextArea = new JTextArea(
				"You are the person in the center.\n(It's an overhead view)\nMove with the arrow keys.");
		initialTextArea.setEditable(false);
		initialDialog.pack();
		initialDialog.setLocation(frame.getX() + (frame.getWidth() - initialDialog.getWidth()) / 3,
				frame.getY() + 3 * (frame.getHeight() - initialDialog.getHeight()) / 4);
		gbc.gridx = 0;
		gbc.gridy = 0;
		initialDialog.add(initialTextArea, gbc);
		initialDialog.setVisible(true);
		for (int i = 0; i < worldWidth; i++) {
			for (int j = 0; j < worldHeight; j++) {
				if (tiles[i][j].type == Tile.UNDESIGNATED) {
					tiles[i][j].setType(Tile.FOREST_FLOOR);
				}
			}
		}
		player.moveTo(worldWidth / 2, worldHeight / 2, getIntersectingTiles(player, worldWidth / 2, worldHeight / 2));
		tutorialState = new int[] { 0 };
		initialButton = new DialogButton("Next") {
			Monster tutorialMonster = new TutorialMonster(player, tutorialState);
			Fencer tutorialFencer = new TutorialFencer(player, tutorialState);

			public void press() {
				tutorialState[0]++;
				frame.requestFocus();
				switch (tutorialState[0]) {
				case 1:
					initialTextArea.setText("Use 'd' and 'g' keys to turn. Use 'p' to pause or save.");
					initialButton.setEnabled(false);
					break;
				case 2:
					initialTextArea.setText(
							"When not in combat mode, press space to interact with\nobjects and people. Try interacting with this monster.");

					initialButton.setEnabled(false);
					double tempy = (player.y < 10 ? player.y + 5 : player.y - 5);
					tutorialMonster.place(player.x, tempy);
					tutorialMonster.behavior = Navigator.STAND;
					break;
				case 3:
					globals.haveSword = true;
					initialTextArea.setText(
							"When you have the sword, press 'c' to enter combat mode.\nYou move faster in combat mode, but time slows down.");
					initialButton.setEnabled(false);
					break;
				case 4:
					initialTextArea.setText(
							"Now the monster will attack you. If you are hit and\nhave full health, your health bar on the right will diminish.\nNormally, if you are hit and do not have full health, you die.\nPress space while in combat mode to attack.");
					tutorialMonster.behavior = Navigator.STAND_UNTIL_DETECT_THEN_ATTACK;
					tutorialMonster.intention = SolidActor.ATTACK;
					initialButton.setEnabled(false);
					break;
				case 5:
					initialTextArea.setText(
							"Here is another person. Try talking to them.\nRemember to press 'c' to toggle combat mode.");
					initialButton.setEnabled(false);
					tutorialMonster.die();
					tutorialFencer.behavior = Navigator.STAND;
					tempy = (player.y < 10 ? player.y + 5 : player.y - 5);
					tutorialFencer.place(player.x, tempy);
					break;
				case 6:
					initialTextArea.setText("Now attack this person.");
					initialButton.setEnabled(false);
					break;
				case 7:
					initialTextArea.setText(
							"Now this person will attack you. When they attack,\nif you are in combat mode, press 'r' to parry and\nriposte. You must face them and time it correctly!");
					tutorialFencer.behavior = Navigator.STAND_UNTIL_DETECT_THEN_ATTACK;
					tutorialFencer.intention = SolidActor.ATTACK;
					tutorialFencer.fightingStyle = SolidActor.IMMEDIATE_ATTACK;
					break;
				case 8:
					initialTextArea.setText(
							"Now this person will try to parry and riposte your attacks.\nPress 'f' to feint, drawing their parry, then press space to\nattack while they are still trying to parry your feint.");
					tutorialFencer.fightingStyle = SolidActor.COUNTERATTACK;
					break;
				case 9:
					initialTextArea.setText(
							"Now this person will try to draw your parry by feinting.\nDo not take the bait. Instead, attack immediately!");
					tutorialFencer.fightingStyle = SolidActor.FEINT_THEN_ATTACK;
					break;
				case 10:
					initialTextArea.setText(
							"Now this person's fighting style will be randomized each time they respawn.\nBack up and draw them in to see what they do. Based on that, determine\ntheir fighting style, draw another another action, and respond accordingly.\nThis is how most human enemies must be fought. Observe your enemy.");
					tutorialFencer.die();
					break;
				case 11:
					initialTextArea.setText(
							"This concludes the tutorial. If you are ready,\npress the button to begin the game.\nRemember you will start without the sword!");
					setText("Begin game");
					break;
				case 12:
					tutorialFencer.die();
					for (int i = 0; i < worldWidth; i++) {
						for (int j = 0; j < worldHeight; j++) {
							tiles[i][j].setType(Tile.UNDESIGNATED);
						}
					}
					initialDialog.remove(this);
					initialDialog.setVisible(false);	//do we need to repaint to fully remove this?
					initialDialog.dispose();
					globals.haveSword = false;
					player.health = Player.maxHealth;
					if (player.intention == SolidActor.ATTACK) {
						toggleCombatMode();
					}
					timer.stop();
					makeRegularGame();
					break;
				}
				initialDialog.pack();
			}
		};
		// Prevent the initialDialog from receiving focus. Otherwise, pressing space can
		// "press" the button, instead of having an in-game effect
		initialButton.setFocusable(false);
		gbc.gridy++;
		//initialButton is only enabled when the user has performed the relevant action.
		initialButton.setEnabled(false);
		initialDialog.add(initialButton);
		initialDialog.pack();
		frame.requestFocus();
		timer = new javax.swing.Timer(drawDelay, this);
		timer.start();
	}

	public void makeRegularGame() {
		//initialButton only for tutorial
		initialButton = null;
		/**
		 * Make a 10-tile thick perimeter around the map. 5 blank tiles, then 5 of
		 * forest floor, all full of trees.
		 **/
		if (player == null) {
			player = new Player();
		}
		if (timeSpeed < 0) {
			timeSpeed = 1;
		}
		setTileType(0, 0, worldWidth, worldHeight, Tile.BLANK);
		setTileType(5, 5, worldWidth - 10, worldHeight - 10, Tile.FOREST_FLOOR);

		// water and bridge above beginning area
		for (int i = 5; i < worldWidth - 5; i++) {
			tiles[i][149].setType(i == 15 ? Tile.BRIDGE : Tile.RIVER);
			tiles[i][148].setType(i == 15 ? Tile.BRIDGE : Tile.RIVER);
		}

		// the tiles north of the bridge should have no tree
		tiles[14][147].canHaveTree = false;
		tiles[15][147].canHaveTree = false;
		tiles[16][147].canHaveTree = false;

		fillWithTrees(0, 0, 10, worldHeight, 0);
		fillWithTrees(worldWidth - 10, 0, 10, worldHeight, 0);
		fillWithTrees(0, 0, worldWidth, 10, 0);
		fillWithTrees(0, worldHeight - 10, worldWidth, 10, 0);

		// Now make the "beginning area"
		makeInitial8(10, 30, 40, 55, 70, 90, 110, 125, 150, 20);

		/**
		 * The walled village. The old woman refers to this as being to the northwest,
		 * so if this is moved, the woman's text must be updated.
		 **/
		makeWalledVillage(15, 15);

		// the dealer/junkie interaction:
		Junkie junkie = makeJunkieCamp(79, 30);
		makeDealerSister(20, 117, junkie);

		// add the festival and raiders (location to be changed!)
		makeFestival(80, 100);

		// add the castle
		makeCastle(120, 20);

		// make stalking area
		makeStalkingArea(120, 80);

		// now set any undesignated tiles to forest floor
		for (int i = 0; i < worldWidth; i++) {
			for (int j = 0; j < worldHeight; j++) {
				if (tiles[i][j].type == Tile.UNDESIGNATED) {
					tiles[i][j].setType(Tile.FOREST_FLOOR);
				}
			}
		}

		// make some bandits
		makeFencers(3, 90, 80, 4, 4);

		// now add trees
		fillWithTrees(10, 10, worldWidth - 20, 140, 2.5);

		if (timer == null) {
			timer = new javax.swing.Timer(drawDelay, this);
		}
		timer.start();
	}

	public static void makePermanentMessage(String title, String message) {
		JFrame jf = new JFrame(title);
		JTextArea jta = new JTextArea(message);
		jf.add(jta);
		jta.setEditable(false);
		jta.setOpaque(false);
		jf.pack();
		jf.setLocation(frame.getX() + (frame.getWidth() - jf.getWidth()) / 2,
				frame.getY() + (frame.getHeight() - jf.getHeight()) / 2);
		jf.setVisible(true);
	}

	// Buttons that appear in the dialog window
	static class DialogButton extends JButton implements ActionListener {
		static boolean restartGameOnClose;

		public DialogButton(String s) {
			super(s);
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			dialog.setVisible(false);
			// release any keys registered as pressed
			for (int i = 0; i < keys.length; i++) {
				keys[i] = false;
			}
			if (restartGameOnClose) {
				timer.start();
			}
			press();

		}

		public void press() {
		}
	}

	// default "Ok" button
	// is an array because it replaces an empty DialogButton array in getResponse
	final static DialogButton[] ok = { new DialogButton("Ok") };
	// Button for pausing
	final static DialogButton resumeButton = new DialogButton("Resume") {
		public void press() {
		}
	};
	final static DialogButton saveButton = new DialogButton("Save") {
		public void press() {
			timer.stop();
			// Designate where to save file
			JFileChooser chooser = new JFileChooser();
			int chooserState = chooser.showSaveDialog(frame);
			if (chooserState == JFileChooser.APPROVE_OPTION) {
				File target = chooser.getSelectedFile();
				// Create a 'save' object
				SavedGame saved = new SavedGame(tiles, actors,
						FestivalRaider.villagers, FestivalRaider.raiders,
						globals, player, dance, timeSpeed);
				try {
					FileOutputStream fileOut = new FileOutputStream(target);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(saved);
					out.close();
					fileOut.close();
					getResponse("Game saved successfully.");
				} catch (IOException i) {
					getResponse("Failed to save.");
					if (narration) {
						i.printStackTrace();
					}
				}
			}
		}
	};
	// collection of DialogButtons currently in dialog window
	// gets cleared out with each new dialog window call
	static ArrayList<DialogButton> dialogButtons = new ArrayList<DialogButton>(4);

	/**
	 * Methods for getting responses with a dialog window. getResponse(s) will pause
	 * the game to ask for a response. getResponse will restart the game timer
	 * afterward. getResponses will not. Use it for multiple consecutive messages.
	 * 
	 * To make a pop-up dialog window, call getResponse(s) with a String (message)
	 * and some amount of DialogButtons. The String is the text to display. The
	 * buttons are the buttons to be available as options. If no button is given, a
	 * default "Ok" button will be used.
	 * 
	 * Each button should have a "press" method to be called when it is pressed. The
	 * recommended way to do this is with an anonymous class overwriting the press
	 * method, like so:
	 * 
	 * getResponse("Message goes here", new DialogButton("Button text"){public void
	 * press(){doSomething();}}, new DialogButton("Different text"){public void
	 * press(){doSomethingElse();}} );
	 * 
	 * Obviously, this is a prime opportunity for lambda expressions
	 **/
	public static void getResponse(String message, DialogButton... options) {
		getResponses(message, options);
		DialogButton.restartGameOnClose = true;
	}

	public static void getResponses(String message, DialogButton... options) {
		// pause the game
		timer.stop();
		// Reset key presses
		for (int i = 0; i < keys.length; i++) {
			keys[i] = false;
		}
		DialogButton.restartGameOnClose = false;
		// if no option buttons given, use a default "Ok"
		if (options.length == 0) {
			options = ok;
		}
		// set text message
		dialogText.setText(message);
		// remove old buttons
		for (DialogButton b : dialogButtons) {
			dialog.remove(b);
		}
		dialogButtons.clear();

		// add new buttons:
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		for (DialogButton b : options) {
			dialog.add(b, gbc);
			gbc.gridx++;
			dialogButtons.add(b);
		}
		dialog.pack();
		dialog.setLocation(frame.getLocation().x + (frame.getWidth() / 2) - (dialog.getWidth() / 2),
				frame.getLocation().y + (frame.getHeight() / 2) - dialog.getHeight() / 2);
		dialogButtons.get(0).requestFocusInWindow();
		dialog.setVisible(true);
		dialog.toFront();

		// at this point, the user will probably pause to consider the situation
		// Use this opportunity to clear out old buttons which are no longer used
		System.gc();
	}

	// These BufferedImages are drawn to the screen
	static BufferedImage visibleImage;
	static BufferedImage invisibleImage;
	static BufferedImage compass1;

	// The AffineTransform used to draw
	static AffineTransform imageMover = new AffineTransform();

	// shapes, for the purpose of collision detection
	public final static int CIRCLE = 2;
	public final static int RECTANGLE = 3;
	public final static int POINT = 1;
	public final static int NULL = 0;

	/**
	 * Collision detection algorithms: Remember, when relevant, to check for
	 * collisions with a Solid's intended destination, NOT its current location!
	 **/
	public static boolean rectangleIntersectRectangle(double x1, double y1, double w1, double h1, double x2, double y2,
			double w2, double h2) {
		double dx = x1 - x2;
		if (dx < 0) {
			dx = -dx;
		}
		double dy = y1 - y2;
		if (dy < 0) {
			dy = -dy;
		}
		return (dx < (w1 + w2) * 0.5 && dy < (h1 + h2) * 0.5);
	}

	public static boolean circleIntersectPoint(double x1, double y1, double r, double x2, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		return ((dx * dx) + (dy * dy) < r * r);
	}

	public static boolean circleIntersectCircle(double x1, double y1, double r1, double x2, double y2, double r2) {
		return circleIntersectPoint(x1, y1, (r1 + r2), x2, y2);
	}

	public static boolean circleIntersectRectangle(double x1, double y1, double r, double x2, double y2, double w,
			double h) {
		if (y1 < y2 - (h * 0.5)) {
			// circle center above top edge of rectangle
			if (x1 < x2 - (w * 0.5)) {
				// circle in top-left corner area
				return circleIntersectPoint(x1, y1, r, x2 - (w * 0.5), y2 - (h * 0.5));
			} else if (x1 < x2 + (w * 0.5)) {
				// circle center above top edge of rectangle
				return (y2 - y1 < r + (h * 0.5));
			} else {
				// circle in top-right corner area
				return circleIntersectPoint(x1, y1, r, x2 + (w * 0.5), y2 - (h * 0.5));
			}
		} else if (y1 < y2 + (h * 0.5)) {
			// circle between top and bottom edges of rectangle
			double dx = x2 - x1;
			if (dx < 0) {
				dx = -dx;
			}
			return (dx < r + (w * 0.5));
		} else {
			// circle below bottom edge of rectangle
			if (x1 < x2 - (w * 0.5)) {
				// circle in bottom-left corner area
				return circleIntersectPoint(x1, y1, r, x2 - (w * 0.5), y2 + (h * 0.5));
			} else if (x1 < x2 + (w * 0.5)) {
				// circle center below bottom edge of rectangle
				return (y1 - y2 < r + (h * 0.5));
			} else {
				// circle in bottom-right corner area
				return circleIntersectPoint(x1, y1, r, x2 + (w * 0.5), y2 + (h * 0.5));
			}
		}
	}

	public static boolean intersect(double x1, double y1, double w1, double h1, int s1, double x2, double y2, double w2,
			double h2, int s2) {
		if (s1 == Game.RECTANGLE) {
			if (s2 == Game.RECTANGLE) {
				return rectangleIntersectRectangle(x1, y1, w1, h1, x2, y2, w2, h2);
			} else if (s2 == Game.CIRCLE) {
				return circleIntersectRectangle(x2, y2, w2 * 0.5, x1, y1, w1, h1);
			} else if (s2 == Game.POINT) {
				return (x2 > x1 - (w1 * 0.5) && x2 < x1 + (w1 * 0.5) && y2 > y1 - (h1 * 0.5) && y2 < y1 + (h1 * 0.5));
			}
		} else if (s1 == Game.CIRCLE) {
			if (s2 == Game.RECTANGLE) {
				return circleIntersectRectangle(x1, y1, w1 * 0.5, x2, y2, w2, h2);
			} else if (s2 == Game.CIRCLE) {
				return circleIntersectCircle(x1, y1, w1 * 0.5, x2, y2, w2 * 0.5);
			} else if (s2 == Game.POINT) {
				double dx = x1 - x2;
				double dy = y1 - y2;
				return ((dx * dx) + (dy * dy) < 0.25 * w1 * w1);
			}
		} else if (s1 == Game.POINT) {
			if (s2 == Game.RECTANGLE) {
				return circleIntersectRectangle(x1, y1, 0, x2, y2, w2, h2);
			} else if (s2 == Game.CIRCLE) {
				return circleIntersectCircle(x1, y1, 0, x2, y2, w2 * 0.5);
			} else if (s2 == Game.POINT) {
				double dx = x1 - x2;
				double dy = y1 - y2;
				return ((dx * dx) + (dy * dy) < 0.25 * pointRadius * pointRadius);
			}
		}
		// else, assume s1 is empty
		return false;
	}

	// intersect with two Solids and two x/y coordinate pairs will check if
	// Solid s1 would intersect Solid s2 while moving from (x0,y0) to (x0+dx,y0+dy)
	public static boolean intersect(Solid s1, double x0, double y0, double dx, double dy, Solid s2) {
		if (s1.shape == Game.CIRCLE) {
			if (s2.shape == Game.CIRCLE) {
				// note that order of s1 and s2 are reversed because s1 is the moving solid,
				// but the method requires the line segment (the moving part) come second
				return circleIntersectLineSegment(s2.x, s2.y, 0.5 * (s1.width + s2.width), x0, y0, dx, dy);
			} else if (s2.shape == Game.RECTANGLE) {
				/**
				 * basic idea: treat the problem as if the circle is a point (its center) and
				 * the rectangle is expanded, with rounded corners. Does this moving point (ie
				 * line segment) intersect this rounded rectangle?
				 * 
				 * The rounded rectangle is essentially two overlapping rectangles, forming a
				 * "+" sign, with circles in the corners
				 * 
				 * Thus it can be done using a combination of rectangle/line tests and
				 * circle/line tests
				 * 
				 * Also note that, if the object is moving up and left, it is unnecessary to
				 * test for intersection with the upper-left corner
				 **/
				if (dx > 0) {
					if (dy > 0) {
						return circleIntersectLineSegment(s2.x - (0.5 * s2.width), s2.y - (0.5 * s2.height),
								0.5 * s1.width, x0, y0, dx, dy)
								|| rectangleIntersectLineSegment(s2.x, s2.y, s1.width + s2.width, s2.height, x0, y0, dx,
										dy)
								|| rectangleIntersectLineSegment(s2.x, s2.y, s2.width, s1.height + s2.height, x0, y0,
										dx, dy)
								|| circleIntersectLineSegment(s2.x - (0.5 * s2.width), s2.y + (0.5 * s2.height),
										0.5 * s1.width, x0, y0, dx, dy)
								|| circleIntersectLineSegment(s2.x + (0.5 * s2.width), s2.y - (0.5 * s2.height),
										0.5 * s1.width, x0, y0, dx, dy);
					} else {
						return circleIntersectLineSegment(s2.x - (0.5 * s2.width), s2.y + (0.5 * s2.height),
								0.5 * s1.width, x0, y0, dx, dy)
								|| rectangleIntersectLineSegment(s2.x, s2.y, s1.width + s2.width, s2.height, x0, y0, dx,
										dy)
								|| rectangleIntersectLineSegment(s2.x, s2.y, s2.width, s1.height + s2.height, x0, y0,
										dx, dy)
								|| circleIntersectLineSegment(s2.x - (0.5 * s2.width), s2.y - (0.5 * s2.height),
										0.5 * s1.width, x0, y0, dx, dy)
								|| circleIntersectLineSegment(s2.x + (0.5 * s2.width), s2.y + (0.5 * s2.height),
										0.5 * s1.width, x0, y0, dx, dy);
					}
				} else {
					if (dy > 0) {
						return circleIntersectLineSegment(s2.x + (0.5 * s2.width), s2.y - (0.5 * s2.height),
								0.5 * s1.width, x0, y0, dx, dy)
								|| rectangleIntersectLineSegment(s2.x, s2.y, s1.width + s2.width, s2.height, x0, y0, dx,
										dy)
								|| rectangleIntersectLineSegment(s2.x, s2.y, s2.width, s1.height + s2.height, x0, y0,
										dx, dy)
								|| circleIntersectLineSegment(s2.x + (0.5 * s2.width), s2.y + (0.5 * s2.height),
										0.5 * s1.width, x0, y0, dx, dy)
								|| circleIntersectLineSegment(s2.x - (0.5 * s2.width), s2.y - (0.5 * s2.height),
										0.5 * s1.width, x0, y0, dx, dy);
					} else {
						return circleIntersectLineSegment(s2.x + (0.5 * s2.width), s2.y + (0.5 * s2.height),
								0.5 * s1.width, x0, y0, dx, dy)
								|| rectangleIntersectLineSegment(s2.x, s2.y, s1.width + s2.width, s2.height, x0, y0, dx,
										dy)
								|| rectangleIntersectLineSegment(s2.x, s2.y, s2.width, s1.height + s2.height, x0, y0,
										dx, dy)
								|| circleIntersectLineSegment(s2.x - (0.5 * s2.width), s2.y + (0.5 * s2.height),
										0.5 * s1.width, x0, y0, dx, dy)
								|| circleIntersectLineSegment(s2.x + (0.5 * s2.width), s2.y - (0.5 * s2.height),
										0.5 * s1.width, x0, y0, dx, dy);
					}
				}
			} else if (s2.shape == Game.POINT) {
				// note that order of s1 and s2 are reversed because s1 is the moving solid,
				// but the method requires the line segment (the moving part) come second
				return circleIntersectLineSegment(s2.x, s2.y, 0.5 * (s1.width), x0, y0, dx, dy);
			}
		} else if (s1.shape == Game.RECTANGLE) {
			if (s2.shape == Game.CIRCLE) {
				/**
				 * Still use the circle/rectangle intersection test mentioned above, but adjust
				 * it The test above assumes the circle is moving. So swap the order of these,
				 * such that the circle moves. It moves in the opposite direction. Everything
				 * gets shifted over to put s1 at its own location, rather than (x1,x2)
				 **/
				return intersect(s2, s2.x + s1.x - x0, s2.y + s1.y - y0, s2.x + s1.x - x0 - dx, s2.y + s1.y - y0 - dy,
						s1);
			} else if (s2.shape == Game.RECTANGLE) {
				// note that order of s1 and s2 are reversed because s1 is the moving solid,
				// but the method requires the line segment (the moving part) come second
				return rectangleIntersectLineSegment(s2.x, s2.y, 0.5 * (s1.width + s2.width),
						0.5 * (s1.height + s2.height), x0, y0, dx, dy);
			} else if (s2.shape == Game.POINT) {
				// note that order of s1 and s2 are reversed because s1 is the moving solid,
				// but the method requires the line segment (the moving part) come second
				return rectangleIntersectLineSegment(s2.x, s2.y, 0.5 * (s1.width), 0.5 * (s1.height), x0, y0, dx, dy);
			}
		} else if (s1.shape == Game.POINT) {
			if (s2.shape == Game.CIRCLE) {
				return circleIntersectLineSegment(s1.x, s1.y, 0.5 * (s1.width), x0, y0, dx, dy);
			} else if (s2.shape == Game.RECTANGLE) {
				return rectangleIntersectLineSegment(s2.x, s2.y, 0.5 * (s1.width), 0.5 * (s1.height), x0, y0, dx, dy);
			} else if (s2.shape == Game.POINT) {
				// Here we determine if one point intersects another while traveling along a
				// line segment. Using circleIntersectLineSegment with a small radius to
				// account for rounding error
				return circleIntersectLineSegment(s2.x, s2.y, pointRadius, x0, y0, dx, dy);
			}
		}
		// else, assume a solid is empty
		return false;
	}

	// tests if circle with center (cx,cy) and radius r intersects line segment
	// spanning
	// from (lx,ly) to (lx+dx,ly+dy)
	public static boolean circleIntersectLineSegment(double cx, double cy, double r, double lx, double ly, double dx,
			double dy) {
		/**
		 * distance squared, from line segment to circle center, is: ( cx - (lx +
		 * kdx))^2 + ( cy - (ly + kdy))^2 derivative is: 2(cx - (lx + kdx))dx + 2(cy -
		 * (ly + kdy))dy when derivative is zero, line is closest to circle. This
		 * happens when: k = ( dx(cx-lx) + dy(cy-ly) )/(dx^2 + dy^2)
		 **/
		double k = ((dx * (cx - lx)) + (dy * (cy - ly))) / ((dx * dx) + (dy * dy));
		// (sx,sy) is the point on the line segment closest to (cx,cy)
		double sx;
		double sy;
		// is (lx+kdx,ly+kdy) on the line segment, ie is 0<=k<=1 ?
		if (k <= 0) {
			// no; line segment is closest at beginning. Check if beginning of line
			// segment is close enough to intersect circle
			sx = cx - lx;
			sy = cy - ly;
		} else if (k >= 1) {
			// no; line segment is closest at end. Check if end of line segment is
			// close enough to intersect circle
			sx = cx - (lx + dx);
			sy = cy - (ly + dy);
		} else {
			// yes; test how close this point actually is
			sx = cx - (lx + (k * dx));
			sy = cy - (ly + (k * dy));
		}
		// compare distance squared, rather than distance, to avoid square roots
		return ((sx * sx) + (sy * sy) < r * r);
	}

	// tests if a rectangle with center (rx,ry) and dimensions rw x rh intersects
	// line spanning from (lx,ly) to (lx+dx,ly+dy)
	public static boolean rectangleIntersectLineSegment(double rx, double ry, double rw, double rh, double lx,
			double ly, double dx, double dy) {
		// This algorithm basically checks for intersections with each edge
		// If the line segment is entirely within the rectangle, it will return false
		double leftEdge = rx - (rw * 0.5);
		double rightEdge = rx + (rw * 0.5);
		double topEdge = ry - (rh * 0.5);
		double bottomEdge = ry + (rh * 0.5);
		double minx = Math.min(lx, lx + dx);
		double maxx = Math.max(lx, lx + dx);
		double miny = Math.min(ly, ly + dy);
		double maxy = Math.max(ly, ly + dy);
		if (dx != 0) {
			// check left edge
			double e = ly + ((leftEdge - lx) * (dy / dx));
			if (minx <= leftEdge && maxx > leftEdge && e >= topEdge && e < bottomEdge) {
				return true;
			}
			// check right edge
			e = ly + ((rightEdge - lx) * (dy / dx));
			if (minx <= rightEdge && maxx > rightEdge && e >= topEdge && e < bottomEdge) {
				return true;
			}
		}
		if (dy != 0) {
			// check top edge
			double e = lx + ((topEdge - ly) * (dx / dy));
			if (miny <= topEdge && maxy > topEdge && e >= leftEdge && e < rightEdge) {
				return true;
			}
			// check bottom edge
			e = lx + ((bottomEdge - ly) * (dx / dy));
			if (miny <= bottomEdge && maxy > bottomEdge && e >= leftEdge && e < rightEdge) {
				return true;
			}
		}
		// no intersections!
		return false;
	}

	// solidIntersectLineSegment will test if a given line segment
	// intersects a given solid
	// the line segment spans from (lx,ly) to (lx+dx,ly+dy)
	public static boolean solidIntersectLineSegment(Solid s, double lx, double ly, double dx, double dy) {
		if (s.shape == Game.CIRCLE) {
			return circleIntersectLineSegment(s.x, s.y, s.width * 0.5, lx, ly, dx, dy);
		} else if (s.shape == Game.RECTANGLE) {
			return rectangleIntersectLineSegment(s.x, s.y, s.width, s.height, lx, ly, dx, dy);
		} else if (s.shape == Game.POINT) {
			// assume the solid is a point
			return circleIntersectLineSegment(s.x, s.y, pointRadius, lx, ly, dx, dy);
		}
		// else, assume s is empty
		return false;
	}

	/**
	 * This is the beginning of the "where" methods. They determine where an
	 * intersection occurs as an object moves along a given path.
	 * 
	 * They do not determine the distance this object could travel before
	 * intersecting. Rather, they find a coefficient, such that, the traveler's
	 * position, plus the vector of its movement, times this coefficient, is the
	 * point where the intersection occurs.
	 * 
	 * Eg, if object o moves from point p to point (p+v), and a "where" method
	 * determines an intersection occurs at i, then the actual point of the
	 * intersection is p + iv
	 * 
	 **/
	/**
	 * Determines where a point moving from (x0,y0) to (x0+dx,y0+dy) intersects a
	 * circle at (cx,cy) with radius r.
	 **/
	public static double wherePointIntersectCircle(double x0, double y0, double dx, double dy, double cx, double cy,
			double r) {
		/**
		 * Line is (x0,y0)+t(dx,dy) Circle is (x-cx)^2 + (y-cy)^2 = r^2 (x0+tdx-cx)^2 +
		 * (y0+tdy-cy)^2 = r^2 Let a := x0-cx Let b := y0-cy (tdx+a)^2 + (tdy+b)^2 = r^2
		 * (tdx)^2 + 2atdx + a^2 + (tdy)^2 + 2btdy + b^2 = r^2 (dx^2 + dy^2)t^2 + 2(adx
		 * + bdy)t + (a^2 + b^2 - r^2) = 0 Let i := (dx^2 + dy^2) Let j := 2(adx + bdy)
		 * Let k := (a^2 + b^2 - r^2) it^2 + jt + k = 0
		 **/
		double a = x0 - cx;
		double b = y0 - cy;
		double i = (dx * dx) + (dy * dy);
		double j = 2 * ((a * dx) + (b * dy));
		double k = (a * a) + (b * b) - (r * r);
		// Get solutions
		double root = Math.sqrt((j * j) - (4 * i * k));
		double t1 = (-j + root) / (2.0 * i);
		double t2 = (-j - root) / (2.0 * i);
		// Which one happens first?
		return Math.min(t1, t2);
	}

	// Determines where a circle with radius r1, moving from (x0,y0) to
	// (x0+dx,y0+dy)
	// intersects a circle at (cx,cy) with radius r2
	public static double whereCircleIntersectCircle(double r1, double x0, double y0, double dx, double dy, double cx,
			double cy, double r2) {
		return wherePointIntersectCircle(x0, y0, dx, dy, cx, cy, r1 + r2);
	}

	// Determines where a point, moving from (x0,y0) to (x0+dx,y0+dy), intersects a
	// point
	// at (px,py). Assume points have radius pointRadius
	public static double wherePointIntersectPoint(double x0, double y0, double dx, double dy, double px, double py) {
		return wherePointIntersectCircle(x0, y0, dx, dy, px, py, pointRadius);
	}

	// Determines where a point, moving from (x0,y0) to (x0+dx,y0+dy), intersects a
	// rectangle
	// with center (rx,ry) and (width,height) of (w,h)
	public static double wherePointIntersectRectangle(double x0, double y0, double dx, double dy, double rx, double ry,
			double w, double h) {
		double t0 = Double.POSITIVE_INFINITY;
		if (dx != 0) {
			/**
			 * Intersection occurs when x0+tdx = rx +/- (w*0.5) t = (rx - x0 +/- (w*0.5))/dx
			 **/
			double t1 = (rx - x0 + (w * 0.5)) / dx;
			double t2 = (rx - x0 - (w * 0.5)) / dx;
			// Do these intersections occur within the rectangle?
			if (y0 + (t1 * dy) >= ry - (h * 0.5) && y0 + (t1 * dy) < ry + (h * 0.5)) {
				t0 = t1;
			}
			if (y0 + (t2 * dy) >= ry - (h * 0.5) && y0 + (t2 * dy) < ry + (h * 0.5) && t2 < t0) {
				t0 = t2;
			}
		}
		if (dy != 0) {
			/**
			 * Intersection occurs when y0+tdy = ry +/- (h*0.5) t = (ry - y0 +/- (h*0.5))/dy
			 **/
			double t1 = (ry - y0 + (h * 0.5)) / dy;
			double t2 = (ry - y0 - (h * 0.5)) / dy;
			if (x0 + (t1 * dx) >= rx - (w * 0.5) && x0 + (t1 * dx) < rx + (w * 0.5) && t1 < t0) {
				t0 = t1;
			}
			if (x0 + (t2 * dx) >= rx - (w * 0.5) && x0 + (t2 * dx) < rx + (w * 0.5) && t2 < t0) {
				t0 = t2;
			}
		}
		// Now found the earliest intersection point. Return it
		return t0;
	}

	// Determines where a rectangle, with (width,height) of (w1,h1), moving from
	// (x0,y0) to
	// (x0+dx,y0+dy), intersects a rectangle with center (rx,ry) and (width,height)
	// (w2,h2)
	public static double whereRectangleIntersectRectangle(double w1, double h1, double x0, double y0, double dx,
			double dy, double rx, double ry, double w2, double h2) {
		return wherePointIntersectRectangle(x0, y0, dx, dy, rx, ry, w1 + w2, h1 + h2);
	}

	// Determines where a circle, with radius r, moving from (x0,y0) to
	// (x0+dx,y0+dy), intersects
	// a rectangle with center (rx,ry) and (width,height) of (w,h)
	public static double whereCircleIntersectRectangle(double r, double x0, double y0, double dx, double dy, double rx,
			double ry, double w, double h) {
		// This can be modeled as a point intersecting a rectangle with "padded" corners
		// first handle the "cross" of rectangles
		double t0 = Double.POSITIVE_INFINITY;
		if (rectangleIntersectLineSegment(rx, ry, w + r + r, h, x0, y0, dx, dy)) {
			double t = wherePointIntersectRectangle(x0, y0, dx, dy, rx, ry, w + r + r, h);
			if (t < t0) {
				t0 = t;
			}
		}
		if (rectangleIntersectLineSegment(rx, ry, w, h + r + r, x0, y0, dx, dy)) {
			double t = wherePointIntersectRectangle(x0, y0, dx, dy, rx, ry, w, h + r + r);
			if (t < t0) {
				t0 = t;
			}
		}
		// Now handle the circular "corners"
		if (circleIntersectLineSegment(rx - (w * 0.5), ry - (h * 0.5), r, x0, y0, dx, dy)) {
			double t = wherePointIntersectCircle(x0, y0, dx, dy, rx - (w * 0.5), ry - (h * 0.5), r);
			if (t < t0) {
				t0 = t;
			}
		}
		if (circleIntersectLineSegment(rx + (w * 0.5), ry - (h * 0.5), r, x0, y0, dx, dy)) {
			double t = wherePointIntersectCircle(x0, y0, dx, dy, rx + (w * 0.5), ry - (h * 0.5), r);
			if (t < t0) {
				t0 = t;
			}
		}
		if (circleIntersectLineSegment(rx - (w * 0.5), ry + (h * 0.5), r, x0, y0, dx, dy)) {
			double t = wherePointIntersectCircle(x0, y0, dx, dy, rx - (w * 0.5), ry + (h * 0.5), r);
			if (t < t0) {
				t0 = t;
			}
		}
		if (circleIntersectLineSegment(rx + (w * 0.5), ry + (h * 0.5), r, x0, y0, dx, dy)) {
			double t = wherePointIntersectCircle(x0, y0, dx, dy, rx + (w * 0.5), ry + (h * 0.5), r);
			if (t < t0) {
				t0 = t;
			}
		}
		return t0;
	}

	public static double whereRectangleIntersectCircle(double w, double h, double x0, double y0, double dx, double dy,
			double cx, double cy, double r) {
		return whereCircleIntersectRectangle(r, cx, cy, -dx, -dy, x0, y0, w, h);
	}

	// This determines where a point, moving from (x0,y0) to (x0+dx,y0+dy),
	// intersects solid s
	public static double wherePointIntersectSolid(double x0, double y0, double dx, double dy, Solid s) {
		if (s.shape == POINT) {
			return wherePointIntersectPoint(x0, y0, dx, dy, s.x, s.y);
		} else if (s.shape == CIRCLE) {
			return wherePointIntersectCircle(x0, y0, dx, dy, s.x, s.y, 0.5 * s.width);
		} else if (s.shape == RECTANGLE) {
			return wherePointIntersectRectangle(x0, y0, dx, dy, s.x, s.y, s.width, s.height);
		} else {
			return Double.POSITIVE_INFINITY;
		}
	}

	// This determines where solid s1, moving from (x0,y0) to (x0+dx,y0+dy),
	// intersects solid s2
	public static double whereSolidIntersectSolid(Solid s1, double x0, double y0, double dx, double dy, Solid s2) {
		if (s1.shape == POINT) {
			if (s2.shape == POINT) {
				return wherePointIntersectPoint(x0, y0, dx, dy, s2.x, s2.y);
			} else if (s2.shape == CIRCLE) {
				return wherePointIntersectCircle(x0, y0, dx, dy, s2.x, s2.y, 0.5 * s2.width);
			} else if (s2.shape == RECTANGLE) {
				return wherePointIntersectRectangle(x0, y0, dx, dy, s2.x, s2.y, s2.width, s2.height);
			} else {
				return Double.POSITIVE_INFINITY;
			}
		} else if (s1.shape == CIRCLE) {
			if (s2.shape == POINT) {
				return wherePointIntersectCircle(x0, y0, dx, dy, s2.x, s2.y, 0.5 * s1.width);
			} else if (s2.shape == CIRCLE) {
				return whereCircleIntersectCircle(0.5 * s1.width, x0, y0, dx, dy, s2.x, s2.y, 0.5 * s2.width);
			} else if (s2.shape == RECTANGLE) {
				return whereCircleIntersectRectangle(0.5 * s1.width, x0, y0, dx, dy, s2.x, s2.y, s2.width, s2.height);
			} else {
				return Double.POSITIVE_INFINITY;
			}
		} else if (s1.shape == RECTANGLE) {
			if (s2.shape == POINT) {
				return wherePointIntersectRectangle(x0, y0, dx, dy, s2.x, s2.y, s1.width, s1.height);
			} else if (s2.shape == CIRCLE) {
				return whereRectangleIntersectCircle(s1.width, s1.height, x0, y0, dx, dy, s2.x, s2.y, 0.5 * s2.width);
			} else if (s2.shape == RECTANGLE) {
				return whereRectangleIntersectRectangle(s1.width, s1.height, x0, y0, dx, dy, s2.x, s2.y, s2.width,
						s2.height);
			} else {
				return Double.POSITIVE_INFINITY;
			}
		} else {
			return Double.POSITIVE_INFINITY;
		}
	}

	// intersectingTiles is for determining which tiles intersect a solid
	static ArrayList<Tile> intersectingTiles = new ArrayList<Tile>(100);

	// addIntersectingTiles will add tiles (i,j), where j0 <= j < jmax
	// this version is used for the getIntersectingTiles version for lines (rather
	// than moving solids)
	public static void addIntersectingTiles(int i, int j0, double jmax) {
		if (i < 0 || i >= worldWidth) {
			return;
		}
		if (j0 < 0) {
			j0 = 0;
		}
		if (jmax > worldHeight) {
			jmax = worldHeight;
		}
		for (int j = j0; j < jmax; j++) {
			intersectingTiles.add(tiles[i][j]);
		}
	}

	// getIntersectingTiles will return intersectingTiles, which now contains the
	// tiles
	// intersecting a given solid. If no x/y coordinates are given, it will
	// look at the object's current location. If x/y coordinates are given, it will
	// look
	// at the given location
	public static ArrayList<Tile> getIntersectingTiles(Solid s, double x, double y) {
		int xstart = Math.max(0, (int) (x - (s.width * 0.5)));
		double xend = Math.min(worldWidth, x + (s.width * 0.5) + 1);
		int ystart = Math.max(0, (int) (y - (s.height * 0.5)));
		double yend = Math.min(worldHeight, y + (s.height * 0.5) + 1);
		intersectingTiles.clear();
		for (int i = xstart; i < xend; i++) {
			for (int j = ystart; j < yend; j++) {
				if (intersect(x, y, s.width, s.height, s.shape, 0.5 + (int) (i), 0.5 + (int) (j), 1, 1,
						Game.RECTANGLE)) {
					intersectingTiles.add(tiles[i][j]);
				}
			}
		}
		return intersectingTiles;
	}

	public static ArrayList<Tile> getIntersectingTiles(Solid s) {
		return getIntersectingTiles(s, s.x, s.y);
	}

	/**
	 * getIntersecting Tiles, with two x/y coordinate pairs, and a solid will find
	 * tiles intersecting the space through which s would travel, if moving from
	 * (x0,y0) to (x1,y1). Use it to determine if straight-line movement is
	 * possible.
	 **/
	public static ArrayList<Tile> getIntersectingTiles(Solid s, double x0, double y0, double x1, double y1) {
		if (s.shape == Game.POINT) {
			// s is a point; use method for infinitesimally thin line segments
			return getIntersectingTiles(x0, y0, x1, y1);
		}
		intersectingTiles.clear();
		if (s.shape == Game.NULL) {
			// s is empty; return no intersections.
			return intersectingTiles;
		}
		double sw = s.width * 0.5;
		double sh = s.height * 0.5;
		double dx = x1 - x0;
		double dy = y1 - y0;
		if ((int) (x0 - sw) == (int) (x1 - sw) && (int) (x0 + sw) == (int) (x1 + sw)) {
			// Barely any horizontal movement; treat it as strictly vertical
			int top = (int) (Math.max(y0 + sh, y1 + sh));
			int bottom = (int) (Math.min(y0 - sh, y1 - sh));
			for (int i = (int) (x0 - sw); i <= x0 + sw; i++) {
				addIntersectingTiles(bottom, top, i, true);
			}
			return intersectingTiles;
		} else if ((int) (y0 - sh) == (int) (y1 - sh) && (int) (y0 + sh) == (int) (y1 + sh)) {
			// Barely any vertical movement; treat it as strictly horizontal
			int right = (int) (Math.max(x0 + sw, x1 + sw));
			int left = (int) (Math.min(x0 - sw, x1 - sw));
			for (int i = (int) (y0 - sh); i <= y0 + sh; i++) {
				addIntersectingTiles(left, right, i, false);
			}
			return intersectingTiles;
		}
		/**
		 * If we made it this far, it means the movement is diagonal. The object
		 * (assumed to be rectangular) moves along a "thick line," but bounded with
		 * left, right, top, and bottom edges. The actual space spanned by a moving
		 * rectangle is hexagonal.
		 * 
		 * Intersecting tiles are determined by looking at the tiles intersected by two
		 * parallel lines, tracing the movement of opposite corners of the moving solid.
		 **/
		if (Math.abs(dx) > Math.abs(dy)) {
			// Movement is more horizontal
			// Assume it goes left-to-right
			if (x0 > x1) {
				// Swap labels if necessary
				double xTemp = x0;
				double yTemp = y0;
				x0 = x1;
				x1 = xTemp;
				y0 = y1;
				y1 = yTemp;
			}
			double slope = (y1 - y0) / (x1 - x0);
			double top = Math.min(sh + Math.max(y0, y1), worldHeight);
			double bottom = Math.max(Math.min(y0, y1) - sh, 0);
			int left = Math.max(0, (int) (x0 - sw));
			double right = Math.min(x1 + sw, worldWidth);
			// Find the "offset" values for the two parallel lines bounding this hexagon
			// ie, represent each as y = (slope)x + (offset)
			// designate one line as the "upper" line and the other as the "lower" line
			double upper;
			double lower;
			double upperi;
			double loweri;
			if (slope >= 0) {
				// Line goes up; lower line intersects lower right corner
				// if y=ax+b, b = y-ax
				lower = y0 - sh - (slope * (x0 + sw));
				upper = y0 + sh - (slope * (x0 - sw));
				// If going up, lower+(slope)*i is lower than lower+(slope)*(i+1)
				loweri = 0;
				upperi = 1;
			} else {
				lower = y0 - sh - (slope * (x0 - sw));
				upper = y0 + sh - (slope * (x0 + sw));
				// If going down, lower+(slope)*(i+1) is lower than lower+(slope)*i
				loweri = 1;
				upperi = 0;
			}
			// Iterate through the x-values
			for (int i = left; i < right; i++) {
				addIntersectingTiles((int) (Math.max(bottom, (slope * (i + loweri)) + lower)),
						(int) (Math.min(top, (slope * (i + upperi)) + upper)), i, true);
			}
		} else {
			// Movement is more vertical
			// Assume it goes up from below
			if (y0 > y1) {
				// Swap labels if necessary
				double xTemp = x0;
				double yTemp = y0;
				x0 = x1;
				x1 = xTemp;
				y0 = y1;
				y1 = yTemp;
			}
			double slope = (x1 - x0) / (y1 - y0); // slope, in this case, is ∆x/∆y
			double top = Math.min(sh + y1, worldHeight);
			int bottom = Math.max((int) (y0 - sh), 0);
			double left = Math.max(0, Math.min(x0, x1) - sw);
			double right = Math.min(Math.max(x0, x1) + sw, worldWidth);
			// Find the "offset" values for the two parallel lines bounding this hexagon
			// ie, represent each as x = (slope)y + (offset)
			// designate one line as the "lefter" line and the other as the "righter" line
			double lefter;
			double righter;
			double lefteri;
			double righteri;
			if (slope >= 0) {
				// Line goes right; lefter line intersects upper left corner
				// if x=ay+b, b = x-ay
				lefter = x0 - sw - (slope * (y0 + sh));
				righter = x0 + sw - (slope * (y0 - sh));
				// If going right, lefter+(slope)*i is "lefter" than lefter+(slope)*(i+1)
				lefteri = 0;
				righteri = 1;
			} else {
				lefter = x0 - sw - (slope * (y0 - sh));
				righter = x0 + sw - (slope * (y0 + sh));
				// If going left, lefter+(slope)*(i+1) is "lefter" than lefter+(slope)*i
				lefteri = 1;
				righteri = 0;
			}
			// Iterate through the y-values
			for (int i = bottom; i < top; i++) {
				addIntersectingTiles((int) (Math.max(left, (slope * (i + lefteri)) + lefter)),
						(int) (Math.min(right, (slope * (i + righteri)) + righter)), i, false);
			}
		}
		return intersectingTiles;
	}

	// This adds tiles within the given interval to the intersectingTiles arrayList
	// If vertical, it will add tiles (j, i0-i1 (inclusive) )
	// If !vertical, it will add tiles (i0-i1 (inclusive), j)
	public static void addIntersectingTiles(int i0, int i1, int j, boolean vertical) {
		if (vertical) {
			if (i1 >= worldHeight) {
				i1 = worldHeight - 1;
			}
			for (int i = Math.max(i0, 0); i <= i1; i++) {
				intersectingTiles.add(tiles[j][i]);
			}
		} else {
			if (i1 >= worldWidth) {
				i1 = worldWidth - 1;
			}
			for (int i = Math.max(i0, 0); i <= i1; i++) {
				intersectingTiles.add(tiles[i][j]);
			}
		}
	}

	/**
	 * getIntersecting Tiles, with two x/y coordinate pairs, but no solid, will find
	 * tiles intersecting a line segment.
	 **/
	public static ArrayList<Tile> getIntersectingTiles(double x0, double y0, double x1, double y1) {
		intersectingTiles.clear();
		if ((int) (x0) == (int) (x1)) {
			// vertical ("-ish") movement
			addIntersectingTiles((int) (Math.min(y0, y1)), (int) (Math.max(y0, y1)), (int) (x0), true);
		} else if ((int) (y0) == (int) (y1)) {
			// horizontal ("-ish") movement
			addIntersectingTiles((int) (Math.min(x0, x1)), (int) (Math.max(x0, x1)), (int) (y0), false);
		} else {
			// diagonal movement
			// Go column-by-column
			if (x1 < x0) {
				// Movement is left-to-right. Reverse direction if necessary
				x1 = x0 + x1;
				x0 = x1 - x0;
				x1 = x1 - x0;
				y1 = y0 + y1;
				y0 = y1 - y0;
				y1 = y1 - y0;
			}
			double slope = (y1 - y0) / (x1 - x0);
			// as one moves along this line segment, ymin changes to be
			// the highest or lowest y-value in that column
			// the column's y-value spans from ymin to ymin+slope
			double ymin = y0;
			int xmin = (int) (x0);
			xmin = Math.max(xmin, 0);
			double xmax = Math.min(x1, worldWidth);
			// x0 is probably in the middle of a column;
			// finish out this column
			if (x0 > xmin) {
				double ymax0 = ymin + (slope * (xmin + 1 - x0));
				addIntersectingTiles(xmin, (int) (Math.min(ymin, ymax0)), Math.max(ymin, ymax0));
				// now move to the beginning of the next column
				ymin = ymax0;
				xmin++;
			}
			// now iterate through the rest of the columns
			for (int i = xmin; i < xmax; i++) {
				addIntersectingTiles(i, (int) (Math.min(ymin, ymin + slope)), Math.max(ymin, ymin + slope));
				ymin += slope;
			}
		}
		return intersectingTiles;
	}

	// returns the first solid encountered while moving from (x0,y0) to
	// (x0+dx,y0+dy);
	// Looks for intersections between solids and the given line segment
	// Assumes the line segment has no width
	// Intersections with the solid s are ignored
	// If the function returns s, it means no intersection occurred
	public static Solid getIntersectingSolid(Solid s, double x0, double y0, double dx, double dy) {
		intersectingTiles = getIntersectingTiles(x0, y0, x0 + dx, y0 + dy);
		relevantSolids.clear();
		Solid intersectingSolid = s;
		double t0 = Double.POSITIVE_INFINITY;
		// intersectingSolid is the closest intersecting solid
		// t0 is the coefficient of the parameterization of the path at which an
		// intersection occurs
		for (Tile tile : intersectingTiles) {
			for (Solid r : tile.solids) {
				if (r == s || relevantSolids.contains(r)) {
					// don't test s
					// don't test the same thing twice
					continue;
				}
				relevantSolids.add(r);
				// test for intersection with r
				if (solidIntersectLineSegment(r, x0, y0, dx, dy)) {
					double t = wherePointIntersectSolid(x0, y0, dx, dy, r);
					if (t < t0) {
						// this is the closest intersection yet
						intersectingSolid = r;
						t0 = t;
					}
				}
			}
		}
		return intersectingSolid;
	}

	/**
	 * This returns the first obstacle s would encounter moving from (x0,y0) to
	 * (x0+dx,y0+dy). "Obstacles" include solids and unwalkable tiles if s "walks."
	 * 
	 * This was used for an older version of pathfinding.
	 **/
	public static Obstacle getIntersectingObstacle(Solid s, double x0, double y0, double dx, double dy,
			boolean reuseIntersectingTiles) {
		if (!reuseIntersectingTiles) {
			intersectingTiles = getIntersectingTiles(s, x0, y0, x0 + dx, y0 + dy);
		}
		relevantSolids.clear();
		Obstacle intersectingObstacle = s;
		double t0 = Double.POSITIVE_INFINITY;
		// intersectingSolid is the closest intersecting solid
		// t0 is the coefficient of the parameterization of the path at which an
		// intersection occurs
		for (Tile tile : intersectingTiles) {
			// If tile is unwalkable and s walks, check for that
			if (s.walks && !tile.walkable
					&& rectangleIntersectLineSegment(tile.x + 0.5, tile.y + 0.5, 1, 1, x0, y0, dx, dy)) {
				double t = wherePointIntersectRectangle(x0, y0, dx, dy, tile.x + 0.5, tile.y + 0.5, 1, 1);
				if (t < t0) {
					// this is the closest intersection
					intersectingObstacle = tile;
					t0 = t;
				}
			}
			for (Solid r : tile.solids) {
				if (r == s || relevantSolids.contains(r)) {
					// don't test s
					// don't test the same thing twice
					continue;
				}
				relevantSolids.add(r);
				// test for intersection with r
				if (intersect(s, x0, y0, dx, dy, r)) {
					double t = whereSolidIntersectSolid(s, x0, y0, dx, dy, r);
					if (t < t0) {
						// this is the closest intersection yet
						intersectingObstacle = r;
						t0 = t;
					}
				}
			}
		}
		return intersectingObstacle;
	}

	// if reuse is not specified, assume intersectingTiles must be re-determined
	public static Obstacle getIntersectingObstacle(Solid s, double x0, double y0, double dx, double dy) {
		return getIntersectingObstacle(s, x0, y0, dx, dy, false);
	}

	/**
	 * PATHFINDING BEGINS HERE! Node objects have been replaced with arrays to avoid
	 * calling the Garbage Collector. Each node is associated with an index number,
	 * which represents its place among the arrays. Each array contains the values
	 * of a single attribute for all nodes. eg, nodex.get(5) is the x-value of node
	 * #5
	 * 
	 * Because the loop always wants to get the node with the least
	 * "distanceThrough," unevaluated is a heap, ordered by distanceThrough.
	 * 
	 * This structure is defined below the other arrays.
	 * 
	 * Note that evaluated and unevaluated are lists of nodes, therefore they are
	 * actually lists of INDEX NUMBERS.
	 **/
	static EditableDoubleArray nodex = new EditableDoubleArray(200); // node x values
	static EditableDoubleArray nodey = new EditableDoubleArray(200); // node y values
	static EditableDoubleArray distanceTo = new EditableDoubleArray(200); // distance from start to a node
	static EditableDoubleArray distanceFrom = new EditableDoubleArray(200); // distance from node to destination
	static EditableDoubleArray distanceThrough = new EditableDoubleArray(200);// distance from start, through node,
	// to destination
	static EditableIntArray parent = new EditableIntArray(200); // node's parent node

	static EditableIntArray evaluated = new EditableIntArray(200); // nodes whose neighbors have been determined
	static EditableIntArray[][] tileNodes = new EditableIntArray[worldWidth][worldHeight]; // nodes, grouped by tiles
	static EditableIntArray neighbors = new EditableIntArray(40); // nodes connected to the currently evaluated node

	static class HeapOrderedByDistanceThrough {
		// This is what it sounds like: A heap, sorted by distanceThrough.
		// It is used to always get the node with the least distanceThrough.
		EditableIntArray array;
		HashSet<Integer> set = new HashSet<Integer>(); // for the "contains" method

		public HeapOrderedByDistanceThrough(int length) {
			array = new EditableIntArray(100);
		}

		private double getValue(int i) {
			// Note that "i" is the index number in "array"
			// array[i] is the index number for a node in the other arrays
			// "i" is the index number of an index number
			return distanceThrough.get(array.get(i));
		}

		private void siftUp(int i) {
			// potentially swaps an element with its parents
			if (i == 0) {
				return; // top element has no parent
			}
			int parenti = (i - 1) / 2;
			double value = getValue(i);
			double parentValue = getValue(parenti);
			if (value < parentValue) {
				// swap them
				int geti = array.get(i);
				array.set(i, array.get(parenti));
				array.set(parenti, geti);
				// call again on the new parent
				siftUp(parenti);
			}
		}

		private void siftDown(int i) {
			// potentially swaps an element with its child
			int childi = (i * 2) + 1;
			if (childi >= array.size()) {
				// element has no children; abort
				return;
			}
			double value = getValue(i);
			double childValue = getValue(childi);
			if (childi + 1 < array.size()) {
				// also consider second child
				double child2Value = getValue(childi + 1);
				if (child2Value < childValue) {
					// use child2 instead
					childi++;
					childValue = child2Value;
				}
			}
			// should child swap with parent?
			if (childValue < value) {
				// yes; swap
				int geti = array.get(i);
				array.set(i, array.get(childi));
				array.set(childi, geti);
				// call again on new child
				siftDown(childi);
			}
		}

		public void update(int i) {
			// This method finds node i and sifts it UP if necessary.
			if (array.size() == 0) {
				// nothing to sift. Empty array.
				return;
			}
			update(i, 0);
		}

		public boolean update(int i, int j) {
			// This method attempts to find node i and sifts it UP if necessary.
			// It starts looking at index number j and then tries j's children
			// Returns whether it found the desired node
			if (array.get(j) == i) {
				// found it
				siftUp(j);
				return true;
			} else if (getValue(j) > distanceThrough.get(i)) {
				// Went too far; these nodes would be children of i
				return false;
			} else {
				if (j + j + 1 < array.size() && update(i, j + j + 1)) {
					// got it!
					return true;
				}
				if (j + j + 2 < array.size()) {
					// try second child
					return update(1, j + j + 2);
				}
				// made it this far. We did not find it.
				return false;
			}
		}

		public void clear() {
			array.clear();
			set.clear();
		}

		public int size() {
			return array.size();
		}

		// public heap methods
		public void push(int i) {
			array.add(i);
			set.add(i);
			siftUp(array.size() - 1);
		}

		public int pop() {
			int top = array.get(0); // top (least) element
			if (array.size() > 1) {
				// array.set(0, array.remove(array.size()-1)); //replace with last element
				array.remove(0);
				siftDown(0); // sift down to re-heapify
			} else {
				array.clear();
			}
			set.remove(top);
			return top;
		}

		public boolean contains(int node) {
			// This is why we have the set!
			return set.contains(node);
		}
	}

	static HeapOrderedByDistanceThrough unevaluated = new HeapOrderedByDistanceThrough(200); // nodes connected to
																								// evaluated nodes

	// createNode creates a node at (x,y), adding it to the appropriate arrays.
	// Its return value is the node's index number, to be added to another array
	public static int createNode(double x, double y, double destinationx, double destinationy) {
		nodex.add(x);
		nodey.add(y);
		distanceTo.add(Double.MAX_VALUE);
		distanceThrough.add(Double.MAX_VALUE);
		distanceFrom.add(distanceBetween(x, y, destinationx, destinationy));
		parent.add(0);
		return (nodex.size() - 1);
	}

	// The setNodes method gets the nodes within a tile and adds them to the
	// appropriate tileNodes
	// The input parameter "s" is just there to prevent creating nodes around s
	// itself
	public static void setNodes(int i, int j, Solid s, double destinationx, double destinationy) {
		EditableIntArray nodes = tileNodes[i][j];
		if (nodes.size() > 0) {
			// Error! This should only be called when size == 0
			System.out.println("Error! Calling setNodes on a non-empty tileNodes array!");
			System.out.println(" Tile:  " + i + ", " + j);
			System.out.println(" Solid: " + s);
		}
		double sw = s.width * 0.5;
		relevantSolids.clear();
		/**
		 * This method should find all relevant nodes within this tile and place them
		 * within this tile's tileNodes collection.
		 * 
		 * A solid in an adjacent tile may create a node in this tile, as the node is
		 * not actually at the solid's corner. Rather, it must leave space for s's
		 * width.
		 * 
		 * Thus, this actually should look at tile (i,j) and the 8 surrounding tiles,
		 * but keep only nodes actually in tile (i,j).
		 **/
		for (int ii = i - 1; ii < i + 2; ii++) {
			if (ii < 0) {
				continue;
			}
			if (ii >= worldWidth) {
				break;
			}
			for (int jj = j - 1; jj < j + 2; jj++) {
				if (jj < 0) {
					continue;
				}
				if (jj >= worldHeight) {
					break;
				}
				for (Solid o : tiles[ii][jj].solids) {
					if (o == s || relevantSolids.contains(o)) {
						// No need to look at the object's own corners
						// No need to look at corners of the same object twice
						continue;
					}
					relevantSolids.add(o); // record this solid as having been "processed"
					if (o.shape == CIRCLE || o.shape == POINT) {
						double ow = (o.shape == CIRCLE ? pointRadius + (o.width * 0.5) : pointRadius);
						for (int k = 0; k < circleCorners.length; k++) {
							double nx = o.x + (circleCorners[k][0] * (ow + sw));
							double ny = o.y + (circleCorners[k][1] * (ow + sw));
							if (nodeInTile(nx, ny, i, j)) {
								nodes.add(createNode(nx, ny, destinationx, destinationy));
							}
						}
					} else if (o.shape == RECTANGLE) {
						double ow = o.width * 0.5;
						double oh = o.height * 0.5;
						int nodesCreated = 0;
						for (int k = 0; k < 4; k++) {
							double nx = o.x + ((ow + sw + pointRadius) * (k < 2 ? 1 : -1)); // try all four corners
							double ny = o.y + ((oh + sw + pointRadius) * (k % 2 == 0 ? 1 : -1));
							if (nodeInTile(nx, ny, i, j)) {
								// corner in tile. Add as node.
								nodes.add(createNode(nx, ny, destinationx, destinationy));
								nodesCreated++;
							}
						}
						// If no nodes created at corners, try nodes at object's edges
						if (nodesCreated == 0) {
							for (int k = 0; k < 4; k++) {
								double nx;
								double ny;
								if (k < 2) {
									// try horizontal edges
									if (k == 0) {
										ny = o.y + oh + sw + pointRadius;
									} else {
										ny = o.y - (oh + sw + pointRadius);
									}
									// try a point along this tile's horizontal midpoint
									nx = i + 0.5;
								} else {
									// try vertical edges
									if (k == 2) {
										nx = o.x + ow + sw + pointRadius;
									} else {
										nx = o.x - (ow + sw + pointRadius);
									}
									// try a point along this tile's vertical midpoint
									ny = j + 0.5;
								}
								if (nodeInTile(nx, ny, i, j)) {
									// edge point in tile. Add as node.
									nodes.add(createNode(nx, ny, destinationx, destinationy));
								}
							}
						}
					}
				}
			}
		}
		// put a node in the center if the tile is empty
		if (nodes.size() == 0) {
			nodes.add(createNode(i + 0.5, j + 0.5, destinationx, destinationy));
		}
	}

	public static boolean nodeInTile(double nodex, double nodey, int tilex, int tiley) {
		// tests whether a point(node) is in a tile
		return (nodex >= tilex && nodex < tilex + 1 && nodey >= tiley && nodey < tiley + 1);
	}

	static double[] nextStep = new double[2]; // The two doubles returned by the pathfinding algorithm

	/**
	 * getNextStep finds the next step in the path s could take from (x0,y0) to
	 * (x1,y1). It will generally try to find the shortest path, but only to an
	 * extent.
	 * 
	 * It implements the A-Star algorithm. The nodes are tile centers and corners of
	 * obstructions.
	 * 
	 * After finding a path, the path should be optimized to avoid unnecessary
	 * zig-zags.
	 **/

	public static double[] getNextStep(Solid s, double x0, double y0, double x1, double y1) {
		// clear all editable arrays
		nodex.clear();
		nodey.clear();
		distanceTo.clear();
		distanceFrom.clear();
		distanceThrough.clear();
		parent.clear();
		unevaluated.clear();
		evaluated.clear();
		for (int i = 0; i < worldWidth; i++) {
			EditableIntArray[] tileNodesi = tileNodes[i];
			for (int j = 0; j < worldHeight; j++) {
				tileNodesi[j].clear();
			}
		}
		distanceThrough.clear();

		// create starting point, add to all relevant arrays
		nodex.add(x0);
		nodey.add(y0);
		distanceTo.add(0);
		distanceFrom.add(distanceBetween(x0, y0, x1, y1));
		distanceThrough.add(distanceBetween(x0, y0, x1, y1));
		parent.add(0);
		unevaluated.push(0);

		for (int pathFindingIterations = 0; unevaluated.size() > 0
				&& pathFindingIterations < s.pathFindingIterations; pathFindingIterations++) {
			// Get the node from unevaluated with least distanceThrough
			// call this node "current"
			// "Pop" it to remove it. As we will now evaluate it, it is no longer
			// unevaluated
			int current = unevaluated.pop();
			// if current is the destination, reconstruct path (and optimize)
			if (nodex.get(current) == x1 && nodey.get(current) == y1) {
				// Found the destination node! Reconstruct the path and optimize
				return optimizePath(s, current);
			}
			if (narration) {
				tiles[(int) (nodex.get(current))][(int) (nodey.get(current))].drawBrown = true;
			}
			// add current to evaluated, for same reason
			evaluated.add(current);
			neighbors.clear();
			// Now look for neighbors in current's tile and the eight surrounding it
			int tilex = (int) (nodex.get(current));
			int tiley = (int) (nodey.get(current));
			double currentx = nodex.get(current);
			double currenty = nodey.get(current);
			for (int i = tilex - 1; i < tilex + 2; i++) {
				// don't look for things off the edge of the map!
				if (i < 0) {
					continue;
				}
				if (i >= worldWidth) {
					break;
				}
				for (int j = tiley - 1; j < tiley + 2; j++) {
					if (j < 0) {
						continue;
					}
					if (j >= worldHeight) {
						break;
					}
					if (s.walks && !tiles[i][j].walkable) {
						continue;
					}
					EditableIntArray nodes = tileNodes[i][j];
					// We are now searching for neighbors in tile i,j
					if (nodes.size() == 0) {
						// Its nodes have not yet been set. Set them!
						setNodes(i, j, s, x1, y1);
						// add the destination node if this is the appropriate tile
						if (nodeInTile(x1, y1, i, j)) {
							nodes.add(createNode(x1, y1, x1, y1));
						}
					}
					for (int k = 0; k < nodes.size(); k++) {
						// if this node is connected to current, add it to neighbors
						int nodek = nodes.get(k);
						if (nodek == current) {
							// a node should not be considered its own neighbor
							continue;
						}
						if (canMove(s, currentx, currenty, nodex.get(nodek) - currentx, nodey.get(nodek) - currenty)) {
							neighbors.add(nodek);
						}
					}
				}
			}
			// we now have neighbors; iterate through them
			for (int i = 0; i < neighbors.size(); i++) {
				int neighbor = neighbors.get(i);
				double newDistanceTo = distanceTo.get(current)
						+ distanceBetween(currentx, currenty, nodex.get(neighbor), nodey.get(neighbor));
				if (newDistanceTo < distanceTo.get(neighbor)) {
					// This is a shorter path to neighbor. Save it!
					parent.set(neighbor, current);
					distanceTo.set(neighbor, newDistanceTo);
					distanceThrough.set(neighbor, distanceTo.get(neighbor) + distanceFrom.get(neighbor));
					// if neighbor is not in unevaluated, add it
					if (!unevaluated.contains(neighbor)) {
						unevaluated.push(neighbor);
					}
					// if neighbor is already in unevaluated, we need to update with the new value
					else {
						unevaluated.update(neighbor);
					}
				}
			}
		}
		// If we made it out of the loop, then a path to the destination could not be
		// found.
		// Instead, just try to find a path to the point closest to the destination
		int closest = evaluated.get(0);
		for (int i = 1; i < evaluated.size(); i++) {
			if (distanceFrom.get(evaluated.get(i)) < distanceFrom.get(closest)) {
				// This is closer than the previously found closest point. Save it
				closest = evaluated.get(i);
			}
		}
		return optimizePath(s, closest);
	}

	/**
	 * This version of getNextStep attempts to move s toward target Solid t, without
	 * accounting for collisions with t.
	 **/
	public static double[] getNextStep(Solid s, double x0, double y0, Solid t) {
		for (Tile tt : t.tiles) {
			tt.solids.remove(t);
		}
		double[] n = getNextStep(s, x0, y0, t.x, t.y);
		for (Tile tt : t.tiles) {
			tt.solids.add(t);
		}
		return n;
	}

	/**
	 * optimizePath takes a path, represented as the node at the end, and connected
	 * by parent attributes, and finds the furthest node along this path that can be
	 * reached directly from the starting node.
	 **/
	public static double[] optimizePath(Solid s, int node) {
		int start = node;
		// find starting node

		while (parent.get(start) != start) {
			start = parent.get(start);
		}
		// now node is the end and start is the beginning
		double startx = nodex.get(start);
		double starty = nodey.get(start);
		while (!canMove(s, startx, starty, nodex.get(node) - startx, nodey.get(node) - starty)) {
			// If s cannot move from start to "node," replace node with its parent, one step
			// back
			node = parent.get(node);
		}
		nextStep[0] = nodex.get(node);
		nextStep[1] = nodey.get(node);
		if (narration)
			tiles[(int) (nextStep[0])][(int) (nextStep[1])].drawBlack = true;
		return nextStep;
	}

	// canMove determines whether a given Solid can move to a given location
	// This should be updated such that relevantSolids is filled as
	// intersectingTiles is filled, to avoid iterating through tiles twice
	public static boolean canMove(Solid s, double x, double y) {
		if (x < 0 || y < 0 || x >= worldWidth || y >= worldHeight) {
			return false;
		}
		if (s.walks && !tiles[(int) (x)][(int) (y)].walkable) {
			return false;
		}

		intersectingTiles = getIntersectingTiles(s, x, y);
		relevantSolids.clear();
		for (Tile t : intersectingTiles) {
			for (Solid r : t.solids) {
				if (r != s && !relevantSolids.contains(r)) {
					if (intersect(x, y, s.width, s.height, s.shape, r.x, r.y, r.width, r.height, r.shape)) {
						return false;
					}
					relevantSolids.add(r);
				}
			}
		}
		return true;
	}

	/**
	 * The version of canMove with two x/y pairs determines if s can move from
	 * (x0,y0) to (x0+dx,y0+dy). It will check for collisions along the way, not
	 * just at the final destination
	 **/
	public static boolean canMove(Solid s, double x0, double y0, double dx, double dy) {
		// is the destination in the world?
		if (x0 + dx < 0 || y0 + dy < 0 || x0 + dx >= worldWidth || y0 + dy >= worldHeight) {
			return false;
		}
		getIntersectingTiles(s, x0, y0, x0 + dx, y0 + dy);
		relevantSolids.clear();
		for (Tile t : intersectingTiles) {
			if (s.walks && !t.walkable) {
				if (rectangleIntersectLineSegment(t.x + 0.5, t.y + 0.5, 1, 1, x0, y0, dx, dy)) {
					return false;
				}
			}
			for (Solid r : t.solids) {
				if (r != s && !relevantSolids.contains(r)) {
					// check if s will intersect r
					if (intersect(s, x0, y0, dx, dy, r)) {
						return false;
					}
					relevantSolids.add(r);
				}
			}
		}
		return true;
	}

	// moveIfCan moves s to (x,y) if possible. Returns whether it was possible.
	public static boolean moveIfCan(Solid s, double x, double y) {
		if (canMove(s, x, y)) {
			s.moveTo(x, y, intersectingTiles);
			return true;
		}
		return false;
	}

	// finds the square of the distance between two points
	public static double distanceBetween2(double x0, double y0, double x1, double y1) {
		double dx = x1 - x0;
		double dy = y1 - y0;
		return (dx * dx) + (dy * dy);
	}

	public static double distanceBetween(double x0, double y0, double x1, double y1) {
		double dx = x1 - x0;
		double dy = y1 - y0;
		if (dx == 0) {
			return Math.abs(dy);
		} else if (dy == 0) {
			return Math.abs(dx);
		}
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	/**
	 * getClosestSolidInCollection will find the Solid in Collection c, closest to
	 * s, within a range of r. If no such Solid exists, it will return s.
	 * 
	 * Note that it measures distance to a Solid's center, not its exterior.
	 **/
	public static Solid getClosestSolidInCollection(Solid s, Collection<? extends Solid> c, double r) {
		Solid closest = s;
		relevantSolids.clear();
		// first, try s' own tile
		int x = (int) (s.x);
		int y = (int) (s.y);
		addSolidsIfRelevant(x, y, s, c, r);
		// if that yielded nothing, expand outward
		for (int i = 1; i <= r && relevantSolids.isEmpty(); i++) {
			// try all tiles around the edge of the square with radius i
			// start with upper edge
			for (int j = x - i; j < x + i; j++) {
				addSolidsIfRelevant(j, y - i, s, c, r);
			}
			// right edge
			for (int j = y - i; j < y + i; j++) {
				addSolidsIfRelevant(x + i, j, s, c, r);
			}
			// bottom edge
			for (int j = x + i; j > x - i; j--) {
				addSolidsIfRelevant(j, y + i, s, c, r);
			}
			// left edge
			for (int j = y + i; j > y - i; j--) {
				addSolidsIfRelevant(x - i, j, s, c, r);
			}
		}
		// if relevantSolids is nonempty, find the closest Solid in it
		double leastDist2 = Double.POSITIVE_INFINITY;
		for (Solid d : relevantSolids) {
			double newDist2 = distanceBetween2(s.x, s.y, d.x, d.y);
			if (newDist2 < leastDist2) {
				closest = d;
				leastDist2 = newDist2;
			}
		}
		return closest;
	}

	/**
	 * This is used by getClosestSolidInCollection to add Solids to relevantSolids
	 * if they are actually relevant
	 **/
	private static void addSolidsIfRelevant(int i, int j, Solid s, Collection<? extends Solid> c, double r) {
		if (i < 0 || j < 0 || i >= worldWidth || j >= worldHeight) {
			// don't consider tiles out of the world
			return;
		}
		for (Solid d : tiles[i][j].solids) {
			if (d != s && c.contains(d) && distanceBetween2(s.x, s.y, d.x, d.y) <= r * r) {
				relevantSolids.add(d);
			}
		}
	}

	/**
	 * This method determines if an enemy of s is nearby
	 **/
	public static boolean enemyNearby(Navigator s) {
		final double r = s.detectionRange; // how far this method looks for enemies
		for (int i = Math.max(0, (int) (s.x - r)); i < s.x + r; i++) {
			if (i >= worldWidth) {
				break;
			}
			for (int j = Math.max(0, (int) (s.y - r)); j < s.y + r; j++) {
				if (j >= worldHeight) {
					break;
				}
				for (Solid t : tiles[i][j].solids) {
					if (t != s && s.enemies.contains(t)) {
						// is it close enough?
						if (distanceBetween2(t.x, t.y, s.x, s.y) <= r * r) {
							return true;
						}
					}
				}
			}
		}
		// nothing found!
		return false;
	}

	/**
	 * This method will take a given Navigator s, find the direction that takes it
	 * away from its enemies, and write that direction to s.tx and s.ty
	 * 
	 * Navigators that flee when detecting an enemy can then move, or navigate,
	 * toward the point: (their location + (tx,ty))
	 **/
	public static void flee(Navigator s) {
		final double r = s.detectionRange; // how far this method looks for enemies
		relevantSolids.clear();
		for (int i = Math.max(0, (int) (s.x - r)); i < s.x + r && i < worldWidth; i++) {
			for (int j = Math.max(0, (int) (s.y - r)); j < s.y + r && j < worldHeight; j++) {
				for (Solid t : tiles[i][j].solids) {
					if (t != s && s.enemies.contains(t) && !relevantSolids.contains(t)) {
						// find nearby enemies, but count each only once, and only if close enough
						if (distanceBetween2(t.x, t.y, s.x, s.y) <= r * r) {
							relevantSolids.add(t);
						}
					}
				}
			}
		}
		// (fleex,fleey) is the movement vector
		double fleex = 0;
		double fleey = 0;
		for (Solid t : relevantSolids) {
			// (fleex, fleey) gets "pushed" by nearby enemies
			double dx = t.x - s.x;
			double dy = t.y - s.y;
			double d2 = (dx * dx) + (dy * dy);
			// this push is scaled by the enemies' distance squared
			fleex += (s.x - t.x) / d2;
			fleey += (s.y - t.y) / d2;
		}
		if (fleex != 0 || fleey != 0) {
			// if this would change something, write it to s.tx and s.ty
			// scale it such that the vector is always 10 units long
			double d = Math.sqrt((fleex * fleex) + (fleey * fleey));
			fleex *= 10 / d;
			fleey *= 10 / d;
			s.tx = fleex;
			s.ty = fleey;
		}
	}

	// The maximum "gap" between trees in a wall:
	final public static double treeGap = Player.width * 0.25;

	/**
	 * MAP MAKING METHODS
	 * 
	 * fillWithTrees will attempt to fill the designated rectangle with trees. It
	 * uses the list remainingTiles to track which tiles have already been
	 * addressed.
	 **/
	private static ArrayList<Tile> remainingTiles = new ArrayList<Tile>();

	public static void fillWithTrees(int x, int y, int width, int height, double gap) {
		/**
		 * This will iterate through the tiles in the designated region, but in random
		 * order. For each tile, it will attempt to place a tree of random width, in a
		 * random place in the chosen tile. It will then remove this tile from the list.
		 * If a "gap" is chosen, it will try to leave that much space blank beside each
		 * tree, to leave a gap through which navigators can move.
		 * 
		 * First, fill remainingTiles with relevant tiles
		 **/
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		width = Math.min(width, worldWidth - x);
		height = Math.min(height, worldHeight - y);
		remainingTiles.clear();
		for (int i = x; i < x + width; i++) {
			for (int j = y; j < y + height; j++) {
				remainingTiles.add(tiles[i][j]);
			}
		}
		// Now expand the area, to prevent a gap that runs around the entire perimeter
		x = (int) (x - gap);
		y = (int) (y - gap);
		width = (int) (width + gap + gap);
		height = (int) (height + gap + gap);
		Tree e = new Tree(1);
		// now go through them randomly.
		while (!remainingTiles.isEmpty()) {
			Tile t = remainingTiles.remove((int) (Math.random() * remainingTiles.size()));
			if (!t.canHaveTree) {
				// don't place a tree on water
				continue;
			}
			/**
			 * Pick a random width. Can a tree be placed here? Note that the width is
			 * artificially large, to leave a gap around the tree. Later, the tree's correct
			 * width will be set.
			 **/
			e.width = (2 * gap) + Tree.minWidth + (Math.random() * (Tree.maxWidth - Tree.minWidth));
			e.height = e.width;
			double ex = t.x + Math.random();
			double ey = t.y + Math.random();
			// Can this be placed here?
			boolean good = true;
			for (int i = 0; i < 2; i++) {
				good = true;
				if (!canMove(e, ex, ey)) {
					// no; intersects something
					good = false;
				} else if (ex - (e.width * 0.5) < x || ex + (e.width * 0.5) > x + width || ey - (e.width * 0.5) < y
						|| ey + (e.width * 0.5) > y + height) {
					// exceeds bounds of area
					good = false;
				}
				if (good) {
					// break loop and place tree
					break;
				} else {
					// try again with smallest tree size
					e.width = (2 * gap) + Tree.minWidth;
					e.height = e.width;
				}
			}
			if (good) {
				// now place the tree with the correct width
				e.width -= (2 * gap);
				e.height = e.width;
				moveIfCan(e, ex, ey);
				// now make a new tree to place
				e = new Tree(1);
			}
		}
	}

	/**
	 * makeFencers places n fencers within the rectangle with corner (x,y) and
	 * width/height (w,h) These fencers will have randomized fightingStyles. Also,
	 * when one notices you, the others will, too.
	 **/
	public void makeFencers(int n, int x, int y, int w, int h) {
		// clear remainingTiles
		remainingTiles.clear();

		// make a "friends" list
		ArrayList<Navigator> friends = new ArrayList<Navigator>(n);

		// now add the relevant ones
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		w = Math.min(w, worldWidth - x);
		h = Math.min(h, worldHeight - y);
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				remainingTiles.add(tiles[i][j]);
			}
		}

		int fencersPlaced = 0;

		// now make the first Fencer
		double r = Math.random();
		Fencer f = new Fencer(player);

		// Now go through remainingTiles iteratively and randomly pick one to use
		while (!remainingTiles.isEmpty()) {
			Tile t = remainingTiles.remove((int) (Math.random() * remainingTiles.size()));
			if (canMove(f, t.x + 0.5, t.y + 0.5)) {
				// can place Fencer f here. Do it.
				f.place(t.x + 0.5, t.y + 0.5);
				// record the placement
				fencersPlaced++;
				friends.add(f);
				if (fencersPlaced < n) {
					// make a new one to place
					f = new Fencer(player);
				} else {
					break;
				}
			}
		}
		// now add the friends list to every fencer, so they know their friends
		for (Navigator nav : friends) {
			nav.friends = friends;
		}
	}

	/**
	 * This makes a wall with a door in the center
	 **/
	public static void makeWallWithDoor(double x, double y, double width, double height, boolean vertical,
			double doorWidth) {
		if (vertical) {
			new Wall(x, y, width, (height - doorWidth) * 0.5);
			new Wall(x, y + ((height + doorWidth) * 0.5), width, (height - doorWidth) * 0.5);
		} else {
			new Wall(x, y, (width - doorWidth) * 0.5, height);
			new Wall(x + ((width + doorWidth) * 0.5), y, (width - doorWidth) * 0.5, height);
		}
	}

	/**
	 * makeBuilding will make a rectangular building, with northwest corner at (x,y)
	 * and internal width and height as given. "Thickness" refers to the thickness
	 * of the walls.
	 * 
	 * This building also has doorways. Each doorway is determined by its
	 * corresponding boolean. Doorways are placed in the center of their walls. They
	 * are doorWidth in width.
	 * 
	 * The building's interior is stone floor.
	 * 
	 * Note that the building's entire width is width + (2*thickness)! Width is just
	 * the width of the open inside area!
	 **/
	final public static double doorWidth = 1.4;

	public static void makeBuilding(int x, int y, int width, int height, double thickness, boolean northDoor,
			boolean eastDoor, boolean southDoor, boolean westDoor) {
		// set the tiles to be tiles
		for (int i = x; i < x + width; i++) {
			for (int j = y; j < y + height; j++) {
				tiles[i][j].setType(Tile.STONE_FLOOR);
			}
		}

		// north wall
		if (northDoor) {
			makeWallWithDoor(x - thickness, y - thickness, width + thickness + thickness, thickness, false, doorWidth);
		} else {
			new Wall(x - thickness, y - thickness, width + thickness + thickness, thickness);
		}

		// south wall
		if (southDoor) {
			makeWallWithDoor(x - thickness, y + height, width + thickness + thickness, thickness, false, doorWidth);
		} else {
			new Wall(x - thickness, y + height, width + thickness + thickness, thickness);
		}

		// east wall
		if (eastDoor) {
			makeWallWithDoor(x + width, y - thickness, thickness, height + thickness + thickness, true, doorWidth);
		} else {
			new Wall(x + width, y - thickness, thickness, height + thickness + thickness);
		}

		// west wall
		if (westDoor) {
			makeWallWithDoor(x - thickness, y - thickness, thickness, height + thickness + thickness, true, doorWidth);
		} else {
			new Wall(x - thickness, y - thickness, thickness, height + thickness + thickness);
		}
	}

	// makes the area with the corpse and its sword
	public static void makeCorpseArea(int x, int y, int w, int h) {
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				tiles[i][j].setType(Tile.FOREST_FLOOR);
			}
		}
		Corpse steve = new Corpse(1.3 * 0.63, 1.3 * 0.96);
		double sx = x + 3 + (Math.random() * (w - 6));
		double sy = y + 3 + (Math.random() * (h - 4));
		steve.moveTo(sx, sy, getIntersectingTiles(steve, sx, sy));
		fillWithTrees(x, y, w, h, 2);
	}

	// makes a starting area with you and trees
	public static void makePlayerArea(int x, int y, int w, int h) {
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				tiles[i][j].setType(Tile.FOREST_FLOOR);
			}
		}
		player.moveTo(x + (w * 0.5), y + (h * 0.5), getIntersectingTiles(player, x + (w * 0.5), y + (h * 0.5)));

		fillWithTrees(x, y, w, h, 2);
	}

	// makes an area with the old man and his home
	public static void makeOldManArea(int x, int y, int w, int h) {
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				tiles[i][j].setType(Tile.FOREST_FLOOR);
			}
		}
		// The cabin
		ShingledRoof shin = new ShingledRoof(5, 5) {
			public void hit(int damage) {
				getResponse("The old man does not seem to appreciate you hitting his house with your sword.");
			}
		};
		double hx = x + 3.5 + (Math.random() * (w - 7));
		double hy = y + 3.5 + (Math.random() * (h - 10));
		moveIfCan(shin, hx, hy);

		// The man outside the cabin
		Talker monsterMan = new MonsterMan(hx, hy + 4, Math.PI, new Color(90, 90, 30), player);
		fillWithTrees(x, y, w, h, 2);
	}

	// makes an area with randomized trees of a given spacing
	public void makeIntermediateArea(int x, int y, int w, int h, double s) {
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				tiles[i][j].setType(Tile.FOREST_FLOOR);
			}
		}
		fillWithTrees(x, y, w, h, s);
	}

	// makes an area with a wandering monster and some trees
	public void makeMonsterArea(int x, int y, int w, int h) {
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				tiles[i][j].setType(Tile.FOREST_FLOOR);
			}
		}
		// The monster
		new KnownMonster(x + (w / 2), y + (h / 2), x, y + 4, w, h - 4, player);
		fillWithTrees(x, y + 4, w, h - 4, 2.4);
	}

	/**
	 * makes an area with the old woman at the top, presumably by the stream above
	 * her. She tells you about the game's general goal. Remember to put this next
	 * to water!
	 **/
	public static void makeOldWomanArea(int x, int y, int w, int h) {
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				tiles[i][j].setType(Tile.FOREST_FLOOR);
			}
		}

		Talker oldWoman = new OldWoman(x + (h * 0.5) - 0.5 + Math.random(), y, 0, new Color(90, 90, 30), player);
		fillWithTrees(x, y, w, h, 2.5);
	}

	/**
	 * Makes the eight initial areas: Monster and bridge intermediate area Man with
	 * cabin Starting (Player) area Corpse area intermediate area Woman with cabin
	 * Single spider
	 * 
	 * x-values refer to the edges of each area y is the top h is the height of the
	 * area
	 **/
	public void makeInitial8(int x0, int x1, int x2, int x3, int x4, int x5, int x6, int x7, int y, int h) {
		makeMonsterArea(x0, y, x1 - x0, h);
		makeIntermediateArea(x1, y, x2 - x1, h, 2.4);
		makeOldManArea(x2, y, x3 - x2, h);
		makePlayerArea(x3, y, x4 - x3, h);
		makeCorpseArea(x4, y, x5 - x4, h);
		makeIntermediateArea(x5, y, x6 - x5, h, 2);
		makeOldWomanArea(x6, y, x7 - x6, h);
		makeSingleSpider(x7, y, (worldWidth - 10) - x7, h);
	}

	// a convenient way to set tile types:
	public void setTileType(int x, int y, int w, int h, int type) {
		x = Math.max(0, x);
		y = Math.max(0, y);
		w = Math.min(w, worldWidth - x);
		h = Math.min(h, worldHeight - y);
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				tiles[i][j].setType(type);
			}
		}
	}

	// makes an orchard with regularly arranged trees
	// first tree is in the northwest corner
	// tree centers appear every [spacing] units
	// trees are "width" in width
	public void makeOrchard(int x, int y, int w, int h, double spacing, double width) {
		for (double i = x + (width * 0.5); i < x + w; i += spacing) {
			for (double j = y + (width * 0.5); j < y + h; j += spacing) {
				new Tree(width, i, j);
			}
		}
	}

	/**
	 * Makes buildings within the designated area. They form either a row or column.
	 * (x,y) is the northwest corner of the area (w,h) is the width and height of
	 * the area vertical is whether this is a column if vertical, then every
	 * building will have width of w, but with variable height northeast is whether
	 * the buildings have doors on the north or east side vertical building columns
	 * have doors on their west or east sides n is the number of buildings m is the
	 * minimum building width/height (depending on whether this is a row/column) a
	 * is "alley width," the space between buildings
	 * 
	 * Note that this method does not set tile types, except for the stone floors
	 * inside buildings
	 * 
	 * length must be a minimum of n*(m+2+a) - a
	 **/
	public void makeBuildingArea(int x, int y, int w, int h, boolean vertical, boolean northeast, int n, int m, int a) {
		int l = (vertical ? h : w); // "length" of space to fill with buildings
		int[] builds = new int[n]; // array of building lengths
		for (int i = 0; i < n; i++) {
			builds[i] = m;
			l -= m;
		}
		// l is the length remaining to be assigned
		// note that, between adjacent buildings is an alley of 2+a
		l -= (2 + a) * (n - 1);
		// also have 1-tile gaps at the very edges
		l -= 2;
		// now to randomly assign the remaining length
		while (l > 0) {
			builds[(int) (Math.random() * n)]++;
			l--;
		}
		// now builds represents building lengths
		int t = 1; // the "cursor" where the next building will be placed
		for (int i = 0; i < n; i++) {
			if (vertical) {
				// building has width of w (actually -2 because of space on either side)
				makeBuilding(x + 1, y + t, w - 2, builds[i], 0.3, false, northeast, false, !northeast);
				t += builds[i] + 2 + a;
			} else {
				makeBuilding(x + t, y + 1, builds[i], h - 2, 0.3, northeast, false, !northeast, false);
				t += builds[i] + 2 + a;
			}
		}
	}

	// makes a walled village
	public void makeWalledVillage(int x, int y) {
		int width = 60;
		int height = 49;
		// make the walls
		new Wall(x, y - 0.5, width, 0.5);
		new Wall(x - 0.5, y - 0.5, 0.5, height + 1);
		new Wall(x + width, y - 0.5, 0.5, height + 1);
		// entrances as x==12 and 47
		new Wall(x, y + height, 11, 0.5);
		new Wall(x + 14, y + height, 32, 0.5);
		new Wall(x + 49, y + height, 11, 0.5);
		// set tiles
		setTileType(x, y, width, height, Tile.STONE_STREET);
		setTileType(x, y, 37, 16, Tile.FOREST_FLOOR);
		// garden before corner building
		setTileType(x + 50, y + 10, 8, 5, Tile.FOREST_FLOOR);
		// make left edge of buildings
		makeBuildingArea(x + 1, y + 16, 10, 32, true, true, 3, 7, 1);
		/**
		 * //make their beds - this has been turned off! new Bed(x+4, y+20, 4, 2,
		 * Math.PI*0.5); new Bed(x+4, y+32, 4, 2, Math.PI*0.5); new Bed(x+4, y+46, 4, 2,
		 * Math.PI*0.5);
		 **/
		// make extra wide building
		makeBuilding(x + 15, y + 39, 12, 8, 0.3, false, false, false, true);
		// it has a pillar
		new Pillar(1, x + 21, y + 43);
		// make middle row of buildings
		makeBuildingArea(x + 14, y + 28, 32, 9, false, true, 3, 7, 1);
		// make building behind extra wide
		makeBuilding(x + 30, y + 39, 15, 8, 0.3, false, true, false, false);
		// it also has pillars
		new Pillar(1, x + 35, y + 43);
		new Pillar(1, x + 40, y + 43);
		// make right edge of buildings
		makeBuildingArea(x + 49, y + 16, 10, 32, true, false, 3, 7, 1);
		// make town hall
		makeBuilding(x + 39, y + 2, 8, 13, 0.3, false, true, true, false);
		// make town hall pillars
		for (int i = x + 41; i < x + 46; i += 4) {
			for (double j = y + 4.5; j < y + 13; j += 4) {
				new Pillar(1, i, j);
			}
		}
		// make well
		Well well = new Well(1.5);
		well.moveTo(x + 43, y + 20, getIntersectingTiles(well, x + 43, y + 20));
		// make corner building (tower)
		makeBuilding(x + 52, y + 2, 6, 6, 0.3, false, false, true, false);
		// corpse within tower
		Corpse lenny = new CorpseWithShard(1.3 * 0.63, 1.3 * 0.96);
		lenny.moveTo(x + 53, y + 5, getIntersectingTiles(lenny, x + 53, y + 5));
		// set corpse's visibility area
		lenny.hiding = true;
		lenny.setVisibilityArea(x, y, width, height);
		// make upper row buildings
		int midWidth = (int) (Math.random() * 2);
		makeBuilding(x + 15, y + 17, 8 + midWidth, 7, 0.3, false, false, true, false);
		makeBuilding(x + 26 + midWidth, y + 17, 9 - midWidth, 7, 0.3, false, true, false, false);
		// make tree below corner building
		new Tree(1.5, x + 54, y + 12.5);
		// make big orchard
		makeOrchard(x + 3, y + 1, 30, 13, 3.5, 1);
		// make wandering monster
		WanderingMonster wamo = new HidingWanderingMonster(x + 5, y + 3, x + 2, y + 3, 30, 13, player);
		wamo.setVisibilityArea(x, y, width, height);

		// make spiders
		// first set: TrapSpiders
		ArrayList<Navigator> trapSpiders = new ArrayList<Navigator>(6);
		double wx = well.x - 6;
		double wy = well.y - 6;
		double ww = 12;
		double wh = 11;
		trapSpiders.add(new TrapSpider(x + 34, y + 18, player, x, y, width, height, wx, wy, ww, wh));
		trapSpiders.add(new TrapSpider(x + 41, y + 11, player, x, y, width, height, wx, wy, ww, wh)); // town hall
		trapSpiders.add(new TrapSpider(x + 45, y + 11, player, x, y, width, height, wx, wy, ww, wh)); // town hall
		trapSpiders.add(new TrapSpider(x + 51, y + 18, player, x, y, width, height, wx, wy, ww, wh));
		trapSpiders.add(new TrapSpider(x + 39, y + 30, player, x, y, width, height, wx, wy, ww, wh));
		for (Navigator s : trapSpiders) {
			s.friends = trapSpiders;
		}
		// second set: regular Spiders
		ArrayList<Navigator> spiders = new ArrayList<Navigator>(6);
		spiders.add(new Spider(player, x + 4, y + 18));
		spiders.add(new Spider(player, x + 4, y + 22));
		spiders.add(new Spider(player, x + 1, y + 26.5));
		spiders.add(new Spider(player, x + 16, y + 18));
		for (Navigator s : spiders) {
			s.friends = spiders;
			s.hiding = true;
			s.setVisibilityArea(x, y, width, height);
		}

		// now make all tiles unable to have trees, to avoid unwanted randomized future
		// placements
		for (int i = x; i < x + width; i++) {
			for (int j = y; j < y + height; j++) {
				tiles[i][j].canHaveTree = false;
			}
		}
	}

	/**
	 * The junkie/sister situation is as follows: A junkie accidentally killed his
	 * dealer while trying to get more drugs. The dealer's sister is aware of the
	 * event, but is unable to locate the junkie, who fled after the incident. If
	 * you meet the dealer's sister, she will describe the junkie to you.
	 * 
	 * The junkie is hiding at a certain camp, which contains a cold fire pit. If
	 * you reach the camp before having met the sister, you will see the remains of
	 * a camp, but not the junkie himself. Once you meet the sister, if you visit
	 * the camp, the junkie will be there. You can confront him. If you kill him,
	 * you can return to the sister for a reward.
	 * 
	 * Alternatively, if you decline to kill him, he will flee. You can then return
	 * to the sister's house and inform her of what happened. She will thank you and
	 * go to the camp to investigate. This leaves her home unoccupied. You can
	 * exploit the opportunity and search it for the reward you would have received.
	 * 
	 * The dealer's sister is a Navigator. When her intention is STAND, she will
	 * talk. If you tell her about the junkie's camp, her intention changes to MOVE
	 * and she will walk to the camp.
	 **/
	public static boolean receivedRewardForJunkie = false;
	public static boolean dealerSisterGone = false;
	public static Navigator dealerSister;
	public TableWithShard tibble;

	public void makeDealerSister(int x, int y, Junkie junkie) {
		int w = 30;
		int h = 30;
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				tiles[i][j].setType(Tile.FOREST_FLOOR);
			}
		}
		int hx = x + 15;
		int hy = y + 15;
		// The cabin
		makeBuilding(hx - 3, hy - 4, 6, 4, 0.3, false, false, true, false);
		/**
		 * The table with the shard should possibly be replaced with something else,
		 * like a dresser or a (treasure) chest, when I determine how to draw it
		 * recgonizably
		 **/
		new TableWithTubers(hx - 2, hy - 1, 2, 2);
		new Bed(hx + 2, hy - 2.15, 2, 3.7, 0);
		tibble = new TableWithShard(hx + 0.5, hy - 3.5, 1, 1);
		// The woman outside the cabin
		dealerSister = new DealerSister(1, 1, tibble, junkie);
		dealerSister.direction = Math.PI;
		dealerSister.place(hx, hy + 0.15);

		// tx and ty let the dealerSister find the junkie
		fillWithTrees(x, y, w, h, 2);
	}

	// the state of the junkie interaction

	public static FirePit fip;

	public Junkie makeJunkieCamp(int x, int y) {
		int w = 30;
		int h = 30;
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				tiles[i][j].setType(Tile.FOREST_FLOOR);
			}
		}
		// The fire pit
		fip = new FirePit(x + (w * 0.5), y + (h * 0.5), 1.5);
		// the tent
		Tent tent = new Tent(fip.x + 5, fip.y, 2.7 * 0.93, 2.7 * 0.85);
		// the junkie
		Junkie junkie = new Junkie(player);
		junkie.place(fip.x, fip.y - 2);
		// set junkie's "territory"
		junkie.wx = junkie.tx - 8;
		junkie.wy = junkie.ty - 8;
		junkie.ww = 16;
		junkie.wh = 16;
		// fill with trees
		fillWithTrees(x, y, w, h, 2);
		// now remove the junkie. You only see him after talking with the sister
		junkie.disconnectTiles();
		actors.remove(junkie);
		return junkie;
	}

	/**
	 * This makes a small village. The villagers will be celebrating. You can meet a
	 * beautiful person and go someplace private with them.
	 * 
	 * Then they are attacked by raiders, who will target you and any of the
	 * villagers.
	 * 
	 * The raiders are made at the north/west edges, then hidden. They only appear
	 * when "summoned" by lover.
	 **/

	public void makeFestival(int x, int y) {
		int w = 30; // width of festival
		int h = 30; // height of festival
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				tiles[i][j].setType(Tile.FOREST_FLOOR);
				tiles[i][j].canHaveTree = false;
			}
		}
		// buildings
		ShingledRoof cabin1 = new ShingledRoof(4.9, 4.9);
		double cx = x + 25;
		double cy = y + 12;
		cabin1.moveTo(cx, cy, getIntersectingTiles(cabin1, cx, cy));
		ShingledRoof cabin2 = new ShingledRoof(4.9, 4.9);
		cx = x + 22;
		cy = y + 24;
		cabin2.moveTo(cx, cy, getIntersectingTiles(cabin2, cx, cy));
		ShingledRoof cabin3 = new ShingledRoof(4.9, 4.9);
		cx = x + 5;
		cy = y + 20;
		cabin3.moveTo(cx, cy, getIntersectingTiles(cabin3, cx, cy));
		// fire pit
		Bonfire b = new Bonfire(x + (w * 0.5), y + (h * 0.5), 1.5);
		// table with food
		new TableWithFood(x + 10, y + 10, 2, 4);
		// table with plates
		new TableWithPlates(x + 11, y + 18, 4, 2);
		// lists of villagers and raiders. Each knows a list of the others
		final ArrayList<SolidActor> villagers = new ArrayList<SolidActor>(20);
		final ArrayList<Fencer> raiders = new ArrayList<Fencer>(20);
		// what do the villagers say?
		String m = "This person is talking to another. You don't want to interrupt.";
		// make the villagers:
		// two by the upper left corner
		villagers.add(new FestiveVillager(player, m, x + 7, y + 9, Math.PI));
		villagers.add(new FestiveVillager(player, m, x + 7, y + 10.5, 0));
		// two watching the fire
		m = "You talk to a villager. They're celebrating some holiday.\nThis person might be drunk.";
		villagers.add(new FestiveVillager(player, m, b.x, b.y + 2, 0));
		villagers.add(new FestiveVillager(player, m, b.x - 3, b.y, Math.PI * 1.5));
		// three talking southeast of the fire
		villagers.add(new FestiveVillager(player, "This person is telling a story. You don't want to interrupt.",
				b.x + 5, b.y + 1, Math.PI));
		m = "This person is listening to someone tell a story. You don't want to interrupt.";
		villagers.add(new FestiveVillager(player, m, b.x + 4, b.y + 2, Math.PI * 1.75));
		villagers.add(new FestiveVillager(player, m, b.x + 5.5, b.y + 2.5, Math.PI * 0.125));
		// three musicians
		m = "This person is playing music. You don't want to interrupt.";
		villagers.add(new FestiveVillager(player, m, x + 14, y + 7, Math.PI));
		villagers.add(new FestiveVillager(player, m, x + 16, y + 7, Math.PI));
		villagers.add(new FestiveVillager(player, m, x + 18, y + 7.5, Math.PI * 0.875));
		// the dancers
		// keep pairs 1.1 apart!
		// the one with the "false" role has a greater y-value
		m = "This person is dancing. You don't want to interrupt.";
		villagers.add(new VillageDancer(player, m, x + 15.5, y + 11.5, true));
		villagers.add(new VillageDancer(player, m, x + 15.5, y + 12.6, false));
		villagers.add(new VillageDancer(player, m, x + 13, y + 11.5, true));
		villagers.add(new VillageDancer(player, m, x + 13, y + 12.6, false));
		// Your lover
		villagers.add(new Lover(x + 22, y + 8.5, Math.PI * 0.6, x + 19, y + 11.5, cabin1.x - (cabin1.width * 0.5),
				cabin1.y - (cabin1.height * 0.5), cabin1.width, cabin1.height));
		// make villagers enemies of raiders
		for (SolidActor v : villagers) {
			v.enemies = raiders;
		}
		// Now make the raiders
		int num = villagers.size();
		// raiders can still attack player!
		villagers.add(player);
		// raiders are like fencers, but they wander after fighting
		// They start along the north and west edges of the village
		for (int i = 0; i < num; i++) {
			// alternate making one along the north and west edge
			raiders.add(new FestivalRaider(villagers.get(i), (i % 2 == 0 ? x + 1 + (0.75 * i) : x + 1),
					(i % 2 == 1 ? y + 1.75 + (0.75 * i) : y + 1)));
		}
		FestivalRaider.raiders = raiders;
		FestivalRaider.actors = actors;
		FestivalRaider.villagers = villagers;
	}

	/**
	 * This makes a castle, inhabited by squatters. It contains one special
	 * squatter, the "summoner." When you encounter him, he will beg for his life.
	 * If you spare him, he will flee to get reinforcements.
	 * 
	 * Northwest corner and dimensions (x, y, w, h): Entire castle, including walls:
	 * x+1, y, k + 30 - x, 40.4 courtyard: x+2, y+1, k-x-2.3, 38.4 infirmary: k+0.3,
	 * y+1, 10.4, 10.4 lounge: k+11.3, y+1, 8.4, 10.4 storage/workshop: k+20.3, y+1,
	 * 9.7, 10.4 great hall: k+2.6, y+14.3, 13.1, 12.8 kitchen: k+16.3, y+18.7,
	 * 11.4, 8.4 pantry: k+16.3, y+14.3, 11.4, 3.8 common bedroom 1: k+0.3, y+30,
	 * 11.4, 10.5 common bedroom 2: k+12.3, y+30, 11.4, 10.5 master bedroom: k+24.3,
	 * y+30, 5.7, 10.5
	 * 
	 * Hallways are 1.7 in width
	 * 
	 **/
	public void makeCastle(int x, int y) {
		// k is the x-value of the beginning of the keep
		final int k = x + 12;
		// overall castle dimensions:
		double vx = x + 1;
		double vy = y;
		double vw = k + 30 - x;
		double vh = 40.4;
		// make tile floor
		setTileType(k, y + 1, 30, 40, Tile.STONE_FLOOR);
		// prevent trees in courtyard
		for (int i = x + 2; i < k - 0.3; i++) {
			for (int j = y + 1; j < y + 39.4; j++) {
				tiles[i][j].canHaveTree = false;
			}
		}
		// keep front entrance
		Wall.make(k, y + 1, 0, 39.4, 0.6, 2);
		// infirmary:
		Wall.make(k + 11, y + 1, 0, 11, 0.6, 0);
		Wall.make(k + 0.3, y + 11.7, 10.4, 0, 0.6, 1.5);
		// lounge:
		Wall.make(k + 20, y + 1, 0, 11, 0.6, 0);
		Wall.make(k + 11.3, y + 11.7, 8.4, 0, 0.6, 1.5);
		// storage/workshop:
		Wall.make(k + 20.3, y + 11.7, 9.7, 0, 0.6, 1.5);
		// great hall/kitchen/pantry:
		Wall.make(k + 2.3, y + 13.7, 0, 14, 0.6, 1.5);
		Wall.make(k + 2.6, y + 14, 25.1, 0, 0.6, 0);
		Wall.make(k + 2.6, y + 27.4, 25.1, 0, 0.6, 0);
		Wall.make(k + 28, y + 13.7, 0, 14, 0.6, 1.5);
		// pantry
		Wall.make(k + 16, y + 14, 0, 4.7, 0.6, 0);
		Wall.make(k + 16.3, y + 18.4, 11.4, 0, 0.6, 1.5);
		// kitchen
		Wall.make(k + 16, y + 18.7, 0, 4.2, 0.6, 1.5);
		Wall.make(k + 16, y + 22.9, 0, 4.2, 0.6, 1.5);
		// common bedroom
		Wall.make(k + 0.3, y + 29.7, 11.4, 0, 0.6, 1.5);
		Wall.make(k + 12, y + 29.4, 0, 11, 0.6, 0);
		// second common bedroom
		Wall.make(k + 12.3, y + 29.7, 11.4, 0, 0.6, 1.5);
		Wall.make(k + 24, y + 29.4, 0, 11, 0.6, 0);
		// master bedroom:
		Wall.make(k + 24.3, y + 29.7, 5.7, 0, 0.6, 1.5);
		// outer walls
		Wall.make(k + 30.5, y + 1, 0, 39.4, 1, 0);
		Wall.make(x + 1, y + 0.5, 30 + (k - x), 0, 1, 0);
		Wall.make(x + 1, y + 40.9, 30 + (k - x), 0, 1, 0);
		// front gate
		Wall.make(x + 1.5, y + 1, 0, 39.4, 1, 2);

		// four invisible guys in courtyard
		// They are "summoned" if you choose to spare the summoner
		final ArrayList<Navigator> reinforcements = new ArrayList<Navigator>(7);
		// front entrance is at (k, y+20.7)
		reinforcements.add(new Fencer(player));
		reinforcements.add(new Fencer(player));
		reinforcements.add(new Fencer(player));
		reinforcements.add(new Fencer(player));
		double a = Math.PI / 6.0;
		for (Navigator nav : reinforcements) {
			nav.friends = reinforcements;
			nav.x = k - 4 - (4 * Math.sin(a));
			nav.y = y + 20.7 + (4 * Math.cos(a));
			nav.direction = -a;
			a += Math.PI / 6.0;
			if (Math.abs(a - Math.PI * 0.5) < 0.01) {
				// leave the center point for the summoner
				a += Math.PI / 6.0;
			}
		}

		// courtyard
		double spacing = 1.5;
		for (double i = x + 2 + spacing + 0.5; i < k - (0.3 + spacing + 0.5); i += 1 + spacing) {
			new Bush(i, y + 8, 1, 10);
			new Bush(i, y + 32.4, 1, 10);
		}
		// the wandering gardener
		// randomly among the north or south bushes
		Gardener gardener = new Gardener(player, x + 4 + (spacing * 2.5), y + 8 + (Math.random() < 0.5 ? 0 : 24.4));
		gardener.hiding = true;
		gardener.tx = gardener.x;
		gardener.ty = gardener.y;
		gardener.wx = gardener.x;
		gardener.wy = gardener.ty - 5;
		gardener.ww = 0;
		gardener.wh = 10;
		gardener.intention = Navigator.WANDER;
		gardener.action = SolidActor.STAND;
		gardener.state = 0;
		gardener.behavior = Navigator.WANDER_UNTIL_DETECT_THEN_ATTACK;
		gardener.setVisibilityArea(vx, vy, vw, vh);

		// great hall tables and pillar
		new TableWithPlates(k + 9, y + 17, 8, 2, 0.5);
		new TableWithPlates(k + 9, y + 24, 8, 2, 0.5);
		new Pillar(1, k + 7.15, y + 20.7);
		new Pillar(1, k + 11.15, y + 20.7);

		// kitchen
		Cauldron caul = new Cauldron(k + 26.7, y + 26, 1.2);
		new TableWithKnives(k + 21.4, y + 23, 2, 5);
		ArrayList<Navigator> cooks = new ArrayList<Navigator>(2);
		cooks.add(new Fencer(player, caul.x - 1.5, caul.y, Math.PI * 1.5));
		cooks.add(new Fencer(player, caul.x, caul.y - 1.5, Math.PI));
		for (Navigator cook : cooks) {
			cook.friends = cooks;
			cook.hiding = true;
			cook.setVisibilityArea(vx, vy, vw, vh);
		}

		// pantry
		new Shelf(k + 22, y + 14.8, 11.4, 1);

		// standard bed length
		double bedLength = 3.7;

		// first common bedroom
		new Bed(k + 2.5, y + 38, 2, bedLength, Math.PI);
		new Bed(k + 6, y + 38, 2, bedLength, Math.PI);
		new Bed(k + 9.5, y + 38, 2, bedLength, Math.PI);

		// second common bedroom
		new Bed(k + 14.5, y + 38, 2, bedLength, Math.PI);
		new Bed(k + 18, y + 38, 2, bedLength, Math.PI);
		new Bed(k + 21.5, y + 38, 2, bedLength, Math.PI);

		// infirmary
		new TableWithSurgeryTools(k + 9.7, y + 8.9, 2, 5);
		new Bed(k + 2, y + 9.4, 2, bedLength, Math.PI);
		new Bed(k + 2, y + 3, 2, bedLength, 0);
		new Bed(k + 5.5, y + 3, 2, bedLength, 0);
		new Bed(k + 9, y + 3, 2, bedLength, 0);

		// lounge
		final TableWithShard tableWithShard = new TableWithShard(k + 15.5, y + 6.2, 2, 4);

		// hallway guy
		Fencer hallMonitor = new Fencer(player, k + 15.5, y + 12.85, Math.PI * 0.5);
		hallMonitor.hiding = true;
		hallMonitor.vx = k + 0.3;
		hallMonitor.vw = 29.7;
		hallMonitor.vy = y + 1;
		hallMonitor.vh = 13;
		hallMonitor.setDetectionRange(10);

		// master bedroom
		new Bed(k + 27.15, y + 38, 2, bedLength, Math.PI);
		// summoner
		CastleSummoner summoner = new CastleSummoner(player, reinforcements, gardener, actors);
		summoner.setVisibilityArea(vx, vy, vw, vh);
		summoner.place(k + 28.75, y + 38);
		// (tx,ty) is the point to which the summoner goes after summoning
		summoner.tx = k - 8;
		summoner.ty = y + 20.7;

		// workshop/storage
		new ShelfWithTools(k + 25.15, y + 1.5, 9.7, 1);
		new TableWithTools(k + 25, y + 6.4, 6, 2);
		Fencer smith = new Fencer(player, k + 25, y + 4.7, Math.PI);
		smith.hiding = true;
		smith.setVisibilityArea(vx, vy, vw, vh);
	}

	/**
	 * The stalkingArea is a larger area with a smaller "center" area within. While
	 * you explore the larger area, enemies will "stalk" you, remaining just on the
	 * edge of the screen, but not engaging unless cornered.
	 * 
	 * As you approach the center, someone will approach you and talk. Then, you
	 * must fight.
	 * 
	 * The stalkers should have separate bedroom huts
	 **/

	public void makeStalkingArea(int x, int y) {
		final int h = 50;
		final int w = 50;
		final int areax = x;
		final int areay = y;
		final ArrayList<Navigator> stalkers = new ArrayList<Navigator>(6);

		// make stalkers near each corner of the area
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				Stalker s = new Stalker(player, x + 1 + ((w - 2) * i), y + 1 + ((h - 2) * j), areax, areay, w, h);
				stalkers.add(s);
				s.friends = stalkers;
			}
		}
		// the stalkerTalker is the stalker that talks to you.
		StalkerTalker stalkerTalker = new StalkerTalker(player, x + (w / 2), y + (h / 2), stalkers);
		stalkerTalker.pathFindingIterations = 80;
		stalkerTalker.vx = x + (w / 2) - 4;
		stalkerTalker.vw = 9;
		stalkerTalker.vy = y + (h / 2) - 6;
		stalkerTalker.vh = 16.8;
		stalkerTalker.tx = stalkerTalker.vx + 7.3;
		stalkerTalker.ty = stalkerTalker.vy + 4.5;
		stalkerTalker.hiding = true;
		// Make the growing field
		for (int i = 0; i < 4; i++) {
			new SpecialBush(x + (w / 2) - 3.3 + (2.54 * i), y + (h / 2) + 5, 0.9, 8.8);
		}
		// make the building and courtyard wall
		makeBuilding(x + (w / 2) - 4, y + (h / 2) - 6, 9, 5, 0.3, false, false, true, false);
		// intrabuilding wall
		Wall.make(x + (w / 2) + 2.5, y + (h / 2) - 4.6, 0, 3.6, 0.3, 0);
		TableWithFlowers tflow = new TableWithFlowers(x + (w / 2) - 1.3, y + (h / 2) - 5, 4.1, 1.2);
		tflow.direction = Math.PI; // decided to orient it the other way
		new Cauldron(x + (w / 2) - 2.2, y + (h / 2) - 2.3, 1);
		new TableWithShard(x + (w / 2) + 4.5, y + (h / 2) - 1.5, 1, 1);
		Wall.make(x + (w / 2) - 4.05, y + (h / 2) - 0.7, 0, 12, 0.5, 0);
		Wall.make(x + (w / 2) + 5.05, y + (h / 2) - 0.7, 0, 12, 0.5, 0);
		Wall.make(x + (w / 2) - 4, y + (h / 2) + 11.05, 9, 0, 0.5, 1.4);
		// Prevent any trees from being placed in the courtyard
		for (int i = x + (w / 2) - 4; i < x + (w / 2) + 5; i++) {
			for (int j = y + (h / 2) - 1; j < y + (h / 2) + 11; j++) {
				tiles[i][j].canHaveTree = false;
			}
		}
	}

	// This places a single spider with some trees.
	// It gives you an opportunity to learn about spiders before
	// get swarmed in the abandoned city
	public void makeSingleSpider(int x, int y, int w, int h) {
		// place a spider somewhere in here, randomly
		Spider spider = new Spider(player, x + 1 + (Math.random() * (w - 2)), y + 1 + (Math.random() * (h - 2)));
		spider.hiding = true;
		spider.setVisibilityArea(x, y, w, h);
		fillWithTrees(x, y, w, h, 1.9);
	}

	public static void toggleCombatMode() {
		tutorialEvent(3);
		player.toggleState();
		player.walks = true;
		timeSpeed = 1.5 - timeSpeed;
	}

	// this right here is what happens every game cycle. This "does" the game!
	public void actionPerformed(ActionEvent e) {
		// actors act
		for (Actor a : actors) {
			a.act();
		}
		drawScreen();
		// resetters reset
		for (Resetter r : resetters) {
			r.reset();
		}
		resetters.clear();
	}

	// this is the testing version of actionPerformed
	// Use it to measure timing
	/**
	 * public void actionPerformed(ActionEvent e){ //actors act long t1 =
	 * System.currentTimeMillis(); for (Actor a : actors){ a.act(); } long t2 =
	 * System.currentTimeMillis(); drawScreen(); long t3 =
	 * System.currentTimeMillis(); //resetters reset for (Resetter r : resetters){
	 * r.reset(); } resetters.clear(); long t4 = System.currentTimeMillis(); if
	 * (narration){ System.out.println("acting: "+(t2-t1));
	 * System.out.println("drawing: "+(t3-t2)); System.out.println("resetting:
	 * "+(t4-t3)); narration = false; } }
	 **/

	/**
	 * Beware of using the following keys: z, c, n, a, s, e, y, u, i, o l also seems
	 * bad They can cause Java to stop receiving key events! This issue can
	 * apparently be resolved via a command in the Terminal, but requiring that
	 * means requiring other users to do something tedious
	 **/
	public static boolean[] keys = new boolean[1000];

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key < keys.length) {
			keys[key] = true;
		}
		if (key == KeyEvent.VK_N) {
			// used for testing
			narration = !narration;
		}
		if (key == KeyEvent.VK_C && globals.haveSword) {
			toggleCombatMode();
		}
		// used for testing.
		if (key == KeyEvent.VK_O) {
			player.speed = 0.6 - player.speed;
			player.walks = !player.walks;
		}
		if (key == KeyEvent.VK_P) {
			getResponse("Game paused.", resumeButton, saveButton);
		}
		if (key == KeyEvent.VK_SPACE) {
			// space: interact if not fighting; attack if fighting
			if (player.intention == SolidActor.ATTACK && player.action != SolidActor.ATTACK) {
				player.nextAction = SolidActor.ATTACK;
			} else if (player.intention == SolidActor.WANDER) {
				// interact with what is before you
				double targetx = -(0.8) * Math.sin(player.direction);
				double targety = -(0.8) * Math.cos(player.direction);
				Solid target = Game.getIntersectingSolid(player, player.x, player.y, targetx, targety);
				if (target != player) {
					target.interact();
				}
			}
		}
		if (key == KeyEvent.VK_F && player.intention == SolidActor.ATTACK && player.action != SolidActor.FEINT) {
			player.nextAction = SolidActor.FEINT;
		}
		if (key == KeyEvent.VK_R && player.intention == SolidActor.ATTACK && player.action != SolidActor.PARRY) {
			player.nextAction = SolidActor.PARRY;
		}
	}

	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if (key < keys.length) {
			keys[key] = false;
		}
	}

	public void keyTyped(KeyEvent e) {}

	// the image of the compass, before rotation
	final static BufferedImage compass0 = ImageMaker.get("compass.png");
	// how much the original image gets scaled to fit onto the screen
	final static double compassScaleFactor = 1.0 * healthBarWidth / compass0.getWidth();

	// drawScreen draws the screen (obviously)
	public static void drawScreen() {
		double x = player.x;
		double y = player.y;
		relevantSolids.clear();

		// consider the tiles to be drawn
		int xmin = (int) (x - (0.75 * screenWidth));
		xmin = Math.max(xmin, 0);
		int ymin = (int) (y - (0.75 * screenHeight));
		ymin = Math.max(ymin, 0);
		int xmax = (int) (x + (0.75 * screenWidth) + 1);
		xmax = Math.min(xmax, worldWidth);
		int ymax = (int) (y + (0.75 * screenHeight) + 1);
		ymax = Math.min(ymax, worldHeight);

		// everything gets drawn to invisibleImage.
		// Then it gets rotated to make visibleImage, which is actually drawn to the
		// screen
		Graphics2D graphics = invisibleImage.createGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, frameWidth * 3 / 2, frameHeight * 3 / 2);

		// go through these tiles in order
		// each tile contributes an image
		// These are combined to form the screen, over which Solids are drawn
		for (int i = xmin; i < xmax; i++) {
			Tile[] tilesi = tiles[i];
			for (int j = ymin; j < ymax; j++) {
				// select the relevant Tile
				Tile thisTile = tilesi[j];
				// obtain its image
				BufferedImage newImage = thisTile.draw();
				// get the AffineTransform ready to move that image onto invisibleImage
				imageMover.setToTranslation((frameWidth075) + ((i - x) * tileWidth),
						(frameHeight075) + ((j - y) * tileHeight));
				imageMover.scale(1.0 * tileWidth / newImage.getWidth(), 1.0 * tileHeight / newImage.getHeight());
				// draw the Tile's image to invisibleImage
				graphics.drawRenderedImage(newImage, imageMover);
				// collect all Solids from this Tile
				for (Solid s : thisTile.solids) {
					// no need to rotate your image, just to rotate it back
					if (s != player && !relevantSolids.contains(s)) {
						relevantSolids.add(s);
					}
				}
			}
		}

		// Now the tiles have been drawn and the relevant solids have been collected
		// Time to draw the solids
		for (Solid s : relevantSolids) {
			if (s.hiding && !s.isVisible()) {
				// do not draw hiding things if you are not within their "visibility area"
				continue;
			}
			ScaledImage scaled = s.draw();
			if (scaled == ScaledImage.nullImage) {
				// if something returned nullImage, it does not want to be drawn
				continue;
			}
			// get the Solid's dimensions
			double sW = s.width;
			double sH = s.height;
			// get the image's dimensions
			int iW = scaled.image.getWidth();
			int iH = scaled.image.getHeight();
			// get the pre-rotation scaling
			double swc = scaled.widthCoefficient;
			double shc = scaled.heightCoefficient;

			/**
			 * image center at (iW*0.5, iH*0.5)
			 * 
			 * pre-rotation scaling: scale by (swc * tileWidth / iW) x (shc * tileHeight /
			 * iH) This makes the image the size of a tile if swc=shc=1, or something else,
			 * according to the pre-rotation scaling coefficients image center at
			 * (swc*tileWidth*0.5, shc*tileHeight*0.5)
			 * 
			 * rotation: if (direction != 0), rotate by (-s.direction) around point
			 * (swc*0.5*tileWidth, shc*0.5*tileHeight) image center at (swc*tileWidth*0.5,
			 * shc*tileHeight*0.5) (unchanged)
			 * 
			 * post-rotation scaling: now scale by width x height This makes the image fill
			 * the appopriate number of tiles image center at (width*swc*tileWidth*0.5,
			 * height*shc*tileHeight*0.5)
			 * 
			 * translation: the image's center should be at: (frameWidth075 + ((s.x-x) *
			 * tileWidth), frameHeight075 + ((s.y-y) * tileHeight)) Therefore, translation
			 * is that, minus the actual center: (frameWidth075 + ((s.x-x) * tileWidth) -
			 * (width*swc*tileWidth*0.5), frameHeight075 + ((s.y-y) * tileHeight) -
			 * (height*shc*tileHeight*0.5)) Factor out tileWidth/Height: (frameWidth075 +
			 * (tileWidth * (s.x - x - (width*swc*0.5))), frameHeight075 + (tileHeight *
			 * (s.y - y - (height*shc*0.5))))
			 **/
			double sWidth = s.width;
			double sHeight = s.height;
			imageMover.setToTranslation(frameWidth075 + (tileWidth * (s.x - x - (sWidth * swc * 0.5))),
					frameHeight075 + (tileHeight * (s.y - y - (sHeight * shc * 0.5))));
			imageMover.scale(sWidth, sHeight);
			double direction = s.direction;
			if (s.direction != 0) {
				imageMover.rotate(-direction, swc * 0.5 * tileWidth, shc * 0.5 * tileHeight);
			}
			imageMover.scale(swc * tileWidth / iW, shc * tileHeight / iH);
			// the transformation is finished. Apply it!
			graphics.drawRenderedImage(scaled.image, imageMover);
		}

		// Now invisibleImage is finished. That last step is to make visibleImage,
		// the rotated version.
		// Note that invisibleImage is 1.5-times longer in each dimension
		imageMover.setToTranslation(-0.25 * frameWidth, -0.25 * frameHeight);
		imageMover.rotate(player.direction, frameWidth075, frameHeight075);
		graphics = visibleImage.createGraphics();
		graphics.drawRenderedImage(invisibleImage, imageMover);
		// drew invisibleImage to visibleImage; now draw player
		ScaledImage scaled = player.draw();
		double w = player.width * scaled.widthCoefficient;
		double h = player.height * scaled.heightCoefficient;
		imageMover.setToTranslation((frameWidth - (w * tileWidth)) * 0.5, (frameHeight - (h * tileHeight)) * 0.5);
		imageMover.scale(w * tileWidth / scaled.image.getWidth(), h * tileHeight / scaled.image.getHeight());
		graphics.drawRenderedImage(scaled.image, imageMover);
		// now rotate compass0 to make compass1, which gets drawn
		imageMover.setToRotation(player.direction, healthBarWidth / 2, healthBarWidth / 2);
		imageMover.scale(compassScaleFactor, compassScaleFactor);
		graphics = compass1.createGraphics();
		graphics.drawRenderedImage(compass0, imageMover);
		// the paintComponent method, called by repaint, should draw visibleImage
		panel.repaint();
	}
}