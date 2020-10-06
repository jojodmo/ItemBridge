package com.jojodmo.itembridge;

public class ItemBridgeKey{

    private String namespace;
    private String item;

    public ItemBridgeKey(ItemBridge bridge, String item){
        this(bridge.defaultKey, item);
    }

    public ItemBridgeKey(String namespace, String item){
        if(namespace.contains(":")){throw new IllegalArgumentException("key cannot contain a colon. Tried to use the key '" + namespace + "'");}
        this.namespace = namespace;
        this.item = item;
    }

    public String getNamespace(){
        return namespace;
    }

    public String getItem(){
        return item;
    }

    @Override
    public String toString(){
        return namespace + ":" + item;
    }
}
