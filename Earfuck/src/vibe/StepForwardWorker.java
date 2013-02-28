package vibe;

import javax.swing.SwingWorker;

import earfuck.Parser;

public class StepForwardWorker extends SwingWorker<Void, Void> {
	Parser mParser;
	
	public StepForwardWorker(Parser parser) {
		mParser = parser;
	}
	
	@Override
	public Void doInBackground() {
		mParser.stepForward();
		return null;
	}
}
