/**
Obstacle refers collectively to Solids and Tiles.
**/

abstract class Obstacle {
  abstract double getx();
  abstract double gety();
  abstract double getWidth();
  abstract double getHeight();
  abstract int getShape();
  
  abstract boolean isTile();
}