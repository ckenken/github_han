package com.ckenken.implement.run;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import lab.adsl.optics.Haversine;

import com.ckenken.implement.sparse.Gcenter;
import com.ckenken.implement.sparse.ParseCheckin;
import com.ckenken.implement.sparse.PredictSparse;
import com.ckenken.implement.storage.DataPoint;
import com.ckenken.io.JDBC;

public class TestTrajectory {
	
	public static void main(String[] args) throws SQLException, ParseException, IOException {
		
		if (Gcenter.jdbc == null || Gcenter.jdbc.con.isClosed()) {
			Gcenter.jdbc = new JDBC("han");
		}
		
		String sql = "select * from sequence22";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		PredictSparse.datas = new ArrayList<DataPoint>();
		
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
			
			PredictSparse.datas.add(temp);
		}		
		rs.close();
		
		
		ArrayList<ArrayList<Integer>> realTrajectory = ParseCheckin.findNearRaw();
		int sum = 0;
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
		
			int symbol1 = PredictSparse.findMostSim(temp);
			int symbol2 = PredictSparse.findMostSim(tempNext);
			
			if (symbol1 == symbol2 || symbol1 == -1 || symbol2 == -1)
				continue;
			
			System.out.print(preId + "->" + nextId +": " + symbol1 + "->" + symbol2 + " counter = ");
			
			String pa = symbol1 + "," + symbol2;
			
			sql = "select count(*) from datapattern where datapattern like '%" + pa + ",%'";
			
			rs = Gcenter.jdbc.query(sql);
			rs.next();
			if (rs.getInt("count(*)") > 0) {
				sum++;
			}
			System.out.println(rs.getInt("count(*)"));
		
		}
		System.out.println("sum = " + sum);
	}
}
