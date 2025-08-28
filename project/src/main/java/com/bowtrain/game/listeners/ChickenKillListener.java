package com.bowtrain.game.listeners;

import com.bowtrain.BowTrainChicken;
import com.bowtrain.game.GameSession;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChickenKillListener implements Listener {
    
    private final BowTrainChicken plugin;
    
    public ChickenKillListener(BowTrainChicken plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Chicken)) return;
        if (!(event.getDamager() instanceof Projectile)) return;
        
        Projectile projectile = (Projectile) event.getDamager();
        if (!(projectile.getShooter() instanceof Player)) return;
        
        Player player = (Player) projectile.getShooter();
        GameSession session = plugin.getGameManager().getSession(player);
        
        if (session != null && !session.isGameEnded()) {
            Chicken chicken = (Chicken) event.getEntity();
            
            // Check if this is the session's chicken
            if (chicken.equals(session.getCurrentChicken())) {
                chicken.setHealth(0); // Ensure it dies
                session.onChickenKilled();
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getGameManager().isPlayerInGame(player)) {
            plugin.getGameManager().forceEndGame(player);
        }
    }
}