package earcompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

import ef.EarfuckMemory;

/**
 * Compiler for the EAR language. <br/>
 * Takes in EAR code and compiles to raw EF.
 * @author Ryan Norris
 *
 */
public class EARCompiler {
	private static Note STARTING_NOTE = Note.c4;
	private enum Note {
		ab3, bb3, b3, c4, d4, eb4, f4, g4,
		ab4, bb4, b4, c5, d5, eb5, f5, g5,
		ab5, bb5, b5;

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

	/**
	 * This stores the start position in the compiled EF code for each
	 * EAR command.
	 * Index = EAR command index
	 * Value = first EF command index in the given EAR command
	 */
	private ArrayList<Integer> lineStartPositions;

	public EARCompiler() {
		resetState();
	}

	public void resetState() {
		p=0;
		memory = new EarfuckMemory();
		currentNote = STARTING_NOTE;
		optimism = 0;
		instructionSet = getInstructionSet();
		branchLocStack = new Stack<Integer>();
		branchNoteStack = new Stack<Note>();
		branchOptimismStack = new Stack<Integer>();
		lineStartPositions = new ArrayList<Integer>();
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
		instructions.put("DIV", DIV);
		instructions.put("WHILE", WHILE);
		instructions.put("ENDWHILE", ENDWHILE);
		instructions.put("ZERO", ZERO);
		instructions.put("COPY",COPY);
		instructions.put("MOV",MOV);

		return instructions;
	}

	/**
	 * Compiles provided EARCode into an EF program
	 * @param EARCode
	 * @return String containing EF program
	 */
	public String compile(String EARCode) throws EARException {
		resetState();
		String output = "";

		//Discard comments (OLD COMMENT STYLE = "\\([^\\)]*\\)")
		EARCode = EARCode.replaceAll("//.*\\n","\n");

		//Split into individual instructions
		String[] instructions = EARCode.split("(\\r?\\n)+");

		output += currentNote.toString()+" ";

		for (int i=0; i<instructions.length; i++) {
			String compiledInstruction = "";
			String instruction = instructions[i].replaceAll(" +", " ");
			instruction = instruction.replaceAll("^ +", "");
			if (!instruction.equals("")) {
				String[] parsedInstruction = instruction.split(" ");

				String[] args = Arrays.copyOfRange(parsedInstruction, 1,
												parsedInstruction.length);
				String opcode = parsedInstruction[0];

				EARInstruction command = instructionSet.get(opcode);

				//Check opcode actually generated a command
				if (command==null) {
					String message = "Invalid opcode at instruction "+i+":";
					if (i>0) {
						message += "\n"+(i-1)+": "+instructions[i-1];
					}
					message += "\n"+i+": "+instruction;
					if (i<instructions.length-1) {
						message += "\n"+(i+1)+": "+instructions[i+1];
					}
					throw new EARInvalidOpcodeException(message);
				}

				//Check validity of command and throw helpful error message
				if (!command.checkArgs(instruction)) {
					String message = "Invalid instruction signature at instruction "+i+":";
					if (i>0) {
						message += "\n"+(i-1)+": "+instructions[i-1];
					}
					message += "\n"+i+": "+instruction;
					if (i<instructions.length-1) {
						message += "\n"+(i+1)+": "+instructions[i+1];
					}
					throw new EARInvalidSignatureException(message);
				}
				compiledInstruction = command.compile(args);
			}

			//calculate how many commands into the EF code we are
			int commands = 0;
			for (char c : output.toCharArray()) {
				if (c==' ') {
					commands++;
				}
			}
			//Add it to the array of start positions
			lineStartPositions.add(commands);
			output += compiledInstruction;
		}
		return output;
	}

