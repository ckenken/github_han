package com.ckenken.implement.sparse;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import com.ckenken.implement.storage.DataPoint;
import com.ckenken.io.JDBC;

public class Ranked_Coverage {
	//final public static String OUTPUT = "Ranked_Coverage_Gowalla_sup3_77-_37_3hour.txt"; 
	
	public static void main(String[] args) throws SQLException, ParseException, IOException {
	//	PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
	//	System.setOut(outstream);
		
		if (Gcenter.jdbc == null || Gcenter.jdbc.con.isClosed()) {
			Gcenter.jdbc = new JDBC("han");
		}
	
//	ArrayList<EndSequence> TP2 = TPMiner.runTPMiner(TPMiner.NO_INSERT);

		ParseCheckin.NEAR_WINDOW = 10800;
		
		ArrayList<EndSequence> TP2 = new ArrayList<EndSequence>();
		
		String sql = "select * from datapattern order by frequent desc";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		while(rs.next()) {
			String line = rs.getString("datapattern");
			
			String [] SP = line.split(",");
			
			EndSequence es = new EndSequence();
			
			for(int i = 0; i<SP.length; i++) {
				EndPoint e = EndPoint.keyToEndPoint(SP[i]);
				es.endPoints.add(e);
			}
			TP2.add(es);
		}
		
		int [] appear = new int[1500]; 
		
		for(int i = 0; i<TP2.size(); i++) {
			for(int j = 0; j<TP2.get(i).endPoints.size(); j++) {
				appear[TP2.get(i).endPoints.get(j).symbolid] = 1;
			}
		}

//		int [] appear = PredictSparse.patternSymbols();
		
		ArrayList<DataPoint> datas = new ArrayList<DataPoint>();
		
		sql = "select * from sequence22";
		
		rs = Gcenter.jdbc.query(sql);
				
		for(int i = 0; i<appear.length; i++) {
			if (appear[i] == 1) {
				sql = "select * from sequence22 where symbol = " + i;
				
				ResultSet rs3 = Gcenter.jdbc.query(sql);
				
				rs3.next();
				
				int seqid = rs3.getInt("seqid");
				int sameid = rs3.getInt("sameid");
				double lat = rs3.getDouble("lat");
				double lng = rs3.getDouble("lng");
				int G = rs3.getInt("G");
				String cate = rs3.getString("cate");
				String startTime = rs3.getString("time");
				String endTime = rs3.getString("endtime");
				
				DataPoint comp = new DataPoint(seqid,sameid, lat, lng, G, cate, startTime, endTime);
				
				comp.symbol = rs3.getInt("symbol");							
				
				rs3.close();
				
				sql = "select * from prefixcenter where symbolid = " + comp.symbol;

				ResultSet rs6 = Gcenter.jdbc.query(sql);
				
				rs6.next();
				
				comp.setDistributions(rs6.getString("gDistribution"), rs6.getString("timeDistribution"));
				rs6.close();
				
				datas.add(comp);
			}
		}
		
		ArrayList<ArrayList<Integer>> realTrajectory = ParseCheckin.findNearRaw();
		
		double mother = 0.0;
		double son = 0.0;
		
		for(int i = 0; i<realTrajectory.size(); i++) {
			int preId = realTrajectory.get(i).get(0);
			int nextId = realTrajectory.get(i).get(1);
			
			sql = "select * from raw2 where id = " + preId;
			
			rs = Gcenter.jdbc.query(sql);
			
			if (!rs.next()) {
				System.out.println("Error!!");
				System.exit(1);
			}
			
			int id = rs.getInt("id");
			int same = rs.getInt("same");
			double lat = rs.getDouble("lat");
			double lng = rs.getDouble("lng");
			int G = rs.getInt("G");
			String cate = rs.getString("cate");
			String startTime = rs.getString("date");
			String endTime = rs.getString("date");
			
			DataPoint temp = new DataPoint(id,same, lat, lng, G, cate, startTime, endTime);		
			
			rs.close();
			
			sql = "select * from raw2 where id = " + nextId;
			
			rs = Gcenter.jdbc.query(sql);
			
			if (!rs.next()) {
				System.out.println("Error!!");
				System.exit(1);
			}
			
			id = rs.getInt("id");
			same = rs.getInt("same");
			lat = rs.getDouble("lat");
			lng = rs.getDouble("lng");
			G = rs.getInt("G");
			cate = rs.getString("cate");
			startTime = rs.getString("date");
			endTime = rs.getString("date");
			
			DataPoint tempNext = new DataPoint(id,same, lat, lng, G, cate, startTime, endTime);		
			
			rs.close();
			
			int symbol1 = Coverage.findMostSim(datas, temp);
			int symbol2 = Coverage.findMostSim(datas, tempNext);
			
			System.out.println(preId + "->" + nextId + ": " + symbol1 + "->" + symbol2);
			
			if (symbol1 == -1 || symbol2 == -1) {
				continue;
			}
			
		//	boolean flag = false;
			boolean hit = false;
			for(int j = 0; j<TP2.size(); j++) {
				for(int k = 0; k<TP2.get(j).endPoints.size(); k++) {
					if (symbol1 == TP2.get(j).endPoints.get(k).symbolid) {
						hit = true;
						for(int q = k+1; q<TP2.get(j).endPoints.size(); q++) {
							if (symbol2 == TP2.get(j).endPoints.get(q).symbolid) {
						//		flag = true;
								son += 1;
								break;
							}
						}
					}	
					if (hit)
						break;
				}
				if (hit)
					break;
			}
			mother += 1;
		}
		
		System.out.println("son =" + son + ", mother = " + mother + "(" + (double)(son/mother) +")");
	}
}
