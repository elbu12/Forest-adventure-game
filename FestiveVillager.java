import java.util.*;
import java.awt.*;
import java.awt.image.*;
/**
The festive villagers hang out at the festival. When the festival is
attacked, they will flee or be killed.
**/
public class FestiveVillager extends Navigator{
  static boolean haveBeenAlerted = false;
  static BufferedImage greenImage = Fencer.getBallAndStick(new Color(0, 100, 0))[0];
  protected BufferedImage image = greenImage;
  
  public String message;
  
  public FestiveVillager(Solid target, String message, double x, double y, double direction){
    super(1, 1);
    place(x, y);
    this.direction = direction;
    this.target = target;
    this.message = message;
    behavior = STAND_UNTIL_DETECT_THEN_FLEE;
    intention = STAND;
    action = STAND;
    state = 0;
    fastSpeed = 0.14;
    slowSpeed = 0.07;
    speed = slowSpeed;
    setDetectionRange(6);
    pathFindingIterations = 160;
  }
  public void reset(){
    Game.actors.remove(this);
    resetActions();
  }
  public void interact(){
    if (intention == FLEE){
      Game.getResponse("This person is terrified, hysterical.");
    }
    else {
      Game.getResponse(message);
    }
  }
  public void hit(int damage){
    if (damage == 0){
      Game.getResponse("To talk to a villager, press space when not in combat mode.");
    }
    else {
      super.hit(damage);
    }
  }
  
  public ScaledImage draw(){
    if (!haveBeenAlerted && Game.distanceBetween2(Game.player.x, Game.player.y, x, y) < 5*5){
      Game.getResponse("You find what appears to be some sort of festival. People are" +
                       "\neating, drinking, dancing. Music is playing.");
      haveBeenAlerted = true;
    }
    return ScaledImage.get(image);
  }
  public void act(){
    if (intention == FLEE && dieWhenFar(14)){
      //Once villagers begin fleeing, they will disappear if too far from you.
    }
    else {
      super.act();
    }
  }
}