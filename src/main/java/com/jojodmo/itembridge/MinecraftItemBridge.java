package com.jojodmo.itembridge;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

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
}
