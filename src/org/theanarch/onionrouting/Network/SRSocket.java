package org.theanarch.onionrouting.Network;

import org.theanarch.onionrouting.NodeStorage.Node;
import org.theanarch.onionrouting.NodeStorage.Nodes;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

import static org.theanarch.onionrouting.Main.*;
import static org.theanarch.onionrouting.Network.Crypto.*;

public class SRSocket extends Socket {

    private InputStream in;
    private OutputStream out;
    private Node node;
    private SecretKey secret;

    private byte[] buf = new byte[8];

    public SRSocket(){
        super();
    }

    public SRSocket(String id)throws Exception {
        Nodes nodes = new Nodes().openList();
        this.node = nodes.getNode(id);

        connect(new InetSocketAddress(node.getAddress(), node.getPort()), 5000);
    }

    public boolean handshake()throws Exception {
        in = getInputStream();
        out = getOutputStream();

        if(node == null){
            boolean valid = serverHandshake();
            return valid;
        }else{
            boolean valid = clientHandshake();
            return valid;
        }
    }

    private boolean serverHandshake(){
        try{
            if(in.read() == 0x00){
                byte[] buffer = new byte[readInt()];
                in.read(buffer);
                String sessionId = new String(buffer);
                ServerSessionManager.Session session = ServerSessionManager.getSession(sessionId);
                if(session == null){
                    out.write(0x01);
                }else{
                    secret = session.secret;
                    out.write(0x00);
                    createWrappedInputStream();
                    createWrappedOutputStream();
                    return true;
                }
            }else{
                byte[] buffer = new byte[readInt()];
                in.read(buffer);

                PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(readPreference("key").getBytes())));
                //password = rsaDecrypt(new String(buffer), privateKey);
                generateSecret(rsaDecrypt(new String(buffer), privateKey));

                //we must create session
                String sessionId = UUID.randomUUID().toString();
                writeInt(sessionId.getBytes().length);
                out.write(sessionId.getBytes());
                ServerSessionManager.createSession(sessionId, secret);

                out.write(0x00);
                createWrappedInputStream();
                createWrappedOutputStream();
                return true;
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean clientHandshake(){
        return clientHandshake(node);
    }

    public boolean clientHandshake(Node node){
        ClientSessionManager.Session session = ClientSessionManager.getSession(node.getId());
        try{

            if(session == null){
                //HANDSHAKE
                out.write(0x01);

                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(node.getKey().getBytes())));

                SecureRandom random = new SecureRandom();
                byte[] salt = new byte[128];
                random.nextBytes(salt);
                //password = new String(salt);
                generateSecret(new String(salt));

                String encrypted = rsaEncrypt(salt, publicKey);
                writeInt(encrypted.getBytes().length);
                out.write(encrypted.getBytes());

                //we must create session
                byte[] buffer = new byte[readInt()];
                in.read(buffer);
                String sessionId = new String(buffer);
                ClientSessionManager.createSession(node.getId(), sessionId, secret);

                if(in.read() == 0x00){
                    createWrappedInputStream();
                    createWrappedOutputStream();
                    return true;
                }

            }else{
                out.write(0x00);
                writeInt(session.sessionId.getBytes().length);
                out.write(session.sessionId.getBytes());
                secret = session.secret;

                if(in.read() == 0x00){
                    createWrappedInputStream();
                    createWrappedOutputStream();
                    return true;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private void readFully(byte[] b, int off, int len)throws Exception {
        if(len < 0){
            throw new IndexOutOfBoundsException("Negative length: "+len);
        }

        while(len > 0){
            int numread = in.read(b, off, len);
            if(numread < 0){
                throw new EOFException();
            }
            len -= numread;
            off += numread;
        }
    }

    private int readInt()throws Exception {
        readFully(buf, 0, 4);
        return (((buf[0] & 0xff) << 24) | ((buf[1] & 0xff) << 16) | ((buf[2] & 0xff) << 8) | (buf[3] & 0xff));
    }

    private void writeInt(int value)throws Exception {
        byte[] buf = new byte[]{
                (byte) (0xff & (value >> 24)),
                (byte) (0xff & (value >> 16)),
                (byte) (0xff & (value >> 8)),
                (byte) (0xff & value) };
        out.write(buf);
    }



    private void generateSecret(String password)throws Exception {
        byte[] salt = new byte[8];
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(keySpec);
        secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    public SROutputStream getSecureOutputStream()throws Exception {
        if(out instanceof SROutputStream){
            return (SROutputStream) out;
        }else{
            throw new Exception("Handshake was ether not made or not approved");
        }
    }

    public SRInputStream getSecureInputStream()throws Exception {
        if(in instanceof SRInputStream){
            return (SRInputStream) in;
        }else{
            throw new Exception("Handshake was ether not made or not approved");
        }
    }

    private SROutputStream createWrappedOutputStream()throws Exception {
        byte[] iv = new byte[16];

        Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));

        out = new SROutputStream(out, cipher);
        return (SROutputStream) out;
    }

    private SRInputStream createWrappedInputStream()throws Exception {
        byte[] iv = new byte[16];

        Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));

        in = new SRInputStream(in, cipher);
        return (SRInputStream) in;
    }

    public void quickClose(){
        try{
            if(!isOutputShutdown()){
                shutdownOutput();
            }
            if(!isInputShutdown()){
                shutdownInput();
            }

            close();
        }catch(Exception e){
            //e.printStackTrace();
        }
    }
}
