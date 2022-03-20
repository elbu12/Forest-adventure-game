import java.awt.*;
import java.awt.image.*;
/**
Note: this table is assumed to be vertically oriented!
If horizontal, the knives may need to be relocated
**/
public class TableWithKnives extends Table{
  protected BufferedImage image;
  
  public TableWithKnives(double x, double y, double width, double height){
    super(x, y, width, height);
    moveTo(x, y, Game.getIntersectingTiles(this, x, y));
    image = new BufferedImage((int)(Game.tileWidth*width), (int)(Game.tileHeight*height), BufferedImage.TYPE_INT_ARGB);
    int w = image.getWidth(); //actual pixel width of image
    int h = image.getHeight(); //actual pixel height of image
    Graphics g = image.createGraphics();
    //the table
    g.setColor(TABLE);
    g.fillRoundRect(0, 0, w, h, w/5, h/5);
    //the knives
    g.setColor(Color.BLACK);
    g.fillRect(w-22, (h/2), 12, 3);
    g.fillRect(w-25, (h/2)+15, 12, 3);
    g.setColor(Color.WHITE);
    g.fillRect(w-37, (h/2), 15, 4);
    g.fillRect(w-43, (h/2)+15, 18, 3);
  }
  
  public ScaledImage draw(){
    return ScaledImage.get(image);
  }
  
  public void interact(){
    Game.getResponse("This table has some dirty knives on it.");
  }
}