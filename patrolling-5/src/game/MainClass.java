package game;

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
		
		ControlPanel cp;
		String path = "out//";

		System.out.println("Running the system");
		cp = new ControlPanel(x, y, obstacles, path);
		cp.init();
	}
}
