import java.awt.*;
import java.awt.image.*;

public class Table extends Solid{
  protected BufferedImage image;
  
  final protected static Color TABLE = new Color(80,70,0);
  final protected static Color PLATE = new Color(125, 115, 105);
  
  
  public Table(double x, double y, double width, double height){
    super(width, height, Game.RECTANGLE);
    moveTo(x, y, Game.getIntersectingTiles(this, x, y));
    image = new BufferedImage((int)(Game.tileWidth*width), (int)(Game.tileHeight*height), BufferedImage.TYPE_INT_ARGB);
    int w = image.getWidth(); //actual pixel width of image
    int h = image.getHeight(); //actual pixel height of image
    Graphics g = image.createGraphics();
    //the table
    g.setColor(TABLE);
    g.fillRoundRect(0, 0, w, h, w/5, h/5);
  }
  
  public ScaledImage draw(){
    return ScaledImage.get(image);
  }
  
  public void interact(){
    Game.getResponse("This is a table.");
  }
}