package vibe;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

public class ParserWorker extends SwingWorker<Void,Integer> {
VibeController mController;
	
	public ParserWorker(VibeController controller) {
		mController = controller;
	}
	
	@Override
	public Void doInBackground() {
		while (!this.isCancelled()) {
			publish(mController.getParser().getPlace());
			
			//Change tempo if the slider has been moved
			int newTempo = mController.getFrame().getTempo();
			if (newTempo != mController.getParser().getTempo()) {
				mController.getParser().setTempo(newTempo);
			}
			mController.getParser().stepForward();
		}
		return null;
	}
	
	@Override
	protected void process(List<Integer> indices) {
		try {
			//Get EF command index
			int currentEFCommand = indices.get(indices.size()-1);
			//Get EAR command index
			int currentEARCommand = 0;
			ArrayList<Integer> EARCommandStartPositions = 
					mController.getEARCommandStartPositions();
			for (int i=0; i<EARCommandStartPositions.size(); i++) {
				if (EARCommandStartPositions.get(i) <= currentEFCommand) {
					currentEARCommand = i;
				}
			}
			
			mController.getFrame().getEFTextPane().
					setCurrentCommandIndex(currentEFCommand);
			mController.getFrame().getEFTextPane().invalidate();
			
			mController.getFrame().getEARTextPane().
					setCurrentCommandIndex(currentEARCommand);
			mController.getFrame().getEARTextPane().invalidate();
		}
		catch (IndexOutOfBoundsException ex) {
			this.cancel(true);
		}
	}
	
	@Override
	protected void done() {
		mController.setPlayState(VibeController.PlayState.STOPPED);
	}
}