	public ArrayList<Integer> getCommandStartPositions() {
		return lineStartPositions;
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
		private String signature;

		public EARInstruction(String sig) {
			signature = sig;
		}

		/**
		 * Checks if the given arg string is of the correct signature.
		 * @param args
		 * @return True/False
		 */
		public boolean checkArgs(String args) {
			return args.matches(signature);
		}

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
	public EARInstruction GOTO = new EARInstruction("GOTO\\s+-?\\d+\\s*") {
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
	public EARInstruction ZERO = new EARInstruction("ZERO\\s+-?\\d+\\s*") {
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
	public EARInstruction IN = new EARInstruction("IN\\s+-?\\d+\\s*") {
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
	public EARInstruction OUT = new EARInstruction("OUT\\s+-?\\d+\\s*") {
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
	 * Should be matched with an WHILE
	 * e.g.
	 * WHILE 5;
	 */
	public EARInstruction WHILE = new EARInstruction("WHILE\\s+-?\\d+\\s*") {
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
	public EARInstruction ENDWHILE = new EARInstruction("ENDWHILE") {
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
	 * THE SUMMAND IS DESTROYED (zeroed)
	 * e.g.
	 * ADD @5 2 3 4;
	 * Adds the value in cell 5 to cells 2, 3 and 4.
	 */
	public EARInstruction ADD = new EARInstruction(
			"ADD\\s+(@|@-)?\\d+\\s+(-?\\d+\\s+)*-?\\d+\\s*") {
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
	 * THE SUBTRACTAND IS DESTROYED (zeroed)
	 * e.g.
	 * SUB @5 2 3 4;
	 * Subtracts the value in cell 5 from cells 2, 3 and 4.
	 */
	public EARInstruction SUB = new EARInstruction(
			"SUB\\s+(@|@-)?\\d+\\s+(-?\\d+\\s+)*-?\\d+\\s*") {
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
	 * THE TWO MULTIPLICANDS ARE DESTROYED (zeroed).
	 * THE WORKING CELL IS NOT ZEROED
	 * e.g.
	 * MUL @5 @3 1 0
	 * Multiplies cell 5 with cell 3, stores the answer in cell 1,
	 * and uses cell 0 for working.
	 */
	public EARInstruction MUL = new EARInstruction(
			"MUL\\s+((@|@-)?\\d+\\s*){2}-?\\d+\\s+-?\\d+\\s*") {
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
							{String.valueOf(a*b),targetCell});
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

	/**
	 * Divides the first argument by the second, and puts the result
	 * in the cell specified by the third argument.
	 * The final arguments specify working cells (6 in total)
	 * THE TWO DIVISANDS ARE DESTROYED (zeroed).
	 * By an awesome coincidence, the numerator will be left as the remainder.
	 * THE WORKING CELLS ARE NOT ZEROED
	 * e.g.
	 * DIV @5 @3 1 0 2 4 6 7
	 * Divides cell 5 by cell 3, stores the answer in cell 1,
	 * and uses cells 0, 2, 4, 6 and 7 for working.
	 */
	public EARInstruction DIV = new EARInstruction(
			"DIV\\s+((@|@-)?\\d+\\s*){2}(-?\\d+\\s+){6}-?\\d+") {
		public String compile(String[] args) {
			String tempA,tempB;
			String output = "";
			String numerator = args[0];
			String denominator = args[1];
			String targetCell = args[2];
			//This cell will contain a flag for if the division fails
			String flag = args[3];
			String workingCell1 = args[4];
			String workingCell2 = args[5];
			String workingCell3 = args[6];
			String workingCell4 = args[7];
			String workingCell5 = args[8];
			boolean numeratorIsReference = (numerator.charAt(0) == '@');
			boolean denominatorIsReference = (denominator.charAt(0) == '@');

			//Zero the target cell
			output += ZERO.compile(new String[]{targetCell});

			if (!numeratorIsReference && !denominatorIsReference) {
				//Both absolute, just do the division at compile time.
				int a = Integer.parseInt(numerator);
				int b = Integer.parseInt(denominator);
				output += MOV.compile(new String[]{Integer.toString(a/b), targetCell});
			}
			else {
				//We now know at least one arg isn't absolute.
				//If one IS absolute, put it in the working cell.
				if (!numeratorIsReference) {
					output += MOV.compile(new String[]{numerator, workingCell1});
					numerator = "@" + workingCell1;
				}
				else if (!denominatorIsReference) {
					output += MOV.compile(new String[]{denominator, workingCell1});
					denominator = "@" + workingCell1;
				}

				//Strip off @s
				numerator = numerator.substring(1, numerator.length());
				denominator = denominator.substring(1, denominator.length());

				//Now we have 2 values in cells. We should divide them.
				//Note: we may no longer use workingCell1, as it may contain
				//one of the numbers we're calculating with.

				//Zero the working cells we'll use for the division
				output += ZERO.compile(new String[]{workingCell2});
				output += ZERO.compile(new String[]{workingCell3});
				output += ZERO.compile(new String[]{workingCell4});
				output += ZERO.compile(new String[]{workingCell5});

				//Set the flag to 1
				output += MOV.compile(new String[]{"1",flag});

				//Copy the denominator to working cell 2
				output += COPY.compile(new String[]{"@" + denominator, workingCell2, workingCell4});

				//While numerator not 0
				output += WHILE.compile(new String[]{numerator});

				//Use workingCell3 to indicate if one of the numbers hit 0, so we
				//don't try to subtract from it.
				output += MOV.compile(new String[]{"1", workingCell3});

				output += WHILE.compile(new String[]{workingCell3});

				//Subtract one from each
				output += SUB.compile(new String[]{"1",numerator});
				output += SUB.compile(new String[]{"1",denominator});

				//Work out if either hit 0
				//Proc for this is:
				//Copy into WC4, use WC5 as flag.

				//If numerator is 0
				output += COPY.compile(new String[]{"@" + numerator, workingCell4, workingCell5});
				output += MOV.compile(new String[]{"1", workingCell5});
				output += WHILE.compile(new String[]{workingCell4});
				output += ZERO.compile(new String[]{workingCell5});
				output += ZERO.compile(new String[]{workingCell4});
				output += ENDWHILE.compile(new String[]{});
				output += WHILE.compile(new String[]{workingCell5});
				//Set flag
				output += ZERO.compile(new String[]{workingCell3});
				output += ZERO.compile(new String[]{workingCell5});
				output += ENDWHILE.compile(new String[]{});

				//If denominator is 0
				output += COPY.compile(new String[]{"@" + denominator, workingCell4, workingCell5});
				output += MOV.compile(new String[]{"1", workingCell5});
				output += WHILE.compile(new String[]{workingCell4});
				output += ZERO.compile(new String[]{workingCell5});
				output += ZERO.compile(new String[]{workingCell4});
				output += ENDWHILE.compile(new String[]{});
				output += WHILE.compile(new String[]{workingCell5});
				//Set flag, increment counter and refill denominator
				output += ZERO.compile(new String[]{workingCell3});
				output += ZERO.compile(new String[]{workingCell5});
				output += ADD.compile(new String[]{"1", targetCell});
				output += COPY.compile(new String[]{"@" + workingCell2, denominator, workingCell4});
				output += ENDWHILE.compile(new String[]{});

				output += ENDWHILE.compile(new String[]{});

				output += ENDWHILE.compile(new String[]{});
			}

			return output;
		}
	};


	/**
	 * Copies one cell into all the given targets.
	 * Uses the final argument as a working cell.
	 * Note: Working cell is not used if value is absolute.
	 * DOES NOT DESTROY THE ORIGINAL
	 * e.g.
	 * COPY @2 3 4 5;
	 * Copies cell 2 into cells 3 & 4, using cell 5 as working space.
	 */
	public EARInstruction COPY = new EARInstruction(
			"COPY\\s+(@|@-)?\\d+\\s+(-?\\d+\\s+)+-?\\d+\\s*") {
		public String compile(String[] args) {
			String output = "";

			//If copying an absolute, use MOV, it's faster
			if (args[0].charAt(0)!='@') {
				String[] movArgs = Arrays.copyOfRange(args, 0, args.length-1);
				output += MOV.compile(movArgs);
				return output;
			}

			//Move last argument to beginning of argument list
			//for use in moving values back from working cell later
			//this makes sense if you think about it hard enough.
			//I promise.
			ArrayList<String> addArgs = new ArrayList<String>();
			addArgs.add("@"+args[args.length-1]);
			for (String item : Arrays.copyOfRange(args, 0, args.length-1)) {
				addArgs.add(item);
			}

			//Zero the working cell
			output += ZERO.compile(new String[]{args[args.length-1]});
			//Zero all target cells
			for (int i=1; i<args.length-1; i++) {
				output += ZERO.compile(new String[]{args[i]});
			}

			//Move the cell to be copied to the working cell & all targets
			output += ADD.compile(args);

			//Move it back from the working cell to the original if it was a reference.
			if (args[0].charAt(0)=='@') {
				output += ADD.compile(new String[]
						{"@"+args[args.length-1],args[0].substring(1)});
			}

			return output;
		}
	};

	/**
	 * Moves one cell into all the given targets.
	 * DESTROYS ORIGINAL
	 * e.g.
	 * MOV @2 3 4;
	 * Moves cell 2 into cells 3 & 4.
	 */
	public EARInstruction MOV = new EARInstruction(
			"MOV\\s+(@|@-)?\\d+\\s+(-?\\d+\\s+)*-?\\d+\\s*") {
		public String compile(String[] args) {
			String output = "";

			//Move last argument to beginning of argument list
			//for use in moving values back from working cell later
			//this makes sense if you think about it hard enough.
			//I promise.
			ArrayList<String> addArgs = new ArrayList<String>();
			addArgs.add("@"+args[args.length-1]);
			for (String item : Arrays.copyOfRange(args, 0, args.length-1)) {
				addArgs.add(item);
			}

			//Zero all target cells
			for (int i=1; i<args.length; i++) {
				output += ZERO.compile(new String[]{args[i]});
			}

			//Move the cell to be copied to the working cell & all targets
			output += ADD.compile(args);

			return output;
		}
	};
}
