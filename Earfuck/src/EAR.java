import java.util.Arrays;
import java.util.HashMap;


public class EAR {
	private static Note STARTING_NOTE = Note.c4;
	private enum Note {
		c2, d2, e2, f2, g2, a2, b2,
		c3, d3, e3, f3, g3, a3, b3,
		c4, d4, e4, f4, g4, a4, b4,
		c5, d5, e5, f5, g5, a5, b5
	}
	private HashMap<String,EARInstruction> instructionSet;
	
	private int p; //Cell pointer
	private HashMap<Integer,Integer> memory;
	private Note currentNote;
	
	public EAR() {
		p=0;
		memory = new HashMap<Integer,Integer>();
		currentNote = STARTING_NOTE;
	}
	
	public String compile(String earCode) {
		String output = "";
		String[] instructions = earCode.split(";");
		
		output += currentNote.toString();
		
		for (String instruction : instructions) {
			String[] parsedInstruction = instruction.split(" ");
			String[] args = Arrays.copyOfRange(parsedInstruction, 1, 
											parsedInstruction.length);
			String opcode = parsedInstruction[0];
			
			output += instructionSet.get(opcode).compile(args);
		}
		return output;
	}
	
	public class EARInstruction{
		public String compile(String[] args){
			return "";
		}	
	}
	
	//Defines all the instructions
	public EARInstruction GOTO = new EARInstruction() {
		public String compile(String[] args) {
			int destination = Integer.parseInt(args[0]);
			return null;
		}
	};
	
	public EARInstruction ADD = new EARInstruction() {
		public String compile(String[] args) {
			return "ADD";
		}
	};
}
