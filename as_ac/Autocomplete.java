import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * Implements autocomplete on prefixes for a given dictionary of terms and weights.
 * @author Z Wang 
 *         cs61b-blr
 */
public class Autocomplete {

    private Map<String, Double> master;
    private static final double MIN_VAL = -1.1;
    ATrie trie;

    /**
     * Initializes required data structures from parallel arrays.
     * @param terms Array of terms.
     * @param weights Array of weights.
     */
    public Autocomplete(String[] terms, double[] weights) {
        master = new TreeMap<String, Double>();
        trie = new ATrie();
        if (terms.length != weights.length) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < terms.length; i++) {
            if (weights[i] < 0) {
                throw new IllegalArgumentException();
            }
            master.put(terms[i], weights[i]);
            trie.insert(terms[i], weights[i]);
        }
        if (master.size() != terms.length) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Find the weight of a given term. If it is not in the dictionary, return 0.0
     * @param term Word
     * @return weight
     */
    public double weightOf(String term) {
        Double res = master.get(term);
        if (res == null) {
            return 0.0;
        } else {
            return res;
        }
    }

    /**
     * Return the top match for given prefix, or null if there is no matching term.
     * @param prefix Input prefix to match against.
     * @return Best (highest weight) matching string in the dictionary.
     */
    public String topMatch(String prefix) {
        for (String onlyOne: topMatches(prefix, 1)) {
            return onlyOne;
        }
        return null;
    }

    /**
     * Custom comparator that orders Strings from high to
     * low weight.
     */
    public class WSComparator implements Comparator<String> {
        @Override
        public int compare(String a, String b) {
            if (master.get(b) - master.get(a) > 0.0) {
                return 1;
            }
            return -1;
        }
        /*@Override
        public boolean equals(String a) {
            return master.get(this) == master.get(a);
        }*/
    }

    /**
     * Another custom comparator that sorts ANodes from
     * high to low maxWeight values.
     */
    public class WNComparator implements Comparator<ANode> {
        @Override
        public int compare(ANode a, ANode b) {
            if (b.maxWeight - a.maxWeight > 0.0) {
                return 1;
            }
            return -1;
        }
        /*@Override
        public boolean equals(ANode a) {
            return this.maxWeight == a.maxWeight;
        }*/
    }

    /**
     * Returns the top k matching terms (in descending order of weight) as an iterable.
     * If there are less than k matches, return all the matching terms.
     * @param prefix To look for
     * @param k Number of options wanted
     * @return Iterable over the k top matches
     */
    public Iterable<String> topMatches(String prefix, int k) {
        if (k < 0) {
            throw new IllegalArgumentException();
        }
        if (k == 0) {
            return new TreeSet<String>();
        }

        // Ordered set of words based on weight
        TreeSet<String> matches = new TreeSet<String>(new WSComparator());
        // Ordered MaxPQ of nodes based on maxWeight
        PriorityQueue<ANode> nodes = new PriorityQueue<ANode>(k, new WNComparator());
        // Finds the last ANode that completes the prefix in the ATrie
        ANode start = trace(trie.root, prefix.toCharArray(), 0);

        if (start == null) {
            return matches;
        }
        if (start.weight != null) {
            matches.add(start.word);
        }
        if (start != trie.root) {
            start = start.down;
        } else if (prefix != null && prefix.length() > 0 
            && start.car.charAt(0) == prefix.charAt(0)) {
            start = start.down;
        }
        if (start == null) {
            return matches;
        }

        nodes.add(start);
        boolean done = false;
        while (!done) {
            ANode dissect = nodes.poll();
            if (dissect.weight != null) {
                matches.add(dissect.word);
            }
            if (dissect.left != null) {
                nodes.add(dissect.left);
            }
            if (dissect.down != null) {
                nodes.add(dissect.down);
            }
            if (dissect.right != null) {
                nodes.add(dissect.right);
            }
            Double smallest = MIN_VAL;
            if (matches.size() > 0) {
                smallest = master.get(matches.last());
            }
            if (matches.size() == k) {
                if (!nodes.isEmpty() && nodes.peek().maxWeight <= smallest) {
                    done = true;
                }
            }
            if (matches.size() > k) {
                matches.pollLast();
            }
            if (nodes.isEmpty()) {
                done = true;
            }
        }

        return matches;
    }

