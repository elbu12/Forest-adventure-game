import java.awt.*;
import java.awt.image.*;
//this is a cold, used fire pit

public class FirePit extends Solid{
  private BufferedImage image = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);
  
  public FirePit(double x, double y, double width){
    super(width, width, Game.CIRCLE);
    moveTo(x, y, Game.getIntersectingTiles(this, x, y));
    Graphics g = image.createGraphics();
    //The pit
    g.setColor(Color.BLACK);
    g.fillOval(5,5,90,90);
    //The stones
    for (int i=0; i<18; i++){
      int shade = 32 + (int)(Math.random()*128);
      g.setColor(new Color(shade, shade, shade));
      g.fillOval(42 + (int)(42*Math.sin(Math.PI*2.0*i/18.0)), 42 + (int)(42*Math.cos(Math.PI*2.0*i/18.0)), 16, 16);
    }
    //the logs
    g.setColor(new Color(50, 40, 0));
    g.fillRoundRect(40, 34, 30, 13, 7, 7);
    g.fillRoundRect(34, 52, 30, 13, 7, 7);
  }
  
  public ScaledImage draw(){
    return ScaledImage.get(image);
  }
  
  public void interact(){
    Game.getResponse("The remains of a fire. This was recent.");
  }
}
  