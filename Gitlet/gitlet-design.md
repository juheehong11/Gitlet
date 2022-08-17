# Gitlet Design Document

**Name**: Juhee Hong

## Classes and Data Structures

###Gitlet

####Fields

1. static final File CWD
2. static final File DOT_GITLET
3. headCommitSHA - String of head's SHA
4. activeBranch - String of name of active branch
5. stage - staging area instance

### Commit

#### Fields

1. Message - contains the message of a commit.
2. Timestamp - time when the commit was made. Assigned by the constructor.
3. Parent - the parent commit's SHA of the current commit.
4. SHA - the current commit's SHA
5. Blobs - a hashmap of all blobs this commit tracks
6. Commit Folder File for persistence


### Blob

#### Fields

1. Filename - the file name of the blob
2. Blob directory - the blob's directory (joined with CWD)
3. Blob content - string of blob's contents if blob directory exists
4. SHA - this blob's SHA

###Branch

####Fields

1. Branch name - the name of the branch
2. Latest Commit SHA - the SHA of the most recent commit this branch tracks
3. Branch parent SHA - the SHA of the commit this branch branched off from
4. Branch folder file for persistence

###StagingArea

####Fields

1. addition - the stage for adding; hashmap
2. removal - the stage for removal; hashmap

## Algorithms

###Init()
If .gitlet exists, exit. Otherwise, make .gitlet directory, initialise variables.
Create new commit and master branch. write().

###Commit()
If stage is empty, exit. If the message is "", exit.
Get the blobs tracked by parent of commit. If the parent does track blobs, add all to a new hashmap.
Use putAll() to put all files in the addition stage into the same hashmap so that any repeated keys will be replaced with the stage's files.
Make a new commit with the blobs as the newly created hashmap, and put it into commits.
Clear stage and write().

###Add()
Create a new blob with the given filename and see if its directory exists. If not, print "File does not exist." and exit.
Get the head commit and the blobs the head commit tracks.
If head's blobs contains the filename and file SHA's and blob SHA's match, no need to stage. Exit.
Otherwise, add to stage, and write().

###Checkout()

####checkout filename
If file doesn't exist, print "File does not exist in that commit." and exit.
Otherwise, take the blob contents from the head commit and writeObject to the given file.

####checkout (commit id) (filename)
If commit at given commit id doesn't exist, print "No commit with that id exists." and exit.
If filename not in commit's blobs, print "File does not exist in that commit." and exit.
Otherwise, take the blob contents from the given commit and writeObject to the given file.

###checkout (branch)
Takes all files in the commit at the head of the given branch, and puts them in the working directory,
overwriting the versions of the files that are already there if they exist.
Also, at the end of this command, the given branch will now be considered the current branch (HEAD).
Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
The staging area is cleared, unless the checked-out branch is the current branch.

###Log()

Starting from the head commit, for each commit print out the SHA, timestamp, and commit message.
Move the temporary pointer to head commit backwards and repeat until the pointer points to null.

###Status()

Displays what branches currently exist, and marks the current branch with a *.
Also displays what files have been staged for addition or removal.

###Reset()
Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit.
Also moves the current branch’s head to that commit node.

###untrackedFileChecker()

A helper method for checkout <branch> (checkout3) and reset; checks if there are any untracked files.
  
###Merge()
Checks for error/exit cases, uses untrackedFileChecker() to check for any untracked files (in which case it exits).
Otherwise, calls MergeHelper() for actual merging. 

