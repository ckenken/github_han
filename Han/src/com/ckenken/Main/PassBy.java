package com.ckenken.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import lab.adsl.object.Point;

import com.ckenken.io.JDBC;
import com.ckenken.storage.NewPoint;

public class PassBy {

	public static void main(String [] args) throws NumberFormatException, SQLException, ParseException 
	{
		countPassBy();
		
//		JDBC jdbc = new JDBC();
//		
//		String sql = "select * from raw2 where same != -1";
//		
//		ResultSet rs = jdbc.query(sql);
//		ResultSet rs2 = null;
//		
//		rs.next();
//		double lat = rs.getDouble("lat");
//		double lng = rs.getDouble("lng");
//		String time = rs.getString("date");
//		
//		Date d = Main_v2.parseDate(time);
//		NewPoint np = new NewPoint(d, lat, lng);
//		Point startPoint = new Point(0, np);
//		startPoint.same = Integer.parseInt(rs.getString("same"));
//		
//		System.out.println(Main_v2.sdFormat.format(startPoint.ckTime));		
//				
//		while(rs.next()) {
//			lat = Double.parseDouble(rs.getString("lat"));
//			lng = Double.parseDouble(rs.getString("lng"));
//			time = rs.getString("date");
//			d = Main_v2.parseDate(time);
//			np = new NewPoint(d, lat, lng);
//			Point p = new Point(0, np);
//			p.same = Integer.parseInt(rs.getString("same"));		
//			
//			if(p.same != startPoint.same) { 
//				
//				sql = "select * from same where sameid = " +  startPoint.same;
//				rs2 = jdbc.query(sql);
//				rs2.next();
//				
//				int appear = rs2.getInt("appear");
//				
//				sql = "update same set appear = " + (appear+1) + " where sameid = " +startPoint.same;
//				jdbc.insertQuery(sql);
//				
//				startPoint = p;
//			}
//			else  
//			{
//			}
//		}
		
	}
	
	public static void countPassBy() throws SQLException, ParseException
	{
		JDBC jdbc = new JDBC();
		
		String sql = "select * from raw2 where same != -1";
		
		ResultSet rs = jdbc.query(sql);
		ResultSet rs2 = null;
		
		rs.next();
		double lat = rs.getDouble("lat");
		double lng = rs.getDouble("lng");
		String time = rs.getString("date");
		
		Date d = Main_v2.parseDate(time);
		NewPoint np = new NewPoint(d, lat, lng);
		Point startPoint = new Point(0, np);
		startPoint.same = Integer.parseInt(rs.getString("same"));
		
		System.out.println(Main_v2.sdFormat.format(startPoint.ckTime));		
		
		Point previous = startPoint;
		
		while(rs.next()) {
			lat = Double.parseDouble(rs.getString("lat"));
			lng = Double.parseDouble(rs.getString("lng"));
			time = rs.getString("date");
			d = Main_v2.parseDate(time);
			np = new NewPoint(d, lat, lng);
			Point p = new Point(0, np);
			p.same = Integer.parseInt(rs.getString("same"));		
			
			if(p.same != startPoint.same) { 
				if (previous.ckTime.getTime() - startPoint.ckTime.getTime() <= 120000) { // passby
					
					System.out.println(startPoint.same + ": " + Main_v2.sdFormat.format(startPoint.ckTime) + "~" + Main_v2.sdFormat.format(previous.ckTime));
					
					sql = "select * from same where sameid = " + startPoint.same;
					rs2 = jdbc.query(sql);
					rs2.next();
					int passby = rs2.getInt("passby");
					sql = "update same set passby = " + (passby+1) + " where sameid = " + startPoint.same; 
					jdbc.insertQuery(sql);
				//	System.out.println(startPoint.same + " +1");
				}
				else {  // not passby  
					sql = "select * from same where sameid = " + startPoint.same;
					rs2 = jdbc.query(sql);
					rs2.next();
					int passby = rs2.getInt("passby");
					sql = "update same set passby = " + (passby-1) + " where sameid = " + startPoint.same; 
					jdbc.insertQuery(sql);					
		//			System.out.println(startPoint.same + " -1");
				}
				startPoint = p;
			}
			else  
			{
				
			}
			previous = p;
		}
	}
	
}
