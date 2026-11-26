package dev.schule.anticheat.replay;

import dev.schule.anticheat.AntiCheatPlugin;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Speichert Replays als Dateien in plugins/SchoolAntiCheat/replays/ und liest sie wieder ein.
 *
 * Aufbau einer Datei:
 *   Zeile 1:  # SchoolAntiCheat-Replay v1
 *   Zeile 2:  player=xSalkinTV;uuid=...;check=Fly;time=2026-11-26 03:20:11
 *   danach:   ein Frame pro Zeile (siehe ReplayFrame.toLine)
 */
public class ReplayManager {

    /** Kurzinfo zu einem gespeicherten Replay (ohne die Frames). */
    public record Info(int id, String player, UUID uuid, String check, LocalDateTime time, Path file) {
    }

    private static final String MAGIC = "# SchoolAntiCheat-Replay v1";
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AntiCheatPlugin plugin;
    private final Path folder;
    /** Alle bekannten Replays, nach Nummer sortiert */
    private final List<Info> replays = new ArrayList<>();

    public ReplayManager(AntiCheatPlugin plugin) {
        this.plugin = plugin;
        this.folder = plugin.getDataFolder().toPath().resolve("replays");
        loadIndex();
    }

    /**
     * Aufnahme speichern.
     *
     * @return Nummer des neuen Replays, oder 0 wenn nichts gespeichert wurde
     */
    public int save(Player player, String check, List<ReplayFrame> frames) {
        if (frames.isEmpty()) {
            return 0;
        }
        int id = replays.isEmpty() ? 1 : replays.getLast().id() + 1;
        LocalDateTime now = LocalDateTime.now();
        Path file = folder.resolve(String.format("%04d_%s_%s.replay", id, player.getName(), check));

        List<String> lines = new ArrayList<>();
        lines.add(MAGIC);
        lines.add("player=" + player.getName() + ";uuid=" + player.getUniqueId()
                + ";check=" + check + ";time=" + now.format(TIME));
        for (ReplayFrame frame : frames) {
            lines.add(frame.toLine());
        }
        try {
            Files.createDirectories(folder);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().warning("Replay konnte nicht gespeichert werden: " + e.getMessage());
            return 0;
        }
        replays.add(new Info(id, player.getName(), player.getUniqueId(), check, now, file));
        deleteOldReplays();
        return id;
    }

    public Info get(int id) {
        for (Info info : replays) {
            if (info.id() == id) {
                return info;
            }
        }
        return null;
    }

    /** Die neuesten Replays zuerst. */
    public List<Info> latest(int limit) {
        List<Info> result = new ArrayList<>();
        for (int i = replays.size() - 1; i >= 0 && result.size() < limit; i--) {
            result.add(replays.get(i));
        }
        return result;
    }

    /** Alle Frames eines Replays aus der Datei lesen. */
    public List<ReplayFrame> loadFrames(Info info) {
        List<ReplayFrame> frames = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(info.file(), StandardCharsets.UTF_8);
            for (int i = 2; i < lines.size(); i++) {   // die ersten 2 Zeilen sind der Kopf
                try {
                    frames.add(ReplayFrame.fromLine(lines.get(i)));
                } catch (RuntimeException ignored) {
                    // kaputte Zeile überspringen
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Replay konnte nicht gelesen werden: " + e.getMessage());
        }
        return frames;
    }

    /** Beim Start: nur die Kopfzeilen aller Replay-Dateien einlesen. */
    private void loadIndex() {
        if (!Files.isDirectory(folder)) {
            return;
        }
        try (Stream<Path> files = Files.list(folder)) {
            files.filter(f -> f.getFileName().toString().endsWith(".replay")).forEach(this::readHeader);
        } catch (IOException e) {
            plugin.getLogger().warning("Replay-Ordner konnte nicht gelesen werden: " + e.getMessage());
        }
        replays.sort(Comparator.comparingInt(Info::id));
    }

    private void readHeader(Path file) {
        try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
            List<String> head = lines.limit(2).toList();
            if (head.size() < 2 || !head.get(0).equals(MAGIC)) {
                return;
            }
            Map<String, String> values = new HashMap<>();
            for (String part : head.get(1).split(";")) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length == 2) {
                    values.put(keyValue[0], keyValue[1]);
                }
            }
            // Die Nummer steht am Anfang des Dateinamens: 0007_Name_Check.replay
            int id = Integer.parseInt(file.getFileName().toString().split("_")[0]);
            replays.add(new Info(id, values.get("player"), UUID.fromString(values.get("uuid")),
                    values.get("check"), LocalDateTime.parse(values.get("time"), TIME), file));
        } catch (IOException | RuntimeException e) {
            plugin.getLogger().warning("Replay-Datei übersprungen: " + file.getFileName());
        }
    }

    /** Nur die neuesten "replay.max-files" Replays behalten. */
    private void deleteOldReplays() {
        int max = plugin.getConfig().getInt("replay.max-files", 50);
        while (replays.size() > max) {
            Info oldest = replays.removeFirst();
            try {
                Files.deleteIfExists(oldest.file());
            } catch (IOException e) {
                plugin.getLogger().warning("Altes Replay konnte nicht gelöscht werden: " + e.getMessage());
            }
        }
    }
}
