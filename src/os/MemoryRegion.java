package os;

public class MemoryRegion {
    private Process process;
    private int base;
    private int size;
    private boolean isFree;

    public MemoryRegion(int base, int size, boolean isFree) {
        this.base = base;
        this.size = size;
        this.isFree = isFree;
    }

    public MemoryRegion(Process process, int base, int size, boolean isFree) {
        this.base = base;
        this.size = size;
        this.isFree = isFree;
        this.process = process;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    @Override
    public String toString() {
        return "MemoryRegion{" +
                "process=" + process +
                ", base=" + base +
                ", size=" + size +
                ", isFree=" + isFree +
                '}';
    }
}
