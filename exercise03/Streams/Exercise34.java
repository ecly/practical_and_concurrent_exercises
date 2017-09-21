import java.util.stream.IntStream;
import java.util.stream.DoubleStream;
import java.util.function.DoubleSupplier;

class Exercise34 {
    public static void main(String[] args) {
        int N = 999_999_999;
        //IntStream is = IntStream.range(1, N);
        //DoubleStream ds = is.mapToDouble(n -> 1.0/n);
        // 1.
        //System.out.printf("Sum = %20.16f%n", ds.sum());
        // 2.
        //System.out.printf("Sum = %20.16f%n", ds.parallel().sum());
        //3
        /*
        Double res = 0.0;
        for (int i = 1; i <= N; i++){
            res += 1.0/i;
        }
        System.out.printf("Sum = %20.16f%n", res);
        */
        //4
        DoubleSupplier generator = new DoubleSupplier() {
            double current = 1.0;

            public double getAsDouble() {
                return 1.0 / current++;
            }
        };
        /*
        DoubleStream ds = DoubleStream.generate(generator).limit(N);
        System.out.printf("Sum = %20.16f%n", ds.sum());
        */
        //5
        DoubleStream ds = DoubleStream.generate(generator).limit(N);
        System.out.printf("Sum = %20.16f%n", ds.parallel().sum());
    }
}
