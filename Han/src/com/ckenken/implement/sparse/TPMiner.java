package com.ckenken.implement.sparse;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import com.ckenken.implement.algo.Prefix;
import com.ckenken.implement.storage.DataPoint;
import com.ckenken.implement.storage.DataSequence;
import com.ckenken.io.JDBC;

public class TPMiner {
	final public static String OUTPUT = "TPMiner_Gowalla_sup3_77_37.txt"; 
	
	public static int [] supCounter = new int[2000];
	final public static int MIN_SUP = 3; 
	final public static int MIN_SUP2 = 3;
	
	final public static int NO_INSERT = 0;
	final public static int INSERT = 1;
	
	public static ArrayList<ArrayList<EndPoint>> createEndPointSequence(ArrayList<ArrayList<EndPoint>> day)
	{
		ArrayList<ArrayList<EndPoint>> end_sequence = new ArrayList<ArrayList<EndPoint>>();
		
		for(int i = 0; i<24; i++) {
			ArrayList<EndPoint> list = new ArrayList<EndPoint>();
			end_sequence.add(list);
		}
		
		for(int i = 0; i<day.size(); i++) {
			while(day.get(i).size() > 0) {
				boolean back = false;
				
				if (i < 23) {
					back = recursiveScanEndPoint(day, day.get(i).get(0).symbolid, i+1, end_sequence);
				}
				
				EndPoint t = new EndPoint();
				t.symbolid = day.get(i).get(0).symbolid;
				t.type = EndPoint.PLUS;
				
				end_sequence.get(i).add(t); 
				
				if (i == 23 || back) {
					t = new EndPoint();
					t.symbolid = day.get(i).get(0).symbolid;
					t.type = EndPoint.MINUS;
					
					end_sequence.get(i).add(t);
				}
				
				day.get(i).remove(0);
			}
		}
		
		return end_sequence;
	}
	
	public static boolean recursiveScanEndPoint(ArrayList<ArrayList<EndPoint>> day, int symbol, int index, ArrayList<ArrayList<EndPoint>> end_sequence)
	{	
		boolean flag = false;
		for(int i = 0; i<day.get(index).size(); i++) {
			
			if (day.get(index).get(i).symbolid == symbol) {
				
				boolean back = false;
				
				if (index != 23) {
					back = recursiveScanEndPoint(day, symbol, index+1, end_sequence);
				}
				
				if (index == 23 || back) {
					EndPoint t = new EndPoint();
					t.symbolid = symbol;
					t.type = EndPoint.MINUS;
					end_sequence.get(index).add(t);		
				}
				
				day.get(index).remove(i);
				flag = true;
				break;
			}
		}
		
		return !flag;
	}
	
	public static void printDayEndPoint(ArrayList<ArrayList<EndPoint>> day) 
	{
		for(int i = 0; i<day.size(); i++) {
			ArrayList<EndPoint> hour = day.get(i);
			System.out.print(i + ": ");
			for(int j = 0; j<hour.size(); j++) {
				if (j == 0)
					System.out.print(hour.get(j).symbolid + (hour.get(j).type == 0?"+":"-"));
				else 
					System.out.print(", " + hour.get(j).symbolid + (hour.get(j).type == 0?"+":"-"));
			}
			System.out.println();
		}	
	}
	
	public static void countSupport(ArrayList<ArrayList<EndPoint>> end_seq) 
	{
		for(int i = 0; i<end_seq.size(); i++) {
			for(int j = 0; j<end_seq.get(i).size(); j++) {
				if (end_seq.get(i).get(j).type == EndPoint.PLUS) {
					supCounter[end_seq.get(i).get(j).symbolid]++;
				}
			}
		}
	}
	
