import java.util.*;

public class intersectionArray {
    static Set<Integer> intersect(int[] a, int[] b) {
        Set<Integer> seen = new HashSet<>();
        for (int i : a) {
            seen.add(i);
        }
        Set<Integer> result = new LinkedHashSet<>();
        for (int i : b)
            if (seen.contains(i))
                result.add(i);
        return result;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int sizea = sc.nextInt(), sizeb = sc.nextInt();
        int[] a = new int[sizea];
        int[] b = new int[sizeb];

        for (int i = 0; i < sizea; i++) {
            a[i] = sc.nextInt();
        }   

        for (int i = 0; i < sizeb; i++) {
            b[i] = sc.nextInt();
        }
        System.out.println(intersect(a, b));
    }
}