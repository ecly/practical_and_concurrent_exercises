import java.util.stream.IntStream;
import java.util.stream.DoubleStream;

class Exercise34 {
	public static void main(String[] args) {
		int N = 999_999_999;
		IntStream is = IntStream.range(1, N);
		DoubleStream ds = is.mapToDouble(n -> 1.0/n);
		// 1.
      	// System.out.printf("Sum = %20.16f%n", ds.sum());
		// 2.
		// System.out.printf("Sum = %20.16f%n", ds.parallel().sum());
	}
}             
