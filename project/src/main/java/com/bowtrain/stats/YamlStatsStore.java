package com.bowtrain.stats;

import com.bowtrain.BowTrainChicken;
import com.bowtrain.stats.models.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YamlStatsStore {
    
    private final BowTrainChicken plugin;
    private final File statsFile;
    private FileConfiguration statsConfig;
    private final Map<UUID, PlayerStats> cachedStats;
    
    public YamlStatsStore(BowTrainChicken plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        this.cachedStats = new HashMap<>();
        
        loadData();
    }
    
    public PlayerStats loadPlayerStats(UUID playerId) {
        if (cachedStats.containsKey(playerId)) {
            return cachedStats.get(playerId);
        }
        
        ConfigurationSection playersSection = statsConfig.getConfigurationSection("players");
        if (playersSection != null) {
            ConfigurationSection playerSection = playersSection.getConfigurationSection(playerId.toString());
            if (playerSection != null) {
                PlayerStats stats = PlayerStats.fromConfig(playerId, playerSection);
                if (stats != null) {
                    cachedStats.put(playerId, stats);
                    return stats;
                }
            }
        }
        
        return null;
    }
    
    public void savePlayerStats(PlayerStats stats) {
        cachedStats.put(stats.getPlayerId(), stats);
        
        ConfigurationSection playersSection = statsConfig.getConfigurationSection("players");
        if (playersSection == null) {
            playersSection = statsConfig.createSection("players");
        }
        
        ConfigurationSection playerSection = playersSection.createSection(stats.getPlayerId().toString());
        stats.saveToConfig(playerSection);
        
        // Save player name for reference
        String playerName = Bukkit.getOfflinePlayer(stats.getPlayerId()).getName();
        if (playerName != null) {
            playerSection.set("name", playerName);
        }
    }
    
    public Map<UUID, PlayerStats> loadAllPlayerStats() {
        Map<UUID, PlayerStats> allStats = new HashMap<>(cachedStats);
        
        ConfigurationSection playersSection = statsConfig.getConfigurationSection("players");
        if (playersSection != null) {
            for (String playerIdString : playersSection.getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(playerIdString);
                    if (!allStats.containsKey(playerId)) {
                        PlayerStats stats = loadPlayerStats(playerId);
                        if (stats != null) {
                            allStats.put(playerId, stats);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in statistics file: " + playerIdString);
                }
            }
        }
        
        return allStats;
    }
    
    public void saveData() {
        // Save all cached stats
        for (PlayerStats stats : cachedStats.values()) {
            savePlayerStats(stats);
        }
        
        // Save reset times
        ConfigurationSection metaSection = statsConfig.getConfigurationSection("meta");
        if (metaSection == null) {
            metaSection = statsConfig.createSection("meta");
        }
        
        ConfigurationSection lastResetSection = metaSection.getConfigurationSection("lastReset");
        if (lastResetSection == null) {
            lastResetSection = metaSection.createSection("lastReset");
        }
        
        LocalDateTime now = LocalDateTime.now();
        long nowMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        if (!lastResetSection.contains("daily")) {
            lastResetSection.set("daily", nowMillis);
        }
        if (!lastResetSection.contains("weekly")) {
            lastResetSection.set("weekly", nowMillis);
        }
        if (!lastResetSection.contains("monthly")) {
            lastResetSection.set("monthly", nowMillis);
        }
        if (!lastResetSection.contains("yearly")) {
            lastResetSection.set("yearly", nowMillis);
        }
        
        try {
            statsConfig.save(statsFile);
            plugin.getLogger().info("Statistics data saved successfully");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save stats.yml file: " + e.getMessage());
        }
    }
    
    public LocalDateTime getLastReset(String period) {
        ConfigurationSection metaSection = statsConfig.getConfigurationSection("meta");
        if (metaSection != null) {
            ConfigurationSection lastResetSection = metaSection.getConfigurationSection("lastReset");
            if (lastResetSection != null) {
                long millis = lastResetSection.getLong(period, System.currentTimeMillis());
                return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), ZoneId.systemDefault());
            }
        }
        return LocalDateTime.now();
    }
    
    public void setLastReset(String period, LocalDateTime time) {
        ConfigurationSection metaSection = statsConfig.getConfigurationSection("meta");
        if (metaSection == null) {
            metaSection = statsConfig.createSection("meta");
        }
        
        ConfigurationSection lastResetSection = metaSection.getConfigurationSection("lastReset");
        if (lastResetSection == null) {
            lastResetSection = metaSection.createSection("lastReset");
        }
        
        long millis = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        lastResetSection.set(period, millis);
    }
    
    private void loadData() {
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create stats.yml file");
                return;
            }
        }
        
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
    }
}