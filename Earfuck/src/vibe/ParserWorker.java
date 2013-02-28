package vibe;

import java.util.List;

import javax.swing.SwingWorker;

import earfuck.Parser;

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
			mController.getFrame().getEFTextPane().setCurrentCommandIndex(indices.get(indices.size()-1));
			mController.getFrame().getEFTextPane().invalidate();
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
