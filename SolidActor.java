import java.awt.image.*;
import java.util.*;

/**
This is the base class for things that act. A SolidActor can
act and be drawn. Player is a SolidActor, as are non-player
characters. The NPCs are generally Navigators, as they can
plot paths to points. Thus, "SolidActor" is the common super-
class of both Player and NPCs, after which they diverge.
**/

abstract class SolidActor extends Solid implements Actor, Resetter{
  /**
  attackTime, recoverTime, feintTime, and parryTime determine how
  long each such activity takes. When initiating an attack, feint,
  or parry, the state attribute starts at the appropriate "time"
  value and counts down to zero. When it reaches zero, the action
  is complete. When an attack is finished, it will then recover.
  **/
  protected int attackTime;
  protected int recoverTime;
  protected int feintTime;
  protected int parryTime;
  
  public int fightingStyle = NULL;
  //fighting styles:
//final public static int NULL = -1;
  final public static int IMMEDIATE_ATTACK = 1;
  final public static int COUNTERATTACK = 2;
  final public static int FEINT_THEN_ATTACK = 3;
  final public static int STAND_AND_COUNTER = 5;
  final public static int APPROACH_THEN_WAIT_THEN_ATTACK = 6;
  
  /**
  attackRange is the maximum distance a Solid's exterior can be
  from this SolidActor's exterior and still get hit by it.
  
  When attackRange < 1.2, this can chase while attacking, rather than stopping to attack
  **/
  public double attackRange;
  //determines if the distance between s and t is < d
  public boolean distanceLessThan(Solid s, Solid t, double d, boolean equalTo){
    double dx = s.x - t.x;
    double dy = s.y - t.y;
    if (equalTo){
      return (dx*dx) + (dy*dy) <= d*d;
    }
    return (dx*dx) + (dy*dy) < d*d;
  }
  //determines whether s is within attack range
  public boolean canHit(Solid s){
    //is it in range?
    if (distanceLessThan(this, s, attackRange + ((s.width+width)*0.5), true)){
      //yes. Is it the first thing s would hit on this line?
      if (Game.getIntersectingSolid(this, x, y, s.x-x, s.y-y) == s){
        //yes
        return true;
      }
    }
    return false;
  }
  /**
  intention, action, and state describe what a SolidActor is doing.
  "Intention" refers to their general goal, ie what are the trying
  to accomplish (wander, attack, stand idly, etc). "Action" refers
  to what they are physically doing at that moment (attacking,
  feinting, parrying, etc). "State" is a numerical variable for
  how far they are in the process of an action. For actions that
  require multiple "turns," the state variable counts back down to
  zero.
  
  In some cases, a SolidActor may intend to perform another action
  after the current one. This can be stored in "nextAction"
  **/
  public int intention;
  protected int action  = NULL;
  public int state;
  public int nextAction = NULL;
  
  public int getAction(){
    return action;
  }
  public void setAction(int action){
    this.action = action;
  }
  public void resetActions(){
    action = NULL;
    nextAction = NULL;
  }
  
  //Intentions:
  final public static int STAND = 0;
  final public static int WANDER = 1;
  final public static int ATTACK = 2;
//final public static int DANCE = 9;
  final public static int FLEE = 10;
  final public static int STALK = 11;
  
  //Actions:
  final public static int NULL = -1;
//final public static int STAND = 0;
  final public static int MOVE = 1;
//final public static int ATTACK = 2;
  final public static int UNPARRIABLE_ATTACK = 3;
  final public static int FEINT = 4;
  final public static int PARRY = 5;
  final public static int BAD_PARRY = 6; //an ineffective parry
  final public static int RECOVER = 7;
  final public static int DIE = 8;
  final public static int DANCE = 9;
  
  double speed;
  double fastSpeed;
  double slowSpeed;
  
  //moveToward tries to move in a straight line toward the target
  protected boolean moveToward(double targetx, double targety){
    double dx = targetx-this.x;
    double dy = targety-this.y;
    //(dx,dy) is the movement vector, but it may be too long for one step
    double ll = (dx*dx) + (dy*dy);
    //speedTimeSpeed is how far this can go in one step
    double speedTimeSpeed = speed*Game.timeSpeed;
    if (ll > speedTimeSpeed*speedTimeSpeed){
      //if it would travel farther than possible, scale by speed*timeSpeed/l
      double speedTimeSpeedl = speedTimeSpeed/Math.sqrt(ll);
      dx *= speedTimeSpeedl;
      dy *= speedTimeSpeedl;
    }
    if (Game.moveIfCan(this, x+dx, y+dy)){
      return true;
    }
    else {
      //If unable to move, try moving by half as much
      return Game.moveIfCan(this, x+(dx*0.5), y+(dy*0.5));
    }
  }
  
  //The "enemies" collection is for Navigators that react to multiple "targets;"
  //Those that are set to flee will flee from detectable Solids in the set.
  public Collection <? extends Solid> enemies;
  
  //Does this hit whatever is in front of it, or only the intended target?
  public boolean canHitOnlyTarget = true;
  public Solid target;
  
  public SolidActor(double width, double height){
    super(width, height, Game.CIRCLE);
    walks = true;
  }
  
