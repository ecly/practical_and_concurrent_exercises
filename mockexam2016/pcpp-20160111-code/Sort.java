// COMPILE:
// javac -cp scala.jar:akka-actor.jar Broadcast.java 
// RUN:
// java -cp scala.jar:akka-actor.jar:akka-config.jar:. Broadcast

import java.util.*;
import java.io.*;
import akka.actor.*;
import java.util.ArrayList;

// -- MESSAGES --------------------------------------------------
class InitSorterMessage implements Serializable {
	public final ActorRef sorter;
	public InitSorterMessage(ActorRef sorter) {
		this.sorter = sorter;
	}
}

class NumMessage implements Serializable {
	public final Integer num;
	public NumMessage(Integer num) {
		this.num = num;
	}
}

class TransmitMessage implements Serializable {
	public final ActorRef sorter;
	public final Integer[] items;
	public TransmitMessage(ActorRef sorter, Integer[] items) {
		this.sorter = sorter;
		this.items = items;
	}
}

// -- ACTORS --------------------------------------------------
class SorterActor extends UntypedActor {
	ActorRef out;
	ArrayList<Integer> items = new ArrayList<>();

	public void onReceive(Object o) throws Exception {
		if (o instanceof InitSorterMessage) {
			InitSorterMessage sm = (InitSorterMessage) o;
			out = sm.sorter;
		} else if (o instanceof NumMessage) {
			NumMessage nm = (NumMessage) o;
			if (items.size() < 4) {
				items.add(nm.num);
				Collections.sort(items);
			}
			else {
				Integer item = items.remove(0);
				items.add(nm.num);
				Collections.sort(items);
				out.tell(new NumMessage(item), getSelf());
			}
		}
	}
}

class EchoActor extends UntypedActor {
	public void onReceive(Object o) throws Exception {
		if (o instanceof NumMessage) {
			NumMessage nm = (NumMessage) o;
			System.out.println(nm.num);
		}
	}
}

class TransmitActor extends UntypedActor {
	public void onReceive(Object o) throws Exception {
		if (o instanceof TransmitMessage) {
			TransmitMessage tm = (TransmitMessage) o;
			ActorRef sorter = tm.sorter;
			Integer[] items = tm.items;
			for(Integer i : items) {
				sorter.tell(new NumMessage(i), getSelf());
			}
		}
	}
}

// -- MAIN --------------------------------------------------

public class Sort {
	public static void main(String[] args) {
		final ActorSystem system = ActorSystem.create("SorterSystem");
		final ActorRef first = system.actorOf(Props.create(SorterActor.class), "first");
		final ActorRef second = system.actorOf(Props.create(SorterActor.class), "second");
		final ActorRef echo = system.actorOf(Props.create(EchoActor.class), "echo");
		final ActorRef transmit = system.actorOf(Props.create(TransmitActor.class), "transmit");

		first.tell(new InitSorterMessage(second), ActorRef.noSender());
		second.tell(new InitSorterMessage(echo), ActorRef.noSender());	

		transmit.tell(new TransmitMessage(first, new Integer[]{4,7,2,8,6,1,5,3}), ActorRef.noSender());
		transmit.tell(new TransmitMessage(first, new Integer[]{9,9,9,9,9,9,9,9}), ActorRef.noSender());
		system.shutdown();
	}
}
