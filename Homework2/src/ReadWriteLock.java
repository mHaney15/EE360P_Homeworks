/*
 * EE360P Homework 2 Question 2: Read Write Lock
 * Authors: Matthew Haney and Kelvin Pang
 * UT eids: mah4687 and kkp452
 */
import java.util.concurrent.Semaphore;

public class ReadWriteLock {
    // This class has to provide the following properties:
    // a. There is no read-write or write-write conflict.
    // b. A writer thread that invokes beginWrite() will be block only when
    //    there is a thread holding the lock.
    // c. A reader thread that invokes beginRead() will be block if either
    //    the lock is held by a writer or there is a waiting writer thread.
    // d. A reader thread cannot be blocked if all preceding writer threads
    //    have acquired and released the lock or no preceding writer thread
    //    exists.

	Semaphore mutex = new Semaphore(1);
	Semaphore wLock = new Semaphore(1);
	int numReaders = 0;	
	
	public void beginRead() {
		while(wLock.hasQueuedThreads()){
			//Do NOTHING
		}
		try {
			mutex.acquire();
			numReaders++;
			if(numReaders == 1){wLock.acquire();}
			mutex.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void endRead() {
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		numReaders--;
		if(numReaders == 0){wLock.release();}
		mutex.release();
	}

	public void beginWrite() {
		try {
			wLock.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void endWrite() {
		wLock.release();
	}
}


