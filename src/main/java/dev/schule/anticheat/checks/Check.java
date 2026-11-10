package dev.schule.anticheat.checks;

import dev.schule.anticheat.AntiCheatPlugin;
import dev.schule.anticheat.data.Movement;
import dev.schule.anticheat.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Basisklasse für alle Checks.
 * Jeder Check (Fly, Speed, ...) "erbt" von dieser Klasse und überschreibt
 * nur die Methoden, die er braucht (onMove, onAttack, onSwing, onTick).
 */
public abstract class Check {

    protected final AntiCheatPlugin plugin;
    private final String name;         // Name in der config.yml, z. B. "speed"
    private final String displayName;  // Name in Meldungen, z. B. "Speed"

    protected Check(AntiCheatPlugin plugin, String name, String displayName) {
        this.plugin = plugin;
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    // ---------- Werte aus der config.yml lesen ----------

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean(path("enabled"), true);
    }

    public int getKickVl() {
        return plugin.getConfig().getInt(path("kick-vl"), 20);
    }

    public double getDecay() {
        return num("decay-per-second", 1.0);
    }

    /** Liest eine Zahl aus checks.<name>.<key> in der config.yml. */
    protected double num(String key, double defaultValue) {
        return plugin.getConfig().getDouble(path(key), defaultValue);
    }

    private String path(String key) {
        return "checks." + name + "." + key;
    }

    // ---------- Verstoß melden ----------

    /**
     * Meldet einen Verstoß an den ViolationManager.
     * Damit der Chat nicht vollgespammt wird, gibt es eine Pause (flag-cooldown-ticks).
     */
    protected void flag(Player player, PlayerData data, double amount, String details) {
        long cooldown = (long) num("flag-cooldown-ticks", 10);
        Long last = data.lastFlagTick.get(name);
        if (last != null && PlayerData.ticksSince(last) < cooldown) {
            return;
        }
        data.lastFlagTick.put(name, PlayerData.now());
        plugin.getViolationManager().flag(player, this, amount, details);
    }

    /**
     * Allgemeine Ausnahmen, bei denen KEIN Check prüft:
     * Kreativ/Zuschauer, Fliegen erlaubt, in einem Fahrzeug, tot,
     * gerade gejoint (2 s) oder gerade teleportiert (1 s).
     */
    protected boolean isExempt(Player player, PlayerData data) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR
                || player.getAllowFlight() || player.isFlying()
                || player.isInsideVehicle() || player.isDead()
                || PlayerData.ticksSince(data.joinTick) < 40
                || PlayerData.ticksSince(data.lastTeleportTick) < 20;
    }

    /** Zahl schön formatieren, z. B. 0,62 */
    protected static String fmt(double value) {
        return String.format(Locale.GERMANY, "%.2f", value);
    }

    // ---------- Ereignisse: werden vom EventListener aufgerufen ----------

    /** Spieler hat sich bewegt. */
    public void onMove(Player player, PlayerData data, Movement move) {
    }

    /** Spieler hat ein Wesen angegriffen. */
    public void onAttack(Player player, PlayerData data, Entity target) {
    }

    /** Spieler hat mit dem Arm geschwungen (Linksklick). */
    public void onSwing(Player player, PlayerData data) {
    }

    /** Wird alle 10 Ticks (0,5 s) für jeden Spieler aufgerufen. */
    public void onTick(Player player, PlayerData data) {
    }
}
