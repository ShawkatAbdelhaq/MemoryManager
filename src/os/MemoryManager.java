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
        Iterator<Process> iterator = readyQueue.iterator();
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
                activeProcesses.add(process);
                System.out.println("Allocated Process " + process.id + " of size " + process.size + "MB to memory.");
                return true;
            }
        }
        System.out.println("Failed to allocate Process " + process.id + " of size " + process.size + "MB to memory.");
        return false;
    }

    void executeProcesses() {
        List<Process> processesToRemove = new ArrayList<>();
        for (Process process : activeProcesses) {
            process.timeInMemory--;
            if (process.timeInMemory == 0) {
                deallocateProcess(process);
                processesToRemove.add(process);
            }
        }
        activeProcesses.removeAll(processesToRemove);
        allocateJobFromQueue();
    }

    void deallocateProcess(Process process) {
        Iterator<MemoryPartition> iterator = memory.iterator();
        while (iterator.hasNext()) {
            MemoryPartition partition = iterator.next();
            if (!partition.isFree && partition.base == process.base) {
                iterator.remove();
                memory.add(new MemoryPartition(process.base, process.size, true));
                mergeFreePartitions();
                System.out.println("Deallocated Process " + process.id + " of size " + process.size + "MB from memory.");
                break;
            }
        }
    }

    void mergeFreePartitions() {
        memory.sort(Comparator.comparingInt(p -> p.base));
        for (int i = 0; i < memory.size() - 1; ) {
            MemoryPartition current = memory.get(i);
            MemoryPartition next = memory.get(i + 1);
            if (current.isFree && next.isFree) {
                current.size += next.size;
                memory.remove(next);
            } else {
                i++;
            }
        }
    }

    void allocateJobFromQueue() {
        boolean allocated;
        do {
            allocated = false;
            Iterator<Process> iterator = jobQueue.iterator();
            while (iterator.hasNext()) {
                Process job = iterator.next();
                if (allocateProcessToMemory(job)) {
                    iterator.remove();
                    allocated = true;
                }
            }
        } while (allocated);
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
        List<MemoryPartition> occupiedPartitions = new ArrayList<>();
        int occupiedMemory = OS_SIZE;

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
