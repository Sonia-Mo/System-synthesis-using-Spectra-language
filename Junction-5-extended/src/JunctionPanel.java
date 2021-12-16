import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;

enum Light {RED, GREEN};
enum TrafficLight {VEHICLE_N_RIGHT, VEHICLE_N_STRAIGHT, VEHICLE_N_LEFT,
	VEHICLE_S_RIGHT, VEHICLE_S_STRAIGHT, VEHICLE_S_LEFT,
	PEDESTRIAN_NW, PEDESTRIAN_NE, PEDESTRIAN_WN, 
	PEDESTRIAN_SW, PEDESTRIAN_SE, PEDESTRIAN_ES};

@SuppressWarnings("serial")
public class JunctionPanel extends BackgroundPanel implements ActionListener {
	private JunctionController controller = new JunctionController();
	private List<Car> _cars; // List of the vehicles on the screen
	private List<Pedestrian> _pedestrians; // List of the pedestrians on the screen
	private CarTrafficLight[] _carsTrafficLights = new CarTrafficLight[4];
	private PedestrianTrafficLight[] _pedsTrafficLights = new PedestrianTrafficLight[8];
	private JunctionElement _fog; // fog image
	private JunctionElement _closedRoad; // closed road image
	private JunctionElement _loading; // image for waiting sign
	private JunctionElement _freezeMode; // image for freeze mode
	private JunctionElement _manualMode; // image for manual mode
	private HashMap<Integer, VehicleOptions> _carsInLanes = new HashMap<>(); // Map that indicates whether a lane is
																				// occupied
	private javax.swing.Timer _timer;

	private boolean eventTime = true;

	private JunctionTimer eventTimer = new JunctionTimer();
	private JunctionTimer trafficTimer = new JunctionTimer();
	private JunctionTimer freezeModeTimer = new JunctionTimer();
	private JunctionTimer fogTimer = new JunctionTimer();
	private JunctionTimer roadConstructionTimer = new JunctionTimer();
	private JunctionTimer manualModeTimer = new JunctionTimer();

	boolean _isFogAction;
	boolean _isClosedRoadAction;
	boolean _isFreezeMode;
	boolean _isManualMode;
	TrafficLight manualTrafficLight = TrafficLight.VEHICLE_N_STRAIGHT;
	Light trafficLightColor = Light.RED;

