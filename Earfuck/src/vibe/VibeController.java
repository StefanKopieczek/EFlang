package vibe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import earcompiler.EARCompiler;
import earcompiler.EARException;

public class VibeController implements ActionListener {
	MainFrame mFrame;
	EARCompiler mEARCompiler;
	
	public VibeController(MainFrame frame) {
		mFrame = frame;
		mEARCompiler = new EARCompiler();
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
		mFrame.dispose();
		System.exit(0);
	}
	
	private void openFile() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new EFCodeFilter());
		int returnVal = fc.showOpenDialog(mFrame);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			
		}
	}
	
}
