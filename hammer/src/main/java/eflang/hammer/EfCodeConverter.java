package eflang.hammer;

import eflang.core.MusicSource;
import eflang.core.StringMusicSource;

public class EfCodeConverter implements CodeConverter {
    @Override
    public MusicSource apply(String code) {
        return new StringMusicSource(code);
    }
}
