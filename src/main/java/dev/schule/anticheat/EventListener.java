package dev.schule.anticheat;

import dev.schule.anticheat.checks.Check;
import dev.schule.anticheat.data.Movement;
import dev.schule.anticheat.data.PlayerData;
import dev.schule.anticheat.util.BlockUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

/**
 * Empfängt alle Ereignisse vom Server und verteilt sie an die Checks.
 * Außerdem merkt er sich Dinge für Ausnahmen (Teleport, Knockback, Wasser ...).
 *
 * EventPriority.MONITOR = wir schauen nur zu und laufen nach allen anderen Plugins.
 */
public class EventListener implements Listener {

    private final AntiCheatPlugin plugin;

    public EventListener(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    // ==================== Bewegung ====================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getData(player);
        Location from = event.getFrom();
        Location to = event.getTo();
        long now = PlayerData.now();

        // 1) Kopfdrehung merken (braucht der KillAura-Check)
        float yawChange = angleDifference(from.getYaw(), to.getYaw());
        if (yawChange > 0) {
            data.lastYawChange = yawChange;
            data.lastRotationTick = now;
        }

        // Nur gedreht, nicht bewegt? Dann gibt es für die Bewegungs-Checks nichts zu tun.
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }
        data.lastMoveTick = now;

        // 2) Umgebung prüfen und für Ausnahmen merken
        if (player.isGliding()) {
            data.lastGlideTick = now;
        }
        boolean special = BlockUtil.isNearSpecialBlock(to) || BlockUtil.isNearSpecialBlock(from);
        if (special) {
            data.lastSpecialBlockTick = now;
        }
        if (BlockUtil.isOnIce(to) || BlockUtil.isOnIce(from)) {
            data.lastIceTick = now;
        }
        if (BlockUtil.isHeadBlocked(to) || BlockUtil.isHeadBlocked(from)) {
            data.lastHeadBlockedTick = now;
        }

        // 3) Bewegung ausrechnen
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);   // Satz des Pythagoras
        boolean groundTo = BlockUtil.isOnGround(to);
        Movement move = new Movement(from, to, dx, dy, dz, horizontal, groundTo);

        // 4) Alle Checks fragen
        for (Check check : plugin.getChecks()) {
            if (check.isEnabled()) {
                check.onMove(player, data, move);
            }
        }

        // 5) Zustand für den nächsten Schritt speichern
        if (groundTo) {
            data.airTicks = 0;
            data.groundTicks++;
            data.highestY = to.getY();
        } else {
            data.airTicks++;
            data.groundTicks = 0;
            data.highestY = Math.max(data.highestY, to.getY());
        }
        if (special) {
            data.highestY = to.getY();   // Wasser & Co. bremsen den Fall
        }
        data.wasOnGround = groundTo;
        data.lastDeltaY = dy;
    }

    // ==================== Kampf ====================

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttack(EntityDamageByEntityEvent event) {
        // Nur direkte Nahkampf-Schläge eines Spielers (keine Pfeile, kein Schwung-Schaden)
        if (!(event.getDamager() instanceof Player player)
                || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        PlayerData data = plugin.getData(player);
        for (Check check : plugin.getChecks()) {
            if (check.isEnabled()) {
                check.onAttack(player, data, event.getEntity());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSwing(PlayerArmSwingEvent event) {
        // Nur die Haupthand zählt als Linksklick
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Player player = event.getPlayer();
        PlayerData data = plugin.getData(player);
        for (Check check : plugin.getChecks()) {
            if (check.isEnabled()) {
                check.onSwing(player, data);
            }
        }
    }

    // ==================== Abbauen (beim Abbauen schwingt der Arm dauernd) ====================

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDigStart(BlockDamageEvent event) {
        PlayerData data = plugin.getData(event.getPlayer());
        data.digging = true;
        data.lastDigTick = PlayerData.now();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDigAbort(BlockDamageAbortEvent event) {
        plugin.getData(event.getPlayer()).digging = false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        plugin.getData(event.getPlayer()).digging = false;
    }

    // ==================== Merken für Ausnahmen ====================

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player
                && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            plugin.getData(player).lastFallDamageTick = PlayerData.now();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVelocity(PlayerVelocityEvent event) {
        PlayerData data = plugin.getData(event.getPlayer());
        Vector velocity = event.getVelocity();
        data.lastVelocityTick = PlayerData.now();
        data.lastVelocityHorizontal = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        resetAfterTeleport(plugin.getData(event.getPlayer()), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        resetAfterTeleport(plugin.getData(event.getPlayer()), event.getRespawnLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        resetAfterTeleport(plugin.getData(event.getPlayer()), event.getPlayer().getLocation());
    }

    private void resetAfterTeleport(PlayerData data, Location to) {
        data.lastTeleportTick = PlayerData.now();
        data.highestY = to.getY();
        data.airTicks = 0;
        data.wasOnGround = true;
        data.lastDeltaY = 0;
    }

    // ==================== Join / Quit ====================

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        PlayerData data = plugin.getData(event.getPlayer());
        data.joinTick = PlayerData.now();
        data.highestY = event.getPlayer().getLocation().getY();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.removeData(event.getPlayer());
    }

    /** Unterschied zwischen zwei Blickrichtungen in Grad (0 bis 180). */
    private static float angleDifference(float a, float b) {
        float diff = Math.abs(a - b) % 360f;
        return diff > 180f ? 360f - diff : diff;
    }
}
