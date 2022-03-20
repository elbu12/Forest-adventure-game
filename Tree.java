import java.awt.*;
import java.awt.image.*;

public class Tree extends Solid{
  
  //maxWidth and minWidth are for other methods that make trees.
  //A tree can be any width if that value is given to the constructor.
  final public static double maxWidth = 2;
  final public static double minWidth = 1;
  
  //each tree is a random color, though the spatial dimensions are determined by the call
  //to the constructor
  public Double shade = Math.random();
  public Color color = new Color(40+(int)(shade*80), 40+(int)(shade*80),(int)(Math.random()*40));
  
  private BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
  
  public Tree(){
    this(minWidth + (Math.random()*(maxWidth-minWidth)));
  }
  
  public Tree(double width){
    super(width, width, Game.CIRCLE);
    Graphics g = image.createGraphics();
    g.setColor(color);
    g.fillOval(0,0,100,100);
    g.setColor(Color.BLACK);
    g.fillOval(20,20,60,60);
    walks = false;
  }
  
  public Tree(double width, double x, double y){
    this(width);
    moveTo(x, y, Game.getIntersectingTiles(this, x, y));
  }
  
  public double getHeight(){
    //trees are circles
    return width;
  }
  
  public ScaledImage draw(){
    return ScaledImage.get(image);
  }
  
  public void interact(){
    Game.getResponse("This is a tree. It's a cut-away view.");
  }
}