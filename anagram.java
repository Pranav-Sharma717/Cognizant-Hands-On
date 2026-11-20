import java.util.*;

public class anagram {
    static boolean check(String s, String r) {
        if (s.length() != r.length())
            return false;
        int[] count = new int[26];
        for (int i = 0; i < s.length(); i++) {
            count[s.charAt(i) - 'a']++;
            count[r.charAt(i) - 'a']--;
        }
        for (int i : count)
            if (i != 0)
                return false;
        return true;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.nextLine();
        String s2 = sc.nextLine();
        System.out.println(check(s, s2));
    }
}