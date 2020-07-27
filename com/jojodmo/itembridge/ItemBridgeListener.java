package com.jojodmo.itembridge;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public interface ItemBridgeListener{

    /**
     * @return the priority of this listener (HIGHEST is checked first, and LOWEST is checked last)
     */
    default ItemBridgeListenerPriority getPriority(){
        return ItemBridgeListenerPriority.MEDIUM;
    }

    /**
     * Method to fetch an item in this plugin by the given name
     * @param item the name of the item in this plugin
     * @param parameters (optional)
     * @return the ItemStack from this plugin given by the name {@code item} and (optional) arguments {@code params},
     * or {@code null} if the item does not exist
     */
    @Nullable
    default ItemStack fetchItemStack(@NotNull String item, @NotNull Map<String, Object> parameters){
        return fetchItemStack(item);
    }

    @Nullable
    default ItemStack fetchItemStack(@NotNull String item, @NotNull Map<String, Object> parameters, int amount){
        ItemStack fetched = fetchItemStack(item, parameters);
        if(fetched != null){
            fetched.setAmount(amount);
        }
        return fetched;
    }

    @Nullable
    default ItemStack fetchItemStack(@NotNull String item, int amount){
        ItemStack fetched = fetchItemStack(item);
        if(fetched != null){
            fetched.setAmount(amount);
        }
        return fetched;
    }

    @Nullable
    default Map<String, Object> getParameters(@NotNull ItemStack stack){
        return null;
    }

    /**
     * Method to get the name of the given ItemStack
     * @param stack the ItemStack
     * @return the name of this ItemStack if it is from this plugin, or {@code null} if this ItemStack doesn't exist in this plugin
     */
    @Nullable
    default String getItemName(@NotNull ItemStack stack){
        return null;
    }

    default boolean isItem(@NotNull ItemStack stack, @NotNull String item, @NotNull Map<String, Object> parameters){
        if(isItem(stack, item)){
            Map<String, Object> current = getParameters(stack);
            if(current == null){return parameters.isEmpty();}

            for(Map.Entry<String, Object> param : parameters.entrySet()){
                if(!Objects.equals(param.getValue(), current.get(param.getKey()))){
                    return false;
                }
            }

            for(Map.Entry<String, Object> param : current.entrySet()){
                if(!Objects.equals(param.getValue(), parameters.get(param.getKey()))){
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Method to check whether or not an ItemStack is from this plugin and has the name {@code item}
     * @param stack the ItemStack
     * @param item the name of the item
     * @return {@code true} iff this plugin added {@code stack} AND the item name of {@code stack} is the same as the item name
     * given for {@code item}. False otherwise.
     */
    default boolean isItem(@NotNull ItemStack stack, @NotNull String item){
        return item.equals(getItemName(stack));
    }

    /**
     * Method to fetch an item in this plugin by the given name
     * @param item the name of the item in this plugin
     * @return the ItemStack from this plugin given by the name {@code item},
     * or {@code null} if the item does not exist
     */
    @Nullable
    ItemStack fetchItemStack(@NotNull String item);
}
