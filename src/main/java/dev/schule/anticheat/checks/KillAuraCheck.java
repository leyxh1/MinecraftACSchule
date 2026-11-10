package dev.schule.anticheat.checks;

import dev.schule.anticheat.AntiCheatPlugin;
import dev.schule.anticheat.data.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * KillAura-Check: Erkennt automatisches Angreifen.
 *
 * Eine KillAura greift alles in der Nähe an - ohne dass der Spieler zielt.
 * Wir achten auf drei typische Spuren:
 *  1. Blickfeld: Das Ziel liegt weit neben dem Fadenkreuz (oder hinter einem).
 *  2. Snap:      Der Kopf dreht sich in einem einzigen Tick ruckartig zum Ziel.
 *  3. Multi:     Viele verschiedene Ziele in sehr kurzer Zeit.
 */
public class KillAuraCheck extends Check {

    public KillAuraCheck(AntiCheatPlugin plugin) {
        super(plugin, "killaura", "KillAura");
    }

    @Override
    public void onAttack(Player player, PlayerData data, Entity target) {
        if (isExempt(player, data)) {
            return;
        }
        String reason = null;

        // --- 1) Blickfeld: Winkel zwischen Blickrichtung und Richtung zum Ziel ---
        Vector eyes = player.getEyeLocation().toVector();
        Vector look = player.getEyeLocation().getDirection();
        Vector toTarget = target.getBoundingBox().getCenter().subtract(eyes);
        if (toTarget.length() > 1.0) {   // ganz nah sind große Winkel normal
            double angle = Math.toDegrees(look.angle(toTarget));
            if (angle > num("max-angle", 75)) {
                reason = "Ziel " + Math.round(angle) + "° neben dem Fadenkreuz";
            }
        }

        // --- 2) Snap: große Kopfdrehung direkt vor dem Schlag ---
        if (reason == null
                && PlayerData.ticksSince(data.lastRotationTick) <= 1
                && data.lastYawChange > num("snap-angle", 45)) {
            reason = "Kopf in 1 Tick um " + Math.round(data.lastYawChange) + "° gedreht";
        }

        // --- 3) Multi: verschiedene Ziele in kurzer Zeit ---
        long now = PlayerData.now();
        long window = (long) num("target-window-ticks", 10);
        data.recentTargets.addLast(new long[]{target.getEntityId(), now});
        while (!data.recentTargets.isEmpty() && now - data.recentTargets.peekFirst()[1] > window) {
            data.recentTargets.removeFirst();
        }
        Set<Long> differentTargets = new HashSet<>();
        for (long[] entry : data.recentTargets) {
            differentTargets.add(entry[0]);
        }
        if (reason == null && differentTargets.size() >= num("max-targets", 3)) {
            reason = differentTargets.size() + " verschiedene Ziele in " + (window / 20.0) + " s";
        }

        // --- Auswertung über den Puffer ---
        if (reason != null) {
            data.killAuraBuffer++;
            if (data.killAuraBuffer >= num("buffer", 3)) {
                flag(player, data, 1, reason);
                data.killAuraBuffer = 0;
            }
        } else {
            data.killAuraBuffer = Math.max(0, data.killAuraBuffer - 0.5);
        }
    }
}
