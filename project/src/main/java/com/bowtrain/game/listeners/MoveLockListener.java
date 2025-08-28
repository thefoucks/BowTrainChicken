package com.bowtrain.game.listeners;

import com.bowtrain.BowTrainChicken;
import com.bowtrain.game.GameSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveLockListener implements Listener {
    
    private final BowTrainChicken plugin;
    
    public MoveLockListener(BowTrainChicken plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        GameSession session = plugin.getGameManager().getSession(player);
        
        if (session != null && !session.isGameEnded()) {
            // Check if player moved significantly (not just rotation)
            if (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getZ() != event.getTo().getZ() ||
                event.getFrom().getY() != event.getTo().getY()) {
                
                // Teleport back to spawn location
                player.teleport(session.getGameSpawnLocation());
            }
        }
    }
}