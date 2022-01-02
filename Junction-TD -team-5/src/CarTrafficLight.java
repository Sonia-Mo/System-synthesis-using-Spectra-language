
class CarTrafficLight extends PedestrianTrafficLight {
	private int _trafficLightColor = -1;
	private int _degree = 0;

	public CarTrafficLight(int x, int y) {
		this(x, y, 0);
	}

	public CarTrafficLight(int x, int y, int degree) {
		super(x, y);
		this._degree = degree;
		changeColor(0);
	}

	public void changeColor(int newColor) {
		if (this._trafficLightColor != newColor) {
			String stateToFileName = "img/Images/" + Integer.toString(this._degree) + "/"
					+ Integer.toString(newColor) + ".png";
			loadImage(stateToFileName);
			getImageDimensions();
			this._trafficLightColor = newColor;
		}
	}

	public int getLight() {
		return this._trafficLightColor;
	}

	/*
	 * traffic lights are binary encoded by bits:[down,right,up,left]
	 * e.g. 1011 (11 decimal) means that down & up & left are green and will map to 11.png
	 */
	public void updateState(boolean left, boolean straight, boolean right) {
		int a = left ? 1 : 0;
		int b = straight ? 2 : 0;
		int c = right ? 4 : 0;
		changeColor (a + b + c);
	}
}
