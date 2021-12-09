import java.util.HashMap;
import java.util.Map;

import tau.smlab.syntech.controller.executor.ControllerExecutor;
import tau.smlab.syntech.controller.jit.BasicJitController;


//wrapper class for spectra's controller
class JunctionController {
	JunctionState junctionState = new JunctionState();
	ControllerExecutor ctrlExec;

	public JunctionController() {
		Map<String,String> inputs = new HashMap<>();
		try {
			for (String env : junctionState.getEnvState().keySet()) {
				inputs.put(env, junctionState.getEnvVariable(env));
			}
			ctrlExec = new ControllerExecutor(new BasicJitController(), "out");
			ctrlExec.initState(inputs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateState() {
		Map<String,String> inputs = new HashMap<>();
		try {
			// update environment
			for (String env : junctionState.getEnvState().keySet()) {
				inputs.put(env, junctionState.getEnvVariable(env));
			}
			ctrlExec.updateState(inputs); // get new state from spectra
			// update system
			for (String sys : ctrlExec.getCurrOutputs().keySet())
				junctionState.updateSysVariable(sys, ctrlExec.getCurrValue(sys));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	public void free() {
		ctrlExec.free();
	}

	public void updateEnvVariable(String s, String b) {
		this.junctionState.updateEnvVariable(s, b);
	}

	public boolean getSysVariable(String var) {
		return Boolean.parseBoolean(this.junctionState.getSysVariable(var));
	}

	public String getEnvVariable(String relevantLane) {
		return this.junctionState.getEnvVariable(relevantLane);
	}

	public HashMap<String, String> getSysMap() {
		return this.junctionState.getSysState();
	}
}
