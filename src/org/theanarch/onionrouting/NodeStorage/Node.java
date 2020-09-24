package org.theanarch.onionrouting.NodeStorage;

import javax.xml.bind.annotation.XmlAttribute;

public class Node {

    @XmlAttribute(name="address")
    private String address;

    @XmlAttribute(name="code")
    private String code;

    @XmlAttribute(name="id")
    private String id;

    @XmlAttribute(name="key")
    private String key;

    @XmlAttribute(name="port")
    private int port;

    public String getAddress(){
        return address;
    }

    public String getCode(){
        return code;
    }

    public String getId(){
        return id;
    }

    public String getKey(){
        return key;
    }

    public int getPort(){
        return port;
    }
}
