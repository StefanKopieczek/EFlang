package beautifier;

/**
 * Represents a single pitch.
 * @author Stefan
 * 
 * TODO: Get notes in range, get note name, get octave.
 */

public class Note implements Comparable<Note> {
    private static final Note minNote = new Note("a2");
    private static final Note maxNote = new Note("b6");
    
    private static final String[] noteNames = {"c", "c#", "d", "eb", "e", "f", 
    	                                       "f#", "g", "g#", "a", "bb", "b"};
    
    private int mNoteValue;
    
    public Note(Note other) {
        this(other.toString());
    }
    
    public Note(String noteString) throws InvalidNoteException {
        noteString = noteString.toLowerCase();
        if (!isValidNoteString(noteString)) {
            throw new InvalidNoteException(noteString);
        }        
        int asciiIndexOfLowercaseC = 99;
        int semitonesInOctave = 13;
        int noteValue = (int)noteString.charAt(0) - asciiIndexOfLowercaseC;        
        int octave = Character.getNumericValue(noteString.charAt(noteString.length()-1));
        
        noteValue += noteValue < 0 ? 13 : 0; // Add 8ve if == a or b.     
        noteValue += semitonesInOctave * octave;
        
        if (noteString.length() == 3) {
            switch (noteString.charAt(1)) {
                case '#': noteValue += 1;
                          break;
                case 'b': noteValue -= 1;
                          break;
                default:  throw new InvalidNoteException(noteString);                          
            }
        }
        
        mNoteValue = noteValue;
        
        if (minNote != null && this.compareTo(minNote) < 0 || 
            maxNote != null && this.compareTo(maxNote) > 0) {
        	throw new NoteOutOfBoundsException(this);
        }                        
    }
    
    public int compareTo(Note other) {
    	return mNoteValue - other.getNoteValue();
    }
    
    @Override
    public boolean equals(Object other) {
    	boolean result;
    	if (!(other instanceof Note)) {
    		result = false;
    	}
    	else {
    		result = mNoteValue == ((Note)other).getNoteValue();
    	}
    	return result;
    }
    
    public int getNoteValue() {
    	return mNoteValue;
    }
    
    public String toString() {    	
        int mRawNoteValue = mNoteValue % 13;
        int octaves = mNoteValue / 13;        
        String result = noteNames[mRawNoteValue] + Integer.toString(octaves);
        return result;        
    }
    
    public Note getNextNote() {
        Note newNote = new Note(this);
        newNote.changePitch(1);
        return newNote;
    }
    
    public Note getPrevNote() {
    	Note newNote = new Note(this);
        newNote.changePitch(-1);
        return newNote;
    }    
    
    public void changePitch(int delta) {        
    	if (mNoteValue + delta < minNote.getNoteValue() ||
    	    mNoteValue + delta > maxNote.getNoteValue()) 
    	{
    		throw new NoteOutOfBoundsException(this.toString() + " + " + delta + " semitones");
    	}
    	else 
    	{
    		mNoteValue += delta;
    	}
    }       
    
    protected boolean isValidNoteString(String noteString) {
        boolean isValid = true;        
        char noteChar = noteString.charAt(0);
        char octChar = noteString.charAt(noteString.length() - 1);
        char accidentalChar = noteString.length() == 3 ? noteString.charAt(1) : '\u0000';        
        isValid &= (noteString.length() == 2 || noteString.length() == 3);
        isValid &= (noteChar >= 'a' && noteChar <= 'g');
        isValid &= (octChar >= '2' && octChar <= '6');
        isValid &= (accidentalChar =='\u0000' || accidentalChar == 'b' || accidentalChar == '#');
        return isValid;
    }
       
}
