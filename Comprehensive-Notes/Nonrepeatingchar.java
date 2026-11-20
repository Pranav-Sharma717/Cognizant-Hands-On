import java.util.*;

public class Nonrepeatingchar {
    static Character firstUnique(String s) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : s.toCharArray())
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        for (char c : s.toCharArray())
            if (freq.get(c) == 1)
                return c;
        return null;
    }

    public static void main(String[] args) {
        System.out.println(firstUnique("Swiss")); // w
    }
}