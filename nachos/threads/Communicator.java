
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
		flag=new Condition2(lock);
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
		while(mess!=null)S.sleep();
		mess=word;
		L.wake();
		flag.sleep();
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
    	while(mess==null)L.sleep();
    	int tp=mess;
    	mess=null;
    	S.wake();
    	flag.wake();
    	lock.release();
		return tp;
    }
    private Lock lock;
    private Condition2 S,L,flag;
    private Integer mess;
    
	protected static class ComTest implements Runnable
	{
		private int num;
		private static Communicator Com=new Communicator();
		
		ComTest(int num){this.num = num;}
		public void run() 
		{
			//test1
		    if(num>0)
		    {
		    	for(int i=0;i<4;i++)
		    	{
		            System.out.println("Thread "+num+" speak "+i);
		            Com.speak(i);
		        }
		    }else
		    {
		    	for(int i=0;i<12;i++)
		    	{
		            System.out.println("Thread "+num+ " listening "+i);
		            int word=Com.listen();
		            System.out.println("Thread "+num+" heard "+word);
		        }
		    }
		    if(num==0)
		    	System.out.println("Test 1 finished");
		    ThreadedKernel.alarm.waitUntil(4096);
		    
		    //test2
		    if(num==0)
		    {
		        for(int i=0;i<12;i++)
		        {
		            System.out.println("Thread "+num+" speak "+i);
		            Com.speak(i);
		        }
		    } else
		    {
		        for(int i=0;i<4;i++)
		        {
		            System.out.println("Thread "+num+" listening "+i);
		            int word=Com.listen();
		            System.out.println("Thread "+num+" heard "+word);
		        }
		    }
		    
		    if(num==0)
		    	System.out.println("Test 1 finished");
		    ThreadedKernel.alarm.waitUntil(4096);
		}
	}

	public static void selfTest() {
		KThread thread1 = new KThread(new ComTest(1));
		KThread thread2 = new KThread(new ComTest(2));
		KThread thread3 = new KThread(new ComTest(3));
		thread1.fork();
		thread2.fork();
		thread3.fork();
		new ComTest(0).run();
	}
}


