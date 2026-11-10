package dev.schule.anticheat;

import dev.schule.anticheat.checks.Check;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Verwaltet die Violation-Level (VL) aller Spieler.
 * Aufgaben: VL hochzählen, Admins warnen, in die Logdatei schreiben, kicken, VL wieder abbauen.
 */
public class ViolationManager {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AntiCheatPlugin plugin;
    /** Spieler-UUID -> (Check-Name -> Violation-Level) */
    private final Map<UUID, Map<String, Double>> violations = new HashMap<>();
    /** Admins, die die Meldungen mit /ac alerts ausgeschaltet haben */
    private final Set<UUID> alertsOff = new HashSet<>();
    private final Path logFile;

    public ViolationManager(AntiCheatPlugin plugin) {
        this.plugin = plugin;
        this.logFile = plugin.getDataFolder().toPath().resolve("violations.log");
    }

    /** Ein Check hat einen Verstoß erkannt. */
    public void flag(Player player, Check check, double amount, String details) {
        // 1) VL erhöhen
        Map<String, Double> playerVl = violations.computeIfAbsent(player.getUniqueId(), id -> new HashMap<>());
        double vl = playerVl.getOrDefault(check.getName(), 0.0) + amount;
        playerVl.put(check.getName(), vl);
        int kickVl = check.getKickVl();

        // 2) Admins warnen
        String text = player.getName() + " -> " + check.getDisplayName()
                + " (VL " + format(vl) + "/" + kickVl + ") " + details;
        sendAlert(Component.text(player.getName(), NamedTextColor.WHITE)
                .append(Component.text(" verdächtig: ", NamedTextColor.GRAY))
                .append(Component.text(check.getDisplayName(), NamedTextColor.RED))
                .append(Component.text(" (VL " + format(vl) + "/" + kickVl + ") ", NamedTextColor.YELLOW))
                .append(Component.text(details, NamedTextColor.GRAY)));

        // 3) Logdatei
        writeLog("FLAG", text);

        // 4) Kicken, wenn der Grenzwert erreicht ist
        if (plugin.getConfig().getBoolean("kick.enabled", true) && vl >= kickVl) {
            kick(player, check);
        }
    }

    private void kick(Player player, Check check) {
        violations.remove(player.getUniqueId());  // nach dem Kick fängt er wieder bei 0 an
        String message = plugin.getConfig()
                .getString("kick.message", "Du wurdest vom Anticheat gekickt ({check}).")
                .replace("{check}", check.getDisplayName());
        sendAlert(Component.text(player.getName() + " wurde gekickt (" + check.getDisplayName() + ")",
                NamedTextColor.RED));
        writeLog("KICK", player.getName() + " -> " + check.getDisplayName());
        // Kick erst im nächsten Tick, damit das laufende Ereignis sauber zu Ende läuft
        Bukkit.getScheduler().runTask(plugin, () -> player.kick(Component.text(message, NamedTextColor.RED)));
    }

    /** Wird jede Sekunde aufgerufen: alle VL ein Stück verringern. */
    public void decay() {
        Iterator<Map<String, Double>> players = violations.values().iterator();
        while (players.hasNext()) {
            Map<String, Double> playerVl = players.next();
            Iterator<Map.Entry<String, Double>> entries = playerVl.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Double> entry = entries.next();
                Check check = plugin.getCheck(entry.getKey());
                double decay = check != null ? check.getDecay() : 1.0;
                double newVl = entry.getValue() - decay;
                if (newVl <= 0) {
                    entries.remove();
                } else {
                    entry.setValue(newVl);
                }
            }
            if (playerVl.isEmpty()) {
                players.remove();
            }
        }
    }

    /** Aktuelles VL eines Spielers für einen Check (0, wenn keins). */
    public double getVl(UUID player, String checkName) {
        Map<String, Double> playerVl = violations.get(player);
        return playerVl == null ? 0.0 : playerVl.getOrDefault(checkName, 0.0);
    }

    public void reset(UUID player) {
        violations.remove(player);
    }

    /** Schaltet die Meldungen für einen Admin an/aus. Gibt true zurück, wenn sie jetzt AN sind. */
    public boolean toggleAlerts(UUID admin) {
        if (alertsOff.remove(admin)) {
            return true;
        }
        alertsOff.add(admin);
        return false;
    }

    /** Schickt eine Meldung an alle Admins und in die Konsole. */
    private void sendAlert(Component message) {
        if (!plugin.getConfig().getBoolean("alerts.enabled", true)) {
            return;
        }
        Component full = Component.text("[AC] ", NamedTextColor.DARK_RED).append(message);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("schoolanticheat.alerts") && !alertsOff.contains(online.getUniqueId())) {
                online.sendMessage(full);
            }
        }
        Bukkit.getConsoleSender().sendMessage(full);
    }

    /** Hängt eine Zeile an violations.log an (ohne IP-Adressen - Datensparsamkeit). */
    private void writeLog(String type, String text) {
        if (!plugin.getConfig().getBoolean("log.enabled", true)) {
            return;
        }
        String line = LocalDateTime.now().format(TIME) + " | " + type + " | " + text + System.lineSeparator();
        try {
            Files.createDirectories(logFile.getParent());
            Files.writeString(logFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte nicht in violations.log schreiben: " + e.getMessage());
        }
    }

    public static String format(double value) {
        return String.format(Locale.GERMANY, "%.1f", value);
    }
}
