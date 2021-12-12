
class PedestrianTrafficLight extends JunctionElement {
	private int _trafficLightColor = -1;
	private int _degree = 0;
	private boolean _isMiddle = false;

	public PedestrianTrafficLight(int x, int y) {
		this(x, y, 0, false);
	}
	
	public PedestrianTrafficLight(int x, int y, int degree, boolean isMiddle) {
		super(x, y);
		this._isMiddle = isMiddle;
		this._degree = degree;
		changeColor(0);
	}

	public void changeColor(int newColor) {
		if (this._trafficLightColor != newColor) {
			String stateToFileName = "img/pedLight/" + Integer.toString(newColor) + ".png";
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
	public void updateState(boolean up, boolean down ,boolean left, boolean right) {
		if(this._isMiddle) { //middle traffic light is the one on the traffic island
			if(this._degree == 0 || this._degree == 180) { //side mid lights cannot show up and downs
				up = false;
				down = false;
			}
			else { //up and down mid lights cannot show sides
				right = false;
				left = false;
			}
		}
		int a = left ? 1 : 0;
		int b = up ? 2 : 0;
		int c = right ? 4 : 0;
		int d = down ? 8 : 0;
		changeColor (a + b + c + d);
	}
}
