package beautifier;

/**
 * Represents a single pitch.
 * @author Stefan
 * 
 * TODO: Custom comparison; range checking in constructor, next-note, prev-note
 * and changePitch; possibly 'get notes in range' function.
 */

public class Note {
    private static final Note minNote = new Note("a2");
    private static final Note maxNoteString = new Note("b6");
    
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
        int octave = (int)(noteString.charAt(noteString.length()-1));
        
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
    }
    
    public String toString() {
        // TODO
        return null;
    }
    
    public Note getNextNote() {
        // TODO
        return null;
    }
    
    public Note getPrevNote() {
        // TODO
        return null;
    }    
    
    public void changePitch(int delta) {
        // Need to check note bounds here.
        mNoteValue += delta;
    }       
    
    protected boolean isValidNoteString(String noteString) {
        boolean isValid = true;        
        char noteChar = noteString.charAt(0);
        char octChar = noteString.charAt(noteString.length() - 1);
        char accidentalChar = noteString.length() == 3 ? noteString.charAt(2) : '\u0000';        
        isValid &= (noteString.length() == 2 || noteString.length() == 3);
        isValid &= (noteChar >= 'a' && noteChar <= 'g');
        isValid &= (octChar >= '2' && octChar <= '6');
        isValid &= (accidentalChar =='\u0000' || accidentalChar == 'b' || accidentalChar == '#');
        return isValid;
    }    
}
