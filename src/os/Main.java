package os;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;

public class Main extends Application {
    private Stage stage;
    private static MemoryManager memoryManager;
    private static TextArea memoryDisplay;
    private static VBox memoryRegions;
    private static Canvas memoryCanvas;
    private static final int CANVAS_WIDTH = 400;
    private static final int CANVAS_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        memoryRegions = new VBox(10);
        memoryRegions.setAlignment(Pos.CENTER);
        memoryRegions.setStyle("-fx-border-color:purple");

        memoryManager = new MemoryManager();

        memoryDisplay = new TextArea();
        memoryDisplay.setEditable(false);

        memoryCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);

        Scene scene = new Scene(memoryRegions, 500, 700);
        memoryRegions.setPadding(new Insets(20));
        memoryRegions.getChildren().addAll(memoryCanvas, memoryDisplay);

        stage.setTitle("Memory Management Visualization");
        stage.setScene(scene);
        stage.show();

        new Thread(this::runMemoryManagement).start();
    }

    private void runMemoryManagement() {
        while (!memoryManager.isCompleted()) {
            memoryManager.allocateMemory();
            memoryManager.executeProcesses();
            if (memoryManager.countHoles() > 3) {
                memoryManager.compactMemory();
            }
            displayMemoryStatus();
            displayMemoryRegions();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    private void displayMemoryStatus() {
        Platform.runLater(() -> memoryDisplay.setText(memoryManager.getMemoryStatus()));
    }

    private void displayMemoryRegions() {
        Platform.runLater(() -> {
            synchronized (memoryManager.getMemory()) {
                drawMemory();
            }
        });
    }

    private void drawMemory() {
        GraphicsContext gc = memoryCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        List<MemoryRegion> memory = memoryManager.getMemory();
        double totalMemory = MemoryManager.MEMORY_SIZE;

        double yOffset = 10;
        for (MemoryRegion region : memory) {
            double height = (region.getSize() / totalMemory) * (CANVAS_HEIGHT - 20);
            gc.setFill(region.isFree() ? Color.LIGHTGRAY : Color.PINK);
            gc.fillRect(10, yOffset, CANVAS_WIDTH - 20, height);

            gc.setFill(Color.BLACK);
            gc.strokeRect(10, yOffset, CANVAS_WIDTH - 20, height);
            gc.fillText(region.toString(), 15, yOffset + 15);

            yOffset += height + 5;
        }
    }


    public static void main(String[] args) {
        launch();
    }
}
