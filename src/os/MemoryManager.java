package os;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MemoryManager {
    public static final int MEMORY_SIZE = 2048;
    public static final int OS_SIZE = 512;
    private static final int AVAILABLE_MEMORY = MEMORY_SIZE - OS_SIZE;

    private List<Process> readyQueue = new ArrayList<>();
    private List<Process> jobQueue = new ArrayList<>();
    private List<MemoryRegion> memory = new ArrayList<>();
    private List<Process> activeProcesses = new ArrayList<>();

    public MemoryManager() throws IOException {
        readInputFiles();
        initializeMemory();
    }

    private void readInputFiles() throws IOException {
        try (BufferedReader readyReader = new BufferedReader(new FileReader("ready.txt"));
             BufferedReader jobReader = new BufferedReader(new FileReader("job.txt"))) {
            String line;
            while ((line = readyReader.readLine()) != null) {
                String[] parts = line.split(" ");
                int id = Integer.parseInt(parts[0]);
                int size = Integer.parseInt(parts[1]);
                int timeInMemory = Integer.parseInt(parts[2]);
                readyQueue.add(new Process(id, size, timeInMemory));
            }

            while ((line = jobReader.readLine()) != null) {
                String[] parts = line.split(" ");
                int id = Integer.parseInt(parts[0]);
                int size = Integer.parseInt(parts[1]);
                int timeInMemory = Integer.parseInt(parts[2]);
                jobQueue.add(new Process(id, size, timeInMemory));
            }
        }
    }

    private void initializeMemory() {
        memory.add(new MemoryRegion(0, OS_SIZE, false));
        memory.add(new MemoryRegion(OS_SIZE, AVAILABLE_MEMORY, true));
    }

    public void allocateMemory() {
        Iterator<Process> iterator = readyQueue.iterator();
        while (iterator.hasNext()) {
            Process process = iterator.next();
            if (allocateProcessToMemory(process)) {
                iterator.remove();
            }
        }
    }

    private boolean allocateProcessToMemory(Process process) {
        for (MemoryRegion partition : memory) {
            if (partition.isFree() && partition.getSize() >= process.getSize()) {
                process.setBase(partition.getBase());
                process.setLimit(partition.getBase() + process.getSize());
                partition.setBase(partition.getBase() + process.getSize());
                partition.setSize(partition.getSize() - process.getSize());
                if (partition.getSize() == 0) {
                    memory.remove(partition);
                }
                memory.add(new MemoryRegion(process, process.getBase(), process.getSize(), false));
                activeProcesses.add(process);
                System.out.println("Allocated Process " + process.getId() + " of size " + process.getSize() + "MB to memory.");
                return true;
            }
        }
        System.out.println("Failed to allocate Process " + process.getId() + " of size " + process.getSize() + "MB to memory.");
        return false;
    }

    public void executeProcesses() {
        List<Process> processesToRemove = new ArrayList<>();
        for (Process process : activeProcesses) {
            process.setTimeInMemory(process.getTimeInMemory() - 1);
            if (process.getTimeInMemory() == 0) {
                deallocateProcess(process);
                processesToRemove.add(process);
            }
        }
        activeProcesses.removeAll(processesToRemove);
        allocateJobFromQueue();
    }

    private void deallocateProcess(Process process) {
        Iterator<MemoryRegion> iterator = memory.iterator();
        while (iterator.hasNext()) {
            MemoryRegion partition = iterator.next();
            if (!partition.isFree() && partition.getBase() == process.getBase()) {
                iterator.remove();
                memory.add(new MemoryRegion(process, process.getBase(), process.getSize(), true));
                mergeFreePartitions();
                System.out.println("Deallocated Process " + process.getId() + " of size " + process.getSize() + "MB from memory.");
                break;
            }
        }
    }

    private void mergeFreePartitions() {
        memory.sort(Comparator.comparingInt(MemoryRegion::getBase));
        for (int i = 0; i < memory.size() - 1; ) {
            MemoryRegion current = memory.get(i);
            MemoryRegion next = memory.get(i + 1);
            if (current.isFree() && next.isFree()) {
                current.setSize(current.getSize() + next.getSize());
                memory.remove(next);
            } else {
                i++;
            }
        }
    }

    private void allocateJobFromQueue() {
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

    public int countHoles() {
        int holes = 0;
        for (MemoryRegion partition : memory) {
            if (partition.isFree()) {
                holes++;
            }
        }
        return holes;
    }

    public void compactMemory() {
        List<MemoryRegion> occupiedPartitions = new ArrayList<>();
        int occupiedMemory = OS_SIZE;

        for (MemoryRegion partition : memory) {
            if (!partition.isFree()) {
                occupiedPartitions.add(partition);
                occupiedMemory += partition.getSize();
            }
        }

        memory.clear();
        memory.add(new MemoryRegion(0, OS_SIZE, false));

        int base = OS_SIZE;
        for (MemoryRegion partition : occupiedPartitions) {
            partition.setBase(base);
            base += partition.getSize();
            memory.add(partition);
        }
        memory.add(new MemoryRegion(base, MEMORY_SIZE - base, true));

        System.out.println("Memory compaction performed.");
    }

    public String getMemoryStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Memory Status:\n");
        for (MemoryRegion partition : memory) {
            sb.append("Base: ").append(partition.getBase())
                    .append(", Size: ").append(partition.getSize())
                    .append(", Free: ").append(partition.isFree()).append("\n");
        }
        return sb.toString();
    }

    public List<MemoryRegion> getMemory() {
        return memory;
    }

    public boolean isCompleted() {
        return readyQueue.isEmpty() && jobQueue.isEmpty() && activeProcesses.isEmpty();
    }
}
