package org.theanarch.onionrouting.Network;

import org.theanarch.onionrouting.NodeStorage.Nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Circuits {

    private static HashMap<String, String[]> circuits = new HashMap<>();

    private static void createCircuits()throws Exception {
        Nodes nodes = new Nodes().openList();
        for(int i = 0; i < 5; i++){
            String[] circuit = new String[3];
            circuit[0] = nodes.getRandomNode().getId();
            circuit[1] = nodes.getRandomNode().getId();
            circuit[2] = nodes.getRandomNode().getId();

            String id = circuit[0]+circuit[1]+circuit[2];
            if(!circuits.containsKey(id)){
                circuits.put(id, circuit);
            }
        }
    }

    private static void createCircuit()throws Exception {
        Nodes nodes = new Nodes().openList();
        String[] circuit = new String[3];
        circuit[0] = nodes.getRandomNode().getId();
        circuit[1] = nodes.getRandomNode().getId();
        circuit[2] = nodes.getRandomNode().getId();

        String id = circuit[0]+circuit[1]+circuit[2];
        if(!circuits.containsKey(id)){
            circuits.put(id, circuit);
        }
    }

    public static String[] getRandomCircuit()throws Exception {
        if(circuits.size() < 1){
            createCircuits();
        }
        Set<String> keySet = circuits.keySet();
        List<String> keyList = new ArrayList<>(keySet);

        int random = (int) (Math.random()*circuits.size());
        return circuits.get(keyList.get(random));
    }

    public static void removeCircuit(String id)throws Exception {
        if(circuits.containsKey(id)){
            circuits.remove(id);
            createCircuit();
        }
    }
}
