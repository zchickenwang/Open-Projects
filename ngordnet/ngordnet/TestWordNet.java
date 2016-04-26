package ngordnet;
import java.io.FileNotFoundException;

public class TestWordNet {
	
	public static void main(String[] args) {
		WordNet wn = new WordNet("./WordNet/synsets.txt", "./WordNet/hyponyms.txt");

        System.out.println(wn.isNoun("jump"));
        System.out.println(wn.isNoun("leap"));
        System.out.println(wn.isNoun("nasal_decongestant"));
/*
        System.out.println("All nouns:");
        for (String noun : wn.nouns()) {
            System.out.println(noun);
        }

        System.out.println("Hypnoyms of increase:");
        for (String noun : wn.hyponyms("increase")) {
            System.out.println(noun);
        }
*/
        System.out.println("Hypnoyms of change:");

        WordNet wn2 = new WordNet("./WordNet/synsets14.txt", "./WordNet/hyponyms14.txt");
        for (String noun : wn2.hyponyms("change")) {
            System.out.println(noun);
        }

        System.out.println("Hypnoyms of jump:");
        for (String noun : wn2.hyponyms("jump")) {
            System.out.println(noun);
        }  

	}
}