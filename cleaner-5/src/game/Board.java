package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
// Responsible for visualization/animation of transitions
public class Board extends JPanel {

	int animation_steps = 0;
	Timer timer;
	ControlPanel cp;

	double robot_rotation;

	// The robots current, start, and target locations for a single transition
	// - used for animation
	Point robot_graphics;
	Point start_graphics;
	Point target_graphics;

	BufferedImage buffer;
	BufferedImage robot_image;
	BufferedImage robot_blue_image;
	BufferedImage robot_green_image;
	BufferedImage robot_red_image;
	BufferedImage base_robot_image;
	BufferedImage obstacle_image;
	BufferedImage target_image;
	BufferedImage orange_zone_image;

	public Board(ControlPanel cp) {
		super();
		this.cp = cp;
	}

	public void init() throws Exception {
		start_graphics = new Point();
		target_graphics = new Point();
		robot_graphics = new Point();

		obstacle_image = ImageIO.read(new File("img/Obstacle.png"));
		target_image = ImageIO.read(new File("img/Goal0.png"));

		// Load images for different robot colors
		robot_blue_image = ImageIO.read(new File("img/Robot2.png"));
		robot_red_image = ImageIO.read(new File("img/Robot0.png"));
		robot_green_image = ImageIO.read(new File("img/Robot1.png"));

		base_robot_image = robot_blue_image;
		robot_image = base_robot_image;

		orange_zone_image = ImageIO.read(new File("img/orange_cube.png"));
	}

	// Animate a transition (from robots_prev to robots)
	public void animate() throws Exception {

		update_base_robot_image();

		int diff_x = cp.robot.getX() - cp.robot_prev.getX();
		int diff_y = cp.robot.getY() - cp.robot_prev.getY();
		
		// Whenever the robot reaches the orange zone, it repeatedly switches its usual appearance
		// Its appearance rotates 90 degrees to the right at each step, until it leaves the orange zone
		if (cp.orange_zone.contains(cp.robot)) {
			switch (cp.robot_rotation) {
			case DEG_0:
				robot_rotation = 0;
				break;
			case DEG_90:
				robot_rotation = Math.PI / 2;
				break;
			case DEG_180:
				robot_rotation = Math.PI;
				break;
			case DEG_270:
				robot_rotation = 3 * Math.PI / 2;
				break;
			}
		} else {
			// rotate robots based on direction
			switch (diff_x) {
			case -1:
				robot_rotation = 3 * Math.PI / 2;
				break;
			case 1:
				robot_rotation = Math.PI / 2;
				break;
			}
			switch (diff_y) {
			case -1:
				robot_rotation = 0;
				break;
			case 1:
				robot_rotation = Math.PI;
				break;
			}
		}
		robot_image = Utility.rotate(base_robot_image, robot_rotation);

		robot_graphics.setX(cp.robot_prev.getX() * cp.dim);
		robot_graphics.setY(cp.robot_prev.getY() * cp.dim);
		start_graphics.setX(cp.robot_prev.getX() * cp.dim);
		start_graphics.setY(cp.robot_prev.getY() * cp.dim);
		target_graphics.setX(cp.robot.getX() * cp.dim);
		target_graphics.setY(cp.robot.getY() * cp.dim);

		// Each tick of the timer advances the animation
		timer = new Timer(16, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int num_steps = 20;
				if (animation_steps > num_steps)
				// Animation ended
				{
					timer.stop();
					animation_steps = 0;
					cp.ready_for_next = true;
					if (cp.autorun) {
						try {
							cp.next();
						} catch (Exception ex) {
							System.out.println(ex);
						}
					} else {
						cp.advance_button.setText("Next step");
					}
					return;
				}
				// Update robot location for current animation step
				robot_graphics.setX((int) (start_graphics.getX() * (1 - (double) (animation_steps) / num_steps)
						+ target_graphics.getX() * ((double) (animation_steps) / num_steps)));
				robot_graphics.setY((int) (start_graphics.getY() * (1 - (double) (animation_steps) / num_steps)
						+ target_graphics.getY() * ((double) (animation_steps) / num_steps)));

				animation_steps++;
				// Redraw
				updateBuffer();
				repaint();
			}
		});

		timer.start();
	}

	@Override
	public void invalidate() {
		buffer = null;
		// updateBuffer();
		super.invalidate();
	}

	// Use buffering for smooth animations
	protected void updateBuffer() {
		if (getWidth() > 0 && getHeight() > 0) {

			if (buffer == null) {
				buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			}

			Graphics2D g2d = buffer.createGraphics();
			g2d.clearRect(0, 0, cp.x * cp.dim + 8, cp.y * cp.dim + 8);
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

			int row;
			int col;
			// Draw background
			for (row = 0; row < cp.y; row++) {
				for (col = 0; col < cp.x; col++) {
					if ((row % 2) == (col % 2))
						g2d.setColor(Color.WHITE);
					else
						g2d.setColor(Color.WHITE);

					g2d.fillRect(col * cp.dim, row * cp.dim, cp.dim, cp.dim);
				}
			}

			g2d.setColor(Color.WHITE);
			// g2d.drawImage(robot_image, temp, dim, null);

			// Draw orange zone
			for (Point point : cp.orange_zone) {
				g2d.drawImage(orange_zone_image, point.getX() * cp.dim, point.getY() * cp.dim, null);
			}

			// Draw target - if there is no cleaning request, ignore the target
			if (cp.cleaning_request) {
				g2d.drawImage(target_image, cp.target.getX() * cp.dim, cp.target.getY() * cp.dim, null);
			}

			// Draw robots
			g2d.drawImage(robot_image, robot_graphics.getX(), robot_graphics.getY(), null);

			// Draw obstacles
			for (int i = 0; i < cp.num_obstacles; i++) {
				g2d.drawImage(obstacle_image, cp.obstacles[i].getX() * cp.dim, cp.obstacles[i].getY() * cp.dim, null);
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		if (buffer != null) {
			g2d.drawImage(buffer, 0, 0, this);
		}
	}

	// Update base robot image:
	// In the 8 initial steps robot is blue and than it turns red until green light is on (permission_to_move = false).
	// When robot reaches a target it turns green.
	// Whenever robot is waiting at orange zone (permission_to_move = false) it turns red. 
	// In all other cases, robot is blue.
	public void update_base_robot_image() {
		if (cp.initial_wait_count < 8) {
			base_robot_image = robot_blue_image;
		} else if (!cp.permission_to_move) {
			base_robot_image = robot_red_image;
		} else if (cp.robot.equals(cp.target)) {
			base_robot_image = robot_green_image;
		} else {
			base_robot_image = robot_blue_image;
		}
	}
}