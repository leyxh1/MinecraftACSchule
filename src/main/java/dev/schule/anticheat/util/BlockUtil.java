package dev.schule.anticheat.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.util.BoundingBox;

/**
 * Hilfsfunktionen rund um Blöcke.
 *
 * Wichtig: Der Client sagt dem Server zwar "ich stehe auf dem Boden",
 * aber genau das fälschen Cheats (z. B. NoFall). Deshalb prüfen wir
 * selbst anhand der echten Blöcke in der Welt - "never trust the client".
 */
public final class BlockUtil {

    /** Ein Spieler ist 0,6 Blöcke breit, also 0,3 von der Mitte zu jeder Seite. */
    private static final double HALF_WIDTH = 0.3;
    /** Ein Spieler ist 1,8 Blöcke hoch. */
    private static final double HEIGHT = 1.8;

    /** Blöcke, bei denen sich Spieler anders bewegen als normal. */
    private static final String[] SPECIAL_BLOCKS = {
            "LADDER", "VINE", "SCAFFOLDING", "COBWEB", "POWDER_SNOW", "BUBBLE_COLUMN",
            "HONEY_BLOCK", "SLIME_BLOCK", "_BED", "HAY_BLOCK", "SWEET_BERRY_BUSH",
            "KELP", "SEAGRASS", "WATER", "LAVA"
    };

    private BlockUtil() {
        // Nur statische Methoden, keine Objekte nötig
    }

    /** Steht ein Spieler an dieser Position auf einem festen Block? */
    public static boolean isOnGround(Location loc) {
        // Bereich direkt unter den Füßen: so breit wie der Spieler, 0,1 Blöcke tief
        BoundingBox feet = new BoundingBox(
                loc.getX() - HALF_WIDTH, loc.getY() - 0.1, loc.getZ() - HALF_WIDTH,
                loc.getX() + HALF_WIDTH, loc.getY() + 0.01, loc.getZ() + HALF_WIDTH);
        return containsSolidBlock(loc.getWorld(), feet);
    }

    /** Ist direkt über dem Kopf ein fester Block (Decke)? */
    public static boolean isHeadBlocked(Location loc) {
        BoundingBox head = new BoundingBox(
                loc.getX() - HALF_WIDTH, loc.getY() + HEIGHT, loc.getZ() - HALF_WIDTH,
                loc.getX() + HALF_WIDTH, loc.getY() + HEIGHT + 0.5, loc.getZ() + HALF_WIDTH);
        return containsSolidBlock(loc.getWorld(), head);
    }

    /** Ist unter den Füßen Eis? (Eis, Packeis, Blaueis ...) */
    public static boolean isOnIce(Location loc) {
        BoundingBox below = new BoundingBox(
                loc.getX() - HALF_WIDTH, loc.getY() - 0.6, loc.getZ() - HALF_WIDTH,
                loc.getX() + HALF_WIDTH, loc.getY(), loc.getZ() + HALF_WIDTH);
        return anyBlockMatches(loc.getWorld(), below, block -> block.getType().name().contains("ICE"));
    }

    /** Berührt der Spieler Wasser, Leitern, Spinnennetze, Slime usw.? */
    public static boolean isNearSpecialBlock(Location loc) {
        // Etwas größer als der Spieler, damit auch "gerade so" Berührungen zählen
        BoundingBox area = new BoundingBox(
                loc.getX() - HALF_WIDTH - 0.2, loc.getY() - 0.6, loc.getZ() - HALF_WIDTH - 0.2,
                loc.getX() + HALF_WIDTH + 0.2, loc.getY() + HEIGHT + 0.2, loc.getZ() + HALF_WIDTH + 0.2);
        return anyBlockMatches(loc.getWorld(), area, BlockUtil::isSpecial);
    }

    private static boolean isSpecial(Block block) {
        if (block.isLiquid()) {
            return true;
        }
        BlockData data = block.getBlockData();
        if (data instanceof Waterlogged waterlogged && waterlogged.isWaterlogged()) {
            return true;
        }
        String name = block.getType().name();
        for (String special : SPECIAL_BLOCKS) {
            if (name.contains(special)) {
                return true;
            }
        }
        return false;
    }

    /** Gibt es im Bereich einen festen Block, mit dem man zusammenstößt? */
    private static boolean containsSolidBlock(World world, BoundingBox box) {
        return anyBlockMatches(world, box,
                block -> !block.isPassable() && block.getBoundingBox().overlaps(box));
    }

    /** Geht alle Blöcke im Bereich durch und prüft, ob einer die Bedingung erfüllt. */
    private static boolean anyBlockMatches(World world, BoundingBox box, java.util.function.Predicate<Block> test) {
        int minX = (int) Math.floor(box.getMinX());
        int minY = (int) Math.floor(box.getMinY());
        int minZ = (int) Math.floor(box.getMinZ());
        int maxX = (int) Math.floor(box.getMaxX());
        int maxY = (int) Math.floor(box.getMaxY());
        int maxZ = (int) Math.floor(box.getMaxZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (test.test(world.getBlockAt(x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
