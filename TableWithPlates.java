import java.awt.*;
import java.awt.image.*;

public class TableWithPlates extends Table{
  
  public TableWithPlates(double x, double y, double width, double height, double plateProbability){
    super(x, y, width, height);
    int w = image.getWidth(); //actual pixel width of image
    int h = image.getHeight(); //actual pixel height of image
    Graphics g = image.createGraphics();
    g.setColor(PLATE);
    //generate them randomly, but evenly spaced
    //each plate is 35x35, with 10 unit spacing
    //place every 2 of 3
    int placed = 0;
    for (int i=10; i<w-34; i+=45){
      for (int j=10; j<h-34; j+=45){
        //each place randomly has a plate or not
        if (placed < 2 || Math.random() < plateProbability){
          int ii = i - 4 + (int)(Math.random()*8);
          int jj = j - 4 + (int)(Math.random()*8);
          g.fillOval(ii, jj, 35, 35);
          placed++;
        }
      }
    }
  }
  //use this constructor if you do not wish to specify plate probability
  public TableWithPlates(double x, double y, double width, double height){
    this(x, y, width, height, 0.6);
  }
  
  public void interact(){
    Game.getResponse("This table has only empty, dirty plates.");
  }
  
}