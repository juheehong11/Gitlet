package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Jessie Hong
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String timestamp;
    private List<String> parents; //SHA of parents
    private String sHA; //SHA of this commit
    private HashMap<String, Blob> blobs;

    static final File COMMIT_FOLDER = Utils.join(Gitlet.DOT_GITLET, "commits");

    public Commit(String message, List<String> parent, HashMap<String, Blob> blobs) {
        this.message = message;
        this.parents = parent;
        this.blobs = blobs;

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

        if (parent == null) {
            cal.clear();
            this.timestamp = formatter.format(cal.getTime());
        } else {
            this.timestamp = formatter.format(cal.getTime());
        }
        generateSHA();
    }

    public static Commit fromFile(String sHA) {
        Commit c = Utils.readObject(Utils.join(COMMIT_FOLDER.getPath(), sHA), Commit.class);
        return c;
    }

    public void generateSHA() {
        sHA = Utils.sha1(Utils.serialize(this));
    }

    public String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public List<String> getParents() {
        return this.parents;
    }

    public String getSHA() {
        return this.sHA;
    }

    public void saveCommit() {
        File c = Utils.join(COMMIT_FOLDER, this.sHA);
        Utils.writeObject(c, this);
    }

    public HashMap<String, Blob> getBlobs() {
        return this.blobs;
    }

}
