import java.awt.*;
import java.awt.image.*;
public class Pillar extends Solid{
  
  private BufferedImage image = setUp();
  //if you strike a pillar 100 times, your sword breaks
  private static int remainingStrikes = 100;
  
  private BufferedImage setUp(){
    BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.createGraphics();
    g.setColor(Color.DARK_GRAY);
    g.fillOval(0,0,100,100);
    g.setColor(Color.BLACK);
    g.fillOval(20,20,60,60);
    return image;
  }
  
  public Pillar(double width){
    super(width, width, Game.CIRCLE);
    walks = false;
  }
  
  public Pillar(double width, double x, double y){
    this(width);
    moveTo(x, y, Game.getIntersectingTiles(this, x, y));
  }
  
  public double getHeight(){
    //pillars are circles
    return width;
  }
  
  public ScaledImage draw(){
    return ScaledImage.get(image);
  }
  
  public void interact(){
    Game.getResponse("This is a pillar.");
  }
  
  public void hit(int damage){
    remainingStrikes--;
    if (remainingStrikes > 0){
      Game.getResponse("Your sword hits the pillar.\nIf you do this " + remainingStrikes + " more times," +
                       " your sword will break.");
    }
    else {
      Game.getResponse("Your sword breaks.");
      Game.haveSword = false;
      Game.toggleCombatMode();
      remainingStrikes = 100;
    }
  }
}