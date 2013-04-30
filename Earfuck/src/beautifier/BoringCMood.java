package beautifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class BoringCMood implements MusicMood {
	private String[] notes = new String[]{
			"c2","d2","e2","f2","g2","a2","b2",
			"c3","d3","e3","f3","g3","a3","b3",
			"c4","d4","e4","f4","g4","a4","b4",
			"c5","d5","e5","f5","g5","a5","b5"};
	
	private Random mRandom;
	
	public BoringCMood() {
		mRandom = new Random();
	}

	@Override
	public String getHigherThan(String note) throws ComposerTooDemandingException {
		return getHigherThan(note, new String[]{});
	}

	@Override
	public String getHigherThan(String note, String[] disallowedNotes) 
			throws ComposerTooDemandingException {
		int index = Arrays.asList(notes).indexOf(note);
		ArrayList<String> possibleNotes = new ArrayList<String>();
		
		for (int i=index+1; i<notes.length; i++) {
			if (!Arrays.asList(disallowedNotes).contains(notes[i])) {
				possibleNotes.add(notes[i]);
			}
		}
		
		for (String n : possibleNotes.toArray(new String[]{})) {
			if (mRandom.nextFloat() > 0.5) {
				return n;
			}
		}
		
		throw new ComposerTooDemandingException(
				"There is no valid note higher than: "+note+".");
	}

	@Override
	public String getLowerThan(String note) throws ComposerTooDemandingException {
		return getLowerThan(note, new String[]{});
	}

	@Override
	public String getLowerThan(String note, String[] disallowedNotes) 
			throws ComposerTooDemandingException {
		int index = Arrays.asList(notes).indexOf(note);
		ArrayList<String> possibleNotes = new ArrayList<String>();
		
		for (int i=index-1; i>0; i--) {
			if (!Arrays.asList(disallowedNotes).contains(notes[i])) {
				possibleNotes.add(notes[i]);
			}
		}
		
		for (String n : possibleNotes.toArray(new String[]{})) {
			if (mRandom.nextFloat() > 0.5) {
				return n;
			}
		}
		
		throw new ComposerTooDemandingException(
				"There is no valid note higher than: "+note+".");
	}

	@Override
	public boolean isHigherThan(String note1, String note2) {
		int i1 = Arrays.asList(notes).indexOf(note1);
		int i2 = Arrays.asList(notes).indexOf(note2);
		
		return i1 > i2;
	}

	@Override
	public boolean isEqual(String note1, String note2) {
		return note1.equals(note2);
	}

	@Override
	public String getFirstNote() {
		return "c4";
	}

}
