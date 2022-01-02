class Car extends JunctionElement {
	private int _randomImage; // each time a different car will be presented to GUI
	private int EPSILON = 25; // epsilon for pixel distance
	private final int BOARD_WIDTH = 890; // including car size
	private final int BOARD_HEIGHT = 890; // including car size
	private int CAR_SPEED = 10; // speed in pixels for every draw iteration
	private int _degree = 0; // current degree (0 - north, 90 - east, 180 - south, 270 - west)
	private CarDirection _direction; // current direction to turn (right / left / straight )
	private VehicleOptions _isEmergency; // flag for marking emergency vehicles
	private int _laneCode = -1; // the code for the ORIGIN lane = (degree/90)*10 + (0-right/1-straight/2-left) for unique encoding
	private int _stopLine = 0; // pixel coordinates of the stop line before the traffic light
	private CarState _carState = CarState.MOVING; // current state of the vehicle

	public Car(int x, int y, int deg, CarDirection dir) {
		this(x, y, deg, dir, false);
	}

	public Car(int x, int y, int deg, CarDirection dir, boolean isEmergency) {
		super(x, y);
		this._degree = deg;
		this._direction = dir;
		this._isEmergency = isEmergency ? VehicleOptions.EMERGENCY : VehicleOptions.CAR; // set vehicle type
		this._randomImage = isEmergency ? JunctionElement._random.nextInt(3) : JunctionElement._random.nextInt(4); // get random image of the tipe
		this._laneCode = (deg / 90) * 10 + dir.getValue(); // calculate lane code e.g. 21 = 180 + straight
		calculateStopLine();
		getImageForDegree();
	}

	private void calculateStopLine() {
		switch (this._degree) {
		case 0: // north
			this._stopLine = 170;
			break;
		case 90: // east
			this._stopLine = 625;
			break;
		case 180: // south
			this._stopLine = 625;
			break;
		case 270: // west
			this._stopLine = 170;
			break;
		}
	}

	private void getImageForDegree() {
		loadImage("img/Images/" + Integer.toString(this._degree) + (this._isEmergency == VehicleOptions.EMERGENCY ? "/emergency" : "/car") + _randomImage + ".png");
		getImageDimensions();

	}

	public void move() {
		/*
		 * For each case: move the car in the right direction according to its degree makeTurn will use the stopline+scalar to determine where to turn to right lane
		 */
		switch (this._degree) {
		case 0:
			this._y += CAR_SPEED;
			makeTurn(this._stopLine + (this._direction == CarDirection.RIGHT ? 30 : 260), this._y);
			break;
		case 90:
			this._x -= CAR_SPEED;
			makeTurn(-1 * this._stopLine + (this._direction == CarDirection.RIGHT ? 70 : 300), -1 * this._x);
			break;
		case 180:
			this._y -= CAR_SPEED;
			makeTurn(-1 * this._stopLine + (this._direction == CarDirection.RIGHT ? 77 : 305), -1 * this._y);
			break;
		case 270:
			this._x += CAR_SPEED;
			makeTurn(this._stopLine + (this._direction == CarDirection.RIGHT ? 30 : 260), this._x);
			break;
		}
		// car is out of screen borders
		if (this._y > BOARD_HEIGHT || this._y < 0 || this._x > BOARD_WIDTH || this._x < 0) {
			this._visible = false;
		}
	}

	private void makeTurn(int turnLine, int pos) {
		if (this._direction == CarDirection.STRAIGHT)
			return; // straight never turns
		if (pos >= turnLine) { // passed the turn line
			if (this._direction == CarDirection.LEFT) {
				// sets new degree for left turn
				this._degree = this._degree == 0 ? 270 : this._degree - 90;
			} else {
				// sets new degree for right turn
				this._degree = this._degree == 270 ? 0 : this._degree + 90;
			}
			// after turning - all cars go straigt on same axis
			this._direction = CarDirection.STRAIGHT;
			// update the cars image according to new angle
			getImageForDegree();
		}
	}

	// determines if a car needs to stop before its traffic lights
	public boolean needsToStop(boolean isGreen) {
		boolean shouldStop = false;
		// cars that are crossing or finished crossing don't need to stop
		if (_carState == CarState.CROSSING || _carState == CarState.FINISHED)
			return false;
		else if (this._degree == 0 && (this._y + this._image.getHeight(null)) + EPSILON >= this._stopLine) {
			_carState = CarState.WAITING;
			shouldStop = true;
		} else if (this._degree == 90 && this._x - EPSILON <= this._stopLine) {
			_carState = CarState.WAITING;
			shouldStop = true;
		} else if (this._degree == 180 && (this._y - EPSILON) <= this._stopLine) {
			_carState = CarState.WAITING;
			shouldStop = true;
		} else if (this._degree == 270 && (this._x + this._image.getWidth(null) + EPSILON) >= this._stopLine) {
			_carState = CarState.WAITING;
			shouldStop = true;
		}
		if (isGreen)
			shouldStop = false;
		return shouldStop;
	}

	// returns the relevant name of the cars spectra variable
	public String getRelevantSpectraVariable(boolean environment) {
		String to = "";
		String from = "";
		StringBuilder relevantTraffic = new StringBuilder(environment ? "vehicles" : "green");
		int degree = (this._laneCode / 10) * 90; // restore original degree
		int direction = this._laneCode % 10; // restore original direction
		if (degree == 0) {
			from = "North";
		} else if (degree == 90) {
			from = "East";
		} else if (degree == 180) {
			from = "South";
		} else if (degree == 270) {
			from = "West";
		}
		if (direction == 0) {
			to = "[0]";
		} else if (direction == 1) {
			to = "[1]";
		} else if (direction == 2) {
			to = "[2]";
		}
		return relevantTraffic.append(from).append(environment ? "" : "Vehicles").append(to).toString();
	}

	public CarState getCarState() {
		return _carState;
	}

	public void setCarState(CarState carState) {
		this._carState = carState;
	}

	public CarDirection getDirection() {
		return _direction;
	}

	public int getDegree() {
		return _degree;
	}

	public VehicleOptions isEmergency() {
		return this._isEmergency;
	}

	public void setEmergency(VehicleOptions isEmergency) {
		this._isEmergency = isEmergency;
	}

	public int getLaneCode() {
		return _laneCode;
	}

	public void changeSpeed(boolean enableFastForward) {
		this.CAR_SPEED = enableFastForward ? 25 : 10;
	}

	// checks if the car went past its relevant crossline (out of the junction but still on screen) 
	public boolean hasCrossed() {
		if (this._carState == CarState.CROSSING) {
			if (this._degree == 0 && (this._y >= 650)) { // north crossline
				_carState = CarState.FINISHED;
			} else if (this._degree == 90 && this._x + this._image.getWidth(null) <= 155) { // east crossline
				_carState = CarState.FINISHED;
			} else if (this._degree == 180 && (this._y + this._image.getHeight(null)) <= 160) { // south crossline
				_carState = CarState.FINISHED;
			} else if (this._degree == 270 && (this._x) >= 645) { // west crossline
				_carState = CarState.FINISHED;
			}
		}
		return _carState == CarState.FINISHED;
	}
}
