package os;

import java.io.*;
import javax.swing.*;
import java.awt.*;

public class Main {
    static MemoryManager memoryManager;
    static JFrame frame;
    static JTextArea memoryDisplay;

    public static void main(String[] args) throws IOException {
        memoryManager = new MemoryManager();

        frame = new JFrame("Memory Management");
        memoryDisplay = new JTextArea();
        memoryDisplay.setEditable(false);
        frame.add(new JScrollPane(memoryDisplay), BorderLayout.CENTER);
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        while (!memoryManager.isCompleted()) {
            memoryManager.allocateMemory();
            memoryManager.executeProcesses();
            if (memoryManager.countHoles() > 3) {
                memoryManager.compactMemory();
            }
            displayMemoryStatus();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void displayMemoryStatus() {
        memoryDisplay.setText(memoryManager.getMemoryStatus());
    }
}