    /**
     * Finds the end of a prefix in the main trie structure.
     * If the prefix is valid in the trie, this returns
     * the ANode of the last letter in the prefix. If the
     * trie does not contain the prefix, then return null.
     * @param start Current base node
     * @param want Word as character array
     * @param ptr Current index of traversal in the word
     * @return last ANode in the word
     */
    private static ANode trace(ANode start, char[] want, int ptr) {
        if (want.length == 0) {
            return start;
        }
        if (start == null) {
            return null;
        }
        if (want[ptr] < start.car.charAt(0)) {
            return trace(start.left, want, ptr);
        }
        if (want[ptr] > start.car.charAt(0)) {
            return trace(start.right, want, ptr);
        } else {
            if (ptr == want.length - 1) {
                return start;
            }
            return trace(start.down, want, ptr + 1);
        }
    }

    /**
     * Returns the highest weighted matches within k edit distance of the word.
     * If the word is in the dictionary, then return an empty list.
     * @param word The word to spell-check
     * @param dist Maximum edit distance to search
     * @param k    Number of results to return 
     * @return Iterable in descending weight order of the matches
     */
    public Iterable<String> spellCheck(String word, int dist, int k) {
        LinkedList<String> results = new LinkedList<String>();
        /* YOUR CODE HERE; LEAVE BLANK IF NOT PURSUING BONUS */
        return results;
    }

    /**
     * Test client. Reads the data from the file, 
     * then repeatedly reads autocomplete queries from standard input and 
     * prints out the top k matching terms.
     * @param args takes the name of an input file and an integer k 
     * as command-line arguments
     */
    public static void main(String[] args) {
        // initialize autocomplete data structure
        In in = new In(args[0]);
        int N = in.readInt();
        String[] terms = new String[N];
        double[] weights = new double[N];
        for (int i = 0; i < N; i++) {
            weights[i] = in .readDouble(); // read the next weight
            in.readChar(); // scan past the tab
            terms[i] = in.readLine(); // read the next term
        }

        Autocomplete autocomplete = new Autocomplete(terms, weights);

        // process queries from standard input
        int k = Integer.parseInt(args[1]);
        while (StdIn.hasNextLine()) {
            String prefix = StdIn.readLine();
            for (String term: autocomplete.topMatches(prefix, k)) {
                StdOut.printf("%14.1f  %s\n", autocomplete.weightOf(term), term);
            }
        }
    }

    /**
     * Class that defins the ANode, which composes
     * the nodes in the ATrie. This node contains
     * pointers to its children, as well as its values
     * and the maximum weight in its subtrie.
     */
    public static class ANode {
        ANode left;
        ANode right;
        ANode down;
        String car;
        String word;
        Double weight;
        double maxWeight;

        /**
         * Initializes new ANode with a character
         * value.
         * @param character value
         */
        public ANode(String character) {
            this.car = character;
            this.left = null;
            this.right = null;
            this.down = null;
            this.weight = null;
            this.word = null;
            this.maxWeight = 0.0;
        }

        /**
         * Inserts a word and weight, meaning this ANode
         * completes that word in the ATrie.
         * @param s Word
         * @param w Corresponding weight
         */
        public void putWord(String s, double w) {
            this.weight = w;
            this.word = s;
        }
    }

    /**
     * Class that defines the ATrie structure. It is a
     * TST that is comprised of ANodes, and maintains a
     * pointer to its root node.
     */
    public static class ATrie {
        ANode root;

        /**
         * Initializes a new ATrie.
         */
        public ATrie() {
            root = null;
        }

        /**
         * Inserts a String into the ATrie structure.
         * @param s String to be added
         * @param w Weight of the string
         */
        public void insert(String s, double w) {
            check(s);
            root = insertHelper(root, s.toCharArray(), 0, w);
        }

        /**
         * Recursive helper method that builds a String
         * into the ATrie, as needed, with new ANodes.
         * @param r Current root node
         * @param word Character array of the String to be added
         * @param ptr Current index traversed in the String
         * @param weight Weight of the String
         * @return updated root
         */
        private ANode insertHelper(ANode r, char[] word, int ptr, double weight) {
            if (r == null) {
                r = new ANode(Character.toString(word[ptr]));
                r.maxWeight = weight;
            }
            if (word[ptr] < r.car.charAt(0)) {
                r.left = insertHelper(r.left, word, ptr, weight);
            } else if (word[ptr] > r.car.charAt(0)) {
                r.right = insertHelper(r.right, word, ptr, weight);
            } else {
                if (ptr + 1 < word.length) {
                    r.down = insertHelper(r.down, word, ptr + 1, weight);
                } else {
                    r.putWord(new String(word), weight);
                }
            }
            if (weight > r.maxWeight) {
                r.maxWeight = weight;
            }
            return r;
        }

        /**
         * Checks if a String is valid for use
         * as an argument to the other methods (if
         * null or empty), and throws an
         * IllegalArgumentException if not.
         * @param s Word
         */
        private void check(String s) {
            if (s == null || s.equals("")) {
                throw new IllegalArgumentException();
            }
        }
    }
}