	public static ArrayList<ArrayList<EndPoint>> createSupSequence(ArrayList<ArrayList<EndPoint>> end_seq, int min_sup)
	{
		ArrayList<ArrayList<EndPoint>> sup_seq = new ArrayList<ArrayList<EndPoint>>();
		
		for(int i = 0; i<24; i++) {
			ArrayList<EndPoint> temp = new ArrayList<EndPoint>();
			sup_seq.add(temp);		
		}
		
		for(int i = 0; i<end_seq.size(); i++) {
			for(int j = 0; j<end_seq.get(i).size(); j++) {
				if (supCounter[end_seq.get(i).get(j).symbolid] >= min_sup) {
					EndPoint t = new EndPoint();
					t.symbolid = end_seq.get(i).get(j).symbolid;
					t.type = end_seq.get(i).get(j).type;
					sup_seq.get(i).add(t);
				}
			}
		}		
		
		return sup_seq;
	}
	
	public static ArrayList<EndPoint> transform(ArrayList<ArrayList<EndPoint>> day) 
	{
		ArrayList<EndPoint> output = new ArrayList<EndPoint>();
		
		
		for(int i = 0; i<day.size(); i++) {
			for(int j = 0; j<day.get(i).size(); j++) {
				EndPoint t = new EndPoint();
				t.symbolid = day.get(i).get(j).symbolid;
				t.type = day.get(i).get(j).type;
				t.timestamp = i;
				
				output.add(t);
			}
		}
		
		return output;
	}
	
	public static ArrayList<EndSequence> extendByEndSequence(ArrayList<EndSequence> extended, ArrayList<EndSequence> origin, ArrayList<EndPoint> symbol_sequence)
	{
		for(int i = 0; i<origin.size(); i++) {
			EndSequence seq = origin.get(i);
			
			for(int j = 0; j<seq.endPoints.size(); j++) {
				if(seq.endPoints.get(j).symbolid == symbol_sequence.get(0).symbolid && 
				   seq.endPoints.get(j).type == symbol_sequence.get(0).type) {
					
					Stack<EndPoint> st = new Stack<EndPoint>();
					st.push(seq.endPoints.get(j).copy());
					
					if (symbol_sequence.size() > 1) {
						extendEndRecursive(extended, seq, symbol_sequence, st, seq.endPoints.get(j).timestamp, 1);
					}
					else {
						EndSequence tempSubSequence = new EndSequence();
						int startTimestamp = seq.endPoints.get(j).timestamp;
						
						for(int k = 0; k<seq.endPoints.size(); k++) {
							EndPoint e = seq.endPoints.get(k);
							
							if (e.timestamp < startTimestamp) {
								continue;
							}
							
							boolean contain = false;
							for(int q = 0; q<symbol_sequence.size(); q++) {
								if (e.symbolid == symbol_sequence.get(q).symbolid && e.type == symbol_sequence.get(q).type) {
									contain = true;
									break;
								}
							}
							if (!contain) {
								EndPoint t = new EndPoint(e.symbolid, e.type, e.timestamp);
								tempSubSequence.endPoints.add(t);
							}
						}
						
						while(!st.isEmpty()) {
							tempSubSequence.endPoints.add(0, st.pop());
						}
						
						extended.add(tempSubSequence);
					}
				}
			}
		}
		
//		for(int i = 0; i<origin.size(); i++) {
//			EndSequence seq = origin.get(i);
//			for(int j = 0; j<seq.endPoints.size(); j++)
//			if(seq.endPoints.get(j).symbolid == symbol_sequence.get(0).symbolid && seq.endPoints.get(j).type == symbol_sequence.get(0).type) {
//				
//				ArrayList<EndPoint> st = new ArrayList<EndPoint>();
//				
//				st.add(seq.endPoints.get(j));
//				if(symbol_sequence.size()>1) {
//					extendEndRecursive(extended, seq, symbol_sequence, st, j, 1);
//				}
//				else {
//					EndSequence tempSubEndSequence = new EndSequence();
//					tempSubEndSequence.endPoints = seq.getSubSequence(j+1);
//					
//					for(int k = 0; k<seq.endPoints.size(); k++) {
//						if (seq.endPoints.get(k).timestamp == seq.endPoints.get(j).timestamp && !tempSubEndSequence.endPoints.contains(seq.endPoints.get(k)) && !st.contains(seq.endPoints.get(k))) {
//							tempSubEndSequence.endPoints.add(0, seq.endPoints.get(k));
//						}
//					}
//					
//					for(int k = st.size()-1; k>=0; k--) {
//						tempSubEndSequence.endPoints.add(0, st.get(k));
//					}
//					extended.add(tempSubEndSequence);
//				} 
//					
//				st.remove(0);
//			}
//		}
		return extended;
	}	
	
