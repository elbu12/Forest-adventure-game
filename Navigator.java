import java.util.*;
/*
Navigators are SolidActors that can plot paths to destinations.
NPCs are generally Navigators.

Navigators have behavior, which determines how they act when not
fighting, and fightingStyle, which determines the counterpart.
**/
public abstract class Navigator extends SolidActor{
  public Navigator(double width, double height){
    super(width, height);
    //How many times to iterate the pathfinding algorithm before giving up:
    pathFindingIterations = 300;
  }
  public int behavior;
  //behaviors:
  final public static int STAND_UNTIL_DETECT_THEN_ATTACK = 1;
  final public static int WANDER_UNTIL_DETECT_THEN_ATTACK = 2;
  final public static int STAND = 0;
  final public static int WANDER_UNLESS_CLOSE_THEN_STAND = 4;
  final public static int DEFEND_TERRITORY = 5;
  final public static int STAND_UNTIL_DETECT_THEN_FLEE = 6;
  final public static int DANCE_UNTIL_DETECT_THEN_FLEE = 7;
  final public static int STALK_UNTIL_DETECT_THEN_ATTACK = 8;
  
  //wandering Navigators need to handle wandering personally, as only they
  //store the area in which they wander
  
  //The following stuff is for pathfinding:
  public double[] nextNode = new double[2];
  //Note that the constructor "creates" a Navigator, but does not place
  //it on the map, nor does it add it to actors. Use the "place" method
  //to connect a Navigator to the game
  public boolean place(double x, double y){
    if (Game.moveIfCan(this, x, y)){
      Game.actors.add(this);
      nextNode[0] = x;
      nextNode[1] = y;
      return true;
    }
    return false;
  }
  
  //navigateToward tries to move along the shortest path toward a target
  protected void navigateToward(double x1, double y1){
    double[] tempNextNode = Game.getNextStep(this, x, y, x1, y1);
    nextNode[0] = tempNextNode[0];
    nextNode[1] = tempNextNode[1];
    moveToward(nextNode[0], nextNode[1]);
  }
  //this version moves toward Solid s, without considering collisions with s
  protected void navigateToward(Solid s){
    //moves toward Solid s without considering collisions with s
    double[] tempNextNode = Game.getNextStep(this, x, y, s);
    nextNode[0] = tempNextNode[0];
    nextNode[1] = tempNextNode[1];
    moveToward(nextNode[0], nextNode[1]);
  }
  //This method will attempt to move this Navigator directly away from
  //the given point. If it can't move directly away, it will try to
  //move perpendicularly.
  protected boolean moveAwayFrom(double px, double py){
    //get the vector pointing away, in the direction this wants to
    //move
    double dx = x-px;
    double dy = y-py;
    //scale it if too small
    double ll = (dx*dx) + (dy*dy);
    if (ll < 1){
      //length less than one
      dx /= ll;
      dy /= ll;
      //now length greater than one
    }
    //now scale by speed
    dx *= speed;
    dy *= speed;
    if (moveToward(x + dx, y + dy)){
      //done
      return true;
    }
    else if (moveToward(x - dy, y + dx)){
      //try perpendicularly
      return true;
    }
    //try other perpendicular direction
    return moveToward(x + dy, y - dx);
  }
  protected void moveAwayFrom(Solid s){
    moveAwayFrom(s.x, s.y);
  }
  //This method will determine if this and another solid are so
  //close that this could take a step away and still be in range
  //to fight. For fighters that want to stay at the end of their range,
  //this is how they know when to back up
  private boolean tooClose(Solid s){
    double dx = s.x - x;
    double dy = s.y - y;
    double rangeMinusSpeed = attackRange + ((s.width+width)*0.5) - speed;
    return ((dx*dx) + (dy*dy) <= rangeMinusSpeed*rangeMinusSpeed);
  }
  //detectionRange2 is the square of how close something must be
  //for this to notice it
  //set this with setDetectionRange
  double detectionRange;
  void setDetectionRange(double detectionRange){
    this.detectionRange = detectionRange;
  }
  //determines if this can detect s, ie is s within detectionRange
  //hiding Navigators will not detect you if you are outside the
  //"visibility area"
  protected boolean canDetect(Solid s){
    if (hiding && (s.x < vx || s.x >= vx+vw || s.y < vy || s.y >= vy+vh)){
      return false;
    }
    double dx = s.x - x;
    double dy = s.y - y;
    return ((dx*dx) + (dy*dy) < detectionRange*detectionRange);
  }
  public void hit(int damage){
    //this may be improved over time
    die();
  }
  //removes this from the world. Death is the final frontier.
  protected void die(){
    disconnectTiles();
    action = DIE;
    Game.resetters.add(this);
  }
  protected void removeFromGame(){
  }
  public void reset(){
    //removes dying Navigator
    if (action == DIE){
      Game.actors.remove(this);
      resetActions();
    }
    else {
      super.reset();
    }
  }
  
