import java.util.*;
public class removeduplicates{
    static int remove(int[]a){
        if(a.length==0)return 0;
        int w = 1;
        for(int i=1;i<a.length;i++){
            if(a[i]!=a[w-1])a[w++]=a[i];
        }
        return w;
    }
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int [] a = new int[n];
        for(int i =0;i<n;i++){
            a[i]=sc.nextInt();
        }
        int k = remove(a);
        System.out.println(Arrays.toString(Arrays.copyOf(a,k)));
    }
}