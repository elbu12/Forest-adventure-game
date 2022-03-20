import java.awt.*;
import java.awt.image.*;

public class TableWithFood extends Table{
  
  public TableWithFood(double x, double y, double width, double height){
    super(x, y, width, height);
    int w = image.getWidth(); //actual pixel width of image
    int h = image.getHeight(); //actual pixel height of image
    Graphics g = image.createGraphics();
    //generate them randomly, but evenly spaced
    //each dish is 35x35, with 10 unit spacing
    int dishes = 0;
    for (int i=10; i<w-34; i+=45){
      for (int j=10; j<h-34; j+=45){
        //remember to increment "options" if adding another option!
        int options = 5;
        double dish;
        /**
         Guarantee at least one of each dish is present 
        **/
        if (dishes < options){
          dish = 1.0*dishes/options;
        }
        else {
          dish = Math.random();
        }
        int ii = i - 4 + (int)(Math.random()*8);
        int jj = j - 4 + (int)(Math.random()*8);
        if (dish < 1.0/options){
          //meat
          g.setColor(PLATE);
          g.fillOval(ii, jj, 35, 35);
          g.setColor(new Color(65 + (int)(Math.random()*10), 55 + (int)(Math.random()*10), 0));
          g.fillOval(ii+4, jj+4, 9, 27);
          g.fillOval(ii+13, jj+4, 9, 27);
          g.fillOval(ii+20, jj+4, 9, 27);
        }
        else if (dish < 2.0/options){
          //greens
          g.setColor(PLATE);
          g.fillOval(ii, jj, 35, 35);
          g.setColor(new Color(0, 80+(int)(Math.random()*30), 0));
          g.fillOval(ii+7, jj+5, 19, 19);
          g.fillOval(ii+5, jj+11, 19, 19);
          g.fillOval(ii+10, jj+11, 19, 19);
        }
        else if (dish < 3.0/options){
          //fruit
          g.setColor(new Color(90+(int)(Math.random()*30), 0, 0));
          g.fillOval(ii+2, jj+11, 12, 12);
          g.fillOval(ii+21, jj+2, 12, 12);
          g.fillOval(ii+18, jj+21, 12, 12);
        }
        else if (dish < 4.0/options){
          //starch
          g.setColor(PLATE);
          g.fillOval(ii, jj, 35, 35);
          int c = 170 + (int)(Math.random()*24);
          g.setColor(new Color(c, c, c));
          g.fillOval(ii+4, jj+4, 27, 27);
        }
        else if (dish < 5.0/options){
          //empty plate
          g.setColor(PLATE);
          g.fillOval(ii, jj, 35, 35);
        }
        dishes++;
      }
    }
  }
  
  public void interact(){
    Game.getResponse("Now is not the time to load up a plate with food.");
  }
}