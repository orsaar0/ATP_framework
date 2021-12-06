package bgu.atd.a1;

import bgu.atd.a1.sim.Warehouse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * represents an actor thread pool - to understand what this class does please
 * refer to your assignment.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class ActorThreadPool {

	private final Integer nthreads;
	private final AtomicInteger currentActions = new AtomicInteger(0);
	protected Boolean terminate = false;
	private final List<Thread> activeThreads = new ArrayList<>();
	private final List<Thread> sleepingThreads = new ArrayList<>();
	private final Lock threadsLock  = new ReentrantLock();
	private final HashMap<String, PrivateState> actors = new HashMap<>();
	private final Map<String, Lock> locksByID = new ConcurrentHashMap<>();
	private final Map<String, Queue<Action<?>>> actionsByActorID = new ConcurrentHashMap<>();
	public Warehouse warehouse;

	/**
	 * creates a {@link ActorThreadPool} which has nthreads. Note, threads
	 * should not get started until calling to the {@link #start()} method.
	 * <p>
	 * Implementors note: you may not add other constructors to this class nor
	 * you allowed to add any other parameter to this constructor - changing
	 * this may cause automatic tests to fail..
	 *
	 * @param nthreads the number of threads that should be started by this thread
	 *                 pool
	 */
	public ActorThreadPool(int nthreads) {
		this.nthreads = nthreads;
	}

	/**
	 * getter for actors
	 *
	 * @return actors
	 */
	public Map<String, PrivateState> getActors() {
		return actors;
	}

	/**
	 * getter for actor's private state
	 *
	 * @param actorId actor's id
	 * @return actor's private state
	 */
	public PrivateState getPrivateState(String actorId) {
		return actors.get(actorId);
	}

	/**
	 * submits an action into an actor to be executed by a thread belongs to
	 * this thread pool
	 *
	 * @param action     the action to execute
	 * @param actorId    corresponding actor's id
	 * @param actorState actor's private state (actor's information)
	 */
	public void submit(Action<?> action, String actorId, PrivateState actorState) {
//		if (terminate)
//			return;
		actors.putIfAbsent(actorId, actorState);
		actionsByActorID.putIfAbsent(actorId, new ConcurrentLinkedQueue<>());
		locksByID.putIfAbsent(actorId, new ReentrantLock());

		currentActions.incrementAndGet();

//		currentActions is decremented last (added again), so the program shuts down gracefully
		action.getResult().subscribe(() ->
				action.getResult().subscribe(() -> {
					currentActions.decrementAndGet();
					System.out.println("promise: Number of actions after decrement: " + currentActions.get());
				}));

		actionsByActorID.get(actorId).add(action);
		System.out.println("submit: Number of actions after increment: " + currentActions.get());

//		threadsLock.lock();
//		if (activeThreads.size() < nthreads && activeThreads.size() < actionsByActorID.keySet().size()) {
//			Thread worker = sleepingThreads.remove(0);
//			worker.start();
//			activeThreads.add(worker);
//		}
//		threadsLock.unlock();

	}

	/**
	 * closes the thread pool - this method interrupts all the threads and waits
	 * for them to stop - it is returns *only* when there are no live threads in
	 * the queue.
	 * <p>
	 * after calling this method - one should not use the queue anymore.
	 *
	 * @throws InterruptedException if the thread that shut down the threads is interrupted
	 */
	public void shutdown() throws InterruptedException {
		terminate = true;
		System.out.println("shutdown: Number of actions after shutdown: " + currentActions.get());
		while (currentActions.get() != 0) {
			Thread.sleep(1000);
		}
		for (Thread thread: activeThreads) {
			thread.interrupt();
//			thread.join();
			System.out.println("shutdown: found an active thread: " + thread.getName());
		}
		for (Thread thread: sleepingThreads) {
			thread.interrupt();
//			thread.join();
			System.out.println("shutdown: found an active thread: " + thread.getName());
		}
//		System.exit(0);
	}

	/**
	 * start the threads belongs to this thread pool
	 */
	public void start() {
		for (int i = 0; i < nthreads; i++) {
			Thread worker = new Thread(this::task);
			worker.setName("Worker " + (i));
//			sleepingThreads.add(worker);
			activeThreads.add(worker);
			worker.start();
		}

	}

	private void task(){
        while (true) {
//			threadsLock.lock();
//			if (activeThreads.size() > actionsByActorID.keySet().size()) {
//				System.out.println("task: found self " + activeThreads.remove(Thread.currentThread()));
//				activeThreads.remove(Thread.currentThread());
//				sleepingThreads.add(Thread.currentThread());
//				threadsLock.unlock();
//				Thread.currentThread().interrupt();
//			} else
//				threadsLock.unlock();
            for (String actor : locksByID.keySet()) {
                try {
                    if (locksByID.get(actor).tryLock()) {
                        Action<?> currAction = actionsByActorID.get(actor).poll();
                        if (currAction != null) {
                            currAction.handle(this, actor, actors.get(actor));
                        }
                        locksByID.get(actor).unlock();
                    }
                } catch (Exception e) {
					e.printStackTrace();
				}
            }
        }
    }
}
