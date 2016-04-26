/* Gitlet.java
 * 
 * @author Z Wang
 * login: blr
 * CS61B Spring 2015
 * Project 2
 *
 *
 * This is the brains of the operation. Gitlet builds the Commit 
 * tree with Commit objects, and in turn acts on these objects 
 * according to various commands. The main method acts as a
 * GUI for the user to input commands. These range from displaying
 * his Commit history to adding and removing files. To save certain
 * data (i.e. state of the Commit tree) through separate runs of the
 * program, I serialize several key structures at the end of a run.
 *
 * Specifically, Commits are stored in various data structures, and
 * files in the system are names by their original file name with
 * the corresponding Commit ID in the front.
 *
 * Apologies in advance for the many various variable names.
 * I changed variable names in identical statements from different
 * methods so as to know the origin of errors during testing.
 *
 *
 */

import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.FileNotFoundException;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Stack;
import java.util.ArrayList;


public class Gitlet {

    private static final int INITIALID = 1000000; // refers to the ID of the first Commit
    private static int IDLENGTH = 7; // refers to length of ID, exclusive
    private static final StandardCopyOption REPLACE = StandardCopyOption.REPLACE_EXISTING;

    // special word used to reference the next unique Commit ID
    private static final String SECRETKEY = "illuminatedbythedawn";

    // maps all Commits with their unique ID's
    private static Map<Integer, Commit> all = new TreeMap<Integer, Commit>();

    // maps all branch heads to their corresponding Commits
    private static Map<String, Commit> pointers = new HashMap<String, Commit>();

    // maps Commit messages with the ID's that have that message
    private static Map<String, TreeSet<Integer>> byMessage 
        = new HashMap<String, TreeSet<Integer>>();

    private static String currBranch = "none";
    private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static boolean init = new File(".gitlet").exists();

    
    /* Method that deserializes the .gitlet data from a previous run. This
     * essentially loads up the most recent Gitlet session, and gives the
     * main data structures values based on the serialized files.
     */
    private static void decerealize() {
        try {
            FileInputStream treeF = new FileInputStream(".gitlet/tree.ser");
            ObjectInputStream treeO = new ObjectInputStream(treeF);
            all = (TreeMap) treeO.readObject();
            treeO.close();
            treeF.close();

            FileInputStream pointF = new FileInputStream(".gitlet/point.ser");
            ObjectInputStream pointO = new ObjectInputStream(pointF);
            pointers = (HashMap) pointO.readObject();
            pointO.close();
            pointF.close();

            FileInputStream branchF = new FileInputStream(".gitlet/branch.ser");
            ObjectInputStream branchO = new ObjectInputStream(branchF);
            currBranch = (String) branchO.readObject();
            branchO.close();
            branchF.close();

            FileInputStream messF = new FileInputStream(".gitlet/message.ser");
            ObjectInputStream messO = new ObjectInputStream(messF);
            byMessage = (HashMap) messO.readObject();
            messO.close();
            messF.close();
        } catch (IOException io) {
            //io.printStackTrace();
        } catch (ClassNotFoundException cnf) {
            //cnf.printStackTrace();
        }
    }

    /* Initializes a new .gitlet system in a directory. Only occurs if there
     * is no existing .gitlet file system in place. Initializes all subfolders
     * and adds the first initial Commit to the system.
     */
    private static void caseInit() {
        if (init) {
            System.out.println("A gitlet version control system already "
                + "exists in the current directory.");
            return;
        }
        File un = new File(".gitlet/files");
        File deux = new File(".gitlet/staged");
        File trois = new File(".gitlet/removed");
        init = un.mkdirs();
        deux.mkdir();
        trois.mkdir();

        // Creates empty Commit as the first
        Date date0 = new Date();
        Commit don = new Commit(INITIALID, FORMAT.format(date0), "initial commit", null);
        all.put(INITIALID, don);
        currBranch = "master";
        pointers.put(currBranch, don);
        byMessageAdd(byMessage, "initial commit", INITIALID);

        // secret keyword used to keep track of the next Commit ID between sessions
        // I hoped/assumed that nobody would use this as a message.
        byMessageAdd(byMessage, SECRETKEY, INITIALID + 1);
    }

