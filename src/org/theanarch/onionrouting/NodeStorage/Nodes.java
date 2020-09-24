package org.theanarch.onionrouting.NodeStorage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@XmlRootElement(name = "nodes")
public class Nodes {

    private HashMap<String, Node> nodes = new HashMap<>();

    public Nodes openList()throws Exception {
        JAXBContext context = JAXBContext.newInstance(Nodes.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (Nodes) unmarshaller.unmarshal(new File(getClass().getResource("/nodes.xml").getFile()));
    }

    @XmlElement(name = "node")
    public void setNode(Node node){
        nodes.put(node.getId(), node);
    }

    public Node getNode(String id){
        return nodes.get(id);
    }

    public Node getRandomNode(){
        Set<String> keySet = nodes.keySet();
        ArrayList<String> keyList = new ArrayList<>(keySet);

        int random = (int) (Math.random()*nodes.size());
        return nodes.get(keyList.get(random));
    }
}
