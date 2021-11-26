package game;

import java.io.File;

import javax.imageio.ImageIO;

@SuppressWarnings("serial")
public class UnlabelledBoard extends Board {
	public UnlabelledBoard(ControlPanel cp) {
		super(cp);
	}

	@Override
	public void init() throws Exception {
		start_graphics = new Point();
		target_graphics = new Point();
		robot_graphics = new Point();
		

		obstacle_image = ImageIO.read(new File("img/Obstacle.png"));

		// Unlabelled: Use the same image for all robots/goals
		base_robot_images = ImageIO.read(new File("img/Robot0.png"));
		robot_image = ImageIO.read(new File("img/Robot0.png"));
		for (int i = 0; i < goals_images.length; i++) {
			goals_images[i] = ImageIO.read(new File("img/Goal" + String.valueOf(0) + ".png"));
		}
	}
}
