package malleus;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import abc.notation.Tune;
import abc.parser.TuneParser;
import abc.ui.swing.JScoreComponent;

public class Scribe {
	
	private JScoreComponent mScore;
	
	public Scribe(String abcString) {
		Tune tune = new TuneParser().parse(abcString);
		mScore = new JScoreComponent();
		mScore.setJustification(true);
		mScore.setTune(tune);		
	}
	
	public JFrame getScoreWindow() {		
		JFrame frame = new JFrame();
		frame.add(mScore);
		frame.pack();
		
		return frame;
	}
	
	public void writeToFile(File f) throws IOException {
		mScore.writeScoreTo(f);
	}
	
	public static void main(String[] args)
	{
		Scribe scribe = new Scribe("X:1\nT:Hello world\nM:4/4\nL:1/4\nK:C\nc d e");
		scribe.getScoreWindow().setVisible(true);
	}
}
