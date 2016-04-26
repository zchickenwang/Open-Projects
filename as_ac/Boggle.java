/**
 * Filler
 * @author Z Wang
 */
public class Boggle {

    private static int[] mergeSort(int[] a, int[] b) {
        int aPointer = 0;
        int bPointer = 0;
        int pointer = 0;
        int[] result = new int[a.length + b.length];
        while (pointer < result.length) {
            if (a[aPointer] < b[bPointer]) {
                result[pointer] = a[aPointer];
                aPointer++;
            }
            else {
                result[pointer] = b[bPointer];
                bPointer++;
            }
            pointer++;
        }
        return result;
    }
    /**
     * Filler
     * @param args Filler
     */
    public static void main(String[] args) {
        return;
    }
}
