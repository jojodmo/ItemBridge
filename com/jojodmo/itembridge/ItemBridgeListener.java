package com.jojodmo.itembridge;

import com.sun.istack.internal.NotNull;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface ItemBridgeListener{

    /**
     * Method to fetch an item in this plugin by the given name
     * @param item the name of the item in this plugin
     * @param parameters (optional)
     * @return the ItemStack from this plugin given by the name {@code item} and (optional) arguments {@code params},
     * or {@code null} if the item does not exist
     */
    default ItemStack fetchItemStack(@NotNull String item, @NotNull Map<String, Object> parameters){
        return fetchItemStack(item);
    }

    default ItemStack fetchItemStack(@NotNull String item, @NotNull Map<String, Object> parameters, int amount){
        ItemStack fetched = fetchItemStack(item, parameters);
        if(fetched != null){
            fetched.setAmount(amount);
        }
        return fetched;
    }

    default ItemStack fetchItemStack(@NotNull String item, int amount){
        ItemStack fetched = fetchItemStack(item);
        if(fetched != null){
            fetched.setAmount(amount);
        }
        return fetched;
    }

    /**
     * Method to fetch an item in this plugin by the given name
     * @param item the name of the item in this plugin
     * @return the ItemStack from this plugin given by the name {@code item},
     * or {@code null} if the item does not exist
     */
    ItemStack fetchItemStack(@NotNull String item);
}
