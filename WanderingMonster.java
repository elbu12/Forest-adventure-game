public class WanderingMonster extends Monster{
  public WanderingMonster(double x, double y, double wx, double wy, double ww, double wh, Solid target){
    super(target);
    place(x, y);
    this.wx = wx;
    this.wy = wy;
    this.ww = ww;
    this.wh = wh;
    behavior = WANDER_UNTIL_DETECT_THEN_ATTACK;
    intention = WANDER;
    tx = x;
    ty = y;
    action = STAND;
    state = 0;
  }
}