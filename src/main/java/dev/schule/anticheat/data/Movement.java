package dev.schule.anticheat.data;

import org.bukkit.Location;

/**
 * Ein einzelner Bewegungsschritt eines Spielers (meist 1 Tick = 1/20 Sekunde).
 * Ein "record" ist eine einfache Datenklasse: Werte rein, Werte raus, nichts ändert sich.
 *
 * @param from       Position vorher
 * @param to         Position nachher
 * @param dx         Bewegung in X-Richtung
 * @param dy         Bewegung nach oben (+) oder unten (-)
 * @param dz         Bewegung in Z-Richtung
 * @param horizontal Strecke auf dem Boden (X und Z zusammen, ohne Höhe)
 * @param groundTo   steht der Spieler nachher auf festem Boden? (vom SERVER geprüft)
 */
public record Movement(Location from, Location to,
                       double dx, double dy, double dz,
                       double horizontal, boolean groundTo) {
}
