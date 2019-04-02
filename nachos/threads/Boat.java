package nachos.threads;
import nachos.machine.*;
import java.util.LinkedList;
import java.util.Vector;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    
    public static void selfTest()
    {
		BoatGrader b = new BoatGrader();
		//begin(0, 2, b);
		//begin(1, 2, b);
		//begin(3, 3, b);
		begin(120, 120, b);
    }

	public static void init()
	{
		lock = new Lock(); 
		CoahuA = new Condition(lock);
		CoahuC = new Condition(lock);
		CmoloC = new Condition(lock);
		parentT = new Condition(lock);
		wait_second = new Condition(lock);
		moloC = 0;
		moloA = 0;
		oahuA = 0;
		oahuC = 0;
		RmoloC = 0;
		RoahuA = 0;
		RoahuC = 0;
		posB = "Oahu";
		finish = false;
		waited_sb = false;
		wait_two_children = false;
		first_child = false;
		second_child = false;
		waitC = new LinkedList<KThread>();
	}

    public static void begin( int adults, int children, BoatGrader b )
    {
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		// Instantiate global variables here
		
		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.
		/*
		// Old One.
		Runnable r = new Runnable() {
			public void run() {
					SampleItinerary();
				}
			};
			KThread t = new KThread(r);
			t.setName("Sample Boat Thread");
			t.fork();
		*/
		System.out.println("\n---------------Testing Boats with with " + children  + " children(s), " + adults + " adult(s).---------------");
        Boat.adults = adults;
		Boat.children = children;
		init();
		Vector<KThread> test_list =  new Vector<KThread>();
		for(int i = 1; i <= adults; i++)
		{
			//System.out.println("Adult " + i);
			Runnable r = new Runnable() { public void run() { AdultItinerary(); } };
			KThread t = new KThread(r);
			t.setName("Adult on Oahu");
			t.fork();
			test_list.add(t);
		}
		for(int i = 1; i <= children; i++)
		{
			//System.out.println("Child " + i);
			Runnable r = new Runnable() { public void run() { ChildItinerary(); } };
			KThread t = new KThread(r);
			t.setName("Child on Oahu");
			t.fork(); 
			test_list.add(t);
		}
		//System.out.println("\n ***Testing Boats with only 2 children***");
		lock.acquire();
		System.out.println("All_people: " + adults + " " + children);
		//System.out.println("Molokai: " + moloA + " " + moloC + "/Oahu: " + oahuA + " " + oahuC + "/Local info: moloC:" + RmoloC + " oahu:" + RoahuA + " " + RoahuC);
		while(!game_over())
		{
			parentT.sleep();
			System.out.println("Molokai: " + moloA + " " + moloC + "/Oahu: " + oahuA + " " + oahuC + "/Local info: moloC:" + RmoloC + " oahu:" + RoahuA + " " + RoahuC);
		} 
		finish = true;
		System.out.println("Finish and begin to close all threads");
		// Close all thread
		/*
		for (int i = 0; i < test_list.size(); i++)
			System.out.println(test_list.get(i).getName());
		*/
		CoahuA.wakeAll();
		CoahuC.wakeAll();
		CmoloC.wakeAll();
		lock.release();
		int maxlen = (int)(0.01 * (adults + children));
		if (maxlen < 1)
			maxlen = 1;
		ThreadedKernel.alarm.waitUntil(maxlen);
    }

    public static Boolean game_over() { return adults == moloA && children == moloC && posB.equals("Molokai"); }
    public static Boolean local_game_over() { return RoahuA == 0 && RoahuC == 0 && posB.equals("Molokai"); }
	
	private static Boolean StartCond_C()
	{
		return (posB.equals("Oahu") && KThread.currentThread().getName().equals("Child on Oahu") && waitC.size() < 2) ||
			   (posB.equals("Molokai") && KThread.currentThread().getName().equals("Child on Molokai"));
	}

	private static Boolean StartCond_A()
	{
		return posB.equals("Oahu") && KThread.currentThread().getName().equals("Adult on Oahu") && RmoloC > 0 && !wait_two_children;
	}

    static void AdultItinerary()
    {
		//System.out.println("Begin");
		bg.initializeAdult(); //Required for autograder interface. Must be the first thing called.
		//DO NOT PUT ANYTHING ABOVE THIS LINE. 

		/* This is where you should put your solutions. Make calls
		to the BoatGrader to show that it is synchronized. For
		example:
			bg.AdultRowToMolokai();
		indicates that an adult has rowed the boat across to Molokai
		*/
		lock.acquire();
		oahuA++;
		while (!StartCond_A())
		{
			if (posB.equals("Oahu")) CoahuC.wake();
			else CmoloC.wake();
            CoahuA.sleep();
		}
		transportA(posB); //posB == "Oahu"
        CmoloC.wake(); //Children need to go back to ohua.
		lock.release();
	}

    static void ChildItinerary()
    {
		//if (num > 1)
		//	System.exit(0);
		//System.out.println("Begin");
		//System.out.flush();
		//System.exit(0);
		bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
		//DO NOT PUT ANYTHING ABOVE THIS LINE. 
		lock.acquire();
		//System.out.println(lock.isHeldByCurrentThread());
		///System.out.flush();
		oahuC++;
        while (!finish)
        {
            while (!StartCond_C())
            {
                if (posB.equals("Oahu"))
                    CoahuC.wake();
                else
                    CmoloC.wake();
				CoahuA.wake(); // Adult can get lock before children when the boat on oahu, or after children drive back to oahu.
				//System.out.println(KThread.currentThread().getName());
                Lib.assertTrue(KThread.currentThread().getName().equals("Child on Oahu") || KThread.currentThread().getName().equals("Child on Molokai"));
                if (KThread.currentThread().getName().equals("Child on Oahu"))
                    CoahuC.sleep();
                else if (KThread.currentThread().getName().equals("Child on Molokai"))
                    CmoloC.sleep();
            }
            if (finish) break;
            transportC(posB);
            if (local_game_over())
            {
                parentT.wake(); //Check if game is over.
                CoahuA.wake(); // Not over, check both side
                CmoloC.sleep(); 
            }
            else
            {
                if (first_child)
                {
                    CoahuC.wake();
                    first_child = false;
                    second_child = true;
                    wait_second.sleep();
                }
                else 
                {
                    if (posB.equals("Oahu"))
                    {
                        CoahuA.wake();
                        CoahuC.wake();
                        CoahuC.sleep();
                    }
                    else
                    {
                        if (second_child)
                        {
                            wait_second.wake();
                            second_child = false;
                        }
                        CmoloC.wake();
                        CoahuA.wake();
                        CmoloC.sleep();
                    }
                }
            }
        }
        lock.release();
	}
	//moloWaitChildren;

    //All transports are done when having lock.
	public static void transportA(String pos) //Transport Adult
	{
        Lib.assertTrue(pos.equals("Oahu"));
		if (pos.equals("Oahu"))
		{
			oahuA--;
            //Only Children need the information at the other side.
			RoahuA = oahuA;
			RoahuC = oahuC;
			bg.AdultRowToMolokai();
			moloA++;
			posB = "Molokai"; 
			KThread.currentThread().setName("Adult on Molokai");
		}
	}

	public static void transportC(String pos) //Transport Children
	{
		if (pos.equals("Oahu"))
		{
			if (!wait_two_children)
            {
				oahuC--;
                RoahuA = oahuA;
                RoahuC = oahuC;
				bg.ChildRowToMolokai();
				KThread.currentThread().setName("Child on Boat");
				wait_two_children = true;
                first_child = true;
				waitC.add(KThread.currentThread());
			}
            else
            { 
				oahuC--;
                RoahuA = oahuA;
                RoahuC = oahuC;
				bg.ChildRideToMolokai();
				posB = "Molokai";
				KThread.currentThread().setName("Child on Molokai");
				KThread kidFirst = waitC.removeFirst();
				kidFirst.setName("Child on Molokai");
				moloC = moloC + 2;
				//System.out.println("Molo Child: " + moloC);
				wait_two_children = false;
			}
		}
        else
        { 
			moloC--;
            RmoloC = moloC;
			bg.ChildRowToOahu();
			oahuC++;
			//System.out.println("Molo Child: " + moloC);
			KThread.currentThread().setName("Child on Oahu");
			posB = "Oahu";
		}
	}

	static void SampleItinerary()
	{
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
    }    
	private static Lock lock;
	private static Condition CoahuA, CoahuC, CmoloC, parentT, wait_second;
	private static int moloC, moloA, oahuA, oahuC, RmoloC, RoahuA, RoahuC, adults, children;
	private static String posB;
	private static Boolean finish, waited_sb, wait_two_children, first_child, second_child;
	private static LinkedList<KThread> waitC;
}
