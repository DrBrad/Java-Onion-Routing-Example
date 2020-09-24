package org.theanarch.onionrouting.Server;

import org.theanarch.onionrouting.Network.SRInputStream;
import org.theanarch.onionrouting.Network.SROutputStream;
import org.theanarch.onionrouting.Network.SRSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

public class Tunnel extends Thread {

    private SRSocket socket;
    private SRSocket server;
    private SRInputStream clientIn;
    private SROutputStream clientOut;
    private InputStream serverIn;
    private OutputStream serverOut;

    public Tunnel(SRSocket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            if(socket.handshake()){
                clientIn = socket.getSecureInputStream();
                clientOut = socket.getSecureOutputStream();

                if(clientIn.read() == 0x00){ //GUARD | RELAY
                    byte[] buffer = new byte[clientIn.readInt()];
                    clientIn.read(buffer);
                    String nodeId = new String(buffer);

                    server = new SRSocket(nodeId);
                    serverIn = server.getInputStream();
                    serverOut = server.getOutputStream();
                    if(!server.isClosed()){
                        clientOut.write(0x00);
                        relay();
                    }else{
                        clientOut.write(0x01);
                    }

                }else{ //END NODE

                    byte[] buffer = new byte[4096];
                    int length = clientIn.read(buffer);
                    System.out.println(new String(buffer, 0, length));

                }
            }

        }catch(Exception e){
            //e.printStackTrace();
        }finally{
            socket.quickClose();
            if(server != null){
                server.quickClose();
            }
        }
    }

    public void relay(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    while(!socket.isClosed() && !server.isClosed() && !socket.isInputShutdown() && !server.isOutputShutdown() && !isInterrupted()){
                        byte[] buffer = new byte[4096];
                        int length;

                        try{
                            length = clientIn.read(buffer);
                        }catch(InterruptedIOException e){
                            length = 0;
                        }catch(IOException e){
                            length = -1;
                        }catch(Exception e){
                            length = -1;
                        }

                        if(length < 0){
                            socket.shutdownInput();
                            server.shutdownOutput();
                            break;
                        }else if(length > 0){
                            try{
                                serverOut.write(buffer, 0, length);
                                serverOut.flush();
                            }catch(Exception e){
                            }
                        }
                    }
                }catch(Exception e){
                }
            }
        });

        thread.start();

        try{
            byte[] buffer = new byte[4096];
            int length;

            while(!socket.isClosed() && !server.isClosed() && !server.isInputShutdown() && !socket.isOutputShutdown() && !thread.isInterrupted()){
                try{
                    length = serverIn.read(buffer);
                }catch(InterruptedIOException e){
                    length = 0;
                }catch(IOException e){
                    length = -1;
                }catch(Exception e){
                    length = -1;
                }

                if(length < 0){
                    server.shutdownInput();
                    socket.shutdownOutput();
                    break;
                }else if(length > 0){
                    try{
                        clientOut.write(buffer, 0, length);
                        clientOut.flush();
                    }catch(Exception e){
                    }
                }
            }

            thread.interrupt();
        }catch(Exception e){
        }
    }
}
