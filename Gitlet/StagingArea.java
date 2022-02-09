package gitlet;

import java.util.HashMap;
import java.io.Serializable;

public class StagingArea implements Serializable {

    private HashMap<String, Blob> addition;
    private HashMap<String, Blob> removal;

    public StagingArea() {
        clearStage();
    }

    public void clearStage() {
        addition = new HashMap<>();
        removal = new HashMap<>();
    }

    public HashMap<String, Blob> getAddition() {
        return addition;
    }

    public HashMap<String, Blob> getRemoval() {
        return removal;
    }

    public boolean stageEmpty() {
        return addition.isEmpty() && removal.isEmpty();
    }

    public void addToAdditionStage(String filename, Blob b) {
        addition.put(filename, b);
    }

    public void addToRemovalStage(String filename, Blob b) {
        removal.put(filename, b);
    }
}
