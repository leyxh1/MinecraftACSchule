package dev.schule.anticheat.checks;

import dev.schule.anticheat.AntiCheatPlugin;
import dev.schule.anticheat.data.Movement;
import dev.schule.anticheat.data.PlayerData;
import dev.schule.anticheat.util.BlockUtil;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * Fly-Check: Erkennt Spieler, die fliegen oder in der Luft schweben.
 *
 * Idee: In Minecraft gilt in der Luft immer dieselbe Schwerkraft-Formel:
 *     neue Höhenänderung = (alte Höhenänderung - 0,08) * 0,98
 * Wer in der Luft deutlich davon abweicht (z. B. gar nicht fällt), fliegt.
 *
 * Zusätzlich: Wer mehrere Sekunden bewegungslos in der Luft steht, schwebt.
 */
public class FlyCheck extends Check {

    /** Schwerkraft pro Tick */
    private static final double GRAVITY = 0.08;
    /** Luftwiderstand: pro Tick bleiben 98 % der Geschwindigkeit übrig */
    private static final double DRAG = 0.98;

    public FlyCheck(AntiCheatPlugin plugin) {
        super(plugin, "fly", "Fly");
    }

    @Override
    public void onMove(Player player, PlayerData data, Movement move) {
        if (isExemptFromFly(player, data)) {
            data.flyBuffer = 0;
            return;
        }
        // Nur prüfen, wenn der Spieler schon mindestens 2 Schritte komplett in der Luft ist.
        // (Absprung und Landung haben andere Regeln.)
        if (move.groundTo() || data.wasOnGround || data.airTicks < 2) {
            return;
        }

        double expected = (data.lastDeltaY - GRAVITY) * DRAG;   // was die Schwerkraft verlangt
        double difference = Math.abs(move.dy() - expected);     // was wirklich passiert ist

        if (difference > num("tolerance", 0.03)) {
            data.flyBuffer++;
            if (data.flyBuffer >= num("buffer", 4)) {
                flag(player, data, 1, "Höhe " + fmt(move.dy()) + " statt " + fmt(expected) + " (Schwerkraft)");
                data.flyBuffer = 0;
            }
        } else {
            // Normale Bewegung baut den Puffer langsam wieder ab
            data.flyBuffer = Math.max(0, data.flyBuffer - 0.5);
        }
    }

    /**
     * Schweben: Wenn ein Spieler still in der Luft steht, schickt der Client
     * kaum Bewegungen - deshalb prüfen wir das zusätzlich zeitgesteuert.
     */
    @Override
    public void onTick(Player player, PlayerData data) {
        if (isExemptFromFly(player, data) || BlockUtil.isOnGround(player.getLocation())) {
            return;
        }
        long stillTicks = PlayerData.ticksSince(data.lastMoveTick);
        long limit = (long) (num("hover-seconds", 2) * 20);
        if (stillTicks >= limit) {
            flag(player, data, 2, "schwebt seit " + (stillTicks / 20) + " s bewegungslos in der Luft");
        }
    }

    /** Situationen, in denen man legal nicht normal fällt. */
    private boolean isExemptFromFly(Player player, PlayerData data) {
        return isExempt(player, data)
                || player.isGliding() || PlayerData.ticksSince(data.lastGlideTick) < 20     // Elytra
                || player.isRiptiding()                                                      // Dreizack
                || player.hasPotionEffect(PotionEffectType.LEVITATION)                       // Schwebe-Effekt
                || player.hasPotionEffect(PotionEffectType.SLOW_FALLING)                     // Sanfter Fall
                || PlayerData.ticksSince(data.lastSpecialBlockTick) < 10                     // Wasser, Leiter, Netz ...
                || PlayerData.ticksSince(data.lastVelocityTick) < 20                         // Knockback
                || PlayerData.ticksSince(data.lastHeadBlockedTick) < 5;                      // Kopf an der Decke
    }
}
