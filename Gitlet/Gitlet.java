package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class Gitlet implements Serializable {
    //current working directory
    static final File CWD = new File(System.getProperty("user.dir"));
    //create .gitlet directory
    static final File DOT_GITLET = Utils.join(CWD, ".gitlet");

    private String headCommitSHA;
    private String activeBranch;
    private StagingArea stage;

    public Gitlet() {
        File s = Utils.join(DOT_GITLET, "staging_area");
        if (!s.exists()) {
            stage = new StagingArea();
        } else {
            stage = Utils.readObject(s, StagingArea.class);
        }
        File h = Utils.join(DOT_GITLET, "head_commit");
        if (h.exists()) {
            headCommitSHA = Utils.readContentsAsString(h);
        } else {
            headCommitSHA = "";
        }
        File a = Utils.join(DOT_GITLET, "active_branch");
        if (a.exists()) {
            activeBranch = Utils.readContentsAsString(a);
        } else {
            activeBranch = "";
        }
    }

    public void init() {
        if (DOT_GITLET.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        DOT_GITLET.mkdirs();
        if (!Commit.COMMIT_FOLDER.exists()) {
            Commit.COMMIT_FOLDER.mkdirs();
        }
        if (!Branch.BRANCH_FOLDER.exists()) {
            Branch.BRANCH_FOLDER.mkdirs();
        }
        stage = new StagingArea();

        Commit initial = new Commit("initial commit", null, new HashMap<>());
        initial.saveCommit();
        String sHA = initial.getSHA();
        headCommitSHA = sHA;
        Branch m = new Branch("master", headCommitSHA);
        m.saveBranch();
        String aB = m.getBranchname();
        activeBranch = aB;
        write();
    }

    public void commit(String commitmsg) {
        if (stage.stageEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (commitmsg.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        //read from computer the head commit obj and staging area
        //clone HEAD commit
        //modify messages and timestamps according to user input
        //Use staging area to modify the files tracked by the new commit

        Commit parent = Commit.fromFile(headCommitSHA);

        HashMap<String, Blob> parentBlobs = parent.getBlobs();
        HashMap<String, Blob> newCommitBlobs = new HashMap<>();

        if (parentBlobs != null) {
            newCommitBlobs.putAll(parentBlobs);
        }

        newCommitBlobs.putAll(stage.getAddition());

        newCommitBlobs.keySet().removeAll(stage.getRemoval().keySet());

        //find the parents of the commit and make a list of their sha values
        List<String> parents = new ArrayList<>();
        parents.add(headCommitSHA);

        Commit newcommit = new Commit(commitmsg, parents, newCommitBlobs);

        newcommit.saveCommit();
        headCommitSHA = newcommit.getSHA();
        stage.clearStage();
        Branch br = Branch.fromFile(activeBranch);
        br.setLatestCommit(headCommitSHA);
        write();
    }

    public void add(String filename) {
        if (!new File(filename).exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blob newblob = new Blob(filename);

        Commit head = Commit.fromFile(headCommitSHA);
        HashMap<String, Blob> headBlobs = head.getBlobs();

        if (headBlobs.containsKey(filename)) {
            String filesha = headBlobs.get(filename).getSHA();
            String newblobsha = newblob.getSHA();

            if (filesha.equals(newblobsha)) {
                if (stage.getRemoval().containsKey(filename)) {
                    stage.getRemoval().remove(filename);
                }
                write();
                return;
            }
        }
        String s = Utils.readContentsAsString(newblob.getDirectory());
        Utils.writeContents(newblob.getDirectory(), s);

        headBlobs.put(filename, newblob);
        stage.addToAdditionStage(filename, newblob);
        write();
    }

    public void checkout(String filename) {
        File headcommitFile = Utils.join(CWD, filename);
        if (!headcommitFile.exists()) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Commit headcommit = Commit.fromFile(headCommitSHA);
        Blob headBlob = headcommit.getBlobs().get(filename);
        Utils.writeContents(headcommitFile, headBlob.getContent());
        write();
    }

    public void checkout(String commitID, String filename) {
        /*
        1. process short id
        2. find the larger id
        3. normal operations
         */
        List<String> allCommits = Utils.plainFilenamesIn(Commit.COMMIT_FOLDER);
        if (commitID.length() < 40) {
            commitID = commitID.substring(0, 6);
            for (String id : allCommits) {
                if (id.substring(0, 6).equals(commitID)) {
                    commitID = id;
                }
            }
        }

        if (!allCommits.contains(commitID)) { //check if a file exists
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = Commit.fromFile(commitID);

        Blob commitBlob = commit.getBlobs().get(filename);
        if (commitBlob == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File givenFile = Utils.join(CWD, filename);
        Utils.writeContents(givenFile, commitBlob.getContent());
        write();
    }

    public void checkout3(String branchname) {
        List<String> branchOrdered = Utils.plainFilenamesIn(Branch.BRANCH_FOLDER);
        if (!branchOrdered.contains(branchname)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branchname.equals(activeBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        Branch givenBranch = Branch.fromFile(branchname);
        Branch headBranch = Branch.fromFile(activeBranch);

        Commit headCommit = Commit.fromFile(headBranch.getLatestCommit());
        Commit branchCommit = Commit.fromFile(givenBranch.getLatestCommit());

        untrackedFileChecker(headCommit, branchCommit);

        HashMap<String, Blob> temp = new HashMap<>();

        //loop through files in the current branch
        // check if the blobs exist in given branch and if it doesn't delete

        for (Blob b : headCommit.getBlobs().values()) {
            if (!branchCommit.getBlobs().containsValue(b)) {
                b.getDirectory().delete();
            }
        }

        if (branchCommit.getParents() != null) {
            temp.putAll(Commit.fromFile(branchCommit.getParents().get(0)).getBlobs());
            if (branchCommit.getParents().size() == 2) {
                temp.putAll(Commit.fromFile(branchCommit.getParents().get(1)).getBlobs());
            }
        }
        for (Blob bHB: temp.values()) {
            Blob bGB = branchCommit.getBlobs().get(bHB.getFileName());
            if (bGB == null && bHB.getDirectory().isFile()) {
                bHB.getDirectory().delete();
            }
        }
        for (Blob b : branchCommit.getBlobs().values()) {
            Utils.writeContents(b.getDirectory(), b.getContent());
        }

        if (!headBranch.equals(givenBranch)) {
            stage.clearStage();
        }
        activeBranch = branchname;
        headCommitSHA = givenBranch.getLatestCommit();
        write();
    }

    public void log() {
        Commit current = Commit.fromFile(headCommitSHA);
        List<String> parentSHA = current.getParents();
        while (current != null) {
            System.out.println("===");
            System.out.println("commit " + current.getSHA());
            if (parentSHA.size() == 2) {
                System.out.println("Merge: " + parentSHA.get(0).substring(0, 7)
                        + " " + parentSHA.get(1).substring(0, 7));
            }
            System.out.println("Date: " + current.getTimestamp());
            System.out.println(current.getMessage());
            System.out.println();
            parentSHA = current.getParents();

            if (parentSHA != null) {
                current = Commit.fromFile(parentSHA.get(0));
            } else {
                current = null;
            }
        }

    }

    public void rm(String filename) {
        HashMap<String, Blob> b = Commit.fromFile(headCommitSHA).getBlobs();
        if (stage.getAddition().containsKey(filename)) {
            stage.getAddition().remove(filename);
        } else if (b.containsKey(filename)) {
            Blob bb = b.get(filename);
            //stage.getRemoval().put(filename, bb);
            stage.addToRemovalStage(filename, bb);

            if (bb.getDirectory().exists()) { //check if file exists
                Utils.restrictedDelete(filename);
            }

            write();
        } else {
            System.out.println("No reason to remove the file.");
        }
        write();
    }

    public void globalLog() {
        List<String> allCommits = Utils.plainFilenamesIn(Commit.COMMIT_FOLDER);
        for (String currentSHA : allCommits) {
            System.out.println("===");
            System.out.println("commit " + currentSHA);
            System.out.println("Date: " + Commit.fromFile(currentSHA).getTimestamp());
            System.out.println(Commit.fromFile(currentSHA).getMessage());
            System.out.println();
        }
    }

    public void find(String commitmsg) {
        boolean found = false;
        List<String> allCommits = Utils.plainFilenamesIn(Commit.COMMIT_FOLDER);
        for (String currentSHA : allCommits) {
            Commit current = Commit.fromFile(currentSHA);
            if (current.getMessage().equals(commitmsg)) {
                System.out.println(current.getSHA());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        List<String> branchOrdered = Utils.plainFilenamesIn(Branch.BRANCH_FOLDER);
        Collections.sort(branchOrdered);
        System.out.println("=== Branches ===");
        for (String br : branchOrdered) {
            if (activeBranch.equals(br)) {
                System.out.println("*" + br);
            } else {
                System.out.println(br);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String add : stage.getAddition().keySet()) {
            System.out.println(add);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String delete : stage.getRemoval().keySet()) {
            System.out.println(delete);
        }

        List<String> allFiles = Utils.plainFilenamesIn(CWD);
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        HashMap<String, Blob> entireStage = new HashMap<>();
        entireStage.putAll(stage.getAddition());
        entireStage.putAll(stage.getRemoval());

        for (String f : allFiles) {
            Blob b = new Blob(f);
            HashMap<String, Blob> commitBlobs = Commit.fromFile(headCommitSHA).getBlobs();
            if (b.getDirectory().exists()) {
                if (commitBlobs.containsValue(b)
                        && !b.getSHA().equals(commitBlobs.get(f).getSHA())
                        && !entireStage.containsKey(f)) { //containsValue(f)
                    System.out.print(b.getFileName());
                    System.out.println(" (modified) ");
                } else if (stage.getAddition().keySet().contains(f)
                        && !b.getSHA().equals(stage.getAddition().get(f).getSHA())) {
                    System.out.print(b.getFileName());
                    System.out.println(" (modified) ");
                }
            } else {
                if (stage.getAddition().keySet().contains(f)) {
                    System.out.print(b.getFileName());
                    System.out.println(" (deleted) ");
                } else if (!stage.getRemoval().keySet().contains(f)
                        && commitBlobs.containsValue(b)) {
                    System.out.print(b.getFileName());
                    System.out.println(" (deleted) ");
                }
            }
        }

        System.out.println("\n=== Untracked Files ===");
        HashMap<String, Blob> blobby = Commit.fromFile(headCommitSHA).getBlobs();
        for (String f : allFiles) {
            if (!blobby.containsKey(f) && !stage.getAddition().containsKey(f)) {
                System.out.println(f);
            }
        }
        System.out.println();
    }

    public void branch(String branchName) {
        List<String> branchOrdered = Utils.plainFilenamesIn(Branch.BRANCH_FOLDER);
        if (branchOrdered.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Branch newbranch = new Branch(branchName, headCommitSHA);
        newbranch.saveBranch();
    }

    public void rmBranch(String branchName) {
        List<String> branchOrdered = Utils.plainFilenamesIn(Branch.BRANCH_FOLDER);
        if (!branchOrdered.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (activeBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        } else {
            File f = Utils.join(Branch.BRANCH_FOLDER, branchName);
            f.delete();
        }
    }

    public void reset(String commitID) {
        List<String> allCommits = Utils.plainFilenamesIn(Commit.COMMIT_FOLDER);
        if (commitID.length() < 40) {
            commitID = commitID.substring(0, 6);
            for (String id : allCommits) {
                id = id.substring(0, 6);
            }
        }
        if (!allCommits.contains(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit toreset = Commit.fromFile(commitID); //branchCommit
        Commit hCommit = Commit.fromFile(Branch.fromFile(activeBranch).getLatestCommit());
        untrackedFileChecker(hCommit, toreset);

        for (String blo : toreset.getBlobs().keySet()) {
            checkout(commitID, blo);
        }

        for (String h : hCommit.getBlobs().keySet()) {
            if (!toreset.getBlobs().keySet().contains(h)) {
                Utils.restrictedDelete(h);
            }
        }

        headCommitSHA = commitID;
        Branch.fromFile(activeBranch).setLatestCommit(headCommitSHA);
        Branch.fromFile(activeBranch).saveBranch();
        stage.clearStage();
        write();
    }

    public void merge(String branchname) {
        List<String> branchOrdered = Utils.plainFilenamesIn(Branch.BRANCH_FOLDER);
        if (!branchOrdered.contains(branchname)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (!stage.stageEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (activeBranch.equals(branchname)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        Branch bN = Branch.fromFile(branchname);
        Branch aB = Branch.fromFile(activeBranch);
        //last common ancestor of activeBranch and givenBranch
        String lCA = ancestorOf(aB.getLatestCommit(), Commit.fromFile(bN.getLatestCommit()));

        if (lCA.equals(aB.getLatestCommit())) {
            aB.setLatestCommit(bN.getLatestCommit());
            checkout3(branchname);
            System.out.println("Current branch fast-forwarded.");
        } else if (lCA.equals(bN.getLatestCommit())) {
            System.out.println("Given branch is an ancestor of the current branch.");
        } else {
            Commit headCommit = Commit.fromFile(aB.getLatestCommit()); //commit in active branch
            Commit otherCommit = Commit.fromFile(bN.getLatestCommit()); //commit in other branch
            Commit split = Commit.fromFile(lCA);

            untrackedFileChecker(headCommit, otherCommit);

            boolean mergeConflict = mergeHelper(headCommit, otherCommit, split);
            mergeCommit(aB, bN, headCommit, otherCommit, lCA);
            if (mergeConflict) {
                System.out.println("Encountered a merge conflict.");
            }
        }
        write();
    }

    private boolean mergeHelper(Commit headCommit, Commit otherCommit, Commit split) {
        boolean hasmergeConflict = false;
        for (String filenameInSplit : split.getBlobs().keySet()) {
            Blob fileInHead = headCommit.getBlobs().get(filenameInSplit);
            Blob fileInOther = otherCommit.getBlobs().get(filenameInSplit);
            Blob fileInSplit = split.getBlobs().get(filenameInSplit);
            // 3b. modified both OTHER and HEAD and in different ways
            /*
                case 1: file in OTHER but not in HEAD ; file modified in OTHER
                case 2: file not in OTHER but in HEAD; file modified in HEAD
                case 3: file in both OTHER and HEAD; file modified in both OTHER and HEAD;
                        modification in OTHER != modification in HEAD
             */

            if (fileInHead != null && fileInOther != null
                    && !fileInOther.getSHA().equals(fileInSplit.getSHA())
                    && fileInHead.getSHA().equals(fileInSplit.getSHA())) {
                // 1. content modified in OTHER branch but not ACTIVE branch
                checkout(otherCommit.getSHA(), filenameInSplit);
                stage.addToAdditionStage(filenameInSplit, fileInOther);
            } else if (fileInOther != null && fileInHead == null
                    && !fileInOther.getSHA().equals(fileInSplit.getSHA())) {
                // 3b case 1
                hasmergeConflict = true;
                writeMergeConflict(fileInHead, fileInOther);
            } else if (fileInOther == null && fileInHead != null
                    && !fileInHead.getSHA().equals(fileInSplit.getSHA())) {
                // 3b case 2
                hasmergeConflict = true;
                writeMergeConflict(fileInHead, fileInOther);
            } else if (fileInOther != null && fileInHead != null
                    && !fileInOther.getSHA().equals(fileInSplit.getSHA())
                    && !fileInHead.getSHA().equals(fileInSplit.getSHA())
                    && !fileInOther.getSHA().equals(fileInHead.getSHA())) {
                // 3b case 3
                hasmergeConflict = true;
                writeMergeConflict(fileInHead, fileInOther);
            } else if (fileInHead != null && fileInOther == null
                    && fileInHead.getSHA().equals(fileInSplit.getSHA())) {
                rm(filenameInSplit);
            }
        }
        // now dealing with all files not in original split but in OTHER or ACTIVE
        for (String filenameInOther : otherCommit.getBlobs().keySet()) {
            Blob fileInHead = headCommit.getBlobs().get(filenameInOther);
            Blob fileInOther = otherCommit.getBlobs().get(filenameInOther);
            Blob fileInSplit = split.getBlobs().get(filenameInOther);
            // 6. if file not present in ACTIVE branch but present in OTHER branch
            if (fileInSplit == null && fileInHead == null && fileInOther != null) {
                checkout(otherCommit.getSHA(), filenameInOther);
                stage.addToAdditionStage(filenameInOther, fileInOther);
            }
        }
        write();
        return hasmergeConflict;
    }
    public void mergeCommit(Branch branch1, Branch branch2,
                            Commit parent1, Commit parent2, String lCA) {
        //parent1 = active branch commit, parent2 = other branch commit
        //branch1 = active branch, branch2 = other branch
        if (stage.stageEmpty()) { //do I even need this?
            return;
        }
        String commitmsg = "Merged " + branch2.getBranchname()
                + " into " + branch1.getBranchname() + ".";

        HashMap<String, Blob> parentBlobs = parent1.getBlobs();
        //just the blobs from activeBranch will do as
        // I'm checking out and removing along the way in mergeHelper
        HashMap<String, Blob> newCommitBlobs = new HashMap<>();

        if (parentBlobs != null) {
            newCommitBlobs.putAll(parentBlobs);
        }
        newCommitBlobs.putAll(stage.getAddition());
        newCommitBlobs.keySet().removeAll(stage.getRemoval().keySet());

        List<String> parents = new ArrayList<>();
        parents.add(parent1.getSHA());
        parents.add(parent2.getSHA());

        Commit newcommit = new Commit(commitmsg, parents, newCommitBlobs);

        newcommit.saveCommit();
        headCommitSHA = newcommit.getSHA();
        stage.clearStage();
        Branch br = Branch.fromFile(activeBranch);
        br.setLatestCommit(headCommitSHA);
        br.setSplitCommit(lCA);
        write();
    }

    private String ancestorOf(String latestHeadCommitSHA, Commit otherCommit) {
        Set<String> ancestors = new HashSet<>();
        ancestorsOf(ancestors, otherCommit);
        List<String> headCommitShas = new ArrayList<>();
        headCommitShas.add(latestHeadCommitSHA);
        do {
            String sha = headCommitShas.remove(0);
            if (ancestors.contains(sha)) {
                return sha;
            } else {
                Commit c = Commit.fromFile(sha);
                headCommitShas.addAll(c.getParents());
            }
        } while (headCommitShas.size() != 0);
        return null;
    }

    private void ancestorsOf(Set<String> ancestors, Commit c) {
        ancestors.add(c.getSHA());
        List<String> parents = c.getParents();
        if (parents != null && !parents.isEmpty()) {
            for (String p : parents) {
                ancestorsOf(ancestors, Commit.fromFile(p));
            }
        }
    }

    private void writeMergeConflict(Blob headBlob, Blob otherBlob) {
        String head = "<<<<<<< HEAD\n";
        String divider = "=======\n";
        String end = ">>>>>>>\n";
        String headContent;
        String otherContent;
        if (headBlob == null) {
            headContent = "";
        } else {
            headContent = headBlob.getContent();
        }
        if (otherBlob == null) {
            otherContent = "";
        } else {
            otherContent = otherBlob.getContent();
        }
        String s = head + headContent + divider + otherContent + end;
        Utils.writeContents(headBlob.getDirectory(), s);
    }

    private void untrackedFileChecker(Commit headCommit, Commit branchCommit) {
        List<String> allfilesDir = Utils.plainFilenamesIn(CWD);

        if (!branchCommit.getBlobs().isEmpty()) {
            for (String fn : allfilesDir) {
                Blob b = new Blob(fn);

                Blob bHB = headCommit.getBlobs().get(fn);
                Blob bGB = branchCommit.getBlobs().get(fn);

                boolean currNotbGBVer = b.getDirectory().isFile() && bGB != null
                        && !b.getSHA().equals(bGB.getSHA());

                if (bHB == null) {
                    if (currNotbGBVer) {
                        untrackedFileError();
                    }
                } else if (currNotbGBVer && !bHB.getSHA().equals(b.getSHA())) {
                    untrackedFileError();
                }
            }
            /*
            1. not tracked by the head commit
            2. current version does not match the version destination
               commit has (commit given by branchname)
            3. not saved for addition
            if any one of these fails, untracked
             */
        }
    }

    private void untrackedFileError() {
        System.out.println("There is an untracked file in the way; "
                + "delete it, or add and commit it first.");
        System.exit(0);
    }

    private void write() {
        Utils.writeContents(Utils.join(DOT_GITLET, "head_commit"), headCommitSHA);
        Utils.writeObject(Utils.join(DOT_GITLET, "staging_area"), stage);
        Utils.writeContents(Utils.join(DOT_GITLET, "active_branch"), activeBranch);
    }
}