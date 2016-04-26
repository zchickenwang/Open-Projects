import java.io.File;
import java.util.Scanner;
import java.util.HashSet;

public class OutputTester {
		public static void main(String[] args) {
			try {
				String inputFileName = args[0];
				String outputFileName = args[1];
				File inputFile = new File(inputFileName);
				File outputFile = new File(outputFileName);
				Scanner inputScanner = new Scanner(inputFile);
				Scanner outputScanner = new Scanner(outputFile);
				HashSet<String> inputWords = new HashSet<String>();
				HashSet<String> extraOutput = new HashSet<String>();
				while (inputScanner.hasNextLine()) {
					inputWords.add(inputScanner.nextLine());
				}
				while (outputScanner.hasNextLine()) {
					String next = outputScanner.nextLine();
					if (inputWords.contains(next)) {
						inputWords.remove(next);
					} else {
						extraOutput.add(next);
					}
				}
				System.out.println("All input that was not outputted: ");
				for (String s : inputWords) {
					System.out.println(s);
				}
				System.out.println("\nAll output that was not in the input: ");
				for (String s : extraOutput) {
					System.out.println(s);
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
}