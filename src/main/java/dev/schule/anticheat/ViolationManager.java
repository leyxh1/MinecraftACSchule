package dev.schule.anticheat;

import dev.schule.anticheat.checks.Check;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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
    /**
     * Checks, die beim Spieler schon den Höchstwert (kick-vl) erreicht haben.
     * Diese VL bleiben auf Maximum stehen (kein Abbau), auch nach Kick oder Rejoin,
     * bis ein Admin sie mit /ac reset zurücksetzt oder der Server neu startet.
     */
    private final Map<UUID, Set<String>> maxed = new HashMap<>();
    /** Admins, die die Meldungen mit /ac alerts ausgeschaltet haben */
    private final Set<UUID> alertsOff = new HashSet<>();
    private final Path logFile;

    public ViolationManager(AntiCheatPlugin plugin) {
        this.plugin = plugin;
        this.logFile = plugin.getDataFolder().toPath().resolve("violations.log");
    }

    /** Ein Check hat einen Verstoß erkannt. */
    public void flag(Player player, Check check, double amount, String details) {
        // 1) VL erhöhen - aber nie über den Höchstwert (kick-vl) hinaus
        Map<String, Double> playerVl = violations.computeIfAbsent(player.getUniqueId(), id -> new HashMap<>());
        int kickVl = check.getKickVl();
        double vl = Math.min(playerVl.getOrDefault(check.getName(), 0.0) + amount, kickVl);
        playerVl.put(check.getName(), vl);

        // Höchstwert erreicht? Dann bleibt der Check ab jetzt auf Maximum stehen
        if (vl >= kickVl && plugin.getConfig().getBoolean("violations.lock-at-max", true)) {
            maxed.computeIfAbsent(player.getUniqueId(), id -> new HashSet<>()).add(check.getName());
        }

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
            kick(player, check, vl);
        }
    }

    private void kick(Player player, Check check, double vl) {
        // Ohne lock-at-max fängt der Spieler nach dem Kick wieder bei 0 an
        if (!plugin.getConfig().getBoolean("violations.lock-at-max", true)) {
            violations.remove(player.getUniqueId());
        }
        String message = plugin.getConfig()
                .getString("kick.message", "Du wurdest vom Anticheat gekickt ({check}).")
                .replace("{check}", check.getDisplayName());
        // Meldung an Admins mit anklickbarem [Bannen]-Knopf (schlägt den Befehl nur vor -
        // ob wirklich gebannt wird, entscheidet der Admin)
        sendAlert(Component.text(player.getName() + " wurde gekickt (" + check.getDisplayName() + ") ",
                        NamedTextColor.RED)
                .append(banButton(player.getName(), check.getDisplayName())));
        writeLog("KICK", player.getName() + " -> " + check.getDisplayName());
        // In die Kick-Liste eintragen (/ac kicks, Datei kicks.log)
        plugin.getKickLog().add(player.getName(), check.getDisplayName(),
                "VL " + format(vl) + "/" + check.getKickVl());
        // Kick erst im nächsten Tick, damit das laufende Ereignis sauber zu Ende läuft
        Bukkit.getScheduler().runTask(plugin, () -> player.kick(Component.text(message, NamedTextColor.RED)));
    }

    /** Wird jede Sekunde aufgerufen: alle VL ein Stück verringern. */
    public void decay() {
        Iterator<Map.Entry<UUID, Map<String, Double>>> players = violations.entrySet().iterator();
        while (players.hasNext()) {
            Map.Entry<UUID, Map<String, Double>> playerEntry = players.next();
            Map<String, Double> playerVl = playerEntry.getValue();
            Set<String> lockedChecks = maxed.getOrDefault(playerEntry.getKey(), Set.of());
            Iterator<Map.Entry<String, Double>> entries = playerVl.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Double> entry = entries.next();
                if (lockedChecks.contains(entry.getKey())) {
                    continue;   // auf Maximum eingefroren -> kein Abbau
                }
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

    /** Hat der Spieler bei diesem Check schon den Höchstwert erreicht? */
    public boolean isMaxed(UUID player, String checkName) {
        return maxed.getOrDefault(player, Set.of()).contains(checkName);
    }

    /** Alle VL eines Spielers löschen (für /ac reset). */
    public void reset(UUID player) {
        violations.remove(player);
        maxed.remove(player);
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

    /**
     * Anklickbarer Text [Bannen]: Beim Klick steht der Ban-Befehl im Chat,
     * der Admin muss ihn nur noch mit Enter bestätigen.
     */
    public static Component banButton(String playerName, String checkName) {
        String command = "/ban " + playerName + " Cheating (" + checkName + ")";
        return Component.text("[Bannen]", NamedTextColor.DARK_RED)
                .clickEvent(ClickEvent.suggestCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text("Klicken: " + command, NamedTextColor.GRAY)));
    }

    public static String format(double value) {
        return String.format(Locale.GERMANY, "%.1f", value);
    }
}
