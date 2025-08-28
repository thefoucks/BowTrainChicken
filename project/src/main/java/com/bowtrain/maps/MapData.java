package com.bowtrain.maps;

import com.bowtrain.utils.Locations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class MapData {
    
    private final String mapKind;
    private final String worldName;
    private final Location spawnLocation;
    private Location[] chickenSpawnArea;
    private int chickenPoints;
    
    public MapData(String mapKind, String worldName, Location spawnLocation) {
        this.mapKind = mapKind;
        this.worldName = worldName;
        this.spawnLocation = spawnLocation;
        this.chickenPoints = 1; // Default points per chicken
    }
    
    public String getMapKind() {
        return mapKind;
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public Location getSpawnLocation() {
        return spawnLocation;
    }
    
    public Location[] getChickenSpawnArea() {
        return chickenSpawnArea;
    }
    
    public void setChickenSpawnArea(Location corner1, Location corner2) {
        this.chickenSpawnArea = new Location[]{corner1, corner2};
    }
    
    public int getChickenPoints() {
        return chickenPoints;
    }
    
    public void setChickenPoints(int chickenPoints) {
        this.chickenPoints = chickenPoints;
    }
    
    public void saveToConfig(ConfigurationSection section) {
        section.set("mapKind", mapKind);
        section.set("worldName", worldName);
        Locations.saveToConfig(section, "spawn", spawnLocation);
        section.set("chickenPoints", chickenPoints);
        
        if (chickenSpawnArea != null && chickenSpawnArea.length == 2) {
            Locations.saveToConfig(section, "chickenArea.corner1", chickenSpawnArea[0]);
            Locations.saveToConfig(section, "chickenArea.corner2", chickenSpawnArea[1]);
        }
    }
    
    public static MapData fromConfig(ConfigurationSection section) {
        try {
            String mapKind = section.getString("mapKind");
            String worldName = section.getString("worldName");
            
            Location spawnLocation = Locations.loadFromConfig(section, "spawn");
            if (spawnLocation == null) {
                return null;
            }
            
            MapData mapData = new MapData(mapKind, worldName, spawnLocation);
            mapData.setChickenPoints(section.getInt("chickenPoints", 1));
            
            // Load chicken spawn area if exists
            if (section.contains("chickenArea")) {
                Location corner1 = Locations.loadFromConfig(section, "chickenArea.corner1");
                Location corner2 = Locations.loadFromConfig(section, "chickenArea.corner2");
                
                if (corner1 != null && corner2 != null) {
                    mapData.setChickenSpawnArea(corner1, corner2);
                }
            }
            
            return mapData;
        } catch (Exception e) {
            return null;
        }
    }
}