package vibe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class ProgramTimer extends Timer {
	int mCycles;

	public ProgramTimer(int delay, ActionListener listener) {
		super(delay, listener);
		mCycles = 0;
	}
	
	@Override
	protected void fireActionPerformed(ActionEvent e) {
		mCycles++;
		super.fireActionPerformed(e);
	}

	public int getCycles() {
		return mCycles;
	}
	
	public int getTime() {
		return getDelay() * getCycles();
	}
	
	@Override
	public void restart() {
		mCycles = 0;
		super.restart();
	}
	
	public void reset() {
		restart();
		stop();
		mCycles = 0;
	}
}
