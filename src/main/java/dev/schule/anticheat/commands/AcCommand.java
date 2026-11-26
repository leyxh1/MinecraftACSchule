package dev.schule.anticheat.commands;

import dev.schule.anticheat.AntiCheatPlugin;
import dev.schule.anticheat.KickLog;
import dev.schule.anticheat.ViolationManager;
import dev.schule.anticheat.checks.Check;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Der Befehl /ac:
 *   /ac status <Spieler>  - zeigt die aktuellen Violation-Level (auch nach einem Kick)
 *   /ac reset <Spieler>   - setzt alle Violation-Level eines Spielers auf 0
 *   /ac kicks [Spieler]   - letzte Kicks durch das Anticheat, mit [Bannen]-Knopf
 *   /ac alerts            - eigene Meldungen an/aus
 *   /ac reload            - config.yml neu laden
 */
public class AcCommand implements CommandExecutor, TabCompleter {

    /** Breite der Balkenanzeige in /ac status */
    private static final int BAR_LENGTH = 20;
    /** So viele Kicks zeigt /ac kicks an */
    private static final int KICK_LIST_LENGTH = 10;
    private static final DateTimeFormatter KICK_TIME = DateTimeFormatter.ofPattern("dd.MM. HH:mm");

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
            case "reset" -> reset(sender, args);
            case "kicks" -> kicks(sender, args);
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
        OfflinePlayer target = findPlayer(sender, args, "/ac status <Spieler>");
        if (target == null) {
            return;
        }
        UUID id = target.getUniqueId();
        ViolationManager vm = plugin.getViolationManager();

        sender.sendMessage(Component.text("===== Anticheat-Status: " + target.getName() + " =====", NamedTextColor.GOLD));
        for (Check check : plugin.getChecks()) {
            double vl = vm.getVl(id, check.getName());
            int kickVl = check.getKickVl();
            boolean max = vm.isMaxed(id, check.getName());

            // Farbe: grün = sauber, gelb = auffällig, rot = halb voll oder mehr / Maximum
            NamedTextColor color = max ? NamedTextColor.DARK_RED
                    : vl == 0 ? NamedTextColor.GREEN
                    : vl < kickVl / 2.0 ? NamedTextColor.YELLOW : NamedTextColor.RED;

            Component line = Component.text(" " + padRight(check.getDisplayName(), 11), NamedTextColor.GRAY)
                    .append(bar(vl, kickVl, color))
                    .append(Component.text(" " + ViolationManager.format(vl) + " / " + kickVl, color));
            if (max) {
                line = line.append(Component.text(" MAX", NamedTextColor.DARK_RED, TextDecoration.BOLD));
            }
            if (!check.isEnabled()) {
                line = line.append(Component.text(" (aus)", NamedTextColor.DARK_GRAY));
            }
            sender.sendMessage(line);
        }

        Player online = target.getPlayer();
        sender.sendMessage(Component.text(online != null
                ? " Online, Ping: " + online.getPing() + " ms"
                : " Offline (z. B. gekickt)", NamedTextColor.GRAY));
    }

    private void reset(CommandSender sender, String[] args) {
        OfflinePlayer target = findPlayer(sender, args, "/ac reset <Spieler>");
        if (target == null) {
            return;
        }
        plugin.getViolationManager().reset(target.getUniqueId());
        sender.sendMessage(Component.text("Alle Violation-Level von " + target.getName() + " auf 0 gesetzt.",
                NamedTextColor.GREEN));
    }

    /**
     * /ac kicks [Spieler]: die letzten Kicks durch das Anticheat (neueste zuerst),
     * jeweils mit [Bannen]-Knopf.
     */
    private void kicks(CommandSender sender, String[] args) {
        String player = args.length >= 2 ? args[1] : null;
        List<KickLog.Entry> entries = plugin.getKickLog().latest(player, KICK_LIST_LENGTH);

        String title = player == null ? "Letzte Kicks" : "Kicks von " + player;
        sender.sendMessage(Component.text("===== " + title + " =====", NamedTextColor.GOLD));
        if (entries.isEmpty()) {
            sender.sendMessage(Component.text(" Keine Kicks gespeichert.", NamedTextColor.GREEN));
            return;
        }
        for (KickLog.Entry entry : entries) {
            sender.sendMessage(Component.text(" " + entry.time().format(KICK_TIME) + " ", NamedTextColor.GRAY)
                    .append(Component.text(entry.player(), NamedTextColor.WHITE))
                    .append(Component.text(" - " + entry.check() + " (" + entry.vl() + ") ", NamedTextColor.RED))
                    .append(ViolationManager.banButton(entry.player(), entry.check())));
        }
        sender.sendMessage(Component.text(" Alle Kicks: plugins/SchoolAntiCheat/kicks.log", NamedTextColor.DARK_GRAY));
    }

    /** Sucht einen Spieler - online oder schon einmal auf dem Server gewesen (z. B. nach einem Kick). */
    private OfflinePlayer findPlayer(CommandSender sender, String[] args, String usage) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Benutzung: " + usage, NamedTextColor.RED));
            return null;
        }
        OfflinePlayer target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            target = Bukkit.getOfflinePlayerIfCached(args[1]);
        }
        if (target == null) {
            sender.sendMessage(Component.text("Spieler " + args[1] + " war noch nie auf dem Server.", NamedTextColor.RED));
        }
        return target;
    }

    /** Balken wie [||||||||||          ], damit man den Stand auf einen Blick sieht. */
    private static Component bar(double vl, int kickVl, NamedTextColor color) {
        int filled = (int) Math.round(Math.min(vl / kickVl, 1.0) * BAR_LENGTH);
        return Component.text("[", NamedTextColor.DARK_GRAY)
                .append(Component.text("|".repeat(filled), color))
                .append(Component.text("|".repeat(BAR_LENGTH - filled), NamedTextColor.DARK_GRAY))
                .append(Component.text("]", NamedTextColor.DARK_GRAY));
    }

    private static String padRight(String text, int length) {
        return text.length() >= length ? text : text + " ".repeat(length - text.length());
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
        sender.sendMessage(Component.text(" /ac reset <Spieler>", NamedTextColor.YELLOW)
                .append(Component.text(" - Violation-Level auf 0 setzen", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text(" /ac kicks [Spieler]", NamedTextColor.YELLOW)
                .append(Component.text(" - letzte Kicks (mit [Bannen]-Knopf)", NamedTextColor.GRAY)));
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
            for (String sub : List.of("status", "reset", "kicks", "alerts", "reload")) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    result.add(sub);
                }
            }
        } else if (args.length == 2
                && List.of("status", "reset", "kicks").contains(args[0].toLowerCase())) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    result.add(online.getName());
                }
            }
        }
        return result;
    }
}
