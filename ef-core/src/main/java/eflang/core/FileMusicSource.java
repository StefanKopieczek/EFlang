package eflang.core;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.NoSuchElementException;

/**
 * Allows playing music from a file on disk without pulling the whole thing into memory.
 */
public class FileMusicSource implements MusicSource {
    private RandomAccessFile file;
    private String bufferedToken;
    private long bufferedTokenLocation;
    private long currentTokenLocation;

    public FileMusicSource(RandomAccessFile file) {
        this.file = file;
        this.bufferedTokenLocation = 0;
        this.currentTokenLocation = 0;
    }

    @Override
    public long getPos() {
        return currentTokenLocation;
    }

    @Override
    public void seek(long pos) {
        try {
            file.seek(pos);
            bufferedToken = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() {
        if (bufferedToken != null) {
            return true;
        }

        try {
            bufferNextToken();
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public String next() {
        if (bufferedToken == null) {
            bufferNextToken();
        }

        currentTokenLocation = bufferedTokenLocation;
        String token = bufferedToken;
        bufferedToken = null;
        return token;
    }

    private void bufferNextToken() {
        char c = ' ';

        // Skip whitespace.
        while (Character.isWhitespace(c)) {
            c = readCharOrThrow();
        }

        try {
            bufferedTokenLocation = file.getFilePointer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Read until whitespace.
        StringBuilder token = new StringBuilder(3);  // Longest possible token is length 3.
        while (!Character.isWhitespace(c)) {
            token.append(c);
            c = readCharOrThrow();
        }
        String note = token.toString();
        bufferedToken = note;
    }

    private char readCharOrThrow() {
        try {
            char c = (char)file.readByte();
            return c;
        } catch (IOException e) {
            throw new NoSuchElementException("File finished");
        }
    }
}
