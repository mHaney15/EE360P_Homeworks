/*
 * EE360P Homework 1 Question 1: Parrallel Sort
 * Authors: Matthew Haney and Kelvin Pang
 * UT eids: mah4687 and kkp452
 */

public class PSort implements Runnable{
	
	int Array[];
	int strt;
	int end;
	
	private PSort(int Array[], int strt, int end){
		this.Array = Array;
		this.strt = strt;
		this.end = end;
	}
	
	public static void parallelSort(int[] A, int begin, int end){		
		if(end - begin < 2){return;} //No need to sort an empty or single element array!
		int partition = partition(A, begin, end);
		PSort p1 = new PSort(A, begin, partition); //create a Runnable for each half
		PSort p2 = new PSort(A, partition, end);
		Thread t1 = new Thread(p1); //start Runnables on concurrent threads
		Thread t2 = new Thread(p2);
		t1.start();
		t2.start();
		try {
			t1.join(); //wait for the threads to finish sorting
			t2.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		QuickSort(Array, strt, end); //each thread performs a recursive sort on an array.
	}

	void QuickSort(int A[], int s, int e){
		if( e-s < 2){return;}
		int partition = partition(A,s,e);
		QuickSort(A,s,partition);
		QuickSort(A,partition,e);		
	}
	
		static int partition(int Arry[], int strt, int end){
		if(Arry.length < 2){return 1;}
		int i = strt;
		int j = end - 1;
		int p = strt + (end - strt)/2;
		while(i < j){
			while(Arry[i] <= Arry[p] && i < p){i++;}
			int temp = Arry[i];
			Arry[i] = Arry[p];
			Arry[p] = temp;
			p = i;
			while(Arry[j] >= Arry[p] && j > p){j--;}
			temp = Arry[j];
			Arry[j] = Arry[p];
			Arry[p] = temp;
			p = j;
		}
		return p;
	}
	
}
