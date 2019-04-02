package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator()
	{
		lock=new Lock();
		S=new Condition2(lock);
		L=new Condition2(lock);
		sending=false;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word)
	{
		lock.acquire();
		while(cntL==0||sending==true)L.sleep();
		mess=word;
		sending=true;
		S.wake();
		cntL--;
		lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() 
    {
    	lock.acquire();
    	while(sending==false)
    	{
    		L.wake();
    		cntL++;
    		S.sleep();
    	}
    	int tp=mess;
    	sending=false;
    	L.wake();
    	lock.release();
		return tp;
    }
    private Lock lock;
    private Condition2 S,L;
    private boolean sending;
    private int mess,cntL=0;
    public static void selfTest()
	{
		KThread t1 = new KThread(new Comm(1));
		KThread t2 = new KThread(new Comm(2));
		KThread t3 = new KThread(new Comm(3));
		KThread t4 = new KThread(new Comm(4));
		t1.fork();
		t2.fork();
		t3.fork();
		t4.fork();	
		//run the test
		System.out.println("-----Communicator Test---------");
		new Comm(0).run();
	}
		
		
	protected static class Comm implements Runnable
	{
		private int comID;
		private static Communicator comm=new Communicator();
	 
	 // Construct the object. Pass the comID of the thread plus any variables you
	 // want to share between threads. You may want to pass a KThread as a global
	 // variable to test join.
		Comm(int comID) {this.comID = comID;}
		

		public void run() 
		{
		    // Use an if statement to make the different threads execute different
		    // code.
		    if(comID==0) 
		    {
		        for(int i=0;i<4;i++) 
		        {
		            System.out.println("ComTest "+comID+" Speak("+i+")");
		            comm.speak(i);
		        }
		    }
		    else
		    {
		        for(int i=0;i<4;i++) 
		        {
		            System.out.println("ComTest "+comID+" listening to... "+i);
		            int word=comm.listen();
		            System.out.println("ComTest "+comID+" heard word "+word);
		        }
		    }		    
		    if (comID==0)
		    	System.out.println("-----Communicator Test Complete-------");
		    ThreadedKernel.alarm.waitUntil(2000);
		}
	}
}
