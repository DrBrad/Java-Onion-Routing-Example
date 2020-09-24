package org.theanarch.onionrouting.Network;

import javax.crypto.Cipher;
import javax.crypto.NullCipher;
import javax.crypto.ShortBufferException;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SRInputStream extends FilterInputStream {

    private Cipher cipher;

    public SRInputStream(InputStream in){
        this(in, new NullCipher());
    }

    public SRInputStream(InputStream in, Cipher cipher){
        super(in);
        this.cipher = cipher;
    }

    public int read()throws IOException {
        byte[] buf = new byte[1];
        read(buf, 0, 1);
        return buf[0];
    }

    public int read(byte[] buf)throws IOException {
        return read(buf, 0, buf.length);
    }

    public int read(byte[] buf, int off, int len)throws IOException {
        try{
            int length = in.read(buf, off, len);
            if(length > 0){
                length = cipher.update(buf, 0, length, buf, 0);
            }
            return length;
        }catch(ShortBufferException e){
            return 0;
        }
    }


    private byte[] buf = new byte[8];

    public void readFully(byte[] b, int off, int len)throws Exception {
        if(len < 0){
            throw new IndexOutOfBoundsException("Negative length: "+len);
        }

        while(len > 0){
            int numread = read(b, off, len);
            if(numread < 0){
                throw new EOFException();
            }
            len -= numread;
            off += numread;
        }
    }

    public int readInt()throws Exception {
        readFully(buf, 0, 4);
        return (((buf[0] & 0xff) << 24) | ((buf[1] & 0xff) << 16) | ((buf[2] & 0xff) << 8) | (buf[3] & 0xff));
    }
}
