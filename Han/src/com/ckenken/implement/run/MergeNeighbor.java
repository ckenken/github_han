package com.ckenken.implement.run;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

import com.ckenken.Main.Main_v2;
import com.ckenken.implement.storage.DataPoint;
import com.ckenken.io.JDBC;

public class MergeNeighbor {
	
	public static void main(String[] args) throws SQLException, ParseException, IOException {
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from sequence30" + args[0];
		
		ResultSet rs = jdbc.query(sql);
		
		rs.next();
		
		int seqid = rs.getInt("seqid");
		int sameid = rs.getInt("sameid");
		double lat = rs.getDouble("lat");
		double lng = rs.getDouble("lng");
		int G = rs.getInt("G");
		String cate = rs.getString("cate");
		String startTime = rs.getString("time");
		String endTime = rs.getString("endtime");
		
		DataPoint previous = new DataPoint(seqid, sameid, lat, lng, G, cate, startTime, endTime);
		
		previous.symbol = rs.getInt("symbol");
		
		while(rs.next()) 
		{
			seqid = rs.getInt("seqid");
			sameid = rs.getInt("sameid");
			lat = rs.getDouble("lat");
			lng = rs.getDouble("lng");
			G = rs.getInt("G");
			cate = rs.getString("cate");
			startTime = rs.getString("time");
			endTime = rs.getString("endtime");
			
			DataPoint temp = new DataPoint(seqid, sameid, lat, lng, G, cate, startTime, endTime);
			
			temp.symbol = rs.getInt("symbol");
			
			if(previous.symbol != temp.symbol) {
				
		//		JDBC jdbc2 = new JDBC("history4");
				sql = "insert into merged_sequence30" + args[0] + " values(" + previous.seqid +  "," + previous.sameid + "," + previous.lat + "," + previous.lng + "," + previous.G + ",'" + previous.cate + "','" +  Main_v2.sdFormat.format(previous.startTime) +  "','" +  Main_v2.sdFormat.format(previous.endTime) + "'," + previous.symbol + ")";				
		
		//		System.out.println(sql);
				
				jdbc.insertQuery(sql);
				previous = temp;
			}
			else {
				DataPoint merge = new DataPoint();				
				
				merge.seqid = previous.seqid;
				merge.lat = ((previous.lat + temp.lat)/2.0);
				merge.lng = ((previous.lng + temp.lng)/2.0);
				merge.cate = previous.cate;
				merge.G = previous.G;
				merge.sameid = previous.sameid + 1000;
				merge.startTime = previous.startTime;
				merge.endTime = temp.endTime;
				merge.symbol = previous.symbol;
				previous = merge;
			}
			
		}
	}
}
