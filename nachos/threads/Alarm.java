package nachos.threads;
import java.util.PriorityQueue;
import java.util.Comparator;
import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    public static void alarm_test1()
    {
        int durations[] = {1000, 1001, 10*1000, 100*1000};
        long t0, t1;
    
        for (int d : durations)
        {
            t0 = Machine.timer().getTime();
            ThreadedKernel.alarm.waitUntil (d);
            t1 = Machine.timer().getTime();
            System.out.println ("Alarm_test1: waited for " + (t1 - t0) + " ticks, ddl:" + d);
        }
    }
    
    public static void selfTest()
    {
        alarm_test1();
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        boolean inStatus=Machine.interrupt().disable();
        while(!waitQueue.isEmpty() && waitQueue.peek().wake_time <= Machine.timer().getTime())
            waitQueue.poll().thread.ready();
		KThread.yield();
		Machine.interrupt().restore(inStatus);
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
    long wakeTime = Machine.timer().getTime() + x;
    /*
    //Busy Wait
	while (wakeTime > Machine.timer().getTime())
        KThread.yield();
    */
    KThread currentthread = KThread.currentThread();
	Thread_with_WakeTime thread_time=new Thread_with_WakeTime(currentthread, wakeTime);
	boolean inStatus=Machine.interrupt().disable();
	waitQueue.add(thread_time);
	KThread.sleep();
	Machine.interrupt().restore(inStatus);
    }

//#####
    private class Thread_with_WakeTime
    {
		KThread thread;
		long wake_time;
		public Thread_with_WakeTime(KThread thread, long wake_time){
			this.thread = thread;
			this.wake_time = wake_time;
		}
    }
    

    private static Comparator<Thread_with_WakeTime> Thread_Comparator = new Comparator<Thread_with_WakeTime>()
    {
 
        @Override
        public int compare(Thread_with_WakeTime x, Thread_with_WakeTime y) {
            if (x.wake_time < y.wake_time) return -1;
            else if (x.wake_time > y.wake_time) return 1;
            else return 0;
        }
    };
 
	private PriorityQueue<Thread_with_WakeTime> waitQueue=new PriorityQueue<Thread_with_WakeTime>(11, Thread_Comparator);
//#####
}