	public static void extendEndRecursive(ArrayList<EndSequence> extended, EndSequence seq, ArrayList<EndPoint> symbol_sequence, Stack<EndPoint> st, int nowTimestamp, int symbol_index) {

		for(int i = 0; i<seq.endPoints.size(); i++) {
			EndPoint p = seq.endPoints.get(i);
			if (p.timestamp < nowTimestamp) {
				continue;
			}
			if (p.symbolid == symbol_sequence.get(symbol_index).symbolid && 
					p.type == symbol_sequence.get(symbol_index).type &&
					(p.timestamp - st.get(0).timestamp) == symbol_sequence.get(symbol_index).timestamp) {
				st.push(p.copy());
				if (symbol_index == (symbol_sequence.size() - 1)) { //last one, output one extended
					
//					st.push(p.copy());
					
					EndSequence tempSubSequence = new EndSequence();
					int startTimestamp = p.timestamp;
					for(int k = 0; k<seq.endPoints.size(); k++) {
						EndPoint e = seq.endPoints.get(k);
						
						if (e.timestamp < startTimestamp) {
							continue;
						}
						
						boolean contain = false;
						for(int q = 0; q<symbol_sequence.size(); q++) {
							if (e.symbolid == symbol_sequence.get(q).symbolid && e.type == symbol_sequence.get(q).type) {
								contain = true;
								break;
							}
						}
						if (!contain) {
							EndPoint t = new EndPoint(e.symbolid, e.type, e.timestamp);
							tempSubSequence.endPoints.add(t);
						}
					}
					
//					while(!st.isEmpty()) {
//						tempSubSequence.endPoints.add(0, st.pop());
//					}
					
					for(int k = st.size()-1; k>=0; k--) {
						tempSubSequence.endPoints.add(0, st.get(k).copy());
					}
					
					extended.add(tempSubSequence);
				}
				else {  // not last one, recursive gogogo 
//					st.push(p);
					extendEndRecursive(extended, seq, symbol_sequence, st, p.timestamp, symbol_index+1);
//					if (!st.isEmpty()) {
//						st.pop();
//					}
				}
				st.pop();
			}
		}
		
		
//		for(int i = now; i< seq.endPoints.size(); i++) {
//			EndPoint p = seq.endPoints.get(i);
//			
//			if(p.symbolid == symbol_sequence.get(symbol_index).symbolid && p.type == symbol_sequence.get(symbol_index).type) { //符合條件的點
//				if(symbol_index == symbol_sequence.size() - 1) { // last point , output one extended
//					
//					EndSequence tempSubEndSequence = new EndSequence();
//					tempSubEndSequence.endPoints = seq.getSubSequence(i);
//					
//					for(int j = 0; j<seq.endPoints.size(); j++) {
//						if (seq.endPoints.get(j).timestamp == seq.endPoints.get(i).timestamp && !tempSubEndSequence.endPoints.contains(seq.endPoints.get(j)) && !stack.contains(seq.endPoints.get(j))) {
//							tempSubEndSequence.endPoints.add(0, seq.endPoints.get(j));
//						}
//					}
//					
//					for(int j = stack.size()-1; j>=0; j--) {
//						tempSubEndSequence.endPoints.add(0, stack.get(j));
//					}
//					extended.add(tempSubEndSequence);
//				}
//				else  // not last one
//				{
//					stack.add(p);
//					extendEndRecursive(extended, seq, symbol_sequence, stack, i, symbol_index+1);
//					stack.remove(stack.size()-1);
//				}
//			}
//		}
	}
	
