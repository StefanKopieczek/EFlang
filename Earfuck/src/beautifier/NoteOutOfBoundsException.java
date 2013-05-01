package beautifier;

public class NoteOutOfBoundsException extends RuntimeException {
    public NoteOutOfBoundsException(String message) {
        super(message);
    }

	public NoteOutOfBoundsException(Note note) {
		this(note.toString());
	}
}

