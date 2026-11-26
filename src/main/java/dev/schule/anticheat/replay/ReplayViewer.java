package dev.schule.anticheat.replay;

import dev.schule.anticheat.AntiCheatPlugin;
import dev.schule.anticheat.ViolationManager;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Spielt ein Replay ab.
 *
 * Eine Spielerpuppe (Mannequin) mit dem Skin des Verdächtigen läuft Tick für Tick
 * genau seinen aufgezeichneten Weg nach. Nur der zuschauende Admin sieht die Puppe.
 * Der Admin ist währenddessen im Zuschauermodus und kann frei herumfliegen.
 * Unten im Bild (Actionbar) laufen Zeit und die letzte Anticheat-Meldung mit.
 */
public class ReplayViewer implements Listener {

    /** Ein laufendes Replay für einen Admin. */
    private static class Session {
        Player viewer;
        ReplayManager.Info info;
        List<ReplayFrame> frames;
        Mannequin puppet;
        int index;                       // aktueller Frame
        String lastFlag = "noch keine Meldung";
        int lastFlagIndex = -1000;
        Location returnLocation;         // wohin der Admin danach zurück soll
        GameMode returnMode;
        BukkitTask task;
    }

    private static final Particle.DustOptions TRAIL = new Particle.DustOptions(Color.WHITE, 0.7f);
    private static final Particle.DustOptions TRAIL_FLAGGED = new Particle.DustOptions(Color.RED, 0.9f);
    private static final Particle.DustOptions FLAG_BURST = new Particle.DustOptions(Color.RED, 1.6f);

    private final AntiCheatPlugin plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();

