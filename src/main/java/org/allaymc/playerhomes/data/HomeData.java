package org.allaymc.playerhomes.data;

import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.math.location.Location3dc;
import org.allaymc.api.world.Dimension;

public class HomeData {
    
    private String name;
    private float x;
    private float y;
    private float z;
    private String worldName;
    private int dimensionId;
    private long createdAt;
    
    public HomeData(String name, float x, float y, float z, String worldName, int dimensionId) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
        this.dimensionId = dimensionId;
        this.createdAt = System.currentTimeMillis();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public float getZ() {
        return z;
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public int getDimensionId() {
        return dimensionId;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public Location3dc toLocation(Dimension dimension) {
        return new Location3d(x, y, z, dimension);
    }
}
