package com.bowtrain.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class Locations {
    
    public static void saveToConfig(ConfigurationSection section, String path, Location location) {
        section.set(path + ".world", location.getWorld().getName());
        section.set(path + ".x", location.getX());
        section.set(path + ".y", location.getY());
        section.set(path + ".z", location.getZ());
        section.set(path + ".yaw", location.getYaw());
        section.set(path + ".pitch", location.getPitch());
    }
    
    public static Location loadFromConfig(ConfigurationSection section, String path) {
        try {
            String worldName = section.getString(path + ".world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return null;
            }
            
            double x = section.getDouble(path + ".x");
            double y = section.getDouble(path + ".y");
            double z = section.getDouble(path + ".z");
            float yaw = (float) section.getDouble(path + ".yaw");
            float pitch = (float) section.getDouble(path + ".pitch");
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }
}