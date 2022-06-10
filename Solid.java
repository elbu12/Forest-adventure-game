import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.Serializable;

import javax.swing.*;
import java.util.*;

//Note that Solid locations indicate their center, not corner

public abstract class Solid extends Obstacle implements Serializable {

	private static final long serialVersionUID = 1L;

	public transient ArrayList<Tile> tiles = new ArrayList<Tile>();
	public double x;
	public double y;
	public double width;
	public double height;
	public int shape;
	public double direction = 0;
	// walks is mostly relevant for things that move
	public boolean walks;
	// The "enemies" collection is for Navigators that react to multiple "targets;"
	// Those that are set to flee will flee from detectable Solids in the set.
	// public Collection <Solid> enemies;

	public Solid(double width, double height, int shape) {
		/**
		 * note that the constructor does not ask for x/y values. When a solid is
		 * placed, appropriate tiles should be connected. Do this with moveTo
		 **/
		this.width = width;
		this.height = height;
		this.shape = shape;
	}

	public Solid(double width, double height, int shape, double direction) {
		this(width, height, shape);
		this.direction = direction;
	}

	final public double getx() {
		return x;
	}

	final public double gety() {
		return y;
	}

	final public double getWidth() {
		return width;
	}

	// getHeight is not final because trees override it
	public double getHeight() {
		return height;
	}

	final public int getShape() {
		return shape;
	}

	final public boolean isTile() {
		return false;
	}

	// note that a direction of zero implies something is facing UPWARD
	// angles are measured counterclockwise

	// walks determines whether this solid is obstructed by unwalkable tiles

	// disconnectTiles removes this from all containing tiles, then clears tiles.
	// Use it before changing location
	public void disconnectTiles() {
		for (Tile t : tiles) {
			t.solids.remove(this);
		}
		tiles.clear();
	}

	// connectTiles adds intersectingTiles to this and adds this to the tiles
	// use it after changing location
	// should this throw an Exception?
	public void connectTiles(Collection<Tile> intersectingTiles) {
		if (tiles.size() > 0) {
			JOptionPane.showMessageDialog(null,
					"Warning! Attempting to connect tiles to a solid with a non-empty tiles Collection!");
		}
		for (Tile t : intersectingTiles) {
			t.solids.add(this);
		}
		tiles.addAll(intersectingTiles);
	}

	// Note: moveTo should be accompanied by disconnecting and reconnecting relevant
	// tiles!
	private void moveTo(double x, double y) {
		this.x = x;
		this.y = y;
	}

	// Call this version
	public void moveTo(double x, double y, Collection<Tile> intersectingTiles) {
		disconnectTiles();
		moveTo(x, y);
		connectTiles(intersectingTiles);
	}

	// Things that are "hittable" get hit with this.
	// Currently, damage is irrelevant, but may be used later.
	public void hit(int damage) {
	}

	// you may be able to interact with some things
	public void interact() {
	}

	// SolidActors that react to their opponents use this to determine their own
	// action
	public int getAction() {
		return SolidActor.STAND;
	}

	// when Navigators try to find a path to a point, their algorithm will iterate
	// this many times before giving up
	public int pathFindingIterations;

	// riposte is called when something attacks this, but is successfully parried
	public void riposte(Solid s) {
	}

	// used for calculating angles
	final static double tau = 2 * Math.PI;

	// the getAngle methods return the angle from this solid's direction to the
	// argument's implied location. Ie it is (the absolute value of) how much
	// this solid must turn to face the given point
	public double getAngle(double ax, double ay) {
		// get the raw angle
		double angle = direction - Angle.get(ax - x, ay - y);
		// make positive
		while (angle < 0) {
			angle += tau;
		}
		if (angle > Math.PI) {
			return tau - angle;
		}
		return angle;
	}

	public double getAngle(Solid s) {
		return getAngle(s.x, s.y);
	}

	/**
	 * Every Solid has a "draw" method that returns a ScaledImage to be drawn. Some
	 * Solids are "hidden," meaning they will not be visible unless the player is
	 * within a certain "visibility area," designated by vx,vy and vw,vh.
	 * 
	 * Such Solids also have the boolean hiding == true
	 * 
	 * isVisible() tests if Game.player is within the visibility area
	 **/
	protected double vx;
	protected double vy;
	protected double vw;
	protected double vh;

	public void setVisibilityArea(double vx, double vy, double vw, double vh) {
		this.vx = vx;
		this.vy = vy;
		this.vw = vw;
		this.vh = vh;
	}

	boolean hiding = false;

	public boolean isVisible() {
		return (Game.player.x >= vx && Game.player.x < vx + vw && Game.player.y >= vy && Game.player.y < vy + vh);
	}

	abstract ScaledImage draw();
}