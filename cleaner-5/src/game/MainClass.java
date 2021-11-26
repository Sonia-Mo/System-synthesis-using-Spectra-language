package game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import tau.smlab.syntech.controller.executor.ControllerExecutor;
import tau.smlab.syntech.controller.jit.BasicJitController;

public class MainClass {
	public static void main(String args[]) throws Exception {
		// Read variant_num from the programs arguments with default value set to 1.
		int variant_num;
		try {
			variant_num = Integer.parseInt(args[0]);
			if (variant_num < 1 || variant_num > 3) {
				variant_num = 1;
			}
		}
		catch (Exception e)
		{
			variant_num = 1;
		}

		int x = 5;
		int y = 5;
		final int num_obstacles = 5;
		final int num_targets = 3;

		Point[] obstacles = new Point[num_obstacles];
		obstacles[0] = new Point(1, 1);		
		obstacles[1] = new Point(2, 1);
		obstacles[2] = new Point(0, 3);
		obstacles[3] = new Point(3, 3);
		obstacles[4] = new Point(3, 4);
		
		Point[] goals = new Point[num_targets]; // i = 0: targetA; i = 1: targetB; i = 2: targetC;
		
		// Initialize set with all points that could not serve as targets
		Set<Point> forbbiden_points = new HashSet<>();
		for (int i = 0; i < num_obstacles; i++) {
			forbbiden_points.add(obstacles[i]);
		}
		// targets cannot be located on origin or (4,4) due to unrealizability.
		forbbiden_points.add(new Point(0,0));
		forbbiden_points.add(new Point(4,4)); 


		// Choose targets randomly
		Random rand = new Random();
		for (int i = 0; i < num_targets; i++) {
			Point rand_target;
			do {
				rand_target = new Point(rand.nextInt(x - 1), rand.nextInt(y - 1));
			}
			while (forbbiden_points.contains(rand_target));
			goals[i] = rand_target;
			forbbiden_points.add(rand_target);
		}
		
		
		ControlPanel cp;
		String path = "out//";

		System.out.println("Running the system");
		cp = new ControlPanel(x, y, obstacles, goals, path, variant_num);
		cp.init();

	}
}