	public static void TPSpan(EndSequence a, ArrayList<EndSequence> extended, ArrayList<EndSequence> TP)
	{
		ArrayList<EndPoint> FE = new ArrayList<EndPoint>();
		
		FE = scan_pruning(a, extended);
		FE = point_prunuing(a, FE);
		
//		System.out.println("FE:");
//		for(int i = 0; i<FE.size(); i++) {
//			System.out.print(FE.get(i).symbolid + (FE.get(i).type == EndPoint.PLUS?"+":"-") + ",");
//		}
//		System.out.println();
		
		for(int i = 0; i<FE.size(); i++) {
			a.endPoints.add(FE.get(i).copy());
			
			if (checkTempral(a)) {		
		
				TP.add(a.copy());
			}
				
			ArrayList<EndSequence> backup = backupExtended(extended);
			
			extended = postfix_pruning(a, extended);
			
			TPSpan(a, extended, TP);
			releaseExtended(extended);
			extended = backup;
			
			a.endPoints.remove(a.endPoints.size()-1);
		}
	}
	
	public static ArrayList<EndPoint> scan_pruning(EndSequence a, ArrayList<EndSequence> extended) 
	{
		ArrayList<EndPoint> FE = new ArrayList<EndPoint>();
		
		int [] plus = new int[2000];
		int [] minus = new int[2000];
		
		HashMap<String, Integer> aMap = new HashMap<String, Integer>();
		
		for(int i = 0; i<a.endPoints.size(); i++) {
			aMap.put(a.endPoints.get(i).getStringKey(), 1);
		}
				
		for(int i = 0; i<a.endPoints.size(); i++) {
			if (a.endPoints.get(i).type == EndPoint.PLUS) {
				plus[a.endPoints.get(i).symbolid] = 1;
			}
			else {
				minus[a.endPoints.get(i).symbolid] = 1;
			} 
		}
		
		HashMap<String, Integer> canMap = new HashMap<String, Integer>();
		
		for(int i = 0; i<extended.size(); i++) {
			int timestamp = -1;
			for(int j = 0; j<extended.get(i).endPoints.size(); j++) {
				if (plus[extended.get(i).endPoints.get(j).symbolid] == 1 && extended.get(i).endPoints.get(j).type == EndPoint.MINUS) {
					timestamp = extended.get(i).endPoints.get(j).timestamp;
					break;
				}
			}
			if (timestamp != -1) {  // find successful
				for(int j = 0; j<extended.get(i).endPoints.size(); j++) {
					if (plus[extended.get(i).endPoints.get(j).symbolid] != 1 && 
							extended.get(i).endPoints.get(j).type == EndPoint.PLUS && 
							extended.get(i).endPoints.get(j).timestamp <= timestamp || 
						minus[extended.get(i).endPoints.get(j).symbolid] != 1 &&
							extended.get(i).endPoints.get(j).type == EndPoint.MINUS && 
							extended.get(i).endPoints.get(j).timestamp <= timestamp) {
						
						EndPoint e = new EndPoint(extended.get(i).endPoints.get(j).symbolid, 
												  extended.get(i).endPoints.get(j).type, 
												  extended.get(i).endPoints.get(j).timestamp - extended.get(i).endPoints.get(0).timestamp);
						
						if (canMap.containsKey(e.getStringKey())) {
							canMap.put(e.getStringKey(), canMap.get(e.getStringKey()) + 1);
						}
						else {
							canMap.put(e.getStringKey(), 1);
						}
						
					}
				}
			}
		}
		
		Iterator<String> it = canMap.keySet().iterator();
		
		while(it.hasNext()) 
		{
			String key = it.next();
			if (canMap.get(key) >= MIN_SUP2) {
				EndPoint e = EndPoint.keyToEndPoint(key);
				FE.add(e);
			}
		}
		
		
//		for(int i = 0; i<extended.size(); i++) {
//			int timestamp = -1;
//			for(int j = 0; j<extended.get(i).endPoints.size(); j++) {
//				if (plus[extended.get(i).endPoints.get(j).symbolid] == 1 && extended.get(i).endPoints.get(j).type == EndPoint.MINUS) {
//					timestamp = extended.get(i).endPoints.get(j).timestamp;
//					break;
//				}
//			}
//			if (timestamp != -1) {  // find successful
//				for(int j = 0; j<extended.get(i).endPoints.size(); j++) {
//					if (plus[extended.get(i).endPoints.get(j).symbolid] != 1 && extended.get(i).endPoints.get(j).type == EndPoint.PLUS && extended.get(i).endPoints.get(j).timestamp <= timestamp) {
//						canPlus[extended.get(i).endPoints.get(j).symbolid]++;
//					}
//					else if (minus[extended.get(i).endPoints.get(j).symbolid] != 1 && extended.get(i).endPoints.get(j).type == EndPoint.MINUS && extended.get(i).endPoints.get(j).timestamp <= timestamp){
//						canMinus[extended.get(i).endPoints.get(j).symbolid]++;
//					}
//				}
//			}
//		}
//		
//		for(int i = 0; i<canPlus.length; i++) {
//			if (canPlus[i] >= MIN_SUP2) {
//				EndPoint e = new EndPoint();
//				e.symbolid = i;
//				e.type = EndPoint.PLUS;
//				e.timestamp = -1;
//				FE.add(e);
//			}
//			else if (canMinus[i] >= MIN_SUP2) {
//				EndPoint e = new EndPoint();
//				e.symbolid = i;
//				e.type = EndPoint.MINUS;
//				e.timestamp = -1;
//				FE.add(e);
//			}
//		}
		
		return FE;
	}
	
