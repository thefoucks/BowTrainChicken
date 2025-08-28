package com.bowtrain.maps;

import com.bowtrain.BowTrainChicken;
import com.bowtrain.utils.Worlds;
import com.bowtrain.utils.Locations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MapManager {
    
    private final BowTrainChicken plugin;
    private final Map<String, MapData> maps;
    private final File mapsFile;
    private FileConfiguration mapsConfig;
    
    public MapManager(BowTrainChicken plugin) {
        this.plugin = plugin;
        this.maps = new HashMap<>();
        this.mapsFile = new File(plugin.getDataFolder(), "maps.yml");
        
        loadData();
    }
    
    public boolean mapExists(String mapKind) {
        return maps.containsKey(mapKind.toLowerCase());
    }
    
    public MapData getMapData(String mapKind) {
        return maps.get(mapKind.toLowerCase());
    }
    
    public Set<String> getAvailableMapKinds() {
        return maps.keySet();
    }
    
    public boolean createMap(Player player, String mapName) {
        if (maps.containsKey(mapName.toLowerCase())) {
            return false; // Map already exists
        }
        
        Location loc = player.getLocation();
        MapData mapData = new MapData(mapName, loc.getWorld().getName(), loc);
        maps.put(mapName.toLowerCase(), mapData);
        saveData();
        return true;
    }
    
    public boolean deleteMap(String mapName) {
        if (!maps.containsKey(mapName.toLowerCase())) {
            return false; // Map doesn't exist
        }
        
        maps.remove(mapName.toLowerCase());
        saveData();
        return true;
    }

    // יצירת עולם חדש שטוח למפה
    public boolean createMapWorld(String mapName) {
        if (mapExists(mapName)) {
            return false;
        }

        try {
            WorldCreator creator = new WorldCreator(mapName);
            creator.type(org.bukkit.WorldType.FLAT);
            creator.generatorSettings("minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block");
            World world = creator.createWorld();

            if (world == null) {
                return false;
            }

            // ברירת מחדל spawn באמצע העולם
            Location spawn = world.getSpawnLocation();
            MapData mapData = new MapData(mapName, world.getName(), spawn);
            maps.put(mapName.toLowerCase(), mapData);
            saveData();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create map world: " + e.getMessage());
            return false;
        }
    }

    // מחיקת עולם מקור (לא session זמני)
    public boolean deleteMapWorld(String mapName) {
        if (!mapExists(mapName)) {
            return false;
        }

        try {
            String worldName = maps.get(mapName.toLowerCase()).getWorldName();
            World world = plugin.getServer().getWorld(worldName);

            if (world != null) {
                plugin.getServer().unloadWorld(world, false);
            }

            File worldFolder = new File(plugin.getServer().getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                Worlds.deleteDirectory(worldFolder);
            }

            maps.remove(mapName.toLowerCase());
            saveData();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to delete map world: " + e.getMessage());
            return false;
        }
    }

    // שיגור שחקן לעולם של מפה
    public boolean tpToMap(Player player, String mapName) {
        if (!mapExists(mapName)) {
            return false;
        }

        MapData mapData = getMapData(mapName);
        World world = Bukkit.getWorld(mapData.getWorldName());

        if (world == null) {
            plugin.getLogger().warning("World not loaded: " + mapData.getWorldName());
            return false;
        }

        player.teleport(mapData.getSpawnLocation());
        return true;
    }
    
    public boolean setMapSpawn(Player player, String mapName) {
        if (!maps.containsKey(mapName.toLowerCase())) {
            return false; // Map doesn't exist
        }
        
        Location loc = player.getLocation();
        MapData mapData = maps.get(mapName.toLowerCase());
        
        // Update spawn location
        MapData updatedMapData = new MapData(mapName, loc.getWorld().getName(), loc);
        updatedMapData.setChickenPoints(mapData.getChickenPoints());
        if (mapData.getChickenSpawnArea() != null) {
            updatedMapData.setChickenSpawnArea(mapData.getChickenSpawnArea()[0], mapData.getChickenSpawnArea()[1]);
        }
        
        maps.put(mapName.toLowerCase(), updatedMapData);
        saveData();
        return true;
    }
    
    public World createGameWorld(String mapKind, UUID playerId) {
        if (!mapExists(mapKind)) {
            return null;
        }
        
        String gameWorldName = "BTC_" + mapKind + "_" + playerId.toString().substring(0, 8);
        
        try {
            // Copy original world to game world
            MapData mapData = getMapData(mapKind);
            File originalWorldFolder = new File(plugin.getServer().getWorldContainer(), mapData.getWorldName());
            File gameWorldFolder = new File(plugin.getServer().getWorldContainer(), gameWorldName);
            
            if (originalWorldFolder.exists()) {
                Worlds.copyWorld(originalWorldFolder, gameWorldFolder);
                
                // Create and load the world
                WorldCreator creator = new WorldCreator(gameWorldName);
                World gameWorld = creator.createWorld();
                
                return gameWorld;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create game world: " + e.getMessage());
        }
        
        return null;
    }
    
    public void deleteGameWorld(String worldName) {
        try {
            // Only delete worlds that start with BTC_ (safety check)
            if (!worldName.startsWith("BTC_")) {
                plugin.getLogger().warning("Attempted to delete non-game world: " + worldName);
                return;
            }
            
            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                plugin.getServer().unloadWorld(world, false);
            }
            
            File worldFolder = new File(plugin.getServer().getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                Worlds.deleteDirectory(worldFolder);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to delete game world: " + e.getMessage());
        }
    }
    
    public void saveData() {
        if (mapsConfig == null) {
            return;
        }
        
        ConfigurationSection mapsSection = mapsConfig.createSection("maps");
        
        for (Map.Entry<String, MapData> entry : maps.entrySet()) {
            ConfigurationSection mapSection = mapsSection.createSection(entry.getKey());
            entry.getValue().saveToConfig(mapSection);
        }
        
        try {
            mapsConfig.save(mapsFile);
            plugin.getLogger().info("Maps data saved successfully");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save maps.yml file: " + e.getMessage());
        }
    }
    
    private void loadData() {
        if (!mapsFile.exists()) {
            try {
                mapsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create maps.yml file");
                return;
            }
        }
        
        mapsConfig = YamlConfiguration.loadConfiguration(mapsFile);
        
        ConfigurationSection mapsSection = mapsConfig.getConfigurationSection("maps");
        if (mapsSection != null) {
            for (String mapKind : mapsSection.getKeys(false)) {
                ConfigurationSection mapSection = mapsSection.getConfigurationSection(mapKind);
                if (mapSection != null) {
                    MapData mapData = MapData.fromConfig(mapSection);
                    if (mapData != null) {
                        maps.put(mapKind.toLowerCase(), mapData);
                    }
                }
            }
        }
    }
}