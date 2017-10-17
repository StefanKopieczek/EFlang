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
		
		getStyledDocument().setCharacterAttributes(0, getText().length(), noStyle, true);
		if (i==-1) {
			return;
		}
		
		int j=0;
		while (i>0) {
			if (code.charAt(j)==mDivider) {
				while (code.charAt(j)==mDivider) {
					j++;
				}
				i--;
			}
			else {
				j++;
			}
		}
		int commandLength = code.substring(j).indexOf(mDivider);
		if (commandLength==-1) {
			commandLength = code.substring(j).length();
		}

		getStyledDocument().setCharacterAttributes(j,commandLength, style, true);
	}
}