	public static ArrayList<EndPoint> point_prunuing(EndSequence a, ArrayList<EndPoint> FE)
	{
		ArrayList<EndPoint> FE2 = new ArrayList<EndPoint>();
		
		for(int i = 0; i<FE.size(); i++) {
			EndPoint temp = FE.get(i);
			
			boolean flag = false;
			for(int j = 0; j<a.endPoints.size(); j++){
				if (temp.symbolid == a.endPoints.get(j).symbolid && temp.type == EndPoint.MINUS && a.endPoints.get(j).type == EndPoint.PLUS) {
					flag = true;
					break;
				}
				else if (temp.type == EndPoint.PLUS) {
					flag = true;
					break;
				}
			}
			if (flag) {
				FE2.add(FE.get(i).copy());		
			}
		}
		
		return FE2;
	}
	
	public static boolean checkTempral(EndSequence a) 
	{
		boolean flag = true;
		for(int i = 0; i<a.endPoints.size(); i++) {
			if (a.endPoints.get(i).type == EndPoint.PLUS) {
	
				boolean internalFlag = false;
				for(int j = 0; j<a.endPoints.size(); j++) {
					if (a.endPoints.get(j).symbolid == a.endPoints.get(i).symbolid && a.endPoints.get(j).type == EndPoint.MINUS) {
						internalFlag = true;
						break;
					}
				}
				if (!internalFlag) {
					flag = false;
					break;
				}
//				if (!a.endPoints.contains(e)) {
//					flag = false;
//					break;
//				}
			}
		}
		return flag;
	}
	
	public static ArrayList<EndSequence> postfix_pruning(EndSequence a, ArrayList<EndSequence> new_origin)
	{
		ArrayList<EndSequence> extended = new ArrayList<EndSequence>();
		
		extended = extendByEndSequence(extended, new_origin, a.endPoints);
		
		return extended;
	}
	
