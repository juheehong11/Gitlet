package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    private String fileName;
    private File blobdirectory;
    private String blobcontent;
    private String SHA;

    public Blob(String filename) {
        this.fileName = filename;
        this.blobdirectory = Utils.join(Gitlet.CWD, filename);
        generateSHA();
        if (blobdirectory.exists()) {
            blobcontent = Utils.readContentsAsString(blobdirectory);
        }
    }

    public void generateSHA() {
        byte[] b = Utils.readContents(blobdirectory);
        SHA = Utils.sha1(b + fileName);
        SHA = Utils.sha1(b, fileName);
    }

    public File getDirectory() {
        return this.blobdirectory;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getContent() {
        return this.blobcontent;
    }

    public String getSHA() {
        return this.SHA;
    }

}
