package com.jojodmo.itembridge;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class ItemBridge{

    private static List<ItemBridge> instances = new ArrayList<>();
    private static Map<String, ItemBridge> instanceMap = new HashMap<>();

    Plugin plugin;
    String defaultKey;
    List<String> allKeys = new ArrayList<>();
    private boolean valid = false;
    private List<ItemBridgeListener> listeners = new ArrayList<>();

    private static Map<ItemBridgeListenerPriority, List<ItemBridgeListenerWrapper>> listenersByPriority = new HashMap<>();

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

        List<String> added = new ArrayList<>();
        this.plugin = plugin;
        if(keys == null || keys.isEmpty()){
            if(checkAndAddKey(plugin.getName())){
                added.add(plugin.getName());
            }
        }
        else{
            for(String key : keys){
                if(checkAndAddKey(key)){
                    added.add(key);
                }
            }
        }

        if(added.size() > 0){
            StringBuilder addedStr = new StringBuilder();
            String s = "s";
            if(added.size() == 1){
                s = "";
                addedStr.append("'" + added.get(0) + "'");
            }
            if(added.size() == 2){
                addedStr.append("'" + added.get(0) + "' and '" + added.get(1) + "'");
            }
            else{
                for(int i = 0; i < added.size() - 1; i++){
                    addedStr.append("'" + added.get(i) + "', ");
                }
                addedStr.append("and '" + added.get(added.size() - 1) + "'");
            }
            Bukkit.getLogger().log(Level.INFO,"[ItemBridge] Successfully attached the key" + s + " " + addedStr.toString() + " to the plugin " + this.plugin.getName());
        }

        valid = added.size() > 0;
        if(valid){
            this.defaultKey = added.get(0);
            instances.add(this);
            allKeys = added;
        }
    }

    private ItemBridge(Plugin plugin){this.plugin = plugin;}
    static ItemBridge init(Plugin plugin, String... keys){
        ItemBridge itemBridge = new ItemBridge(plugin);
        for(String key : keys){
            instanceMap.put(key.toLowerCase(), itemBridge);
        }
        itemBridge.valid = true;
        itemBridge.defaultKey = keys[0].toLowerCase();
        instances.add(itemBridge);
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
        else if(instanceMap.containsKey(key)){
            Bukkit.getLogger().log(Level.WARNING, "[ItemBridge] Could not associate the key '" + key + "' with the plugin '" + this.plugin.getName() + "'. Reason: The key is already in use by the plugin '" + instanceMap.get(key).plugin.getName() + "'");
            return false;
        }
        else{
            //Bukkit.getLogger().log(Level.INFO, "[ItemBridge] Successfully attached the key '" + key + "' to the plugin " + this.plugin.getName());
            instanceMap.put(key, this);
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
        for(ItemBridgeListener listener : listeners){
            registerListener(listener);
        }
    }

    public void registerListener(ItemBridgeListener listener){
        ItemBridgeListenerPriority p = listener.getPriority();
        List<ItemBridgeListenerWrapper> list = listenersByPriority.getOrDefault(p, new ArrayList<>());
        list.add(new ItemBridgeListenerWrapper(listener, this));
        listenersByPriority.put(p, list);

        this.listeners.add(listener);
    }

    public void removeListener(ItemBridgeListener listener){
        ItemBridgeListenerPriority p = listener.getPriority();
        List<ItemBridgeListenerWrapper> list = listenersByPriority.getOrDefault(p, new ArrayList<>());
        list.remove(new ItemBridgeListenerWrapper(listener, this));
        listenersByPriority.put(p, list);

        this.listeners.remove(listener);
    }

    public void removeAllListeners(){
        for(ItemBridgeListener listener : this.listeners){
            ItemBridgeListenerPriority p = listener.getPriority();
            List<ItemBridgeListenerWrapper> list = listenersByPriority.getOrDefault(p, new ArrayList<>());
            list.remove(new ItemBridgeListenerWrapper(listener, this));
            listenersByPriority.put(p, list);
        }

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

    /**
     * Fetch an ItemStack from the given plugin
     * @param plugin the plugin to fetch the ItemStack from
     * @param item the name of the item in the plugin
     * @param parameters the parameters for the item (optional)
     * @return ItemStack given by the name {@code item} for the plugin {@code plugin}, or {@code null} if the item does not exist
     */
    public static ItemStack getItemStack(Plugin plugin, String item, @NotNull Map<String, Object> parameters){
        return getItemStack(plugin.getName(), item, parameters);
    }

    /**
     * Fetch an ItemStack from the given NamespacedKey
     * @param key the NamespacedKey for the given item
     * @return ItemStack given by the NamespacedKey {@code key}, or {@code null} if the item does not exist
     * @deprecated Will be removed in an upcoming update. Use getItemStack(ItemBridgeKey) instead.
     */
    @Deprecated
    public static ItemStack getItemStack(NamespacedKey key){
        return getItemStack(key, new HashMap<>());
    }

    /**
     * Fetch an ItemStack from the given plugin
     * @param key the NamespacedKey for the given item
     * @param parameters the parameters for the item (optional)
     * @return ItemStack given by the name {@code item} for the plugin {@code plugin}, or {@code null} if the item does not exist
     * @deprecated Will be removed in an upcoming update. Use getItemStack(ItemBridgeKey) instead.
     */
    @Deprecated
    public static ItemStack getItemStack(NamespacedKey key, @NotNull Map<String, Object> parameters){
        return getItemStack(key.getNamespace(), key.getKey(), parameters);
    }

    /**
     * Fetch an ItemStack from the given plugin
     * @param key the ItemBridgeKey for the given item
     * @return ItemStack given by the name {@code item} for the plugin {@code plugin}, or {@code null} if the item does not exist
     */
    public static ItemStack getItemStack(ItemBridgeKey key){
        return getItemStack(key, new HashMap<>());
    }

    /**
     * Fetch an ItemStack from the given plugin
     * @param key the ItemBridgeKey for the given item
     * @param parameters the parameters for the item (optional)
     * @return ItemStack given by the name {@code item} for the plugin {@code plugin}, or {@code null} if the item does not exist
     */
    public static ItemStack getItemStack(ItemBridgeKey key, @NotNull Map<String, Object> parameters){
        return getItemStack(key.getNamespace(), key.getItem(), parameters);
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

    /**
     * Fetch an ItemStack for the given search key
     * @param key a String that represents where this item came from. If "minecraft" or {@code null} is passed, Minecraft materials will be used
     * @param item the name of the item
     * @param parameters the parameters for the item (optional)
     * @return ItemStack given by the key {@code key} and item name {@code item}, or {@code null} if the item does not exist
     */
    public static ItemStack getItemStack(String key, String item, @NotNull Map<String, Object> parameters){
        if(key == null){
            return Main.getMinecraftBridge().fetchItemStack(item);
        }

        ItemBridge instance = instanceMap.get(key.toLowerCase());
        if(instance == null){return null;}

        if(parameters == null){parameters = new HashMap<>();}
        for(ItemBridgeListener listener : instance.listeners){
            ItemStack is = listener.fetchItemStack(item, parameters);
            if(is != null){return is;}
        }
        return null;
    }

    /**
     * Fetch all valid keys
     * @return List<String> of all the valid keys
     */
    public static List<String> getValidKeys() {
        ArrayList<String> list = new ArrayList<>();
        for (String key : SavedItemBridge.cachedKeys()) {
            list.add("saved:" + key);
        }
        return list;
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

    /**
     * Fetch an ItemStack for the given String
     * @param item a String in the format PLUGIN:ITEM_NAME_HERE that represents the item from the given plugin
     * @param parameters the parameters for the item (optional)
     * @return an ItemStack for the given String, or {@code null} if the item does not exist
     *         if PLUGIN is "minecraft", or not present (eg. "minecraft:DIAMOND_ORE" or "GRASS_BLOCK"), the
     *         default Minecraft material for the given name will be returned
     */
    public static ItemStack getItemStack(@Nullable String item, Map<String, Object> parameters){
        if(item == null){return null;}

        String[] split = item.split(":", 2);
        if(split.length == 1){
            return getItemStack("minecraft", item, parameters);
        }
        return getItemStack(split[0], split[1], parameters);
    }

    /**
     * Determine whether or not an ItemStack is the same as the one for the given name
     * @param stack the ItemStack
     * @param key the plugin name
     * @param item the item name
     * @param params the parameters for the item (optional)
     * @return true iff stack is the item given by key:item (this should mean that item is like getItemStack(key, item, params)
     */
    public static boolean isItemStack(ItemStack stack, String key, String item, Map<String, Object> params){
        if(key == null){
            return Main.getMinecraftBridge().isItem(stack, item);
        }

        ItemBridge instance = instanceMap.get(key.toLowerCase());
        if(instance == null){return false;}

        for(ItemBridgeListener listener : instance.listeners){
            if(listener.isItem(stack, item, params)){
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether or not an ItemStack is the same as the one for the given name
     * @param stack the ItemStack
     * @param key the plugin name
     * @param item the item name
     * @return true iff stack is the item given by key:item (this should mean that item is like getItemStack(key, item)
     */
    public static boolean isItemStack(ItemStack stack, String key, String item){
        if(key == null){
            return Main.getMinecraftBridge().isItem(stack, item);
        }

        ItemBridge instance = instanceMap.get(key.toLowerCase());
        if(instance == null){return false;}

        for(ItemBridgeListener listener : instance.listeners){
            if(listener.isItem(stack, item)){
                return true;
            }
        }
        return false;
    }

    /**
     * Get the key for the given ItemStack
     * @param stack the ItemStack
     * @return an ItemBridgeKey for the given item. If no item is found for the given stack, this will return minecraft:TYPE,
     * where type is stack.getType()
     */
    @Nullable
    public static ItemBridgeKey getItemKey(@Nullable ItemStack stack){
        if(stack == null){return null;}

        for(ItemBridgeListenerPriority p : ItemBridgeListenerPriority.values()){
            ItemBridgeKey key = getItemKey(p, stack);
            if(key != null){return key;}
        }
        return null;
    }

    private static ItemBridgeKey getItemKey(ItemBridgeListenerPriority p, ItemStack stack){
        List<ItemBridgeListenerWrapper> list = listenersByPriority.get(p);
        if(list != null){
            for(ItemBridgeListenerWrapper l : list){
                String s = l.listener.getItemName(stack);
                if(s != null){
                    return new ItemBridgeKey(l.bridge, s);
                }
            }
        }
        return null;
    }

    /**
     * Get the ItemBridge parameters for the given ItemStack
     * @param stack the ItemStack
     * @return a Map of the parameters for the given item. If no item is found for the given stack, this will return null.
     */
    @Nullable
    public static Map<String, Object> getItemParameters(@Nullable ItemStack stack){
        if(stack == null){return null;}
        for(ItemBridgeListenerPriority p : ItemBridgeListenerPriority.values()){
            Map<String, Object> params = getItemParameters(p, stack);
            if(params != null){return params;}
        }
        return null;
    }

    private static Map<String, Object> getItemParameters(ItemBridgeListenerPriority p, ItemStack stack){
        List<ItemBridgeListenerWrapper> list = listenersByPriority.get(p);
        if(list != null){
            for(ItemBridgeListenerWrapper l : list){
                Map<String, Object> m = l.listener.getParameters(stack);
                if(m != null){return m;}
            }
        }
        return null;
    }

    /**
     * Get the key for the block at the given location
     * @param location the location of the block
     * @return an ItemBridgeKey for the given item. If the given location is a vanilla Minecraft block, this will return minecraft:TYPE,
     * where type is location.getBlock().getType().name()
     */
    @NotNull
    public static ItemBridgeKey getBlock(@NotNull Location location){
        for(ItemBridgeListenerPriority p : ItemBridgeListenerPriority.values()){
            List<ItemBridgeListenerWrapper> list = listenersByPriority.get(p);
            if(list != null){
                for(ItemBridgeListenerWrapper l : list){
                    String s = l.listener.getBlock(location);
                    if(s != null){
                        return new ItemBridgeKey(l.bridge, s);
                    }
                }
            }
        }
        return new ItemBridgeKey("minecraft", location.getBlock().getType().name());
    }

    public static boolean isBlock(@NotNull Location location, @NotNull String id){
        for(ItemBridgeListenerPriority p : ItemBridgeListenerPriority.values()){
            List<ItemBridgeListenerWrapper> list = listenersByPriority.get(p);
            if(list != null){
                for(ItemBridgeListenerWrapper l : list){
                    if(l.listener.isBlock(location, id)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isBlock(@NotNull Location location, @NotNull String id, @NotNull Map<String, Object> parameters){
        for(ItemBridgeListenerPriority p : ItemBridgeListenerPriority.values()){
            List<ItemBridgeListenerWrapper> list = listenersByPriority.get(p);
            if(list != null){
                for(ItemBridgeListenerWrapper l : list){
                    if(l.listener.isBlock(location, id, parameters)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get the ItemBridge parameters for the block at the given location
     * @param location the location of the block
     * @return a Map of the parameters for the block at the given location. If the block is a vanilla block, or
     * the block doesn't have any parameters, this will return {@code null}
     */
    @Nullable
    public static Map<String, Object> getBlockParameters(@NotNull Location location){
        for(ItemBridgeListenerPriority p : ItemBridgeListenerPriority.values()){
            List<ItemBridgeListenerWrapper> list = listenersByPriority.get(p);
            if(list != null){
                for(ItemBridgeListenerWrapper l : list){
                    Map<String, Object> params = l.listener.getBlockParameters(location);
                    if(params != null){
                        return params;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Remove the block at the given location
     * @param location the location of the block
     * @return {@code true} if the block was successfully removed, or {@code false} if not. If this function
     * returns {@code false}, you should fall back to your own code for removing the block, likely location.getBlock().setType(Material.AIR)
     */
    public static boolean removeBlock(@NotNull Location location){
        for(ItemBridgeListenerPriority p : ItemBridgeListenerPriority.values()){
            List<ItemBridgeListenerWrapper> list = listenersByPriority.get(p);
            if(list != null){
                for(ItemBridgeListenerWrapper l : list){
                    if(l.listener.removeBlock(location)){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean setBlock(@NotNull Location location, @NotNull String key){
        return setBlock(location, key, new HashMap<>());
    }

    public static boolean setBlock(@NotNull Location location, @NotNull String id, @Nullable Map<String, Object> parameters){
        String[] split = id.split(":", 2);
        if(split.length == 1){
            return setBlock(location, "minecraft", id, parameters);
        }
        return setBlock(location, split[0], split[1], parameters);
    }

    public static boolean setBlock(@NotNull Location location, @NotNull ItemBridgeKey key){
        return setBlock(location, key, null);
    }

    public static boolean setBlock(@NotNull Location location, @NotNull ItemBridgeKey key, @Nullable Map<String, Object> parameters){
        return setBlock(location, key.getNamespace(), key.getItem(), parameters);
    }

    public static boolean setBlock(@NotNull Location location, @NotNull Plugin plugin, @NotNull String id){
        return setBlock(location, plugin, id, null);
    }

    public static boolean setBlock(@NotNull Location location, @NotNull Plugin plugin, @NotNull String id, @Nullable Map<String, Object> parameters){
        return setBlock(location, plugin.getName(), id, parameters);
    }

    public static boolean setBlock(@NotNull Location location, @NotNull String key, @NotNull String id){
        return setBlock(location, key, id, null);
    }

    public static boolean setBlock(@NotNull Location location, @NotNull String key, @NotNull String id, @Nullable Map<String, Object> parameters){
        ItemBridge instance = instanceMap.get(key.toLowerCase());
        if(instance == null){return false;}

        if(parameters == null){parameters = new HashMap<>();}
        for(ItemBridgeListener listener : instance.listeners){
            if(listener.setBlock(location, id, parameters)){
                return true;
            }
        }
        return false;
    }

    public Map<String, List<String>> getEnabledListeners(){
        Map<String, List<String>> result = new HashMap<>();
        for(ItemBridge ib : instances){
            result.put(ib.defaultKey, Collections.unmodifiableList(ib.allKeys));
        }

        return result;
    }

    @Nullable
    public List<String> getAvailableItems(@NotNull String plugin){
        ItemBridge instance = instanceMap.get(plugin.toLowerCase());
        if(instance == null){return null;}

        List<String> available = new ArrayList<>();
        for(ItemBridgeListener listener : instance.listeners){
            available.addAll(listener.getAvailableItems());
        }
        return available;
    }

    @Nullable
    public List<String> getAvailableBlocks(@NotNull String plugin){
        ItemBridge instance = instanceMap.get(plugin.toLowerCase());
        if(instance == null){return null;}

        List<String> available = new ArrayList<>();
        for(ItemBridgeListener listener : instance.listeners){
            available.addAll(listener.getAvailableBlocks());
        }
        return available;
    }


    private static class ItemBridgeListenerWrapper{
        private ItemBridgeListener listener;
        private ItemBridge bridge;

        public ItemBridgeListenerWrapper(ItemBridgeListener listener, ItemBridge bridge){
            this.listener = listener;
            this.bridge = bridge;
        }

        public ItemBridgeListener getListener(){
            return listener;
        }

        public ItemBridge getBridge(){
            return bridge;
        }

        @Override
        public boolean equals(Object o){
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            ItemBridgeListenerWrapper that = (ItemBridgeListenerWrapper) o;
            return Objects.equals(listener, that.listener) &&
                    Objects.equals(bridge, that.bridge);
        }

        @Override
        public int hashCode(){
            return Objects.hash(listener, bridge);
        }
    }
}
