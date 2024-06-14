package os;

public class MemoryPartition {
    int base;
    int size;
    boolean isFree;

    public MemoryPartition(int base, int size, boolean isFree) {
        this.base = base;
        this.size = size;
        this.isFree = isFree;
    }
}