package org.theanarch.onionrouting.Network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

public class SRServerSocket extends ServerSocket {

    public SRServerSocket(int port)throws IOException {
        super(port);
    }

    @Override
    public SRSocket accept()throws IOException {
        if(isClosed()){
            throw new SocketException("Socket is closed");
        }
        if(!isBound()){
            throw new SocketException("Socket is not bound yet");
        }

        SRSocket socket = new SRSocket();
        implAccept(socket);
        return socket;
    }
}
