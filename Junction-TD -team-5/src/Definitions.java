import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
 * a class for most definitions of this project 
 */
final class Definitions {
	public static final String REGULAR_MSG = "Manual mode, waiting for input..."; 
	public static final String CLEAR_MSG = "Waiting for junction to clear..."; 
	public static HashMap<Integer, ArrayList<Integer>> emergencyCollidingList = new HashMap<Integer, ArrayList<Integer>>();
	public static HashMap<Integer, ArrayList<Integer>> possibleCars = new HashMap<Integer, ArrayList<Integer>>();
	public static HashMap<Integer, ArrayList<Integer>> possiblePeds = new HashMap<Integer, ArrayList<Integer>>();
	static {
		// map colliding emergency lanes
		emergencyCollidingList.put(1, new ArrayList<Integer>(Arrays.asList(11, 31, 32, 22)));
		emergencyCollidingList.put(2, new ArrayList<Integer>(Arrays.asList(11, 12, 32, 21)));
		emergencyCollidingList.put(11, new ArrayList<Integer>(Arrays.asList(21, 32, 1, 2)));
		emergencyCollidingList.put(12, new ArrayList<Integer>(Arrays.asList(21, 22, 31, 2)));
		emergencyCollidingList.put(31, new ArrayList<Integer>(Arrays.asList(21, 22, 12, 1)));
		emergencyCollidingList.put(32, new ArrayList<Integer>(Arrays.asList(22, 11, 1, 2)));
		emergencyCollidingList.put(21, new ArrayList<Integer>(Arrays.asList(31, 12, 11, 2)));
		emergencyCollidingList.put(22, new ArrayList<Integer>(Arrays.asList(31, 32, 12, 1)));

		// map possible cars
		possibleCars.put(0, new ArrayList<Integer>(Arrays.asList(200, 0, 0, 0)));
		possibleCars.put(1, new ArrayList<Integer>(Arrays.asList(260, 0, 0, 1)));
		possibleCars.put(2, new ArrayList<Integer>(Arrays.asList(320, 0, 0, 2)));
			
		possibleCars.put(20, new ArrayList<Integer>(Arrays.asList(547, 800, 180, 0)));
		possibleCars.put(21, new ArrayList<Integer>(Arrays.asList(490, 800, 180, 1)));
		possibleCars.put(22, new ArrayList<Integer>(Arrays.asList(430, 800, 180, 2)));
		
		//map possible pedestrians
		possiblePeds.put(0, new ArrayList<Integer>(Arrays.asList( 152, 0, 0 )));
		possiblePeds.put(1, new ArrayList<Integer>(Arrays.asList( 608, 0, 0 )));
		possiblePeds.put(2, new ArrayList<Integer>(Arrays.asList( 800, 155, 90 )));
		possiblePeds.put(3, new ArrayList<Integer>(Arrays.asList( 800, 605, 90 )));
		possiblePeds.put(4, new ArrayList<Integer>(Arrays.asList( 0, 155, 270 )));
		possiblePeds.put(5, new ArrayList<Integer>(Arrays.asList( 0, 605, 270 )));
		possiblePeds.put(6, new ArrayList<Integer>(Arrays.asList(152, 800, 180)));
		possiblePeds.put(7, new ArrayList<Integer>(Arrays.asList(608, 800, 180)));
	}
}

enum CarDirection {
	STRAIGHT(1), LEFT(2), RIGHT(0);
	private final int direction;

	CarDirection(int direction) {
		this.direction = direction;
	}

	public int getValue() {
		return direction;
	}

	public static CarDirection getDirection(int val) {
		switch (val) {
		case 0:
			return RIGHT;
		case 2:
			return LEFT;
		default:
			return STRAIGHT;
		}
	}
}

enum CarState {
	MOVING, WAITING, CROSSING, FINISHED;
}

enum PedestrianState {
	MOVING, WAITING_FIRST, CROSSING_FIRST, WAITING_SECOND, CROSSING_SECOND, FINISHED;
}

enum VehicleOptions {
	NONE, CAR, EMERGENCY
}

enum DemoMode {
	NONE, WAITING, RUNNING
}
