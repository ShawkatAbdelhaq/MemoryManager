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
    static List<Process> activeProcesses = new ArrayList<>();

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
        memory.add(new MemoryPartition(0, OS_SIZE, false));
        memory.add(new MemoryPartition(OS_SIZE, AVAILABLE_MEMORY, true));
    }

    void allocateMemory() {
        for (int i = 0; i < readyQueue.size(); ) {
            Process process = readyQueue.get(i);
            if (allocateProcessToMemory(process)) {
                readyQueue.remove(i);
            } else {
                i++;
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
                activeProcesses.add(process);
                System.out.println("Allocated Process " + process.id + " of size " + process.size + "MB to memory.");
                return true;
            }
        }
        System.out.println("Failed to allocate Process " + process.id + " of size " + process.size + "MB to memory.");
        return false;
    }

    void executeProcesses() {
        for (int i = 0; i < activeProcesses.size(); ) {
            Process process = activeProcesses.get(i);
            process.timeInMemory--;
            if (process.timeInMemory == 0) {
                deallocateProcess(process);
                activeProcesses.remove(i);
                allocateJobFromQueue();
            } else {
                i++;
            }
        }
    }

    void deallocateProcess(Process process) {
        for (MemoryPartition partition : memory) {
            if (!partition.isFree && partition.base == process.base) {
                memory.remove(partition);
                memory.add(new MemoryPartition(process.base, process.size, true));
                System.out.println("Deallocated Process " + process.id + " of size " + process.size + "MB from memory.");
                break;
            }
        }
    }

    void allocateJobFromQueue() {
        boolean allocated;
        do {
            allocated = false;
            for (int i = 0; i < jobQueue.size(); ) {
                Process job = jobQueue.get(i);
                if (allocateProcessToMemory(job)) {
                    jobQueue.remove(i);
                    allocated = true;
                } else {
                    i++;
                }
            }
        } while (allocated);
    }

    Process findProcessByBase(int base) {
        for (Process process : activeProcesses) {
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
        System.out.println("Memory compaction performed.");
    }

    String getMemoryStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Memory Status:\n");
        for (MemoryPartition partition : memory) {
            sb.append("Base: ").append(partition.base)
                    .append(", Size: ").append(partition.size)
                    .append(", Free: ").append(partition.isFree).append("\n");
        }
        return sb.toString();
    }

    boolean isCompleted() {
        return readyQueue.isEmpty() && jobQueue.isEmpty() && activeProcesses.isEmpty();
    }
}