import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
import javax.swing.*;

public class Corpse extends Solid{
  private static BufferedImage image = getFetalPosition();
  private static boolean receivedMessage = false;
  
  private static BufferedImage getFetalPosition(){
    BufferedImage image = null;
    try {
      image = ImageIO.read(new File("fetalPosition.png"));
    }
    catch (Exception e){
      JOptionPane.showMessageDialog(null, "Error! Could not find fetalPosition.png");
      System.exit(0);
    }
    return image;
  }
  //set the corpse's dimensions
  //Good dimensions appear to be (1.3*0.63 x 1.3*0.96)
  public Corpse(double width, double height){
    super(width, height, Game.RECTANGLE);
  }
  public ScaledImage draw(){
    if (!receivedMessage && Game.distanceBetween2(Game.player.x, Game.player.y, x, y) < Game.screenWidth*0.2*Game.screenWidth){
      receivedMessage = true;
      Game.getResponse("You see a body. Approach it and press space to investigate.");
    }
    return ScaledImage.get(image);
  }
  public void interact(){
    if (Game.haveSword){
      Game.getResponse("You reinspect the corpse but find nothing of interest.");
    }
    else {
      Game.getResponse("You inspect the body, finding it to be dead.\nIts only valuable item is a sword, apparently still in good condition.",
                        new Game.DialogButton("Take sword"){public void press(){takeSword();}},
                        new Game.DialogButton("Be lame"){public void press(){leaveSword();}}
                        );
    }
  }
  private void takeSword(){
    Game.haveSword = true;
    shape = Game.NULL;
    Game.makePermanentMessage("You take the sword", "You many now press c to toggle combat mode.\n\n" +
                              "In combat mode, you move faster, but time slows down.\n" +
                              "Press space to attack, f to feint, and r to parry/riposte.\n\n" +
                              "You may close this window, or leave it up as a reminder."
                             );
  }
  private void leaveSword(){
    Game.getResponse("You leave the sword for someone cooler.");
  }
  public void hit(int damage){
    Game.getResponse("You stab the corpse. It continues to be dead.");
  }
}