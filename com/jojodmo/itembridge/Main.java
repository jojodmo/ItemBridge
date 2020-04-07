package com.jojodmo.itembridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{

    static Main that;
    private static MinecraftItemBridge minecraftBridge;
    private static SavedItemBridge savedBridge;
    private static boolean isEnabled = false;

    static MinecraftItemBridge getMinecraftBridge(){
        return minecraftBridge;
    }

    @Override
    public void onEnable(){
        that = this;
        minecraftBridge = new MinecraftItemBridge(this);
        savedBridge = new SavedItemBridge(this);
        isEnabled = true;
        Bukkit.getConsoleSender().sendMessage("[ItemBridge] ItemBridge v" + getDescription().getVersion() + " by jojodmo successfully enabled!");

    }

    @Override
    public void onDisable(){
        isEnabled = false;
        minecraftBridge = null;
        savedBridge = null;
        that = null;
        //mcBridge = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(cmd.getLabel().equalsIgnoreCase("itembridge")){
            if(args.length == 0 || args[0].toLowerCase().matches("^info|plugin|pl$")){
                return sendInfo(sender);
            }
            else if(args[0].toLowerCase().matches("^help|\\?$")){
                sendMessage(sender, "Usage: /" + label + " <get/give/save/plugin>");
                return true;
            }
            else if(args[0].equalsIgnoreCase("get")){
                if(!checkPermission(sender, "itembridge.get", "itembridge.give")){return true;}
                if(!checkPlayer(sender)){return true;}
                if(args.length < 2){return sendUsage(sender, label,"get <item> [amount]");}

                return giveItem(sender, (Player) sender, "you", args[1], args.length > 2 ? args[2] : "1");
            }
            else if(args[0].equalsIgnoreCase("give")){
                if(!checkPermission(sender, "itembridge.get")){return true;}
                if(args.length < 3){return sendUsage(sender, label,"give <player> <item> [amount]");}

                Player to = Bukkit.getPlayer(args[1]);
                if(to == null || !to.isOnline()){
                    sendMessage(sender, "Couldn't find the player " + args[1]);
                    return true;
                }
                return giveItem(sender, to, to.getDisplayName(), args[2], args.length > 3 ? args[3] : "1");
            }
            else if(args[0].equalsIgnoreCase("save")){
                if(!checkPermission(sender, "itembridge.save")){return true;}
                if(!checkPlayer(sender)){return true;}

                ItemStack item = ((Player) sender).getInventory().getItemInHand();
                if(args.length < 2 || item.getType() == Material.AIR){
                    sendMessage(sender, "Hold an item in your main hand and use /" + label + " save <name>");
                    return true;
                }

                String name = args[1].toLowerCase();
                if(!name.matches("^[a-zA-Z0-9_-]+$")){
                    sendMessage(sender, "Your item name can only contain letters, numbers, dashes, and underscores");
                }
                else if(name.startsWith("_")){
                    sendMessage(sender, "Your item name can not start with an underscore");
                }
                else{
                    if(SavedItemBridge.put(name, item, ((Player) sender).getUniqueId())){
                        sendMessage(sender, "Successfully saved the item in your hand to ItemBridge/saves/" + name.toLowerCase() + ".yml");

                    }
                    else{
                        sendMessage(sender, "An unexpected error occurred while trying to save your item. Does your server have permission to create files and directories?");
                    }
                }

                return true;
            }

            return sendInfo(sender);
        }
        return false;
    }

    private boolean giveItem(CommandSender from, Player to, String name, String item, String amountStr){
        ItemStack stack = ItemBridge.getItemStack(item);
        if(stack == null){
            String[] s = item.split(":", 2);
            String didYouMean = "";
            if(s.length > 0){
                String itemToTry = s.length == 2 ? s[1] : s[0];
                if(ItemBridge.getItemStack("saved", itemToTry) != null){
                    didYouMean = ". Did you mean " + ChatColor.GREEN + "saved:" + itemToTry + ChatColor.YELLOW  + "?";
                }
                else if(ItemBridge.getItemStack("minecraft", itemToTry) != null){
                    didYouMean = ". Did you mean " + ChatColor.GREEN + "minecraft:" + itemToTry + ChatColor.YELLOW  + "?";
                }
            }
            sendMessage(from, "Couldn't find the item " + ChatColor.RED + item + ChatColor.YELLOW + didYouMean);
        }
        else{
            Integer amount = 1;
            try{amount = Integer.parseInt(amountStr);}
            catch(Exception ignore){}

            amount = Math.max(1, amount);
            stack.setAmount(amount);
            sendMessage(from, "Giving " + name + " " + amount + " " + item);
            stack.setAmount(amount);
            to.getInventory().addItem(stack);
        }
        return true;
    }

    private boolean sendInfo(CommandSender s){
        sendMessage(s, "Running ItemBridge v" + getDescription().getVersion() + " by jojodmo");
        return true;
    }

    private static final String PREFIX = ChatColor.GOLD + "[ItemBridge] " + ChatColor.YELLOW;
    static void sendMessage(CommandSender s, String m){
        s.sendMessage(PREFIX + m);
    }

    static boolean checkPlayer(CommandSender s){
        if(!(s instanceof Player)){
            sendMessage(s, "Only players can run this command!");
            return false;
        }
        return true;
    }

    static boolean sendUsage(CommandSender sender, String label, String usage){
        sendMessage(sender, "Usage: /" + label + " " + usage);
        return true;
    }

    static boolean checkPermission(CommandSender s, String... perms){
        for(String p : perms){
            if(s.hasPermission(p)){
                return true;
            }
        }
        noPermission(s);
        return false;
    }

    static void noPermission(CommandSender s){
        sendMessage(s, "You don't have permission to use this command");
    }

}
