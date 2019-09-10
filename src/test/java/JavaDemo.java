/**
 * @Classname JavaDemo
 * @Description TODO
 * @Date 2019/9/7 23:24
 * @Created by yuhousheng
 */
public class JavaDemo {

    public static void main(String[] args) {

        recur(3);

    }

    public static int recur(int n) {
        int f;
        if (n==0) {
            f = 1;
        } else {
            f= n*recur(n-1);
        }
        System.out.println(f);
        return f;
    }
}
