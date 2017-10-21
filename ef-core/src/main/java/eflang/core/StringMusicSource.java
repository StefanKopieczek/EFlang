package eflang.core;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class StringMusicSource implements MusicSource {
    private List<String> notes;
    private long pos;

    public StringMusicSource(String music) {
        this.notes = Arrays.asList(music.split("\\s+"));
        this.pos = 0;
    }

    @Override
    public long getPos() {
        return pos;
    }

    @Override
    public void seek(long newPos) {
        pos = newPos;
    }

    @Override
    public boolean hasNext() {
        return pos < notes.size();
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No notes remaining");
        }

        return notes.get((int)pos++);
    }
}
