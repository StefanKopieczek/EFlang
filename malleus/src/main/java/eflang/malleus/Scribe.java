package eflang.malleus;

import abc.notation.Tune;
import abc.parser.TuneParser;
import abc.ui.swing.JScoreComponent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Scribe {
	
	private JScoreComponent mScore;
	
	public Scribe(String abcString) {
		System.out.println(abcString);
		Tune tune = new TuneParser().parse(abcString);
		mScore = new JScoreComponent();
		mScore.setJustification(true);
		mScore.setTune(tune);		
	}
	
	public JFrame getScoreWindow() {		
		JFrame frame = new JFrame();
		JScrollPane scrollPane = new JScrollPane(mScore);
		frame.add(scrollPane);
		frame.setTitle("Earfuck Score - Preview");
		frame.pack();
		
		return frame;
	}
	
	public void writeToFile(File f) throws IOException {
		mScore.writeScoreTo(f);
	}
}
