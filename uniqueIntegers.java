import java.util.*;

public class uniqueIntegers {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int size = scanner.nextInt();
        int[] values = new int[size];

        for (int i = 0; i < size; i++) {
            values[i] = scanner.nextInt();
        }

        Set<Integer> uniqueDescending = new TreeSet<>(Collections.reverseOrder());

        for (int value : values) {
            uniqueDescending.add(value);
        }

        System.out.println(uniqueDescending);
    }
}