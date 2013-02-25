import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;


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
	private Stack<Integer> branchLocStack;
	private Stack<Note> branchNoteStack;
	private Stack<Integer> branchOptimismStack;
	
	public EARCompiler() {
		p=0;
		memory = new EarfuckMemory();
		currentNote = STARTING_NOTE;
		optimism = 0;
		instructionSet = getInstructionSet();
		branchLocStack = new Stack<Integer>();
		branchNoteStack = new Stack<Note>();
		branchOptimismStack = new Stack<Integer>();
	}
	
	private HashMap<String,EARInstruction> getInstructionSet() {
		HashMap<String,EARInstruction> instructions = 
				new HashMap<String,EARInstruction>();
		instructions.put("GOTO", GOTO);
		instructions.put("IN", IN);
		instructions.put("OUT", OUT);
		instructions.put("ADD", ADD);
		instructions.put("SUB", SUB);
		instructions.put("MUL", MUL);
		instructions.put("WHILE", WHILE);
		instructions.put("ENDWHILE", ENDWHILE);
		instructions.put("ZERO", ZERO);
		
		return instructions;
	}
	
	/**
	 * Compiles provided EARCode into an EF program
	 * @param EARCode
	 * @return String containing EF program
	 */
	public String compile(String EARCode) {
		String output = "";
		
		EARCode = EARCode.replaceAll(";\\s*",";");
		String[] instructions = EARCode.split(";");
		
		
		output += currentNote.toString()+" ";
		
		for (String instruction : instructions) {
			instruction = instruction.replaceAll("(\n)? +(\n)?", " ");
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
			output += currentNote.getPrev().getPrev().getPrev().toString()+" ";
			currentNote = currentNote.getPrev().getPrev().getPrev();
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
			output += currentNote.getNext().getNext().getNext().toString()+" ";
			currentNote = currentNote.getNext().getNext().getNext();
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
		String output = "";
		memory.put(p, memory.get(p) + 1);
		if (optimism!=1) {
			output += moveLeft() + moveRight();
		}
		optimism = 1;
		output += currentNote.toString()+" ";
		return output;
	}
	
	/**
	 * Safely adds one to current cell
	 * @return The EF code to make it happen
	 */
	private String decrement() {
		String output = "";
		memory.put(p, memory.get(p) + 1);
		if (optimism!=-1) {
			output += moveRight() + moveLeft();
		}
		optimism = -1;
		output += currentNote.toString()+" ";
		return output;
	}
	
	/**
	 * Changes current note to specified target note
	 * without changing the pointer/optimism
	 * @param target
	 * @return
	 */
	private String changeNoteTo(Note target) {
		String output = "";
		int tempOptimism = 0;
		if (currentNote==target) {
			return output;
		}
		//Move note away from ends
		if (currentNote.ordinal()==0) {
			currentNote = currentNote.getNext().getNext();
			output += currentNote.toString()+" ";
			currentNote = currentNote.getPrev();
			output += currentNote.toString()+" ";
		}
		if (currentNote.ordinal()==Note.values().length-1) {
			currentNote = currentNote.getPrev().getPrev();
			output += currentNote.toString()+" ";
			currentNote = currentNote.getNext();
			output += currentNote.toString()+" ";
		}
		
		if (currentNote.ordinal()<target.ordinal()) {
			output += currentNote.getPrev().toString()+" ";
			tempOptimism = 1;
		}
		if (currentNote.ordinal()>target.ordinal()) {
			output += currentNote.getNext().toString()+" ";
			tempOptimism = -1;
		}
		
		output += target.toString()+" ";
		currentNote = target;
		
		//Now we're at target, but may have changed the optimism
		//here we restore optimism
		//Note, optimism may be impossible if the target note is the highest/lowest
		//in this case, we should throw an exception
		if (tempOptimism < optimism) {
			if (currentNote.ordinal()==Note.values().length-1) {
				//Throw Exception!!
			}
			output += moveLeft();
			output += moveRight();
		}
		if (tempOptimism > optimism) {
			if (currentNote.ordinal()==0) {
				//Throw Exception!!
			}
			output += moveRight();
			output += moveLeft();
		}
		
		return output;
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
	 * GOTO 5;
	 */
	public EARInstruction GOTO = new EARInstruction() {
		public String compile(String[] args) {
			int destination = Integer.parseInt(args[0]);
			String output = "";

			while (p<destination) {
				output += moveRight();
			}
			while (p>destination) {
				output += moveLeft();
			}
			return output;
		}
	};
	
	/**
	 * Resets target cell to zero
	 * e.g.
	 * ZERO 5;
	 */
	public EARInstruction ZERO = new EARInstruction() {
		public String compile(String[] args) {
			String output = "";

			output += GOTO.compile(args);
			//Ensure pessimism (this way the loop can just be 1 instruction)
			if (optimism != -1) {
				output += moveRight() + moveLeft();
			}
			output += "( ";
			output += decrement();
			output += ") ";
			return output;
		}
	};
	
	/**
	 * Takes input to target cell
	 * e.g.
	 * IN 5;
	 */
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
	
	/**
	 * Outputs target cell.
	 * e.g.
	 * OUT 5;
	 */
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
	
	/**
	 * Begins a loop conditional on the target cell.
	 * Should be matched with an REPIF
	 * e.g.
	 * IF 5;
	 */
	public EARInstruction WHILE = new EARInstruction() {
		public String compile(String[] args) {
			String output = "";
			if (args.length!=0){
				//goto cell
				output += GOTO.compile(args);
			}
			
			output += "( ";
			
			//Store where we were when we came in
			branchLocStack.push(p);
			branchNoteStack.push(currentNote);
			branchOptimismStack.push(optimism);
			
			return output;
		}
	};
	
	/**
	 * Returns to start of loop if conditioned cell is non-0
	 * Conditioned cell chosen by previous maching IF.
	 * e.g.
	 * REPIF;
	 */
	public EARInstruction ENDWHILE = new EARInstruction() {
		public String compile(String[] args) {
			String output = "";
			int branchExitPoint = branchLocStack.pop();

			//return to branch exit point
			output += GOTO.compile(new String[]{String.valueOf(branchExitPoint)});
			//ensure optimism same as start of loop
			int branchEntryOptimism = branchOptimismStack.pop();
			if (branchEntryOptimism<optimism) {
				output += moveRight();
				output += moveLeft();
			}
			if (branchEntryOptimism>optimism) {
				output += moveLeft();
				output += moveRight();
			}
			//ensure on same note as start of loop
			//(to ensure same behaviour in each loop)
			Note branchEntryNote = branchNoteStack.pop();
			output += changeNoteTo(branchEntryNote);
			
			//exit branch
			output += ") ";
			
			return output;
		}
	};
	
	/**
	 * Adds the value of the first argument (use @ for a pointer)
	 * to the cells given by the remaining arguments (as many as you like)
	 * e.g.
	 * ADD @5 2 3 4;
	 * Adds the value in cell 5 to cells 2, 3 and 4.
	 */
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
				//Ensure pessimism
				if (optimism!=-1) {
					output += moveRight() + moveLeft();
				}
				//Until cell is 0
				output += WHILE.compile(new String[]{args[0].substring(1)});
				output += decrement();
				
				//for each target cell
				for (int index : targets) {
					//Goto target cell
					output += GOTO.compile(new String[]{String.valueOf(index)});
					output += increment();
				}
				//Return to summand cell
				output += GOTO.compile(new String[]{args[0].substring(1)});
				//Ensure pessimism
				if (optimism!=-1) {
					output += moveRight() + moveLeft();
				}
				//end loop
				output += ENDWHILE.compile(new String[]{});
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
	
	/**
	 * Subtracts the value of the first argument (use @ for a pointer)
	 * from the cells given by the remaining arguments (as many as you like)
	 * e.g.
	 * SUB @5 2 3 4;
	 * Subtracts the value in cell 5 from cells 2, 3 and 4.
	 */
	public EARInstruction SUB = new EARInstruction() {
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
				//Ensure pessimism
				if (optimism!=-1) {
					output += moveRight() + moveLeft();
				}
				//Until cell is 0
				output += WHILE.compile(new String[]{args[0].substring(1)});
				output += decrement();
				
				//for each target cell
				for (int index : targets) {
					//Goto target cell
					output += GOTO.compile(new String[]{String.valueOf(index)});
					output += decrement();
				}
				//Return to summand cell
				output += GOTO.compile(new String[]{args[0].substring(1)});
				//Ensure pessimism
				if (optimism!=-1) {
					output += moveRight() + moveLeft();
				}
				//end loop
				output += ENDWHILE.compile(new String[]{});
			}
			else { //If given absolute
				amount = Integer.parseInt(args[0]);
				//for each target cell
				for (int index : targets) {
					//Goto target cell
					output += GOTO.compile(new String[]{String.valueOf(index)});
					for (int i=0;i<amount;i++) {
						output += decrement();
					}
					
				}
			}
			return output;
		}
	};
	
	/**
	 * Multiplies two values into the given cell.
	 * The final argument specifies a working cell
	 * e.g.
	 * MUL @5 @3 1 0
	 * Multiplies cell 5 with cell 3, stores the answer in cell 1, 
	 * and uses cell 0 for working.
	 */
	public EARInstruction MUL = new EARInstruction() {
		public String compile(String[] args) {
			String tempA,tempB;
			String output = "";
			String targetCell = args[2];
			String workingCell = args[3];
			
			//Zero the target cell
			output += ZERO.compile(new String[]{targetCell});
			
			if (args[0].charAt(0)=='@') {
				if (args[1].charAt(0)=='@') {
					//BOTH REFERENCES - HARD CASE
					//Clear working cell
					output += ZERO.compile(new String[]{workingCell});
					//Get cell indices
					tempA = args[0].substring(1);
					tempB = args[1].substring(1);
					
					//loop on tempA
					output += WHILE.compile(new String[]{tempA});
					//subtract one from tempA
					output += decrement();
					
					//move tempB back where it was
					//Note, this doesn't really make sense in the first
					//iteration, however it won't do anything the first time round
					//(except move to the workingCell)
					//Having it at the start of the loop means it won't move
					//one of the arguments back into it's original cell after we're done
					//this is always a time-improvement.
					//This also makes behaviour consistent, in that
					//both argument cells are destroyed after the algorithm is done.
					output += ADD.compile(new String[]{"@"+workingCell,tempB});
					
					//add tempB to target & working space
					output += ADD.compile(new String[]
							{"@"+tempB,targetCell,workingCell});
					
					//end loop
					output += ENDWHILE.compile(new String[]{});
					
					//DONE!
					return output;
				}
				else {
					//tempA = cell reference
					//tempB = absolute value
					tempA = args[0];
					tempB = args[1];
				}
			}
			else {
				if (args[1].charAt(0)=='@') {
					//tempA = cell reference
					//tempB = absolute value
					tempA = args[1];
					tempB = args[0];
				}
				else {
					//If both absolute, just do it and add
					int a = Integer.parseInt(args[0]);
					int b = Integer.parseInt(args[1]);
					output += ADD.compile(new String[]
							{String.valueOf(a*b),"@"+targetCell});
					return output;
				}
			}
			
			//Now we're definitely in the 1 reference, 1 absolute case
			//get value of absolute
			int absoluteValue = Integer.parseInt(tempB);
			//Clear working cell
			output += ZERO.compile(new String[]{workingCell});
			//just add that many times
			for (int i=0; i<absoluteValue; i++) {
				output += ADD.compile(new String[]{tempA,targetCell,workingCell});
				output += ADD.compile(new String[]{"@"+workingCell,tempA.substring(1)});
			}
			return output;
		}
	};
}
