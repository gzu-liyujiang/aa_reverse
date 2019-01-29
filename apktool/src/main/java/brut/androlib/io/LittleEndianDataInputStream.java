//by       : aenu
//license  : MIT
package brut.androlib.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class LittleEndianDataInputStream extends FilterInputStream implements DataInput {

    public LittleEndianDataInputStream(InputStream in) {
        super(in);
    }

    @Override
    public String readLine() {
        throw new UnsupportedOperationException("readLine");
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        if(b==null)
            throw new NullPointerException();
        readFully(b,0,b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        if(b==null)
            throw new NullPointerException();
        int total=len-off;
        int l=read(b,off,len);
        if(l!=total)
            throw new EOFException();
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return (int) in.skip(n);
    }

    @Override
    public int readUnsignedByte() throws IOException {
        int b1 = in.read();
        if (b1<0) {
            throw new EOFException();
        }

        return b1;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        int b1 = readAndCheckByte()&255;
        int b2 = readAndCheckByte()&255;

        return (b2<<8)|(b1<<0);
    }

    @Override
    public int readInt() throws IOException {
        int b1 = readAndCheckByte()&255;
        int b2 = readAndCheckByte()&255;
        int b3 = readAndCheckByte()&255;
        int b4 = readAndCheckByte()&255;
        return (b4<<24)|(b3<<16)|(b2<<8)|(b1<<0);
    }

    @Override
    public long readLong() throws IOException {
        long b1 = readAndCheckByte()&255L;
        long b2 = readAndCheckByte()&255L;
        long b3 = readAndCheckByte()&255L;
        long b4 = readAndCheckByte()&255L;
        long b5 = readAndCheckByte()&255L;
        long b6 = readAndCheckByte()&255L;
        long b7 = readAndCheckByte()&255L;
        long b8 = readAndCheckByte()&255L;

        return (b8<<56L)|(b7<<48L)|(b6<<40L)|(b5<<32L)|(b4<<24L)|(b3<<16L)|(b2<<8L)|(b1<<0L);
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readUTF() throws IOException {
        return new DataInputStream(in).readUTF();
    }

    @Override
    public short readShort() throws IOException {
        return (short) readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return (char) readUnsignedShort();
    }

    @Override
    public byte readByte() throws IOException {
        return (byte) readUnsignedByte();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return readUnsignedByte() != 0;
    }

    private byte readAndCheckByte() throws IOException, EOFException {
        int b1 = in.read();

        if (b1==-1) {
            throw new EOFException();
        }

        return (byte)b1;
    }
}

