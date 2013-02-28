package vibe;

import java.util.List;

import javax.swing.SwingWorker;

import earfuck.Parser;

public class ParserWorker extends SwingWorker<Void,Integer> {
	Parser mParser;
	MainFrame mFrame;

	public ParserWorker(Parser parser, MainFrame frame) {
		mParser = parser;
		mFrame = frame;
	}
	
	@Override
	public Void doInBackground() {
		while (!this.isCancelled()) {
			publish(mParser.getPlace());
			mParser.stepForward();
		}
		return null;
	}
	
	@Override
	protected void process(List<Integer> indices) {
		try {
			mFrame.getEFTextPane().setCurrentCommandIndex(indices.get(indices.size()-1));
			mFrame.getEFTextPane().invalidate();
		}
		catch (IndexOutOfBoundsException ex) {
			this.cancel(true);
		}
	}
}
