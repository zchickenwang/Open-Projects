package wordnet;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;


public class WordNet {

	private Map<Integer, String[]> synsets;
	private Map<Integer, Set<Integer>> hyponyms;
	private Set<String> allNouns;

	/** Creates a WordNet using files form SYNSETFILENAME and HYPONYMFILENAME */
    public WordNet(String synsetFilename, String hyponymFilename) throws FileNotFoundException {
    	synsets = new HashMap<Integer, String[]>();
    	hyponyms = new HashMap<Integer, Set<Integer>>();
    	allNouns = new HashSet<String>();

    	Scanner sysScan = new Scanner(new File(synsetFilename));
    	sysScan.useDelimiter(",");
   		int code;
   		String nouns;
   		String[] nounList;
    		
    	while (sysScan.hasNext()) {
    		code = sysScan.nextInt();
   			nouns = sysScan.next();
   			nounList = nouns.split("\\s+");
   			synsets.put(code, nounList);
   			for (String curr : nounList) {
   				allNouns.add(curr);
    		}
    		sysScan.nextLine();
    	}
    	
    	Scanner hypScan = new Scanner(new File(hyponymFilename));
    	hypScan.useDelimiter(",");
    	int codex, curr;
    	String rest;
    	Set<Integer> ints;
    	Scanner helper;

    	while (hypScan.hasNext()) {
    		codex = hypScan.nextInt();
			rest = hypScan.nextLine();
    		helper = new Scanner(rest);
    		helper.useDelimiter(",");
    		ints = new HashSet<Integer>();
    		while (helper.hasNext()) {
    			curr = helper.nextInt();
    			ints.add(curr);
    		}

    		hyponyms.put(codex, ints);
    	}

    }

    /* Returns true if NOUN is a word in some synset. */
    public boolean isNoun(String noun) {
    	return allNouns.contains(noun);
    	/*for (String[] nouns : synsets) {
    		for (String each : nouns) {
    			if each.equals(noun) {
    				return true;
    			}
    		}
    	}
    	return false;*/
    }

    /* Returns the set of all nouns. */
    public Set<String> nouns() {
    	return allNouns;
    	/*Set<String> allNouns = new HashSet<String>();
    	for (String[] nouns : synsets) {
    		for (String each : nouns) {
    			allNouns.add(each);
    		}
    	}*/
    }

    /** Concatenates two arrays and resizes accordingly
      */
    private String[] conc(String[] a, String[] b) {
    	String[] res = new String[a.length + b.length];
    	System.arraycopy(a, 0, res, 0, a.length);
    	System.arraycopy(b, 0, res, a.length, b.length);
    	return res;
    }

    /** Returns the set of all hyponyms of WORD as well as all synonyms of
      * WORD. If WORD belongs to multiple synsets, return all hyponyms of
      * all of these synsets. See http://goo.gl/EGLoys for an example.
      * Do not include hyponyms of synonyms.
      */
    public Set<String> hyponyms(String word) {
    	if (!isNoun(word)) {
    		return null;
    	}

    	Set<String> hypes = new HashSet<String>();
    	String[] synonyms = new String[0];
    	Set<Integer> toFind = new HashSet<Integer>();

    	for (int code : synsets.keySet()) {
    		String[] nouns = synsets.get(code);
    		for (String each : nouns) {
    			if (each.equals(word)) {
    				synonyms = conc(synonyms, nouns);
    				toFind.add(code);
    			}
    		}
    	}

    	for (int codex : toFind) {
    		if (hyponyms.keySet().contains(codex)) {
    			Set<Integer> hypSet = hyponyms.get(codex);
    			for (int hao : hypSet) {
    				String[] mustAdd = synsets.get(hao);
    				for (String h : mustAdd) {
    					hypes.add(h);
    					if (hyponyms.keySet().contains(hao)) {
    						hypes.addAll(hyponyms(h)); //may be wrong
    					}
    				}
    			}
    		}
    		
    	}

    	for (String s : synonyms) {
    		if (!s.equals(word)) {
    			hypes.add(s);
    		}
    	}

    	hypes.add(word);
    	return hypes;
    }

}