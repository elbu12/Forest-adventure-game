import java.awt.*;
import java.awt.image.*;

public class Monster extends Navigator {

	private static final long serialVersionUID = 1L;
	//the setUp method makes the image(s)
	private static BufferedImage image = setUp();

	private static BufferedImage setUp() {
		BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.fillOval(73, 100, 54, 100); // butt
		g.fillOval(73, 60, 54, 90); // ribs
		g.fillOval(73, 50, 54, 50); // shoulders
		g.fillOval(83, 0, 34, 100); // head
		g.fillOval(73, 18, 54, 18); // ears
		g.setColor(Color.YELLOW);
		g.drawOval(0, 0, 200, 200);
		g.drawOval(1, 1, 198, 198);
		return image;
	}

	public Monster(Solid target) {
		super(1.9, 1.9);
		fastSpeed = 0.2;
		slowSpeed = 0.07;
		behavior = STAND_UNTIL_DETECT_THEN_ATTACK;
		fightingStyle = IMMEDIATE_ATTACK;
		intention = STAND;
		resetActions();
		state = 0;
		speed = slowSpeed;
		attackTime = 2;
		recoverTime = 1;
		attackRange = 0.11;
		setDetectionRange(8);
		walks = true;
		this.target = target;
	}

	public Monster(Solid target, double x, double y) {
		this(target);
		place(x, y);
	}

	public ScaledImage draw() {
		return ScaledImage.get(image, 1, 1);
	}
}