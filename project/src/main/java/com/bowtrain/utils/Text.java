package com.bowtrain.utils;

import com.bowtrain.BowTrainChicken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Text {
    
    private static final BowTrainChicken plugin = BowTrainChicken.getInstance();
    
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    public static String getMessage(String key) {
        String message = plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key);
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        return colorize(prefix + message);
    }
    
    public static void sendActionBar(Player player, String message) {
        try {
            // Try Adventure API first (Paper)
            if (plugin.getConfig().getBoolean("text.use_adventure_actionbar", true)) {
                Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
                player.sendActionBar(component);
            } else {
                // Fallback to Spigot
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(colorize(message)));
            }
        } catch (Exception e) {
            // Final fallback - send as regular message
            player.sendMessage(colorize(message));
        }
    }
}