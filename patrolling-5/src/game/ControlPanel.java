package game;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;

import tau.smlab.syntech.controller.executor.ControllerExecutor;
import tau.smlab.syntech.controller.jit.BasicJitController;

/*
 * Manages the simulation - GUI, controller input/output, board (visualization)
 */

public class ControlPanel {
	// board dimensions
	int x;
	int y;
	// board constants
	final int dim = 100;
	static final int y_offset = 30;

	int num_robots;
	int num_obstacles;
	Point[] robots;
	Point[] obstacles;
	Point[] goals;

	// holds the robots previous position (for use when animating transitions)
	Point[] robots_prev = new Point[num_robots];

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

	public ControlPanel(int x, int y, int num_robots, Point[] obstacles, Point[] goals, String path) {
		this.x = x;
		this.y = y;
		this.num_robots = num_robots;
		this.num_obstacles = obstacles.length;
		this.robots = new Point[num_robots];
		this.robots_prev = new Point[num_robots];
		this.obstacles = obstacles;
		this.goals = goals;
		this.path = path;
	}

	public void init() throws Exception {
		autorun = false;

		for (int i = 0; i < num_robots; i++) {
			robots[i] = new Point();
			robots_prev[i] = new Point();
		}

		// init controller
		executor = new ControllerExecutor(new BasicJitController(), this.path);
		executor.initState(inputs);

		Map<String, String> sysValues = executor.getCurrOutputs();

		// set initial robot locations
		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots_prev[i].setY(Integer.parseInt(sysValues.get("robotY")));
			robots[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots[i].setY(Integer.parseInt(sysValues.get("robotY")));
		}

		setUpUI();
	}

	// handle next turn
	void next() throws Exception {
		ready_for_next = false;
		advance_button.setText("...");
		for (int i = 0; i < num_robots; i++) {
			robots_prev[i].setX(robots[i].getX());
			robots_prev[i].setY(robots[i].getY());
		}
		executor.updateState(inputs);

		// Receive updated values from the controller
		Map<String, String> sysValues = executor.getCurrOutputs();

		// Update robot locations
		for (int i = 0; i < num_robots; i++) {
			robots[i].setX(Integer.parseInt(sysValues.get("robotX")));
			robots[i].setY(Integer.parseInt(sysValues.get("robotY")));
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

}
