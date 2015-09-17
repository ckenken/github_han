package com.ckenken.implement.run;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import com.ckenken.implement.storage.DataPoint;
import com.ckenken.io.JDBC;

public class ErrorRate {
	
	public static void main(String[] args) throws SQLException, ParseException, IOException {		
		
		ArrayList<DataPoint> originalDatas = new ArrayList<DataPoint>();
		
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
		
		for(int i = 0; i<datas.size(); i++) {
			originalDatas.add(DataPoint.copy(datas.get(i)));
		}
		
		HashMap<Integer, Integer> map = IndexSymbol.symbolizeByHotRegion(datas, IndexSymbol.SS);
		
		for(int i = 0; i<datas.size(); i++) {
			datas.get(i).symbol = map.get(datas.get(i).seqid);
		}
		
		for(int i = 0; i<datas.size(); i++) {
		//	System.out.println(datas.get(i).seqid + ": " + datas.get(i).symbol);
			
			sql = "update sequence30_training set symbol = " + datas.get(i).symbol + " where seqid = " + datas.get(i).seqid;
			jdbc.insertQuery(sql);	
		}

		///////// merged HASH ////////
		
		sql = "select max(symbol) from sequence30_training";
		
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
		double sum = 0.0;
		for(int i = 0; i<datas.size(); i++) {
			DataPoint a = DataPoint.copy(datas.get(i));
			DataPoint b = DataPoint.copy(originalDatas.get(i));
			double loss = 0;
		//	if (DataPoint.similarity_cos(a, b) <= 10) {
				loss = 1.0 - DataPoint.similarity_cos_v2(a, b);
	//		}
			sum += loss;					
		}
		
		double totalLoss = sum / (double)datas.size();
		
		System.out.println("totalLoss = " + totalLoss);
		
	}
}
