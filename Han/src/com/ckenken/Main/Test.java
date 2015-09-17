package com.ckenken.Main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import lab.adsl.object.Point;

import com.ckenken.implement.sparse.EndPoint;
import com.ckenken.storage.Coarse_pattern;
import com.ckenken.storage.Sequence;
import com.ckenken.storage.Snippet;

public class Test {
	public static void main(String [] args)
	{
//		Coarse_pattern cp = new Coarse_pattern();
//		
//		Snippet s1 = new Snippet();
//		Snippet s2 = new Snippet();
//		Snippet s3 = new Snippet();
//		
//		Sequence seq1 = new Sequence();
//		Sequence seq2 = new Sequence();
//		Sequence seq3 = new Sequence();
//		
//		
//		Point p1 = new Point();
//		p1.lat = 0.0;
//		p1.lng= 0.0;
//		
//		seq1.points.add(p1);
//		
//		Point p2 = new Point();
//		p2.lat = 1.0;
//		p2.lng= 1.0;
//		
//		seq2.points.add(p2);
//		
//		Point p3 = new Point();
//		p3.lat = 2.0;
//		p3.lng= 2.0;
//		
//		seq3.points.add(p3);
//		
//		s1.s = seq1;
//		s2.s = seq2;
//		s3.s = seq3;
//		
//		s1.weight = 1;
//		s2.weight = 1;
//		s3.weight = 1;
//		
//		s1.shiftId = -1;
//		s2.shiftId = -1;
//		s3.shiftId = -1;
//		
//		cp.snippet_sets.add(s1);
//		cp.snippet_sets.add(s2);
//		cp.snippet_sets.add(s3);
//		
//		cp.meanShiftClustering();
//		
//		System.out.println(cp.snippet_sets.get(0).shiftId);
//		System.out.println(cp.snippet_sets.get(1).shiftId);
//		System.out.println(cp.snippet_sets.get(2).shiftId);
		
//		Stack<Integer> st = new Stack<Integer>();
//		
//		st.push(33);
//		st.push(44);
//		
//		System.out.println(st.get(0));
//		System.out.println(st.get(1));
//		
//		st.pop();
//		
//		System.out.println(st.get(0));
		
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		EndPoint e = new EndPoint(36, EndPoint.PLUS, 20);
		map.put(e.getStringKey(), 0);
		e = new EndPoint(36, EndPoint.PLUS, 21);
		map.put(e.getStringKey(), 0);
		e = new EndPoint(100, EndPoint.PLUS, 20);
		map.put(e.getStringKey(), 0);
		e = new EndPoint(100, EndPoint.PLUS, 21);
		map.put(e.getStringKey(), 0);
		
		Iterator<String> it = map.keySet().iterator();
		
		while(it.hasNext()) 
		{
			String key = it.next();
			System.out.println(key + ": " + map.get(key));
		}
	
		e = new EndPoint(36, EndPoint.PLUS, 20);
		map.put(e.getStringKey(), map.get(e.getStringKey())+1);
		
		System.out.println("==========");
		
		it = map.keySet().iterator();
		
		while(it.hasNext()) 
		{
			String key = it.next();
			System.out.println(key + ": " + map.get(key));
		}
		
	}
}
