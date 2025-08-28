package com.bowtrain;

import com.bowtrain.commands.BTCCommand;
import com.bowtrain.commands.BTCCompleter;
import com.bowtrain.game.GameManager;
import com.bowtrain.game.listeners.ChickenKillListener;
import com.bowtrain.game.listeners.MoveLockListener;
import com.bowtrain.maps.MapManager;
import com.bowtrain.stats.StatisticsManager;
import com.bowtrain.permissions.PermissionManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BowTrainChicken extends JavaPlugin {
    
    private static BowTrainChicken instance;
    private GameManager gameManager;
    private MapManager mapManager;
    private StatisticsManager statisticsManager;
    private PermissionManager permissionManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        this.mapManager = new MapManager(this);
        this.statisticsManager = new StatisticsManager(this);
        this.permissionManager = new PermissionManager(this);
        this.gameManager = new GameManager(this);
        
        // Register commands
        BTCCommand commandExecutor = new BTCCommand(this);
        getCommand("btc").setExecutor(commandExecutor);
        getCommand("btc").setTabCompleter(new BTCCompleter(this));
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new ChickenKillListener(this), this);
        getServer().getPluginManager().registerEvents(new MoveLockListener(this), this);
        
        getLogger().info("BowTrainChicken has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Clean up any active games
        if (gameManager != null) {
            gameManager.shutdownCleanup();
        }
        
        // Save statistics
        if (statisticsManager != null) {
            statisticsManager.saveData();
        }
        
        // Save maps
        if (mapManager != null) {
            mapManager.saveData();
        }
        
        // Save permissions
        if (permissionManager != null) {
            permissionManager.saveData();
        }
        
        getLogger().info("BowTrainChicken has been disabled!");
    }
    
    public static BowTrainChicken getInstance() {
        return instance;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public MapManager getMapManager() {
        return mapManager;
    }
    
    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }
    
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
}