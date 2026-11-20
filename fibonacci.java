import java.util.*;

public class fibonacci {
    static int rfib(int n) {
        if (n <= 1)
            return n;
        return (rfib(n - 1) + rfib(n - 2));
    }

    static int ifib(int n) {
        int a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            int next = a + b;
            a = b;
            b = next;
        }
        return a;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        System.out.println(rfib(n));
        System.out.println(ifib(n));
    }
}
