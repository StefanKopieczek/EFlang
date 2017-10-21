package eflang.hammer;

import eflang.core.MusicSource;
import eflang.core.StringMusicSource;

import java.util.function.Supplier;

public class EFCodeSupplier implements Supplier<MusicSource> {
    private String efCode;

    public EFCodeSupplier(String efCode) {
        this.efCode = efCode;
    }

    @Override
    public MusicSource get() {
        return new StringMusicSource(efCode);
    }
}
