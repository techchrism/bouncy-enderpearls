package com.darkender.plugins.bouncyenderpearls;

import org.bukkit.Bukkit;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BouncyEnderpearls extends JavaPlugin implements Listener
{
    private NamespacedKey bounces;
    private final Set<UUID> allowed = new HashSet<>();
    
    private double bounciness = 0.8;
    private int maxBounces = 5;
    
    @Override
    public void onEnable()
    {
        bounces = new NamespacedKey(this, "bounces");
        getServer().getPluginManager().registerEvents(this, this);
        BEPCommand bepCommand = new BEPCommand(this);
        getCommand("bouncyenderpearls").setExecutor(bepCommand);
        getCommand("bouncyenderpearls").setTabCompleter(bepCommand);
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
        // This event is called before the "onProjectileHit" event so a set is needed
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
        // Check if it's a player firing an enderpearl
        if(event.getEntityType() == EntityType.ENDER_PEARL && event.getEntity().getShooter() instanceof Player)
        {
            Player p = (Player) event.getEntity().getShooter();
            // If they don't have permission, ensure the destination teleport behaves normally
            if(!p.hasPermission("bouncyenderpearls.enabled"))
            {
                // Make this the last teleport
                allowed.add(p.getUniqueId());
                // Make this the last bounce
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
            
            // The vector to "bounce" off of
            Vector n;
            if(event.getHitEntity() != null)
            {
                // Treat the plane orthogonal to the vector between the pearl and the entity as a solid plane to bounce off of
                n = old.getLocation().toVector()
                        .subtract(event.getHitEntity().getBoundingBox().getCenter())
                        .normalize();
            }
            else
            {
                n = event.getHitBlockFace().getDirection();
            }
            
            PersistentDataContainer data = old.getPersistentDataContainer();
            int bounceCount = 1;
            if(data.has(bounces, PersistentDataType.INTEGER))
            {
                bounceCount = data.get(bounces, PersistentDataType.INTEGER);
                // Allow the player to teleport if it's the last bounce
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
            
            Vector reflection = old.getVelocity().clone().subtract(
                    n.multiply(2 * old.getVelocity().dot(n))).multiply(bounciness);
            
            EnderPearl pearlNew = old.getWorld().spawn(old.getLocation(), EnderPearl.class);
            pearlNew.setShooter(old.getShooter());
            old.remove();
            
            // Launch the event for other plugins to use
            ProjectileLaunchEvent launchEvent = new ProjectileLaunchEvent(pearlNew);
            Bukkit.getPluginManager().callEvent(launchEvent);
            
            pearlNew.setVelocity(reflection);
            PersistentDataContainer newData = pearlNew.getPersistentDataContainer();
            newData.set(bounces, PersistentDataType.INTEGER,bounceCount + 1);
            
            pearlNew.getWorld().playSound(pearlNew.getLocation(), Sound.BLOCK_SLIME_BLOCK_FALL, 1F, 1F);
        }
    }
}
