import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Sorts words from input according
 * to a custom alphabet. Does this in Theta(M*N)
 * time, where M = max length of word and
 * N = total number of words, due to the linear
 * time of using a custom Trie structure.
 * @author Z Wang 
 *         cs61b-blr
 */
public class AlphabetSort {

    /**
     * Recursive method that performs a search in a CTrie
     * based on the ordering of each nodes children.
     * Prints out all words in the subtrie of the given
     * CNode in alphabetic order (according to ASCII codes).
     * @param root Base node
     */
    private static void traverse(CNode root) {
        for (CNode curr: root.children) {
            if (curr != null) {
                if (curr.fullWord) {
                    System.out.println(curr.word);
                }
                traverse(curr);
            }
        }
    }

    /**
     * Main method that takes in a sequence of Strings.
     * Using the first as the custom ordering of letters, this
     * then sorts the other Strings based on this ordering,
     * printing them to output.
     * @param args Takes in custom alphabet and words from input
     */
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        ArrayList<String> input = new ArrayList<String>();
        while (in.hasNextLine()) {
            String newb = in.nextLine();
            input.add(newb);
        }
        if (input.size() < 3) {
            throw new IllegalArgumentException();
        }

        String s = input.get(0);
        Map<String, Integer> bet = new HashMap<String, Integer>();
        for (int i = 0; i < s.length(); i++) {
            bet.put(s.substring(i, i + 1), i);
        }
        if (bet.size() != s.length()) {
            throw new IllegalArgumentException();
        }

        CTrie trie = new CTrie(bet);
        input.remove(0);
        for (String rest: input) {
            trie.insert(rest);
        }

        traverse(trie.root);
    }

    /**
     * Defines a CNode which makes up the CTrie.
     */
    public static class CNode {
        boolean fullWord;
        CNode[] children;
        String word;

        /**
         * Initializes a new CNode
         * @param size Number of children.
         */
        public CNode(int size) {
            fullWord = false;
            children = new CNode[size];
            word = null;
        }

        /**
         * Marks that this CNode is the
         * ending to a certain word.
         * @param w Word
         */
        public void putWord(String w) {
            word = w;
            fullWord = true;
        }
    }

    /**
     * Defines the CTrie structure used to
     * sort the words from the main method.
     * Should perform methods in linear time.
     */
    public static class CTrie {

        int size;
        CNode root;
        Map<String, Integer> alphabet;

        /**
         * Initializes a new CTrie based on a custom alphabet.
         * @param alphabetX custom alphabet
         */
        public CTrie(Map<String, Integer> alphabetX) {
            alphabet = alphabetX;
            size = alphabetX.size();
            root = new CNode(size);
        }

        /**
         * Checks if a word exists in the CTrie.
         * @param s Word
         * @param isFullWord boolean
         * @return boolean if found
         */
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

        /**
         * Inserts a word into this CTrie.
         * @param s Word
         */
        public void insert(String s) {
            check(s);
            CNode curr = root;
            for (int i = 0; i < s.length(); i++) {
                Integer code = alphabet.get(Character.toString(s.charAt(i)));
                if (code == null) {
                    return;
                }
                if (curr.children[code] == null) {
                    curr.children[code] = new CNode(size);
                }
                curr = curr.children[code];
                if (i == s.length() - 1) {
                    curr.putWord(s);
                }
            }
        }

        /**
         * Checks if a String is valid for use
         * by the other methods.
         * @param s Word
         */
        private void check(String s) {
            if (s == null || s.equals("")) {
                throw new IllegalArgumentException();
            }
        }
    }
}
