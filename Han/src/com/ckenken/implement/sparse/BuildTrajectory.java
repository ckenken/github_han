package com.ckenken.implement.sparse;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;

import lab.adsl.optics.Haversine;

import com.ckenken.Main.Main_v2;
import com.ckenken.implement.storage.DataPoint;
import com.ckenken.implement.storage.DataSequence;
import com.ckenken.io.JDBC;

public class BuildTrajectory {
	
	final private static String OUTPUT = "buildTrajectoryXXX.txt";  
	public static ArrayList<DataSequence> origin;
	public static int max_seqid = 0;
	public static int max_trajectory_seqid = 0;
	
	final public static double GAP_DISTANCE = 500.0;
	
	public static ArrayList<ArrayList<Integer>> getDayArray(String date) throws SQLException
	{
		ArrayList<ArrayList<Integer>> day = new ArrayList<ArrayList<Integer>>();
	
		for(int i = 0; i<24; i++) {
			ArrayList<Integer> temp = new ArrayList<Integer>();
			day.add(temp);
		}
		
		String sql = "select * from raw2 where day = '" + date + "' and same != -1";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		while(rs.next()) {
			int gid = rs.getInt("g");
			int timestamp = rs.getInt("timestamp");
			
			if (!day.get(timestamp).contains(gid)) {
				day.get(timestamp).add(gid);
			}
		}
		rs.close();
		
		return day;
	}
	
	public static ArrayList<ArrayList<EndPoint>> getDaySymbolArray(String date) throws SQLException, ParseException
	{
		ArrayList<ArrayList<EndPoint>> day = new ArrayList<ArrayList<EndPoint>>();
	
		for(int i = 0; i<24; i++) {
			ArrayList<EndPoint> temp = new ArrayList<EndPoint>();
			day.add(temp);
		}
		
		String sql = "select * from sequence22 where day = '" + date + "'";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		while(rs.next()) {
			int symbolid = rs.getInt("symbol");
			Date startTime = Main_v2.sdFormat.parse(rs.getString("time"));
			Date endTime = Main_v2.sdFormat.parse(rs.getString("endTime"));
			
			EndPoint tempStep = new EndPoint(symbolid, EndPoint.PLUS, -1);
			
			for(int i = startTime.getHours(); i<=endTime.getHours(); i++) {
				if (!day.get(i).contains(tempStep)) {
					day.get(i).add(tempStep);
				}	
			}
		}
		rs.close();
		
		return day;
	}
	
	public static ArrayList<DataSequence> createOrigin() throws SQLException, ParseException 
	{
		BuildTrajectory.origin = new ArrayList<DataSequence>();
		
		String sql = "select distinct day from sequence22";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		while(rs.next()) {
			
			String date = rs.getString("day");
			
		//	date = "1-27";
			
			ArrayList<ArrayList<EndPoint>> day = getDaySymbolArray(date);
			System.out.println("========= " + date + " =========");
			
			printDay(day);
			
			for(int i = 0; i<day.size()-1; i++) { // 0~22, 23 is not considered
				ArrayList<EndPoint> hour = day.get(i);
				for(int j = 0; j<hour.size(); j++) {
					
					ArrayList<DataPoint> stack = new ArrayList<DataPoint>();
					
					ArrayList<EndPoint> ahead = day.get(i+1);
					
					boolean flag = false;
					for(int k = 0; k<ahead.size(); k++) {
						if (ahead.get(k).symbolid == hour.get(j).symbolid) {
							flag = true;
							break;
						}
					}
					
					if (flag) {  // if not the end of symbol then dont start recursive
						continue;
					}
					
					recursiveGetDataSequences(day, hour.get(j).symbolid, stack, i, date);			
				
				}
			}
		}
		rs.close();
		
		return BuildTrajectory.origin;
	}
	