    /* Creates a new Commit and adds it to the end of the current branch
     * head. Takes in any files in staged folder as new files, inherits old
     * files from the new Commit's parent, and leaves out any files specified
     * in the removed folder.
     */
    private static void caseCommit(String message) {
        ArrayList<String> staged = new ArrayList<String>();
        getAllFiles(".gitlet/staged/", staged, "");
        ArrayList<String> removed = new ArrayList<String>();
        getAllFiles(".gitlet/removed/", removed, "");
        if ((staged != null && staged.size() < 1) 
            && (removed != null && removed.size() < 1)) {
            System.out.println("No changes added to the commit.");
            return;
        }

        // initiates new Commit and adds it to neccessary structures
        Commit old = pointers.get(currBranch);
        Date date1 = new Date();
        int code = getCode(byMessage);
        Commit knu = new Commit(code, FORMAT.format(date1), message, old);
        old.addChild(knu);
        pointers.put(currBranch, knu);
        all.put(code, knu);
        byMessageAdd(byMessage, message, code);

        Set<String> dontChange = old.getAllFiles();
        Set<String> allPrev = new HashSet<String>(dontChange);

        // moves files in staged folder to file system
        for (String s : staged) {
            allPrev = findAndDestroy(allPrev, s);
            String newName = Integer.toString(knu.getId()) + s;
            try {
                makeThatDir(newName, "files");
                Path begin = Paths.get(".gitlet/staged/" + s);
                Path end = Paths.get(".gitlet/files/" + newName);
                Files.move(begin, end, REPLACE);
                cleanUp(s, "staged");
            } catch (IOException io) {
                //io.printStackTrace();
                return;
            }
            knu.addFile(newName);
            if (knu.getExiled().contains(s)) {
                knu.remExiled(s);
            }
        }

        // removes files in removed folder while also removing them from
        // the inherited files of the new Commit
        for (String r : removed) {
            allPrev = findAndDestroy(allPrev, r);
            File bad = new File(".gitlet/removed/" + r);
            bad.delete();
            cleanUp(r, "removed");
            knu.addExiled(r);
        }

        knu.beInherited(allPrev);
    }

    /* Helper method called from main that checks if
     * adding a user-specified file is valid, and if so,
     * copies it into the staged folder in preparation for
     * the next Commit.
     * Returns false only if there has been an error and the
     * loop back in main should be broken.
     */
    private static boolean caseAdd(String curr, Commit don, HashSet<String> files) {
        File currFile = new File(curr);
        if (!currFile.exists()) {
            System.out.println("File does not exist.");
            return false;
        }
        
        // checks if this version has already been committed
        for (String eachFi : files) {
            if (eachFi.substring(7).equals(curr)) {
                if (equals(new File(".gitlet/files/" + eachFi), currFile)) {
                    System.out.println("File has not been "
                        + "modified since the last commit.");
                    return false;
                }
            }
        }

        // if the file is scheduled to be removed, undo
        File rem = new File(".gitlet/removed/" + curr);
        if (rem.exists()) {
            rem.delete();
            return false;
        }

        // copies the file into staged folder
        makeThatDir(curr, "staged");
        Path source = Paths.get(curr);
        Path destination = Paths.get(".gitlet/staged/" + curr);
        try {
            Files.copy(source, destination, REPLACE);
        } catch (IOException io) {
            //io.printStackTrace();
            return false;
        }
        return true;
    }

    /* Helper method called from main that checks if a 
     * user-specified file can be removed, and if so,
     * find it in the files folder and copy it to
     * the removed folder for the next Commit.
     */
    private static void caseRm(String dontWant) {

        // if file has been staged, unstage
        File inStage = new File(".gitlet/staged/" + dontWant);
        if (inStage.exists()) {
            inStage.delete();
            return;
        }

        Commit currCommit = pointers.get(currBranch);
        Set<String> allF = currCommit.getAllFiles();
        
        // finds the file in files folder and copies it
        for (String eau : allF) {
            if (eau.substring(7).equals(dontWant)) {
                makeThatDir(dontWant, "removed");
                Path source = Paths.get(".gitlet/files/" + eau);
                Path destination = Paths.get(".gitlet/removed/" + dontWant);
                try {
                    Files.copy(source, destination, REPLACE);
                    return;
                } catch (IOException io) {
                    //io.printStackTrace();
                    return;
                }
            }
        }

        // if file was not found in the files folder
        System.out.println("No reason to remove the file.");
    }

    /* Prints out a log of Commits and their data from
     * the history of the current branch.
     */
    private static void caseLog() {
        Commit currNode = pointers.get(currBranch);
        while (currNode != null) {
            System.out.println("====");
            System.out.println("Commit" + Integer.toString(currNode.getId()) + ".");
            System.out.println(currNode.getTime());
            System.out.println(currNode.getMessage());
            System.out.println();
            currNode = currNode.getParent();
        }
    }

