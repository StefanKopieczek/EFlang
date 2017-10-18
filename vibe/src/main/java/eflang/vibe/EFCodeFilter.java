package eflang.vibe;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Lets through only files of type .lobe, .ear and .ef <br/>
 * Allows user to change directory.
 * @author Ryan Norris
 *
 */
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

        return ((extension.equals("lobe")) ||
                (extension.equals("ear")) ||
                (extension.equals("ef")));
    }

    @Override
    public String getDescription() {
        return "LOBE Code (.lobe), EAR Code (.ear), EF Code (.ef)";
    }

}
