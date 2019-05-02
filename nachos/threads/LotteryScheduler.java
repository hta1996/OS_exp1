package nachos.threads;

import java.util.*;
import nachos.machine.*;
import nachos.threads.PriorityScheduler.ThreadState;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

//WO CAO NI DA YE
//WO ZHENG GE JJ DOU DUAN LE A

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    }
    
    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	// implement me
    //Yes, sir
        return new LotteryQueue(transferPriority);
    }

    //==============================================================

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
    public static final int priorityMaximum = Integer.MAX_VALUE;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected LotteryThreadState getLotteryThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new LotteryThreadState(thread);

	return (LotteryThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
	protected class LotteryQueue extends ThreadQueue {
		LotteryQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			//System.out.println("++ "+getThreadState(thread).thread.getName());
			getLotteryThreadState(thread).waitForAccess(this);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getLotteryThreadState(thread).acquire(this);
		}

		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// implement me
			//暴走鸡兽
			LotteryThreadState tmp=pickNextThread();
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
		protected LotteryThreadState pickNextThread() {
			//暴走鸡兽
			// implement me
            //remove resource holder
			if(ResourceHolder!=null){
				if(ResourceHolder.myQueue.indexOf(this)!=-1)
                    ResourceHolder.myQueue.remove(this);
				ResourceHolder.Lottery_update();
			}
			LotteryThreadState Answer=null;


			if(thisQueue.isEmpty())return null;
            int total_ticket=0;
            int taoan_huang=0;
			for(LotteryThreadState i:thisQueue){
				int jishou=i.getEffectivePriority();
                total_ticket+=jishou;
			}
			Random random_number = new Random();
            taoan_huang=random_number.nextInt(total_ticket);
            int tmp=0;
			for(LotteryThreadState i:thisQueue){
				int jishou=i.getEffectivePriority();
                tmp+=jishou;
                if(taoan_huang<tmp){
                    Answer=i;
                    break;
                }
			}
			thisQueue.remove(Answer);
			Answer.MyWaitingQueue=null;
			ResourceHolder=Answer;
            Answer.myQueue.add(this);
            Answer.Lottery_update();
		//	System.out.println(total_ticket+" "+taoan_huang);
        //    System.out.println(Answer.thread.getName());
		//	System.out.println(thisQueue.size());
			
			return Answer;
		}
		
		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
		//	System.out.println("JIBA "+" "+thisQueue.size()+" "+ResourceHolder.thread.getName());
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
		private LinkedList<LotteryThreadState> thisQueue = new LinkedList<LotteryThreadState>();
		private LotteryThreadState ResourceHolder=null;

	}

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class LotteryThreadState extends ThreadState{
		/**
		* Allocate a new <tt>ThreadState</tt> object and associate it with the
		* specified thread.
		*
		* @param	thread	the thread this state belongs to.
		*/
		LotteryThreadState(KThread thread) {
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
			Lottery_update();
		}

		//**暴走鸡兽**
		public void Lottery_update(){
            int total_tickets=priority;
			for(LotteryQueue Q:myQueue)if(Q.transferPriority==true){
				for(ThreadState j:Q.thisQueue){
					int jiba=j.getEffectivePriority();
                    total_tickets+=jiba;
				}
			}
			if(MyWaitingQueue!=null && MyWaitingQueue.ResourceHolder!=null&&
				MyWaitingQueue.transferPriority==true){
				EffectivePriority=total_tickets;
                //System.out.println(MyWaitingQueue.ResourceHolder.thread.getName());
				MyWaitingQueue.ResourceHolder.Lottery_update();
			}
			EffectivePriority=total_tickets;
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
		public void waitForAccess(LotteryQueue waitQueue) {
			//暴走鸡兽
			//implement me
			age=Machine.timer().getTime();
			waitQueue.thisQueue.add(this);
			MyWaitingQueue=waitQueue;
			//if(waitQueue.ResourceHolder==this)
				//waitQueue.print();
			if(waitQueue.ResourceHolder!=null){
				waitQueue.ResourceHolder.Lottery_update();
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
		public void acquire(LotteryQueue waitQueue) {
            Lib.assertTrue(Machine.interrupt().disabled());
                
            Lib.assertTrue(waitQueue.thisQueue.isEmpty());
			//暴走鸡兽
			// implement me
			if(waitQueue.ResourceHolder!=null){
				waitQueue.ResourceHolder.myQueue.remove(waitQueue);
				waitQueue.ResourceHolder.Lottery_update();
			}
			if(waitQueue.thisQueue.indexOf(this)!=-1)waitQueue.thisQueue.remove(this);
			waitQueue.ResourceHolder=this;
			MyWaitingQueue=null;
			myQueue.add(waitQueue);
			Lottery_update();
		}	

		/** The thread with which this object is associated. */	   
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority;
		//暴走鸡兽
		protected int EffectivePriority;
		private LinkedList<LotteryQueue> myQueue = new LinkedList<LotteryQueue>();
		private LotteryQueue MyWaitingQueue=null;
		protected long age;
    }




}
