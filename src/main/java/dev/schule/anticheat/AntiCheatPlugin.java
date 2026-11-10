package dev.schule.anticheat;

import dev.schule.anticheat.checks.AutoClickerCheck;
import dev.schule.anticheat.checks.Check;
import dev.schule.anticheat.checks.FlyCheck;
import dev.schule.anticheat.checks.KillAuraCheck;
import dev.schule.anticheat.checks.NoFallCheck;
import dev.schule.anticheat.checks.ReachCheck;
import dev.schule.anticheat.checks.SpeedCheck;
import dev.schule.anticheat.commands.AcCommand;
import dev.schule.anticheat.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hauptklasse des Plugins. Paper erzeugt genau ein Objekt davon,
 * sobald der Server startet (siehe "main" in plugin.yml).
 */
public class AntiCheatPlugin extends JavaPlugin {

    /** Alle Checks, die bei jedem Ereignis gefragt werden. */
    private final List<Check> checks = new ArrayList<>();
    /** Gespeicherte Daten pro Spieler (siehe PlayerData). */
    private final Map<UUID, PlayerData> playerData = new HashMap<>();
    private ViolationManager violationManager;

    /** Wird aufgerufen, wenn das Plugin geladen und aktiviert wird. */
    @Override
    public void onEnable() {
        // config.yml anlegen, falls es sie noch nicht gibt
        saveDefaultConfig();

        violationManager = new ViolationManager(this);

        // Alle Checks registrieren
        checks.add(new FlyCheck(this));
        checks.add(new SpeedCheck(this));
        checks.add(new NoFallCheck(this));
        checks.add(new ReachCheck(this));
        checks.add(new KillAuraCheck(this));
        checks.add(new AutoClickerCheck(this));

        // Ereignisse (Bewegung, Angriffe, ...) an unseren Listener weiterleiten
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        // Befehl /ac
        AcCommand command = new AcCommand(this);
        PluginCommand ac = getCommand("ac");
        if (ac != null) {
            ac.setExecutor(command);
            ac.setTabCompleter(command);
        }

        // Jede Sekunde (20 Ticks) die VL etwas abbauen
        Bukkit.getScheduler().runTaskTimer(this, violationManager::decay, 20L, 20L);

        // Alle 10 Ticks die zeitgesteuerten Prüfungen (z. B. Schweben) ausführen
        Bukkit.getScheduler().runTaskTimer(this, this::tickChecks, 10L, 10L);

        // Falls das Plugin bei laufendem Server neu geladen wird: Daten für alle Online-Spieler anlegen
        for (Player player : Bukkit.getOnlinePlayers()) {
            getData(player);
        }

        getLogger().info("SchoolAntiCheat gestartet (Version " + getPluginMeta().getVersion()
                + ", " + checks.size() + " Checks)");
    }

    /** Wird aufgerufen, wenn der Server stoppt oder das Plugin deaktiviert wird. */
    @Override
    public void onDisable() {
        getLogger().info("SchoolAntiCheat gestoppt.");
    }

    private void tickChecks() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = getData(player);
            for (Check check : checks) {
                if (check.isEnabled()) {
                    check.onTick(player, data);
                }
            }
        }
    }

    /** Daten eines Spielers holen (werden beim ersten Mal angelegt). */
    public PlayerData getData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), id -> new PlayerData());
    }

    /** Daten eines Spielers löschen (beim Verlassen des Servers). */
    public void removeData(Player player) {
        playerData.remove(player.getUniqueId());
        violationManager.reset(player.getUniqueId());
    }

    public List<Check> getChecks() {
        return Collections.unmodifiableList(checks);
    }

    /** Check anhand seines Namens aus der config.yml finden, z. B. "speed". */
    public Check getCheck(String name) {
        for (Check check : checks) {
            if (check.getName().equalsIgnoreCase(name)) {
                return check;
            }
        }
        return null;
    }

    public ViolationManager getViolationManager() {
        return violationManager;
    }
}
