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
	{     //used for testing wake and wakeall
    	System.out.println("--------------Testing Condition 2 ------------------");
       
       //Variables for testing functions
       final Lock lock = new Lock();
       final Condition2 con2 = new Condition2(lock);
       
       KThread sleep = new KThread(new Runnable()
       {
       	//Test 1: Sleep
      	public void run()
       	{
    	  //get the Lock
    	   lock.acquire();
    	   
    	   System.out.println("TESTING SLEEP"); 
    	   System.out.println("Test 1:\n...Going to sleep.....\n");
    	   con2.sleep();
    	   System.out.println("Test 1 Complete: Woke up!\n");
    	   lock.release();
    	 }
       
    	});
       
       sleep.fork();
      
		KThread wake =	new KThread(new Runnable()
		{
		//Test 2: Wake
           public void run()
           {
        	   lock.acquire();
        	   System.out.println("TESTING WAKE"); 
               System.out.println("Test 2:\n...Waking a thread...\n");
               con2.wake();      
			   System.out.println("Test 2 Complete: Waking Up!");
			   lock.release();
			} 
		});
		wake.fork();
		sleep.join();
		
		System.out.println("\nTEST 3: SLEEP AND WAKEALL");
		KThread sleep1 =	new KThread(new Runnable()
		{
		//Test 3: Wake All sleeping thread 1
           public void run()
           {
        	   lock.acquire();
               System.out.println("\n...Sleep1 going to sleep...\n");
               con2.sleep();      
				System.out.println("Test 3: Sleep1 waking up!");
				lock.release();
       } } );
		sleep1.fork();
		
		KThread sleep2 =	new KThread(new Runnable()
		{
		//Test 3: Wake All sleeping thead 2
           public void run()
           {
        	   lock.acquire();
               System.out.println("\n...Sleep2 going to sleep...\n");
               con2.sleep();      
				System.out.println("Test 3: Sleep2 waking up!");

				System.out.println("Test 3 Complete: Everyone is awake!");
				lock.release();
       } } );
		sleep2.fork();
		
		
		KThread wakeall =	new KThread(new Runnable()
		{
		//Test 3: Wake all
           public void run()
           {
        	   lock.acquire();
        	   System.out.println("TESTING WAKEALL"); 
               System.out.println("\n...Waking all sleeping threads...\n");
               con2.wakeAll();    
				lock.release();
       } } );
		wakeall.fork();


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
