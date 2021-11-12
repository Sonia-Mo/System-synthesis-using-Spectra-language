package game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import tau.smlab.syntech.controller.executor.ControllerExecutor;
import tau.smlab.syntech.controller.jit.BasicJitController;

public class MainClass {
	public static void main(String args[]) throws Exception {

		int x = 9;
		int y = 9;
		int num_robots = 1;
		
		// TODO: target coordinates
		int x_target;
		int y_target;
		final int num_targets = 3;

		Point[] obstacles = new Point[9];
		obstacles[0] = new Point(5, 0);	
		obstacles[1] = new Point(1, 1);		
		obstacles[2] = new Point(5, 1);
		obstacles[3] = new Point(3, 3);
		obstacles[4] = new Point(5, 3);
		obstacles[5] = new Point(2, 5);
		obstacles[6] = new Point(5, 5);
		obstacles[7] = new Point(3, 7);
		obstacles[8] = new Point(5, 7);

//		Point[] goals = new Point[num_robots];
		
		// TODO: Choose targets randomly
		Point[] goals = new Point[num_targets];

		Random rand = new Random();
		for (int i = 0; i < num_targets; i++) {
			do {
				x_target = rand.nextInt(8);
				y_target = rand.nextInt(8);
			}
			// TODO: maybe try to change to something else?!
			while (x_target == 0 & y_target == 0 | x_target == 5 & y_target == 0 | x_target == 1 & y_target == 1 |
					x_target == 5 & y_target == 1 | x_target == 3 & y_target == 3 | x_target == 5 & y_target == 3 |
					x_target == 2 & y_target == 5 | x_target == 5 & y_target == 5 | x_target == 3 & y_target == 7 |
					x_target == 5 & y_target == 7);
			
			goals[i] = new Point(x_target, y_target);
		}
		
		ControlPanel cp;
		String path = "out//";

		System.out.println("Running the system");
		cp = new ControlPanel(x, y, num_robots, obstacles, goals, path);
		cp.init();

	}
}
