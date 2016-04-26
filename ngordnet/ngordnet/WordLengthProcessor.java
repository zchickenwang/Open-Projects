package ngordnet;

public class WordLengthProcessor implements YearlyRecordProcessor {
    public double process(YearlyRecord yearlyRecord) {
        double sum = 0.0;
        double total = 0.0;
        for (String curr : yearlyRecord.words()) {
            sum += curr.length() * yearlyRecord.count(curr);
            total += yearlyRecord.count(curr);
        }
        return sum / total;
    }
}