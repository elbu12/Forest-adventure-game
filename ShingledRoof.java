import java.awt.*;
import java.awt.image.*;

public class ShingledRoof extends Solid{
  
  final private static int shingleWidth = 16;
  final private static int shingleHeight = 8;
  
  final public static Color chimneyColor = new Color(160, 80, 0);
  
  private BufferedImage image;
  public ShingledRoof(double width, double height){
    super(width, height, Game.RECTANGLE);
    image = new BufferedImage((int)(width*Game.tileWidth), (int)(height*Game.tileHeight), BufferedImage.TYPE_INT_RGB);
    Graphics g = image.createGraphics();
    //shingles are in rows
    //the lower half is lighter
    for (int i=0; i<image.getHeight(); i += shingleHeight){
      int j0 = 0 - (int)(Math.random()*shingleHeight);
      for (int j=j0; j<image.getWidth(); j += shingleWidth){
        //pick a random shade of gray
        int shade = 40 + (int)(Math.random()*85);
        if (i > image.getHeight()/2){
          shade += 40;
        }
        Color color = new Color(shade, shade, shade);
        g.setColor(color);
        g.fillRect(j, i, shingleWidth, shingleHeight);
        g.setColor(Color.BLACK);
        g.drawRect(j, i, shingleWidth+1, shingleHeight+1);
      }
    }
    //now make a chimney
    g.setColor(chimneyColor);
    int topOfChimney = shingleHeight*2;
    g.fillRect((image.getWidth()/2) - (shingleWidth*3/2), topOfChimney, shingleWidth*3, shingleHeight*6);
    g.setColor(Color.BLACK);
    g.fillRect((image.getWidth()/2) - (shingleWidth*3/2) + 8, topOfChimney + 8, (shingleWidth*3) - 16, (shingleHeight*6) - 16);
  }
  public ScaledImage draw(){
    return ScaledImage.get(image, 1, 1);
  }
  public void hit(int damage){
    Game.getResponse("Your sword hits the house. The house survives.");
  }
}