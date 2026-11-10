package dev.schule.anticheat.commands;

import dev.schule.anticheat.AntiCheatPlugin;
import dev.schule.anticheat.ViolationManager;
import dev.schule.anticheat.checks.Check;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Der Befehl /ac:
 *   /ac status <Spieler>  - zeigt die aktuellen Violation-Level
 *   /ac alerts            - eigene Meldungen an/aus
 *   /ac reload            - config.yml neu laden
 */
public class AcCommand implements CommandExecutor, TabCompleter {

    private final AntiCheatPlugin plugin;

    public AcCommand(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "status" -> status(sender, args);
            case "alerts" -> alerts(sender);
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(Component.text("config.yml neu geladen.", NamedTextColor.GREEN));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void status(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Benutzung: /ac status <Spieler>", NamedTextColor.RED));
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Spieler " + args[1] + " ist nicht online.", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("===== Anticheat-Status: " + target.getName() + " =====", NamedTextColor.GOLD));
        ViolationManager vm = plugin.getViolationManager();
        for (Check check : plugin.getChecks()) {
            double vl = vm.getVl(target.getUniqueId(), check.getName());
            int kickVl = check.getKickVl();
            // Farbe: grün = sauber, gelb = auffällig, rot = kurz vor dem Kick
            NamedTextColor color = vl == 0 ? NamedTextColor.GREEN
                    : vl < kickVl / 2.0 ? NamedTextColor.YELLOW : NamedTextColor.RED;
            String state = check.isEnabled() ? "" : " (aus)";
            sender.sendMessage(Component.text(" " + check.getDisplayName() + state + ": ", NamedTextColor.GRAY)
                    .append(Component.text(ViolationManager.format(vl) + " / " + kickVl, color)));
        }
        sender.sendMessage(Component.text(" Ping: " + target.getPing() + " ms", NamedTextColor.GRAY));
    }

    private void alerts(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Nur für Spieler.", NamedTextColor.RED));
            return;
        }
        boolean on = plugin.getViolationManager().toggleAlerts(player.getUniqueId());
        sender.sendMessage(Component.text("Anticheat-Meldungen: " + (on ? "AN" : "AUS"),
                on ? NamedTextColor.GREEN : NamedTextColor.RED));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("SchoolAntiCheat-Befehle:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text(" /ac status <Spieler>", NamedTextColor.YELLOW)
                .append(Component.text(" - Violation-Level anzeigen", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text(" /ac alerts", NamedTextColor.YELLOW)
                .append(Component.text(" - eigene Meldungen an/aus", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text(" /ac reload", NamedTextColor.YELLOW)
                .append(Component.text(" - config.yml neu laden", NamedTextColor.GRAY)));
    }

    /** Vorschläge beim Drücken der Tab-Taste. */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : List.of("status", "alerts", "reload")) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    result.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("status")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    result.add(online.getName());
                }
            }
        }
        return result;
    }
}
