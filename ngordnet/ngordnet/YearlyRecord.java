package ngordnet;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.LinkedHashSet;

public class YearlyRecord {
    private Map<String, Number> record;
    private TreeMap<Integer, HashSet<String>> flipped;
    private Map<String, Integer> rank;
    // private Map<Integer, String> rankFlip;
    private LinkedHashSet<String> words;
    private TreeSet<Integer> counts;
    private boolean ranked;

    // private TreeSet<Number> counts;

    /** Creates a new empty YearlyRecord. */
    public YearlyRecord() {
        record = new TreeMap<String, Number>();
        flipped = new TreeMap<Integer, HashSet<String>>();
        rank = new LinkedHashMap<String, Integer>();
        // rankFlip = new TreeMap<Integer, String>();
        words = new LinkedHashSet<String>();
        ranked = false;
        // counts = new TreeSet<Number>();
    }

    /** Creates a YearlyRecord using the given data. */
    public YearlyRecord(HashMap<String, Integer> otherCountMap) {
        this();
        for (String curr : otherCountMap.keySet()) {
            put(curr, otherCountMap.get(curr));
        }
    }

    /** Returns the number of times WORD appeared in this year. */
    public int count(String word) {
        if (record.containsKey(word)) {
            return record.get(word).intValue();
        }
        return 0;
    }

    /** Records that WORD occurred COUNT times in this year. */
    public void put(String word, int count) {
        if (record.containsKey(word)) {
            int c = record.get(word).intValue();
            flipped.get(c).remove(word);
        }

        record.put(word, count);
        // counts.add(count);
        /*
         * double mark = (new Integer(count)).doubleValue(); while
         * (flipped.containsKey(mark)) { mark += 0.01; } flipped.put(mark,
         * word);
         */
        if (flipped.containsKey(count)) {
            flipped.get(count).add(word);
        } else {
            HashSet<String> knu = new HashSet<String>();
            knu.add(word);
            flipped.put(count, knu);
        }
        ranked = false;
    }

    /** Returns the number of words recorded this year. */
    public int size() {
        return record.size();
    }

    private void findRank() {
        /*
         * int r = 1; for (Double curr : flipped.descendingKeySet()) {
         * rank.put(flipped.get(curr), r); rankFlip.put(-r, flipped.get(curr));
         * r += 1; }
         */
        int r = record.size();
        for (int curr : flipped.keySet()) {
            for (String zi : flipped.get(curr)) {
                rank.put(zi, r);
                // rankFlip.put(-r, zi);
                words.add(zi);
                r -= 1;
            }
        }
        ranked = true;
    }

    /** Returns all words in ascending order of count. */
    public Collection<String> words() {
        if (!ranked) {
            findRank();
        }
        return words;
    }

    /** Returns all counts in ascending order of count. */
    public Collection<Number> counts() {
        return new PriorityQueue<Number>(record.values());
        // return counts;
    }

    /**
     * Returns rank of WORD. Most common word is rank 1. If two words have the
     * same rank, break ties arbitrarily. No two words should have the same
     * rank.
     */
    public int rank(String word) {
        if (!ranked) {
            findRank();
        }
        return rank.get(word);
    }
}
