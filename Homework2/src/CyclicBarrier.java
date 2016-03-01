/*
 * EE360P Homework 2 Question 1: Cyclic Barrier
 * Authors: Matthew Haney and Kelvin Pang
 * UT eids: mah4687 and kkp452
 */

public class CyclicBarrier {
	
	private int parties;
	private int numWaiting;
	public CyclicBarrier(int parties) {
        // Creates a new CyclicBarrier that will trip when
        // the given number of parties (threads) are waiting upon it.
		this.parties = parties;
		this.numWaiting = 0;
	}

	public synchronized int await() throws InterruptedException {
        // Waits until all parties have invoked await on this barrier.
        // If the current thread is not the last to arrive then it is
        // disabled for thread scheduling purposes and lies dormant until
        // the last thread arrives.
        // Returns: the arrival index of the current thread, where index
        // (parties - 1) indicates the first to arrive and zero indicates
        // the last to arrive.
		numWaiting++;
		int index = parties - numWaiting;
		if (index > 0){
			this.wait();
		}
		else {
			numWaiting = 0;
			this.notifyAll();
		}
		return index;
	}
}