###MergeHelper()
Does the actual merging.
Based on the requirements for merge, actually only checks for cases 1, 4, and 6.

            1. a file from splitpoint: content modified in OTHER branch but not ACTIVE branch
            --> save the file in OTHER branch (save the modified one)
            --> add this change to the staging area: stage for addition b/c content changed to OTHER branch's

            2. a file from splitpoint: content same in OTHER branch but modified in ACTIVE branch
            --> save the file in ACTIVE branch (save the modified one); keep file from active branch
            --> no need to stage because ACTIVE branch's contents remain the same

            3a. a file from splitpoint: file not present in both ACTIVE and OTHER branch (deleted in both branches)
            --> essentially, this is equivalent to:
                modified in both OTHER and HEAD : modified in the same way
            --> no need to save anything or commit; have the file exactly as it is in the ACTIVE BRANCH
             
             3b. a file from splitpoint: file in (or maybe absent from) ACTIVE branch doesn't match with file
                                          from splitpt or file in OTHER branch
                                          (and vice versa); modified both OTHER and HEAD and in different ways
                case 1: file in OTHER but not in HEAD ; file modified in OTHER
                case 2: file not in OTHER but in HEAD; file modified in HEAD
                case 3: file in both OTHER and HEAD; file modified in both OTHER and HEAD;
                        modification in OTHER != modification in HEAD
             --> CONFLICT error

             4. a file from splitpoint: file unmodified and present in ACTIVE branch, but not present in OTHER branch
             --> remove the file
             --> stage for removal since ACTIVE branch's file content is changed

             5. a file from splitpoint: file not present in ACTIVE branch, but unmodified and present in OTHER branch
             --> file remains removed
             --> no need to stage as result the same as in ACTIVE branch


             

             6. a file NOT present in splitpoint: if file not present in ACTIVE branch but present in OTHER branch
             --> save the file from OTHER branch
             --> stage for addition since ACTIVE branch missing the file

             7. a file NOT present in splitpoint: if file present in ACTIVE branch but not present in OTHER branch
             --> keep
             --> not need to stage

###Merge()
Checks for error/exit cases, uses untrackedFileChecker() to check for any untracked files (in which case it exits).
Otherwise, calls MergeHelper() for actual merging.

###MergeHelper()
Does the actual merging.
Based on the requirements for merge, actually only checks for cases 1, 4, and 6.

            1. a file from splitpoint: content modified in OTHER branch but not ACTIVE branch
            --> save the file in OTHER branch (save the modified one)
            --> add this change to the staging area: stage for addition b/c content changed to OTHER branch's

            2. a file from splitpoint: content same in OTHER branch but modified in ACTIVE branch
            --> save the file in ACTIVE branch (save the modified one); keep file from active branch
            --> no need to stage because ACTIVE branch's contents remain the same

            3a. a file from splitpoint: file not present in both ACTIVE and OTHER branch (deleted in both branches)
            --> essentially, this is equivalent to:
                modified in both OTHER and HEAD : modified in the same way
            --> no need to save anything or commit; have the file exactly as it is in the ACTIVE BRANCH
             
             3b. a file from splitpoint: file in (or maybe absent from) ACTIVE branch doesn't match with file
                                          from splitpt or file in OTHER branch
                                          (and vice versa); modified both OTHER and HEAD and in different ways
                case 1: file in OTHER but not in HEAD ; file modified in OTHER
                case 2: file not in OTHER but in HEAD; file modified in HEAD
                case 3: file in both OTHER and HEAD; file modified in both OTHER and HEAD;
                        modification in OTHER != modification in HEAD
             --> CONFLICT error

             4. a file from splitpoint: file unmodified and present in ACTIVE branch, but not present in OTHER branch
             --> remove the file
             --> stage for removal since ACTIVE branch's file content is changed

             5. a file from splitpoint: file not present in ACTIVE branch, but unmodified and present in OTHER branch
             --> file remains removed
             --> no need to stage as result the same as in ACTIVE branch


             

             6. a file NOT present in splitpoint: if file not present in ACTIVE branch but present in OTHER branch
             --> save the file from OTHER branch
             --> stage for addition since ACTIVE branch missing the file

             7. a file NOT present in splitpoint: if file present in ACTIVE branch but not present in OTHER branch
             --> keep
             --> not need to stage

###Write()

Private method.
Calls writeObject for commits, headCommitSHA, stage, activeBranch, and branches.

## Persistence

Call write() whenever one of the commits, headCommitSHA, stage, activeBranch, and branches has been updated. I.e., write() in the following methods:
* init()
* commit()
* add()

Call saveCommit() instance method whenever a new commit is created.

Call saveBranch() instance method whenever a new branch is created.
