package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Jessie Hong
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        try {
            String arg0 = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        Gitlet g = new Gitlet();
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                g.init();
                break;

            case "add":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                if (args.length > 1) {
                    g.add(args[1]);
                }
                break;
            case "commit":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                if (args.length > 1) {
                    g.commit(args[1]);
                }
                break;
            case "checkout":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                if (args.length == 2) {
                    if (args[1] != null) {
                        g.checkout3(args[1]);
                    }
                    return;
                }
                if (args.length == 3) {
                    if (args[1].equals("--") && args[2] != null) {
                        g.checkout(args[2]);
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                } else if (args.length == 4) {
                    if (args[2].equals("--") && args[1] != null && args[3] != null) {
                        g.checkout(args[1], args[3]);
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "log":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                g.log();
                break;
            case "rm":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                if (args.length == 2) {
                    g.rm(args[1]);
                }
                break;
            case "global-log":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                g.globalLog();
                break;
            case "find":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                if (args.length == 2) {
                    g.find(args[1]);
                }
                break;
            case "status":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                g.status();
                break;
            case "branch":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                if (args.length == 2) {
                    g.branch(args[1]);
                }
                break;
            case "rm-branch":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                if (args.length == 2) {
                    g.rmBranch(args[1]);
                }
                break;
            case "reset":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                if (args.length == 2) {
                    g.reset(args[1]);
                }
                break;
            case "merge":
                if (!notInitialisedError(initialised(g))) {
                    return;
                }
                if (args.length == 2) {
                    g.merge(args[1]);
                }
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }

    private static boolean initialised(Gitlet g) {
        return g.DOT_GITLET.exists();
    }

    private static boolean notInitialisedError(boolean b) {
        if (!b) {
            System.out.println("Not in an initialized Gitlet directory.");
        }
        return b;
    }
}
