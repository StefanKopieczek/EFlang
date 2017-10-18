package eflang.vibe;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Lets through only files of type .png.
 * 
 * @author Stefan Kopieczek
 *
 */
public class PngFileFilter extends FileFilter {

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

        return extension.equals("png");
    }

    @Override
    public String getDescription() {
        return "Portable Network Graphics (.png)";
    }

}
