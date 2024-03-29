// For week 1
// sestoft@itu.dk * 2015-10-29

public class TestLocking0 {
    public static void main(String[] args) {
        final int count = 1_000_000;
        Mystery m = new Mystery();
        Thread t1 = new Thread(() -> { 
            for (int i=0; i<count; i++)
                m.addInstance(1); 
        });
        Thread t2 = new Thread(() -> { 
            for (int i=0; i<count; i++)
                Mystery.addStatic(1); 
        });
        t1.start(); t2.start();
        try { t1.join(); t2.join(); } catch (InterruptedException exn) { }
        System.out.printf("Sum is %f and should be %f%n", Mystery.sum(), 2.0 * count);
    }
}

class Mystery {
    private static double sum = 0;
    private static Object lock = new Object();

    public static void addStatic(double x) {
        synchronized(lock){
            sum += x;
        }
    }

    public void addInstance(double x) {
        synchronized(lock){
            sum += x;
        }
    }

    public static synchronized double sum() {
        return sum;
    }
}
