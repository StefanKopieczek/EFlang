package eflang.core;

import java.util.Iterator;

public interface MusicSource extends Iterator<String> {
    int getPos();
    void seek(int pos);
}
