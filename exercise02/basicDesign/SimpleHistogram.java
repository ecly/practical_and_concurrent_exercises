// For week 2
// sestoft@itu.dk * 2014-09-04
import java.util.concurrent.atomic.*;

class SimpleHistogram {
    public static void main(String[] args) {
        final int range = 5_000_000;
        final Histogram histogram = new Histogram4(range);

        Thread[] threads = new Thread[10];
        for(int i = 0; i < 10; i++){
            final int h = i;
            threads[i]=  new Thread(() -> { 
                for (int p=h*499_999+h; p<(h+1)*499_999+h+1; p++)
                    histogram.increment(countFactors(p));
            });
        }

        for(Thread thread : threads){
            thread.start();
        }

        for(Thread thread : threads){
            try{ thread.join();}
            catch(Exception e){System.out.println(e);}
        }

        for(int i = 0; i < 10; i++){
            System.out.printf("%d: %d \n", i, histogram.getCount(i));
        }
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
    public static void dump(Histogram histogram) {
        int totalCount = 0;
        for (int bin=0; bin<histogram.getSpan(); bin++) {
            System.out.printf("%4d: %9d%n", bin, histogram.getCount(bin));
            totalCount += histogram.getCount(bin);
        }
        System.out.printf("      %9d%n", totalCount);
    }
}

interface Histogram {
    public void increment(int bin);
    public int getCount(int bin);
    public int getSpan();
    public int[] getBins();
}

class Histogram5 implements Histogram {
    private LongAdder[] counts;

    public Histogram5(int span) {
        counts = new LongAdder[span];
        for(int i = 0; i < span; i++)
            counts[i] = new LongAdder();
    }
    public void increment(int bin) {
        counts[bin].increment();
    }
    public int getCount(int bin) {
        return counts[bin].intValue();
    }
    public int getSpan() {
        return counts.length;
    }

    public int[] getBins(){
        int[] bins = new int[counts.length];
        for(int i = 0; i < counts.length; i++){
            bins[i] = counts[i].intValue();
        }
        return bins;
    }
}

class Histogram4 implements Histogram {
    private AtomicIntegerArray counts;
    public Histogram4(int span) {
        counts = new AtomicIntegerArray(span);
    }
    public void increment(int bin) {
        counts.getAndAdd(bin, 1); 
    }
    public int getCount(int bin) {
        return counts.get(bin);
    }
    public int getSpan() {
        return counts.length();
    }

    public int[] getBins(){
        int[] bins = new int[counts.length()];
        for(int i = 0; i < counts.length(); i++){
            bins[i] = counts.get(i);
        }
        return bins;

    }
}

class Histogram3 implements Histogram {
    private AtomicInteger[] counts;
    public Histogram3(int span) {
        counts = new AtomicInteger[span];
        for(int i = 0; i < span; i++)
            counts[i] = new AtomicInteger(0);
    }
    public void increment(int bin) {
        counts[bin].incrementAndGet();
    }
    public int getCount(int bin) {
        return counts[bin].get();
    }
    public int getSpan() {
        return counts.length;
    }

    public int[] getBins(){
        int[] bins = new int[counts.length];
        for(int i = 0; i < counts.length; i++){
            bins[i] = counts[i].get();
        }
        return bins;

    }
}

class Histogram2 implements Histogram {
    private int[] counts;
    public Histogram2(int span) {
        counts = new int[span];
    }
    public synchronized void increment(int bin) {
        counts[bin] = counts[bin] + 1;
    }
    public synchronized int getCount(int bin) {
        return counts[bin];
    }
    public int getSpan() {
        return counts.length;
    }

    public synchronized int[] getBins(){
        return counts.clone();
    }
}

class Histogram1 implements Histogram {
    private int[] counts;
    public Histogram1(int span) {
        counts = new int[span];
    }
    public void increment(int bin) {
        counts[bin] = counts[bin] + 1;
    }
    public int getCount(int bin) {
        return counts[bin];
    }
    public int getSpan() {
        return counts.length;
    }

    public int[] getBins(){
        return counts.clone();
    }
}
