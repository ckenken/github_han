package com.ckenken.implement.algo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import com.ckenken.implement.run.IM_Main;
import com.ckenken.implement.storage.DataPoint;
import com.ckenken.implement.storage.DataSequence;
import com.ckenken.io.JDBC;

public class BruteForce {
	
	public static ArrayList<ArrayList<Integer>> test_sequence = new ArrayList<ArrayList<Integer>>();
	
	private static final String OUTPUT = "brute_force_pattern.txt";  
	
	public static ArrayList<DataSequence> seqs = new ArrayList<DataSequence>();
	public static DataSequence seq;	
	
	public static void main(String[] args) throws SQLException, ParseException, IOException {

//		PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
//		System.setOut(outstream);		
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from sequence30";
		
		ResultSet rs = jdbc.query(sql);
		
		ArrayList<DataPoint> datas = new ArrayList<DataPoint>();
		
		while(rs.next()) {
			int seqid = rs.getInt("seqid");
			int sameid = rs.getInt("sameid");
			double lat = rs.getDouble("lat");
			double lng = rs.getDouble("lng");
			int G = rs.getInt("G");
			String cate = rs.getString("cate");
			String startTime = rs.getString("time");
			String endTime = rs.getString("endtime");
			
			DataPoint np = new DataPoint(seqid,sameid, lat, lng, G, cate, startTime, endTime);
			
			np.symbol = rs.getInt("symbol");
			
			datas.add(np);
		}

		seq = new DataSequence(datas);				
		
		seqs.add(seq);
		
		sql = "select max(symbol) from sequence30";
		rs = jdbc.query(sql);
		rs.next();
		
		int max_symbol = rs.getInt("max(symbol)");
		
		double startTime = 0,endTime = 0, totTime = 0;
		startTime = System.currentTimeMillis();
		
		for(int i = 2; i<4; i++) {	
			ArrayList<Integer> testPattern = new ArrayList<Integer>();
			generateTestSequence(testPattern, i, max_symbol);				
		}
		
		
		endTime = System.currentTimeMillis();
		
		totTime = endTime - startTime;		
		
		System.out.println("time = " + totTime + "ms");
		
//		for(int i = 0; i<test_sequence.size(); i++) {
//			for(int j = 0; j<test_sequence.get(i).size(); j++) {
//				if(j == 0)
//					System.out.print(test_sequence.get(i).get(j));
//				else 
//					System.out.print("->" + test_sequence.get(i).get(j));
//			}
//			System.out.println();
//		}
	}
	
	public static void generateTestSequence(ArrayList<Integer> origin, int length, int max_symbol) throws SQLException, FileNotFoundException, ParseException
	{		
		if (origin.size() >= length) {
			
			if (checkCont(origin)) {
				
//				ArrayList<Integer> temp = new ArrayList<Integer>();
//				for(int i = 0; i<origin.size(); i++) {
//					temp.add(origin.get(i));
//				}
//				test_sequence.add(temp);

				Prefix.extended = new ArrayList<DataSequence>();
				
				Prefix.extendByDataSequence(seqs, origin, IM_Main.DT);
//				if(Prefix.extended.size() != 0) {
//					for(int j = 0; j<origin.size(); j++) {
//						if(j == 0)
//							System.out.print(origin.get(j));
//						else 
//							System.out.print("->" + origin.get(j));
//					}				
//	//				System.out.println();
//					System.out.println(" " + Prefix.extended.size());
//				}
				
//				if(Prefix.extended.size() != 0)
//					System.out.println(" " + Prefix.extended.size());
//				else 
//					System.out.println();
				
				return;
			}	
			else 
				return;
		}
		
		for(int i = 0; i<=max_symbol; i++) {
			origin.add(i);
			generateTestSequence(origin, length, max_symbol);
			origin.remove(origin.size()-1);
		}
	}
	
	public static boolean checkCont(ArrayList<Integer> pattern) 
	{
		
		for(int i = 0; i<pattern.size()-1; i++) {
			if(pattern.get(i).equals(pattern.get(i+1)))
				return false;
		}
		return true;
	}
	
}
