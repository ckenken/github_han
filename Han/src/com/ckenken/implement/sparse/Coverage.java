package com.ckenken.implement.sparse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import com.ckenken.implement.run.IM_Main;
import com.ckenken.implement.storage.DataPoint;
import com.ckenken.io.JDBC;

public class Coverage {
	final public static String OUTPUT = "Coverage_77.txt"; 
	
	public static int findMostSim(ArrayList<DataPoint> datas, DataPoint temp) 
	{
		double min = 20000000.0;
		int minI = -1;
		for(int i = 0; i<datas.size(); i++) {
			double score = DataPoint.similarity_cos(datas.get(i), temp);
			
			if (score != IM_Main.NOT_SIM)
				score = 1-score;
			
			if(score != IM_Main.NOT_SIM && score < min) {
				minI = i;
				min = score;
			}
		}
		
		if(minI == -1) {
//			System.out.println(temp.seqid + " no sim point!");
			return -1;
		}
//		int minSymbol = datas.get(minI).symbol;
		
		int minSymbol = datas.get(minI).symbol;

		return minSymbol;
	}
	
	public static void main(String[] args) throws SQLException, ParseException, IOException {
		
		PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
		System.setOut(outstream);
		
		if (Gcenter.jdbc == null || Gcenter.jdbc.con.isClosed()) {
			Gcenter.jdbc = new JDBC("han");
		}
	
		ArrayList<EndSequence> TP2 = TPMiner.runTPMiner(TPMiner.NO_INSERT);
		
		int [] appear = new int[1500]; 

//		for(int i = 0; i<1049; i++) {
//			appear[i] = 1;
//		}
		
		for(int i = 0; i<TP2.size(); i++) {
			for(int j = 0; j<TP2.get(i).endPoints.size(); j++) {
				appear[TP2.get(i).endPoints.get(j).symbolid] = 1;
			}
		}

//		int [] appear = PredictSparse.patternSymbols();
		
		ArrayList<DataPoint> datas = new ArrayList<DataPoint>();
		
		String sql = "select * from sequence22";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
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
//		rs.close();
		
		
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
			
			int symbol1 = findMostSim(datas, temp);
			int symbol2 = findMostSim(datas, tempNext);
			
			System.out.println(preId + "->" + nextId + ": " + symbol1 + "->" + symbol2);
			
			if (symbol1 == -1 || symbol2 == -1) {
				continue;
			}
			
			boolean flag = false;
			for(int j = 0; j<TP2.size(); j++) {
				for(int k = 0; k<TP2.get(j).endPoints.size(); k++) {
					if (symbol1 == TP2.get(j).endPoints.get(k).symbolid) {
						for(int q = k+1; q<TP2.get(j).endPoints.size(); q++) {
							if (symbol2 == TP2.get(j).endPoints.get(q).symbolid) {
								flag = true;
								son += 1;
								break;
							}
						}
					}	
					if (flag)
						break;
				}
				if (flag)
					break;
			}
			mother += 1;
		}
		
		System.out.println("son =" + son + ", mother = " + mother + "(" + (double)(son/mother) +")");
	}
}
