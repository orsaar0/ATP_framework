package bgu.atd.a1;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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

	private Map<String, PrivateState> actors;
	private Map<String, Lock> locksByID;
	private Map<String, Queue<Action>> actionsByActorID;

    /**
	 * creates a {@link ActorThreadPool} which has nthreads. Note, threads
	 * should not get started until calling to the {@link #start()} method.
	 *
	 * Implementors note: you may not add other constructors to this class nor
	 * you allowed to add any other parameter to this constructor - changing
	 * this may cause automatic tests to fail..
	 *
	 * @param nthreads
	 *            the number of threads that should be started by this thread
	 *            pool
	 */
	public ActorThreadPool(int nthreads) {
		actors = new ConcurrentHashMap<>(nthreads);
        actionsByActorID = new ConcurrentHashMap<>(nthreads);
	}

	/**
	 * getter for actors
	 * @return actors
	 */
	public Map<String, PrivateState> getActors(){
		return actors;
	}
	
	/**
	 * getter for actor's private state
	 * @param actorId actor's id
	 * @return actor's private state
	 */
	public PrivateState getPrivateState(String actorId){
		return actors.get(actorId);
	}

	/**
	 * submits an action into an actor to be executed by a thread belongs to
	 * this thread pool
	 *
	 * @param action
	 *            the action to execute
	 * @param actorId
	 *            corresponding actor's id
	 * @param actorState
	 *            actor's private state (actor's information)
	 */
	public void submit(Action<?> action, String actorId, PrivateState actorState) {
		actors.putIfAbsent(actorId, actorState);
		actionsByActorID.putIfAbsent(actorId, new ConcurrentLinkedQueue<>());
		locksByID.putIfAbsent(actorId, new ReentrantLock());

		actionsByActorID.get(actorId).add(action);
	}

	/**
	 * closes the thread pool - this method interrupts all the threads and waits
	 * for them to stop - it is returns *only* when there are no live threads in
	 * the queue.
	 *
	 * after calling this method - one should not use the queue anymore.
	 *
	 * @throws InterruptedException
	 *             if the thread that shut down the threads is interrupted
	 */
	public void shutdown() throws InterruptedException {
		// TODO: replace method body with real implementation
		throw new UnsupportedOperationException("Not Implemented Yet.");
	}

	/**
	 * start the threads belongs to this thread pool
	 */
	public void start() {
		// TODO: replace method body with real implementation
		throw new UnsupportedOperationException("Not Implemented Yet.");
	}

}
