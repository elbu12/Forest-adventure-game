import java.awt.*;
import java.awt.image.*;
//this is aan empty tent

public class Tent extends Solid{
  private BufferedImage image = ImageMaker.get("tent.png");
  
  //the tent is not quite square; good dimenions are 93x85
  public Tent(double x, double y, double width, double height){
    super(width, height, Game.RECTANGLE);
    moveTo(x, y, Game.getIntersectingTiles(this, x, y));
  }
  
  public ScaledImage draw(){
    //the tent's cords stick out beyond its edge, hence the scaling
    return ScaledImage.get(image, 1.15, 1.16);
  }
  
  public void interact(){
    Game.getResponse("A simple tent. You see nothing valuable in it.");
  }
  
  public void hit(int damage){
    Game.getResponse("Your sword pokes a hole in the tent.");
  }
  
  //this part below is just for testing!
  public static void main(String[] args){
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB){
      Graphics g = createGraphics();
      
    };
    final int iterations = 9999999;
    long t0 = System.currentTimeMillis();
    for (int i=0; i<iterations; i++){
      Graphics g = img.createGraphics();
  //    g.setColor(Color.BLUE);
  //    g.fillRect(0,0,10,10);
    }
    long t1 = System.currentTimeMillis();
    System.out.println(t1-t0);
    t0 = System.currentTimeMillis();
    Graphics g = img.createGraphics();
    for (int i=0; i<iterations; i++){
   //   g.setColor(Color.BLUE);
    //  g.fillRect(0,0,10,10);
    }
    t1 = System.currentTimeMillis();
    System.out.println(t1-t0);
  }
}
  