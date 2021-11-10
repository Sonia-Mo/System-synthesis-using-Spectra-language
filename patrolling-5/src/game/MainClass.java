package game;

import java.util.HashMap;
import java.util.Map;

import tau.smlab.syntech.controller.executor.ControllerExecutor;
import tau.smlab.syntech.controller.jit.BasicJitController;

public class MainClass {
	public static void main(String args[]) throws Exception {

		int x = 9;
		int y = 9;
		int num_robots = 1;

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


		// TODO WTF
		Point[] goals = new Point[num_robots];
		goals[0] = new Point(8, 8);

		ControlPanel cp;
		String path = "out//";

		System.out.println("Running the system");
		cp = new ControlPanel(x, y, num_robots, obstacles, goals, path);
		cp.init();

	}
}
