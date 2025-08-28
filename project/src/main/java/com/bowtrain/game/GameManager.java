package com.bowtrain.game;

import com.bowtrain.BowTrainChicken;
import com.bowtrain.stats.PeriodClock;
import com.bowtrain.utils.Text;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {
    
    private final BowTrainChicken plugin;
    private final Map<UUID, GameSession> activeSessions;
    private final PeriodClock periodClock;
    
    public GameManager(BowTrainChicken plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
        this.periodClock = new PeriodClock(plugin);
        
        // Start the statistics reset task
        startStatisticsResetTask();
    }
    
    public boolean isPlayerInGame(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }
    
    public boolean startGame(Player player, String mapKind) {
        if (isPlayerInGame(player)) {
            player.sendMessage(Text.getMessage("already_in_game"));
            return false;
        }
        
        if (!plugin.getMapManager().mapExists(mapKind)) {
            player.sendMessage(Text.getMessage("map_not_found").replace("{map}", mapKind));
            return false;
        }
        
        GameSession session = new GameSession(plugin, player, mapKind);
        if (session.start()) {
            activeSessions.put(player.getUniqueId(), session);
            return true;
        }
        
        return false;
    }
    
    public void endGame(Player player) {
        GameSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            session.end();
        }
    }
    
    public void forceEndGame(Player player) {
        GameSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            session.forceEnd();
        }
    }
    
    public GameSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }
    
    public void shutdownCleanup() {
        for (GameSession session : activeSessions.values()) {
            session.forceEnd();
        }
        activeSessions.clear();
    }
    
    private void startStatisticsResetTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                periodClock.checkAndResetStats();
            }
        }.runTaskTimer(plugin, 20L * 60L, 20L * 60L); // Check every minute
    }
}