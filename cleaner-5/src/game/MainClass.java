package game;

import java.util.HashSet;
import java.util.Set;

public class MainClass {
	public static void main(String args[]) throws Exception {

		int x = 8;
		int y = 8;
		final int num_obstacles = 12;


		Point[] obstacles = new Point[num_obstacles];
		obstacles[0] = new Point(1, 1);		
		obstacles[1] = new Point(2, 1);
		obstacles[2] = new Point(3, 1);
		obstacles[3] = new Point(4, 1);
		obstacles[4] = new Point(5, 1);
		obstacles[5] = new Point(1, 4);		
		obstacles[6] = new Point(2, 4);
		obstacles[7] = new Point(3, 4);
		obstacles[8] = new Point(4, 4);
		obstacles[9] = new Point(5, 4);
		obstacles[10] = new Point(1, 7);		
		obstacles[11] = new Point(4, 7);
		
		//TODO add doc
		int[] y_coord = {0, 2, 3, 5, 6};
		Set<Point> orange_zone = new HashSet<>();
		for (int x_coord = 1; x_coord <= 5; x_coord++) {
			for (int i = 0; i < y_coord.length; i++) {
				orange_zone.add(new Point(x_coord, y_coord[i]));
			}
		}
	
		
		ControlPanel cp;
		String path = "out//";

		System.out.println("Running the system");
		cp = new ControlPanel(x, y, obstacles, orange_zone, path);
		cp.init();
	}
}
