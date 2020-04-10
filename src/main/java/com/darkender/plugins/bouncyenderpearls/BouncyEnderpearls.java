package com.darkender.plugins.bouncyenderpearls;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BouncyEnderpearls extends JavaPlugin implements Listener
{
    private NamespacedKey bounces;
    private List<UUID> allowed = new ArrayList<UUID>();
    
    private double bounciness = 0.8;
    private int maxBounces = 5;
    
    @Override
    public void onEnable()
    {
        bounces = new NamespacedKey(this, "bounces");
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("bouncyenderpearls").setExecutor(new BEPCommand(this));
        reload();
    }
    
    void reload()
    {
        saveDefaultConfig();
        reloadConfig();
        bounciness = getConfig().getDouble("bounciness");
        maxBounces = getConfig().getInt("max-bounces");
    }
    
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event)
    {
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
        {
            if(allowed.contains(event.getPlayer().getUniqueId()))
            {
                allowed.remove(event.getPlayer().getUniqueId());
            }
            else
            {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event)
    {
        if(event.getEntityType() == EntityType.ENDER_PEARL && event.getEntity().getShooter() instanceof Player)
        {
            Player p = (Player) event.getEntity().getShooter();
            if(!p.hasPermission("bouncyenderpearls.enabled"))
            {
                allowed.add(p.getUniqueId());
                event.getEntity().getPersistentDataContainer().set(bounces, PersistentDataType.INTEGER, maxBounces + 1);
            }
        }
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event)
    {
        if(event.getEntityType() == EntityType.ENDER_PEARL)
        {
            EnderPearl old = (EnderPearl) event.getEntity();
            if(event.getHitEntity() != null)
            {
                return;
            }
            
            PersistentDataContainer data = old.getPersistentDataContainer();
            int bounceCount = 1;
            if(data.has(bounces, PersistentDataType.INTEGER))
            {
                bounceCount = data.get(bounces, PersistentDataType.INTEGER);
                if(bounceCount == maxBounces)
                {
                    if(old.getShooter() instanceof Player)
                    {
                        allowed.add(((Player) old.getShooter()).getUniqueId());
                    }
                }
                else if(bounceCount > maxBounces)
                {
                    return;
                }
            }
            
            
            Vector n = event.getHitBlockFace().getDirection();
            Vector reflection = old.getVelocity().clone().subtract(
                    n.multiply(2 * old.getVelocity().dot(n))).multiply(bounciness);
            
            EnderPearl pearlNew = old.getWorld().spawn(old.getLocation(), EnderPearl.class);
            pearlNew.setVelocity(reflection);
            pearlNew.setShooter(old.getShooter());
            PersistentDataContainer newData = pearlNew.getPersistentDataContainer();
            newData.set(bounces, PersistentDataType.INTEGER,bounceCount + 1);
            
            pearlNew.getWorld().playSound(pearlNew.getLocation(), Sound.BLOCK_SLIME_BLOCK_FALL, 1F, 1F);
        }
    }
}
