/*
 * EE360P Homework 2 Question 3: Priority Queue
 * Authors: Matthew Haney and Kelvin Pang
 * UT eids: mah4687 and kkp452
 */

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {

	int capacity;
	AtomicInteger size;
	Node head;
	Node tail;
	ReentrantLock addLock;
	ReentrantLock pollLock;
	Condition notFull;
	Condition notEmpty;
	
	private class Node {
		String name;
		int priority;
		Node next;
		ReentrantLock nodeLock;
		
		public Node(String n, int p){
			
			this.name = n;
			this.priority = p;
			next = null;
			nodeLock = new ReentrantLock();
		}
		void lock(){nodeLock.lock();}
		void unlock(){nodeLock.unlock();}
	}
	
	public PriorityQueue(int maxSize) {
        // Creates a Priority queue with maximum allowed size as capacity
		this.capacity = maxSize;
		this.size = new AtomicInteger(0);
		this.head = new Node("dummyHead", 10);
		this.tail = new Node("dummyTail", -1);
		this.head.next = tail;
		this.addLock = new ReentrantLock();
		this.pollLock = new ReentrantLock();
		this.notFull = addLock.newCondition();
		this.notEmpty = pollLock.newCondition();
	}

	public int add(String name, int priority) {
        // Adds the name with its priority to this queue.
        // Returns the current position in the list where the name was inserted;
        // otherwise, returns -1 if the name is already present in the list.
        // This method blocks when the list is full.
		int position;
		addLock.lock();
		try{
			while(size.get() == capacity){
				try{
					notFull.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			size.getAndIncrement();
			if(this.search(name) >= 0){
				size.getAndDecrement();
				notFull.signalAll();
				return -1;
			}
			Node newNode = new Node(name, priority);
			position = 0;
			Node node1 = head;
			node1.lock();
			Node node2 = head.next;
			node2.lock();
			while(node2.priority >= newNode.priority){
				node1.unlock();
				node1 = node2;
				node2 = node1.next;
				node2.lock();
				position++;
			}
			newNode.next = node2;
			node1.next = newNode;
			node1.unlock();
			node2.unlock();
			pollLock.lock();
			notEmpty.signalAll();
			pollLock.unlock();
			if(size.get() < capacity){
				notFull.signalAll();
			}			
		}
		finally {addLock.unlock();}
		return position;
	}
	
	public int search(String name) {
        // Returns the position of the name in the list;
        // otherwise, returns -1 if the name is not found.
		Node node1 = head;
		node1.lock();
		Node node2 = head.next;
		node2.lock();
		int position = 0;
		while(node2.priority >= 0){
			if(node2.name.equals(name)){
				node1.unlock();
				node2.unlock();
				return position;
			}
			node1.unlock();
			node1 = node2;
			node2 = node1.next;
			node2.lock();
			position++;
		}
		node1.unlock();
		node2.unlock();
		return -1;
	}

	public String poll() {
		// Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
		String name;
		pollLock.lock();
		try{
			while(size.get() == 0){
				try {
					notEmpty.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
			size.getAndDecrement();
			head.lock();
			Node pollNode = head.next;
			pollNode.lock();
			head.next = pollNode.next;
			head.unlock();
			pollNode.unlock();
			addLock.lock();
			notFull.signalAll();
			addLock.unlock();
			if(size.get() > 0){
				notEmpty.signalAll();
			}
			name = pollNode.name;
		}
		finally{pollLock.unlock();}
		return name;
	}
}

