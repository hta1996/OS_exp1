package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;
		tq=ThreadedKernel.scheduler.newThreadQueue(false);
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		conditionLock.release();

		boolean intS=Machine.interrupt().disable();
		tq.waitForAccess(KThread.currentThread());
		KThread.sleep();
		Machine.interrupt().restore(intS);

		conditionLock.acquire();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		

		boolean intS=Machine.interrupt().disable();
		KThread tp=tq.nextThread();
		if(tp!=null)tp.ready();
		Machine.interrupt().restore(intS);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		boolean intS=Machine.interrupt().disable();
		KThread tp=tq.nextThread();
		while(tp!=null)
		{
			tp.ready();
			tp=tq.nextThread();
		}
		Machine.interrupt().restore(intS);
    }
	public static void cond2test()
	{     
    	System.out.println("Testing Condition2\n");
    	final Lock lock=new Lock();
    	final Condition2 Con=new Condition2(lock);
       	
       	//first test
       	System.out.println("\nT1: sleep and wake\n");
     	KThread TT1=new KThread(new Runnable()
    	{
     		public void run()
       		{
				lock.acquire();    	   
    	   		System.out.println("T1-1: Go to sleep\n");
    	   		Con.sleep();
    	   		System.out.println("T1-1 Completed\n");
    	   		lock.release();
    	 	}
       	});
    	TT1.fork();
    	KThread TT2=new KThread(new Runnable()
    	{
			public void run()
			{
        		lock.acquire();

            	System.out.println("T2-2: Wake a thread\n");
            	Con.wake();      
				System.out.println("T2-2 Completed\n");
				lock.release();
			} 
		});
		TT2.fork();
		TT1.join();
		
		//second test
		System.out.println("\nT2: wakeall\n");
		KThread SS1=new KThread(new Runnable()
		{

        	public void run()
        	{
        		lock.acquire();
            	System.out.println("T2-1: Go to sleep\n");
            	Con.sleep();      
				System.out.println("T2-1 Completed\n");
				lock.release();
       	}});
		SS1.fork();
		KThread SS2=new KThread(new Runnable()
		{
        	public void run()
        	{
        		lock.acquire();
            	System.out.println("T2-2: Go to sleep\n");
            	Con.sleep();      
				System.out.println("T2-2 Completed\n");
				lock.release();
       	}});
		SS2.fork();
		KThread SS3=new KThread(new Runnable()
		{
        	public void run()
        	{
        		lock.acquire();
            	System.out.println("T2-3: Wakeall\n");
            	Con.wakeAll();
            	System.out.println("T2-3 Completed\n");
				lock.release();
       	}});
		SS3.fork();
	}

    /**
     */
    public static void selfTest() {
		//Lib.debug(dbgThread, "Enter KThread.selfTest");
		//joinTest1();
		cond2test();
	//new KThread(new PingTest(1)).setName("forked thread").fork();
    //new PingTest(0).run();
    }
    private Lock conditionLock;
    private ThreadQueue tq;
}


