import java.util.Timer;
import java.util.TimerTask;

public class JunctionTimer {
	protected Timer timer = new Timer();
	protected boolean inProgress;

	protected void startTimer(long time) {
		inProgress = true;
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				inProgress = false;
			}
		};
		timer.schedule(task, time);
	}
	
	public boolean isOn() {
		return inProgress;
	}
}
