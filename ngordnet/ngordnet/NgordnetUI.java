/* Starter code for NgordnetUI (part 7 of the project). Rename this file and 
   remove this comment. */

package ngordnet;

import edu.princeton.cs.introcs.StdIn;
import edu.princeton.cs.introcs.In;
import java.util.Set;
import java.util.Arrays;

/**
 * Provides a simple user interface for exploring WordNet and NGram data.
 * 
 * @author [yournamehere mcjones]
 */
public class NgordnetUI {
    public static void main(String[] args) {
        In in = new In("./ngordnet/ngordnetui.config");
        System.out.println("Reading ngordnetui.config...");

        String wordFile = in.readString();
        String countFile = in.readString();
        String synsetFile = in.readString();
        String hyponymFile = in.readString();

        System.out
                .println("\nBased on ngordnetui.config, using the following: "
                        + wordFile + ", " + countFile + ", " + synsetFile
                        + ", and " + hyponymFile + ".");

        // System.out.println("\nFor tips on implementing NgordnetUI, see ExampleUI.java.");
        WordNet wNet = new WordNet(synsetFile, hyponymFile);
        NGramMap ngMap = new NGramMap(wordFile, countFile);
        int sYear = 0;
        int eYear = 3000;
        int count, year;
        String word;
        Set<String> hyponyms;

        while (true) {
            System.out.print("> ");
            String line = StdIn.readLine();
            String[] rawTokens = line.split(" ");
            String command = rawTokens[0];
            String[] tokens = new String[rawTokens.length - 1];
            System.arraycopy(rawTokens, 1, tokens, 0, rawTokens.length - 1);
            switch (command) {
            case "quit":
                return;
            case "help":
                break;
            case "range":
                sYear = Integer.parseInt(tokens[0]);
                eYear = Integer.parseInt(tokens[1]);
                System.out.println("Start year: " + sYear);
                System.out.println("End year: " + eYear);
                break;
            case "count":
                word = tokens[0];
                year = Integer.parseInt(tokens[1]);
                System.out.println(ngMap.countInYear(word, year));
                break;
            case "hyponyms":
                word = tokens[0];
                hyponyms = wNet.hyponyms(word);
                if (hyponyms == null) {
                    System.out.println("FMLFFL");
                    break;
                }
                System.out.println(Arrays.toString(hyponyms.toArray()));
                break;
            case "history":
                Plotter.plotAllWords(ngMap, tokens, sYear, eYear);
                break;
            case "hypohist":
                Plotter.plotCategoryWeights(ngMap, wNet, tokens, sYear, eYear);
                break;
            case "wordLength":
                Plotter.plotProcessedHistory(ngMap, sYear, eYear,
                        new WordLengthProcessor());
                break;
            case "zipf":
                year = Integer.parseInt(tokens[0]);
                Plotter.plotZipfsLaw(ngMap, year);
                break;
            default:
                System.out.println("Invalid command.");
                break;
            }
        }

    }
}
