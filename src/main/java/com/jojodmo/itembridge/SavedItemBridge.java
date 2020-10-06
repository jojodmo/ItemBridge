package com.jojodmo.itembridge;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class SavedItemBridge implements ItemBridgeListener{

    private Plugin plugin;
    private ItemBridge bridge;
    SavedItemBridge(Plugin p){
        this.plugin = p;
        bridge = ItemBridge.init(p, "itembridge", "saved", "save", "saves");
        bridge.registerListener(this);
    }

    @Override
    public ItemStack fetchItemStack(String item){
        return get(item);
    }

    static void reload(){
        cached.clear();
    }

    private static Map<String, ItemStack> cached = new HashMap<>();

    static ItemStack get(String key){
        key = key.toLowerCase();
        if(cached.containsKey(key)){
            return cached.get(key);
        }

        File file = getFile("saves/" + key + ".yml");
        if(!file.exists()){
            cached.put(key, null);
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ItemStack stack = config.getItemStack("item");
        cached.put(key, stack);
        return stack;
    }

    static boolean put(String key, ItemStack item, UUID saver){
        File folder = getFile("saves");
        if(!folder.exists()){
            if(!folder.mkdirs()){
                return false;
            }
        }

        File file = getFile("saves/" + key.toLowerCase() + ".yml");
        if(!file.exists()){
            try{
                if(!file.createNewFile()){
                    return false;
                }
            }
            catch(Exception ex){ex.printStackTrace();}
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("key", key);
        config.set("ItemBridgeVersion", Main.that.getDescription().getVersion());
        config.set("saveTime", System.currentTimeMillis());
        config.set("saveMethod", "playerCommand");
        config.set("savedBy", saver.toString());

        config.set("item", item);
        config.set("version", 1);

        try{
            config.save(file);
        }
        catch(Exception ex){
            ex.printStackTrace();
            return false;
        }

        cached.put(key.toLowerCase(), item);
        return true;
    }

    private static File getFile(String fileName){
        return new File(Main.that.getDataFolder().getPath() + File.separator + fileName);
    }
}