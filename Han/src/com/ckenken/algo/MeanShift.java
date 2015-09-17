package com.ckenken.algo;

import com.ckenken.storage.Coarse_pattern;
import com.ckenken.storage.Vector;

public class MeanShift {
	
	public static final double RADIUS = 0.005;
	
	public static void meanShiftClustering(Coarse_pattern cp)
	{
		if(cp.snippet_sets.size() <= 1) {
			
			for(int i = 0; i<cp.snippet_sets.size(); i++) {
				cp.snippet_sets.get(i).shiftId = 0;
			}
			return;
		}
		
		Vector start = new Vector(cp.snippet_sets.get(0));
		
		double sum = 0.0;
		
		for(int i = 0; i<cp.snippet_sets.size(); i++) {
			double dist = Vector.vectorDistance(start, new Vector(cp.snippet_sets.get(i)));
			sum += dist;
			System.out.println("dist = " + dist);
		}
		System.out.println("sum = " + sum);
		
	}
}
