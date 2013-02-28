package vibe;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

public class CodePane extends JTextPane {
	private char mDivider;
	
	public CodePane() {
		super();
		mDivider = ' ';
	}
	
	public void setDivider(char divider) {
		mDivider = divider;
	}
	
	public char getDivider() {
		return mDivider;
	}
	
	public void setCurrentCommandIndex(int i) {
		Style style = this.addStyle("Redtext", null);
		StyleConstants.setForeground(style, Color.red);
		Style noStyle = this.addStyle("NoStyle", null);
		String code = getText();
		
		int j=0;
		while (i>0) {
			if (code.charAt(j)==mDivider) {
				i--;
			}
			j++;
		}
		int commandLength = code.substring(j).indexOf(mDivider);
		
		getStyledDocument().setCharacterAttributes(0, getText().length(), noStyle, true);
		getStyledDocument().setCharacterAttributes(j,commandLength, style, true);
	}
}
