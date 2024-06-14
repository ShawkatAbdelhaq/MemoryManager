package os;

public class Process {
    private int id;
    private int size;
    private int timeInMemory;
    private int base;
    private int limit;

    public Process(int id, int size, int timeInMemory) {
        this.id = id;
        this.size = size;
        this.timeInMemory = timeInMemory;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public int getTimeInMemory() {
        return timeInMemory;
    }

    public void setTimeInMemory(int timeInMemory) {
        this.timeInMemory = timeInMemory;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
