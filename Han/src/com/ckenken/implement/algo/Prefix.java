package com.ckenken.implement.algo;

import java.util.ArrayList;

import lab.adsl.object.Point;

import com.ckenken.Main.Main_v2;
import com.ckenken.implement.storage.Coarse_data_pattern;
import com.ckenken.implement.storage.DataPoint;
import com.ckenken.implement.storage.DataSequence;
import com.ckenken.storage.Sequence;
import com.ckenken.storage.Snippet;

public class Prefix {
	
	public static ArrayList<DataSequence> extended = new ArrayList<DataSequence>();
	
	public static ArrayList<DataSequence> extendByDataSequence(ArrayList<DataSequence> origin, ArrayList<Integer> symbol_sequence, int dt)
	{
		for(int i = 0; i<origin.size(); i++) {
			DataSequence seq = origin.get(i);
			for(int j = 0; j<seq.dataPoints.size(); j++)
			if(seq.dataPoints.get(j).symbol == symbol_sequence.get(0)) {
				
				ArrayList<DataPoint> st = new ArrayList<DataPoint>();
				
				st.add(seq.dataPoints.get(j));
				if(symbol_sequence.size()>1) {
					extendRecursive(seq, symbol_sequence, st, j, 1, dt);
				}
				else {
					DataSequence tempSubDataSequence = new DataSequence();
					tempSubDataSequence.dataPoints = seq.getSubDataSequence(j);
					
					for(int k = st.size()-1; k>=0; k--) {
						tempSubDataSequence.dataPoints.add(0, st.get(k));
					}
					extended.add(tempSubDataSequence);
				} 
					
				st.remove(0);
			}
		}
		return Prefix.extended;
	}	
	
	public static void extendRecursive(DataSequence seq, ArrayList<Integer> symbol_sequence, ArrayList<DataPoint> stack, int now, int symbol_index, int dt) {
		for(int i = now; i< seq.dataPoints.size(); i++) {
			DataPoint p = seq.dataPoints.get(i);
			
			if(p.symbol == symbol_sequence.get(symbol_index) && Math.abs(p.startTime.getTime() - stack.get(stack.size()-1).endTime.getTime()) < dt) { //符合條件的點
				if(symbol_index == symbol_sequence.size() - 1) { // last point , output one extended
					
					DataSequence tempSubDataSequence = new DataSequence();
					tempSubDataSequence.dataPoints = seq.getSubDataSequence(i);
					
					for(int j = stack.size()-1; j>=0; j--) {
						tempSubDataSequence.dataPoints.add(0, stack.get(j));
					}
					extended.add(tempSubDataSequence);
				}
				else  // not last one
				{
					stack.add(p);
					extendRecursive(seq, symbol_sequence, stack, i, symbol_index+1, dt);
					stack.remove(stack.size()-1);
				}
			}
		}
	}
	
	public static Coarse_data_pattern testDataCoarse(ArrayList<DataSequence> objects_extended, int length, int symbol_1, int symbol_2, int dt, int sigma) 
	{
		Coarse_data_pattern new_coarse_data_pattern = new Coarse_data_pattern();
		
		for(int i = 0; i<length-1; i++) { // 最前面兩(length-1)個點一定是 (e.g. "g1->g2"->g3)
			new_coarse_data_pattern.symbol_sequence.add(objects_extended.get(0).dataPoints.get(i).symbol);
		}
		new_coarse_data_pattern.symbol_sequence.add(symbol_2);
		
		int counter = 0;
		for(int i = 0; i<objects_extended.size(); i++) {
			
			DataSequence o = objects_extended.get(i);
			
			if(o.dataPoints.size() < length) // length k(3, 4, 5, ...) up will be considered
				continue;
			
			long start = (o.dataPoints.get(length-2).endTime.getTime());
//			if(length >=3) {
//				for(int j = 0; j<new_coarse_data_pattern.symbol_sequence.size(); j++) {
//					System.out.print(new_coarse_data_pattern.symbol_sequence.get(j) + ",");
//				}
//				System.out.println("kth = " + o.dataPoints.get(length-2).symbol);
//				System.out.println("stmbol_1 = " + symbol_1);
//			}
			for(int j = length-1; j<o.dataPoints.size(); j++) {
			
				if(o.dataPoints.get(j).symbol == symbol_1) {
					continue;
				}
				
				if(Math.abs(o.dataPoints.get(j).startTime.getTime() - start) <= dt && o.dataPoints.get(j).symbol == symbol_2) { 

					counter++;
//					// 先假設這是ＯＫ的，把它放進去 (g1->g2->g3)
//					ArrayList<DataPoint> test_points = new ArrayList<DataPoint>();
//					for(int k = 0; k<length-1; k++) {
//						test_points.add(o.dataPoints.get(k));
//					}
//					test_points.add(o.dataPoints.get(j));
//					
//					if(new_coarse_data_pattern.symbol_sequence.size() == 0)
//						new_coarse_data_pattern.symbol_sequence = test_points;

				}
			}
		}
		
		new_coarse_data_pattern.frequent = counter;
		
		if(counter >= sigma)
			return new_coarse_data_pattern;
		else 
			return null;
	}
	
}
