
public class KnownMonster extends WanderingMonster{

	private static final long serialVersionUID = 1L;
	public KnownMonster(double x, double y, double wx, double wy, double ww, double wh, Solid target) {
		super(x, y, wx, wy, ww, wh, target);
	}
	protected void die() {
		Game.globals.monsterDead = true;
		super.die();
	}
}
