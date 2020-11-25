package com.jojodmo.itembridge;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface ItemBridgeListener{

    /**
     * @return the priority of this listener (HIGHEST is checked first, and LOWEST is checked last)
     */
    default ItemBridgeListenerPriority getPriority(){
        return ItemBridgeListenerPriority.MEDIUM;
    }

    /**
     * @return {@code} if the plugin is completely loaded. Your listener will still get calls, even if this returns false
     */
    default boolean isReady(){
        return true;
    }

    @NotNull
    default Collection<String> getAvailableItems(){
        return new ArrayList<>();
    }

    @NotNull
    default Collection<String> getAvailableBlocks(){
        return new ArrayList<>();
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
                if(!Objects.deepEquals(param.getValue(), current.get(param.getKey()))){
                    return false;
                }
            }

            for(Map.Entry<String, Object> param : current.entrySet()){
                if(!Objects.deepEquals(param.getValue(), parameters.get(param.getKey()))){
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
     * given for {@code item}. {@code false} otherwise.
     */
    default boolean isItem(@NotNull ItemStack stack, @NotNull String item){
        return item.equalsIgnoreCase(getItemName(stack));
    }

    /**
     * Method to fetch an item in this plugin by the given name
     * @param item the name of the item in this plugin
     * @return the ItemStack from this plugin given by the name {@code item},
     * or {@code null} if the item does not exist
     */
    @Nullable
    ItemStack fetchItemStack(@NotNull String item);

    /**
     * Method to set the block at the location location to the given id.
     * This method MUST NOT change the block in any way if it returns false.
     * @param location the location of the block
     * @param id the ID of the block. This will never be null. Listen for removeBlock to handle the removal of blocks
     * @return {@code true} if a block exists with the given id, and setting the block was successful. {@code false} otherwise.
     */
    default boolean setBlock(@NotNull Location location, @NotNull String id){
        return false;
    }

    default boolean setBlock(@NotNull Location location, @NotNull String id, @NotNull Map<String, Object> parameters){
        return setBlock(location, id);
    }

    /**
     * Method to remove the block at the given location.
     * This method MUST NOT change the block in any way if it returns false.
     * @param location the location of the block to be removed
     * @return {@code true} if the given block is known to be managed by this plugin, {@code false} otherwise. If it's
     * safe to remove blocks by just setting their type to air, it's ok to always return {@code false} here. However,
     * don't return {@code true} unless you're sure that the block removal is handled by your plugin
     */
    default boolean removeBlock(@NotNull Location location){
        return false;
    }

    /**
     * Method to fetch the id of the block at the given location.
     * @param location the name of the item in this plugin
     * @return {@code null} if the block at this location was not set by this plugin, or the ID of the block otherwise.
     */
    @Nullable
    default String getBlock(@NotNull Location location){
        return null;
    }

    @Nullable
    default Map<String, Object> getBlockParameters(@NotNull Location location){
        return null;
    }

    /**
     * Method to check whether or not the block at {@code location} is from this plugin and it's ID is {@code id}
     * @param location the location of block
     * @param id the name of the block
     * @return {@code true} iff this plugin added the block at {@code location} AND the ID of the block is {@code id}.
     * {@code false} otherwise.
     */
    default boolean isBlock(@NotNull Location location, @NotNull String id){
        return id.equalsIgnoreCase(getBlock(location));
    }

    default boolean isBlock(@NotNull Location location, @NotNull String id, @NotNull Map<String, Object> parameters){
        if(isBlock(location, id)){
            Map<String, Object> current = getBlockParameters(location);
            if(current == null){return parameters.isEmpty();}

            for(Map.Entry<String, Object> param : parameters.entrySet()){
                if(!Objects.deepEquals(param.getValue(), current.get(param.getKey()))){
                    return false;
                }
            }

            for(Map.Entry<String, Object> param : current.entrySet()){
                if(!Objects.deepEquals(param.getValue(), parameters.get(param.getKey()))){
                    return false;
                }
            }
        }
        return false;
    }
}
