import java.util.*;

public class SecondLargestDistinctElement {
    static int SecondLargest(int[] a) {
        Integer first = null, second = null;
        for (int i : a) {
            if (first == null || i > first) {
                second = first;
                first = i;
            } else if (i != first && (second == null || i > second))
                second = i;
        }
        return second;
    }

    static int thirdLargest(int[] a) {
        Integer first = null, second = null, third = null;
        for (int i : a) {
            if (first == null || i > first) {
                third = second;
                second = first;
                first = i;
            } else if (i != first && (second == null || i > second)) {
                third = second;
                second = i;
            } else if (i != first && i != second && (third == null || i > third)) {
                third = i;
            }
        }
        return third == null ? -1 : third;
    }

    public static void main(String[] args) {
        int[] a = { 1, 2, 3, 4 };
        System.out.println(SecondLargest(a));
        System.out.println(thirdLargest(a));
    }
}