import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


public class EARCompiler {
	private static Note STARTING_NOTE = Note.c4;
	private enum Note {
		c2, d2, e2, f2, g2, a2, b2,
		c3, d3, e3, f3, g3, a3, b3,
		c4, d4, e4, f4, g4, a4, b4,
		c5, d5, e5, f5, g5, a5, b5;
		
		public Note getNext() {
			if (ordinal()==Note.values().length-1) {
				return Note.values()[0];
			}
			return Note.values()[ordinal() + 1];
		}
		
		public Note getPrev() {
			if (ordinal()==0) {
				return Note.values()[Note.values().length-1];
			}
			return Note.values()[ordinal() - 1];
		}
	}
	
	private HashMap<String,EARInstruction> instructionSet;
		
	
	private int p; //Cell pointer
	private EarfuckMemory memory;
	private Note currentNote;
	private int optimism;
	
	public EARCompiler() {
		p=0;
		memory = new EarfuckMemory();
		currentNote = STARTING_NOTE;
		optimism = 0;
		instructionSet = getInstructionSet();
	}
	
	private HashMap<String,EARInstruction> getInstructionSet() {
		HashMap<String,EARInstruction> instructions = 
				new HashMap<String,EARInstruction>();
		instructions.put("GOTO", GOTO);
		instructions.put("IN", IN);
		instructions.put("OUT", OUT);
		instructions.put("ADD", ADD);
		instructions.put("IF", IF);
		instructions.put("RETIF", RETIF);
		
		return instructions;
	}
	
	public String compile(String EARCode) {
		String output = "";
		String[] instructions = EARCode.split(";");
		
		
		output += currentNote.toString()+" ";
		
		for (String instruction : instructions) {
			String[] parsedInstruction = instruction.split(" ");
			String[] args = Arrays.copyOfRange(parsedInstruction, 1, 
											parsedInstruction.length);
			String opcode = parsedInstruction[0];
			
			EARInstruction command = instructionSet.get(opcode);
			output += command.compile(args);
		}
		return output;
	}
	
	//Some convenience methods, not accessible to the EAR programmer directly
	/**
	 * Safely moves the pointer one to the left.
	 * @return The EF code to make that happen.
	 */
	private String moveLeft() {
		String output = "";
		
		//Decrement pointer
		p--; 
		//Set optimisim
		optimism = -1;
		
		//Add correct notes to output code THIS CODE IS SHIT AND GROSS
		output += currentNote.getPrev().toString()+" ";
		if (currentNote.ordinal()<currentNote.getPrev().ordinal()) {
			output += currentNote.getPrev().getPrev().toString()+" ";
			currentNote = currentNote.getPrev().getPrev();
		} else {
			currentNote = currentNote.getPrev();
		}
		
		return output;
	}
	
	/**
	 * Safely moves the pointer one to the right.
	 * @return The EF code to make that happen.
	 */
	private String moveRight() {
		String output = "";
		
		//Increment pointer
		p++; 
		//Set optimisim
		optimism = 1;
		
		//Add correct notes to output code THIS CODE IS SHIT AND GROSS
		output += currentNote.getNext().toString()+" ";
		if (currentNote.ordinal()>currentNote.getNext().ordinal()) {
			output += currentNote.getNext().getNext().toString()+" ";
			currentNote = currentNote.getNext().getNext();
		} else {
			currentNote = currentNote.getNext();
		}
		
		return output;
	}
	
	/**
	 * Safely adds one to current cell
	 * @return The EF code to make it happen
	 */
	private String increment() {
		memory.put(p, memory.get(p) + 1);
		if (optimism==1) {
			return currentNote.toString()+" ";
		}
		optimism = 1;
		return currentNote.getPrev().toString() + " " + currentNote.toString() +
				" " + currentNote.toString() + " ";
	}
	
	/**
	 * Safely adds one to current cell
	 * @return The EF code to make it happen
	 */
	private String decrement() {
		memory.put(p, memory.get(p) - 1);
		if (optimism==-1) {
			return currentNote.toString()+" ";
		}
		optimism = -1;
		return currentNote.getNext().toString() + " " + currentNote.toString() +
				" " + currentNote.toString() + " ";
	}
	
	public class EARInstruction{
		public String compile(String[] args){
			return "";
		}	
	}
	
	//Defines all the instructions
	
	/**
	 * Moves the pointer to specified cell.
	 * e.g.
	 * GOTO 5
	 */
	public EARInstruction GOTO = new EARInstruction() {
		public String compile(String[] args) {
			int destination;
			String output = "";
			
			if (args[0].charAt(0)=='~') {
				destination = Integer.parseInt(args[0].substring(1))+p;
			}
			else {
				destination = Integer.parseInt(args[0]);
			}
			
			while (p<destination) {
				output += moveRight();
			}
			while (p>destination) {
				output += moveLeft();
			}
			return output;
		}
	};
	
	public EARInstruction IN = new EARInstruction() {
		public String compile(String[] args) {
			String output = "";
			if (args.length!=0){
				//goto cell
				output += GOTO.compile(args);
			}
			
			//ensure pessimism
			if (optimism!=-1) {
				output += moveRight();
				output += moveLeft();
			}
			//take input
			output += "r ";
			
			return output;
		}
	};
	
	public EARInstruction OUT = new EARInstruction() {
		public String compile(String[] args) {
			String output = "";
			if (args.length!=0){
				//goto cell
				output += GOTO.compile(args);
			}
			
			//ensure optimism
			if (optimism!=1) {
				output += moveLeft();
				output += moveRight();
			}
			//give output
			output += "r ";
			
			return output;
		}
	};
	
	public EARInstruction IF = new EARInstruction() {
		public String compile(String[] args) {
			String output = "";
			if (args.length!=0){
				//goto cell
				output += GOTO.compile(args);
			}
			
			output += "( ";
			
			return output;
		}
	};
	
	public EARInstruction RETIF = new EARInstruction() {
		public String compile(String[] args) {
			String output = "";
			if (args.length!=0){
				//goto cell
				output += GOTO.compile(args);
			}
			
			output += ") ";
			
			return output;
		}
	};
	
	public EARInstruction ADD = new EARInstruction() {
		public String compile(String[] args) {
			String output = "";
			int amount;
			
			//Parse & sort list of target cells
			ArrayList<Integer> targets = new ArrayList<Integer>();
			for (String s : Arrays.copyOfRange(args, 1, args.length)) {
				targets.add(Integer.parseInt(s));
			}
			Collections.sort(targets);
			
			//If given pointer
			if (args[0].charAt(0)=='@') { //If pointer
				//Goto summand cell
				output += GOTO.compile(new String[]{args[0].substring(1)});
				//Until cell is 0
				output += "( ";
				output += decrement();
				
				//for each target cell
				for (int index : targets) {
					//Goto target cell
					output += GOTO.compile(new String[]{String.valueOf(index)});
					output += increment();
				}
				//Return to summand cell
				output += GOTO.compile(new String[]{args[0].substring(1)});
				//end loop
				output += ") ";
			}
			else { //If given absolute
				amount = Integer.parseInt(args[0]);
				//for each target cell
				for (int index : targets) {
					//Goto target cell
					output += GOTO.compile(new String[]{String.valueOf(index)});
					for (int i=0;i<amount;i++) {
						output += increment();
					}
					
				}
			}
			return output;
		}
	};
}
