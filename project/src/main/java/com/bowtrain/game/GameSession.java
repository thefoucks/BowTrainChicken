package com.bowtrain.game;

import com.bowtrain.BowTrainChicken;
import com.bowtrain.maps.MapData;
import com.bowtrain.utils.Text;
import com.bowtrain.utils.Worlds;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class GameSession {
    
    private final BowTrainChicken plugin;
    private final Player player;
    private final String mapKind;
    private final World gameWorld;
    private final Location originalLocation;
    private final ItemStack[] originalInventory;
    private final ItemStack[] originalArmor;
    private final ItemStack originalOffHand;
    private final Random random;
    
    private int score;
    private Chicken currentChicken;
    private BukkitTask gameTask;
    private BukkitTask chickenTask;
    private boolean gameEnded;
    private Location gameSpawnLocation;
    
    public GameSession(BowTrainChicken plugin, Player player, String mapKind) {
        this.plugin = plugin;
        this.player = player;
        this.mapKind = mapKind;
        this.originalLocation = player.getLocation().clone();
        this.originalInventory = player.getInventory().getContents().clone();
        this.originalArmor = player.getInventory().getArmorContents().clone();
        this.originalOffHand = player.getInventory().getItemInOffHand().clone();
        this.random = new Random();
        this.score = 0;
        this.gameEnded = false;
        
        // Create game world
        this.gameWorld = plugin.getMapManager().createGameWorld(mapKind, player.getUniqueId());
    }
    
    public boolean start() {
        if (gameWorld == null) {
            player.sendMessage(Text.colorize("&cFailed to create game world!"));
            return false;
        }
        
        MapData mapData = plugin.getMapManager().getMapData(mapKind);
        if (mapData == null) {
            player.sendMessage(Text.colorize("&cMap data not found!"));
            return false;
        }
        
        // Set up game world rules
        Worlds.setupGameRules(gameWorld);
        
        // Teleport player to game world
        gameSpawnLocation = new Location(gameWorld, 
            mapData.getSpawnLocation().getX(),
            mapData.getSpawnLocation().getY(),
            mapData.getSpawnLocation().getZ(),
            mapData.getSpawnLocation().getYaw(),
            mapData.getSpawnLocation().getPitch());
        
        player.teleport(gameSpawnLocation);
        
        // Clear inventory and give bow and arrows
        setupPlayerInventory();
        
        // Freeze player
        freezePlayer();
        
        int countdown = plugin.getConfig().getInt("game.countdown", 5);
        player.sendMessage(Text.getMessage("game_start").replace("{countdown}", String.valueOf(countdown)));
        player.sendTitle(Text.colorize("&6BowTrainChicken"), Text.colorize("&eGet ready to shoot!"), 10, 40, 10);
        
        // Start countdown
        new BukkitRunnable() {
            int timeLeft = countdown;
            
            @Override
            public void run() {
                if (gameEnded) {
                    cancel();
                    return;
                }
                
                if (timeLeft > 0) {
                    player.sendMessage(Text.colorize("&e" + timeLeft + "..."));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    timeLeft--;
                } else {
                    player.sendMessage(Text.colorize("&aGO!"));
                    player.sendTitle(Text.colorize("&aGO!"), "", 5, 20, 5);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    startGameTimer();
                    spawnChicken();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        return true;
    }
    
    private void setupPlayerInventory() {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(new ItemStack[4]);
        inv.setItemInOffHand(new ItemStack(Material.AIR));
        
        // Create bow with enchantments
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.INFINITY, plugin.getConfig().getInt("weapon.infinity_level", 1));
        bow.addEnchantment(Enchantment.UNBREAKING, plugin.getConfig().getInt("weapon.unbreaking_level", 10));
        
        // Give bow and arrows
        inv.setItem(0, bow);
        inv.setItem(9, new ItemStack(Material.ARROW, plugin.getConfig().getInt("weapon.arrow_amount", 64)));
    }
    
    private void freezePlayer() {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 128, true, false));
    }
    
    private void unfreezePlayer() {
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
    }
    
    private void startGameTimer() {
        int duration = plugin.getConfig().getInt("game.duration", 90);
        
        gameTask = new BukkitRunnable() {
            int timeLeft = duration;
            
            @Override
            public void run() {
                if (gameEnded) {
                    cancel();
                    return;
                }
                
                if (timeLeft <= 0) {
                    endGame();
                    cancel();
                    return;
                }
                
                // Show time remaining and score
                String actionBarText = Text.colorize("&eTime: &6" + timeLeft + "s &7| &eScore: &6" + score);
                Text.sendActionBar(player, actionBarText);
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
    
    public void spawnChicken() {
        if (gameEnded) return;
        
        if (currentChicken != null && !currentChicken.isDead()) {
            currentChicken.remove();
        }
        
        MapData mapData = plugin.getMapManager().getMapData(mapKind);
        Location spawnLocation;
        
        if (mapData != null && mapData.getChickenSpawnArea() != null) {
            // Spawn in defined area
            Location[] spawnArea = mapData.getChickenSpawnArea();
            Location min = spawnArea[0];
            Location max = spawnArea[1];
            
            double x = Math.min(min.getX(), max.getX()) + random.nextDouble() * Math.abs(max.getX() - min.getX());
            double y = Math.min(min.getY(), max.getY()) + random.nextDouble() * Math.abs(max.getY() - min.getY());
            double z = Math.min(min.getZ(), max.getZ()) + random.nextDouble() * Math.abs(max.getZ() - min.getZ());
            
            spawnLocation = new Location(gameWorld, x, y, z);
        } else {
            // Default spawn area around player
            Location playerLoc = gameSpawnLocation;
            spawnLocation = playerLoc.clone().add(
                random.nextInt(21) - 10, // -10 to 10
                random.nextInt(5) + 1,   // 1 to 5
                random.nextInt(21) - 10  // -10 to 10
            );
        }
        
        spawnChickenAt(spawnLocation);
    }
    
    private void spawnChickenAt(Location location) {
        currentChicken = (Chicken) gameWorld.spawnEntity(location, EntityType.CHICKEN);
        
        // Configure chicken
        double health = plugin.getConfig().getDouble("defaults.chicken_health", 1.0);
        currentChicken.setHealth(health);
        currentChicken.setAI(false);
        currentChicken.setGravity(false);
        currentChicken.setSilent(true);
        currentChicken.setInvulnerable(false);
        currentChicken.setGlowing(true);
        
        player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 0.5f, 1.0f);
    }
    
    public void onChickenKilled() {
        if (gameEnded) return;
        
        // Remove the current chicken
        if (currentChicken != null && !currentChicken.isDead()) {
            currentChicken.remove();
        }
        
        MapData mapData = plugin.getMapManager().getMapData(mapKind);
        int points = (mapData != null) ? mapData.getChickenPoints() : plugin.getConfig().getInt("defaults.chicken_points", 1);
        
        score += points;
        String actionBarText = Text.colorize("&a+&6" + points + " &apoints! &7| &eTotal: &6" + score);
        Text.sendActionBar(player, actionBarText);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        
        // Spawn next chicken after a short delay
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameEnded) {
                    spawnChicken();
                }
            }
        }.runTaskLater(plugin, 5L); // 0.25 second delay
    }
    
    public void end() {
        if (gameEnded) return;
        gameEnded = true;
        
        // Cancel tasks
        if (gameTask != null) {
            gameTask.cancel();
        }
        if (chickenTask != null) {
            chickenTask.cancel();
        }
        
        // Remove chicken
        if (currentChicken != null && !currentChicken.isDead()) {
            currentChicken.remove();
        }
        
        // Unfreeze player
        unfreezePlayer();
        
        // Show final score
        player.sendMessage(Text.getMessage("game_over"));
        player.sendMessage(Text.getMessage("score_display").replace("{score}", String.valueOf(score)));
        player.sendTitle(Text.colorize("&6Game Over!"), Text.colorize("&eScore: &6" + score), 10, 60, 10);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        
        // Save statistics
        plugin.getStatisticsManager().addPoints(player, mapKind, score);
        
        // Teleport back after delay
        int backDelay = plugin.getConfig().getInt("game.back_delay", 10);
        new BukkitRunnable() {
            @Override
            public void run() {
                teleportBack();
            }
        }.runTaskLater(plugin, backDelay * 20L);
    }
    
    public void forceEnd() {
        gameEnded = true;
        
        if (gameTask != null) {
            gameTask.cancel();
        }
        if (chickenTask != null) {
            chickenTask.cancel();
        }
        
        if (currentChicken != null && !currentChicken.isDead()) {
            currentChicken.remove();
        }
        
        unfreezePlayer();
        teleportBack();
    }

    private void endGame() {
        end();
    }

    private void teleportBack() {
        // Restore inventory
        player.getInventory().setContents(originalInventory);
        player.getInventory().setArmorContents(originalArmor);
        player.getInventory().setItemInOffHand(originalOffHand);
        
        // Teleport back
        player.teleport(originalLocation);
        player.sendMessage(Text.colorize("&aYou have been teleported back!"));
        
        // Delete game world
        String worldName = gameWorld.getName();
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getMapManager().deleteGameWorld(worldName);
            }
        }.runTaskLater(plugin, 20L); // 1 second delay
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getMapKind() {
        return mapKind;
    }
    
    public int getScore() {
        return score;
    }
    
    public boolean isGameEnded() {
        return gameEnded;
    }
    
    public Location getGameSpawnLocation() {
        return gameSpawnLocation;
    }
    
    public Chicken getCurrentChicken() {
        return currentChicken;
    }
}