public class ReverseWords {
  static String reverseWords(String s) {
    s=s.trim();
    if(s.isEmpty()) return "";
    String[] words=s.split("\\s+");
    StringBuilder ans=new StringBuilder();
    for(int i=words.length-1;i>=0;i--) {
      if(ans.length()>0) ans.append(' ');
      ans.append(words[i]);
    }
    return ans.toString();
  }
  public static void main(String[] args) {
    System.out.println(reverseWords("  Java   React  "));
  }
}