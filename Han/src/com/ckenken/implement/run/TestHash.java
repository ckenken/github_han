package com.ckenken.implement.run;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import com.ckenken.implement.storage.DataPoint;
import com.ckenken.io.JDBC;

public class TestHash {
	private static final String OUTPUT = "Test_Hash.txt";  
	
	public static void main(String[] args) throws SQLException, ParseException, IOException {
		
////		System.out.println(0.2/Math.sqrt(0.2));
//		
//		double [] v = new double[9];
//	
//		v[0] = 0.4;
//		
//		for(int i = 1; i<2; i++)
//			v[i] = 0.4;
//		v[2] = 0.2;
//		
//		DataPoint a = new DataPoint();
//
//		for(int i = 0; i<a.gDistribution.length; i++)
//			a.gDistribution[i] = v[i];
//		
//		double [] v2 = new double[9];
//		
//		v2[0] = 0.5;
//		v2[1] = 0.5;
//		
//		for(int i = 1; i<v.length; i++)
//			v[i] = 0;
//		
//		DataPoint b = new DataPoint();		
//		
//		for(int i = 0; i<b.gDistribution.length; i++)
//			b.gDistribution[i] = v2[i];
//		
//		System.out.println("sem = " + DataPoint.semantic_cos(a, b));
//		
//		
		
		PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
		System.setOut(outstream);		
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from sequence30_training";
		
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
		
//		for(int i = 0; i<datas.size(); i++) {
//			System.out.println(datas.get(i).seqid + ": " + datas.get(i).symbol);
//		}
		
		
		sql = "select max(symbol) from sequence30_training";
		
		rs = jdbc.query(sql);
		
		rs.next();
		
		int max_symbolid = rs.getInt("max(symbol)");
		
		for(int k = 0; k<=max_symbolid; k++) {
			for(int i = 0; i<datas.size(); i++) {
				if (datas.get(i).symbol == k) {
					for(int j = i+1; j<datas.size(); j++) {
						if (datas.get(j).symbol == k) {
							System.out.println(datas.get(i).seqid + "<->" + datas.get(j).seqid + ": sem = " + DataPoint.semantic_cos(datas.get(i), datas.get(j)) + ", time = " + DataPoint.time_cos(datas.get(i), datas.get(j)) );
						}
					}
				}
			}			
		}

	}
}
