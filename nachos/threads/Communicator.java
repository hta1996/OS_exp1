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
    private int mess;
    private cntL=0;
}
