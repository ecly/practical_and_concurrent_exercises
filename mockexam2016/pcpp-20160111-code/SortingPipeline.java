// Pipelined sorting using P>=1 stages, each maintaining an internal
// collection of size S>=1.  Stage 1 contains the largest items, stage
// 2 the second largest, ..., stage P the smallest ones.  In each
// stage, the internal collection of items is organized as a minheap.
// When a stage receives an item x and its collection is not full, it
// inserts it in the heap.  If the collection is full and x is less
// than or equal to the collections's least item, it forwards the item
// to the next stage; otherwise forwards the collection's least item
// and inserts x into the collection instead.

// When there are itemCount items and stageCount stages, each stage
// must be able to hold at least ceil(itemCount/stageCount) items,
// which equals (itemCount-1)/stageCount+1.

// sestoft@itu.dk * 2016-01-10

import java.util.stream.DoubleStream;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.IntToDoubleFunction;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Executors;
import org.multiverse.api.references.*;
import static org.multiverse.api.StmUtils.*;
import org.multiverse.api.StmUtils;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Collectors.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.
import java.util.Arrays;

public class SortingPipeline {
	public static void main(String[] args) {
		SystemInfo();
		final int count = 60, P = 4;
		final double[] arr = DoubleArray.randomPermutation(count);
        /*
		final BlockingDoubleQueue[] queues = new BlockingDoubleQueue[P+1];
		for(int i = 0; i <= P; i++) {
			queues[i] = new StmBlockingNDoubleQueue();
		}	
		SortingPipeline.sortPipeline(arr, P, queues);*/
		StreamSortingStage stage0 = new StreamSortingStage(DoubleStream.of(arr), 20);
		DoubleStream output0 = stage0.getInput().flatmap(x -> stage0.stage.apply(x));
		StreamSortingStage stage1 = new StreamSortingStage(output0, 20);
		DoubleStream output1 = stage1.getInput.flatmap(x -> stage1.stage.apply(x));
	    StreamSortingStage stage2 = new StreamSortingStage(output1, 20);
		DoubleStream output2 = stage2.getInput.flatmap(x -> stage2.stage.apply(x));
		for(double d : output2.toArray()) {
			System.out.println(d);
		}
	}

	private static void sortPipeline(double[] arr, int P, BlockingDoubleQueue[] queues) {
		ArrayList<Thread> threads = new ArrayList<>();
		int S = arr.length/P;
		threads.add(new Thread(new DoubleGenerator(arr, arr.length, queues[0])));
		for(int i = 0; i < P; i++) {
			int itemCount = arr.length + (P-i-1)*S;
			threads.add(new Thread(new SortingStage(queues[i], queues[i+1], itemCount, S)));
		}
		threads.add(new Thread(new SortedChecker(arr.length, queues[queues.length-1])));
		for(Thread t : threads) {
			t.start();
		}
		for(Thread t : threads) {
			try {
				t.join();
			} 
			catch(InterruptedException e) {
				System.out.println(e);
			}
		}
	}

	static class StreamSortingStage {
		private final DoubleStream input;
		private final double[] heap;
		private int heapSize;

		public StreamSortingStage(DoubleStream input, int S) {
			this.input = input;
			heap = new double[S];
		}

		public DoubleStream getInput() {
			return input;
		}

		public DoubleStream stage(double item) {
			if(heapSize < heap.length) {
				heap[heapSize++] = item;
				DoubleArray.minheapSiftup(heap, heapSize-1, heapSize-1);
				return DoubleStream.empty();
			} else if (item <= heap[0]) {
				return DoubleStream.of(item);
			} else {
				double least = heap[0];
				heap[0] = item;
				DoubleArray.minheapSiftdown(heap, 0, heapSize-1);
				return DoubleStream.of(item);
			}
		}

	}

	static class SortingStage implements Runnable {
		private final BlockingDoubleQueue input, output;
		private int heapSize;
		private int itemCount;
		private final double[] heap;

		public SortingStage(BlockingDoubleQueue input, BlockingDoubleQueue output, int itemCount, int size) {
			this.input = input;
			this.output = output;
			this.itemCount = itemCount;
			heap = new double[size];
		}

		public void run() { 
			while (itemCount > 0) {
				double x = input.take();
				if (heapSize < heap.length) { // heap not full, put x into it
					heap[heapSize++] = x;
					DoubleArray.minheapSiftup(heap, heapSize-1, heapSize-1);
				} else if (x <= heap[0]) {
					// x is small, forward
					output.put(x);
					itemCount--;
				} else {
					// forward least, replace with x
					double least = heap[0];
					heap[0] = x;
					DoubleArray.minheapSiftdown(heap, 0, heapSize-1);
					output.put(least);
					itemCount--;
				}
			}  
		}
	}

	static class DoubleGenerator implements Runnable {
		private final BlockingDoubleQueue output;
		private final double[] arr;  // The numbers to feed to output
		private final int infinites;