	public static void recursiveGetDataSequences(ArrayList<ArrayList<EndPoint>> day, int nowSymbol,  ArrayList<DataPoint> stack, int nowTimestamp, String date) throws ParseException, SQLException
	{
		
//		System.out.println(nowSymbol + "(" + nowTimestamp +")");
		
		DataPoint temp = new DataPoint();
		temp.symbol = nowSymbol;
		
		String start = "2011-" + date + "T" + nowTimestamp + ":30:30";
		String end = "2011-" + date + "T" + nowTimestamp + ":30:30";
		
		Date d = Main_v2.parseDate(start);
		temp.startTime = d;
		
		d = Main_v2.parseDate(end);
		temp.endTime = d;
		temp.seqid = max_trajectory_seqid++;
		
		stack.add(temp);	
//		System.out.println("Add " + temp.symbol + "(" + temp.startTime.getHours()+ ") !!!");
	
		if (nowTimestamp >= 23) {  // end of day
			if (stack.size() >= 2) {
				DataSequence D = new DataSequence(BuildTrajectory.getMergedStack(stack));
				BuildTrajectory.origin.add(D);
				
//				for(int i = 0; i<stack.size(); i++) {
//					System.out.print(stack.get(i).symbol + "(" + stack.get(i).startTime.getHours() +")");
//				}
//				System.out.println();
//				System.out.println("-----");
			}
			return;		
		}
		
		boolean sameFlag = false;
		for(int i = 0; i<day.get(nowTimestamp+1).size(); i++) {
			if (day.get(nowTimestamp+1).get(i).symbolid == nowSymbol) {
				sameFlag = true;
				recursiveGetDataSequences(day, day.get(nowTimestamp+1).get(i).symbolid, stack, nowTimestamp+1, date);
				if (stack.size() >= 1) {
					stack.remove(stack.size()-1);
				}
				break;
			}
		}
		
		if (sameFlag) {
			return;
		}
		
		double nowLat = 0.0;
		double nowLng = 0.0;
		
		String sql = "select * from prefixcenter where symbolid = " + nowSymbol;
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		if (rs.next()) {
			nowLat = rs.getDouble("lat");
			nowLng = rs.getDouble("lng");
		}
		else {
			System.out.println("Error !!!!!");			
			System.exit(1);
		}
		rs.close();	
		
		boolean flag = false;
		
		for(int i = 0; i<day.get(nowTimestamp+1).size(); i++) {
			
			double nextLat = 0.0;
			double nextLng = 0.0;

			sql = "select * from prefixcenter where symbolid = " + day.get(nowTimestamp+1).get(i).symbolid ;
			
			rs = Gcenter.jdbc.query(sql);
			
			if (rs.next()) {
				nextLat = rs.getDouble("lat");
				nextLng = rs.getDouble("lng");
			}
			else {
				System.out.println("Error !!!!!");			
				System.exit(1);
			}
			rs.close();
			
			double dis = Haversine.getDistanceDouble(nowLat, nowLng, nextLat, nextLng);
			
			if (dis <= BuildTrajectory.GAP_DISTANCE) {
				flag = true;
//				System.out.println("Add " + temp.symbol + "(" + temp.startTime.getHours()+ ") !!!!!!");
				recursiveGetDataSequences(day, day.get(nowTimestamp+1).get(i).symbolid, stack, nowTimestamp+1, date);
				if (stack.size() >= 1) {
					stack.remove(stack.size()-1);
				}
			}
		}
		if (!flag) {  // no next symbol near nowSymbol
			if (stack.size() >= 2) {
				DataSequence D = new DataSequence(BuildTrajectory.getMergedStack(stack));
				BuildTrajectory.origin.add(D);
//				System.out.println("-----");
			}
			return;		
		}
		
	}

	public static ArrayList<DataPoint> getMergedStack(ArrayList<DataPoint> stack)
	{
		ArrayList<DataPoint> temp = new ArrayList<DataPoint>();
		
		DataPoint d = DataPoint.copy(stack.get(0));
		temp.add(d);
		int j = 1;
		for(int i = 1; i<stack.size(); i++) {
			d = DataPoint.copy(stack.get(i));
			if (temp.get(j-1).symbol == d.symbol) {
				temp.get(j-1).endTime = d.endTime;
			}
			else {
				temp.add(d);
				j++;
			}
		}
		
		return temp;
	}
	
	public static void printDay(ArrayList<ArrayList<EndPoint>> day) 
	{
		for(int i = 0; i<day.size(); i++) {
			ArrayList<EndPoint> hour = day.get(i);
			System.out.print(i + ": ");
			for(int j = 0; j<hour.size(); j++) {
				if (j == 0)
					System.out.print(hour.get(j).symbolid);
				else 
					System.out.print(", " + hour.get(j).symbolid);
			}
			System.out.println();
		}	
	}
	
