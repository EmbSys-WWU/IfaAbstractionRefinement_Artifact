package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Class for parsing and representing a string with bytes like in C.
 * 
 * @author Lukas Ernst
 */
public class CString {

    private static final char[] simpleEscapeChars = "abefnrtv\\'\"?".toCharArray();
    private static final byte[] simpleEscapeCharsOutput =
            new byte[] {0x7, 0x8, 0x1B, 0xC, '\n', '\r', '\t', 0xB, '\\', '\'', '\"', '?'};

    private final byte[] bytes;

    public CString(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other == null || other.getClass() != this.getClass())
            return false;
        return Arrays.equals(this.bytes, ((CString) other).getBytes());
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (byte b : this.bytes)
            out.appendCodePoint(b);
        return out.toString();
    }

    public String toString(Charset charset) {
        return new String(this.bytes, charset);
    }

    public static CString deescape(String escapedString, Charset charset) {
        CStringWriter out = new CStringWriter(charset);
        int index = 0;
        charloop: while (index < escapedString.length()) {
            char c = escapedString.charAt(index++);
            if (c != '\\') {
                out.appendCharacter(c);
                continue;
            }
            c = escapedString.charAt(index++);
            for (int s = 0; s < simpleEscapeChars.length; s++) {
                if (c == simpleEscapeChars[s]) {
                    out.appendByte(simpleEscapeCharsOutput[s]);
                    continue charloop;
                }
            }
            if ('0' <= c && c <= '9') {
                index--;
                int value = 0;
                for (int b = 6; b >= 0; b -= 3)
                    value |= decodeHex(escapedString.charAt(index++)) << b;
                out.appendByte((byte) value);
                continue;
            }
            if (c == 'x') {
                int value = 0;
                for (int b = 4; b >= 0; b -= 4)
                    value |= decodeHex(escapedString.charAt(index++)) << b;
                out.appendByte((byte) value);
                continue;
            }
            if (c == 'u' || c == 'U') {
                int codepoint = 0;
                for (int b = c == 'u' ? 12 : 28; b >= 0; b -= 4)
                    codepoint |= decodeHex(escapedString.charAt(index++)) << b;
                if (codepoint < 0 || codepoint > 0x10FFFF)
                    throw new IllegalArgumentException(
                            "Invalid codepoint " + Long.toHexString(codepoint & 0xFFFFFFFFL));
                out.appendCodePoint(codepoint);
                continue;
            }
            throw new IllegalArgumentException("Invalid escape sequence \"\\" + c + "\"");
        }
        return out.finish();
    }

    private static int decodeHex(char c) {
        if ('0' <= c && c <= '9')
            return c - '0';
        if ('a' <= c && c <= 'z')
            return 10 + (c - 'a');
        if ('A' <= c && c <= 'Z')
            return 10 + (c - 'A');
        throw new IllegalArgumentException("Not a hexadecimal character: '" + c + "'");
    }

    public static class CStringWriter {

        private final ByteArrayOutputStream outputBytes;
        private final StringBuilder characterBuffer;
        private final Charset charset;

        public CStringWriter(Charset charset) {
            this.outputBytes = new ByteArrayOutputStream();
            this.characterBuffer = new StringBuilder();
            this.charset = charset;
        }

        public void flushCharacters() {
            if (this.characterBuffer.isEmpty())
                return;
            this.outputBytes.writeBytes(this.characterBuffer.toString().getBytes(this.charset));
            this.characterBuffer.setLength(0);
        }

        public void appendCharacter(char c) {
            this.characterBuffer.append(c);
        }

        public void appendCodePoint(int codepoint) {
            this.characterBuffer.appendCodePoint(codepoint);
        }

        public void appendByte(byte b) {
            flushCharacters();
            outputBytes.write(b);
        }

        public CString finish() {
            flushCharacters();
            return new CString(this.outputBytes.toByteArray());
        }
    }

}
