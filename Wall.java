import java.awt.*;
import java.awt.image.*;
/**
This class is for stone walls. They have rectangular shapes!
Set where you want their corners to be and the game will
determine the appropriate location, width, and height
**/
public class Wall extends Solid{
  public static BufferedImage image = setUp();
  
  //this constructor makes a wall with corner (x,y) and dimensions (width,height)
  public Wall(double x, double y, double width, double height){
    super(width, height, Game.RECTANGLE);
    walks = false;
    moveTo(x + (width*0.5), y + (height*0.5), Game.getIntersectingTiles(this, x + (width*0.5), y + (height*0.5)));
  }
  
  /**
  This method makes a wall spanning from (x0,y0) to (x0+dx,y0+dx)
  Note that [(dx == 0) || (dy == 0)]
  A horizontal wall will have width exactly equal to dx, but height of "thickness,"
  and will be vertically centered on y
  If "door" is >0, it will place a doorway with width "door" in the middle of the wall
  **/
  public static void make(double x0, double y0, double dx, double dy, double thickness, double door){
    if (dy == 0){
      //horizontal wall
      if (door <= 0){
        new Wall(x0, y0 - (thickness*0.5), dx, thickness);
      }
      else {
        new Wall(x0, y0 - (thickness*0.5), (dx - door)*0.5, thickness);
        new Wall(x0 + ((dx + door)*0.5), y0 - (thickness*0.5), (dx - door)*0.5, thickness);
      }
    }
    else if (dx == 0){
      //wall is vertical
      if (door <= 0){
        new Wall(x0 - (thickness*0.5), y0, thickness, dy);
      }
      else {
        new Wall(x0 - (thickness*0.5), y0, thickness, (dy - door)*0.5);
        new Wall(x0 - (thickness*0.5), y0 + ((dy + door)*0.5), thickness, (dy - door)*0.5);
      }
    }
  }
  
  private static BufferedImage setUp(){
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    Graphics g = image.createGraphics();
    g.setColor(Color.DARK_GRAY);
    g.fillRect(0,0,1,1);
    return image;
  }
  
  public ScaledImage draw(){
    return ScaledImage.get(image, 1, 1);
  }
}