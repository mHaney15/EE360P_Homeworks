/*
 * EE360P Homework 1 Question 2: Parrallel Search
 * Authors: Matthew Haney and Kelvin Pang
 * UT eids: mah4687 and kkp452
 */

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.lang.Math;

public class PSearch implements Callable<Integer>{
	int k;
	int Array[];
	int begin;
	int end;
	Integer result;
	private PSearch(int k, int Array[], int begin, int end){
		this.k = k;
		this.Array = Array;
		this.begin = begin;
		this.end = end;
	}
	
	
	public static int parallelSearch(int k, int[] A, int numThreads){
    if(A.length == 0){return -1;}
    else if(A.length == 1){
    	if(A[0] == k){return 0;}
    	else return -1;
    }
    ArrayList<Future<Integer>> ThreadArray = new ArrayList<Future<Integer>>();
    ExecutorService pool = Executors.newFixedThreadPool(numThreads);
    int searchSize = (int)(Math.ceil((double)A.length/(numThreads)));
    int startIndex = 0;
    int endIndex = searchSize - 1;
    try{
	    for(int i = 0; i < numThreads; i++){
	    	ThreadArray.add(pool.submit(new PSearch(k, A, startIndex, endIndex)));
			startIndex += searchSize;
			endIndex = Integer.min(endIndex + searchSize, A.length - 1);
	    }
	    for(int i = 0; i < ThreadArray.size(); i++){
	    	int x = ThreadArray.get(i).get();
	    	if(x != -1){return x;}
	    }
    } catch(Exception e){
    	e.printStackTrace();
    }
    return -1;
  }

@Override
public Integer call() throws Exception {
	for(int i = begin; i <= end; i++){
		if(Array[i] == k){return i;}
	}
	return -1;
}
}
