package com.darkender.plugins.bouncyenderpearls;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BEPCommand implements CommandExecutor
{
    private BouncyEnderpearls base;
    
    public BEPCommand(BouncyEnderpearls base)
    {
        this.base = base;
    }
    
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] strings)
    {
        base.reload();
        commandSender.sendMessage("Reloaded the config");
        return true;
    }
}
