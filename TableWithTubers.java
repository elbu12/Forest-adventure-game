import java.awt.*;
import java.awt.image.*;

public class TableWithTubers extends Table{
  
  public TableWithTubers(double x, double y, double width, double height){
    super(x, y, width, height);
    int w = image.getWidth(); //actual pixel width of image
    int h = image.getHeight(); //actual pixel height of image
    Graphics g = image.createGraphics();
    //the tubers
    int[] xs = {w/2, w/2 - 20, w/2 + 15, w/2 - 10, w/2 + 12};
    int[] ys = {16, 12, 14, 28, 25};
    for (int i=0; i<xs.length; i++){
      g.setColor(new Color(240, 160+r(80), 100));
      g.fillOval(xs[i]-4, ys[i]-4, 6+r(5), 6+r(5));
    }
  }
  
  public void interact(){
    Game.getResponse("This table has some fresh tubers, ready to be cut and boiled.");
  }
  
  private int r(int n){
    return (int)(Math.random()*n);
  }
}