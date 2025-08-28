package com.bowtrain.permissions;

import com.bowtrain.BowTrainChicken;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionManager {
    
    private final BowTrainChicken plugin;
    private final Map<UUID, Boolean> elevatedPermissions;
    private final Map<UUID, UUID> pendingRequests; // requester -> approver
    private final File permissionsFile;
    private FileConfiguration permissionsConfig;
    
    public PermissionManager(BowTrainChicken plugin) {
        this.plugin = plugin;
        this.elevatedPermissions = new HashMap<>();
        this.pendingRequests = new HashMap<>();
        this.permissionsFile = new File(plugin.getDataFolder(), "permissions.yml");
        
        loadData();
    }
    
    public boolean hasElevatedPermission(Player player) {
        if (player.hasPermission("btc.admin")) {
            return true;
        }
        return elevatedPermissions.getOrDefault(player.getUniqueId(), false);
    }
    
    public void requestPermission(Player requester, Player approver) {
        pendingRequests.put(requester.getUniqueId(), approver.getUniqueId());
        saveData();
    }
    
    public boolean hasPendingRequest(Player requester) {
        return pendingRequests.containsKey(requester.getUniqueId());
    }
    
    public UUID getPendingApprover(Player requester) {
        return pendingRequests.get(requester.getUniqueId());
    }
    
    public void approvePermission(Player approver, Player requester) {
        UUID requesterId = requester.getUniqueId();
        UUID approverId = approver.getUniqueId();
        
        if (pendingRequests.get(requesterId) != null && 
            pendingRequests.get(requesterId).equals(approverId)) {
            
            elevatedPermissions.put(requesterId, true);
            pendingRequests.remove(requesterId);
            saveData();
        }
    }
    
    public void revokePermission(Player player) {
        elevatedPermissions.remove(player.getUniqueId());
        pendingRequests.remove(player.getUniqueId());
        saveData();
    }
    
    public void saveData() {
        permissionsConfig.set("elevated", null);
        permissionsConfig.set("pending", null);
        
        if (!elevatedPermissions.isEmpty()) {
            for (Map.Entry<UUID, Boolean> entry : elevatedPermissions.entrySet()) {
                if (entry.getValue()) {
                    permissionsConfig.set("elevated." + entry.getKey().toString(), true);
                }
            }
        }
        
        if (!pendingRequests.isEmpty()) {
            for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
                permissionsConfig.set("pending." + entry.getKey().toString(), entry.getValue().toString());
            }
        }
        
        try {
            permissionsConfig.save(permissionsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save permissions.yml file");
        }
    }
    
    private void loadData() {
        if (!permissionsFile.exists()) {
            try {
                permissionsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create permissions.yml file");
                return;
            }
        }
        
        permissionsConfig = YamlConfiguration.loadConfiguration(permissionsFile);
        
        // Load elevated permissions
        if (permissionsConfig.contains("elevated")) {
            for (String uuidString : permissionsConfig.getConfigurationSection("elevated").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    elevatedPermissions.put(uuid, permissionsConfig.getBoolean("elevated." + uuidString));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in permissions file: " + uuidString);
                }
            }
        }
        
        // Load pending requests
        if (permissionsConfig.contains("pending")) {
            for (String requesterString : permissionsConfig.getConfigurationSection("pending").getKeys(false)) {
                try {
                    UUID requester = UUID.fromString(requesterString);
                    UUID approver = UUID.fromString(permissionsConfig.getString("pending." + requesterString));
                    pendingRequests.put(requester, approver);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in pending requests: " + requesterString);
                }
            }
        }
    }
}