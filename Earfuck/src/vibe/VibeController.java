package vibe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;

import earcompiler.EARCompiler;
import earcompiler.EARException;
import earfuck.Parser;

public class VibeController implements ActionListener {
	private MainFrame mFrame;
	private EARCompiler mEARCompiler;
	private Parser mParser;
	private String mOpenFilePath;
	private ParserWorker mWorker;
	private VibeMode mMode;
	
	enum VibeMode {
		HIGHLEVEL, EAR, EF;
	}
	
	public VibeController(MainFrame frame) {
		mFrame = frame;
		mEARCompiler = new EARCompiler();
		mOpenFilePath = null;
		mParser = new Parser();
		mWorker = null;
	}

	@Override
	public void actionPerformed(ActionEvent action) {
		if (action.getActionCommand().equals("compile")) {
			compileEARCode();
			return;
		}
		if (action.getActionCommand().equals("exit")) {
			exitProgram();
			return;
		}
		if (action.getActionCommand().equals("open")) {
			openFile();
			return;
		}
		if (action.getActionCommand().equals("save")) {
			save();
			return;
		}
		if (action.getActionCommand().equals("saveas")) {
			saveAs();
			return;
		}
		if (action.getActionCommand().equals("new")) {
			newFile();
			return;
		}
		if (action.getActionCommand().equals("play")) {
			play();
			return;
		}
		if (action.getActionCommand().equals("pause")) {
			pause();
			return;
		}
		if (action.getActionCommand().equals("step")) {
			step();
			return;
		}
		if (action.getActionCommand().equals("stop")) {
			stop();
			return;
		}
	}
	
	private void compileEARCode() {
		String EARCode = mFrame.getEARCode();
		String EFCode = null;
		try {
			EFCode = mEARCompiler.compile(EARCode);
			mFrame.setEFCode(EFCode);
		} catch (EARException e) {
			mFrame.setEFCode(e.getMessage());
		}
	}
	
	private void exitProgram() {
		pause();
		mFrame.dispose();
		System.exit(0);
	}
	
	private void openFile() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new EFCodeFilter());
		int returnVal = fc.showOpenDialog(mFrame);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			mOpenFilePath = file.getPath();
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
			
			
				StringBuilder builder = new StringBuilder();
				
				String currentLine = null;
				while ((currentLine = br.readLine()) != null) {
					builder.append(currentLine+'\n');
				}
				
				br.close();
				mFrame.setEARCode(builder.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void saveFile(File file, String text) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.append(text);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void save() {
		if (mOpenFilePath==null) {
			saveAs();
			return;
		}
		File file = new File(mOpenFilePath);
		saveFile(file,mFrame.getEARCode());
	}
	
	private void saveAs() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new EFCodeFilter());
		int returnVal = fc.showSaveDialog(mFrame);
		
		if (returnVal==JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			saveFile(file, mFrame.getEARCode());
			
			mOpenFilePath = file.getPath();
		}
	}
	
	private void newFile() {
		mOpenFilePath = null;
		mFrame.setEARCode("");
		mFrame.setEFCode("");
		mFrame.setHighLevelCode("");
	}
	
	private void play() {
		mFrame.setButtonEnabled(1,false); //Play
		mFrame.setButtonEnabled(2,true); //Pause
		mFrame.setButtonEnabled(3,true); //Stop
		mFrame.setButtonEnabled(4,false); //Step
		String EFCode = mFrame.getEFCode();
		mParser.giveMusic(EFCode);
		mWorker = new ParserWorker(mParser,mFrame);
		mWorker.execute();
	}
	
	private void pause() {
		mFrame.setButtonEnabled(1,true); //Play
		mFrame.setButtonEnabled(2,false); //Pause
		mFrame.setButtonEnabled(3,true); //Stop
		mFrame.setButtonEnabled(4,true); //Step
		mWorker.cancel(true);
	}
	
	private void stop() {
		mFrame.setButtonEnabled(1,true); //Play
		mFrame.setButtonEnabled(2,false); //Pause
		mFrame.setButtonEnabled(3,false); //Stop
		mFrame.setButtonEnabled(4,true); //Step
		mWorker.cancel(true);
		mParser.refreshState();
	}
	
	private void step() {
		mFrame.setButtonEnabled(4,false); //Step
		if (mParser.getPiece().length==0) {
			mParser.giveMusic(mFrame.getEFCode());
		}
		(new StepForwardWorker(mParser)).execute();
	}
	
	public void setMode(VibeMode mode) {
		mMode = mode;
		mFrame.setupEditPane(mode);
	}
	
	public VibeMode getMode() {
		return mMode;
	}
}
