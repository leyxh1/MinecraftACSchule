package dev.schule.anticheat.replay;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Locale;

/**
 * Ein einzelnes Bild (Frame) einer Aufnahme: Wo war der Spieler in diesem Tick,
 * wohin hat er geschaut, was hat er gemacht?
 *
 * @param world Name der Welt
 * @param x     Position
 * @param y     Position
 * @param z     Position
 * @param yaw   Blickrichtung links/rechts
 * @param pitch Blickrichtung hoch/runter
 * @param state Zustände als Bits (siehe SNEAK, SPRINT, ...)
 * @param flag  Anticheat-Meldung in diesem Tick, sonst ""
 */
public record ReplayFrame(String world, double x, double y, double z,
                          float yaw, float pitch, int state, String flag) {

    // Jeder Zustand ist ein Bit in "state" - so passen mehrere Zustände in eine Zahl
    public static final int SNEAK = 1;
    public static final int SPRINT = 2;
    public static final int GLIDE = 4;
    public static final int SWING = 8;
    public static final int ATTACK = 16;

    /** Ist dieser Zustand in diesem Frame gesetzt? */
    public boolean has(int bit) {
        return (state & bit) != 0;
    }

    /** Position als Bukkit-Location (null, wenn die Welt nicht mehr existiert). */
    public Location toLocation() {
        World w = Bukkit.getWorld(world);
        return w == null ? null : new Location(w, x, y, z, yaw, pitch);
    }

    /** Als Textzeile für die Datei, Werte durch ; getrennt. */
    public String toLine() {
        String safeFlag = flag.replace(";", ",").replace("\n", " ").replace("\r", " ");
        return String.format(Locale.ROOT, "%s;%.3f;%.3f;%.3f;%.1f;%.1f;%d;%s",
                world, x, y, z, yaw, pitch, state, safeFlag);
    }

    /** Textzeile aus der Datei wieder in einen Frame umwandeln. */
    public static ReplayFrame fromLine(String line) {
        String[] p = line.split(";", 8);
        return new ReplayFrame(p[0],
                Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]),
                Float.parseFloat(p[4]), Float.parseFloat(p[5]),
                Integer.parseInt(p[6]), p.length > 7 ? p[7] : "");
    }
}
