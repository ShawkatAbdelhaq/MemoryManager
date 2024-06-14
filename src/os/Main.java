package os;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
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

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        memoryRegions = new VBox(10);
        memoryRegions.setAlignment(Pos.CENTER);
        memoryRegions.setStyle("-fx-border-color:purple");

        memoryManager = new MemoryManager();

        memoryDisplay = new TextArea();
        memoryDisplay.setEditable(false);

        Scene scene = new Scene(memoryRegions, 500, 500);
        memoryRegions.setPadding(new Insets(40));

        stage.setTitle("Memory Management");
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
                memoryRegions.getChildren().clear();
                List<MemoryRegion> memory = memoryManager.getMemory();
                for (MemoryRegion region : memory) {
                    VBox regionBox = new VBox(5);
                    regionBox.setAlignment(Pos.CENTER);

                    Rectangle rect = new Rectangle(100, 50);
                    rect.setFill(region.isFree() ? Color.LIGHTGRAY : Color.PINK);

                    Text text = new Text(region.toString());

                    regionBox.getChildren().addAll(rect, text);
                    memoryRegions.getChildren().add(regionBox);
                }
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
