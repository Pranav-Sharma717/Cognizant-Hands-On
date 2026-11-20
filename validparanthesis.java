import java.util.*;

public class validparanthesis {
    static boolean valid(String s) {
        Deque<Character> st = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '{' || c == '[')
                st.push(c);
            else if (c == ')' || c == ']' || c == '}') {
                if (st.isEmpty())
                    return false;
                char x = st.pop();
                if ((c == ')' && x != '(') || (c == '}' && x != '{') || (c == ']' && x != '['))
                    return false;
            }
        }
        return st.isEmpty();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.nextLine();
        System.out.println(valid(s));
    }
}