import java.awt.*;
import java.awt.image.*;
/**
Note: this table is assumed to be vertically oriented!
If horizontal, the knives may need to be relocated
**/
public class TableWithSurgeryTools extends Table{
  protected BufferedImage image;
  
  public TableWithSurgeryTools(double x, double y, double width, double height){
    super(x, y, width, height);
    moveTo(x, y, Game.getIntersectingTiles(this, x, y));
    image = new BufferedImage((int)(Game.tileWidth*width), (int)(Game.tileHeight*height), BufferedImage.TYPE_INT_ARGB);
    int w = image.getWidth(); //actual pixel width of image
    int h = image.getHeight(); //actual pixel height of image
    Graphics g = image.createGraphics();
    //the table
    g.setColor(TABLE);
    g.fillRoundRect(0, 0, w, h, w/5, h/5);
    //the scalpel
    g.setColor(Color.WHITE);
    g.fillRect(22, (h/4), 15, 3);
    g.fillRect(37, (h/4), 1, 2);
    g.fillRect(38, (h/4), 1, 1);
    //the suturing needle
    g.drawOval(25, (3*h/8), 7, 6);
    g.setColor(TABLE);
    g.fillRect(28, (3*h/8), 7, 6);
    //the scissors/pliers/whatever
    g.setColor(Color.WHITE);
    for (int i=0; i<3; i++){
      g.drawLine(20+i, (h/2)+4, 35+i, (h/2)+11);
      g.drawLine(20+i, (h/2)+11, 35+i, (h/2)+4);
    }
    g.drawOval(19, (h/2), 4, 4);
    g.drawOval(19, (h/2)+11, 4, 4);
  }
  
  public ScaledImage draw(){
    return ScaledImage.get(image);
  }
  
  public void interact(){
    Game.getResponse("On this table you see a scalpel, a curved needle, and scissors.");
  }
}