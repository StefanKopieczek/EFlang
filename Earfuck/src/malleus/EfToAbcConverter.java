package malleus;

public class EfToAbcConverter {
    private static String DEFAULT_NOTE_LENGTH = "1/4";

    public static void main(String[] args) {
        String ef = "c4 c5 d6 e4 fb4";

        System.out.println(EfToAbcConverter.convert(ef));
    }

    public static String convert(String efCode) {
        String[] tokens = efCode.split("\\s+");

        StringBuilder builder = new StringBuilder();

        int noteLength = 1;

        int index = 1;
        String title = "Title";
        String meter = "4/4";
        String key = "C";


        builder.append("X:" + Integer.toString(index) + "\n");
        builder.append("T:" + title + "\n");
        builder.append("M:" + meter + "\n");
        builder.append("L:" + DEFAULT_NOTE_LENGTH + "\n");
        builder.append("K:" + key + "\n");

        for (String token : tokens) {
            if (token == "(") {
               noteLength *= 2;
            }
            else if (token == ")") {
               noteLength /= 2;
            } 
            else {
                builder.append(efNoteToAbc(token, noteLength));
            }
        }

        return builder.toString();
    } 

    private static String efNoteToAbc(String token, int length) {
        char note = token.charAt(0);
        char octave = token.charAt(1);
        int octaveIndex;
        String sharpOrFlat = "";
        char HIGH_OCTAVE_SUFFIX = '`';
        char LOW_OCTAVE_SUFFIX = ',';
        String SHARP_PREFIX = "^";
        String FLAT_PREFIX = "_";

        StringBuilder builder = new StringBuilder();

        if (octave == '#' || octave == 'b') {
            // The second character wasn't an octave index like expected.
            // It was a sharp/flat modifier. Handle this here and then get
            // the actual octave index from the next character.
            sharpOrFlat = (octave == '#') ? SHARP_PREFIX : FLAT_PREFIX;
            octave = token.charAt(2);
        }

        octaveIndex = Character.getNumericValue(octave);

        if (octaveIndex >= 5) {
            note = Character.toUpperCase(note);
        }
        else {
            note = Character.toLowerCase(note);
        }

        builder.append(sharpOrFlat);
        builder.append(note);

        for (int i=6; i <= octaveIndex; i++) {
            builder.append(HIGH_OCTAVE_SUFFIX);
        }

        for (int i=3; i >= octaveIndex; i--) {
            builder.append(LOW_OCTAVE_SUFFIX);
        }

        if (length > 1) {
            builder.append('/');
            builder.append(length);
        }

        return builder.toString();
    }
}
