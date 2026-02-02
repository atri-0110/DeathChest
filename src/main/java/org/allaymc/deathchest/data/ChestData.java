package org.allaymc.deathchest.data;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ChestData {
    
    private UUID chestId;
    private UUID playerId;
    private String playerName;
    private String worldName;
    private long deathTime;
    private double x;
    private double y;
    private double z;
    private int dimensionId;
    private List<ItemData> items;
    private boolean recovered;
}
