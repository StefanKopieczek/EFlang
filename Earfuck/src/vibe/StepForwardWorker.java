package vibe;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

/**
 * This worker runs the EF Parser on a background thread for one EF command
 * and then stops. <br/>
 * It also passes forward information for code highlighting to the text panes.
 * @author Ryan Norris
 *
 */
public class StepForwardWorker extends SwingWorker<Void, Integer> {
	VibeController mController;
	
	public StepForwardWorker(VibeController controller) {
		mController = controller;
	}
	
	@Override
	public Void doInBackground() {
		publish(mController.getParser().getPlace());
		
		//Change tempo if the slider has been moved
		int newTempo = mController.getFrame().getTempo();
		if (newTempo != mController.getParser().getTempo()) {
			mController.getParser().setTempo(newTempo);
		}
		
		//If we've reached the end of the piece, stop playback.
		if (mController.getParser().getPlace() >= 
				mController.getParser().getPiece().length) {
			mController.stop();
		}
		else {
			mController.getParser().stepForward();
		}
		return null;
	}
	
	@Override
	protected void process(List<Integer> indices) {
		//Get EF command index
		int currentEFCommand = indices.get(indices.size()-1);
		//Get EAR command index
		int currentEARLine = 0;
		ArrayList<Integer> earLineStartPositions = 
				mController.getEARCommandStartPositions();
		for (int i=0; i<earLineStartPositions.size(); i++) {
			if (earLineStartPositions.get(i) <= currentEFCommand) {
				currentEARLine = i;
			}
		}
		
		mController.getFrame().getEFTextPane().
				setCurrentCommandIndex(currentEFCommand);
		mController.getFrame().getEFTextPane().invalidate();
		
		mController.getFrame().getEARTextPane().
				setCurrentCommandIndex(currentEARLine);
		mController.getFrame().getEARTextPane().invalidate();
		
		mController.getFrame().updateMemoryVisualiser();
	}
	
	@Override
	protected void done() {
		mController.setPlayState(VibeController.PlayState.PAUSED);
	}
}
