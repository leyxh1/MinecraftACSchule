package dev.schule.anticheat.checks;

import dev.schule.anticheat.AntiCheatPlugin;
import dev.schule.anticheat.data.Movement;
import dev.schule.anticheat.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * NoFall-Check: Erkennt Spieler, die trotz großer Fallhöhe keinen Fallschaden bekommen.
 *
 * Idee: Fallschaden berechnet der Server erst, wenn der CLIENT sagt "ich bin gelandet".
 * NoFall-Cheats lügen dabei (z. B. "ich stehe die ganze Zeit auf dem Boden").
 * Wir messen die Fallhöhe deshalb selbst und schauen nach der Landung,
 * ob wirklich Fallschaden angekommen ist.
 */
public class NoFallCheck extends Check {

    public NoFallCheck(AntiCheatPlugin plugin) {
        super(plugin, "nofall", "NoFall");
    }

    @Override
    public void onMove(Player player, PlayerData data, Movement move) {
        // Landung = vorher in der Luft, jetzt auf festem Boden (vom Server geprüft)
        if (data.wasOnGround || !move.groundTo()) {
            return;
        }
        double fallDistance = data.highestY - move.to().getY();
        double minimum = getSafeFallDistance(player) + num("extra-distance", 1.0);
        if (fallDistance < minimum || isExemptFromNoFall(player, data)) {
            return;
        }

        // Ein paar Ticks warten - so lange darf der Server für den Schaden brauchen
        long landingTick = PlayerData.now();
        long delay = (long) num("check-delay-ticks", 5);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            boolean gotDamage = data.lastFallDamageTick >= landingTick - 2;
            if (!gotDamage) {
                flag(player, data, 1, "Fall aus " + fmt(fallDistance) + " Blöcken ohne Schaden");
            }
        }, delay);
    }

    /** Ab welcher Höhe gibt es Fallschaden? Normal 3 Blöcke, mit Sprungkraft mehr. */
    private static double getSafeFallDistance(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.SAFE_FALL_DISTANCE);
        double safe = attribute != null ? attribute.getValue() : 3.0;
        PotionEffect jumpBoost = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        if (jumpBoost != null) {
            safe += jumpBoost.getAmplifier() + 1;
        }
        return safe;
    }

    /** Situationen, in denen es legal keinen Fallschaden gibt. */
    private boolean isExemptFromNoFall(Player player, PlayerData data) {
        AttributeInstance multiplier = player.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER);
        return isExempt(player, data)
                || player.isMarkedInvulnerable()                                         // per Befehl unverwundbar
                || player.getNoDamageTicks() > 10                                      // gerade erst Schaden bekommen
                || (multiplier != null && multiplier.getValue() <= 0)                  // Fallschaden abgeschaltet
                || player.isGliding() || PlayerData.ticksSince(data.lastGlideTick) < 20
                || player.hasPotionEffect(PotionEffectType.SLOW_FALLING)
                || player.hasPotionEffect(PotionEffectType.LEVITATION)
                || PlayerData.ticksSince(data.lastSpecialBlockTick) < 5;               // in Wasser/Heu/Slime gelandet
    }
}
