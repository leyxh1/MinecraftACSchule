package dev.schule.anticheat.checks;

import dev.schule.anticheat.AntiCheatPlugin;
import dev.schule.anticheat.data.PlayerData;
import org.bukkit.entity.Player;

/**
 * Autoclicker-Check: Erkennt zu viele oder zu gleichmäßige Klicks.
 *
 *  1. Klickrate (CPS = Klicks pro Sekunde): Mehr als ~20 schafft kaum ein Mensch.
 *  2. Gleichmäßigkeit: Menschen klicken nie exakt im selben Takt.
 *     Wir berechnen die Standardabweichung der Abstände zwischen den Klicks.
 *     Ist sie fast 0, klickt ein Programm.
 */
public class AutoClickerCheck extends Check {

    /** So viele Klick-Abstände werden für die Gleichmäßigkeit ausgewertet. */
    private static final int SAMPLES = 20;

    public AutoClickerCheck(AntiCheatPlugin plugin) {
        super(plugin, "autoclicker", "AutoClicker");
    }

    @Override
    public void onSwing(Player player, PlayerData data) {
        // Beim Abbauen eines Blocks schwingt der Arm dauernd - das ist kein Klicken
        if (data.digging && PlayerData.ticksSince(data.lastDigTick) < 400) {
            return;
        }
        long now = System.currentTimeMillis();

        // --- 1) Klicks der letzten Sekunde zählen ---
        data.clickTimes.addLast(now);
        while (!data.clickTimes.isEmpty() && now - data.clickTimes.peekFirst() > 1000) {
            data.clickTimes.removeFirst();
        }
        int cps = data.clickTimes.size();
        if (cps > num("max-cps", 20)) {
            flag(player, data, 1, cps + " Klicks pro Sekunde");
        }

        // --- 2) Gleichmäßigkeit der Abstände ---
        long interval = now - data.lastClickMillis;
        data.lastClickMillis = now;
        if (interval > 500) {
            data.clickIntervals.clear();   // Pause -> neu anfangen
            return;
        }
        data.clickIntervals.addLast(interval);
        if (data.clickIntervals.size() > SAMPLES) {
            data.clickIntervals.removeFirst();
        }
        if (data.clickIntervals.size() == SAMPLES && cps >= num("min-cps-for-consistency", 8)) {
            double deviation = standardDeviation(data);
            if (deviation < num("min-deviation-ms", 10)) {
                flag(player, data, 1, "zu gleichmäßig: Abweichung nur " + fmt(deviation) + " ms bei " + cps + " CPS");
                data.clickIntervals.clear();
            }
        }
    }

    /** Standardabweichung: Wie stark schwanken die Abstände um ihren Durchschnitt? */
    private static double standardDeviation(PlayerData data) {
        double average = 0;
        for (long value : data.clickIntervals) {
            average += value;
        }
        average /= data.clickIntervals.size();

        double sum = 0;
        for (long value : data.clickIntervals) {
            sum += (value - average) * (value - average);
        }
        return Math.sqrt(sum / data.clickIntervals.size());
    }
}
