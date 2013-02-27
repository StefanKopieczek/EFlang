package vibe;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class EFCodeFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String name = f.getName();
		String extension = "";
		int i = name.lastIndexOf('.');
		if ((i>0) && (i<name.length()-1)) {
			extension = name.substring(i+1).toLowerCase();
		}
		
		if ((extension.equals(".ear")) || (extension.equals(".ef"))) {
			return true;
		}
		
		return false;
	}

	@Override
	public String getDescription() {
		return "EAR Code (.ear), EF Code (.ef)";
	}

}
