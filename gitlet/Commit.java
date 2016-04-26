/* Commit.java
 * 
 * @author Z Wang
 * login: blr
 * CS61B Spring 2015
 * Project 2
 *
 *
 * The Commit object is a data type I created to contain all the
 * data that every Commit "snapshot" holds--i.e. its metadata, its
 * place in the Commit tree (parent & children), as well as the
 * files that each Commit contains in its snapshot. Note that these
 * files are simply Strings in the Commit object, as they refer to
 * a specified folder where all files are contained.
 *
 */

import java.util.Set;
import java.util.HashSet;

public class Commit implements java.io.Serializable {

    private int id; // unique ID number
    private String time; // time stamp of creation
    private String message; // message associated with this Commit
    private Set<String> files; // files newly added into this Commit
    private Set<String> inherited; // files inherited from a parent/copy Commit
    private Set<String> allFiles; // all files from both FILES and INHERITED
    private Set<String> exiled; // files removed from this Commit chain
    private Commit parent; // previous node in the Commit tree
    private Set<Commit> children; // all children of this Commit

    /* Instantiates a new Commit from the data in the arguments, this
     * gives the new Commit blank Sets for its files.
     */
    public Commit(int idX, String timeX, String messageX, Commit parentX) {
        this.id = idX;
        this.time = timeX;
        this.message = messageX;
        this.parent = parentX;
        this.files = new HashSet<String>();
        this.children = new HashSet<Commit>();
        this.inherited = new HashSet<String>();
        this.allFiles = new HashSet<String>();
        if (this.parent == null) {
            this.exiled = new HashSet<String>();
        } else {
            this.exiled = this.parent.getExiled();
        }
    }

    /* Instantiates a new Commit from another Commit, but giving it new
     * ID, time stamp, and message, as taken from the arguments. Moreover,
     * this new Commit inherits the files from the other.
     */
    public Commit(Commit copy, int idX, String timeX, String messageX, Commit parentX) {
        this.id = idX;
        this.time = timeX;
        this.message = messageX;
        this.parent = parentX;
        this.files = copy.getFiles();
        this.children = new HashSet<Commit>(); //dont want all children
        this.inherited = copy.getInherited();
        this.allFiles = copy.getAllFiles();
        this.exiled = copy.getExiled();
    }

    /* Public getter method for this Commit's parent. Should return null
     * if this Commit is the first one.
     */
    public Commit getParent() {
        return this.parent;
    }

    /* Public getter method for this Commit's unique ID.
     */
    public int getId() {
        return this.id;
    }

    /* Public getter method for this Commit's time stamp.
     */
    public String getTime() {
        return this.time;
    }

    /* Public getter method for this Commit's message.
     */
    public String getMessage() {
        return this.message;
    }

    /* Public getter method for this Commit's files. This set
     * refers to the files newly added in this Commit.
     */
    public Set<String> getFiles() {
        Set<String> res = new HashSet<String>(this.files);
        return res;
    }

    /* Public getter method for this Commit's inherited files.
     * This means files that the Commit inherited from its
     * parent, and not files that were newly added at this Commit's
     * creation.
     */
    public Set<String> getInherited() {
        Set<String> res = new HashSet<String>(this.inherited);
        return res;
    }

    /* Public getter method for this Commit's children nodes.
     */
    public Set<Commit> getChildren() {
        Set<Commit> res = new HashSet<Commit>(this.children);
        return res;
    }

    /* Method for outside classes (Gitlet.java) to attach other
     * Commits onto this Commit as children.
     */
    public void addChild(Commit c) {
        this.children.add(c);
    }

    /* Method for outside classes to add file names into the 
     * newly added fileset of this Commit.
     */
    public void addFile(String f) {
        this.files.add(f);
        this.allFiles.add(f);
    }

    /* Method for outside classes to add inherited file names
     * into this Commit's data.
     */
    public void addInherited(String i) {
        this.inherited.add(i);
        this.allFiles.add(i);
    }

    /* Method for setting a premade Set of filenames
     * as the inherited fileset for this Commit. Changes are made
     * accordingly to ALLFILES as well.
     */
    public void beInherited(Set<String> set) {
        for (String curr : this.inherited) {
            this.allFiles.remove(curr);
        }
        this.inherited = new HashSet<String>(set);
        this.allFiles.addAll(set);
    }

    /* Method for getting a set of all (all!) filenames
     * that this Commit holds.
     */
    public Set<String> getAllFiles() {
        Set<String> res = new HashSet<String>(this.allFiles);
        return res;
    }

    /* Method for outside classes to add a filename into the 
     * exiled, or previously-removed-and-not-added-since set of
     * this Commit.
     */
    public void addExiled(String e) {
        this.exiled.add(e);
    }

    /* Method to retrieve the set of exiled files in this Commit.
     */
    public Set<String> getExiled() {
        Set<String> res = new HashSet<String>(this.exiled);
        return res;
    }

    /* Method for outside classes to remove a file from exile in
     * this commit. Usually occurs when some version of an exiled
     * file is added back into the Commit chain.
     */
    public void remExiled(String e) {
        this.exiled.remove(e);
    }
}
