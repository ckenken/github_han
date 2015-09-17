	package com.ckenken.implement.sparse;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import com.ckenken.implement.algo.Prefix;
import com.ckenken.implement.run.IM_Main;
import com.ckenken.implement.run.IndexSymbol;
import com.ckenken.implement.storage.Coarse_data_pattern;
import com.ckenken.implement.storage.DataPoint;
import com.ckenken.implement.storage.DataSequence;
import com.ckenken.io.JDBC;

public class SP_Main {
	public static void main(String[] args) throws SQLException, ParseException, IOException {
//		PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
//		System.setOut(outstream);		
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from sequence22";
		
		ResultSet rs = jdbc.query(sql);
		
		ArrayList<DataPoint> datas = new ArrayList<DataPoint>();

		double RunStartTime = 0, RunEndTime = 0, totTime = 0;
		RunStartTime = System.currentTimeMillis();		
		
		while(rs.next()) {
			int seqid = rs.getInt("seqid");
			int sameid = rs.getInt("sameid");
			double lat = rs.getDouble("lat");
			double lng = rs.getDouble("lng");
			int G = rs.getInt("G");
			String cate = rs.getString("cate");
			String startTime = rs.getString("time");
			String endTime = rs.getString("endtime");
			
			DataPoint temp = new DataPoint(seqid,sameid, lat, lng, G, cate, startTime, endTime);
			
			temp.symbol = rs.getInt("symbol");
			
			datas.add(temp);
		}		
		
		HashMap<Integer, Integer> map = IndexSymbol.symbolizeByHotRegion(datas, IndexSymbol.SS);
		
		System.out.println("end");
		
		for(int i = 0; i<datas.size(); i++) {
			datas.get(i).symbol = map.get(datas.get(i).seqid);
		}
		System.out.println("end2");
		
		for(int i = 0; i<datas.size(); i++) {
		//	System.out.println(datas.get(i).seqid + ": " + datas.get(i).symbol);
			
			sql = "update sequence22 set symbol = " + datas.get(i).symbol + " where seqid = " + datas.get(i).seqid;
			jdbc.insertQuery(sql);	
		}
		
		System.out.println("end3");
		
		///////// merged HASH ////////
		
		sql = "select max(symbol) from sequence22";
		
		rs = jdbc.query(sql);
		
		rs.next();
		
		int max_symbolid = rs.getInt("max(symbol)");
		
		for(int i = 0; i<=max_symbolid; i++) {
			DataPoint temp = new DataPoint();
			boolean flag = false;
			
			for(int j = 0; j<datas.size(); j++) {
				if (datas.get(j).symbol == i) {
					temp = DataPoint.copy(datas.get(j));
					flag = true;
					break;
				}
			}
			if (flag == false)
				continue;
			
			for(int j = 0; j<datas.size(); j++) {
				if (datas.get(j).symbol == i) {
					temp = DataPoint.merge(temp, datas.get(j));
				}
			}
			
			for(int j = 0; j<datas.size(); j++) {
				if (datas.get(j).symbol == i) {
					datas.get(j).copyDistribution(temp);
					datas.get(j).copyLatLng(temp);
				}
			}
		}
		
		/////////////////////////////
		
		for(int i = 0; i<datas.size(); i++) {
			DataPoint d = datas.get(i);
			
			StringBuilder SB = new StringBuilder();
			for(int j = 0; j<24; j++) {
				SB.append(j + ":" + d.timeDistribution[j] + "\n");
			}
			
			String timeD = SB.toString();
			
			SB = new StringBuilder();
			
			for(int j = 0; j<d.gDistribution.length; j++) {
				SB.append(j + ":" + d.gDistribution[j] + "\n");
			}			
			
			String gD = SB.toString();
			
			
			sql = "select * from prefixcenter where symbolid = " + d.symbol;
			
			ResultSet rs2 = jdbc.query(sql);
			
			if (rs2.next()) {
				sql = "update prefixCenter set timeDistribution='" + timeD + "', gDistribution='" + gD +"' where symbolid=" + d.symbol;		
			}
			else {
				sql = "insert into prefixcenter values(" + d.symbol + "," + d.lat + "," + d.lng + ",'" + gD + "','" + timeD + "')";
			}
			
			jdbc.insertQuery(sql);
		}

		
		RunEndTime = System.currentTimeMillis();
		
		totTime = RunEndTime - RunStartTime;		
		
		System.out.println("time = " + totTime + "ms");
		
		////
		
//		MergeNeighbor.main(para);
		
		////
		
//		sql = "select * from merged_sequence30_training";
	
//		sql = "select * from sequence22";
//		
//		rs = jdbc.query(sql);
//		
//		datas = new ArrayList<DataPoint>();
//		
//		while(rs.next()) {
//			int seqid = rs.getInt("seqid");
//			int sameid = rs.getInt("sameid");
//			double lat = rs.getDouble("lat");
//			double lng = rs.getDouble("lng");
//			int G = rs.getInt("G");
//			String cate = rs.getString("cate");
//			String startTime = rs.getString("time");
//			String endTime = rs.getString("endtime");
//			
//			DataPoint temp = new DataPoint(seqid,sameid, lat, lng, G, cate, startTime, endTime);
//			
//			temp.symbol = rs.getInt("symbol");
//			
//			datas.add(temp);
//		}
//		
//		//sql = "select max(symbol) from merged_sequence30_training";
//		
//		sql = "select max(symbol) from sequence22";
//		
//		rs = jdbc.query(sql);
//		
//		rs.next();
//		
//		DataPoint.max_symbol = rs.getInt("max(symbol)");
//		
//		//DataPoint.max_symbol = 315;
//				
////		double RunStartTime = 0,endTime = 0, totTime = 0;
////		RunStartTime = System.currentTimeMillis();
//		
//		ArrayList<ArrayList<Integer>> old_symbol_sequence = new ArrayList<ArrayList<Integer>>();
//		ArrayList<ArrayList<Integer>> new_symbol_sequence = new ArrayList<ArrayList<Integer>>();
//		
//		for(int i = 0; i<=DataPoint.max_symbol; i++) {
//			ArrayList<Integer> temp = new ArrayList<Integer>();
//			temp.add(i);
//			old_symbol_sequence.add(temp);
//		}
//		
//		Gcenter.jdbc = new JDBC("han");
//		
//		ArrayList<DataSequence> origin = BuildTrajectory.createOrigin();
//		
//		for(int k = 2; k<= IM_Main.LENGTH; k++) {
//			int counter = 0;
//			for(int i = 0; i<old_symbol_sequence.size(); i++) {
//				Prefix.extended = new ArrayList<DataSequence>();
//				ArrayList<DataSequence> extended = Prefix.extendByDataSequence(origin, old_symbol_sequence.get(i), IM_Main.DT);
//		
//				if(extended.size() == 0) {
//					continue;
//				}
//				
//				for(int j = 0; j<=DataPoint.max_symbol; j++) {
//
//					Coarse_data_pattern cp = Prefix.testDataCoarse(extended, k, old_symbol_sequence.get(i).get(k-2), j, IM_Main.DT, IM_Main.SIGMA);
//					if(cp != null) {
//						cp.show();
//						cp.insertDataPattern("dataPattern");
//						counter++;
//						new_symbol_sequence.add(cp.symbol_sequence);	
//					}				
//				}
//			}
//			
//			System.out.println("counter = " + counter);
//			System.out.println("======================");
//			//  ============== ending initialize ===============
//			old_symbol_sequence.clear();
//			old_symbol_sequence = new ArrayList<ArrayList<Integer>>();
//			for(int i = 0; i<new_symbol_sequence.size(); i++) {
//				ArrayList<Integer> tempGseq = new ArrayList<Integer>();
//				
//				for(int j = 0; j<new_symbol_sequence.get(i).size(); j++) {
//					int temp = new_symbol_sequence.get(i).get(j);
//					tempGseq.add(temp);
//				}
//				
//				old_symbol_sequence.add(tempGseq);
//			}
//			new_symbol_sequence.clear();
//			new_symbol_sequence = new ArrayList<ArrayList<Integer>>();
//			
//		}
		
//		RunEndTime = System.currentTimeMillis();
//		
//		totTime = RunEndTime - RunStartTime;		
//		
//		System.out.println("time = " + totTime + "ms");
		
		
		
	}
}
