import java.util.*;

public class BinarySearch {
    static int BS(int[] a, int target) {
        int l = 0;
        int r = a.length - 1;
        while (l <= r) {
            int mid = l + (r - l) / 2;
            if (a[mid] == target)
                return mid;
            if (a[mid] < target)
                l = mid + 1;
            else
                r = mid - 1;
        }
        return -1;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int sz = sc.nextInt();
        int[] a = new int[sz];
        for (int i = 0; i < sz; i++) {
            a[i] = sc.nextInt();
        }
        int target = sc.nextInt();
        System.out.println(BS(a, target));
    }
}