		public DoubleGenerator(double[] arr, int infinites, BlockingDoubleQueue output) {
			this.arr = arr;
			this.output = output;
			this.infinites = infinites;
		}

		public void run() { 
			for (int i=0; i<arr.length; i++)  // The numbers to sort
				output.put(arr[i]);
			for (int i=0; i<infinites; i++)   // Infinite numbers for wash-out
				output.put(Double.POSITIVE_INFINITY);
		}
	}

	static class SortedChecker implements Runnable {
		// If DEBUG is true, print the first 100 numbers received
		private final static boolean DEBUG = true;
		private final BlockingDoubleQueue input;
		private final int itemCount; // the number of items to check

		public SortedChecker(int itemCount, BlockingDoubleQueue input) {
			this.itemCount = itemCount;
			this.input = input;
		}

		public void run() { 
			int consumed = 0;
			double last = Double.NEGATIVE_INFINITY;
			while (consumed++ < itemCount) {
				double p = input.take();
				if (DEBUG && consumed <= 100) 
					System.out.print(p + " ");
				if (p <= last)
					System.out.printf("Elements out of order: %g before %g%n", last, p);
				last = p;
			}
			if (DEBUG)
				System.out.println();
		}
	}

	// --- Benchmarking infrastructure ---

	// NB: Modified to show milliseconds instead of nanoseconds

	public static double Mark7(String msg, IntToDoubleFunction f) {
		int n = 10, count = 1, totalCount = 0;
		double dummy = 0.0, runningTime = 0.0, st = 0.0, sst = 0.0;
		do { 
			count *= 2;
			st = sst = 0.0;
			for (int j=0; j<n; j++) {
				Timer t = new Timer();
				for (int i=0; i<count; i++) 
					dummy += f.applyAsDouble(i);
				runningTime = t.check();
				double time = runningTime * 1e3 / count;
				st += time; 
				sst += time * time;
				totalCount += count;
			}
		} while (runningTime < 0.25 && count < Integer.MAX_VALUE/2);
		double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
		System.out.printf("%-25s %15.1f ms %10.2f %10d%n", msg, mean, sdev, count);
		return dummy / totalCount;
	}

	public static void SystemInfo() {
		System.out.printf("# OS:   %s; %s; %s%n", 
				System.getProperty("os.name"), 
				System.getProperty("os.version"), 
				System.getProperty("os.arch"));
		System.out.printf("# JVM:  %s; %s%n", 
				System.getProperty("java.vendor"), 
				System.getProperty("java.version"));
		// The processor identifier works only on MS Windows:
		System.out.printf("# CPU:  %s; %d \"cores\"%n", 
				System.getenv("PROCESSOR_IDENTIFIER"),
				Runtime.getRuntime().availableProcessors());
		java.util.Date now = new java.util.Date();
		System.out.printf("# Date: %s%n", 
				new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(now));
	}

	// Crude wall clock timing utility, measuring time in seconds

	static class Timer {
		private long start, spent = 0;
		public Timer() { play(); }
		public double check() { return (System.nanoTime()-start+spent)/1e9; }
		public void pause() { spent += System.nanoTime()-start; }
		public void play() { start = System.nanoTime(); }
	}
}

// ----------------------------------------------------------------------

// Queue interface

interface BlockingDoubleQueue {
	double take();
	void put(double item);
}

// The queue implementations

// TO DO

// ----------------------------------------------------------------------

class DoubleArray {
	public static double[] randomPermutation(int n) {
		double[] arr = fillDoubleArray(n);
		shuffle(arr);
		return arr;
	}

	private static double[] fillDoubleArray(int n) {
		double[] arr = new double[n];
		for (int i = 0; i < n; i++)
			arr[i] = i + 0.1;
		return arr;
	}

	private static final java.util.Random rnd = new java.util.Random();

	private static void shuffle(double[] arr) {
		for (int i = arr.length-1; i > 0; i--)
			swap(arr, i, rnd.nextInt(i+1));
	}

	// Swap arr[s] and arr[t]
	private static void swap(double[] arr, int s, int t) {
		double tmp = arr[s]; arr[s] = arr[t]; arr[t] = tmp;
	}

	// Minheap operations for parallel sort pipelines.  
	// Minheap invariant: 
	// If heap[0..k-1] is a minheap, then heap[(i-1)/2] <= heap[i] for
	// all indexes i=1..k-1.  Thus heap[0] is the smallest element.

	// Although stored in an array, the heap can be considered a tree
	// where each element heap[i] is a node and heap[(i-1)/2] is its
	// parent. Then heap[0] is the tree's root and a node heap[i] has
	// children heap[2*i+1] and heap[2*i+2] if these are in the heap.

	// In heap[0..k], move node heap[i] downwards by swapping it with
	// its smallest child until the heap invariant is reestablished.

	public static void minheapSiftdown(double[] heap, int i, int k) {
		int child = 2 * i + 1;                          
		if (child <= k) {
			if (child+1 <= k && heap[child] > heap[child+1])
				child++;                                  
			if (heap[i] > heap[child]) {
				swap(heap, i, child); 
				minheapSiftdown(heap, child, k); 
			}
		}
	}

