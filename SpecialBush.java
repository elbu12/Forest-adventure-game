import java.awt.*;
public class SpecialBush extends Bush{
  //This bush has flowers!
  final static Color outerColor = Color.ORANGE;
  final static Color innerColor = Color.RED;
  public SpecialBush(double x, double y, double width, double height){
    super(x, y, width, height);
    Graphics g = image.createGraphics();
    //add flowers
    for (int i=0; i<width*height*2; i++){
      int fW = 6 + (int)(Math.random()*4); //flower width
      int fX = 10 + (int)(Math.random()*(image.getWidth()-20-fW));  //flower x
      int fY = 10 + (int)(Math.random()*(image.getHeight()-20-fW)); //flower y
      g.setColor(outerColor);
      g.fillOval(fX, fY, fW, fW);
      g.setColor(innerColor);
      g.fillOval(fX+2, fY+2, fW-4, fW-4);
    }
  }
  public void interact(){
    Game.getResponse("This shrub is flowering.");
  }
  public void hit(int damage){
    Game.getResponse("Your sword disturbs the plant.");
  }
}