	public static ArrayList<EndSequence> backupExtended(ArrayList<EndSequence> extended)
	{
		ArrayList<EndSequence> backup = new ArrayList<EndSequence>();
		
		for(int i = 0; i<extended.size(); i++) {
			EndSequence seq = new EndSequence();
			for(int j = 0; j<extended.get(i).endPoints.size(); j++) {
				seq.endPoints.add(extended.get(i).endPoints.get(j).copy());
			}
			backup.add(seq);		
		}
		return backup;
	}
	
	public static void releaseExtended(ArrayList<EndSequence> extended) 
	{
		for(int i = 0; i<extended.size(); i++) {
			extended.get(i).release();
		}
		extended.clear();
	}
	
	public static void printExtended(ArrayList<EndSequence> extended)
	{
		for(int i = 0; i<extended.size(); i++) {
			for(int j = 0; j<extended.get(i).endPoints.size(); j++) {
				if (j == 0)
					System.out.print(extended.get(i).endPoints.get(j).symbolid + (extended.get(i).endPoints.get(j).type==EndPoint.PLUS?"+":"-"));
				else 
					System.out.print("," + extended.get(i).endPoints.get(j).symbolid + (extended.get(i).endPoints.get(j).type==EndPoint.PLUS?"+":"-"));
			}
			System.out.println();
		}
	}
	
