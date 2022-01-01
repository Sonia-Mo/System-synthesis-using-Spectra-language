import java.util.HashMap;
import java.util.Map;
import tau.smlab.syntech.ctd.CTDExecutor;
import tau.smlab.syntech.ctd.CTDExecutorContinuing;

public class JunctionTDController {
	JunctionState junctionState = new JunctionState();
	CTDExecutor ctrlExec;
	boolean _is_start = true;

	public JunctionTDController() {
		System.out.println("Create CTD controller");

		Map<String, String> inputs = new HashMap<>();
		try {
			for (String env : junctionState.getEnvState().keySet()) {
				inputs.put(env, junctionState.getEnvVariable(env));
			}

			ctrlExec = new CTDExecutorContinuing("out/", /* Folder containing symbolic controller */
					"out/ctd.out/2021-12-27_13-21-14.473/Junction-team-x.spectra_tree.bin", /*
																							 * .spectra_tree.bin test
																							 * suite file
																							 */
					"out/ctd.out/2021-12-27_13-21-14.473/", /* Folder containing the test suite BDDs */
					false); /* Should we turn on auto reordering? */
			
			
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Test Log ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

			// Prints the total number of tests and states.
			System.out.println("Total tests: " + ctrlExec.getTotalTests());
			System.out.println("Total states:" + ctrlExec.getTotalStates());

			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateState() {
		try {
			if (!this._is_start) {
				ctrlExec.updateState(); // get new state from spectra
			} else {
				this._is_start = false;
			}

			// Prints the current test number and the state number within that test.
			System.out.println("          |");
			System.out.println("          v");
			System.out.println("Test-State: " + ctrlExec.getTestNumber() + "-" + ctrlExec.getStateNumber());

			// Additional optional info:
			// True if this state was visited already using the executor.
			// System.out.println("Visited already?: " + ctrlExec.wasVisited());
			// True if we are at the end a of a test.
			// True if we just "jumped" to a new test.
			// System.out.println("Has just jumped?: " + ctrlExec.wasJumped());

			System.out.println("Current Inputs:");
			System.out.println(ctrlExec.getCurrInputs());
			System.out.println("Current Outputs:");
			System.out.println(ctrlExec.getCurrOutputs());

			if (ctrlExec.isEndOfTest())
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~ End of Test ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

			// update system
			for (String sys : ctrlExec.getCurrOutputs().keySet())
				junctionState.updateSysVariable(sys, ctrlExec.getCurrValue(sys));
			// update env
			for (String env : ctrlExec.getCurrInputs().keySet())
				junctionState.updateEnvVariable(env, ctrlExec.getCurrValue(env));

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public boolean hasNext() {
		return ctrlExec.hasNext();
	}

	public boolean isEndOfTest() {
		return ctrlExec.isEndOfTest();
	}

	public int getTestNumber() {
		return ctrlExec.getTestNumber();
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
