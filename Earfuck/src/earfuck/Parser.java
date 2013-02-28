package earfuck;
import java.util.Scanner;
import java.util.Stack;

public class Parser {	
	private static Integer INITIAL_ATTACK_VALUE = 63;
	
	/**
	 * The performer understands the musical notation we use, and is able to
	 * output audio (either to a MIDI file or to a current MIDI stream).
	 */	
	MidiStreamPerformer mPerformer = new MidiStreamPerformer();
	
	/**
	 * Either 'numeric' or 'ascii' - this determines how we render characters
	 * when we print the contents of a cell to output. 
	 */
	String mOutputMode;
	
	/**
	 * The 'ambiance' is the state of the audience's mind, as represented by a
	 * both-ways unbounded linear array of integers. Each time a note is played
	 * that is higher than the last, the audience shift to a more optimistic 
	 * cell (one to the right); and similarly playing a note lower than the 
	 * previous causes the audience to mentally shift to a more pessimistic 
	 * cell (to the left).
	 * Repetition of notes will increment or decrement the value of the 
	 * audience's current mental state, depending on whether the last change of
	 * state was an optimistic or a pessimistic one. 
	 */
	EarfuckMemory mAmbiance;
	
	/**
	 * The 'mental state' of the audience represents which mental state in the 
	 * general ambiance the audience is currently in. This is, naturally, 
	 * modelled by an index to a particular mind-state in the linear ambiance
	 * array.
	 */
	Integer mMentalState;
	
	/**
	 * The audience is always either optimistic, pessimistic, or neutral.
	 * They will be optimistic if the current note is higher than the last
	 * (distinct) note, and pessimistic if it is lower. If all notes that have
	 * been played are the same (or no notes have been played), the audience 
	 * are neutral.
	 */
	Integer mOptimism;
	
	/**
	 * The excitement represents the - well, excitement - of the performers.
	 * The more optimistic they make the audience, the more excited the 
	 * performers become - up to a point. Similarly, if the audience are
	 * pessimistic the performers will become increasingly depressed.
	 * Excited performers will get carried away, and will therefore play more
	 * loudly than usual - correspondingly, depressed performers play more 
	 * quietly.
	 */
	int mExcitement;
	
	/**
	 * The note duration represents the amount of time that each note should
	 * be held for as a fraction of the whole note (breve).
	 */
	float mNoteDuration;
	
	/**
	 * This array contains all the musical tokens that the program is 
	 * currently executing. Tokens can be one of:
	 *  - Musical notes (valid forms e.g.: C, F, Bb, C4, A#5)
	 *  - Rests (written as 'r')
	 *  - Brackets '(' or ')'. Notes inside brackets are played double-time.
	 */
	String[] mComposition;	
	
	/**
	 * The index of the current musical token that is being processed.
	 */
	int mPointer;
	
	/**
	 * Current place in the music.
	 */
	Integer mPlace;
	
	/**
	 * The last note played.
	 */
	String mPreviousNote;
	
	/**
	 * The performers *love* playing changes in rhythm.
	 * In fact they love it so much, that they will play each double-time 
	 * section until the audience gets bored (that is, until the ambiance is 
	 * zero for the current memory state).
	 * In order to do this, we need to keep track of the successive '(' 
	 * characters' positions so that we can return to that position in the code
	 * when we want to replay a section.
	 */
	Stack<Integer> mBrackets;
	
	/**
	 * Bracketed sections are skipped if the ambiance value of the current
	 * mental state is 0. This variable counts the number of left brackets we
	 * have skipped so far, minus the number of right-brackets we have skipped.
	 */
	int mBracketsSkipped;		
	
	Scanner sc;
	
	public Parser() {
		mOutputMode = "numeric";
		refreshState();
	}
	
	/**
	 * Clear all stateful data ready for a new run of code.
	 */
	public void refreshState() {		
		mAmbiance = new EarfuckMemory();
		mOptimism = 0;
		mMentalState = 0;
		mExcitement = INITIAL_ATTACK_VALUE;
		mNoteDuration = 0.25f;
		
		mComposition = new String[0];
		mPointer = 0;
		mPlace = 0;
		mPreviousNote = null;
		mBracketsSkipped = 0;
		mBrackets = new Stack<Integer>();
		
		sc = new Scanner(System.in);
	}
	
	/**
	 * Sets the piece to be played.<br />
	 * Should only be called ONCE, BEFORE execution.
	 * @param piece String of EF Code
	 */
	public void giveMusic(String piece) {
		mComposition = piece.split("\\s+");
	}
	
	/**
	 * Perform the whole piece
	 * 	 * 
	 * @param piece The piece to play - comprised of musical tokens separated
	 * by whitespace.
	 */
	public void perform() {	
		while (mPlace < mComposition.length - 1) {
			stepForward();
		}
		
		mPerformer.onPieceEnd();
		sc.close();
	}
	
	
	public void stepForward() {
		String command = mComposition[mPlace];
		executeCommand(command);
		mPlace++;
	}
	