	public static ArrayList<EndSequence> runTPMiner(int insert) throws SQLException, ParseException
	{
		if (Gcenter.jdbc == null || Gcenter.jdbc.con.isClosed()) {
			Gcenter.jdbc = new JDBC("han");
		}
		
//		ArrayList<ArrayList<EndPoint>> day = BuildTrajectory.getDaySymbolArray("12-14");
//		
//		BuildTrajectory.printDay(day);
//		
//		ArrayList<ArrayList<EndPoint>> end_sequence = createEndPointSequence(day);
//		
//		printDayEndPoint(end_sequence);

		String sql = "Select distinct day from sequence22";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		while(rs.next()) {
			
			ArrayList<ArrayList<EndPoint>> day = BuildTrajectory.getDaySymbolArray(rs.getString("day"));
						
			ArrayList<ArrayList<EndPoint>> end_sequence = createEndPointSequence(day);

			countSupport(end_sequence);
		}
		rs.close();
		
		System.out.println("=============================");
//
//		for(int i = 0;i<1200; i++) {
//			System.out.println(i + ": " + supCounter[i]);
//		}
//		
		sql = "Select distinct day from sequence22";
		
		rs = Gcenter.jdbc.query(sql);
		
		ArrayList<EndSequence> origin = new ArrayList<EndSequence>();
		
		while(rs.next()) {
			
			ArrayList<ArrayList<EndPoint>> day = BuildTrajectory.getDaySymbolArray(rs.getString("day"));
						
			ArrayList<ArrayList<EndPoint>> end_sequence = createEndPointSequence(day);

			ArrayList<ArrayList<EndPoint>> sup_sequence = createSupSequence(end_sequence, 10);
			
			printDayEndPoint(sup_sequence);
			
			System.out.println("====");
			
			ArrayList<EndPoint> e = transform(sup_sequence);
			
			EndSequence es = new EndSequence(e);
			
			origin.add(es);
//			break;
		}
		rs.close();
		ArrayList<EndSequence> old_end_sequence = new ArrayList<EndSequence>();
		ArrayList<EndSequence> new_end_sequence = new ArrayList<EndSequence>();
		
		for(int i = 0; i<2000; i++) {
			if (supCounter[i] >= TPMiner.MIN_SUP) {
				EndPoint t = new EndPoint();
				t.symbolid = i;
				t.type = EndPoint.PLUS;
				t.timestamp = 0;
				ArrayList<EndPoint> temp = new ArrayList<EndPoint>();
				temp.add(t);
				EndSequence es = new EndSequence(temp);
				old_end_sequence.add(es);
			}
		}
			
		ArrayList<EndSequence> extended = new ArrayList<EndSequence>();
		ArrayList<EndSequence> TP = new ArrayList<EndSequence>();
		
		for(int i = 0; i<old_end_sequence.size(); i++) {
			extended = extendByEndSequence(extended, origin, old_end_sequence.get(i).endPoints); 		
		
			if (extended.size() <= 0) {
				continue;
			}
			
//			for(int q = 0; q<old_end_sequence.get(i).endPoints.size(); q++) {
//				System.out.print(old_end_sequence.get(i).endPoints.get(q).symbolid + ",");
//			}
//			System.out.println();
//			System.out.println("==============");
//			
//			printExtended(extended);
						
			if (extended.size() > 0) {
				TPSpan(old_end_sequence.get(i), extended, TP);
			}
			releaseExtended(extended);
		}
		ArrayList<EndSequence> TP2 = new ArrayList<EndSequence>();
		
		for(int i = 0; i<TP.size(); i++) {
			boolean flag = false;
			for(int j = 0; j<TP2.size(); j++) {
				if (EndSequence.AincludeB(TP2.get(j), TP.get(i))) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				TP2.add(TP.get(i).copy());
				
				for(int j = 0; j<TP2.size()-1; j++) {
					if (EndSequence.AincludeB(TP2.get(TP2.size()-1), TP2.get(j))) {
						TP2.remove(j);
						j--;
					}
				}
			}
		}
		
		System.out.println("number of pattern: " + TP2.size());
		
		for(int i = 0; i<TP2.size(); i++) {
			sql = "Select distinct day from sequence22";
			
			System.out.println("TPid = " + i );
			
			rs = Gcenter.jdbc.query(sql);
			
			int counter = 0;
			
			while(rs.next()) {
				
				ArrayList<ArrayList<EndPoint>> day = BuildTrajectory.getDaySymbolArray(rs.getString("day"));
							
				ArrayList<ArrayList<EndPoint>> end_sequence = createEndPointSequence(day);

				ArrayList<ArrayList<EndPoint>> sup_sequence = createSupSequence(end_sequence, 10);
				
				boolean flag = false;

				for(int j = 0; j<sup_sequence.size(); j++) {
					for(int q = 0; q<sup_sequence.get(j).size(); q++) {
						if (sup_sequence.get(j).get(q).symbolid == TP2.get(i).endPoints.get(0).symbolid && 
							sup_sequence.get(j).get(q).type == TP2.get(i).endPoints.get(0).type	) {
							
							flag = recursiveFindPatternInDay(TP2.get(i), sup_sequence, j, 0, 1);
							if (flag) {
								break;
							}
						}
					}
					if (flag) {
						counter++;
						break;
					}
				}
			}
			TP2.get(i).counter = counter;
			counter = 0;
		}
		
		for(int i = 0; i<TP2.size(); i++) {
//			if (TP2.get(i).endPoints.size() <= 2)
//				continue;
			StringBuilder SB = new StringBuilder();
			
			for(int j = 0; j<TP2.get(i).endPoints.size(); j++) {
				if (j == 0) {
				//	System.out.print(TP.get(i).endPoints.get(j).symbolid + (TP.get(i).endPoints.get(j).type == EndPoint.PLUS?"+":"-"));
					System.out.print(TP2.get(i).endPoints.get(j).getStringKey());
					SB.append(TP2.get(i).endPoints.get(j).getStringKey());
				}
				else { 
				//	System.out.print("," + TP.get(i).endPoints.get(j).symbolid + (TP.get(i).endPoints.get(j).type == EndPoint.PLUS?"+":"-"));					
					System.out.print("," + TP2.get(i).endPoints.get(j).getStringKey());
					SB.append("," + TP2.get(i).endPoints.get(j).getStringKey());
				}
			}
			sql = "insert into datapattern values(NULL, '" + SB.toString() +  "'," + TP2.get(i).endPoints.size() + "," + TP2.get(i).counter + ")";
			
			if (insert == INSERT) {
				Gcenter.jdbc.insertQuery(sql);	
			}
			System.out.println(" " + TP2.get(i).counter);
		}		
		
		return TP2;
	}
	
