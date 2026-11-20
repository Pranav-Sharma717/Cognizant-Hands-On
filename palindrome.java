import java.util.*;

public class palindrome {
    static boolean check(String s) {
        int i = 0, j = s.length() - 1;
        while (i <= j) {
            if (Character.toLowerCase(s.charAt(i)) != Character.toLowerCase(s.charAt(j)))
                return false;
            i++;
            j--;
        }
        return true;
    }

    static boolean longcheck(String s) {
        int i = 0, j = s.length() - 1;
        while (i < j) {
            while (i < j && !Character.isLetterOrDigit(s.charAt(i)))
                i++;
            while (i < j && !Character.isLetterOrDigit(s.charAt(j)))
                j--;
            if (Character.toLowerCase(s.charAt(i)) != Character.toLowerCase(s.charAt(j)))
                return false;
            i++;
            j--;
        }
        return true;
    }

    public static void main(String[] args) {
        String s = "Mom";
        System.out.println(check(s));
        System.out.println(longcheck("A man, a plan, a canal: Panama"));
    }
}

    
               
    

    
        
    