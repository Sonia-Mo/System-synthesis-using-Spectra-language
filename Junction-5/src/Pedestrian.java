class Pedestrian extends JunctionElement {

	private final int BOARD_WIDTH = 800;
	private final int BOARD_HEIGHT = 800;
	private int PEDESTRIAN_SPEED = 5; // speed in pixels for every draw iteration
	private int _degree = 0; // current degree (0 - north, 90 - east, 180 - south, 270 - west)
	private int _stopLineFirst = 0; // pixel coordinates of the stop line before the first traffic light
	private int _stopLineSecond = 0; // pixel coordinates of the stop line before the second traffic light
	private int EPSILON = 10; // epsilon for pixel distance
	private PedestrianState _pedState = PedestrianState.MOVING; // current state of the pedestrian
	private int _randomImage; // each time a different car will be presented to GUI

	public Pedestrian(int x, int y) {
		this(x, y, 0);
	}

	public Pedestrian(int x, int y, int deg) {
		super(x, y);
		this._degree = deg;
		this._randomImage = JunctionElement._random.nextInt(2);
		getImageForDegree();
		calculateStopLine();
	}

	private void getImageForDegree() {
		loadImage("img/Images/" + Integer.toString(this._degree) + "/man" + this._randomImage + ".png");
		getImageDimensions();

	}

	private void calculateStopLine() {
		switch (this._degree) {
		case 0: // north
			this._stopLineFirst = 210;
			this._stopLineSecond = 440;
			break;
		case 90: // east
			this._stopLineFirst = 585;
			this._stopLineSecond = 358;
			break;
		case 180: // south
			this._stopLineFirst = 585;
			this._stopLineSecond = 355;
			break;
		case 270: // west
			this._stopLineFirst = 210;
			this._stopLineSecond = 440;
			break;
		}
	}

	public void move() {
		/*
		 * For each case: move the pedestrian in the right direction according to its degree
		 */
		switch (this._degree) {
		case 0:
			this._y += PEDESTRIAN_SPEED;
			break;
		case 90:
			this._x -= PEDESTRIAN_SPEED;
			break;
		case 180:
			this._y -= PEDESTRIAN_SPEED;
			break;
		case 270:
			this._x += PEDESTRIAN_SPEED;
			break;
		default:
			this._y += PEDESTRIAN_SPEED;
		}
		// pedestrian is out of screen borders
		if (this._y > BOARD_HEIGHT || this._y < 0 || this._x > BOARD_WIDTH || this._x < 0) {
			this._visible = false;
		}
	}

	public String getRelevantSpectraVariable(boolean environment) {
		int pos = this._degree == 0 || this._degree == 180 ? this._x : this._y;
		String first = "";
		String second = "";
		String direction = "";
		StringBuilder relevantTrafficLight = new StringBuilder(environment ? "pedestrians" : "green");
		if (this._degree == 0) {
			first = "[0]";
			second = "[1]";
			direction = pos < 400 ? "West" : "East";
		} else if (this._degree == 90) {
			first = "[1]";
			second = "[0]";
			direction = pos < 400 ? "North" : "South";
		} else if (this._degree == 180) {
			first = "[1]";
			second = "[0]";
			direction = pos < 400 ? "West" : "East";
		} else if (this._degree == 270) {
			first = "[0]";
			second = "[1]";
			direction = pos < 400 ? "North" : "South";
		}
		relevantTrafficLight.append(direction).append(environment ? "Pressed" : "Pedestrians");
		if (this._pedState == PedestrianState.WAITING_FIRST || this._pedState == PedestrianState.MOVING)
			relevantTrafficLight.append(first);
		else
			relevantTrafficLight.append(second);
		return relevantTrafficLight.toString();
	}

	// determines if a pedestrians needs to stop before its relevant traffic lights
	public boolean needsToStop(boolean isGreen) {
		boolean shouldStop = false;
		// pedestrians that crossing don't need to stop
		if (_pedState == PedestrianState.CROSSING_SECOND)
			return false;
		// pedestrian is going towards or waiting for first traffic light
		if (_pedState == PedestrianState.MOVING || _pedState == PedestrianState.WAITING_FIRST) {
			if (this._degree == 0 && (this._y + this._image.getHeight(null)) + EPSILON >= this._stopLineFirst) {
				_pedState = PedestrianState.WAITING_FIRST;
				shouldStop = true;
			} else if (this._degree == 90 && this._x - EPSILON <= this._stopLineFirst) {
				_pedState = PedestrianState.WAITING_FIRST;
				shouldStop = true;
			} else if (this._degree == 180 && (this._y - EPSILON) <= this._stopLineFirst) {
				_pedState = PedestrianState.WAITING_FIRST;
				shouldStop = true;
			} else if (this._degree == 270 && (this._x + this._image.getWidth(null) + EPSILON) >= this._stopLineFirst) {
				_pedState = PedestrianState.WAITING_FIRST;
				shouldStop = true;
			}
		}
		// pedestrian is crossing towards or waiting for second traffic light
		else if (_pedState == PedestrianState.CROSSING_FIRST || _pedState == PedestrianState.WAITING_SECOND) {
			if (this._degree == 0 && (this._y + this._image.getHeight(null)) + EPSILON >= this._stopLineSecond) {
				_pedState = PedestrianState.WAITING_SECOND;
				shouldStop = true;
			} else if (this._degree == 90 && this._x - EPSILON <= this._stopLineSecond) {
				_pedState = PedestrianState.WAITING_SECOND;
				shouldStop = true;
			} else if (this._degree == 180 && (this._y - EPSILON) <= this._stopLineSecond) {
				_pedState = PedestrianState.WAITING_SECOND;
				shouldStop = true;
			} else if (this._degree == 270 && (this._x + this._image.getWidth(null) + EPSILON) >= this._stopLineSecond) {
				_pedState = PedestrianState.WAITING_SECOND;
				shouldStop = true;
			}
		}
		if (isGreen)
			shouldStop = false;
		return shouldStop;
	}
	
	// checks if the pedestrian went past its relevant crossline (out of the junction but still on screen) 
	public boolean hasCrossed() {
		if (this._pedState == PedestrianState.CROSSING_SECOND) {
			if (this._degree == 0 && (this._y >= 610)) { // north crossline
				_pedState = PedestrianState.FINISHED;
			} else if (this._degree == 90 && this._x + this._image.getWidth(null) <= 195) { // east crossline
				_pedState = PedestrianState.FINISHED;
			} else if (this._degree == 180 && (this._y + this._image.getHeight(null)) <= 200) { // south crossline
				_pedState = PedestrianState.FINISHED;
			} else if (this._degree == 270 && (this._x >= 605)) { // west crossline
				_pedState = PedestrianState.FINISHED;
			}
		}
		return _pedState == PedestrianState.FINISHED;
	}

	public PedestrianState getPedestrianState() {
		return _pedState;
	}

	public void setPedestrianState(PedestrianState s) {
		_pedState = s;
	}

	public void changeSpeed(boolean enableFastForward) {
		this.PEDESTRIAN_SPEED = enableFastForward ? 10 : 5;
	}
}
