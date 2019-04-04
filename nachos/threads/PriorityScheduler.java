package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
//暴走鸡兽
import java.util.LinkedList;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
	 
    public PriorityScheduler() {
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }


	//暴走鸡兽
	public static void selfTest() {
		Lock resource = new Lock();
		Runnable r1 = new Runnable() {
			@Override
			public void run() {
				System.out.println("pang1");
				resource.acquire();
				KThread.yield();
				System.out.println("I'm T1.");
				resource.release();
			}
		};
		Runnable r2 = new Runnable() {
			@Override
			public void run() {
				System.out.println("pang2");
				KThread.yield();
				System.out.println("I'm T2.");
			}
		};
		KThread t1 = new KThread(r1).setName("T1");
		KThread t2 = new KThread(r2).setName("T2");
		boolean status = Machine.interrupt().disable();
		ThreadedKernel.scheduler.setPriority(t1, 5);
		ThreadedKernel.scheduler.setPriority(t2, 2);
		Machine.interrupt().restore(status);
		// 认为main线程是T3，开始运行
		
		System.out.println("ji1");
		resource.acquire();
		t2.fork(); // T2再运行，打断T3
		System.out.println("ji2");
		t1.fork(); // T1再运行，打断T2
		System.out.println("ji3");
		//resource.waitQueue.print();
		KThread.yield();
		System.out.println("ji4");
		System.out.println("I'm T3.");
		resource.release();
		ThreadedKernel.alarm.waitUntil(10);
}

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
	protected class PriorityQueue extends ThreadQueue {
		PriorityQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			//System.out.println("++ "+getThreadState(thread).thread.getName());
			getThreadState(thread).waitForAccess(this);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).acquire(this);
		}

		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// implement me
			//暴走鸡兽
			ThreadState tmp=pickNextThread();
			if(tmp==null)return null;
			return tmp.thread;
		}

		/**
		* Return the next thread that <tt>nextThread()</tt> would return,
		* without modifying the state of this queue.
		*
		* @return	the next thread that <tt>nextThread()</tt> would
		*		return.
		*/
		protected ThreadState pickNextThread() {
			//暴走鸡兽
			// implement me
			if(thisQueue.isEmpty())return null;
			long young_age=0;
			int maxx=-1;
			ThreadState Answer=null;
			for(ThreadState i:thisQueue){
				int jishou=i.getEffectivePriority();
				//System.out.println(i.thread.getName()+" "+jishou+" "+transferPriority);
				if(jishou>maxx){
					maxx=jishou;
					young_age=i.age;
					Answer=i;
				}
				else if(jishou==maxx && i.age<young_age){
					Answer=i;
					young_age=i.age;
				}
			}
			thisQueue.remove(Answer);
			Answer.MyWaitingQueue=null;
			if(ResourceHolder!=null)ResourceHolder.update();
		//	System.out.println(Answer.thread.getName());
			return Answer;
		}
		
		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			//System.out.println("JIBA "+" "+thisQueue.size()+" "+ResourceHolder.thread.getName());
		//	System.out.println("-----");
		//	for(ThreadState i: thisQueue)System.out.println(i.thread.getName()+" ");
		//	System.out.println(">>>>>");
			// implement me (if you want)
		}
		/**
		* <tt>true</tt> if this queue should transfer priority from waiting
		* threads to the owning thread.
		*/
		public boolean transferPriority;	
		//暴走鸡兽
		private LinkedList<ThreadState> thisQueue = new LinkedList<ThreadState>();
		private ThreadState ResourceHolder=null;

	}

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
		/**
		* Allocate a new <tt>ThreadState</tt> object and associate it with the
		* specified thread.
		*
		* @param	thread	the thread this state belongs to.
		*/
		public ThreadState(KThread thread) {
			this.thread = thread;

			this.age=Machine.timer().getTime();
			
			setPriority(priorityDefault);
		}

		/**
		* Return the priority of the associated thread.
		*
		* @return	the priority of the associated thread.
		*/
		public int getPriority() {
			return priority;
		}

		/**
		* Return the effective priority of the associated thread.
		*
		* @return	the effective priority of the associated thread.
		*/
		public int getEffectivePriority() {
			// implement me
			//暴走鸡兽
			return EffectivePriority;
		}

		/**
		* Set the priority of the associated thread to th
				ResourceHolder=null;e specified value.
		*
		* @param	priority	the new priority.
		*/
		public void setPriority(int priority) {
			if (this.priority == priority)
			return;
			
			// implement me
			//暴走鸡兽
			this.priority=priority;
			update();
		}

		//**暴走鸡兽**
		public void update(){
			int maxx=priority;
			for(PriorityQueue Q:myQueue)if(Q.transferPriority==true){
				for(ThreadState j:Q.thisQueue){
					int jiba=j.getEffectivePriority();
					if(jiba>maxx)maxx=jiba;
				}
			}
			if(MyWaitingQueue!=null && MyWaitingQueue.ResourceHolder!=null&&
				maxx!=EffectivePriority && MyWaitingQueue.transferPriority==true){
				EffectivePriority=maxx;
				MyWaitingQueue.ResourceHolder.update();
			}
			EffectivePriority=maxx;
		}
		/**
		* Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		* the associated thread) is invoked on the specified priority queue.
		* The associated thread is therefore waiting for access to the
		* resource guarded by <tt>waitQueue</tt>. This method is only called
		* if the associated thread cannot immediately obtain access.
		*
		* @param	waitQueue	the queue that the associated thread is
		*				now waiting on.
		*
		* @see	nachos.threads.ThreadQueue#waitForAccess
		*/
		public void waitForAccess(PriorityQueue waitQueue) {
			//暴走鸡兽
			//implement me
			age=Machine.timer().getTime();
			waitQueue.thisQueue.add(this);
			MyWaitingQueue=waitQueue;
			if(waitQueue.ResourceHolder==this)
				waitQueue.print();
			if(waitQueue.ResourceHolder!=null){
				waitQueue.ResourceHolder.update();
			}
		}

		/**
		* Called when the associated thread has acquired access to whatever is
		* guarded by <tt>waitQueue</tt>. This can occur either as a result of
		* <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		* <tt>thread</tt> is the associated thread), or as a result of
		* <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		*
		* @see	nachos.threads.ThreadQueue#acquire
		* @see	nachos.threads.ThreadQueue#nextThread
		*/
		public void acquire(PriorityQueue waitQueue) {
			//暴走鸡兽
			// implement me
			if(waitQueue.ResourceHolder!=null){
				waitQueue.ResourceHolder.myQueue.remove(waitQueue);
				waitQueue.ResourceHolder.update();
			}
			if(waitQueue.thisQueue.indexOf(this)!=-1)waitQueue.thisQueue.remove(this);
			waitQueue.ResourceHolder=this;
			MyWaitingQueue=null;
			myQueue.add(waitQueue);
			update();
		}	

		/** The thread with which this object is associated. */	   
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority;
		//暴走鸡兽
		protected int EffectivePriority;
		private LinkedList<PriorityQueue> myQueue = new LinkedList<PriorityQueue>();
		private PriorityQueue MyWaitingQueue=null;
		protected long age;
    }
}
