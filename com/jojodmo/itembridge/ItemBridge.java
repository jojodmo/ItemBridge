package com.jojodmo.itembridge;

import com.sun.istack.internal.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import sun.jvm.hotspot.utilities.AssertionFailure;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class ItemBridge{

    private static Map<String, ItemBridge> instances = new HashMap<>();

    private Plugin plugin;
    private boolean valid = false;
    private List<ItemBridgeListener> listeners = new ArrayList<>();

    /**
     * Create a new instance of ItemBridge. Make sure to register listeners after using the {@code registerListener} functions
     * @param plugin the plugin creating the ItemBridge
     * @param keys the keys that can be used to access this plugin.
     */
    public ItemBridge(Plugin plugin, String... keys){
        this(plugin, Arrays.asList(keys));
    }

    public ItemBridge(Plugin plugin, Collection<String> keys){
        if(plugin == null){
            throw new IllegalArgumentException("The plugin can not be null!");
        }

        boolean oneAdded = false;
        this.plugin = plugin;
        if(keys == null || keys.isEmpty()){
            oneAdded = checkAndAddKey(plugin.getName());
        }
        else{
            for(String key : keys){
                oneAdded |= checkAndAddKey(key);
            }
        }

        valid = oneAdded;
    }

    private ItemBridge(Plugin plugin){this.plugin = plugin;}
    static ItemBridge init(Plugin plugin, String... keys){
        ItemBridge itemBridge = new ItemBridge(plugin);
        for(String key : keys){
            instances.put(key.toLowerCase(), itemBridge);
        }
        itemBridge.valid = true;
        return itemBridge;
    }

    private static final Pattern RESERVED_KEYS = Pattern.compile("^(minecraft|mc|mojang|itembridge|item|block|entity|mob|monster|potion|saved|save|saves|bukkit|spigot|paper|null|resource)$");
    private boolean checkAndAddKey(String key){
        key = key.toLowerCase();
        if(!key.matches("^[a-z0-9._-]+$")){
            Bukkit.getLogger().log(Level.WARNING, "[ItemBridge] Could not associate the key '" + key + "' with the plugin '" + this.plugin.getName() + "'. Reason: Keys can only contain letters, numbers, dots, dashes, and underscores");
            return false;
        }
        else if(key.isEmpty() || RESERVED_KEYS.matcher(key).matches()){
            Bukkit.getLogger().log(Level.WARNING, "[ItemBridge] Could not associate the key '" + key + "' with the plugin '" + this.plugin.getName() + "'. Reason: This key is reserved");
            return false;
        }
        else if(instances.containsKey(key)){
            Bukkit.getLogger().log(Level.WARNING, "[ItemBridge] Could not associate the key '" + key + "' with the plugin '" + this.plugin.getName() + "'. Reason: The key is already in use by the plugin '" + instances.get(key).plugin.getName() + "'");
            return false;
        }
        else{
            Bukkit.getLogger().log(Level.INFO, "[ItemBridge] Successfully attached the key '" + key + "' to the plugin " + this.plugin.getName());
            instances.put(key, this);
            return true;
        }
    }

    public void registerFunctionAsListener(Function<String, ItemStack> function){
        if(function == null){return;}
        registerListener(function::apply);
    }

    public void registerListeners(ItemBridgeListener... listeners){
        this.registerListeners(Arrays.asList(listeners));
    }

    public void registerListeners(Collection<? extends ItemBridgeListener> listeners){
        this.listeners.addAll(listeners);
    }

    public void registerListener(ItemBridgeListener listener){
        this.listeners.add(listener);
    }

    public void removeListener(ItemBridgeListener listener){
        this.listeners.remove(listener);
    }

    public void removeAllListeners(){
        this.listeners.clear();
    }

    public int getListenerCount(){
        return this.listeners.size();
    }

    public boolean hasListeners(){
        return !this.listeners.isEmpty();
    }

    public boolean isValid(){
        return this.valid;
    }


    // Utility functions

//    public static ItemBridge make(Plugin plugin, ItemBridgeListener listener, String... keys){
//        ItemBridge ib = new ItemBridge(plugin, keys);
//        ib.registerListener(listener);
//        return ib;
//    }

    /**
     * Fetch an ItemStack from the given plugin
     * @param plugin the plugin to fetch the ItemStack from
     * @param item the name of the item in the plugin
     * @return ItemStack given by the name {@code item} for the plugin {@code plugin}, or {@code null} if the item does not exist
     */
    public static ItemStack getItemStack(Plugin plugin, String item){
        return getItemStack(plugin, item, new HashMap<>());
    }

    public static ItemStack getItemStack(Plugin plugin, String item, Map<String, Object> parameters){
        return getItemStack(plugin.getName(), item, parameters);
    }

    /**
     * Fetch an ItemStack from the given NamespacedKey
     * @param key the NamespacedKey for the given item
     * @return ItemStack given by the NamespacedKey {@code key}, or {@code null} if the item does not exist
     */
    public static ItemStack getItemStack(NamespacedKey key){
        return getItemStack(key, new HashMap<>());
    }

    public static ItemStack getItemStack(NamespacedKey key, Map<String, Object> parameters){
        return getItemStack(key.getKey(), key.getNamespace(), parameters);
    }


    /**
     * Fetch an ItemStack for the given search key
     * @param key a String that represents where this item came from. If "minecraft" or {@code null} is passed, Minecraft materials will be used
     * @param item the name of the item
     * @return ItemStack given by the key {@code key} and item name {@code item}, or {@code null} if the item does not exist
     */
    public static ItemStack getItemStack(String key, String item){
        return getItemStack(key, item, new HashMap<>());
    }

    public static ItemStack getItemStack(String key, String item, Map<String, Object> parameters){
        if(key == null){
            return Main.getMinecraftBridge().fetchItemStack(item);
        }

        ItemBridge instance = instances.get(key.toLowerCase());
        if(instance == null){return null;}

        if(parameters == null){parameters = new HashMap<>();}
        for(ItemBridgeListener listener : instance.listeners){
            ItemStack is = listener.fetchItemStack(item, parameters);
            if(is != null){return is;}
        }
        return null;
    }

    /**
     * Fetch an ItemStack for the given String
     * @param item a String in the format PLUGIN:ITEM_NAME_HERE that represents the item from the given plugin
     * @return an ItemStack for the given String, or {@code null} if the item does not exist
     *         if PLUGIN is "minecraft", or not present (eg. "minecraft:DIAMOND_ORE" or "GRASS_BLOCK"), the
     *         default Minecraft material for the given name will be returned
     */
    public static ItemStack getItemStack(@Nullable String item){
        return getItemStack(item, new HashMap<>());
    }

    public static ItemStack getItemStack(@Nullable String item, Map<String, Object> parameters){
        if(item == null){return null;}

        String[] split = item.split(":", 2);
        if(split.length == 1){
            return getItemStack("minecraft", item, parameters);
        }
        return getItemStack(split[0], split[1], parameters);
    }

}
