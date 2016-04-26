/**
 * Prefix-Trie. Supports linear time find() and insert(). 
 * Should support determining whether a word is a full word in the 
 * Trie or a prefix.
 * @author Z Wang 
 *         cs61b-blr
 */
public class Trie {

    private static final int AS = 256;
    Node root = new Node();

    /**
     * Defines the Node that makes up
     * the Trie structure.
     */
    public static class Node {
        boolean fullWord;
        Node[] children;

        /**
         * Initializes a new Node.
         */
        public Node() {
            fullWord = false;
            children = new Node[AS];
        }
    }

    /**
     * Check if a given word exists in the Trie.
     * Whether or not this word is a full entry
     * in the Trie is determined by isFullWord.
     * @param s Word to look for
     * @param isFullWord If the word needs to be full
     * @return exists or not as boolean
     */
    public boolean find(String s, boolean isFullWord) {
        check(s);
        Node curr = root;
        for (int i = 0; i < s.length(); i++) {
            int code = (int) s.charAt(i);
            if (curr.children[code] == null) {
                return false;
            }
            curr = curr.children[code];
        }
        if (isFullWord && !curr.fullWord) {
            return false;
        }
        return true;
    }

    /**
     * Inserts a given word into the Trie.
     * @param s Word
     */
    public void insert(String s) {
        check(s);
        Node curr = root;
        for (int i = 0; i < s.length(); i++) {
            int code = (int) s.charAt(i);
            if (curr.children[code] == null) {
                curr.children[code] = new Node();
            }
            curr = curr.children[code];
            if (i == s.length() - 1) {
                curr.fullWord = true;
            }
        }
    }

    /**
     * Checks if a String is valid for the
     * other methods to use.
     * @param s Word
     */
    private void check(String s) {
        if (s == null || s.equals("")) {
            throw new IllegalArgumentException();
        }
    }
}
