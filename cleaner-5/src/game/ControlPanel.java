package game;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;

import tau.smlab.syntech.controller.executor.ControllerExecutor;
import tau.smlab.syntech.controller.jit.BasicJitController;

/*
 * Manages the simulation - GUI, controller input/output, board (visualization)
 */

enum Color {
	RED, GREEN, BLUE
}

enum Rotation {
	DEG_0, DEG_90, DEG_180, DEG_270
}

public class ControlPanel {
	// board dimensions
	int x;
	int y;
	// board constants
	final int dim = 100;
	static final int y_offset = 30;

	int num_obstacles;
	Color robot_color;
	boolean green_light;
	boolean cleaning_request;
	Set<Point> forbbiden_points = new HashSet<>();
	Random rand = new Random();
	int orange_steps;
	Rotation robot_rotation;
	int initial_wait_count;
	boolean permission_to_move;
	Set<Point> orange_zone;

	Point robot;
	Point[] obstacles;
	Point target;

	// holds the robots previous position (for use when animating transitions)
	Point robot_prev;

	// Board and GUI elements
	JFrame frame;
	Board board;
	JButton advance_button;
	JButton autorun_button;

	// holds states for the animation
	boolean ready_for_next;
	boolean autorun;

	// The controller and its inputs
	ControllerExecutor executor;
	Map<String, String> inputs = new HashMap<String, String>();

	// The path to the controller files
	String path;

	public ControlPanel(int x, int y, Point[] obstacles, Set<Point> orange_zone, String path) {
		this.x = x;
		this.y = y;
		this.num_obstacles = obstacles.length;
		this.obstacles = obstacles;
		this.path = path;
		this.orange_zone = orange_zone;

		// Initialize set with all points that could not serve as target
		for (int i = 0; i < num_obstacles; i++) {
			forbbiden_points.add(obstacles[i]);
		}
		// Target cannot be located at origin
		forbbiden_points.add(new Point(0, 0));
	}

	public void init() throws Exception {
		autorun = false;

		robot = new Point();
		robot_prev = new Point();

		// init controller
		executor = new ControllerExecutor(new BasicJitController(), this.path);

		// Randomize the decision of cleaning request, if cleaning_request = 1, choose
		// random target to clean.
		cleaning_request = rand.nextBoolean();
		inputs.put("cleaning_request", Boolean.toString(cleaning_request));
		target = randomizeTarget();
		inputs.put("target[0]", Integer.toString(target.getX()));
		inputs.put("target[1]", Integer.toString(target.getY()));

		inputs.put("green_light", Boolean.toString(rand.nextBoolean()));

		executor.initState(inputs);

		Map<String, String> sysValues = executor.getCurrOutputs();

		// Set initial robot locations
		robot_prev.setX(Integer.parseInt(sysValues.get("robotX")));
		robot_prev.setY(Integer.parseInt(sysValues.get("robotY")));
		robot.setX(Integer.parseInt(sysValues.get("robotX")));
		robot.setY(Integer.parseInt(sysValues.get("robotY")));

		setUpUI();
	}

	// Handle next turn
	void next() throws Exception {
		ready_for_next = false;
		advance_button.setText("...");
		robot_prev.setX(robot.getX());
		robot_prev.setY(robot.getY());

		// TODO: add doc
		if (cleaning_request && robot.equals(target) || !cleaning_request) {
			cleaning_request = rand.nextBoolean();
			if (cleaning_request) {
				target = randomizeTarget();
			}
		}
		inputs.put("cleaning_request", Boolean.toString(cleaning_request));
		inputs.put("target[0]", Integer.toString(target.getX()));
		inputs.put("target[1]", Integer.toString(target.getY()));

		green_light = rand.nextBoolean();
		inputs.put("green_light", Boolean.toString(green_light));

		executor.updateState(inputs);

		// Receive updated values from the controller
		Map<String, String> sysValues = executor.getCurrOutputs();

		// Update robot locations
		robot.setX(Integer.parseInt(sysValues.get("robotX")));
		robot.setY(Integer.parseInt(sysValues.get("robotY")));

		// TODO: add doc
		orange_steps = Integer.parseInt(sysValues.get("orange_steps"));
		robot_rotation = Rotation.valueOf(sysValues.get("robot_rotation"));

		if (initial_wait_count < 8) {
			initial_wait_count++;
		} else if (orange_steps == 5 || orange_steps == 6) {
			permission_to_move = false;
		} else if (green_light) {
			permission_to_move = true;
		}

		// Animate transition
		board.animate();
	}

	void setUpUI() throws Exception {
		advance_button = new JButton();
		autorun_button = new JButton();
		frame = new JFrame();
		frame.add(advance_button);
		frame.add(autorun_button);
		board = new Board(this);
		board.init();
		frame.setTitle("Robots");
		frame.setSize(x * dim + 8 + 150, y * dim + y_offset + 8);
		board.setSize(x * dim, y * dim);
		frame.setLayout(null);
		frame.add(board, BorderLayout.CENTER);

		// Handle presses of the "next step" button
		advance_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (ready_for_next && !autorun)
						next();
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});
		advance_button.setBounds(x * dim + 8, 0, 130, 50);
		advance_button.setText("Start");

		// Handle presses of the "autorun/stop autorun" button
		autorun_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (autorun) {
						autorun = false;
						autorun_button.setText("Auto run");
					} else if (ready_for_next) {
						autorun = true;
						autorun_button.setText("Stop Auto run");
						next();
					}
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});
		autorun_button.setBounds(x * dim + 8, 50, 130, 50);
		autorun_button.setText("Auto run");
		frame.setVisible(true);
		board.setVisible(true);
		advance_button.setVisible(true);
		autorun_button.setVisible(true);
		ready_for_next = true;
	}

	Point randomizeTarget() {
		Point rand_target;
		do {
			rand_target = new Point(rand.nextInt(x - 1), rand.nextInt(y - 1));
		} while (forbbiden_points.contains(rand_target));
		return rand_target;
	}

}
