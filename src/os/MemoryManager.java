package os;

import java.io.*;
import java.util.*;

public class MemoryManager {
    static final int MEMORY_SIZE = 2048;
    static final int OS_SIZE = 512;
    static final int AVAILABLE_MEMORY = MEMORY_SIZE - OS_SIZE;
    static List<Process> readyQueue = new LinkedList<>();
    static List<Process> jobQueue = new LinkedList<>();
    static List<MemoryPartition> memory = new ArrayList<>();

    public MemoryManager() throws IOException {
        readInputFiles();
        initializeMemory();
    }

    void readInputFiles() throws IOException {
        BufferedReader readyReader = new BufferedReader(new FileReader("ready.txt"));
        BufferedReader jobReader = new BufferedReader(new FileReader("job.txt"));

        String line;
        while ((line = readyReader.readLine()) != null) {
            String[] parts = line.split(" ");
            int id = Integer.parseInt(parts[0]);
            int size = Integer.parseInt(parts[1]);
            int timeInMemory = Integer.parseInt(parts[2]);
            readyQueue.add(new Process(id, size, timeInMemory));
        }
        readyReader.close();

        while ((line = jobReader.readLine()) != null) {
            String[] parts = line.split(" ");
            int id = Integer.parseInt(parts[0]);
            int size = Integer.parseInt(parts[1]);
            int timeInMemory = Integer.parseInt(parts[2]);
            jobQueue.add(new Process(id, size, timeInMemory));
        }
        jobReader.close();
    }

    void initializeMemory() {
        memory.add(new MemoryPartition(0, OS_SIZE, false)); // OS partition
        memory.add(new MemoryPartition(OS_SIZE, AVAILABLE_MEMORY, true)); // Free memory
    }

    void allocateMemory() {
        ListIterator<Process> iterator = readyQueue.listIterator();
        while (iterator.hasNext()) {
            Process process = iterator.next();
            if (allocateProcessToMemory(process)) {
                iterator.remove();
            }
        }
    }

    boolean allocateProcessToMemory(Process process) {
        for (MemoryPartition partition : memory) {
            if (partition.isFree && partition.size >= process.size) {
                process.base = partition.base;
                process.limit = process.base + process.size;
                partition.base += process.size;
                partition.size -= process.size;
                if (partition.size == 0) {
                    memory.remove(partition);
                }
                memory.add(new MemoryPartition(process.base, process.size, false));
                return true;
            }
        }
        return false;
    }

    void executeProcesses() {
        ListIterator<MemoryPartition> iterator = memory.listIterator();
        while (iterator.hasNext()) {
            MemoryPartition partition = iterator.next();
            if (!partition.isFree) {
                Process process = findProcessByBase(partition.base);
                if (process != null) {
                    process.timeInMemory--;
                    if (process.timeInMemory == 0) {
                        iterator.remove();
                        memory.add(new MemoryPartition(process.base, process.size, true));
                        if (!jobQueue.isEmpty()) {
                            readyQueue.add(jobQueue.remove(0));
                        }
                    }
                }
            }
        }
    }

    Process findProcessByBase(int base) {
        for (Process process : readyQueue) {
            if (process.base == base) {
                return process;
            }
        }
        return null;
    }

    int countHoles() {
        int holes = 0;
        for (MemoryPartition partition : memory) {
            if (partition.isFree) {
                holes++;
            }
        }
        return holes;
    }

    void compactMemory() {
        int occupiedMemory = OS_SIZE;
        List<MemoryPartition> occupiedPartitions = new ArrayList<>();

        for (MemoryPartition partition : memory) {
            if (!partition.isFree) {
                occupiedPartitions.add(partition);
                occupiedMemory += partition.size;
            }
        }
        memory.clear();
        memory.add(new MemoryPartition(0, OS_SIZE, false));
        int base = OS_SIZE;
        for (MemoryPartition partition : occupiedPartitions) {
            partition.base = base;
            base += partition.size;
            memory.add(partition);
        }
        memory.add(new MemoryPartition(base, MEMORY_SIZE - base, true));
    }

    String getMemoryStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("حالة الذاكرة:\n");
        for (MemoryPartition partition : memory) {
            sb.append("Base: ").append(partition.base)
                    .append(", Size: ").append(partition.size)
                    .append(", Free: ").append(partition.isFree).append("\n");
        }
        return sb.toString();
    }

    boolean isCompleted() {
        return readyQueue.isEmpty() && jobQueue.isEmpty();
    }
}