    /* Prints out a log of Commits and their data from
     * all Commits in the Commit tree, not just those
     * in the current branch's history.
     */
    private static void caseGlobalLog() {
        for (Commit curr : all.values()) {
            System.out.println("====");
            System.out.println("Commit" + Integer.toString(curr.getId()) + ".");
            System.out.println(curr.getTime());
            System.out.println(curr.getMessage());
            System.out.println();
        }
    }

    /* Finds Commits that have a given message.
     * If Commits are found, their ID's are printed.
     */
    private static void caseFind(String desired) {
        TreeSet<Integer> found = byMessage.get(desired);
        if (found == null) {
            System.out.println("Found no commit with that message");
            return;
        }
        for (int i: found) {
            System.out.println(Integer.toString(i));
        }
    }

    /* Prints out current gitlet system status summary.
     * This includes all branches, specifying the current
     * branch, as well as files in the removed and staged
     * folders destined for action in the upcoming Commit.
     */
    private static void caseStatus() {
        System.out.println("=== Branches ===");
        for (String b : pointers.keySet()) {
            if (b.equals(currBranch)) {
                System.out.println("*" + b);
            } else {
                System.out.println(b);
            }
        }

        System.out.println("\n=== Staged Files ===");
        ArrayList<String> stag = new ArrayList<String>();
        getAllFiles(".gitlet/staged/", stag, "");
        for (String s : stag) {
            System.out.println(s);
        }

        System.out.println("\n=== Files Marked for Removal ===");
        ArrayList<String> remo = new ArrayList<String>();
        getAllFiles(".gitlet/removed/", remo, "");
        for (String r : remo) {
            System.out.println(r);
        }
    }

