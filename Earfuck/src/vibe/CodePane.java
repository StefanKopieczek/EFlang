package vibe;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

public class CodePane extends JTextPane {
	public void setCurrentCommandIndex(int i) {
		Style style = this.addStyle("Redtext", null);
		StyleConstants.setForeground(style, Color.red);
		Style noStyle = this.addStyle("NoStyle", null);
		String code = getText();
		
		int j=0;
		while (i>0) {
			if (code.charAt(j)==' ') {
				i--;
			}
			j++;
		}
		
		getStyledDocument().setCharacterAttributes(0, getText().length(), noStyle, true);
		getStyledDocument().setCharacterAttributes(j,2, style, true);
	}
}
