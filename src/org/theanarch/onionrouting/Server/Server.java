package org.theanarch.onionrouting.Server;

import org.theanarch.onionrouting.Network.SRServerSocket;
import org.theanarch.onionrouting.Network.SRSocket;

public class Server {

    public Server(int port){
        new Thread(new Runnable(){
            private SRSocket socket;

            @Override
            public void run(){
                try{
                    SRServerSocket serverSocket = new SRServerSocket(port);
                    System.out.println("Onion relay started on port "+port);

                    while((socket = serverSocket.accept()) != null){
                        (new Tunnel(socket)).start();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
