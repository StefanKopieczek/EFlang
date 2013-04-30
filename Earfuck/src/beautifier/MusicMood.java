package beautifier;

public interface MusicMood {
	public String getFirstNote();
	public String getHigherThan(String note) throws ComposerTooDemandingException;
	public String getHigherThan(String note, String[] disallowedNotes)
			throws ComposerTooDemandingException;
	public String getLowerThan(String note) throws ComposerTooDemandingException;
	public String getLowerThan(String note, String[] disallowedNotes)
			throws ComposerTooDemandingException;
	public boolean isHigherThan(String note1, String note2);
	public boolean isEqual(String note1, String note2);
}
