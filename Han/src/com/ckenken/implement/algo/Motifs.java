package com.ckenken.implement.algo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import com.ckenken.implement.storage.DataPoint;
import com.ckenken.io.JDBC;

public class Motifs {
	public static void main(String [] args) throws SQLException, ParseException, IOException
	{
		JDBC jdbc = new JDBC();
		
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
			
			DataPoint temp = new DataPoint(seqid,sameid, lat, lng, G, cate, startTime, endTime);
			
			datas.add(temp);
		}
//		
//		for(int i = 0; i<datas.size(); i++) {
//			System.out.println(Main_v2.sdFormat.format(datas.get(i).startTime));
//			System.out.println((datas.get(i).startTime.getHours()) + ":" + datas.get(i).startTime.getMinutes() + ":" + datas.get(i).startTime.getSeconds());
//		
//		}
		
		
//		DataPoint o1 = datas.get(214);
//		DataPoint o2 = datas.get(608);
//		
//		if(DataPoint.same(o1, o2)) {
//			System.out.println("same");
//		}
		
		for(int j = 0; j<datas.size(); j++) {
			DataPoint one = datas.get(j);
			for(int i = j+1; i<datas.size(); i++) {
				if(i != j && DataPoint.same(one, datas.get(i)) && one.sameid == 4) {
					System.out.println(one.seqid + " is same to " + datas.get(i).seqid);
				}
			}	
		}
		

		
		
	}
}
