package dev.schule.anticheat;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Hauptklasse des Plugins. Paper erzeugt genau ein Objekt davon,
 * sobald der Server startet (siehe "main" in plugin.yml).
 */
public class AntiCheatPlugin extends JavaPlugin {

    /** Wird aufgerufen, wenn das Plugin geladen und aktiviert wird. */
    @Override
    public void onEnable() {
        getLogger().info("SchoolAntiCheat gestartet (Version " + getPluginMeta().getVersion() + ")");
    }

    /** Wird aufgerufen, wenn der Server stoppt oder das Plugin deaktiviert wird. */
    @Override
    public void onDisable() {
        getLogger().info("SchoolAntiCheat gestoppt.");
    }
}
