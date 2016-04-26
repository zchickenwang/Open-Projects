/**
 * Prefix-Trie. Supports linear time find() and insert(). 
 * Should support determining whether a word is a full word in the 
 * Trie or a prefix.
 * @author Z Wang 
 *         cs61b-blr
 */

import java.util.Map;

public class ChameleonTrie {

    private int size;
    private CNode root;
    private Map<String, Integer> alphabet;

    public ChameleonTrie(Map<String, Integer> alphabetX) {
        alphabet = alphabetX;
        size = alphabetX.size();
        root = new CNode();
    }

    public CNode getRoot() {
        return root;
    }

    public class CNode {

        boolean fullWord;
        CNode[] children;
        String word;

        public CNode() {
            fullWord = false;
            children = new CNode[size];
            word = null;
        }

        public void putWord(String w) {
            word = w;
            fullWord = true;
        }
    }

    public boolean find(String s, boolean isFullWord) {
        check(s);
        CNode curr = root;
        for (int i = 0; i < s.length(); i++) {
            int code = alphabet.get(s.charAt(i));
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

    public void insert(String s) {
        check(s);
        CNode curr = root;
        for (int i = 0; i < s.length(); i++) {
            int code = alphabet.get(s.charAt(i));
            if (curr.children[code] == null) {
                curr.children[code] = new CNode();
            }
            curr = curr.children[code];
            if (i == s.length() - 1) {
                curr.putWord(s);
            }
        }
    }

    private void check(String s) {
        if (s == null || s.equals("")) {
            throw new IllegalArgumentException();
        }
    }
}
