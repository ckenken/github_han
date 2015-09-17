package com.ckenken.storage;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.ckenken.algo.MeanShift;
import com.ckenken.io.JDBC;

public class Coarse_pattern {
	public static int max_shift_id = 0;
	public static int max_pattern_id = 0;
	public static int max_fine_id = 0;
	public int pattern_id;
	public ArrayList<Snippet> snippet_sets;
	public ArrayList<Integer> G_sequence_ids;
	
	public Coarse_pattern()
	{
		snippet_sets = new ArrayList<Snippet>();
		G_sequence_ids = new ArrayList<Integer>();
	}
	
	public void show()
	{
		
		for(int i = 0; i<G_sequence_ids.size(); i++)
		{
			if(i != G_sequence_ids.size() - 1)
				System.out.print(G_sequence_ids.get(i) + "->");
			else 
				System.out.println(G_sequence_ids.get(i));
		}
		
	//	System.out.println(G_sequence_ids.get(0) + "->" + G_sequence_ids.get(1) + " " + snippet_sets.get(0).weight);
		
		for(int i = 0; i<snippet_sets.size(); i++) {
			//if (snippet_sets.get(i).weight > 6) {
				snippet_sets.get(i).s.show();
				System.out.println(" " + snippet_sets.get(i).weight + ", sid = " + snippet_sets.get(i).shiftId);
		//	}
		}
	}
	
	public void meanShiftClustering()
	{
		if(this.snippet_sets.size() <= 1) {
			
			for(int i = 0; i<this.snippet_sets.size(); i++) {
				this.snippet_sets.get(i).shiftId = Coarse_pattern.max_shift_id++;
			}
			return;
		}

		for(int i = 0; i<this.snippet_sets.size(); i++) {
			if(this.snippet_sets.get(i).shiftId == -1) {
				Vector start = new Vector(this.snippet_sets.get(i));
	//			System.out.println("start point:");
	//			start.show();
				meanShiftRecursive(start);
			}
			
		}
	}
	
	private int meanShiftRecursive(Vector center)
	{
//		System.out.println("center:");
//		center.show();
		Vector move = new Vector();
		
		for(int i = 0; i<this.snippet_sets.get(0).s.points.size(); i++) {
			move.v.add(0.0);
			move.v.add(0.0);
		}
		
		for(int i = 0; i<this.snippet_sets.size(); i++) {
			if(this.snippet_sets.get(i).shiftId == -1) {  // 尚未分類的 snippet
				Vector temp = new Vector(this.snippet_sets.get(i));
				
				double dist = Vector.vectorDistance(center, temp);
		//		System.out.println("distance1: " + dist);
				if(dist <= MeanShift.RADIUS) {  // 在半徑內，可以影響移動 kk
					int w = this.snippet_sets.get(i).weight;
					
					for(int j = 0; j<temp.v.size(); j++) {
						move.v.set(j, move.v.get(j) + Math.pow(((temp.v.get(j) - center.v.get(j)) * w), 3)); 
					}
//					move.show();
				} 
			}
		}
		
		double checksum = 0.0;
		
	//	System.out.println("sum of move:");	
	//	move.show();
		
		for(int i = 0; i<move.v.size(); i++) {
			checksum += move.v.get(i);
		}
		if(Math.abs(checksum) <= 0.000001) { //收斂，要把所有距離 center 半徑內且 shiftId == -1 的 snippet 都分到這個 cluster 
			for(int i = 0; i<this.snippet_sets.size(); i++) {
				if(this.snippet_sets.get(i).shiftId == -1) {  // 已經分類的不再處理，只處理沒被分類的  kk
					Vector temp = new Vector(this.snippet_sets.get(i));
			//		System.out.println("distance = " + Vector.vectorDistance(center, temp));
					if(Vector.vectorDistance(center, temp) <= MeanShift.RADIUS) {
						this.snippet_sets.get(i).shiftId = Coarse_pattern.max_shift_id;
					}
				}
			}
			Coarse_pattern.max_shift_id++;
			return 0;
		}
		else 
		{
			center = Vector.addVector(center, move);
			return meanShiftRecursive(center);
		}
	}
	
	public void insertCoarse()
	{
		StringBuilder SB = new StringBuilder();
		
		int sum = 0;
		for(int i = 0; i<snippet_sets.size(); i++) {
			sum += snippet_sets.get(i).weight;
		}
		
		for(int i = 0; i<G_sequence_ids.size(); i++) {
			if(i == 0)
				SB.append(G_sequence_ids.get(i).toString());
			else 
				SB.append("," + G_sequence_ids.get(i).toString());
		}
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "insert into coarse values(" + max_pattern_id + ",'" + SB.toString() + "', +" + Integer.toString(sum) + "," + G_sequence_ids.size() + ")";
		
		jdbc.insertQuery(sql);
		
		for(int i = 0; i<snippet_sets.size(); i++) {
			SB.delete(0, SB.length());
			Snippet sni = snippet_sets.get(i);
			for(int j = 0; j<sni.s.points.size(); j++) {
				if(j == 0)
					SB.append(Integer.toString((int)sni.s.points.get(j).id));
				else 
					SB.append("," + Integer.toString((int)sni.s.points.get(j).id));
			}
			sql = "insert into fine values(" + max_fine_id + ",'" + SB.toString() + "'," + sni.weight + "," + sni.shiftId + "," + max_pattern_id +")";
			max_fine_id++;
			jdbc.insertQuery(sql);
		}		
		
		max_pattern_id++;
	}	
}
