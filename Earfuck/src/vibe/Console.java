package vibe;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JTextPane;

import ef.IoManager;
import ef.Parser;

/**
 * This is a text pane console designed to handle IO
 * to and from an EF Parser.
 * @author Ryan Norris
 *
 */
public class Console extends JTextPane implements IoManager {
	/**
	 * A list of the lines of text in the console.
	 * Does not include the text being currently entered.
	 */
	private ArrayList<String> mText;
	
	/**
	 * The text currently being entered by the user.
	 */
	private String inputText;
	
	/**
	 * Whether we are currently accepting input or not.
	 * Controls if the user can type in the console.
	 */
	private boolean inputMode = false;
	
	/**
	 * The parser to give input to.
	 */
	private Parser mParser;
	
	/**
	 * The string to display at the beginning of lines in the console.
	 */
	private final String lineStartIndicator = "> ";
	
	/**
	 * The string to display on the input line.
	 */
	private final String inputIndicator = ":>";
	
	public Console() {
		super();
		mText = new ArrayList<String>();
		inputText = "";
		mParser = null;
		updateText();
		setEditable(false);
	}
	
	/**
	 * Overridden key event processing so that the contents of the console
	 * in general cannot be modified by the user.
	 * Only the input text can be changed, and only then if we're in input mode.
	 * Also handles Enter sending the input to the Parser.
	 */
	@Override
	protected void processKeyEvent(KeyEvent event) {
		if (inputMode) {
			if (event.getID()==KeyEvent.KEY_PRESSED) {
				//Handle deleting chars with backspace
				if (event.getKeyCode()==KeyEvent.VK_BACK_SPACE) {
					if (inputText.length()>0) {
						inputText = inputText.substring(0,inputText.length()-1);
					}
				}
				if (event.getKeyCode()==KeyEvent.VK_ENTER) {
					if (mParser!=null) {
						try {
							mParser.giveInput(Integer.parseInt(inputText));
							mParser = null;
							mText.add(inputText);
							inputMode = false;
							setEditable(false);
							
							//This is a hack to lose focus
							setFocusable(false);
							setFocusable(true);
						}
						catch (NumberFormatException e) {
							addLine("Invalid: "+inputText);
						}
					}
					inputText = "";
				}
			}
			if (event.getID()==KeyEvent.KEY_TYPED) {
				
				if (event.getKeyChar() != KeyEvent.CHAR_UNDEFINED &&
						event.getKeyChar() != KeyEvent.VK_BACK_SPACE &&
						event.getKeyChar() != KeyEvent.VK_ENTER) {
					inputText += event.getKeyChar();
				}
			}
		}

		updateText();
	}
	
	/**
	 * Stitches together the lines of text, input text and the line start markers
	 * to create the contents of the box.
	 * Must be called whenever you want to see a change in the box.
	 */
	private void updateText() {
		StringBuilder builder = new StringBuilder();
		for (String line : mText) {
			builder.append(lineStartIndicator+line+"\n");
		}
		
		builder.append(inputIndicator+inputText+"\n");

		String text = builder.toString();
		setText(text.replaceFirst("\\n$", ""));
		this.setCaretPosition(getText().length());
	}
	
	/**
	 * Adds a line of text to the console.
	 * @param line text to add.
	 */
	public void addLine(String line) {
		mText.add(line);
		updateText();
	}
	
	/**
	 * Sets the console to take input from the user.
	 */
	public void takeInput() {
		inputMode = true;
		updateText();
		requestFocusInWindow(false);
		setEditable(true);
	}

	/**
	 * Used by a parser to request input from the console.
	 */
	@Override
	public void requestInput(Parser parser) {
		mParser = parser;
		takeInput();
	}

	/**
	 * Used by a parser to display output in the console.
	 */
	@Override
	public void output(int value) {
		addLine(String.valueOf(value));
	}

	public void clearDisplay() {
		mText = new ArrayList<String>();
		updateText();
	}
}
