package beautifier;

import java.util.Arrays;
import java.util.Stack;

import org.jfugue.MusicStringParser;

public class Beautifier {
	private MusicMood mMood;
	
	public Beautifier() {
		mMood = new BoringCMood();
	}
	
	public String beautify(String inputCode) {
		StringBuilder builder = new StringBuilder();
		
		char[] commands = reduce(inputCode).toCharArray();
		
		int pos = 0;
		char command;
		Stack<Integer> loopEntryPositions = new Stack<Integer>();
		
		String currentNote = mMood.getFirstNote();
		
		builder.append(currentNote);
		
		while (pos < commands.length) {
			command = commands[pos];
			switch (command) {
			case 'r': 	builder.append('r');
						break;
			case '(':	builder.append('(');
						loopEntryPositions.add(pos);
						break;
			case ')':	//HARD BIT
						break;
				
			}
		}
		
		return builder.toString();
	}

	private String reduce(String inputCode) {
		String output = "";
		String[] commands = inputCode.split(" ");
		String prevNote = commands[0];
		
		for (String command : Arrays.copyOfRange(commands, 1, commands.length)) {
			if ((command.equals("r")) || (command.equals("("))
					|| command.equals(")")) {
				output += command;
			}
			else if (getNoteValue(command) > getNoteValue(prevNote)) {
				output += "+";
				prevNote = command;
			}
			else if (getNoteValue(command) < getNoteValue(prevNote)){
				output += "-";
				prevNote = command;
			}
			else {
				output += "0";
				prevNote = command;
			}
			
		}
		
		return output;
	}
	
	private int getNoteValue(String command) {
		return MusicStringParser.getNote(command).getValue();
	}
}