	public JunctionPanel(MainFrame parentFrame) throws IOException {
		super(ImageIO.read(new File("img/Images/junction800.png")), SCALED);
		setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));
		setPreferredSize(new Dimension(800, 800));
		this._pedestrians = new ArrayList<Pedestrian>();
		this._cars = new ArrayList<Car>();
		this._timer = new javax.swing.Timer(100, this);

		this._fog = new JunctionElement(0, 0);
		this._fog.setVisible(false);
		this._fog.loadImage("img/Images/fog.png");
		this._closedRoad = new JunctionElement(435, 13);
		this._closedRoad.loadImage("img/Images/blocked.png");
		this._loading = new JunctionElement(280, 360);
		this._loading.loadImage("img/Images/ClearWait.png");
		this._loading.setVisible(false);
		this._freezeMode = new JunctionElement(365, 365);
		this._freezeMode.loadImage("img/Images/freeze mode.png");
		this._manualMode = new JunctionElement(355, 355);
		this._manualMode.loadImage("img/Images/manual mode.png");
		initTrafficLights();
		this._timer.start();
	}

	private void initTrafficLights() {
		// car traffic lights
		this._carsTrafficLights[0] = new CarTrafficLight(274, 194, 0); // north
		this._carsTrafficLights[1] = new CarTrafficLight(575, 267, 90); // east
		this._carsTrafficLights[2] = new CarTrafficLight(497, 575, 180); // south
		this._carsTrafficLights[3] = new CarTrafficLight(195, 498, 270); // west

		// pedestrian traffic lights
		this._pedsTrafficLights[0] = new PedestrianTrafficLight(385, 185, 0, true); // top middle
		this._pedsTrafficLights[1] = new PedestrianTrafficLight(588, 185); // top right
		this._pedsTrafficLights[2] = new PedestrianTrafficLight(588, 385, 270, true); // right middle
		this._pedsTrafficLights[3] = new PedestrianTrafficLight(588, 588); // bottom right
		this._pedsTrafficLights[4] = new PedestrianTrafficLight(385, 588, 180, true); // bottom middle
		this._pedsTrafficLights[5] = new PedestrianTrafficLight(185, 588); // bottom left
		this._pedsTrafficLights[6] = new PedestrianTrafficLight(185, 385, 90, true); // left middle
		this._pedsTrafficLights[7] = new PedestrianTrafficLight(185, 185); // top left
	}

	public void createPedestrian(int selection) {
		int fixedSelection = selection - 101;
		int index;
		if (fixedSelection == -1) { // Creating a pedestrian in a random position
			Random r = new Random();
			index = r.nextInt(Definitions.possiblePeds.size());
		} else { // Creating a pedestrian in a user provided position
			index = fixedSelection;
		}
		ArrayList<Integer> selectedPed = Definitions.possiblePeds.get(index);
		this._pedestrians.add(new Pedestrian(selectedPed.get(0), selectedPed.get(1), selectedPed.get(2))); // Add
																											// pedestrian
																											// to the //
																											// list
		repaint();
	}

	public void createVehicle(boolean emergency) {
		ArrayList<Integer> possibleLanes = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 20, 21, 22)); // All possible
																										// lanes for
																										// cars
																										// (Lanecode
		ArrayList<Integer> carValues;
		Collections.shuffle(possibleLanes);
		int index;
		do {
			index = possibleLanes.remove(0);
			carValues = Definitions.possibleCars.get(index);
		} while (!possibleLanes.isEmpty()
				&& !createCar(carValues.get(0), carValues.get(1), carValues.get(2), carValues.get(3), emergency, true));
		createCar(carValues.get(0), carValues.get(1), carValues.get(2), carValues.get(3), emergency, false);
		repaint();
	}

	/* Creates the actual car instance, if failed return false */
	public boolean createCar(int x, int y, int deg, int dir, boolean isEmergency, boolean random) {
		Car car = new Car(x, y, deg, CarDirection.getDirection(dir), isEmergency);
		if (checkOrUpdateLane(car, true, true)) {
			return false;
		}
		if (isEmergency) {
			if (isCollidingEmergency(car)) { // assumption - no emergency on colliding lanes
				return false;
			}
		}
		checkOrUpdateLane(car, true, false); // update that there is a car in this lane
		this._cars.add(car); // Add car to the cars list
		return true;
	}

	/* Checks or update whether there is a car in a lane */
	private boolean checkOrUpdateLane(Car car, boolean val, boolean check) {
		if (check) { // check if there is already car in lane
			return this._carsInLanes.containsKey(car.getLaneCode());
		}
		if (val) { // update the car in the current lane
			this._carsInLanes.put(car.getLaneCode(), car.isEmergency());
		} else {
			this._carsInLanes.remove(car.getLaneCode());
		}
		return false;
	}

	/* Checks if there are emergency vehicles in colliding lanes */
	private boolean isCollidingEmergency(Car car) {
		int relevantLane = car.getLaneCode();
		if (relevantLane % 10 == 0) // for right turns - they never collide
			return false;
		for (int lane : Definitions.emergencyCollidingList.get(relevantLane)) {
			if (this._carsInLanes.getOrDefault(lane, VehicleOptions.NONE) == VehicleOptions.EMERGENCY)
				return true;
		}
		return false;
	}

	/* custom painting method for drawing relevant elements on the screen */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		doDrawing(g);

		Toolkit.getDefaultToolkit().sync();
	}

	private void doDrawing(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;

		for (Pedestrian pedestrian : this._pedestrians) { // paint the updated pedestrians
			g2d.drawImage(pedestrian.getImage(), pedestrian.getX(), pedestrian.getY(), this);
		}
		for (Car car : this._cars) { // paint the updated cars
			g2d.drawImage(car.getImage(), car.getX(), car.getY(), this);
		}
		for (CarTrafficLight trafficLight : this._carsTrafficLights) { // paint updated cars traffic lights
			g2d.drawImage(trafficLight.getImage(), trafficLight.getX(), trafficLight.getY(), this);
		}
		for (PedestrianTrafficLight trafficLight : this._pedsTrafficLights) { // paint updated pedestrians traffic
																				// lights
			g2d.drawImage(trafficLight.getImage(), trafficLight.getX(), trafficLight.getY(), this);
		}
		if (displayRoadConstructions()) { // paint road construction image if needed
			g2d.drawImage(this._closedRoad.getImage(), this._closedRoad.getX(), this._closedRoad.getY(), this);
		}
		if (Boolean.parseBoolean(controller.getEnvVariable("foggy"))) { // paint fog image if needed
			g2d.drawImage(this._fog.getImage(), this._fog.getX(), this._fog.getY(), this);
		}
		if (Boolean.parseBoolean(controller.getEnvVariable("freezeMode"))) {
			g2d.drawImage(this._freezeMode.getImage(), this._freezeMode.getX(), this._freezeMode.getY(), this);
		}
		if (Boolean.parseBoolean(controller.getEnvVariable("manualMode"))) {
			g2d.drawImage(this._manualMode.getImage(), this._manualMode.getX(), this._manualMode.getY(), this);
		}
		if (this._loading.isVisible()) { // paint "waiting for junction to clear" image
			g2d.drawImage(this._loading.getImage(), this._loading.getX(), this._loading.getY(), this);
		}
	}

	private boolean timeForEvent() {
		// if timer is over, switch eventTime value and set a new timer
		if (!eventTimer.isOn()) {
			eventTime = !eventTime;
			if (eventTime) {
				eventTimer.startTimer(2000);
			} else {
				eventTimer.startTimer(4000);
			}
		}
		return eventTime;
	}

	private void addEvents() {
		Random rand = new Random();
		// Assume that fog and freeze mode can not occur together

		if (!fogTimer.isOn() && !freezeModeTimer.isOn() && !manualModeTimer.isOn()) {
			int event = rand.nextInt(3);
			switch (event) {
			case 0:
				manualModeTimer.startTimer(6000);
				break;
			case 1:
				fogTimer.startTimer(3000);
				break;
			case 2:
				freezeModeTimer.startTimer(5000);
				break;
			}
		}
		if (!roadConstructionTimer.isOn()) {
			if (rand.nextBoolean()) {
			roadConstructionTimer.startTimer(4000);
			}
		}
	}

	// Inject traffic periodically every timer cycle
	private void injectTraffic() {
		if (!trafficTimer.isOn() && !isCarsCrossing() && !isPedsCrossing()) {
			createPedestrian(100);
			createVehicle(false);
			createVehicle(true);
			trafficTimer.startTimer(400);
		}
	}

	/*
	 * this method is being called every 25ms and updates the elements state TODO:
	 * Here is a possible location to simulate the junction
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		updateCars();
		updatePedestrians();
		updateFog();
		repaint();

		if (timeForEvent()) {
			addEvents();
		}
		injectTraffic(); // Inject traffic into the junction

		// get a new state from the controller if no cars or pedestrians are crossing
		if (!isCarsCrossing() && !isPedsCrossing()) {
			// Assign env variables to spectra specification			
			boolean roadConstruction = roadConstructionTimer.isOn();
			boolean freezeMode = freezeModeTimer.isOn();
			boolean fog = fogTimer.isOn();
			boolean manualMode = manualModeTimer.isOn();

			setRoadConstructions(roadConstruction);
			setFog(fog);
			setFreezeMode(freezeMode);
			setManualMode(manualMode, roadConstruction); 
			getNewState();
		}
	}
	
	// Set manual mode according to 'manual' value. If both 'manual' and 'roadConstruction' are true then
	// manualTrafficLight is not allowed to be 'VEHICLE_S_STRAIGHT'
	public void setManualMode(boolean manualMode, boolean roadConstruction) {		
		roadConstruction = roadConstructionTimer.isOn();
		Random rand = new Random();
		trafficLightColor = Light.values()[rand.nextInt(2)];
		do {
			// keep choosing random value for manualTrafficLight to avoid the scenario where manual mode
			// and road construction are on, and the value of manualTrafficLight is 'VEHICLE_S_STRAIGHT'.
			manualTrafficLight = TrafficLight.values()[rand.nextInt(12)];
		}
		while (manualMode && roadConstruction && manualTrafficLight == TrafficLight.VEHICLE_S_STRAIGHT);
		
		controller.updateEnvVariable("manualMode", String.valueOf(manualMode));
		controller.updateEnvVariable("manualTrafficLight", String.valueOf(manualTrafficLight));
		controller.updateEnvVariable("trafficLightColor", String.valueOf(trafficLightColor));
		
		// TODO turn on for debugging manual mode:		
//		if (manual) {
//			System.out.println(manualTrafficLight);
//			System.out.println(trafficLightColor);
//		}
	}
	
	public void setFreezeMode(boolean freezeMode) {
		controller.updateEnvVariable("freezeMode", String.valueOf(freezeMode));
	}
	
	public void setFog(boolean fog) {
		controller.updateEnvVariable("foggy", String.valueOf(fog)); // update spectra env variable
	}

	public void setRoadConstructions(boolean roadConstruction) {
		// Update spectra for env road construction
		// Assume that while freeze mode is on and the south straight lane traffic light
		// is green, then road construction should be false.
		if (freezeModeTimer.isOn() && roadConstruction && controller.getSysVariable("greenSouthVehicles[1]")) {
			roadConstruction = false;
		}
		
		controller.updateEnvVariable("roadConstructions", String.valueOf(roadConstruction));
	}

	private boolean constructionsLanesAreClear(boolean init, boolean ignoreState) {
		boolean anyCarInRoadClosing = init;
		HashSet<Integer> constructionLanes = new HashSet<Integer>(Arrays.asList(10, 21, 32));
		for (Car car : this._cars) {
			if (constructionLanes.contains(car.getLaneCode())
					&& (ignoreState || car.getCarState() == CarState.FINISHED))
				anyCarInRoadClosing = false;
		}
		return anyCarInRoadClosing;
	}

	private boolean displayRoadConstructions() { // Check whether we need to display the construction image
		boolean roadConstructions = Boolean.parseBoolean(controller.getEnvVariable("roadConstructions"));
		if (!roadConstructions) {
			return false;
		}
		boolean anyCarInRoadClosing = constructionsLanesAreClear(roadConstructions, false);
		return anyCarInRoadClosing;
	}

	// Checks whether a pedestrian is crossing
	private boolean isPedsCrossing() {
		for (Pedestrian p : _pedestrians) {
			if (p.getPedestrianState() == PedestrianState.CROSSING_FIRST
					|| p.getPedestrianState() == PedestrianState.CROSSING_SECOND)
				return true;
		}
		return false;
	}

	// Checks whether a car is crossing
	private boolean isCarsCrossing() {
		for (Car car : _cars) {
			if (car.getCarState() == CarState.CROSSING)
				return true;
		}
		return false;
	}

	/*
	 * update the GUI traffic lights state with the state that we got from the
	 * spectra controller
	 */
	private void getNewState() {
		controller.updateState();
		_carsTrafficLights[0].updateState(controller.getSysVariable("greenNorthVehicles[2]"),
				controller.getSysVariable("greenNorthVehicles[1]"), controller.getSysVariable("greenNorthVehicles[0]"));
		_carsTrafficLights[2].updateState(controller.getSysVariable("greenSouthVehicles[2]"),
				controller.getSysVariable("greenSouthVehicles[1]"), controller.getSysVariable("greenSouthVehicles[0]"));

		_carsTrafficLights[1].updateState(false, false, false);
		_carsTrafficLights[3].updateState(false, false, false);

		_pedsTrafficLights[0].updateState(false, false, controller.getSysVariable("greenNorthPedestrians[0]"),
				controller.getSysVariable("greenNorthPedestrians[1]"));
		_pedsTrafficLights[1].updateState(false, controller.getSysVariable("greenEastPedestrians[0]"),
				controller.getSysVariable("greenNorthPedestrians[1]"), false);
		_pedsTrafficLights[2].updateState(controller.getSysVariable("greenEastPedestrians[0]"),
				controller.getSysVariable("greenEastPedestrians[1]"), false, false);
		_pedsTrafficLights[3].updateState(controller.getSysVariable("greenEastPedestrians[1]"), false,
				controller.getSysVariable("greenSouthPedestrians[1]"), false);
		_pedsTrafficLights[4].updateState(false, false, controller.getSysVariable("greenSouthPedestrians[0]"),
				controller.getSysVariable("greenSouthPedestrians[1]"));
		_pedsTrafficLights[5].updateState(controller.getSysVariable("greenWestPedestrians[1]"), false, false,
				controller.getSysVariable("greenSouthPedestrians[0]"));
		_pedsTrafficLights[6].updateState(controller.getSysVariable("greenWestPedestrians[0]"),
				controller.getSysVariable("greenWestPedestrians[1]"), false, false);
		_pedsTrafficLights[7].updateState(false, controller.getSysVariable("greenWestPedestrians[0]"), false,
				controller.getSysVariable("greenNorthPedestrians[0]"));
	}

	private void updateCars() {
		for (int i = 0; i < this._cars.size(); i++) {
			Car car = this._cars.get(i);
			if (car.isVisible()) { // car is inside the screen
				if (!car.needsToStop(controller.getSysVariable(car.getRelevantSpectraVariable(false)))) { // don't need
																											// to stop
					if (car.getCarState() == CarState.WAITING) {
						car.setCarState(CarState.CROSSING); // update car state
					} else if (car.getCarState() == CarState.CROSSING) {
						if (car.hasCrossed()) { // if car is out of the center of a junction, update the controller env
												// to false and mark lane as closed
							checkOrUpdateLane(car, false, false);
							controller.updateEnvVariable(car.getRelevantSpectraVariable(true), "NONE");
						}
					}
					car.move(); // increase car position on screen
				} else { // stop for red light
					controller.updateEnvVariable(car.getRelevantSpectraVariable(true), car.isEmergency().name());
				}
			} else { // car out of screen, remove from list
				this._cars.remove(i);
			}

		}
	}

	private void updatePedestrians() {
		for (int i = 0; i < this._pedestrians.size(); i++) {
			Pedestrian ped = this._pedestrians.get(i);
			if (ped.isVisible()) { // if pedestrian on screen
				if (!ped.needsToStop(controller.getSysVariable(ped.getRelevantSpectraVariable(false)))) {
					if (ped.getPedestrianState() == PedestrianState.WAITING_FIRST) { // crossed first section of road
						controller.updateEnvVariable(ped.getRelevantSpectraVariable(true), "false"); // update spectra
																										// env to false
						ped.setPedestrianState(PedestrianState.CROSSING_FIRST);
					} else if (ped.getPedestrianState() == PedestrianState.WAITING_SECOND) { // crossing second section
																								// of road
						ped.setPedestrianState(PedestrianState.CROSSING_SECOND);
					} else if (ped.getPedestrianState() == PedestrianState.CROSSING_SECOND) {
						if (ped.hasCrossed()) { // if car is out of the center of a junction, update the controller env
												// to false and mark lane as closed
							controller.updateEnvVariable(ped.getRelevantSpectraVariable(true), "false");
						}
					}
					ped.move(); // increase position on screen
				} else { // stopped on red light, update spectra variable
					controller.updateEnvVariable(ped.getRelevantSpectraVariable(true), "true");
				}
			} else { // pedestrian is out of the screen, remove from list and update spectra
				this._pedestrians.remove(i);
			}
		}
	}

	public void createScenario(int scenario) {
		if (scenario == 0) {
			return;
		}
	}

	public void updateFog() {
		if (this._fog.isVisible()) { // controls the fog movement
			this._fog._x--;
			if (this._fog.getX() == -1600)
				this._fog._x = 0;
		}
	}

	public void free() {
		this.controller.free();
	}
}
