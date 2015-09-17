package com.ckenken.algo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import lab.adsl.object.Point;

import com.ckenken.io.JDBC;
import com.ckenken.storage.Coarse_pattern;
import com.ckenken.storage.NewPoint;
import com.ckenken.storage.Sequence;
import com.ckenken.storage.Snippet;

public class Coarse {
	public static int max_g_num;
	public static ArrayList<Sequence> extended = new ArrayList<Sequence>();
	
	public Coarse()
	{
		extended = new ArrayList<Sequence>();
	}
	
	public Coarse(int input)
	{
		max_g_num = input;
	}
	// extend sequences by g1 (or any index)
	public static ArrayList<Sequence> extend(ArrayList<Sequence> origin, int g_index)
	{
		ArrayList<Sequence> temp = new ArrayList<Sequence>();
		
		for(int i = 0; i<origin.size(); i++)
		{
			for(int j = 0; j<origin.get(i).points.size(); j++) 
			{
				if(origin.get(i).points.get(j).Gid == g_index)
				{
					Sequence tempSubSequence = new Sequence();
					tempSubSequence.objectID = origin.get(i).objectID;
					tempSubSequence.points = origin.get(i).getSubSequence(j);
					temp.add(tempSubSequence);
				}
			}
		}
		
		return temp;
	}
	
	// test for coarse which length = 2, dt = 60, sigma = 3
	public static Coarse_pattern testCoarse(ArrayList<Sequence> objects_extended, int g_index1, int g_index2, int dt, int sigma)
	{
		Coarse_pattern new_coarse_pattern = new Coarse_pattern();
	//	new_coarse_pattern.pattern_id = Coarse_pattern.max_pattern_id;
	//	Coarse_pattern.max_pattern_id++;
		new_coarse_pattern.G_sequence_ids.add(g_index1);
		new_coarse_pattern.G_sequence_ids.add(g_index2);
		
		int counter = 0;
		for(int i = 0; i<objects_extended.size(); i++) {
			Sequence o = objects_extended.get(i);
			long start = (o.points.get(0).ckTime.getTime());
			for(int j = 1; j<o.points.size(); j++) {
			
				if(o.points.get(j).Gid == g_index1) {
					continue;
				}
				if(Math.abs(o.points.get(j).ckTime.getTime() - start) <= dt && o.points.get(j).Gid == g_index2) { 
 			//	if(o.points.get(j).Gid == g_index2) { 
					counter++;
					
					int index = check_exist_snippet(new_coarse_pattern.snippet_sets, o.points.get(0), o.points.get(j));
					
					if(index == -1) // not existed
					{
						if(o.points.get(j).id==28) {
							
							System.out.println("");
							
						}
						
						
						ArrayList<Point> temp_points = new ArrayList<Point>();
						temp_points.add(o.points.get(0));
						temp_points.add(o.points.get(j));	
						Sequence tempSequence = new Sequence(temp_points);
						
						ArrayList<Integer> object_ids = new ArrayList<Integer>();
						object_ids.add(o.objectID);
						
						Snippet tempSnippet = new Snippet(tempSequence, object_ids);
						tempSnippet.shiftId = -1;
						new_coarse_pattern.snippet_sets.add(tempSnippet);
						
						break;	
					}
					else // existed 
					{
				//		new_coarse_pattern.snippet_sets.get(index).weight++;
						if(check_exist_object(new_coarse_pattern.snippet_sets.get(index).object_ids, o.objectID)) {
				//			System.out.println("already existed object!");
						}
						else {
							new_coarse_pattern.snippet_sets.get(index).weight++;
							new_coarse_pattern.snippet_sets.get(index).object_ids.add(o.objectID);
						}
						break;
					}
				}
			}
		}
		
		if(counter >= sigma)
			return new_coarse_pattern;
		else 
			return null;
	}
	
	public static int check_exist_snippet(ArrayList<Snippet> sni_sets, Point p1, Point p2)
	{
		int counter = 0;
//		boolean flag = false;
		int index = 0;
		
		for(int i = 0; i<sni_sets.size(); i++) {
//			for(int j = 0; j<sni_sets.get(i).s.points.size(); j++) {
//				if(sni_sets.get(i).s.points.get(j).equals(p1) && flag == false) {
//					counter++;
//				}
//				else if (sni_sets.get(i).s.points.get(j).equals(p2)) {
//					counter++;
//					flag = true;
//				}
//			}
//			if(counter == 2) {
//				index = i;
//				break;
//			}
//			counter = 0;
//			flag = false;

			if(sni_sets.get(i).s.points.get(0).id == p1.id && sni_sets.get(i).s.points.get(1).id == p2.id)
			{
				counter = 2;
				index = i;
				break;
			}
		}
		
		if(counter == 2)
			return index;
		else 
			return -1;
	}
	
