package dev.schule.anticheat.data;

import org.bukkit.Bukkit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Alles, was sich das Anticheat über EINEN Spieler merkt.
 * Für jeden Spieler auf dem Server gibt es genau ein PlayerData-Objekt.
 *
 * Zeitpunkte speichern wir in Server-Ticks (20 Ticks = 1 Sekunde).
 * -1000 bedeutet "ist lange her / noch nie passiert".
 */
public class PlayerData {

    // ---------- Zeitpunkte für Ausnahmen ----------
    public long joinTick = now();              // Wann ist der Spieler gejoint?
    public long lastTeleportTick = now();      // Teleport, Respawn, Weltwechsel
    public long lastVelocityTick = -1000;      // Knockback / Rückstoß bekommen
    public double lastVelocityHorizontal;      // Stärke des letzten Rückstoßes
    public long lastSpecialBlockTick = -1000;  // Wasser, Leiter, Spinnennetz, Slime ...
    public long lastIceTick = -1000;           // auf Eis gelaufen
    public long lastHeadBlockedTick = -1000;   // Block über dem Kopf
    public long lastGlideTick = -1000;         // mit Elytra geflogen
    public long lastFallDamageTick = -1000;    // Fallschaden bekommen
    public long lastMoveTick = now();          // Position hat sich verändert
    public long lastDigTick = -1000;           // angefangen, einen Block abzubauen
    public boolean digging;                    // baut gerade einen Block ab

    // ---------- Bewegung (vom Server berechnet) ----------
    public boolean wasOnGround = true;         // stand beim letzten Schritt auf festem Boden
    public int airTicks;                       // so viele Schritte schon in der Luft
    public int groundTicks;                    // so viele Schritte schon auf dem Boden
    public double lastDeltaY;                  // letzte Höhenänderung
    public double highestY;                    // höchster Punkt seit dem letzten Bodenkontakt

    // ---------- Puffer: zählen Auffälligkeiten, bevor gemeldet wird ----------
    public double flyBuffer;
    public double speedBuffer;
    public double reachBuffer;
    public double killAuraBuffer;

    // ---------- Kampf ----------
    public float lastYawChange;                // letzte Kopfdrehung (Grad)
    public long lastRotationTick = -1000;
    public final Deque<long[]> recentTargets = new ArrayDeque<>();  // {Entity-ID, Tick}
    public final Deque<Long> clickTimes = new ArrayDeque<>();       // Klick-Zeitpunkte (ms)
    public final Deque<Long> clickIntervals = new ArrayDeque<>();   // Abstände zwischen Klicks (ms)
    public long lastClickMillis;

    // ---------- Wann wurde welcher Check zuletzt gemeldet? ----------
    public final Map<String, Long> lastFlagTick = new HashMap<>();

    /** Aktueller Server-Tick. */
    public static long now() {
        return Bukkit.getCurrentTick();
    }

    /** Wie viele Ticks ist dieser Zeitpunkt her? */
    public static long ticksSince(long tick) {
        return now() - tick;
    }
}
