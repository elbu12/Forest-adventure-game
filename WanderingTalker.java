import java.awt.*;
import java.awt.image.*;
/**
WanderingTalkers wander within a given area. When they see you, they will stop to talk.
"message" determines what they say.

They have an "image," which must be given to them. It is not assigned by the constructor!
**/
abstract class WanderingTalker extends Navigator{
  protected BufferedImage image;
  
  public String message;
  
  /**
  wx, wy, ww, wh refer to the corner and dimensions of the area in which this wanders.
  This will initially be placed in the exact center of this space.
  **/
  public WanderingTalker(Solid target, String message, double wx, double wy, double ww, double wh){
    super(1, 1);
    place(wx + (0.5*ww), wy + (0.5*wh));
    this.target = target;
    this.message = message;
    this.wx = wx;
    this.wy = wy;
    this.ww = ww;
    this.wh = wh;
    behavior = WANDER_UNLESS_CLOSE_THEN_STAND;
    intention = WANDER;
    tx = x;
    ty = y;
    action = STAND;
    state = 0;
    fastSpeed = 0.15;
    slowSpeed = 0.07;
    speed = slowSpeed;
    setDetectionRange(4);
  }
  public ScaledImage draw(){
    return ScaledImage.get(image, 1, 1);
  }
  public void reset(){
    Game.actors.remove(this);
    resetActions();
  }
  public void interact(){
    Game.getResponse(message);
  }
}