	public static boolean check_exist_object(ArrayList<Integer> obj_ids, int id) 
	{	
		boolean flag = false;
		for(int i = 0; i<obj_ids.size(); i++) {
			if(obj_ids.get(i).intValue() == id)
			{
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	public static NewPoint getSameCenterById(int sameId, String DB_name) throws SQLException
	{
		System.out.println("same = " + sameId);
		
		JDBC jdbc = new JDBC(DB_name);
		
		String sql = "select * from same where sameid=" + sameId;
		
		ResultSet rs = jdbc.query(sql);
		
		rs.next();
		double lat = rs.getDouble("lat");
		double lng = rs.getDouble("lng");
		String cate = rs.getString("cate");
		
		NewPoint p = new NewPoint(new Date(), lat, lng, cate);
		
		return p;
	}
	
	
	// extend sequences by g1 (or any index)
	public static ArrayList<Sequence> extendBySequence(ArrayList<Sequence> origin, ArrayList<Integer> g_sequence, int dt)
	{
		for(int i = 0; i<origin.size(); i++) {
			Sequence seq = origin.get(i);
			for(int j = 0; j<seq.points.size(); j++)
			if(seq.points.get(j).Gid == g_sequence.get(0)) {
			
				ArrayList<Point> st = new ArrayList<Point>();
				
				st.add(seq.points.get(j));
				extendRecursive(seq, g_sequence, st, j, 1, dt, seq.objectID);
				st.remove(0);
			}
		}
		return  Coarse.extended;
	}	
	
	
	public static void extendRecursive(Sequence seq, ArrayList<Integer> g_sequence, ArrayList<Point> stack, int now, int g_sequence_index, int dt,int objectID) {
		for(int i = now; i< seq.points.size(); i++) {
			Point p = seq.points.get(i);
			
			if(p.Gid == g_sequence.get(g_sequence_index) && Math.abs(p.ckTime.getTime() - stack.get(stack.size()-1).ckTime.getTime()) < dt) { //符合條件的點
				if(g_sequence_index == g_sequence.size() - 1) { // 這是最後一個要 match 的點了，要output一個extended
					
					Sequence tempSubSequence = new Sequence();
					tempSubSequence.points = seq.getSubSequence(i);
					
					for(int j = stack.size()-1; j>=0; j--) {
						tempSubSequence.points.add(0, stack.get(j));
					}
					tempSubSequence.objectID = objectID;
					extended.add(tempSubSequence);
				}
				else  //不是最後一個，要遞迴呼叫找下一個
				{
					stack.add(p);
					extendRecursive(seq, g_sequence, stack, i, g_sequence_index+1, dt, objectID);
					stack.remove(stack.size()-1);
				}
			}
		}
	}
	
	// test for coarse which length >= 3, dt = 60, sigma = 3
	public static Coarse_pattern testCoarseExtendLength(ArrayList<Sequence> objects_extended, int length, int g_index1, int g_index2, int dt, int sigma)
	{
		Coarse_pattern new_coarse_pattern = new Coarse_pattern();
	//	new_coarse_pattern.pattern_id = Coarse_pattern.max_pattern_id;
	//	Coarse_pattern.max_pattern_id++;
		
		for(int i = 0; i<length-1; i++) { // 最前面兩(length-1)個點一定是 (e.g. "g1->g2"->g3)
			new_coarse_pattern.G_sequence_ids.add(objects_extended.get(0).points.get(i).Gid);
		}
		new_coarse_pattern.G_sequence_ids.add(g_index2);
		
		int counter = 0;
		for(int i = 0; i<objects_extended.size(); i++) {
			
			Sequence o = objects_extended.get(i);
			
			if(o.points.size() < length) // length k(3, 4, 5, ...) up will be considered
				continue;
			
			long start = (o.points.get(length-2).ckTime.getTime());
			for(int j = length-1; j<o.points.size(); j++) {
			
				if(o.points.get(j).Gid == g_index1) {
					continue;
				}
				if(Math.abs(o.points.get(j).ckTime.getTime() - start) <= dt && o.points.get(j).Gid == g_index2) { 
 			//	if(o.points.get(j).Gid == g_index2) { 
					counter++;
					// 先假設這是ＯＫ的，把它放進去 (g1->g2->g3)
					ArrayList<Point> test_points = new ArrayList<Point>();
					for(int k = 0; k<length-1; k++) {
						test_points.add(o.points.get(k));
					}
					test_points.add(o.points.get(j));
					
					int index = check_exist_snippet_v2(new_coarse_pattern.snippet_sets, test_points);
					
					if(index == -1) // not existed
					{
//						ArrayList<Point> temp_points = new ArrayList<Point>();
//						temp_points.add(o.points.get(0));
//						temp_points.add(o.points.get(j));	
						Sequence tempSequence = new Sequence(test_points);
						
						ArrayList<Integer> object_ids = new ArrayList<Integer>();
						object_ids.add(o.objectID);
						
						Snippet tempSnippet = new Snippet(tempSequence, object_ids);
						tempSnippet.shiftId = -1;
						new_coarse_pattern.snippet_sets.add(tempSnippet);
						
						break;	
					}
					else // existed 
					{
					//	new_coarse_pattern.snippet_sets.get(index).weight++;
						if(check_exist_object(new_coarse_pattern.snippet_sets.get(index).object_ids, o.objectID)) {
					//		System.out.println("already existed object!");
						}
						else {
							new_coarse_pattern.snippet_sets.get(index).object_ids.add(o.objectID);
							new_coarse_pattern.snippet_sets.get(index).weight++;
						}
						break;
					}
				}
			}
		}
		
		if(counter >= sigma)
			return new_coarse_pattern;
		else 
			return null;
	}
	
	public static int check_exist_snippet_v2(ArrayList<Snippet> sni_sets, ArrayList<Point> test_points)
	{
		boolean flag = true;
		int index = 0;
		
		if(sni_sets.size() == 0)
			return -1;
		
		for(int i = 0; i<sni_sets.size(); i++) {
			flag = true;
			for(int j = 0; j<sni_sets.get(i).s.points.size(); j++) {
				if(test_points.get(j).id != sni_sets.get(i).s.points.get(j).id) {
					flag = false;
					break;
				}
			}
			if(flag == true)
			{
				index = i;
				break;
			}
		}
		
		if(flag)
			return index;
		else 
			return -1;
	}

	
	public static void main(String [] args) throws ParseException 
	{
		
//		String input = "2014-11-19";
//		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//		Date t = null;
//		
//		t = formatter.parse(input);
//
//		t.setSeconds(11);
//		
//		System.out.print(t.getTime());
		
	}
}
