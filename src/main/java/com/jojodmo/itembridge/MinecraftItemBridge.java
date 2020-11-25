package com.jojodmo.itembridge;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MinecraftItemBridge implements ItemBridgeListener{

    /**
     * NOTE: this instance was created using the ItemBridge.init method, which isn't available to you.
     * That's used so that reserved keys could be assigned.
     *
     * If you wanted to create this same thing, you should use
     * bridge = new ItemBridge(p, "minecraft", "mc");
     */
    private ItemBridge bridge;
    MinecraftItemBridge(Plugin p){
        bridge = ItemBridge.init(p, "minecraft", "mc");
        bridge.registerListener(this);
    }

    @Override
    public ItemBridgeListenerPriority getPriority(){
        return ItemBridgeListenerPriority.LOWEST;
    }

    @Override
    public ItemStack fetchItemStack(String item){
        Material m = Material.matchMaterial(item);
        return m == null ? null : new ItemStack(m);
    }

    @Override
    public String getItemName(ItemStack stack){
        return stack == null ? null : stack.getType().name();
    }

    @Override
    public boolean isItem(ItemStack stack, String name){
        return stack.getType().name().equalsIgnoreCase(name);
    }

    @Override
    public boolean setBlock(Location location, String id){
        Material m = Material.matchMaterial(id);
        if(m == null){return false;}
        location.getBlock().setType(m);
        return true;
    }

    @Override
    public String getBlock(Location location){
        return location.getBlock().getType().name();
    }

    @Override
    public boolean isBlock(Location location, String id){
        return location.getBlock().getType().name().equalsIgnoreCase(id);
    }

    private static List<String> allItemIDs = null;
    private static List<String> allBlockIDs = null;

    @Override
    public @NotNull List<String> getAvailableItems(){
        if(allItemIDs != null){return allItemIDs;}
        List<String> ids = new ArrayList<>();
        for(Material m : Material.values()){
            ids.add(m.name());
        }
        allItemIDs = Collections.unmodifiableList(ids);
        return allItemIDs;
    }

    @Override
    public @NotNull List<String> getAvailableBlocks(){
        if(allBlockIDs != null){return allBlockIDs;}
        List<String> ids = new ArrayList<>();
        for(Material m : Material.values()){
            if(m.isBlock()){
                ids.add(m.name());
            }
        }
        allBlockIDs = Collections.unmodifiableList(ids);
        return allBlockIDs;
    }
}
