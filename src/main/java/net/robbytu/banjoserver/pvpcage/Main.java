package net.robbytu.banjoserver.pvpcage;

import com.sk89q.worldedit.bukkit.selections.Selection;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends JavaPlugin {
    static WorldEditPlugin WE;
    static Permission permission = null;

    @Override
    public void onEnable() {
        WE = this.getWorldEdit();

        if(WE == null) {
            getLogger().warning("You must have WorldEdit installed for this plugin to work.");
            getPluginLoader().disablePlugin(this);
        }

        if(!this.setupPermissions()) {
            getLogger().warning("You must have Vault and some Permissions plugin installed for this plugin to work.");
            getPluginLoader().disablePlugin(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Console may not interact with bs-pvpcage!");
            return true;
        }

        if(args.length > 0 && args[0].equalsIgnoreCase("create")) {
            if(!permission.has(sender, "pvpcage.create")) return this.failCommand(sender, cmd, "Insufficient permissions.");
            if(args.length == 1) return this.failCommand(sender, cmd, "Missing argument: You must specify a name for the new cage.");

            Selection sel = WE.getSelection((Player)sender);
            if(sel == null) return this.failCommand(sender, cmd, "Error: You must select a region using WorldEdit before creating a cage.");

            if(getConfig().contains("cage." + sel.getWorld().getName() + "." + args[1])) return this.failCommand(sender, cmd, "Invalid argument: This name is already in use by another cage.");

            this.getConfig().set("cage." + sel.getWorld().getName() + "." + args[1] + ".min.x", sel.getMinimumPoint().getX());
            this.getConfig().set("cage." + sel.getWorld().getName() + "." + args[1] + ".min.y", sel.getMinimumPoint().getY());
            this.getConfig().set("cage." + sel.getWorld().getName() + "." + args[1] + ".min.z", sel.getMinimumPoint().getZ());

            this.getConfig().set("cage." + sel.getWorld().getName() + "." + args[1] + ".max.x", sel.getMaximumPoint().getX());
            this.getConfig().set("cage." + sel.getWorld().getName() + "." + args[1] + ".max.y", sel.getMaximumPoint().getY());
            this.getConfig().set("cage." + sel.getWorld().getName() + "." + args[1] + ".max.z", sel.getMaximumPoint().getZ());

            this.saveConfig();

            sender.sendMessage(ChatColor.GREEN + "Succesfully created cage.");
            return true;
        }

        if(args.length > 0 && args[0].equalsIgnoreCase("remove")) {
            if(!permission.has(sender, "pvpcage.remove")) return this.failCommand(sender, cmd, "Insufficient permissions.");
            if(args.length == 1) return this.failCommand(sender, cmd, "Missing argument: You must specify the name of the cage to be removed.");
            if(!getConfig().contains("cage." + ((Player)sender).getWorld().getName() + "." + args[1])) return this.failCommand(sender, cmd, "Invalid argument: No such cage found.");

            this.getConfig().set("cage." + ((Player)sender).getWorld().getName() + "." + args[1], null);

            this.saveConfig();

            sender.sendMessage(ChatColor.GREEN + "Succesfully removed cage.");
            return true;
        }

        if(args.length > 0 && args[0].equalsIgnoreCase("require")) {
            if(!permission.has(sender, "pvpcage.require")) return this.failCommand(sender, cmd, "Insufficient permissions.");

            if(args.length == 1) return this.failCommand(sender, cmd, "Missing argument: You must specify wether to <add> or <remove> a requirement.");
            if(args.length == 2) return this.failCommand(sender, cmd, "Missing argument: You must specify the name of the cage to apply new settings to.");
            if(args.length == 3) return this.failCommand(sender, cmd, "Missing argument: You must specify an item to apply.");

            if(!args[1].equalsIgnoreCase("add") || !args[1].equalsIgnoreCase("remove")) return this.failCommand(sender, cmd, "Invalid argument: You must specify wether to <add> or <remove> a requirement.");
            if(!getConfig().contains("cage." + ((Player)sender).getWorld().getName() + "." + args[2])) return this.failCommand(sender, cmd, "Invalid argument: No such cage found.");

            Material mat;
            if(this.isInteger(args[2])) mat = Material.getMaterial(Integer.getInteger(args[2]));
            else mat = Material.getMaterial(args[2]);

            if(mat == null) return this.failCommand(sender, cmd, "Invalid argument: No such Material found (" + args[2] + ").");

            List<Integer> requirements = this.getConfig().getIntegerList("cage." + ((Player)sender).getWorld().getName() + "." + args[2] + ".requirements");
            if(requirements == null) requirements = new ArrayList<Integer>();

            if(args[1] == "add") if(!requirements.contains(mat.getId())) requirements.add(mat.getId());
            if(args[1] == "remove") if(requirements.contains(mat.getId())) requirements.remove(mat.getId());

            this.getConfig().set("cage." + ((Player)sender).getWorld().getName() + "." + args[2] + ".requirements", requirements);

            this.saveConfig();

            sender.sendMessage(ChatColor.GREEN + "Succesfully removed requirement.");
            return true;
        }

        return true;
    }

    private boolean failCommand(CommandSender sender, Command cmd, String error) {
        sender.sendMessage(ChatColor.RED + error);
        sender.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.ITALIC + cmd.getUsage());

        return true;
    }

    private WorldEditPlugin getWorldEdit() {
        if(this.getServer().getPluginManager().getPlugin("WorldEdit") != null) return (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
        return null;
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) permission = permissionProvider.getProvider();
        return (permission != null);
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }
}
