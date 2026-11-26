package dev.schule.anticheat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Merkt sich alle Kicks durch das Anticheat, damit Admins später
 * nachsehen können, wer wann weswegen gekickt wurde - und ihn dann bannen.
 *
 * Gespeichert wird in plugins/SchoolAntiCheat/kicks.log, eine Zeile pro Kick:
 *   2026-11-26 03:20:11 | xSalkinTV | Fly | VL 15/15 | Replay #3
 * Keine IP-Adressen (Datensparsamkeit).
 */
public class KickLog {

    /** Ein einzelner Kick. replayId = Nummer der Aufnahme (0 = keine). */
    public record Entry(LocalDateTime time, String player, String check, String vl, int replayId) {
    }

    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** So viele Kicks werden im Speicher gehalten (die Datei behält alle). */
    private static final int MAX_ENTRIES = 200;

    private final AntiCheatPlugin plugin;
    private final Path file;
    private final Deque<Entry> entries = new ArrayDeque<>();

    public KickLog(AntiCheatPlugin plugin) {
        this.plugin = plugin;
        this.file = plugin.getDataFolder().toPath().resolve("kicks.log");
        load();
    }

    /** Neuen Kick speichern - im Speicher und in der Datei. */
    public void add(String player, String check, String vl, int replayId) {
        Entry entry = new Entry(LocalDateTime.now(), player, check, vl, replayId);
        remember(entry);
        String line = entry.time().format(FILE_TIME) + " | " + player + " | " + check + " | " + vl
                + " | Replay #" + replayId + System.lineSeparator();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte nicht in kicks.log schreiben: " + e.getMessage());
        }
    }

    /**
     * Die letzten Kicks, neueste zuerst.
     *
     * @param player nur Kicks dieses Spielers, oder null für alle
     * @param limit  höchstens so viele Einträge
     */
    public List<Entry> latest(String player, int limit) {
        List<Entry> result = new ArrayList<>();
        var iterator = entries.descendingIterator();   // von hinten = neueste zuerst
        while (iterator.hasNext() && result.size() < limit) {
            Entry entry = iterator.next();
            if (player == null || entry.player().equalsIgnoreCase(player)) {
                result.add(entry);
            }
        }
        return result;
    }

    /** Beim Start die vorhandene kicks.log einlesen, damit nach einem Neustart nichts fehlt. */
    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] parts = line.split(" \\| ");
                if (parts.length < 4) {
                    continue;   // kaputte Zeile überspringen
                }
                try {
                    // Ältere Zeilen haben noch keine Replay-Nummer -> 0
                    int replayId = parts.length >= 5 ? Integer.parseInt(parts[4].replace("Replay #", "").trim()) : 0;
                    remember(new Entry(LocalDateTime.parse(parts[0], FILE_TIME), parts[1], parts[2], parts[3], replayId));
                } catch (DateTimeParseException | NumberFormatException ignored) {
                    // kaputte Zeile überspringen
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Konnte kicks.log nicht lesen: " + e.getMessage());
        }
    }

    private void remember(Entry entry) {
        entries.addLast(entry);
        if (entries.size() > MAX_ENTRIES) {
            entries.removeFirst();   // ältesten Eintrag vergessen
        }
    }
}
