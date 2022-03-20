/**
This is you!
You are treated as an animal, but with some differences.
Your states are WANDERING and ATTACKING. ATTACKING implies combat
mode is on.
**/
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

public class Player extends SolidActor{
  //the health thing might change
  final static int maxHealth = 3600;
  public int health = maxHealth;
  public BufferedImage[] images = Fencer.getBallAndStick(Color.DARK_GRAY);
  
  //This is used for mapmaking
  final public static double width = 1;
  
  //rotationSpeed is the amount added to your direction when you turn
  final public double rotationSpeed = Math.PI/60;
  
  /**
  If this is false, player will not "act" when its act method is called,
  except to heal. Use this when the Game should "take over" player's movement.
  **/
  public boolean acts = true;
  
  public Player(){
    super(Player.width, Player.width);
    fastSpeed = 0.15;
    slowSpeed = 0.1;
    intention = WANDER;
    resetActions();
    state = 0;
    speed = slowSpeed;
    attackTime = 5;
    feintTime = 3;
    parryTime = 5;
    recoverTime = 4;
    //"Target" does not apply to Player
    canHitOnlyTarget = false;
    //Your appearance does not change relative to your target
    appearDifferentWhenFarFromTarget = false;
    attackRange = 1;
    Game.actors.add(this);
  }
  
  //used to toggle between combat mode (state == ATTACKING) and non-combat mode
  //(state == WANDERING)
  public void toggleState(){
    if (intention == ATTACK){
      intention = WANDER;
      speed = slowSpeed;
    }
    else if (intention == WANDER){
      intention = ATTACK;
      speed = fastSpeed;
    }
  }
  
  //used when calculating movement
  static final double inversesqrt2 = 1.0/Math.sqrt(2);
  
  /**
  Player's attack is not unparriable in the way others are. Instead,
  when attacking, and the target has already begun parrying, they
  do a "bad parry" afterward, which does not stop the attack
  **/
  public void attack(Solid s){
    attack(s, true);
  }
  public void attack(){
    attack(this);
  }
  //this marks Player's attacks such that other SolidActors can recognize it.
  public int getDamage(){
    return 0;
  }
  /**
  Player has no Behavior, because its actions are handled directly in the
  act method. When (state == 0), Player is ready to choose a new action.
  When a key is pressed, this changes newAction, to be called the next
  time state is zero.
  **/
  public void act(){
    //increase health if below maximum
    if (health < maxHealth){
      health += 2*Game.timeSpeed;
      if (health > maxHealth){
        health = maxHealth;
      }
    }
    //Should player act?
    if (!acts){
      return;
    }
    /**
     For movement, a movement "vector" is constructed, based on which keys
     are down. If two opposite movement keys are down, they cancel out.
     If you try to move diagonally, the total movement will be scaled
     such that you still travel the same distance.
    **/
    double speedtSpeed = speed*Game.timeSpeed;
    double upx = -speedtSpeed*Math.sin(direction);
    double upy = -speedtSpeed*Math.cos(direction);
    double rightx = -upy;
    double righty = upx;
    boolean horizontalMovement = false;
    boolean verticalMovement = false;
    double dx = 0;
    double dy = 0;
    boolean[] keys = Game.keys;
    if (keys[KeyEvent.VK_LEFT]){
      dx -= rightx;
      dy -= righty;
      horizontalMovement = !horizontalMovement;
    }
    if (keys[KeyEvent.VK_RIGHT]){
      dx += rightx;
      dy += righty;
      horizontalMovement = !horizontalMovement;
    }
    if (keys[KeyEvent.VK_UP]){
      dx += upx;
      dy += upy;
      verticalMovement = !verticalMovement;
    }
    if (keys[KeyEvent.VK_DOWN]){
      dx -= upx;
      dy -= upy;
      verticalMovement = !verticalMovement;
    }
    if (horizontalMovement && verticalMovement){
      //moving diagonally; scale the vectors
      dx *= inversesqrt2;
      dy *= inversesqrt2;
    }
    if (horizontalMovement || verticalMovement){
      //now move accordingly
      double newx = x + dx;
      double newy = y + dy;
      if (Game.canMove(this, newx, newy)){
        moveTo(newx, newy, Game.intersectingTiles);
      }
    }
    if (keys[KeyEvent.VK_D] && !keys[KeyEvent.VK_G]){
      direction = (direction + rotationSpeed) % tau;
    }
    if (keys[KeyEvent.VK_G] && !keys[KeyEvent.VK_D]){
      direction = (direction - rotationSpeed) % tau;
    }
    
    //now handle state and attacking
    //is something already happening?
    if (state > 0){
      fight();
    }
    /**
    When an action finishes, the state returns to zero, but the
    current action is still not "NULL." At this point, if the
    action is not NULL, it changes to NULL. If it is NULL, then
    the next action is drawn from nextAction. This takes a step,
    which puts a one-step delay between ending an action and
    beginning the next one.
    **/
    if (state == 0){
      if (action != NULL){
        action = NULL;
      }
      else {
        //action is NULL; time to draw the next action if it is.
        if (nextAction != NULL){
          action = nextAction;
          nextAction = NULL;
          switch (action){
            case ATTACK:
              attack(this);
              break;
            case FEINT:
              feint();
              break;
            case PARRY:
              parry();
              break;
            case RECOVER:
              recover();
              break;
          }
        }
      }
    }
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
        return ScaledImage.get(images[1]);
      }
      return ScaledImage.get(images[0]);
    }
  }
  public void hit(int damage){
    if (damage == -1){
      //You got hit by the trainer! This one does not count.
      return;
    }
    //what happens when you get hit. Needs to be upated.
    //If you have full health, you survive. Else, death.
    if (health == maxHealth){
      health = 1;
    }
    else {
      Game.timer.stop();
      Game.getResponses("You died.", new Game.DialogButton("Ok"){public void press(){System.exit(0);}});
    }
  }
  //this is just for testing! Do no run this! All code below this for testing!
  public static void main(String[] args){
    long t;
    long tt;
    t = System.currentTimeMillis();
    JTextArea b = new JTextArea();
    b.setText(" ");
    tt = System.currentTimeMillis();
    System.out.println(tt-t);
    t = System.currentTimeMillis();
    JTextArea a = new JTextArea(" ");
    tt = System.currentTimeMillis();
    System.out.println(tt-t);
  }
}