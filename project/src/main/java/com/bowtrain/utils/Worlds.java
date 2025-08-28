package com.bowtrain.utils;

import com.bowtrain.BowTrainChicken;
import org.bukkit.GameRule;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Worlds {
    
    public static void setupGameRules(World world) {
        BowTrainChicken plugin = BowTrainChicken.getInstance();
        
        world.setGameRule(GameRule.DO_MOB_SPAWNING, plugin.getConfig().getBoolean("world.gamerules.doMobSpawning", false));
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, plugin.getConfig().getBoolean("world.gamerules.doDaylightCycle", false));
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, plugin.getConfig().getBoolean("world.gamerules.doWeatherCycle", false));
        world.setGameRule(GameRule.KEEP_INVENTORY, plugin.getConfig().getBoolean("world.gamerules.keepInventory", true));
        world.setGameRule(GameRule.NATURAL_REGENERATION, plugin.getConfig().getBoolean("world.gamerules.naturalRegeneration", true));
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, plugin.getConfig().getBoolean("world.gamerules.announceAdvancements", false));
        world.setGameRule(GameRule.DO_FIRE_TICK, plugin.getConfig().getBoolean("world.gamerules.doFireTick", false));
    }
    
    public static void copyWorld(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdirs();
            }
            
            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(target, file);
                    copyWorld(srcFile, destFile);
                }
            }
        } else {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}