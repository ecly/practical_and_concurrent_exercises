// For week 2
// sestoft@itu.dk * 2014-08-29
import java.util.concurrent.atomic.*;

class TestCountFactors {
    public static void main(String[] args) {
        final int range = 5_000_000;
        final AtomicInteger count = new AtomicInteger(0);
        
        Thread[] threads = new Thread[10];
        for(int i = 0; i < 10; i++){
            final int h = i;
            threads[i]=  new Thread(() -> { 
                for (int p=h*499_999+h; p<(h+1)*499_999+h+1; p++)
                    count.addAndGet(countFactors(p));
            });
        }

        for(Thread thread : threads){
            thread.start();
        }

        for(Thread thread : threads){
            try{ thread.join();}
            catch(Exception e){System.out.println(e);}
        }

        System.out.printf("Total number of factors is %9d%n", count.get());
    }

    public static int countFactors(int p) {
        if (p < 2)
            return 0;
        int factorCount = 1, k = 2;
        while (p >= k * k) {
            if (p % k == 0) {
                factorCount++;
                p /= k;
            } else 
                k++;
        }
        return factorCount;
    }

}
class MyAtomicInteger{
    private int value;
    
    MyAtomicInteger(int value){
        this.value = value;
    }
    
    int get(){
        return value;
    }

    synchronized int addAndGet(int amount){
        value += amount;
        return value;
    }
}