    public ReplayViewer(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    /** Replay für einen Admin starten. */
    public void start(Player viewer, ReplayManager.Info info) {
        stop(viewer);   // falls schon eins läuft

        List<ReplayFrame> frames = plugin.getReplayManager().loadFrames(info);
        if (frames.isEmpty()) {
            viewer.sendMessage(Component.text("Replay #" + info.id() + " ist leer oder beschädigt.", NamedTextColor.RED));
            return;
        }
        Location start = frames.getFirst().toLocation();
        if (start == null) {
            viewer.sendMessage(Component.text("Die Welt aus dem Replay gibt es nicht mehr.", NamedTextColor.RED));
            return;
        }

        Session session = new Session();
        session.viewer = viewer;
        session.info = info;
        session.frames = frames;
        session.returnLocation = viewer.getLocation();
        session.returnMode = viewer.getGameMode();

        // Puppe erzeugen - unsichtbar für alle außer dem Admin
        session.puppet = start.getWorld().spawn(start, Mannequin.class, puppet -> {
            puppet.setVisibleByDefault(false);
            puppet.setPersistent(false);          // wird nicht mit der Welt gespeichert
            puppet.setAI(false);
            puppet.setGravity(false);
            puppet.setInvulnerable(true);
            puppet.setSilent(true);
            puppet.setCollidable(false);
            puppet.setImmovable(true);
            puppet.setProfile(ResolvableProfile.resolvableProfile(Bukkit.createProfile(info.uuid(), info.player())));
            puppet.customName(Component.text("Replay: " + info.player(), NamedTextColor.RED));
            puppet.setCustomNameVisible(true);
            puppet.setDescription(Component.text("Replay #" + info.id() + " - " + info.check(), NamedTextColor.GRAY));
        });
        viewer.showEntity(plugin, session.puppet);

        // Admin in den Zuschauermodus, ein paar Blöcke hinter die Puppe
        viewer.setGameMode(GameMode.SPECTATOR);
        viewer.teleport(viewpoint(start));

        viewer.sendMessage(Component.text("▶ Replay #" + info.id() + ": " + info.player() + " - " + info.check()
                + " (" + seconds(frames.size()) + " s)", NamedTextColor.GOLD));
        viewer.sendMessage(Component.text("  Du bist im Zuschauermodus und kannst frei fliegen. ", NamedTextColor.GRAY)
                .append(Component.text("[Stopp]", NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/ac replay stop"))));

        sessions.put(viewer.getUniqueId(), session);
        session.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> step(session), 1L, 1L);
    }

    /** Ein Tick des Replays: Puppe an die nächste Position setzen. */
    private void step(Session s) {
        if (!s.viewer.isOnline() || !s.puppet.isValid()) {
            end(s);
            return;
        }
        if (s.index >= s.frames.size()) {
            finish(s);
            return;
        }
        ReplayFrame frame = s.frames.get(s.index);
        Location loc = frame.toLocation();
        if (loc != null) {
            s.puppet.teleport(loc);

            // Haltung: Schleichen / Elytra / Stehen
            Pose pose = frame.has(ReplayFrame.SNEAK) ? Pose.SNEAKING
                    : frame.has(ReplayFrame.GLIDE) ? Pose.FALL_FLYING : Pose.STANDING;
            if (Mannequin.validPoses().contains(pose)) {
                s.puppet.setPose(pose, true);
            }
            if (frame.has(ReplayFrame.SWING)) {
                s.puppet.swingMainHand();
            }
            if (frame.has(ReplayFrame.ATTACK)) {
                s.viewer.spawnParticle(Particle.CRIT, loc.clone().add(0, 1.2, 0), 8, 0.3, 0.3, 0.3, 0.1);
            }

            // Meldung in diesem Tick? -> rote Partikelwolke + Chatzeile
            if (!frame.flag().isEmpty()) {
                s.lastFlag = frame.flag();
                s.lastFlagIndex = s.index;
                s.viewer.spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 25, 0.3, 0.6, 0.3, 0, FLAG_BURST);
                s.viewer.sendMessage(Component.text("  ⚠ " + seconds(s.index) + " s: ", NamedTextColor.YELLOW)
                        .append(Component.text(frame.flag(), NamedTextColor.RED)));
            }

            // Spur am Boden: rot kurz nach einer Meldung, sonst weiß
            boolean recentlyFlagged = s.index - s.lastFlagIndex < 20;
            s.viewer.spawnParticle(Particle.DUST, loc.clone().add(0, 0.1, 0), 1, 0, 0, 0, 0,
                    recentlyFlagged ? TRAIL_FLAGGED : TRAIL);
        }

        // Anzeige unten im Bild
        s.viewer.sendActionBar(Component.text("▶ #" + s.info.id() + " " + s.info.player() + "  ", NamedTextColor.GOLD)
                .append(Component.text(seconds(s.index) + " / " + seconds(s.frames.size()) + " s  ", NamedTextColor.WHITE))
                .append(Component.text(s.lastFlag, s.lastFlagIndex >= 0 ? NamedTextColor.RED : NamedTextColor.GRAY)));
        s.index++;
    }

    /** Replay ist durchgelaufen: aufräumen und Entscheidungs-Knöpfe anzeigen. */
    private void finish(Session s) {
        end(s);
        s.viewer.sendMessage(Component.text("■ Replay #" + s.info.id() + " beendet. ", NamedTextColor.GOLD)
                .append(Component.text("[Nochmal] ", NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/ac replay " + s.info.id())))
                .append(ViolationManager.banButton(s.info.player(), s.info.check())));
    }

    /** Replay eines Admins abbrechen (z. B. /ac replay stop). Gibt true zurück, wenn eins lief. */
    public boolean stop(Player viewer) {
        Session session = sessions.get(viewer.getUniqueId());
        if (session == null) {
            return false;
        }
        end(session);
        return true;
    }

    /** Alle Replays beenden (beim Server-Stopp). */
    public void stopAll() {
        for (Session session : new ArrayList<>(sessions.values())) {
            end(session);
        }
    }

    /** Puppe entfernen und den Admin zurückbringen. */
    private void end(Session s) {
        sessions.remove(s.viewer.getUniqueId());
        if (s.task != null) {
            s.task.cancel();
        }
        if (s.puppet != null) {
            s.puppet.remove();
        }
        if (s.viewer.isOnline()) {
            s.viewer.setGameMode(s.returnMode);
            s.viewer.teleport(s.returnLocation);
        }
    }

    /** Admin verlässt den Server während eines Replays -> zurücksetzen. */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stop(event.getPlayer());
    }

    /** Aussichtspunkt: 4 Blöcke hinter und 2 Blöcke über dem Startpunkt, Blick auf die Puppe. */
    private static Location viewpoint(Location start) {
        Vector back = start.getDirection().setY(0);
        if (back.lengthSquared() < 0.01) {
            back = new Vector(0, 0, 1);
        }
        Location view = start.clone().add(back.normalize().multiply(-4)).add(0, 2, 0);
        view.setDirection(start.toVector().add(new Vector(0, 1, 0)).subtract(view.toVector()));
        return view;
    }

    private static String seconds(int frames) {
        return String.format(Locale.GERMANY, "%.1f", frames / 20.0);
    }
}
