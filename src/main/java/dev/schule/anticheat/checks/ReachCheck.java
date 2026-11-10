package dev.schule.anticheat.checks;

import dev.schule.anticheat.AntiCheatPlugin;
import dev.schule.anticheat.data.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/**
 * Reach-Check: Erkennt Angriffe aus zu großer Entfernung.
 *
 * Idee: Wir messen den Abstand von den Augen des Angreifers bis zum
 * nächsten Punkt der Hitbox (Trefferbox) des Ziels.
 * Normal sind höchstens 3 Blöcke (im Kreativmodus mehr).
 * Wegen Lag geben wir etwas Spielraum, abhängig vom Ping.
 */
public class ReachCheck extends Check {

    public ReachCheck(AntiCheatPlugin plugin) {
        super(plugin, "reach", "Reach");
    }

    @Override
    public void onAttack(Player player, PlayerData data, Entity target) {
        if (isExempt(player, data)) {
            return;
        }

        Vector eyes = player.getEyeLocation().toVector();
        BoundingBox hitbox = target.getBoundingBox().clone().expand(0.1);  // kleiner Rand wie im Spiel
        double distance = distanceToBox(eyes, hitbox);

        // Erlaubte Reichweite: Attribut (3 Blöcke) + Toleranz + Ausgleich für Ping
        double ping = Math.min(player.getPing(), num("max-ping", 300));
        double limit = getReach(player)
                + num("tolerance", 0.30)
                + (ping / 50.0) * num("ping-factor", 0.10);

        if (distance > limit) {
            data.reachBuffer++;
            if (data.reachBuffer >= num("buffer", 2)) {
                flag(player, data, 1, fmt(distance) + " > " + fmt(limit) + " Blöcke");
                data.reachBuffer = 0;
            }
        } else {
            data.reachBuffer = Math.max(0, data.reachBuffer - 0.25);
        }
    }

    /** Normale Angriffsreichweite des Spielers (Attribut, Standard 3 Blöcke). */
    private static double getReach(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        return attribute != null ? attribute.getValue() : 3.0;
    }

    /** Abstand von einem Punkt zum nächsten Punkt einer Box (0, wenn der Punkt drin ist). */
    private static double distanceToBox(Vector point, BoundingBox box) {
        double x = clamp(point.getX(), box.getMinX(), box.getMaxX());
        double y = clamp(point.getY(), box.getMinY(), box.getMaxY());
        double z = clamp(point.getZ(), box.getMinZ(), box.getMaxZ());
        return point.distance(new Vector(x, y, z));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
