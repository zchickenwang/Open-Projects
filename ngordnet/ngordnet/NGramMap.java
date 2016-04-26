package ngordnet;

import java.util.Scanner;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.PriorityQueue;

public class NGramMap {

    private TimeSeries<Long> annCount;
    private Map<Integer, YearlyRecord> annRecord;
    private Map<String, TimeSeries<Integer>> wordRecord;

    /** Constructs an NGramMap from WORDSFILENAME and COUNTSFILENAME. */
    public NGramMap(String wordsFilename, String countsFilename) {
        annCount = new TimeSeries<Long>();
        annRecord = new TreeMap<Integer, YearlyRecord>();
        wordRecord = new TreeMap<String, TimeSeries<Integer>>();

        try {
            Scanner wordScan = new Scanner(new File(wordsFilename));
            wordScan.useDelimiter("\t");

            String word;
            int year;
            int count;
            YearlyRecord curr;
            TimeSeries<Integer> currCount;

            while (wordScan.hasNext()) {
                word = wordScan.next();
                year = wordScan.nextInt();
                count = wordScan.nextInt();

                if (annRecord.containsKey(year)) {
                    annRecord.get(year).put(word, count);
                } else {
                    curr = new YearlyRecord();
                    curr.put(word, count);
                    annRecord.put(year, curr);
                }

                if (wordRecord.containsKey(word)) {
                    wordRecord.get(word).put(year, count);
                } else {
                    currCount = new TimeSeries<Integer>();
                    currCount.put(year, count);
                    wordRecord.put(word, currCount);
                }

                wordScan.nextLine();
            }
        } catch (FileNotFoundException err) {
            System.out.println("Found FileNotFoundException");
            return;
        }

        try {
            Scanner countScan = new Scanner(new File(countsFilename));
            countScan.useDelimiter(",");

            int annee;
            Long taux;

            while (countScan.hasNext()) {
                annee = countScan.nextInt();
                taux = countScan.nextLong();

                annCount.put(annee, taux);

                countScan.nextLine();
            }
        } catch (FileNotFoundException err) {
            System.out.println("Found FileNotFoundException");
            return;
        }
    }

    /**
     * Returns the absolute count of WORD in the given YEAR. If the word did not
     * appear in the given year, return 0.
     */
    public int countInYear(String word, int year) {
        return annRecord.get(year).count(word);
    }

    /** Returns a defensive copy of the YearlyRecord of WORD. */
    public YearlyRecord getRecord(int year) {
        if (!annRecord.containsKey(year)) {
            System.out.println("Invalid year");
            return null;
        }
        YearlyRecord yR = annRecord.get(year);
        Collection<String> cWords = yR.words();
        // Iterator<String> cWIter = cWords.iterator();
        // System.out.println(Integer.toString(cWords.size()));
        // int size = cWords.size();
        // String[] words = cWords.toArray(new String[0]);
        // System.out.println(Integer.toString(words.length));

        Collection<Number> cCounts = yR.counts();
        PriorityQueue<Number> pqCounts = new PriorityQueue<Number>(cCounts);
        // Iterator<Number> cCIter = cCounts.iterator();
        // System.out.println(Integer.toString(cCounts.size()));

        // Number[] counts = cCounts.toArray(new Number[0]);
        // System.out.println(Integer.toString(counts.length));
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (String curr : cWords) {
            result.put(curr, pqCounts.poll().intValue());
        }
        /*
         * for (int i = 0; i < words.length; i += 1) { result.put(words[i],
         * counts[i].intValue()); } return new YearlyRecord(result); String
         * word; int count; while (cWIter.hasNext() && cCIter.hasNext()) { word
         * = cWIter.next(); count = cCIter.next().intValue(); result.put(word,
         * count); System.out.println(word + Integer.toString(count)); }
         */
        return new YearlyRecord(result);
    }

    /** Returns the total number of words recorded in all volumes. */
    public TimeSeries<Long> totalCountHistory() {
        return annCount;
    }

    /** Provides the history of WORD between STARTYEAR and ENDYEAR. */
    public TimeSeries<Integer> countHistory(String word, int startYear,
            int endYear) {
        return new TimeSeries<Integer>(wordRecord.get(word), startYear, endYear);
    }

    /** Provides a defensive copy of the history of WORD. */
    public TimeSeries<Integer> countHistory(String word) {
        return new TimeSeries<Integer>(wordRecord.get(word));
    }

    /** Provides the relative frequency of WORD between STARTYEAR and ENDYEAR. */
    public TimeSeries<Double> weightHistory(String word, int startYear,
            int endYear) {
        if (word == null || !wordRecord.containsKey(word)) {
            return null;
        }
        return countHistory(word, startYear, endYear).dividedBy(annCount);
    }

    /** Provides the relative frequency of WORD. */
    public TimeSeries<Double> weightHistory(String word) {
        if (word == null || !wordRecord.containsKey(word)) {
            return null;
        }
        return countHistory(word).dividedBy(annCount);
    }

    /**
     * Provides the summed relative frequency of all WORDS between STARTYEAR and
     * ENDYEAR. If a word does not exist, ignore it rather than throwing an
     * exception.
     */
    public TimeSeries<Double> summedWeightHistory(Collection<String> words,
            int startYear, int endYear) {
        TimeSeries<Double> result = new TimeSeries<Double>();
        for (String curr : words) {
            result = result.plus(weightHistory(curr, startYear, endYear));
        }
        return result;
    }

    /** Returns the summed relative frequency of all WORDS. */
    public TimeSeries<Double> summedWeightHistory(Collection<String> words) {
        TimeSeries<Double> result = new TimeSeries<Double>();
        for (String curr : words) {
            result = result.plus(weightHistory(curr));
        }
        return result;
    }

    /**
     * Provides processed history of all words between STARTYEAR and ENDYEAR as
     * processed by YRP.
     */
    public TimeSeries<Double> processedHistory(int startYear, int endYear,
            YearlyRecordProcessor yrp) {
        TimeSeries<Double> result = new TimeSeries<Double>();
        for (int curr : annRecord.keySet()) {
            if (curr >= startYear && curr <= endYear) {
                result.put(curr, yrp.process(annRecord.get(curr)));
            }
        }
        return result;
    }

    /** Provides processed history of all words ever as processed by YRP. */
    public TimeSeries<Double> processedHistory(YearlyRecordProcessor yrp) {
        TimeSeries<Double> result = new TimeSeries<Double>();
        for (int curr : annRecord.keySet()) {
            result.put(curr, yrp.process(annRecord.get(curr)));
        }
        return result;
    }
}