	public static void createSequence22() throws SQLException, ParseException
	{
		String sql = "select distinct day from raw2 where same != -1";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		ArrayList<DataPoint> datas = new ArrayList<DataPoint>();
		
		while(rs.next()) {
			
			String date = rs.getString("day");
			
			ArrayList<ArrayList<Integer>> day = getDayArray(date);
		
			for(int i = 0; i<day.size(); i++) { // day size = 24
				ArrayList<Integer> hour = day.get(i);
				
				for(int j = 0; j<hour.size(); j++) {
					DataPoint data = recursiveGetItem(day, hour.get(j), i, i, date);
					
			//		data.seqid = BuildTrajectory.max_seqid++;
					data.sameid = -1;
					data.G = hour.get(j);
					data.day = date;
					
					sql = "select * from gcenter where Gid = " + data.G;
					
					ResultSet rs2 = Gcenter.jdbc.query(sql);
					
					if (rs2.next()) {
						double lat = rs2.getDouble("lat");
						double lng = rs2.getDouble("lng");
						String cate = rs2.getString("cate");
						data.cate = cate;
						data.lat = lat;
						data.lng = lng;
					}
					else {
						System.out.println("Error!!!");
						System.exit(1);
					}
					rs2.close();
					
					datas.add(data);
					
					int start = i;
					int end = data.endTime.getHours();
					
					for(int k = start; k<=end; k++) {
						day.get(k).remove(new Integer(data.G));
					} 
				}
			}
			
			for(int i = 0; i<datas.size(); i++) {
				DataPoint d = DataPoint.copy(datas.get(i));
				sql = "insert into sequence22 values(" + BuildTrajectory.max_seqid + ",-1," + d.lat + "," + d.lng +"," + d.G + ",'" + d.cate + "','" + Main_v2.sdFormat.format(d.startTime) + "','" + Main_v2.sdFormat.format(d.endTime) + "',0,'" + d.day + "')";
				BuildTrajectory.max_seqid++;
				Gcenter.jdbc.insertQuery(sql);
			}
			datas.clear();
		}
		rs.close();
		
	}
	
	public static DataPoint recursiveGetItem(ArrayList<ArrayList<Integer>> day, int gid, int startTimestamp, int nowTimestamp, String date) throws ParseException
	{
		DataPoint temp = new DataPoint();
		
		// 2011-12-08T20:53:52
		
		String start = "2011-" + date + "T" + startTimestamp + ":22:22";
		String end = "2011-" + date + "T" + nowTimestamp + ":22:22";
		
		Date d = Main_v2.parseDate(start);
		temp.startTime = d;
		
		d = Main_v2.parseDate(end);
		temp.endTime = d;
		
		if (nowTimestamp == 23) {
			return temp;
		}
		
		if (day.get(nowTimestamp+1).contains(gid)) { // have this gid in next hour
			temp = recursiveGetItem(day, gid, startTimestamp, nowTimestamp+1, date);
		}
		
		return temp;
	}
	
	public static void printDistance() throws SQLException
	{
		String sql = "select * from gcenter";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		ArrayList<Gcenter> gcenters = new ArrayList<Gcenter>();
		
		while(rs.next()) {
			Gcenter g = new Gcenter(rs.getInt("Gid"), 0, rs.getDouble("lat"), rs.getDouble("lng")); 
			gcenters.add(g);
		}
		
		for(int i = 0; i<gcenters.size(); i++) {
			for(int j = i+1; j<gcenters.size(); j++) {
				if (Gcenter.distance(gcenters.get(i), gcenters.get(j)) < 1000) {
					System.out.println(i + "<->" + j + ": " + Gcenter.distance(gcenters.get(i), gcenters.get(j)));	
				}
			}
		}
	}
	
	public static void main(String[] args) throws SQLException, FileNotFoundException, ParseException 
	{
		
		PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
		System.setOut(outstream);		
		
		BuildTrajectory.max_seqid = 0;
		
		Gcenter.jdbc = new JDBC("han");		
		
//		day = new ArrayList<ArrayList<Integer>>();
				
	//	printDistance();
			
		createSequence22();
		
//		createOrigin();
//		
//		for(int i = 0; i<origin.size(); i++) {
//			for(int j = 0; j<origin.get(i).dataPoints.size(); j++) {
//				if (j == 0)
//					System.out.print(origin.get(i).dataPoints.get(j).symbol + "(" + origin.get(i).dataPoints.get(j).startTime.getHours() + "~" + origin.get(i).dataPoints.get(j).endTime.getHours() + ")");
//				else 
//					System.out.print(", " + origin.get(i).dataPoints.get(j).symbol + "(" + origin.get(i).dataPoints.get(j).startTime.getHours() + "~" + origin.get(i).dataPoints.get(j).endTime.getHours() + ")");
//			}
//			System.out.println();
//		}
//		
	}
}