	public static boolean recursiveFindPatternInDay(EndSequence tp, ArrayList<ArrayList<EndPoint>> day, int hour, int step, int tp_index) 
	{
		if (tp.endPoints.get(tp_index).timestamp < step) {
			return false;
		}
		
		boolean flag = false;
		for(int i = 0; i<day.get(hour).size(); i++) {
			if (day.get(hour).get(i).symbolid == tp.endPoints.get(tp_index).symbolid && 
				day.get(hour).get(i).type == tp.endPoints.get(tp_index).type &&
				tp.endPoints.get(tp_index).timestamp == step) {
				
				if (tp_index == tp.endPoints.size()-1) {
					return true;
				}
				
				flag = recursiveFindPatternInDay(tp, day, hour, step, tp_index+1);
			}
		}
		
		if (!flag) {	
			if (hour < 23) {
				flag = recursiveFindPatternInDay(tp, day, hour+1, step+1, tp_index);
			}
		}
		
		return flag;
	}
	
	public static void main(String[] args) throws SQLException, ParseException, FileNotFoundException 
	{
		PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
		System.setOut(outstream);
		
		double RunStartTime = 0, RunEndTime = 0, totTime = 0;
		RunStartTime = System.currentTimeMillis();		
		
		runTPMiner(INSERT);

		RunEndTime = System.currentTimeMillis();
		
		totTime = RunEndTime - RunStartTime;		
		
		System.out.println("time = " + totTime + "ms");
		
//		ArrayList<EndPoint> test = new ArrayList<EndPoint>();
//		
//		EndPoint k = new EndPoint();
//		k.symbolid = 36;
//		k.type = EndPoint.PLUS;
//		k.timestamp = 20;
//		test.add(k);
//		
////		k = new EndPoint();
////		k.symbolid = 36;
////		k.timestamp = 23;
////		k.type = EndPoint.MINUS;
////		test.add(k);		
////		
////		k = new EndPoint();
////		k.symbolid = 135;
////		k.timestamp = 20;
////		k.type = EndPoint.PLUS;
////		test.add(k);		
////
////		k = new EndPoint();
////		k.symbolid = 100;
////		k.timestamp = 21;
////		k.type = EndPoint.PLUS;
////		test.add(k);		
////		
////		k = new EndPoint();
////		k.symbolid = 287;
////		k.timestamp = 21;
////		k.type = EndPoint.PLUS;
////		test.add(k);		
////		
////		k = new EndPoint();
////		k.symbolid = 627;
////		k.timestamp = 22;
////		k.type = EndPoint.PLUS;
////		test.add(k);		
//
//		extended = extendByEndSequence(extended, origin, test); 
//		
//		for(int i = 0; i<extended.size(); i++) {
//			for(int j = 0; j<extended.get(i).endPoints.size(); j++) {
//				if (j == 0) {
//					System.out.print(extended.get(i).endPoints.get(j).symbolid);
//					if (extended.get(i).endPoints.get(j).type == EndPoint.PLUS)
//						System.out.print("+");
//					else 
//						System.out.print("-");
//				}
//				else {
//					System.out.print("," + extended.get(i).endPoints.get(j).symbolid);
//					if (extended.get(i).endPoints.get(j).type == EndPoint.PLUS)
//						System.out.print("+");
//					else 
//						System.out.print("-");					
//				}
//			}
//			System.out.println();
//		}
		
		
		
		
	}
}
