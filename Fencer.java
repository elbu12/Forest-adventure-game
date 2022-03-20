import java.awt.*;
import java.awt.image.*;

public class Fencer extends Navigator{

  protected static BufferedImage[] blueImages = getBallAndStick(Color.BLUE);
  protected BufferedImage[] images = blueImages;
  
  //this constructor randomizes the fightingStyle
  public Fencer(Solid target){
    this(IMMEDIATE_ATTACK, target);
    double r = Math.random();
    if (r < 1.0/3.0){
      fightingStyle = COUNTERATTACK;
    }
    else if (r < 2.0/3.0){
      fightingStyle = FEINT_THEN_ATTACK;
    }
  }
  
  public Fencer(int fightingStyle, Solid target, Color col){
    this(fightingStyle, target);
    images = getBallAndStick(col);
  }
  
  public Fencer(int fightingStyle, Solid target){
    super(1, 1);
    this.fightingStyle = fightingStyle;
    this.target = target;
    fastSpeed = 0.15;
    slowSpeed = 0.07;
    behavior = STAND_UNTIL_DETECT_THEN_ATTACK;
    intention = STAND;
    resetActions();
    state = 0;
    speed = slowSpeed;
    attackTime = 5;
    feintTime = 3;
    parryTime = 5;
    recoverTime = 4;
    attackRange = 1;
    setDetectionRange(5.8);
  }
  public Fencer(Solid target, double x, double y){
    this(target);
    place(x, y);
  }
  public Fencer(Solid target, double x, double y, double direction){
    this(target, x, y);
    this.direction = direction;
  }
  public Fencer(int fightingStyle, Solid target, double x, double y){
    this(fightingStyle, target);
    place(x, y);
  }
  //ballAndStick refers to the overhead view of "fencers"
  //The name is left over from when they were literally ball-and-stick models
  public static BufferedImage[] getBallAndStick(Color color){
    BufferedImage squareStanceImage = null;
    BufferedImage sideStanceImage = null;
    BufferedImage[] ballAndStickImages = new BufferedImage[12];
    /**
    Produces an overhead view of a person.
    The given color is the color of their shoulders.
    Images are as follows:
      0: Idle, facing forward
      1: Idle, sideways
      2-6: sword coming out
      7-11: sword parrying
      **/
    //square stance
    squareStanceImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics g = squareStanceImage.createGraphics();
    g.setColor(color);
    g.fillOval(2, 30, 96, 40);
    g.setColor(Color.BLACK);
    g.fillOval(32, 24, 36, 36);
    g.setColor(Color.YELLOW);
    g.drawOval(0,0,100,100);
    g.drawOval(1,1,98,98);
    //side stance
    sideStanceImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    g = sideStanceImage.createGraphics();
    g.setColor(color);
    g.fillOval(30, 2, 40, 96);
    g.setColor(Color.BLACK);
    g.fillOval(30, 29, 36, 36);
    g.setColor(Color.YELLOW);
    g.drawOval(0,0,100,100);
    g.drawOval(1,1,98,98);
    ballAndStickImages[0] = squareStanceImage;
    ballAndStickImages[1] = sideStanceImage;
    for (int i=2; i<12; i++){
      BufferedImage image = new BufferedImage(100, 300, BufferedImage.TYPE_INT_ARGB);
      g = image.createGraphics();
      g.setColor(Color.WHITE);
      if (i < 7){
        //thrusting images
        int l = i-1;
        g.fillRect(46, 100 - (20*l), 8, 20*l);
        g.drawImage(sideStanceImage, 0, 100, null);
        ballAndStickImages[i] = image;
      }
      else {
        //parrying images
        int tx = 0;
        int ty = 0;
        switch (i){
          case 11:
            tx = 20;
            ty = 75;
            break;
          case 10:
            tx = 35;
            ty = 60;
            break;
          case 9:
            tx = 50;
            ty = 50;
            break;
          case 8:
            tx = 65;
            ty = 60;
            break;
          case 7:
            tx = 80;
            ty = 75;
        }
        if (i > 8){
          for (int j=0; j<7; j++){
            g.drawLine(50-3+j, 110+3-j, tx-3+j, ty+3-j);
          }
        }
        else {
          for (int j=0; j<7; j++){
            g.drawLine(50-3+j, 110-3+j, tx-3+j, ty-3+j);
          }
        }
        g.drawImage(sideStanceImage, 0, 100, null);
        ballAndStickImages[i] = image;
      }
    }
    return ballAndStickImages;
  }
  
  public ScaledImage draw(){
    switch (action){
      case ATTACK:
      case UNPARRIABLE_ATTACK:
        switch (state){
        case 5:
          return ScaledImage.get(images[2], 1, 3);
        case 4:
          return ScaledImage.get(images[3], 1, 3);
        case 3:
          return ScaledImage.get(images[4], 1, 3);
        case 2:
          return ScaledImage.get(images[5], 1, 3);
        case 1:
          return ScaledImage.get(images[6], 1, 3);
        default:
          return ScaledImage.get(images[1]);
      }
      case SolidActor.FEINT:
        switch (state){
        case 3:
        case 1:
          return ScaledImage.get(images[2], 1, 3);
        case 2:
          return ScaledImage.get(images[3], 1, 3);
        default:
          return ScaledImage.get(images[1]);
      }
      case SolidActor.PARRY:
        switch (state){
        case 5:
          return ScaledImage.get(images[11], 1, 3);
        case 4:
          return ScaledImage.get(images[10], 1, 3);
        case 3:
          return ScaledImage.get(images[9], 1, 3);
        case 2:
          return ScaledImage.get(images[8], 1, 3);
        case 1:
          return ScaledImage.get(images[7], 1, 3);
        default:
          return ScaledImage.get(images[1]);
      }
      case SolidActor.BAD_PARRY:
        switch (state){
        case 5:
          return ScaledImage.get(images[7], 1, 3);
        case 4:
          return ScaledImage.get(images[8], 1, 3);
        case 3:
          return ScaledImage.get(images[9], 1, 3);
        case 2:
          return ScaledImage.get(images[10], 1, 3);
        case 1:
          return ScaledImage.get(images[11], 1, 3);
        default:
          return ScaledImage.get(images[1]);
      }
      case SolidActor.RECOVER:
        switch (state){
        case 1:
          return ScaledImage.get(images[2], 1, 3);
        case 2:
          return ScaledImage.get(images[3], 1, 3);
        case 3:
          return ScaledImage.get(images[4], 1, 3);
        case 4:
          return ScaledImage.get(images[5], 1, 3);
        default:
          return ScaledImage.get(images[1]);
      }
      default:
        if (intention == SolidActor.ATTACK){
        /**
         If Player.intention == ATTACK, it will always appear
         in a combat stance. Non-Players will only appear this
         way when close to their target
         **/
        if (appearDifferentWhenFarFromTarget){
          //Is s far from its target?
          double fightingRange = 1 + attackRange + (0.5*(width+target.width));
          double dx = target.x-x;
          double dy = target.y-y;
          double d2 = (dx*dx) + (dy*dy);
          if (d2 > fightingRange*fightingRange){
            //distance is assumed to be too far to fight
            //return square stance
            return ScaledImage.get(images[0]);
          }
          //Close enough; assume combat stance
          return ScaledImage.get(images[1]);
        }
        else {
          return ScaledImage.get(images[1]);
        }
      }
    }
    return ScaledImage.get(images[0]);
  }
}