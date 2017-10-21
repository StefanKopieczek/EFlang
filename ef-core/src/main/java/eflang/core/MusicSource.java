package eflang.core;

import java.util.Iterator;

public interface MusicSource extends Iterator<String> {
    long getPos();
    void seek(long pos);
}
