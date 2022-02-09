package gitlet;

import java.io.File;
import java.io.Serializable;

public class Branch implements Serializable {
    private String branchname;
    private String latestCommitSHA;
    private String branchSplitSHA;
    //branchSplitSHA is the (last) node this branch was branched off from -- i.e., split point

    static final File BRANCH_FOLDER = Utils.join(Gitlet.DOT_GITLET, "branches");

    public Branch(String name, String sHA) {
        this.branchname = name;
        this.latestCommitSHA = sHA;
        this.branchSplitSHA = sHA; //split point
    }

    public static Branch fromFile(String branchname) {
        Branch b = Utils.readObject(Utils.join(BRANCH_FOLDER.getPath(), branchname), Branch.class);
        return b;
    }

    public String getLatestCommit() {
        return this.latestCommitSHA;
    }

    public String getBranchname() {
        return this.branchname;
    }

    public void setLatestCommit(String newcommitSHA) {
        this.latestCommitSHA = newcommitSHA;
        saveBranch();
    }

    public void setSplitCommit(String newSplitSHA) {
        this.branchSplitSHA = newSplitSHA;
        saveBranch();
    }

    public void saveBranch() {
        File b = Utils.join(BRANCH_FOLDER, this.branchname);
        Utils.writeObject(b, this);
    }
}