	// In heap[0..k], move node heap[i] upwards by swapping with its
	// parent until the heap invariant is reestablished.
	public static void minheapSiftup(double[] heap, int i, int k) {
		if (0 < i) {
			int parent = (i - 1) / 2;
			if (heap[i] < heap[parent]) {
				swap(heap, i, parent); 
				minheapSiftup(heap, parent, k); 
			}
		}
	}
}

class WrappedArrayDoubleQueue implements BlockingDoubleQueue {
	private ArrayBlockingQueue<Double> queue;
	private final int capacity = 50;

	public WrappedArrayDoubleQueue() {
		queue = new ArrayBlockingQueue(50);
	}

	public double take() {
		try {
			return queue.take();
		}
		catch (InterruptedException e) {
			System.out.println(e);
			return -1.0;
		}
	}

	public void put(double item) {
		try {
			queue.put(item);
		}
		catch (InterruptedException e) {
			System.out.println(e);
		}
	}
}

class BlockingNDoubleQueue implements BlockingDoubleQueue {
	private final double[] items = new double[50];
	private int head, tail, size; 

	public BlockingNDoubleQueue() {
	}

	public void put(double item) {
		synchronized(this) {
			while (size == items.length) {
				try { this.wait(); }
				catch (InterruptedException e) {}
			}
			items[tail] = item;
			tail = (tail + 1) % items.length;
			size++;
			this.notifyAll();
		}
	}

	public double take() {
		synchronized(this) {
			while (size == 0) {
				try { this.wait(); }
				catch (InterruptedException e) {}
			}
			double item = items[head];
			head = (head + 1) % items.length;
			size--;
			this.notifyAll();
			return item;
		}
	}
}

class UnboundedDoubleQueue implements BlockingDoubleQueue {
	private Node head, tail; 
	private int size;

	public UnboundedDoubleQueue() {}

	private class Node { 
		double item;
		Node next;
	}

	public void put(double item) {
		synchronized(this) {
			Node oldTail = tail;
			tail = new Node();
			tail.item = item;
			tail.next = null;
			if (size == 0) {
				head = tail;
			}
			else {
				oldTail.next = tail;
			}
			size++;
			this.notifyAll();
		}
	}

	public double take() {
		synchronized(this) {
			while (size == 0) {
				try { this.wait(); }
				catch (InterruptedException e) {}
			}
			double item = head.item;
			head = head.next;
			if (size == 0) {
				tail = null;
			}
			size--;
			this.notifyAll();
			return item;
		}
	}
}	

class NolockNDoubleQueue implements BlockingDoubleQueue {
	double[] items = new double[50];
	int head, tail; 

	public NolockNDoubleQueue() {
	}

	public void put(double item) {
		while (tail - head == items.length) {}
		items[tail % items.length] = item;
		tail++;
	}

	public double take() {
		while (tail - head == 0) {}
		double item = items[head % items.length];
		head++;
		return item;
	}
}

class MSUnboundedDoubleQueue implements BlockingDoubleQueue {
	private final AtomicReference<Node> head, tail;

	public MSUnboundedDoubleQueue() {
		Node dummy = new Node(-1.0, null);
		head = new AtomicReference<Node>(dummy);
		tail = new AtomicReference<Node>(dummy);
	}

	private class Node { 
		final double item;
		final AtomicReference<Node> next;

		public Node(double item, Node next) {
			this.item = item;
			this.next = new AtomicReference<Node>(next);
		}
	}

	public void put(double item) {
		Node node = new Node(item, null);
		while (true) {
			Node last = tail.get(),
				 next = last.next.get();
			if (last == tail.get()) {
				if (next == null) {
					if (last.next.compareAndSet(next, node)) {
						tail.compareAndSet(last, node);
						return;
					}
				} else
					tail.compareAndSet(last, next);
			}
		}
	}

	public double take() {
		while (true) {
			Node first = head.get(),
				 last = tail.get(),
				 next = first.next.get();
			if (first == head.get()) {
				while (first == last) {
					last = tail.get();
					next = first.next.get();
					if (next != null) {
						tail.compareAndSet(last, next);
					}	
				}
				double result = next.item;
				if (head.compareAndSet(first, next))
					return result;
			}
		}
	}
}

class StmBlockingNDoubleQueue implements BlockingDoubleQueue {
	private final double[] items = new double[50];
	private final TxnInteger head = StmUtils.newTxnInteger(0);
	private final TxnInteger tail = StmUtils.newTxnInteger(0);
	private int size; 
	private double item;

	public StmBlockingNDoubleQueue() {
	}

	public void put(double putItem) {
	    while (size == items.length) {}

		atomic(() -> {
			items[tail.get()] = putItem;
			tail.set((tail.get() + 1) % items.length);
			size++;
		});
	}

	public double take() {
		while (size == 0) {}

		atomic(() -> {
			item = items[head.get()];
			head.set((head.get() + 1) % items.length);
			size--;
		});
		return item;
	}
}

