import java.util.HashMap;

// class that holds current junction's state
class JunctionState {
	HashMap<String, String> stateSysMap = new HashMap<>();
	HashMap<String, String> stateEnvMap = new HashMap<>();

	public JunctionState() {
		// car sensors
		stateEnvMap.put("vehiclesNorth[0]", "NONE");
		stateEnvMap.put("vehiclesNorth[1]", "NONE");
		stateEnvMap.put("vehiclesNorth[2]", "NONE");
		stateEnvMap.put("vehiclesSouth[0]", "NONE");
		stateEnvMap.put("vehiclesSouth[1]", "NONE");
		stateEnvMap.put("vehiclesSouth[2]", "NONE");

		// pedestrians buttons
		stateEnvMap.put("pedestriansNorthPressed[0]", "false");
		stateEnvMap.put("pedestriansNorthPressed[1]", "false");
		stateEnvMap.put("pedestriansSouthPressed[0]", "false");
		stateEnvMap.put("pedestriansSouthPressed[1]", "false");
		stateEnvMap.put("pedestriansEastPressed[0]", "false");
		stateEnvMap.put("pedestriansEastPressed[1]", "false");
		stateEnvMap.put("pedestriansWestPressed[0]", "false");
		stateEnvMap.put("pedestriansWestPressed[1]", "false");
		// cars lights
		stateSysMap.put("greenNorthVehicles[0]", "false");
		stateSysMap.put("greenNorthVehicles[1]", "false");
		stateSysMap.put("greenNorthVehicles[2]", "false");
		stateSysMap.put("greenSouthVehicles[0]", "false");
		stateSysMap.put("greenSouthVehicles[1]", "false");
		stateSysMap.put("greenSouthVehicles[2]", "false");

		// pedestrian lights
		stateSysMap.put("greenNorthPedestrians[0]", "false");
		stateSysMap.put("greenNorthPedestrians[1]", "false");
		stateSysMap.put("greenSouthPedestrians[0]", "false");
		stateSysMap.put("greenSouthPedestrians[1]", "false");
		stateSysMap.put("greenEastPedestrians[0]", "false");
		stateSysMap.put("greenEastPedestrians[1]", "false");
		stateSysMap.put("greenWestPedestrians[0]", "false");
		stateSysMap.put("greenWestPedestrians[1]", "false");
		// fog & closed roads
		// env
		stateEnvMap.put("foggy", "false");
		stateEnvMap.put("roadConstructions", "false");
		stateEnvMap.put("freezeMode", "false");
		
		// manual mode
		stateEnvMap.put("manualMode", "false");
		stateEnvMap.put("manualTrafficLight", "VEHICLE_N_STRAIGHT"); // choose arbitrary value
		stateEnvMap.put("trafficLightColor", "RED"); // choose arbitrary color
		
	}

	public HashMap<String, String> getEnvState() {
		return stateEnvMap;
	}

	public HashMap<String, String> getSysState() {
		return stateSysMap;
	}

	public void updateEnvVariable(String s, String b) {
		if (this.stateEnvMap.containsKey(s))
			this.stateEnvMap.put(s, b);
	}

	public void updateSysVariable(String s, String b) {
		if (this.stateSysMap.containsKey(s))
			this.stateSysMap.put(s, b);
	}

	public String getEnvVariable(String var) {
		if (this.stateEnvMap.containsKey(var))
			return this.stateEnvMap.get(var);
		return "false";
	}

	public String getSysVariable(String var) {
		if (this.stateSysMap.containsKey(var))
			return this.stateSysMap.get(var);
		return "false";
	}
}