    /* Creates a new branch and appoints its initial head
     * as the current branch's head. Does not automatically
     * switch into the newly created branch.
     */
    private static void caseBranch(String newBranch) {
        if (pointers.containsKey(newBranch)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        pointers.put(newBranch, pointers.get(currBranch));
    }

    /* Removes a branch from the system--specifically, the branch
     * and head pointer entry is deleted, though the Commit tree remains
     * the same.
     */
    private static void caseRmBranch(String badBranch) {
        if (badBranch.equals(currBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        Commit test = pointers.remove(badBranch);
        if (test == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
    }

    /* Helper method for a "checkout" call that responds to
     * calls with one other argument. This other argument can
     * be a branch name or a file name, which must first be
     * determined. Then the appropriate action is done.
     *
     * Although the method takes in the full array of other
     * arguments, the method only works when tokens.length == 1.
     */

    private static void caseCheckout1(String[] tokens) {

        // call to helper method that verifies user intent
        boolean cont = dangerous();
        if (!cont) {
            return;
        }
        if (tokens.length == 1) {
            String anon = tokens[0];
            if (currBranch.equals(anon)) {
                System.out.println("No need to checkout the current branch.");
                return;
            }

            // first check if the argument is a branch, if found
            // the system switches to that branch
            if (pointers.containsKey(anon)) {
                Commit pivot = pointers.get(anon);
                Set<String> toAdd = pivot.getAllFiles();
                for (String a : toAdd) {
                    Path red = Paths.get(".gitlet/files/" + a);
                    Path black = Paths.get("./" + a.substring(7));
                    try {
                        Files.copy(red, black, REPLACE);
                    } catch (IOException io) {
                        //io.printStackTrace();
                        return;
                    }
                }
                currBranch = anon;
                return;
            }

            // else check if it is a file, if found the file is then
            // changed to its version at the last Commit
            Commit ref = pointers.get(currBranch);
            boolean gud = false;
            Set<String> haystack = ref.getAllFiles();
            for (String needle : haystack) {
                if (needle.substring(7).equals(anon)) {
                    Path commence = Paths.get(".gitlet/files/" + needle);
                    Path fini = Paths.get("./" + anon);
                    try {
                        Files.copy(commence, fini, REPLACE);
                    } catch (IOException io) {
                        //io.printStackTrace();
                        return;
                    }
                    gud = true;
                    return;
                }
            }

            if (!gud) {
                System.out.println("File does not exist in the most recent commit, "
                    + "or no such branch exists.");
            }
        }
    }

    /* Second checkout case which takes care of 2 other argument
     * calls. The first is a Commit ID, and the second a file name.
     * If both arguments are valid, the corresponding file
     * in the current workspace is reverted to the specified version.
     */
    private static void caseCheckout2(String[] tokens) {
        
        // user intent verification
        boolean cont = dangerous();
        if (!cont) {
            return;
        }

        if (tokens.length == 2) {
            boolean goood = false;
            int anonId = Integer.parseInt(tokens[0]);
            String anonFile = tokens[1];
            Commit where = all.get(anonId);
            if (where == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
            Set<String> sea = where.getAllFiles();
            for (String fish : sea) {
                if (fish.substring(7).equals(anonFile)) {
                    Path rise = Paths.get(".gitlet/files/" + fish);
                    Path fall = Paths.get("./" + anonFile);
                    try {
                        Files.copy(rise, fall, REPLACE);
                    } catch (IOException io) {
                        //io.printStackTrace();
                        return;
                    }
                    goood = true;
                }
            }

            if (!goood) {
                System.out.println("File does not exist in that commit.");
            }
        }
    }

    /* Resets all files in the current working directory
     * to the snapshot at a given Commit.
     */
    private static void caseReset(String[] tokens) {
        boolean ornah = dangerous();
        if (!ornah) {
            return;
        }

        int better = Integer.parseInt(tokens[0]);
        Commit times = all.get(better);
        if (times == null) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Set<String> tous = times.getAllFiles();
        for (String uns: tous) {
            Path noir = Paths.get(".gitlet/files/" + uns);
            Path blanc = Paths.get("./" + uns.substring(7));
            try {
                Files.copy(noir, blanc, REPLACE);
            } catch (IOException io) {
                //io.printStackTrace();
                return;
            }
        }

        // moves head pointer of current branch to
        // the specified Commit
        pointers.put(currBranch, times);
    }

    /* Handles merging between two branches. Appropriately
     * finds the difference in snapshots between the two
     * head Commits, and makes the corresponding changes
     * to the working directory. Files changed in both
     * have two versions in the workign directory, one
     * which is .conflicted.
     *
     * Doesn't actually change the Commit tree or its data.
     */
    private static void caseMerge(String[] tokens) {
        boolean thumbsup = dangerous();
        if (!thumbsup) {
            return;
        }
        String change = tokens[0];
        if (change.equals(currBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit other = pointers.get(change);
        if (other == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        Commit now = pointers.get(currBranch);
        Commit split = findSplit(now, other); // finds split point
        int spid = split.getId();
        Set<String> allOther = other.getAllFiles();
        Set<String> allNow = now.getAllFiles();
        Set<String> allInHere = new HashSet<String>();

        // files changed in other branch but not current
        Set<String> needToAdd = new HashSet<String>();
        Set<String> conflicted = new HashSet<String>();
        Map<String, String> oldNow = new HashMap<String, String>(); 
        for (String an : allNow) {
            if (Integer.parseInt(an.substring(0, 7)) > spid) {
                allInHere.add(an.substring(7));
            } else {
                oldNow.put(an.substring(7), an.substring(0, 7));
            }
        }
        for (String ao : allOther) {
            if (Integer.parseInt(ao.substring(0, 7)) <= spid) {
                continue;
            } else if (oldNow.keySet().contains(ao.substring(7))) {
                Path lao = Paths.get(".gitlet/files/" + ao);
                Path xin = Paths.get("./" + ao.substring(7));
                try {
                    Files.copy(lao, xin, REPLACE);
                } catch (IOException io) {
                    //io.printStackTrace();
                    return;
                }
            } else if (allInHere.contains(ao.substring(7))) {
                conflicted.add(ao);
            } else {
                needToAdd.add(ao);
            }
        }
        for (String ob : conflicted) {
            Path mano = Paths.get(".gitlet/files/" + ob);
            Path doigt = Paths.get("./" + ob.substring(7) + ".conflicted");
            try {
                Files.copy(mano, doigt, REPLACE);
            } catch (IOException io) {
                //io.printStackTrace();
                return;
            }
        }
        for (String og : needToAdd) {
            Path non = Paths.get(".gitlet/files/" + og);
            Path oui = Paths.get("./" + og.substring(7));
            try {
                Files.copy(non, oui, REPLACE);
            } catch (IOException io) {
                //io.printStackTrace();
                return;
            }
        }             
    }

    /* Checks initial conditions for a rebase call.
     * Includes a warning message and checks if one of the
     * specified branches is in the history of the other.
     *
     * Returns if the rebase call should proceed further.
     */

    private static boolean caseRebaseCheck(String[] tokens) {
        boolean proceed = dangerous();
        if (!proceed) {
            return false;
        }

        String otherB = tokens[0];
        if (otherB.equals(currBranch)) {
            System.out.println("Cannot rebase a branch onto itself.");
            return false;
        }

        Commit current = pointers.get(currBranch);
        Commit theOther = pointers.get(otherB);
        if (theOther == null) {
            System.out.println("A branch with that name does not exist.");
            return false;
        }

        Commit tiago = findSplit(current, theOther); // split point
        if (tiago == theOther) {
            System.out.println("Already up-to-date.");
            return false;
        }
        if (tiago == current) {
            pointers.put(currBranch, theOther);
            return false;
        }

        return true;
    }

    /* Rest of the rebase implementation. This involves
     * finding the Commits that should be replayed onto
     * the other branch, and then resolving the files that
     * belong in each replayed Commit.
     */
    private static void caseRebase(String[] tokens) {
        if (!caseRebaseCheck(tokens)) {
            return;
        }
        String otherB = tokens[0];
        Commit current = pointers.get(currBranch);
        Commit theOther = pointers.get(otherB);
        Commit tiago = findSplit(current, theOther);
        Commit poi = current;
        Stack<Commit> replay = new Stack<Commit>();
        while (poi != tiago) {
            replay.push(poi);
            poi = poi.getParent();
        }
        Commit place = theOther;
        Set<String> otherAllx = theOther.getAllFiles();
        Set<String> tiagoAllx = tiago.getAllFiles();

        // new files in the other branch head added since the split
        Set<String> otherDiff = new HashSet<String>(); 
        Set<String> tiagoAll = new HashSet<String>();
        for (String uno : tiagoAllx) {
            tiagoAll.add(uno.substring(7));
        }
        for (String dos : otherAllx) {
            if (!tiagoAll.contains(dos.substring(7))) {
                otherDiff.add(dos);
            } 
        }
        Commit nu, next;
        for (int i = 0; i < replay.size(); i++) {
            next = replay.pop();
            Date dateR = new Date();
            int codeR = getCode(byMessage);
            nu = new Commit(next, codeR, FORMAT.format(dateR), next.getMessage(), place);
            place.addChild(nu);
            all.put(codeR, nu);
            byMessageAdd(byMessage, nu.getMessage(), codeR);
            Set<String> nuAllx = nu.getAllFiles();
            Set<String> nuAll = new HashSet<String>();

            // files that haven't been updated in current Commit since the split
            HashMap<String, String> nuOld = new HashMap<String, String>(); 
            Set<String> nuInherit = nu.getInherited();
        
            // adds files from the other branch head added after the split
            for (String xyz : nuAllx) {
                if (Integer.parseInt(xyz.substring(0, 7)) <= tiago.getId()) {
                    nuOld.put(xyz.substring(7), xyz.substring(0, 7));
                }
                nuAll.add(xyz.substring(7));
            }
            for (String plz : otherDiff) {
                if (!nuAll.contains(plz.substring(7)) 
                    && !nu.getExiled().contains(plz.substring(7))) {
                    nuInherit.add(plz);
                }
            }
            
            // updates old files that have changes in the other branch
            for (String ono : otherAllx) {
                if (nuOld.keySet().contains(ono.substring(7)) 
                    && Integer.parseInt(ono.substring(0, 7)) > tiago.getId()) {
                    nuInherit.remove(nuOld.get(ono.substring(7)) + ono.substring(7));
                    nuInherit.add(ono);
                }
            }
            nu.beInherited(nuInherit);
            place = nu;
            pointers.put(currBranch, nu);
        }

        // helper method that updates the current directory
        reUpdate();
    }

    /* Handles case of interactive rebase. This is the same as rebase,
     * except before every replayed Commit, the user is prompted as to
     * whether the Commit should be skipped, if its message should be
     * changed, or if everything should proceed as normal.
     */
    private static void caseRebaseI(String[] tokens) {
        if (!caseRebaseCheck(tokens)) {
            return;
        }
        String otherBi = tokens[0];
        Commit currenti = pointers.get(currBranch);
        Commit theOtheri = pointers.get(otherBi);
        Commit tiagoi = findSplit(currenti, theOtheri);
        Commit poii = currenti;
        Stack<Commit> replayi = new Stack<Commit>();
        while (poii != tiagoi) {
            replayi.push(poii);
            poii = poii.getParent();
        }
        Commit placei = theOtheri;
        Set<String> otherAllxi = theOtheri.getAllFiles();
        Set<String> tiagoAllxi = tiagoi.getAllFiles();
        Set<String> otherDiffi = new HashSet<String>();
        Set<String> tiagoAlli = new HashSet<String>();
        for (String uno : tiagoAllxi) {
            tiagoAlli.add(uno.substring(7));
        }
        for (String dos : otherAllxi) {
            if (!tiagoAlli.contains(dos.substring(7))) {
                otherDiffi.add(dos);
            }
        }
        Commit main, nui;
        System.out.println("Currently replaying:");
        int i = 0;
        while (i < replayi.size()) {

            // helper method that gauges user input
            String[] result = replayHelper(replayi, i);
            if (result[0].equals("s")) {
                continue;
            }
            String newMess = result[1];
            i = Integer.parseInt(result[2]);
            main = replayi.pop();
            i += 1;
            if (newMess.equals("")) {
                newMess = main.getMessage();
            }
            Date dateRi = new Date();
            int codeRi = getCode(byMessage);
            nui = new Commit(main, codeRi, FORMAT.format(dateRi), newMess, placei);
            placei.addChild(nui);
            all.put(codeRi, nui);
            byMessageAdd(byMessage, nui.getMessage(), codeRi);
            Set<String> nuAllxi = nui.getAllFiles();
            Set<String> nuAlli = new HashSet<String>();
            HashMap<String, String> nuOldi = new HashMap<String, String>();
            Set<String> nuInheriti = nui.getInherited();
            for (String xyz : nuAllxi) {
                nuAlli.add(xyz.substring(7));
                if (Integer.parseInt(xyz.substring(0, 7)) <= tiagoi.getId()) {
                    nuOldi.put(xyz.substring(7), xyz.substring(0, 7));
                }
            }
            for (String plz : otherDiffi) {
                if (!nuAlli.contains(plz.substring(7)) 
                    && !nui.getExiled().contains(plz.substring(7))) {
                    nuInheriti.add(plz);
                }
            }
            for (String ono : otherAllxi) {
                if (nuOldi.keySet().contains(ono.substring(7)) 
                    && Integer.parseInt(ono.substring(0, 7)) > tiagoi.getId()) {
                    nuInheriti.remove(nuOldi.get(ono.substring(7)) + ono.substring(7));
                    nuInheriti.add(ono);
                }
            }
            nui.beInherited(nuInheriti);
            placei = nui;
            pointers.put(currBranch, nui);
        }
        reUpdate();
    }

    /* Helper method for interactive rebase that prompts user
     * to choose an action for every replayed Commit.
     * Returns an array containing the user's response, a possible
     * new message for the Commit (if newMess is not ""), as well
     * as the updated counter for the replayed Commit stack, 
     * in that order.
     */
    private static String[] replayHelper(Stack<Commit> replayi, int i) {
        int ii = i;
        String answer = "";
        Scanner rebase = new Scanner(System.in);
        while (!answer.equals("c") && !answer.equals("s") && !answer.equals("m")) {
            System.out.println("Would you like to (c)ontinue, " 
                + "(s)kip this commit, or change this commit's (m)essage?");
            answer = rebase.next();
        }

        // skip this Commit and increment the counter
        if (answer.equals("s")) {
            if (ii != 0 && ii != replayi.size() - 1) {
                replayi.pop();
                ii += 1;
            }
        }
        String newMess = "";
        if (answer.equals("m")) {
            while (newMess.equals("")) {
                System.out.println("Please enter a new message for this commit.");
                newMess = rebase.next();
            }
        }

        return new String[]{answer, newMess, Integer.toString(ii)};
    }

    /* Helper method for both rebase's which updates the working directory
     * in accordance with the files in the current branch's head Commit.
     */
    private static void reUpdate() {
        Set<String> reUpdatei = pointers.get(currBranch).getAllFiles();
        for (String rei : reUpdatei) {
            Path meii = Paths.get(".gitlet/files/" + rei);
            Path youi = Paths.get("./" + rei.substring(7));
            try {
                Files.copy(meii, youi, REPLACE);
            } catch (IOException io) {
                //io.printStackTrace();
                return;
            }
        }
    }

    /* Serializes the data structures at the end of the current program run.
     * This saves the state of the Commit tree, as well as the data in each
     * Commit, so that the next run of gitlet can begin where this one leaves
     * off.
     */
    private static void cerealize() {
        try {
            FileOutputStream treeOutF = new FileOutputStream(".gitlet/tree.ser");
            ObjectOutputStream treeOutO = new ObjectOutputStream(treeOutF);
            treeOutO.writeObject(all);
            treeOutO.close();
            treeOutF.close();

            FileOutputStream pointOutF = new FileOutputStream(".gitlet/point.ser");
            ObjectOutputStream pointOutO = new ObjectOutputStream(pointOutF);
            pointOutO.writeObject(pointers);
            pointOutO.close();
            pointOutF.close();

            FileOutputStream branchOutF = new FileOutputStream(".gitlet/branch.ser");
            ObjectOutputStream branchOutO = new ObjectOutputStream(branchOutF);
            branchOutO.writeObject(currBranch);
            branchOutO.close();
            branchOutF.close();

            FileOutputStream messOutF = new FileOutputStream(".gitlet/message.ser");
            ObjectOutputStream messOutO = new ObjectOutputStream(messOutF);
            messOutO.writeObject(byMessage);
            messOutO.close();
            messOutF.close();
        } catch (IOException io) {
            //io.printStackTrace();
        }
    }



    /* THE FOLLOWING METHODS
     *
     * Misc. helper methods used by the helper methods called in the main.
     * These usually access and organize certain information about the
     * current filesystem.
     */


    /* Checks for content equality between two files using a buffer
     * that reads in every element from both documents.
     *
     * adapted from http://www.java2s.com/Tutorial/Java/0170__File/
     * ComparethecontentsoftwoStreamstodetermineiftheyareequalornot.htm
     */
    private static boolean equals(File file1, File file2) {
        try {
            FileInputStream input1 = new FileInputStream(file1);
            FileInputStream input2 = new FileInputStream(file2);
            BufferedInputStream buffer1 = new BufferedInputStream(input1);
            BufferedInputStream buffer2 = new BufferedInputStream(input2);

            int curr1, curr2;
            curr1 = buffer1.read();
            while (curr1 != -1) {
                curr2 = buffer2.read();
                if (curr1 != curr2) {
                    return false;
                }
                curr1 = buffer1.read();
            }
            curr2 = buffer2.read();
            buffer1.close();
            buffer2.close();
            input1.close();
            input2.close();
            return curr2 == -1;
        } catch (FileNotFoundException fnf) {
            //fnf.printStackTrace();
        } catch (IOException io) {
            //io.printStackTrace();
        }

        return false;
    }

    /* Adds a message / Commit ID pair into the byMessage map structure.
     * If the message already exists, the ID is added to the existing set.
     * Else a new set with the ID is instantiated.
     */
    private static void byMessageAdd(Map<String, TreeSet<Integer>> map, String message, int code) {
        TreeSet<Integer> temp = new TreeSet<Integer>();
        if (map.containsKey(message)) {
            temp = map.get(message);
        }
        temp.add(code);
        map.put(message, temp);
    }

    /* Special method which gets the next unique ID for a new Commit.
     * The method checks for my secret word in the byMessage structure, which
     * contains the newest unique ID, and returns this ID, while updating
     * the number again by incrementing by one.
     */
    private static int getCode(Map<String, TreeSet<Integer>> map) {
        TreeSet<Integer> secret = map.get(SECRETKEY);
        int result = secret.pollFirst();
        secret.add(result + 1);
        return result;
    }

    /* Takes in a set of filenames and an other file name (that doesn't
     * include the 7 digit Commit ID at the beginning), and removes all
     * instances of this file (regardless of Commit ID) in the set.
     * The set is then returned.
     * *Nondestructive
     */
    private static Set<String> findAndDestroy(Set<String> set, String target) {
        Set<String> res = new HashSet<String>(set);
        for (String curr : set) {
            String fileName = curr.substring(7);
            if (fileName.equals(target)) {
                res.remove(curr);
            }
        }
        return res;
    }

    /* Checks if a set of filenames that start with Commit ID's contains
     * a specified file name without an ID.
     */
    private static boolean setContains(Set<String> set, String target) {
        for (String curr : set) {
            String fileName = curr.substring(7);
            if (fileName.equals(target)) {
                return true;
            }
        }
        return false;
    }

    /* Used for dangerous commands which can potentially
     * change files in the working directory. Prompts user with
     * a warning and returns if the user wishes to proceed.
     */
    private static boolean dangerous() {
        Scanner ui = new Scanner(System.in);
        System.out.println("Warning: The command you entered may "
            + "alter the files in your working directory. Uncommitted "
            + "changes may be lost. Are you sure you want to continue? (yes/no)");
        String response = ui.next();
        if (response.equals("yes")) {
            return true;
        }
        return false;
    }

    /* Iteratively finds the split point between two Commits
     * by tracing their respective history's to the initial Commit
     * and finding the most recent Commit they share in common.
     * A pointer to this Commit is then returned.
     */
    private static Commit findSplit(Commit a, Commit b) {
        if (a == null || b == null) {
            return null;
        }
        Commit ap = a;
        Commit bp = b;
        LinkedList<Commit> hista = new LinkedList<Commit>();
        LinkedList<Commit> histb = new LinkedList<Commit>();
        while (ap != null) {
            hista.addLast(ap);
            ap = ap.getParent();
        }
        while (bp != null) {
            histb.addLast(bp);
            bp = bp.getParent();
        }

        while (hista.size() > 0) {
            Commit curr = hista.pollFirst();
            if (histb.contains(curr)) {
                return curr;
            }
        }
        return null;
    }

    /* Constructs the necessary subdirectories so that
     * the specified file ADDRESS can be added into FOLDER.
     * Only applicable when the address of the file contains
     * subfolders that don't yet exist in the gitlet system's
     * folders.
     */
    private static void makeThatDir(String address, String folder) {
        int last = address.lastIndexOf("/");
        if (last == -1) {
            return;
        }
        String needMake = address.substring(0, last + 1);
        File baby = new File(".gitlet/" + folder + "/" + needMake);
        baby.mkdirs();
    }

    /* Recursively finds the names of all files in a given directory.
     * Every file's name includes subfolders between it and the current
     * directory.
     */
    private static void getAllFiles(String directoryName, ArrayList<String> files, String parent) {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        if (fList == null) {
            return;
        }
        for (File file : fList) {
            if (file.isFile()) {
                files.add(parent + file.getName());
            } else if (file.isDirectory()) {
                getAllFiles(file.getAbsolutePath(), files, file.getName() + "/");
            }
        }
    }

    /* Deletes any empty subfolder chains that may have been constructed
     * in the system's folders (i.e. when adding a file to staged), but
     * have been emptied and rendered obselete.
     * Without cleaning up, these empty subfolders would show up when
     * searching for all files in a directory, even if there are no
     * concrete files within them.
     */
    private static void cleanUp(String file, String directory) {
        int last = file.lastIndexOf("/");
        if (last == -1) {
            return;
        }
        String needClean = file.substring(0, last + 1);
        File curr = new File(".gitlet/" + directory + "/" + needClean);
        while (curr.isDirectory() && !curr.getName().equals(directory)) {
            if (curr.list().length > 0) {
                return;
            }
            File temp = curr;
            curr = new File(curr.getParent());
            temp.deleteOnExit();
        }
    }

    /* The center of everything that happens in gitlet. The main method
     * is both a GUI that provides a system for user interaction, while
     * also calling the appropriate methods depending on the command.
     * Also serializes and deserializes the neccessary gitlet data.
     */
    public static void main(String[] args) {
        String command = args[0];
        String[] tokens = new String[args.length - 1];
        System.arraycopy(args, 1, tokens, 0, args.length - 1);
        init = new File(".gitlet").exists();
        if (init) {
            decerealize();
        }
        switch (command) {
            case "init":
                caseInit();
                break;
            case "add":
                Commit don = pointers.get(currBranch);
                HashSet<String> files = (HashSet<String>) don.getAllFiles();
                for (String curr : tokens) {
                    boolean destroy = caseAdd(curr, don, files);
                    if (!destroy) {
                        break;
                    }
                }
                break;
            case "commit":
                String message = tokens[0];
                if (message == null) {
                    System.out.println("Please enter a commit message."); 
                    break;
                }
                caseCommit(message);
                break;
            case "rm":
                String dontWant = tokens[0];
                caseRm(dontWant);
                break;
            case "log":
                caseLog();
                break;
            case "global-log":
                caseGlobalLog();
                break;
            case "find":
                caseFind(tokens[0]);
                break;
            case "status":
                caseStatus();
                break;
            case "branch":
                caseBranch(tokens[0]);
                break;
            case "rm-branch":
                caseRmBranch(tokens[0]);
                break;
            case "checkout":
                if (tokens.length == 1) {
                    caseCheckout1(tokens);
                } else {
                    caseCheckout2(tokens);
                }
                break;
            case "reset":
                caseReset(tokens);
                break;
            case "merge":
                caseMerge(tokens);
                break;
            case "rebase":
                caseRebase(tokens);
                break;
            case "i-rebase":
                caseRebaseI(tokens);
                break;
            default:
                break;
        }
        init = new File(".gitlet").exists();
        if (init) {
            cerealize();
        }
    }
}
