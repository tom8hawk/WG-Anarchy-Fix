package ru.overwrite.wggf;

public class ProtectedRegion {
    private final int x1;
    private final int y1;
    private final int z1;
    private final int x2;
    private final int y2;
    private final int z2;
    
    public ProtectedRegion(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }
    
    public boolean contains(int x, int y, int z) {
        return y >= this.y1 && y <= this.y2 && this.contains(x, z);
    }
    
    public boolean contains(int x, int z) {
        return x >= this.x1 && z >= this.z1 && x <= this.x2 && z <= this.z2;
    }
}