	/**
	 * Execute the given command.
	 */
	public void executeCommand(String command) {			
		
		if (command.equals("(")) {
			if (mAmbiance.get(mMentalState) == 0) {
				// We skip this double-time section because the audience is
				// bored.
				// We record how many brackets we pass so that we can stop
				// skipping after the matching ')' character.
				mBracketsSkipped += 1;					
			}
			else {
				// The audience isn't bored, so we play this section.
				// Bracketed sections are played double-time.
				// We also record where we are in the piece so that if the
				// audience isn't bored when we finish this section, we can
				// re-play it.
				mNoteDuration /= 2;
				mBrackets.push(mPlace);
			}
			return;
		}			
		else if (command.equals(")")) {
			if (mBracketsSkipped > 0) {
				// We're skipping a bracketed section at the moment and we
				// haven't got to the end of it, so we note the closing
				// bracket, and continue skipping the section.
				mBracketsSkipped -= 1;					
			}
			else {
				// We've reached the end of a double-time section which we
				// are currently playing through.
				// We resume normal tempo; and if the audience isn't bored
				// we skip back to the start of the section and play it 
				// again!
				Integer startPlace = mBrackets.pop();
				mNoteDuration *= 2;
				if (mAmbiance.get(mMentalState) != 0) {
					// Skip back to just before the opening bracket at the
					// start of this section - we increment by one at the 
					// start of the loop anyway.
					mPlace = startPlace - 1;					
				}
			}
			return;
		}
		
		if (mBracketsSkipped != 0) {
			// If we're skipping through a double-time section, we 
			// shouldn't pay attention to anything other than brackets, so
			// just skip ahead in the loop.
			return;
		}
		
		if (command.equals("r")) {	
			// We've hit a rest - so add it to the play queue.

			if (mOptimism < 0) {
				// When the audience are pessimistic on a rest, we ask for
				// a value from the user to cheer them up.
				System.out.print(":> ");					
				Integer x = sc.nextInt();		
				mAmbiance.put(mMentalState,x);
			}
			else if (mOptimism > 0) {
				// When the audience are optimistic on a rest, they want to
				// tell everyone about it so they tell STDOUT about their
				// ambiance in the current mental state.
				if (mOutputMode.equals("numeric")) {
					// In numeric mode, we display the ambiance value as an
					// integer, and add a newline when outputting.
					System.out.println(mAmbiance.get(mMentalState));
				}
				else if (mOutputMode.equals("ascii")) {
					// In ascii mode, we display the ambiance value as an
					// ascii char, and don't print a newline afterwards.
					System.out.print((char)(mAmbiance.get(mMentalState).intValue()));
				}
			}
			mPerformer.addNote("R", mNoteDuration);
			mPerformer.onRest();
			return;
		}
		
		if ((mPreviousNote != null) && 
		        (mPerformer.getNoteValue(command) == 
		         mPerformer.getNoteValue(mPreviousNote))) {
			// We just played a repeated note.
			// We increase the ambiance value in this mental state if the
			// audience are optimistic, and decrease if we're pessimistic.
			mAmbiance.put(mMentalState,mAmbiance.get(mMentalState) + mOptimism);
		}
		else if (mPreviousNote != null) {
			// We just played a note that wasn't the same as the previous
			// (distinct) note.
			// We are optimistic if the pitch is now higher, and 
			// pessimistic otherwise.
			boolean isHappy = mPerformer.getNoteValue(command) > 
			                  	mPerformer.getNoteValue(mPreviousNote);
			mOptimism = isHappy? 1 : -1;
			mMentalState += mOptimism;
		}
										
		// Calculate how excited the performers are, and then queue the
		// note to be played.
		getExcited();
		mPerformer.addNote(command, mNoteDuration, mExcitement);	
		
		if (isNote(command)) {
			// The last command was a note.
			// Remember it in order to decide on future optimism.
			mPreviousNote = command;
		}
	}
	/**
	 * Calculates the excitement of the performers, based on their current 
	 * excitement levels, and on their optimism.
	 */
	private void getExcited() {
		// Get more excited if we're optimistic, and more depressed otherwise.
		mExcitement += mOptimism * 8;
			
		// Our excitement is bounded above and below to avoid us getting stuck
		// in an unending state of mania or depression.
		int maxExcitement = 127;
		int minExcitement = 23;
				
		if (mOptimism == 1) {			
			// We can't be too depressed if we're optimistic, can we?
			minExcitement = 47;
		}
		if (mOptimism == -1) {
			// It's harder to get excited when you're depressed.
			maxExcitement = 79;
		}
		
		// Curb our boundless excitement by the above max and min values.
		mExcitement = Math.min(mExcitement, maxExcitement);
		mExcitement = Math.max(mExcitement, minExcitement);
	}
	
	/**
	 * Determines if the given string represents a single note.
	 * It is assumed that the string is a valid token (invalids return true!)
	 * @param token The musical token to test.
	 * @return True if the token represents a single note; false otherwise.
	 */
	static boolean isNote(String token) {
		// Everything other than a bracket or rest is a note, assuming the 
		// token is valid.
		return (!(token.equals("(") || 
				  token.equals(")") || 
				  token.equals("r")));  
	}
	
	/**
	 * Returns the piece we are currently playing.
	 * @return
	 */
	public String[] getPiece() {
		return mComposition;
	}
	
	public int getPlace() {
		return mPlace;
	}
}