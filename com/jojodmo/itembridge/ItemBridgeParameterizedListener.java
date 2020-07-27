//package com.jojodmo.itembridge;
//
//import org.bukkit.inventory.ItemStack;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public interface ItemBridgeParameterizedListener<T> extends ItemBridgeListener{
//
//    ItemStack fetchParameterized(String item, Map<String, T> parameters);
//
//    @Override
//    default ItemStack fetch(String item, Map<String, Object> parameters){
//        Map<String, T> used = new HashMap<>();
//        if(parameters != null){
//            for(Map.Entry<String, Object> e : parameters.entrySet()){
//                try{
//                    T val = (T) e.getValue();
//                    used.put(e.getKey(), val);
//                }
//                catch(Exception ignore){}
//            }
//        }
//        return fetchParameterized(item, used);
//    }
//
//    default ItemStack fetch(String item){
//        return fetch(item, null);
//    }
//
//}
