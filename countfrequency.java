import java.util.*;

public class countfrequency {
    public static void main(String[] args) {
        String s = "Banana";
        Map<Character, Integer> freq = new LinkedHashMap<>();
        for (char c : s.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }
        for (var e : freq.entrySet()) {
            System.out.println(e.getKey() + "=" + e.getValue());
        }
    }
}