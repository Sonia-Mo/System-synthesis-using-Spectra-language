package game;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;

import tau.smlab.syntech.controller.executor.ControllerExecutor;
import tau.smlab.syntech.controller.jit.BasicJitController;

/*
 * Manages the simulation - GUI, controller input/output, board (visualization)
 */

enum Color {RED, GREEN, YELLOW}

public class ControlPanel {
	// board dimensions
	int x;
	int y;
	// board constants
	final int dim = 100;
	static final int y_offset = 30;
	
	int num_robots;
	int num_obstacles;
	int variant_num;
	Color origin_color;
	Color[] targets_color; // i = 0: targetA; i = 1: targetB; i = 2: targetC;
	int engine_cooldown_counter;
	boolean engine_problem;

	Point robot;
	Point[] obstacles;
	Point[] goals; // i = 0: targetA; i = 1: targetB; i = 2: targetC;
	
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

	public ControlPanel(int x, int y, Point[] obstacles, Point[] goals, String path, int variant_num) {
		this.x = x;
		this.y = y;
		this.num_obstacles = obstacles.length;
		this.variant_num = variant_num;
		this.obstacles = obstacles;
		this.goals = goals;
		this.path = path;
	}

	public void init() throws Exception {
		autorun = false;

		robot = new Point();
		robot_prev = new Point();
		
		// init controller
		executor = new ControllerExecutor(new BasicJitController(), this.path);
		
		// Choose targets randomly
		inputs.put("targetA[0]", Integer.toString(goals[0].getX()));
		inputs.put("targetA[1]", Integer.toString(goals[0].getY()));
		inputs.put("targetB[0]", Integer.toString(goals[1].getX()));
		inputs.put("targetB[1]", Integer.toString(goals[1].getY()));
		inputs.put("targetC[0]", Integer.toString(goals[2].getX()));
		inputs.put("targetC[1]", Integer.toString(goals[2].getY()));
		
		
		switch (this.variant_num) {
		case 2: 
			// Set initial origin color
			this.origin_color = Color.RED;
			break;
		case 3:
			// No engine problem at the beginning of the run
			engine_problem = false;
			inputs.put("engine_problem", Boolean.toString(engine_problem));
			engine_cooldown_counter = 15;
			break;
		}

		executor.initState(inputs);

		Map<String, String> sysValues = executor.getCurrOutputs();
		
		// Set initial robot locations
		robot_prev.setX(Integer.parseInt(sysValues.get("robotX")));
		robot_prev.setY(Integer.parseInt(sysValues.get("robotY")));
		robot.setX(Integer.parseInt(sysValues.get("robotX")));
		robot.setY(Integer.parseInt(sysValues.get("robotY")));
		
		targets_color = new Color[goals.length];
		for (int i = 0; i < targets_color.length; i++) {
			this.targets_color[i] = Color.GREEN;
		}

		setUpUI();
	}

	// Handle next turn
	void next() throws Exception {
		ready_for_next = false;
		advance_button.setText("...");
		robot_prev.setX(robot.getX());
		robot_prev.setY(robot.getY());
		
		switch(variant_num) {
		case 3:
			// Keep engine problem true if robot is still not at origin.
			if (engine_problem && !(robot_prev.getX() == 0 && robot_prev.getY() == 0)) {
				engine_problem = true;
			// Reset engine cooldown counter and turn off engine problem if robot got to origin.
			} else if (engine_problem && robot_prev.getX() == 0 && robot_prev.getY() == 0) {
				engine_cooldown_counter = 0;
				engine_problem = false;
			// Keep engine problem false while engine cooldown is less than 15. 
			} else if (!engine_problem && engine_cooldown_counter < 15) {
				engine_problem = false;	
				engine_cooldown_counter++; 
			// In case that engine problem is false and engine cooldown is greater than 14, randomize next value of engine problem.
			} else {
				Random random = new Random();
				engine_problem = random.nextBoolean();
			}
			inputs.put("engine_problem", Boolean.toString(engine_problem));		
			break;
		}

		executor.updateState(inputs);

		// Receive updated values from the controller
		Map<String, String> sysValues = executor.getCurrOutputs();
		
		// Update robot locations
		robot.setX(Integer.parseInt(sysValues.get("robotX")));
		robot.setY(Integer.parseInt(sysValues.get("robotY")));
	
		
		switch (variant_num) {
		case 2: 
			this.origin_color = Color.valueOf(sysValues.get("origin_color"));
			break;
		case 3: 
			this.targets_color[0] = Color.valueOf(sysValues.get("targetA_color"));
			this.targets_color[1] = Color.valueOf(sysValues.get("targetB_color"));
			this.targets_color[2] = Color.valueOf(sysValues.get("targetC_color"));
			break;
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
