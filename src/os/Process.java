package os;

public class Process {
    int id;
    int size;
    int timeInMemory;
    int base;
    int limit;

    public Process(int id, int size, int timeInMemory) {
        this.id = id;
        this.size = size;
        this.timeInMemory = timeInMemory;
    }
}
