import java.awt.*;
import java.awt.image.*;

public class Spider extends Navigator{
  //the setUp method makes the image(s)
  private static BufferedImage image = ImageMaker.get("spider.png");
  
  public Spider(Solid target){
    super(1, 1);
    fastSpeed = 0.16;
    slowSpeed = 0.04;
    behavior = STAND_UNTIL_DETECT_THEN_ATTACK;
    fightingStyle = APPROACH_THEN_WAIT_THEN_ATTACK;
    intention = STAND;
    resetActions();
    state = 0;
    speed = slowSpeed;
    attackTime = 2;
    recoverTime = 1;
    attackRange = 0.11;
    setDetectionRange(6);
    this.target = target;
  }
  public Spider(Solid target, double x, double y){
    this(target);
    if (!place(x, y)){
      System.out.println("failed to place at "+x+", "+y);
    }
  }
  public ScaledImage draw(){
    return ScaledImage.get(image, 1, 1);
  }
}