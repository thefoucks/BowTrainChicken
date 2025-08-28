package com.bowtrain.commands;

import com.bowtrain.BowTrainChicken;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BTCCompleter implements TabCompleter {

    private final BowTrainChicken plugin;

    public BTCCompleter(BowTrainChicken plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("start", "totalpoint", "bestpoint", "help"));
            if (sender instanceof Player && hasAdminPermission((Player) sender)) {
                completions.addAll(Arrays.asList(
                        "createmap", "deletemap", "tpmap",
                        "setmapspawn", "setchickenpoint", "setchickenspawnarea", "points"
                ));
            }
            if (sender instanceof Player && ((Player) sender).isOp()) {
                completions.add("permission");
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "start":
                case "setmapspawn":
                case "setchickenpoint":
                case "setchickenspawnarea":
                    completions.addAll(plugin.getMapManager().getAvailableMapKinds());
                    break;
                case "deletemap":
                case "tpmap":
                    completions.addAll(
                            Bukkit.getWorlds().stream()
                                    .map(World::getName)
                                    .collect(Collectors.toList())
                    );
                    break;
                case "createmap":
                    // לא מציע כלום כדי לא לבלבל
                    break;
                case "permission":
                    if (sender instanceof Player && ((Player) sender).isOp()) {
                        completions.add("accept");
                        completions.addAll(Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .collect(Collectors.toList()));
                    }
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
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("permission") && args[1].equalsIgnoreCase("accept")) {
                if (sender instanceof Player && ((Player) sender).isOp()) {
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()));
                }
            } else if (subCommand.equals("totalpoint") || subCommand.equals("bestpoint")) {
                completions.add("all");
                completions.addAll(plugin.getMapManager().getAvailableMapKinds());
            } else if (subCommand.equals("tpmap")) {
                // מציע שחקנים לשיגור
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
            }
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private boolean hasAdminPermission(Player player) {
        return player.hasPermission("btc.admin") || plugin.getPermissionManager().hasElevatedPermission(player);
    }
}
