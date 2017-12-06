// sestoft@itu.dk * 2016-11-18, 2017-01-08

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;

public class TestMSQueueNeater extends Tests {
    public static void parTest(){
        final int threadCount = 10;
        final int perThreadAmount = 1000000;//enqueue/dequeue this amount of numbers per thread
        final ExecutorService executor = Executors.newWorkStealingPool(20);
        final MSQueueNeater<Integer> queue = new MSQueueNeater<Integer>();
        final CyclicBarrier startBarrier = new CyclicBarrier(threadCount*2+1);
        final CyclicBarrier stopBarrier = new CyclicBarrier(threadCount*2+1);
        final AtomicLong putSum = new AtomicLong(0);
        final AtomicLong takeSum = new AtomicLong(0);

        List<Callable<Void>> enqueues = new ArrayList<>();
        for (int t=0; t<threadCount; t++) {
            enqueues.add(() -> { 
                long sum = 0;
                final Random r = new Random();
                startBarrier.await();
                for (int i=0; i<=perThreadAmount; i++){
                    int val = r.nextInt();
                    sum += val;
                    queue.enqueue(val);
                }
                putSum.getAndAdd(sum);
                stopBarrier.await();
                return null;
            });
        }
        List<Callable<Void>> dequeues = new ArrayList<>();
        for (int t=0; t<threadCount; t++) {
            dequeues.add(() -> { 
                long sum = 0L;
                startBarrier.await();
                for (int i=0; i<=perThreadAmount; i++){
                    Integer val = null;
                    while(val == null) val = queue.dequeue();
                    sum += val;
                }
                takeSum.getAndAdd(sum);
                stopBarrier.await();
                return null;
            });
        } try {
            for (Callable<Void> c : enqueues)
                executor.submit(c);
            for (Callable<Void> d : dequeues)
                executor.submit(d);

            startBarrier.await();
            stopBarrier.await();

            assertTrue(takeSum.get() == putSum.get());
            System.out.println("parTest succeeded");
        } catch(Exception e){
            System.out.printf("expected: %d, actual: %d\n", putSum.get(), takeSum.get());
            System.out.println(e);
        }
    }
    public static void seqTest(){
        MSQueueNeater<Integer> queue = new MSQueueNeater<Integer>();

        try{
            // check for that empty queues return null
            assertTrue(queue.dequeue() == null);

            int testVal = 42;
            queue.enqueue(42);
            assertEquals(testVal, queue.dequeue());

            int expectedSum = 5050;
            for(int i = 0; i<=100; i++)
                queue.enqueue(i);

            int actualSum = 0;
            while (true){
                Integer val = queue.dequeue();
                if (val == null) break;
                actualSum+=val;
            }
            assertEquals(expectedSum,actualSum);
            System.out.println("seqTest succeeded");
        }catch(Exception e){
            System.out.print(e);
        }
    }



    public static void main(String[] args) {
        seqTest();
        parTest();
    }
}

class Tests {
    public static void assertEquals(int x, int y) throws Exception {
        if (x != y) 
            throw new Exception(String.format("ERROR: %d not equal to %d%n", x, y));
    }

    public static void assertTrue(boolean b) throws Exception {
        if (!b) 
            throw new Exception(String.format("ERROR: assertTrue"));
    }
}

interface UnboundedQueue<T> {
    void enqueue(T item);
    T dequeue();
}

// Unbounded non-blocking list-based lock-free queue by Michael and
// Scott 1996.  This version inspired by suggestions from Niels
// Abildgaard Roesen.

class MSQueueNeater<T> implements UnboundedQueue<T> {
    private final AtomicReference<Node<T>> head, tail;

    public MSQueueNeater() {
        Node<T> dummy = new Node<T>(null, null);
        head = new AtomicReference<Node<T>>(dummy);
        tail = new AtomicReference<Node<T>>(dummy);
    }

    public void enqueue(T item) { // at tail
        Node<T> node = new Node<T>(item, null);
        while (true) {
            final Node<T> last = tail.get(), next = last.next.get();
            if (next != null)
                tail.compareAndSet(last, next);
            else if (last.next.compareAndSet(null, node)) {
                tail.compareAndSet(last, node);
                return;
            }
        }
    }

    public T dequeue() { // from head
        while (true) {
            final Node<T> first = head.get(), last = tail.get(), next = first.next.get();
            if (next == null)
                return null;
            else if (first == last) 
                tail.compareAndSet(last, next);
            else if (head.compareAndSet(first, next))
                return next.item;
        }
    }

    private static class Node<T> {
        final T item;
        final AtomicReference<Node<T>> next;

        public Node(T item, Node<T> next) {
            this.item = item;
            this.next = new AtomicReference<Node<T>>(next);
        }
    }
}
