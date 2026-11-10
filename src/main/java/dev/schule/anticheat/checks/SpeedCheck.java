package dev.schule.anticheat.checks;

import dev.schule.anticheat.AntiCheatPlugin;
import dev.schule.anticheat.data.Movement;
import dev.schule.anticheat.data.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

/**
 * Speed-Check: Erkennt Spieler, die sich horizontal schneller bewegen als möglich.
 *
 * Idee: Der Server kennt die "Bewegungsgeschwindigkeit" jedes Spielers (Attribut).
 * Darin sind Sprinten, Speed-Trank und Langsamkeit schon eingerechnet.
 * Daraus berechnen wir die erlaubte Strecke pro Tick und geben Spielraum für
 * Springen, Eis, Decken und Knockback.
 */
public class SpeedCheck extends Check {

    /**
     * Umrechnung Attribut -> Blöcke pro Tick auf normalem Boden.
     * Beispiel: Gehen 0,10 * 2,16 = 0,216 Blöcke/Tick = 4,3 Blöcke/Sekunde.
     */
    private static final double ATTRIBUTE_TO_BLOCKS = 2.16;

    public SpeedCheck(AntiCheatPlugin plugin) {
        super(plugin, "speed", "Speed");
    }

    @Override
    public void onMove(Player player, PlayerData data, Movement move) {
        if (isExempt(player, data)
                || player.isGliding() || PlayerData.ticksSince(data.lastGlideTick) < 40   // Elytra
                || player.isRiptiding()                                                    // Dreizack
                || PlayerData.ticksSince(data.lastSpecialBlockTick) < 10) {                // Wasser & Co.
            return;
        }

        // 1) Grundgeschwindigkeit aus dem Attribut (enthält Sprint und Effekte)
        double limit = getMovementSpeed(player) * ATTRIBUTE_TO_BLOCKS;

        // 2) Spielraum für Sprünge: erst nach 3 Schritten auf dem Boden gilt die strenge Grenze
        if (!(move.groundTo() && data.groundTicks >= 3)) {
            limit += num("air-bonus", 0.33);
        }
        // 3) Auf Eis rutscht man viel schneller
        if (PlayerData.ticksSince(data.lastIceTick) < 40) {
            limit *= num("ice-multiplier", 2.5);
        }
        // 4) Springen unter einer Decke macht schneller
        if (PlayerData.ticksSince(data.lastHeadBlockedTick) < 20) {
            limit += num("head-bonus", 0.30);
        }
        // 5) Knockback darf einen wegschleudern
        if (PlayerData.ticksSince(data.lastVelocityTick) < num("velocity-ticks", 30)) {
            limit += data.lastVelocityHorizontal + 0.1;
        }
        // 6) Allgemeine Toleranz (z. B. für Lag)
        limit *= num("tolerance", 1.10);

        if (move.horizontal() > limit) {
            data.speedBuffer++;
            if (data.speedBuffer >= num("buffer", 3)) {
                flag(player, data, 1, fmt(move.horizontal()) + " > " + fmt(limit) + " Blöcke/Tick");
                data.speedBuffer = 0;
            }
        } else {
            data.speedBuffer = Math.max(0, data.speedBuffer - 0.25);
        }
    }

    /** Liest das Bewegungs-Attribut (Standard: 0,1 beim Gehen, 0,13 beim Sprinten). */
    private static double getMovementSpeed(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MOVEMENT_SPEED);
        return attribute != null ? attribute.getValue() : 0.1;
    }
}
