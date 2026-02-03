package org.allaymc.deathchest.data;

import lombok.Data;
import org.cloudburstmc.nbt.NbtMap;

/**
 * Stores item information for death chest serialization.
 * Uses NBT data to preserve complete item state including
 * enchantments, durability, custom names, and other metadata.
 */
@Data
public class ItemData {
    
    /**
     * NBT data representing the complete item state.
     * This preserves all item metadata including enchantments, durability, etc.
     */
    private NbtMap nbtData;
}