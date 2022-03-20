import java.awt.*;
import java.awt.image.*;
/**
This is a blackberry bush. Give it width and height to create a rectangle;
give it just width to create a circle
**/
public class Bush extends Solid{
  BufferedImage image;
  //constructor for rectangles
  public Bush(double x, double y, double width, double height){
    super(width, height, Game.RECTANGLE);
    moveTo(x, y, Game.getIntersectingTiles(this, x, y));
    image = new BufferedImage((int)(Game.tileWidth*width), (int)(Game.tileHeight*height), BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.createGraphics();
    for (int i=0; i<width*height*240; i++){
      g.setColor(new Color(0, (int)(Math.random()*170), 0));
      int xx = 6 + (int)(Math.random()*(image.getWidth()-12));
      int yy = 6 + (int)(Math.random()*(image.getHeight()-12));
      if (Math.random() < 0.5){
        g.fillOval(xx-6, yy-3, 12, 6);
      }
      else {
        g.fillOval(xx-3, yy-6, 6, 12);
      }
    }
  }
  //constructor for circles
  public Bush(double x, double y, double width){
    super(width, width, Game.CIRCLE);
    moveTo(x, y, Game.getIntersectingTiles(this, x, y));
    image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.createGraphics();
    for (int i=0; i<190*width*width; i++){
      g.setColor(new Color(0, (int)(Math.random()*170), 0));
      double rr = Math.random();
      double aa = 6 + (int)(Math.random()*88);
      int xx = (int)(rr*image.getWidth()*0.5*Math.cos(aa)) + (image.getWidth()/2);
      int yy = (int)(rr*image.getWidth()*0.5*Math.sin(aa)) + (image.getWidth()/2);
      if (Math.random() < 0.5){
        g.fillOval(xx-6, yy, 12, 6);
      }
      else {
        g.fillOval(xx, yy-6, 6, 12);
      }
    }
  }
  public ScaledImage draw(){
    return ScaledImage.get(image);
  }
  private static Game.DialogButton[] buttons = new Game.DialogButton[] {
    new Game.DialogButton("Ok"),
      new Game.DialogButton("Eat one"){
      public void press(){
        if (Math.random() < 0.1){
          Game.getResponse("That one was unripe.");
        }
        else {
          Game.getResponse("Delicious!");
        }
      }
    }
  };
  public void interact(){
    Game.getResponse("A blackberry bush.", buttons);
  }
}