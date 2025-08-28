package com.bowtrain.stats;

import com.bowtrain.BowTrainChicken;
import com.bowtrain.stats.models.PlayerStats;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class StatisticsManager {
    
    private final BowTrainChicken plugin;
    private final YamlStatsStore statsStore;
    
    public StatisticsManager(BowTrainChicken plugin) {
        this.plugin = plugin;
        this.statsStore = new YamlStatsStore(plugin);
    }
    
    public void addPoints(Player player, String mapKind, int points) {
        PlayerStats stats = getPlayerStats(player.getUniqueId());
        stats.addPoints(mapKind, points);
        statsStore.savePlayerStats(stats);
        // Force save to disk immediately
        saveData();
    }
    
    public PlayerStats getPlayerStats(UUID playerId) {
        PlayerStats stats = statsStore.loadPlayerStats(playerId);
        if (stats == null) {
            stats = new PlayerStats(playerId);
        }
        return stats;
    }
    
    public List<Map.Entry<UUID, Integer>> getTopTotals(String period, String mapKind) {
        Map<UUID, PlayerStats> allStats = statsStore.loadAllPlayerStats();
        
        return allStats.entrySet().stream()
                .map(entry -> {
                    int score = entry.getValue().getTotalPoints(mapKind, period);
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), score);
                })
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
    
    public List<Map.Entry<UUID, Integer>> getTopBests(String period, String mapKind) {
        Map<UUID, PlayerStats> allStats = statsStore.loadAllPlayerStats();
        
        return allStats.entrySet().stream()
                .map(entry -> {
                    int score = entry.getValue().getBestScore(mapKind, period);
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), score);
                })
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
    
    public void resetDaily() {
        Map<UUID, PlayerStats> allStats = statsStore.loadAllPlayerStats();
        for (PlayerStats stats : allStats.values()) {
            stats.resetDaily();
            statsStore.savePlayerStats(stats);
        }
        plugin.getLogger().info("Daily statistics have been reset");
    }
    
    public void resetWeekly() {
        Map<UUID, PlayerStats> allStats = statsStore.loadAllPlayerStats();
        for (PlayerStats stats : allStats.values()) {
            stats.resetWeekly();
            statsStore.savePlayerStats(stats);
        }
        plugin.getLogger().info("Weekly statistics have been reset");
    }
    
    public void resetMonthly() {
        Map<UUID, PlayerStats> allStats = statsStore.loadAllPlayerStats();
        for (PlayerStats stats : allStats.values()) {
            stats.resetMonthly();
            statsStore.savePlayerStats(stats);
        }
        plugin.getLogger().info("Monthly statistics have been reset");
    }
    
    public void resetYearly() {
        Map<UUID, PlayerStats> allStats = statsStore.loadAllPlayerStats();
        for (PlayerStats stats : allStats.values()) {
            stats.resetYearly();
            statsStore.savePlayerStats(stats);
        }
        plugin.getLogger().info("Yearly statistics have been reset");
    }
    
    public void saveData() {
        statsStore.saveData();
    }
}