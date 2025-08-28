package com.bowtrain.stats.models;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStats {
    
    private final UUID playerId;
    
    // Statistics for each time period
    private final Map<String, Integer> dailyTotal;
    private final Map<String, Integer> weeklyTotal;
    private final Map<String, Integer> monthlyTotal;
    private final Map<String, Integer> yearlyTotal;
    
    private final Map<String, Integer> dailyBest;
    private final Map<String, Integer> weeklyBest;
    private final Map<String, Integer> monthlyBest;
    private final Map<String, Integer> yearlyBest;
    
    public PlayerStats(UUID playerId) {
        this.playerId = playerId;
        this.dailyTotal = new HashMap<>();
        this.weeklyTotal = new HashMap<>();
        this.monthlyTotal = new HashMap<>();
        this.yearlyTotal = new HashMap<>();
        this.dailyBest = new HashMap<>();
        this.weeklyBest = new HashMap<>();
        this.monthlyBest = new HashMap<>();
        this.yearlyBest = new HashMap<>();
    }
    
    public void addPoints(String mapKind, int points) {
        // Add to total points
        dailyTotal.merge(mapKind, points, Integer::sum);
        weeklyTotal.merge(mapKind, points, Integer::sum);
        monthlyTotal.merge(mapKind, points, Integer::sum);
        yearlyTotal.merge(mapKind, points, Integer::sum);
        
        // Update best scores if this game was better
        dailyBest.merge(mapKind, points, Integer::max);
        weeklyBest.merge(mapKind, points, Integer::max);
        monthlyBest.merge(mapKind, points, Integer::max);
        yearlyBest.merge(mapKind, points, Integer::max);
    }
    
    public int getTotalPoints(String mapKind, String period) {
        Map<String, Integer> map = getTotalMap(period);
        if (mapKind.equalsIgnoreCase("all")) {
            return map.values().stream().mapToInt(Integer::intValue).sum();
        }
        return map.getOrDefault(mapKind, 0);
    }
    
    public int getBestScore(String mapKind, String period) {
        Map<String, Integer> map = getBestMap(period);
        if (mapKind.equalsIgnoreCase("all")) {
            return map.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        }
        return map.getOrDefault(mapKind, 0);
    }
    
    private Map<String, Integer> getTotalMap(String period) {
        switch (period.toLowerCase()) {
            case "daily": return dailyTotal;
            case "weekly": return weeklyTotal;
            case "monthly": return monthlyTotal;
            case "yearly": return yearlyTotal;
            default: return dailyTotal;
        }
    }
    
    private Map<String, Integer> getBestMap(String period) {
        switch (period.toLowerCase()) {
            case "daily": return dailyBest;
            case "weekly": return weeklyBest;
            case "monthly": return monthlyBest;
            case "yearly": return yearlyBest;
            default: return dailyBest;
        }
    }
    
    public void resetDaily() {
        dailyTotal.clear();
        dailyBest.clear();
    }
    
    public void resetWeekly() {
        weeklyTotal.clear();
        weeklyBest.clear();
    }
    
    public void resetMonthly() {
        monthlyTotal.clear();
        monthlyBest.clear();
    }
    
    public void resetYearly() {
        yearlyTotal.clear();
        yearlyBest.clear();
    }
    
    public void saveToConfig(ConfigurationSection section) {
        saveMapToConfig(section, "daily.totals", dailyTotal);
        saveMapToConfig(section, "weekly.totals", weeklyTotal);
        saveMapToConfig(section, "monthly.totals", monthlyTotal);
        saveMapToConfig(section, "yearly.totals", yearlyTotal);
        
        saveMapToConfig(section, "daily.best", dailyBest);
        saveMapToConfig(section, "weekly.best", weeklyBest);
        saveMapToConfig(section, "monthly.best", monthlyBest);
        saveMapToConfig(section, "yearly.best", yearlyBest);
    }
    
    private void saveMapToConfig(ConfigurationSection section, String path, Map<String, Integer> map) {
        ConfigurationSection mapSection = section.createSection(path);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            mapSection.set(entry.getKey(), entry.getValue());
        }
    }
    
    public static PlayerStats fromConfig(UUID playerId, ConfigurationSection section) {
        PlayerStats stats = new PlayerStats(playerId);
        
        loadMapFromConfig(section, "daily.totals", stats.dailyTotal);
        loadMapFromConfig(section, "weekly.totals", stats.weeklyTotal);
        loadMapFromConfig(section, "monthly.totals", stats.monthlyTotal);
        loadMapFromConfig(section, "yearly.totals", stats.yearlyTotal);
        
        loadMapFromConfig(section, "daily.best", stats.dailyBest);
        loadMapFromConfig(section, "weekly.best", stats.weeklyBest);
        loadMapFromConfig(section, "monthly.best", stats.monthlyBest);
        loadMapFromConfig(section, "yearly.best", stats.yearlyBest);
        
        return stats;
    }
    
    private static void loadMapFromConfig(ConfigurationSection section, String path, Map<String, Integer> map) {
        ConfigurationSection mapSection = section.getConfigurationSection(path);
        if (mapSection != null) {
            for (String key : mapSection.getKeys(false)) {
                map.put(key, mapSection.getInt(key));
            }
        }
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
}