package de.tub.pes.syscir.analysis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Fans all writes out to two underlying streams.
 * <p>
 * Neither stream is closed when {@link #close()} is called; callers are responsible for closing
 * them independently.
 */
public class TeeOutputStream extends OutputStream {

    public static PrintStream createFilePrintStream(File file) throws FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(file);
        return new PrintStream(fos, true, StandardCharsets.UTF_8);
    }

    private final OutputStream first;
    private final OutputStream second;

    public TeeOutputStream(OutputStream first, OutputStream second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public void write(int b) throws IOException {
        this.first.write(b);
        this.second.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.first.write(b, off, len);
        this.second.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        this.first.flush();
        this.second.flush();
    }

    // close() intentionally not overridden — callers manage stream lifecycles
}