  /**
  The following is for wandering:
  x,y: location
  wx,wy: corner of wandering territory
  ww,wh: width and height of wandering territory
  tx,ty: (x,y) coordinates of current wandering destination
  beginStanding: called to make a wanderer stand temporarily
  **/
  double wx;
  double wy;
  double ww;
  double wh;
  double tx;
  double ty;
  /**
  Alternatively, for a Navigator defending territory, the territory to defend
  is represented by the wx/wy/ww/wh rectangle and tx/ty is the specific spot
  to which the Navigator goes when unprovoked.
  
  Alternatively, for a dancer, tx and ty are the location of the starting point
  of the dance. They will move toward (tx,ty) + (dance.getx(), dance.gety())
  
  role is a boolean representing the dancer's "role" in the dance
  **/
  boolean role;
  
  public void beginStanding(){
    action = STAND;
    state = 60 + (int)(Math.random()*60);
  }
  /**
  The act method makes decisions for a Navigator. It considers both
  its behavior and current intention.
  **/
  public void act(){
    if (action == DIE){
      return;
    }
    //Most navigators remain in attack mode once provoked, but those with behavior
    //DEFEND_TERRITORY will stop attacking when the target leaves
    if (behavior == DEFEND_TERRITORY && intention == ATTACK){
      if (target.x < wx || target.x >= wx+ww || target.y < wy || target.y >= wy+wh){
        //target left the defended territory; return to starting point
        intention = STAND;
        state = 0;
        resetActions();
      }
    }
    if (intention == ATTACK){
      //this part is just about movement
      //be fast! This is combat!
      speed = fastSpeed;
      if (canHit(target)){
        //face the target
        direction = Angle.get(target.x - x, target.y - y);  
        //in range; is it too close?
        if (fightingStyle == COUNTERATTACK && tooClose(target)){
          //yes; back away
          double oldx = x;
          double oldy = y;
          moveAwayFrom(target);
        }
      }
      else if (state == 0){
        //out of range; move toward target if appropriate
        if (fightingStyle == APPROACH_THEN_WAIT_THEN_ATTACK && action == NULL){
          //how far is this from its target?
          double dist2 = Game.distanceBetween2(x, y, target.x, target.y);
          double tooFar = (width*0.5) + (target.width*0.5) + 1.6;
          double tooClose = (width*0.5) + (target.width*0.5) + 1.3;
          double inRange = (width*0.5) + (target.width*0.5) + 1;
          if (dist2 > tooFar*tooFar){
            //too far; approach
            navigateToward(target);
          }
          else if (Math.random() < 0.005 || dist2 <= inRange * inRange){
            //attack if close enough to be attacked, or at random
            action = MOVE;
          }
          else if (dist2 <= tooClose*tooClose){
            //too close for comfort, but not close enough to be attacked; move away
            moveAwayFrom(target);
          }
        }
        else {
          //if fightingStyle is not APPROACH_THEN_WAIT_THEN_ATTACK, just approach
          navigateToward(target);
        }
        //face where you're going
        if (x != nextNode[0] || y != nextNode[1]){
          direction = Angle.get(nextNode[0] - x, nextNode[1] - y);
        }
      }
      //Movement handled; now handle fighting
      //is something already happening?
      if (state > 0){
        if (attackRange < 0.12){
          //those with short attackRanges can chase you while attacking
          navigateToward(target);
        }
        //continue that thing
        fight();
        //face the target
        direction = Angle.get(target.x - x, target.y - y);
      }
      else if (fightingStyle == IMMEDIATE_ATTACK || fightingStyle == APPROACH_THEN_WAIT_THEN_ATTACK){
        //in range to attack?
        if (canHit(target)){
          //yes; attack
          attack(target);
        }
      }
      else if (fightingStyle == COUNTERATTACK){
        //in range to attack?
        if (canHit(target)){
          //yes
          //is target attacking?
          if (target.getAction() == ATTACK){
            //either parry or bad parry
            parry(nextAction != BAD_PARRY);
          }
          else if (target.getAction() == FEINT){
            //bad parry against a feint
            parry(false);
          }
          else {
            //target not attacking; possibly attack at random
            if (Math.random() < 0.01){
              attack(target);
            }
          }
        }
        /**
        Now clear nextAction. Either a bad parry has begun,
        or it is irrelevant. Either way, the information
        BAD_PARRY has been resolved.
        **/
        nextAction = NULL;
      }
      else if (fightingStyle == FEINT_THEN_ATTACK){
        //in range to attack?
        if (canHit(target)){
          //has this feinted yet?
          if (nextAction == ATTACK || nextAction == UNPARRIABLE_ATTACK){
            //yes; attack
            attack(target);
          }
          else {
            //no; feint first
            feint();
          }
        }
        else {
          //not in range; reset actions to feint next time
          resetActions();
        }
      }
    }
    else if (intention == STAND){
      if (behavior == STAND_UNTIL_DETECT_THEN_ATTACK){
        if (canDetect(target)){
          //attack! 
          noticeTarget();
          intendToAttack();
        }
      }
      else if (behavior == DEFEND_TERRITORY){
        if (canDetect(target)){
          if (target.x < wx || target.x >= wx+ww || target.y < wy || target.y >= wy+wh){
            //target is out of the defended territory; just stare them down
            direction = Angle.get(target.x - x, target.y - y);
          }
          else {
            //target is in the defended territory; attack!
            noticeTarget();
            intendToAttack();
          }
        }
        else {
          if (x == tx && y == ty){
            //already at home point; stand there
            direction = Angle.get(target.x - x, target.y - y);
          }
          else {
            //move toward home point
            navigateToward(tx, ty);
            direction = Angle.get(nextNode[0] - x, nextNode[1] - y);
          }
        }
      }
      else if (behavior == STAND_UNTIL_DETECT_THEN_FLEE){
        if (Game.enemyNearby(this)){
          //noticed an enemy!
          intention = FLEE;
        }
      }
    }
    else if (intention == WANDER){
      if (canDetect(target)){
        if (behavior == WANDER_UNTIL_DETECT_THEN_ATTACK){
          //attack! 
          noticeTarget();
          intendToAttack();
        }
        else if (behavior == WANDER_UNLESS_CLOSE_THEN_STAND){
          //stand and face the target
          direction = Angle.get(target.x-x, target.y-y);
        }
      }
      else {
        //wander!
        //is this moving?
        if (action == MOVE){
          //are we at the destination?
          if (tx == x && ty == y){
            //yes; pause for a bit
            beginStanding();
          }
          else {
            //no; can we continue moving?
            if (moveToward(tx, ty)){
              //yes; face the destination
              direction = Angle.get(tx - x, ty - y);
            }
            else {
              //no; pause for a bit
              beginStanding();
            }
          }
        }
        else if (action == STAND){
          //have we finished standing?
          if (state == 0){
            //yes; pick a new destination
            tx = wx + (Math.random()*ww);
            ty = wy + (Math.random()*wh);
            action = MOVE;
          }
          else {
            //no; continue standing
            for (double i=0; i<Game.timeSpeed && state > 0; i+=0.5){
              state--;
            }
          }
        }
      }
    }
    else if (intention == DANCE){
      if (behavior == DANCE_UNTIL_DETECT_THEN_FLEE){
        if (Game.enemyNearby(this)){
          //stop dancing once attacked
          intention = FLEE;
        }
        else {
          moveToward(tx + Game.dance.getx(role), ty + Game.dance.gety(role));
          direction = Game.dance.getDirection(role);
        }
      }
    }
    else if (intention == FLEE){
      //if no enemies around, it will run in the same direction as before
      Game.flee(this);
      speed = fastSpeed;
      navigateToward(this.x + tx, this.y + ty);
      direction = Angle.get(tx, ty);
    }
    else if (intention == STALK){
      //if a stalking navigator is too close to its target, it will attack
      if (canDetect(target)){
        noticeTarget();
      }
      else {
        //how close is the target?
        double dx = x - target.x;
        double dy = y - target.y;
        double dis2 = (dx*dx) + (dy*dy);
        direction = Angle.get(-dx, -dy);
        if (dis2 < Game.screenWidth*Game.screenWidth*0.32){
          //Too close; flee!
          double dis = Math.sqrt(dis2);
          navigateToward(x + (10*dx/dis), y + (10*dy/dis));
        }
        else if (dis2 > Game.screenWidth*Game.screenWidth*0.45){
          //Far enough to follow, but do so only if target is within (wx,wy,ww,wh) area
          if (target.x >= wx && target.y >= wy && target.x < wx+ww && target.y < wy+wh){
            //target within area; follow. The target may be far away, so rather than
            //navigating directly to it, just navigate to a point closer to it.
            double targetx = target.x - x;
            double targety = target.y - y;
            double dd = (targetx*targetx) + (targety*targety); //distance squared
            if (dd > 100){
              double sqrd = Math.sqrt(dd); //actual distance
              //navigate to a point 10 units closer
              navigateToward(x+(targetx*10/sqrd), y+(targety*10/sqrd));
            }
            else {
              //close enough; just move directly
              navigateToward(target.x, target.y);
            }
          }
          else {
            //target not within area; return to starting point if not already there
            if (x != tx || y != ty){ //If you remove this "if" statement, pathfinding errs for everyone. Why?
              navigateToward(tx, ty);
              direction = Angle.get(nextNode[0]-x, nextNode[1]-y);
            }
          }
        }
        else {
          //within the "medium" range; just stand there and watch target
        }
      }
    }
  }
  //This is called when a Navigator's intention changes to attack
  //It also makes this Navigator visible
  public void intendToAttack(){
    intention = ATTACK;
    resetActions();
    state = 0;
    hiding = false;
  }

  /**
  Some navigators have "friends," such that, when one notices its target, it will
  alert its friends. Those without "friends" still have a "friends" list, but it
  just refers to this empty, static list
  **/
  final public static ArrayList <Navigator> emptyFriends = new ArrayList <Navigator> ();
  //Here is the Navigator's actual friends list
  public Collection <Navigator> friends = emptyFriends;
  //Navigators with friends alert their friends when they detect their target
  public void noticeTarget(){
    for (Navigator friend : friends){
      friend.intendToAttack();
    }
  }
  //Some Navigators disappear/die when far from the player. Use this for that
  public boolean dieWhenFar(double d){
    if (Game.distanceBetween2(x, y, Game.player.x, Game.player.y) > d*d){
      die();
      return true;
    }
    return false;
  }
  
}