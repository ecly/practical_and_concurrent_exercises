import java.util.Arrays;
//import java.util.concurrent.atomic.AtomicInteger;

class Exercise32 {
    public static void main(String[] args) {
        int N = 10_000_001;
        int[] a = new int[N];
        Arrays.parallelSetAll(a, i -> isPrime(i) ? 1 : 0);

        Arrays.parallelPrefix(a, (x, y) -> x + y);
        System.out.printf("Prime count: %d \n", a[N-1]);

        int n = N;
        for(int i = N/10; i < N; i += N/10){
            System.out.printf("i/ln(i) ratio for i of %d = %f\n", i, i/Math.log(i));
        }
    }

    private static boolean isPrime(int n) {
        int k = 2;
        while (k * k <= n && n % k != 0)
            k++;
        return n >= 2 && k * k > n;
    }
}