  protected void attack(Solid t, boolean parriable){
    //initiates an attack on t
    target = t;
    setAction(parriable ? ATTACK : UNPARRIABLE_ATTACK);
    state = attackTime;
  }
  protected void attack(Solid t){
    attack(t, t.getAction() != PARRY);
  }
  public void riposte(Solid t){
    //initiates an unparriable attack on t
    attack(t, false);
  }
  protected void feint(){
    setAction(FEINT);
    state = feintTime;
  }
  protected void parry(boolean good){
    setAction(good ? PARRY : BAD_PARRY);
    state = parryTime;
  }
  protected void parry(){
    parry(true);
  }
  protected void recover(){
    recover(recoverTime);
  }
  //This version is for skipping to a specific state value
  protected void recover(int state){
    setAction(RECOVER);
    this.state = state;
  }
  
  public void act(){}
  
  /**
  This method is called when this SolidActor hits something.
  The return value conveys the source of the attack, not the
  magnitude of it. All attacks are equally lethal.
  
  Use this to label SolidActors with special attacks.
  **/
  public int getDamage(){
    return 1;
  }
  
  /**
  This method handles the actions involved in fighting when state
  is nonzero. It essentially "continues" whatever combat decisions
  have already been made. It is not responsible for deciding which
  action to perform.
  **/
  public void fight(){
    boolean parriable = false;
    switch (action){
      case ATTACK:
        //same as UNPARRIABLE_ATTACK, but parriable
        parriable = true;
      case UNPARRIABLE_ATTACK:
        for (double i=0; i<Game.timeSpeed; i+=0.5){
          state--;
          //is the attack finished?
          if (state == 0){
            //yes
            recover();
            break;
          }
          //No. is it hitting anything?
          boolean hasTarget = false;
          double targetx = 0; //these zeroes are placeholder values to be overwritten
          double targety = 0;
          if (canHitOnlyTarget){
            if (target==Game.getIntersectingSolid(this, x, y, target.x-x, target.y-y)){
              //nothing obstructs path toward target
              hasTarget = true;
              targetx = target.x - x;
              targety = target.y - y;
            }
          }
          else {
            targetx = -(attackRange + (width*0.5))*Math.sin(direction);
            targety = -(attackRange + (width*0.5))*Math.cos(direction);
            target = Game.getIntersectingSolid(this, x, y, targetx, targety);
            hasTarget = ( target != this );
          }
          if (hasTarget){
            //is the target in range?
            //determine where the intersection occurs
            double in = Game.wherePointIntersectSolid(x, y, targetx, targety, target);
            //"in" is the portion of the (targetx,targety) vector at which the intersection occurs
            //square it
            in *= in;
            //multiply by the length of (targetx,targety) squared
            double targetLength2 = (targetx*targetx) + (targety*targety);
            in *= targetLength2;
            //now "in" is actually the distance (squared) to the intersection point
            //compare it to the total range (squared) this SolidActor has
            double totalRange = (0.5*width) + (attackRange*(attackTime+1-state)/attackTime);
            double totalRange2 = totalRange*totalRange;
            //is the distance (squared) to the intersection <= total range (squared) ?
            hasTarget = !(totalRange2 < in);
          }
          if (hasTarget){
            //an impact occurs!
            if (!parriable || target.getAction() != PARRY || target.getAngle(this) > Math.PI*0.25){
              /**
               The last term above refers to whether the target is facing close enough to parry.
               If it is facing away, a well-timed parry is still ineffective.
               **/
              target.hit(getDamage());
            }
            else {
              //they parry successfully!
              target.riposte(this);
            }
            //if fighting style is feint_then_attack, then after the feint, this keeps
            //attacking until the target moves out of range.
            if (fightingStyle == FEINT_THEN_ATTACK){
              nextAction = ATTACK;
            }
            //begin recovering from the attack at next step
            Game.resetters.add(this);
            break;
          }
        }
        break;
      case FEINT:
        for (double i=0; i<Game.timeSpeed; i+=0.5){
          state--;
          if (state == 0){
            //attack automatically if the feint was successful?
            if (fightingStyle == FEINT_THEN_ATTACK){
              nextAction = ATTACK;
            }
            break;
          }
        }
        break;
      case PARRY:
      case BAD_PARRY:
        for (double i=0; i<Game.timeSpeed; i+=0.5){
          state--;
          if (state == 0){
            /**
            Parry finished. If target is still attacking, then the attack
            began while this was already parrying. That makes the attack
            unparriable, represented by a bad parry here.
            **/
            if (fightingStyle == COUNTERATTACK && target.getAction() == ATTACK){
              nextAction = BAD_PARRY;
            }
          }
        }
        break;
      case RECOVER:
        for (double i=0; i<Game.timeSpeed && state > 0; i+=0.5){
          state--;
        }
        break;
    }
  }
  
  /**
  Changing from attacking to recovering happens when resetting, else
  the last image of attacking would not be visible
  **/
  public void reset(){
    if (action == ATTACK || action == UNPARRIABLE_ATTACK){
      recover(recoverTime + 2 - state);
    }
  }
  
  //Non-Player SolidActors appear differently when far from their target
  public boolean appearDifferentWhenFarFromTarget = true;
  
  abstract ScaledImage draw();
}