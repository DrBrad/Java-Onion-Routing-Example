package org.theanarch.onionrouting.Network;

import javax.crypto.Cipher;
import javax.crypto.NullCipher;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SROutputStream extends FilterOutputStream {

    private Cipher cipher;

    public SROutputStream(OutputStream out){
        this(out, new NullCipher());
    }

    public SROutputStream(OutputStream out, Cipher cipher){
        super(out);
        this.cipher = cipher;
    }

    public void flush()throws IOException {
        out.flush();
    }

    public void write(int value)throws IOException {
        write(new byte[]{ (byte) value }, 0, 1);
    }

    public void write(byte[] buf)throws IOException {
        write(buf, 0, buf.length);
    }

    public void write(byte[] buf, int off, int len)throws IOException {
        try{
            byte[] crypted = cipher.update(buf, off, len);
            out.write(crypted);
        }catch(Exception e){
            IOException ioex = new IOException(String.valueOf(e));
            ioex.initCause(e);
            throw ioex;
        }
    }

    public void writeInt(int value)throws Exception {
        byte[] buf = new byte[]{
                (byte) (0xff & (value >> 24)),
                (byte) (0xff & (value >> 16)),
                (byte) (0xff & (value >> 8)),
                (byte) (0xff & value) };
        write(buf);
    }
}
