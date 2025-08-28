package com.bowtrain.commands;

import com.bowtrain.BowTrainChicken;
import com.bowtrain.maps.MapData;
import com.bowtrain.stats.models.PlayerStats;
import com.bowtrain.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class BTCCommand implements CommandExecutor {
    
    private final BowTrainChicken plugin;
    
    public BTCCommand(BowTrainChicken plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start":
                return handleStart(sender, args);
            case "createmap":
                return handleCreateMap(sender, args);
            case "deletemap":
                return handleDeleteMap(sender, args);
            case "tpmap":
                return handleTpMap(sender, args);
            case "setmapspawn":
                return handleSetMapSpawn(sender, args);
            case "setchickenpoint":
                return handleSetChickenPoint(sender, args);
            case "setchickenspawnarea":
                return handleSetChickenSpawnArea(sender, args);
            case "permission":
                return handlePermission(sender, args);
            case "totalpoint":
                return handleTotalPoint(sender, args);
            case "bestpoint":
                return handleBestPoint(sender, args);
            case "points":
                return handlePoints(sender, args);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sender.sendMessage(Text.colorize("&cUnknown command. Use &e/btc help &cfor available commands."));
                return true;
        }
    }
    
    private boolean handleStart(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("btc.use")) {
            player.sendMessage(Text.getMessage("no_permission"));
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(Text.colorize("&cUsage: /btc start <mapKind>"));
            return true;
        }
        
        String mapKind = args[1];
        
        if (plugin.getGameManager().startGame(player, mapKind)) {
            player.sendMessage(Text.colorize("&aStarting game on map: &e" + mapKind));
        }
        
        return true;
    }

    private boolean handleCreateMap(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.getMessage("player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (!hasAdminPermission(player)) {
            player.sendMessage(Text.getMessage("no_permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Text.colorize("&cUsage: /btc createmap <mapName>"));
            return true;
        }

        String mapName = args[1];

        if (Bukkit.getWorld(mapName) != null) {
            player.sendMessage(Text.colorize("&cWorld '" + mapName + "' already exists."));
            return true;
        }

        org.bukkit.WorldCreator creator = new org.bukkit.WorldCreator(mapName);
        creator.type(org.bukkit.WorldType.FLAT); // עולם שטוח
        creator.generateStructures(false);       // בלי כפרים/מקדשים
        Bukkit.createWorld(creator);

        player.sendMessage(Text.colorize("&aFlat map '" + mapName + "' has been created successfully!"));
        return true;
    }

    private boolean handleDeleteMap(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.getMessage("player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (!hasAdminPermission(player)) {
            player.sendMessage(Text.getMessage("no_permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Text.colorize("&cUsage: /btc deletemap <mapName>"));
            return true;
        }

        String mapName = args[1];

        if (Bukkit.getWorld(mapName) != null) {
            Bukkit.unloadWorld(mapName, false);
        }

        java.io.File folder = new java.io.File(Bukkit.getWorldContainer(), mapName);
        if (folder.exists()) {
            try {
                deleteFolder(folder);
                player.sendMessage(Text.colorize("&aWorld '" + mapName + "' has been deleted successfully!"));
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(Text.colorize("&cFailed to delete world '" + mapName + "'."));
            }
        } else {
            player.sendMessage(Text.colorize("&cWorld '" + mapName + "' does not exist."));
        }

        return true;
    }

    private void deleteFolder(java.io.File file) {
        if (file.isDirectory()) {
            for (java.io.File child : file.listFiles()) {
                deleteFolder(child);
            }
        }
        file.delete();
    }

    private boolean handleSetMapSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!hasAdminPermission(player)) {
            player.sendMessage(Text.getMessage("no_permission"));
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(Text.colorize("&cUsage: /btc setmapspawn <mapName>"));
            return true;
        }
        
        String mapName = args[1];
        boolean existed = plugin.getMapManager().setMapSpawn(player, mapName);
        
        if (existed) {
            player.sendMessage(Text.colorize("&aMap spawn updated for '" + mapName + "'!"));
        } else {
            player.sendMessage(Text.colorize("&cMap '" + mapName + "' does not exist. Create it first with /btc createmap."));
        }
        
        return true;
    }
    
    private boolean handleSetChickenPoint(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!hasAdminPermission(player)) {
            sender.sendMessage(Text.getMessage("no_permission"));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(Text.colorize("&cUsage: /btc setchickenpoint <mapKind> <points>"));
            return true;
        }
        
        String mapKind = args[1];
        
        try {
            int points = Integer.parseInt(args[2]);
            
            MapData mapData = plugin.getMapManager().getMapData(mapKind);
            if (mapData == null) {
                sender.sendMessage(Text.getMessage("map_not_found").replace("{map}", mapKind));
                return true;
            }
            
            mapData.setChickenPoints(points);
            plugin.getMapManager().saveData();
            sender.sendMessage(Text.colorize("&aSet chicken points to &e" + points + " &afor map '" + mapKind + "'!"));
            
        } catch (NumberFormatException e) {
            sender.sendMessage(Text.colorize("&cInvalid number: " + args[2]));
        }
        
        return true;
    }
    
    private boolean handleSetChickenSpawnArea(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!hasAdminPermission(player)) {
            player.sendMessage(Text.getMessage("no_permission"));
            return true;
        }
        
        if (args.length < 8) {
            player.sendMessage(Text.colorize("&cUsage: /btc setchickenspawnarea <mapKind> <x1> <y1> <z1> <x2> <y2> <z2>"));
            return true;
        }
        
        String mapKind = args[1];
        
        try {
            double x1 = Double.parseDouble(args[2]);
            double y1 = Double.parseDouble(args[3]);
            double z1 = Double.parseDouble(args[4]);
            double x2 = Double.parseDouble(args[5]);
            double y2 = Double.parseDouble(args[6]);
            double z2 = Double.parseDouble(args[7]);
            
            MapData mapData = plugin.getMapManager().getMapData(mapKind);
            if (mapData == null) {
                player.sendMessage(Text.getMessage("map_not_found").replace("{map}", mapKind));
                return true;
            }
            
            Location corner1 = new Location(player.getWorld(), x1, y1, z1);
            Location corner2 = new Location(player.getWorld(), x2, y2, z2);
            
            mapData.setChickenSpawnArea(corner1, corner2);
            plugin.getMapManager().saveData();
            player.sendMessage(Text.colorize("&aChicken spawn area set for map '" + mapKind + "'!"));
            
        } catch (NumberFormatException e) {
            player.sendMessage(Text.colorize("&cInvalid coordinates!"));
        }
        
        return true;
    }
    
    private boolean handlePermission(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            player.sendMessage(Text.colorize("&cUsage: /btc permission <playerName> OR /btc permission accept <playerName>"));
            return true;
        }
        
        if (args[1].equalsIgnoreCase("accept")) {
            // Handle permission approval - only OPs can do this
            if (!player.isOp()) {
                player.sendMessage(Text.colorize("&cOnly server operators can approve permissions!"));
                return true;
            }
            
            if (args.length < 3) {
                player.sendMessage(Text.colorize("&cUsage: /btc permission accept <playerName>"));
                return true;
            }
            
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                player.sendMessage(Text.colorize("&cPlayer not found!"));
                return true;
            }
            
            plugin.getPermissionManager().approvePermission(player, target);
            player.sendMessage(Text.colorize("&aPermission granted to " + target.getName() + "!"));
            target.sendMessage(Text.colorize("&aYour permission request has been approved by " + player.getName() + "!"));
            
        } else {
            // Handle permission request - only OPs can grant permissions
            if (!player.isOp()) {
                player.sendMessage(Text.colorize("&cOnly server operators can grant permissions!"));
                return true;
            }
            
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(Text.colorize("&cPlayer not found!"));
                return true;
            }
            
            if (plugin.getPermissionManager().hasElevatedPermission(target)) {
                player.sendMessage(Text.colorize("&e" + target.getName() + " already has elevated permissions!"));
                return true;
            }
            
            plugin.getPermissionManager().requestPermission(target, player);
            player.sendMessage(Text.colorize("&aPermission request created for " + target.getName() + ". Use '/btc permission accept " + target.getName() + "' to approve."));
            target.sendMessage(Text.colorize("&e" + player.getName() + " has initiated a permission request for you. They need to approve it with '/btc permission accept " + target.getName() + "'."));
        }
        
        return true;
    }
    
    private boolean handleTotalPoint(CommandSender sender, String[] args) {
        if (!sender.hasPermission("btc.use")) {
            sender.sendMessage(Text.getMessage("no_permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Text.colorize("&cUsage: /btc totalpoint <daily|weekly|monthly|yearly> [map|all]"));
            return true;
        }
        
        String period = args[1].toLowerCase();
        String mapKind = (args.length >= 3) ? args[2] : "all";
        
        List<Map.Entry<UUID, Integer>> topPlayers = plugin.getStatisticsManager()
            .getTopTotals(period, mapKind);
        
        sender.sendMessage(Text.colorize("&6&l=== TOP 10 TOTAL POINTS (" + period.toUpperCase() + ") ==="));
        if (!mapKind.equalsIgnoreCase("all")) {
            sender.sendMessage(Text.colorize("&eMap: &6" + mapKind));
        }
        sender.sendMessage("");
        
        for (int i = 0; i < topPlayers.size(); i++) {
            Map.Entry<UUID, Integer> entry = topPlayers.get(i);
            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            sender.sendMessage(Text.colorize("&e" + (i + 1) + ". &6" + playerName + " &7- &a" + entry.getValue() + " points"));
        }
        
        if (topPlayers.isEmpty()) {
            sender.sendMessage(Text.colorize("&7No statistics available yet."));
        }
        
        return true;
    }
    
    private boolean handleBestPoint(CommandSender sender, String[] args) {
        if (!sender.hasPermission("btc.use")) {
            sender.sendMessage(Text.getMessage("no_permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Text.colorize("&cUsage: /btc bestpoint <daily|weekly|monthly|yearly> [map|all]"));
            return true;
        }
        
        String period = args[1].toLowerCase();
        String mapKind = (args.length >= 3) ? args[2] : "all";
        
        List<Map.Entry<UUID, Integer>> topPlayers = plugin.getStatisticsManager()
            .getTopBests(period, mapKind);
        
        sender.sendMessage(Text.colorize("&6&l=== TOP 10 BEST SCORES (" + period.toUpperCase() + ") ==="));
        if (!mapKind.equalsIgnoreCase("all")) {
            sender.sendMessage(Text.colorize("&eMap: &6" + mapKind));
        }
        sender.sendMessage("");
        
        for (int i = 0; i < topPlayers.size(); i++) {
            Map.Entry<UUID, Integer> entry = topPlayers.get(i);
            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            sender.sendMessage(Text.colorize("&e" + (i + 1) + ". &6" + playerName + " &7- &a" + entry.getValue() + " points"));
        }
        
        if (topPlayers.isEmpty()) {
            sender.sendMessage(Text.colorize("&7No statistics available yet."));
        }
        
        return true;
    }
    
    private boolean handlePoints(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.getMessage("player_only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!hasAdminPermission(player)) {
            sender.sendMessage(Text.getMessage("no_permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(Text.colorize("&cUsage: /btc points <player>"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Text.colorize("&cPlayer not found!"));
            return true;
        }
        
        PlayerStats stats = plugin.getStatisticsManager().getPlayerStats(target.getUniqueId());
        
        sender.sendMessage(Text.colorize("&6&l=== " + target.getName() + "'s Statistics ==="));
        sender.sendMessage("");
        
        // Show stats for each time period
        showPeriodStats(sender, stats, "daily");
        showPeriodStats(sender, stats, "weekly");
        showPeriodStats(sender, stats, "monthly");
        showPeriodStats(sender, stats, "yearly");
        
        return true;
    }
    
    private void showPeriodStats(CommandSender sender, PlayerStats stats, String period) {
        sender.sendMessage(Text.colorize("&e&l" + period.toUpperCase() + ":"));
        sender.sendMessage(Text.colorize("  &7Total (All Maps): &a" + stats.getTotalPoints("all", period)));
        sender.sendMessage(Text.colorize("  &7Best Score (All Maps): &a" + stats.getBestScore("all", period)));
        
        // Show per-map stats if available
        for (String mapKind : plugin.getMapManager().getAvailableMapKinds()) {
            int total = stats.getTotalPoints(mapKind, period);
            int best = stats.getBestScore(mapKind, period);
            if (total > 0 || best > 0) {
                sender.sendMessage(Text.colorize("  &7" + mapKind + " - Total: &a" + total + " &7Best: &a" + best));
            }
        }
        sender.sendMessage("");
    }

    private boolean handleTpMap(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.getMessage("player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(Text.colorize("&cUsage: /btc tpmap <mapName> [player]"));
            return true;
        }

        String mapName = args[1];
        Player target = player;

        if (args.length >= 3) {
            Player other = Bukkit.getPlayerExact(args[2]);
            if (other == null) {
                player.sendMessage(Text.colorize("&cPlayer not found: " + args[2]));
                return true;
            }
            target = other;
        }

        // נטען את העולם אם עדיין לא נטען
        if (Bukkit.getWorld(mapName) == null) {
            Bukkit.createWorld(new org.bukkit.WorldCreator(mapName));
        }

        if (Bukkit.getWorld(mapName) == null) {
            player.sendMessage(Text.colorize("&cWorld '" + mapName + "' could not be loaded."));
            return true;
        }

        target.teleport(Bukkit.getWorld(mapName).getSpawnLocation());
        player.sendMessage(Text.colorize("&aTeleported " + target.getName() + " to &e" + mapName));
        return true;
    }
    
    private boolean hasAdminPermission(Player player) {
        return player.hasPermission("btc.admin") || plugin.getPermissionManager().hasElevatedPermission(player);
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Text.colorize("&6&l=== BowTrainChicken Commands ==="));
        sender.sendMessage(Text.colorize("&e/btc start <mapKind> &7- Start a training game"));
        sender.sendMessage(Text.colorize("&e/btc totalpoint <time> [map] &7- View top 10 total points"));
        sender.sendMessage(Text.colorize("&e/btc bestpoint <time> [map] &7- View top 10 best scores"));
        sender.sendMessage("");
        
        if (sender instanceof Player && hasAdminPermission((Player) sender)) {
            sender.sendMessage(Text.colorize("&c&lAdmin & Authorized Commands:"));
            sender.sendMessage(Text.colorize("&e/btc createmap <mapName> &7- Create a new map"));
            sender.sendMessage(Text.colorize("&e/btc deletemap <mapName> &7- Delete a map"));
            sender.sendMessage(Text.colorize("&e/btc tpmap <mapName> [player] &7- Teleport to a map"));
            sender.sendMessage(Text.colorize("&e/btc setmapspawn <mapName> &7- Set map spawn point"));
            sender.sendMessage(Text.colorize("&e/btc setchickenpoint <mapKind> <points> &7- Set chicken points"));
            sender.sendMessage(Text.colorize("&e/btc setchickenspawnarea <mapKind> <x1> <y1> <z1> <x2> <y2> <z2> &7- Set spawn area"));
            sender.sendMessage(Text.colorize("&e/btc points <player> &7- View player statistics"));
        }
        
        if (sender instanceof Player && ((Player) sender).isOp()) {
            sender.sendMessage("");
            sender.sendMessage(Text.colorize("&4&lOP Only Commands:"));
            sender.sendMessage(Text.colorize("&e/btc permission <player> &7- Grant permission to player"));
            sender.sendMessage(Text.colorize("&e/btc permission accept <player> &7- Approve permission request"));
        }
        
        sender.sendMessage("");
        sender.sendMessage(Text.colorize("&7Time periods: daily, weekly, monthly, yearly"));
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // שלב ראשון: הפקודה הראשונה אחרי /btc
        if (args.length == 1) {
            completions.addAll(Arrays.asList(
                    "start",
                    "createmap",
                    "deletemap",
                    "tpmap",
                    "setmapspawn",
                    "setchickenpoint",
                    "setchickenspawnarea",
                    "permission",
                    "totalpoint",
                    "bestpoint",
                    "points",
                    "help"
            ));
        }

        // שלב שני: TAB אחרי תתי פקודות
        else if (args.length == 2) {
            String sub = args[0].toLowerCase();

            switch (sub) {
                case "start":
                case "setmapspawn":
                case "setchickenpoint":
                case "setchickenspawnarea":
                case "tpmap":
                case "deletemap":
                    // שמות מפות קיימות
                    completions.addAll(plugin.getMapManager().getAvailableMapKinds());
                    break;

                case "createmap":
                    // אין הצעות – המשתמש בוחר שם חדש
                    break;

                case "permission":
                    completions.add("accept");
                    completions.add("deny");
                    break;

                case "totalpoint":
                case "bestpoint":
                    completions.addAll(Arrays.asList("daily", "weekly", "monthly", "yearly"));
                    break;

                case "points":
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()));
                    break;
            }
        }

        // שלב שלישי: TAB בארגומנט השלישי
        else if (args.length == 3) {
            String sub = args[0].toLowerCase();

            if (sub.equals("permission") && args[1].equalsIgnoreCase("accept")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
            }

            if (sub.equals("totalpoint") || sub.equals("bestpoint")) {
                completions.add("all");
                completions.addAll(plugin.getMapManager().getAvailableMapKinds());
            }
        }

        // סינון לפי מה שהשחקן כבר התחיל להקליד
        return completions.stream()
                .filter(c -> c.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

}