import java.util.*;

public class reverseString {
    static int cntvowels(String s) {
        if (s == null)
            return 0;
        int cnt = 0;
        for (char c : s.toLowerCase().toCharArray()) {
            if ("aeiou".indexOf(c) != -1)
                cnt++;
        }
        return cnt;
    }

    static String reverse(String s) {
        if (s == null)
            return "Not possible";
        StringBuilder out = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            out.append(s.charAt(i));
        }
        return out.toString();
    }

    public static void main(String[] args) {
        System.out.println(cntvowels("Pranav Sharma"));
        System.out.println(reverse("Pranav Sharma"));

    }
}