package com.darkender.plugins.bouncyenderpearls;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class BEPCommand implements CommandExecutor, TabCompleter
{
    private final BouncyEnderpearls base;
    
    public BEPCommand(BouncyEnderpearls base)
    {
        this.base = base;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        base.reload();
        sender.sendMessage("Reloaded the config");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        return new ArrayList<>();
    }
}
