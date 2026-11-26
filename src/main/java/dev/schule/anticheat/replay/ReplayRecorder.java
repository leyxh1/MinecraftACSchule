package dev.schule.anticheat.replay;

import dev.schule.anticheat.AntiCheatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Nimmt ständig die letzten Sekunden jedes Spielers auf - wie eine Dashcam im Auto.
 * Ältere Frames werden automatisch vergessen. Erst wenn ein Spieler gekickt wird,
 * wird die Aufnahme dauerhaft gespeichert (siehe ReplayManager).
 */
public class ReplayRecorder {

    private final AntiCheatPlugin plugin;
    /** Spieler -> letzte Frames (ältester vorne, neuester hinten) */
    private final Map<UUID, Deque<ReplayFrame>> buffers = new HashMap<>();
    /** Ereignisse seit dem letzten Tick (Schlag, Angriff) als Bits */
    private final Map<UUID, Integer> pendingState = new HashMap<>();
    /** Anticheat-Meldung seit dem letzten Tick */
    private final Map<UUID, String> pendingFlag = new HashMap<>();

    public ReplayRecorder(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    /** Wird jeden Tick aufgerufen: von jedem Spieler ein Frame aufnehmen. */
    public void tick() {
        if (!plugin.getConfig().getBoolean("replay.enabled", true)) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            record(player);
        }
    }

    private void record(Player player) {
        UUID id = player.getUniqueId();
        Location loc = player.getLocation();

        int state = pendingState.getOrDefault(id, 0);
        pendingState.remove(id);
        if (player.isSneaking()) {
            state |= ReplayFrame.SNEAK;
        }
        if (player.isSprinting()) {
            state |= ReplayFrame.SPRINT;
        }
        if (player.isGliding()) {
            state |= ReplayFrame.GLIDE;
        }
        String flag = pendingFlag.getOrDefault(id, "");
        pendingFlag.remove(id);

        Deque<ReplayFrame> buffer = buffers.computeIfAbsent(id, key -> new ArrayDeque<>());
        buffer.addLast(new ReplayFrame(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch(), state, flag));

        // Nur die letzten X Sekunden behalten (20 Frames pro Sekunde)
        int maxFrames = plugin.getConfig().getInt("replay.seconds", 30) * 20;
        while (buffer.size() > maxFrames) {
            buffer.removeFirst();
        }
    }

    public void markSwing(Player player) {
        pendingState.merge(player.getUniqueId(), ReplayFrame.SWING, (a, b) -> a | b);
    }

    public void markAttack(Player player) {
        pendingState.merge(player.getUniqueId(), ReplayFrame.ATTACK | ReplayFrame.SWING, (a, b) -> a | b);
    }

    /** Eine Anticheat-Meldung im nächsten Frame vermerken. */
    public void markFlag(Player player, String text) {
        pendingFlag.put(player.getUniqueId(), text);
    }

    /** Kopie der aktuellen Aufnahme (inklusive des aktuellen Augenblicks). */
    public List<ReplayFrame> snapshot(Player player) {
        record(player);
        return new ArrayList<>(buffers.getOrDefault(player.getUniqueId(), new ArrayDeque<>()));
    }

    /** Aufnahme eines Spielers verwerfen (beim Verlassen des Servers). */
    public void remove(Player player) {
        buffers.remove(player.getUniqueId());
        pendingState.remove(player.getUniqueId());
        pendingFlag.remove(player.getUniqueId());
    }
}
