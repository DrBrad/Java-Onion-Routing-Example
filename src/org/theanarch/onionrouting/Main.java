package org.theanarch.onionrouting;

import org.theanarch.onionrouting.Network.SRInputStream;
import org.theanarch.onionrouting.Network.SROutputStream;
import org.theanarch.onionrouting.Network.SRSocket;
import org.theanarch.onionrouting.NodeStorage.Nodes;
import org.theanarch.onionrouting.Server.Server;

import java.util.prefs.Preferences;

import static org.theanarch.onionrouting.Network.Circuits.*;

public class Main {

    public static void main(String[] args){
        new Server(8008);

        //THIS IS TO SET THE PRIVATE KEY...
        //savePreference("key", "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC58dBYh2oBpvByf0GAti7+1ZxXHtY8JiikHyPWyq9ToVBVhT9F0C9qFO55aqZomyUatkv+nk/DFmfoCRe1T8u24MEIeqX2EfsB5a1+GhoQPgTl3+oYIe9XUsi46XKzDy2RW80wD/IRxgj0FgivnVTWF21qhGQi1xxz5/jpJkwD9+e4MnEdq5J7L9XiLWpltR6viRJHNZhV6i+ofPaguwD/u4VM9KH7x2fH/yjJSTMQoCO3OjftLq9QJIFOPZ7Kro6i8qjreiEoagamHzaRPMkQXL4hDS6O0d2h3+kdlImtZm1TEnIudVDydKMwlhYGrTxuXONeNymeXRSfey0gnlN5AgMBAAECggEALxb7MYBRq0twtz5dA1CgBC9qoXGQxbjQMAgm1l2MGzzOEGpnRLHdYBse7abBr9oigmpKQpgrEPfappuu4l7r80tl91TANNoG3EsjLb1EfnqGWQTkzunBKDcx2Ape3tPoMVizVQmkvzWMzOvLlVXdxz41mnUeuugJL50+nyTwuUwfSJy8JWjj0ORqh0L9njls6X3oEEKpJpLm2HccvprPsd4mo7D7YdfTN9Kkoq6/JlrSOBPyHGPvHBg3swGi6h9iRCM5Fa6+82RmgaszdFA5sdkEb2UuVdJmdLo1h0KX2lGqhdvtl/T/oHhaD67h9VgK1dsMwhD3+KPKKYwOcaChdQKBgQDj9e9COFhIl4Qwqbok24A+KWdDGVlBKGrNgyA6Wm3AsFfFRFQj83bJSJk4syazz7wsZpd6aVQPqWgAkn81lpqZiBPOsQn29ml2x+TZDzj2BCe3ZJI9XBO6z9hzpt/R7wxCQs7qmdoSZqL8DRY4YtATrPd7KAmWGogQVP42zWWucwKBgQDQ0N5+9ka6yNAaNGar9H71bOaCUQB9Oe3M/FGA0VBgtNquMKTTUS1Lz/5DS+YJJY8LpWAOsFpYkyxNeXG4wEwYtkesexG96dNeraksO/JNVPAe7k6lsgOedgb9/nB+WbcGp1btNtEFPK0ijKIw1hIP+EspG7qxtOqdNDoso7hvYwKBgQCL+AXgcxhB+kaAWragyuYDRQjSek4DY/2wEkNHkR6yoZRC26GOMX9ON/VciyPIZFT2Lkf0MZgbxbjGIS1aig6DFrqSvsGPkd1PwY0SefZPmF2KjYX9hvvWSKhu2pezH0Q3qy3BfpYXlk2c1aw/Eu0QJ7QNOpgoQkIpRQaSP6ufYQKBgB+oixMUo4SODX7rkuVfHCB5cjkvrrNNpQ/+8Rxmn5kJ0l22Ykbnd8aWsiXhy+53E0jgub+AXTIwnfbhAqzP+rmEaTltLzNrz+/6xDtAm2u+BOmrl6DnbEUJ/+ViPgvp7iXEGRm9xdLVcCpcx/o/el2Rd1O0MIngs/FBidBawguhAoGAQaf+gsE8O+gN33/7CeJjftXCJuMRUiQuW2sv8UjPAe0OE+p+/3P+QJvF5avqTpF/14HMXufMdGDNi1XhxO7LDC/Q5SHlS9mfp02mwZoJFBbk0qqJt7+ZtEs7jqykpL4LDV0XnhCXLJfcEhmiAkJfUiRb7OcnsXyWoLMtHv2ZcOo=");

        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    Thread.sleep(1000);
                    onionRelayClientConnect("HELLO WORLD");


                    Thread.sleep(1000);
                    onionRelayClientConnect("WHATS UP MY GUY");
                }catch(Exception e){
                }
            }
        }).start();
    }

    public static void onionRelayClientConnect(String message)throws Exception {
        String[] circuit = getRandomCircuit();
        SRSocket socket = null;
        try{
            socket = new SRSocket(circuit[0]);
            if(socket.handshake()){ //GUARD
                System.out.println("[ GUARD ] HANDSHAKE APPROVED");

                SRInputStream in = socket.getSecureInputStream();
                SROutputStream out = socket.getSecureOutputStream();

                out.write(0x00);
                out.writeInt(circuit[1].getBytes().length);
                out.write(circuit[1].getBytes());

                if(in.read() == 0x00){ //RELAY
                    Nodes nodes = new Nodes().openList();
                    if(socket.clientHandshake(nodes.getNode(circuit[1]))){
                        System.out.println("[ RELAY ] HANDSHAKE APPROVED");

                        in = socket.getSecureInputStream();
                        out = socket.getSecureOutputStream();

                        out.write(0x00);
                        out.writeInt(circuit[2].getBytes().length);
                        out.write(circuit[2].getBytes());

                        if(in.read() == 0x00){ //END
                            nodes = new Nodes().openList();
                            if(socket.clientHandshake(nodes.getNode(circuit[1]))){
                                System.out.println("[ END ]   HANDSHAKE APPROVED");

                                in = socket.getSecureInputStream();
                                out = socket.getSecureOutputStream();
                                out.write(0x01);

                                out.write(message.getBytes());
                            }
                        }else{
                            removeCircuit(circuit[0]+circuit[1]+circuit[2]);
                        }
                    }
                }else{
                    removeCircuit(circuit[0]+circuit[1]+circuit[2]);
                }
            }

        }catch(Exception e){
            removeCircuit(circuit[0]+circuit[1]+circuit[2]);
        }finally{
            socket.quickClose();
        }
    }

    public static void savePreference(String key, String value){
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.put(key, value);
    }

    public static String readPreference(String key){
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(key, "");
    }
}
