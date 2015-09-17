package com.ckenken.Main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import lab.adsl.object.Point;

import com.ckenken.algo.Coarse;
import com.ckenken.algo.MeanShift;
import com.ckenken.io.JDBC;
import com.ckenken.storage.Coarse_pattern;
import com.ckenken.storage.NewPoint;
import com.ckenken.storage.Sequence;

public class Main_v2 {
	public static SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	static private final String OUTPUT = "output_3600_10.txt";  
	
	public static final int SIGMA = 15;   // min_sup
	public static final int DT = 3600000;  
	public static final int MAX_POINT_NUM = 110;  // 0~117
	
	public static int [][] distribution;
	
	public static void main(String [] args) throws SQLException, ParseException, FileNotFoundException
	{
		PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
		System.setOut(outstream);
		
//		create_sequence30("han");
	
//		timeDistribution();
		
		ArrayList<Coarse_pattern> cps = new ArrayList<Coarse_pattern>();
		
		ArrayList<Sequence> origin = cut();
	
//		for(int i = 0; i<origin.size(); i++) {
//			System.out.println("o" + origin.get(i).objectID + ":");
//			for(int j = 0; j<origin.get(i).points.size(); j++) {
//				origin.get(i).points.get(j).show();
//			}
//		}

		JDBC jdbc = new JDBC("han");
		
		String sql = "select max(g) from sequence30";
		
		ResultSet rs = jdbc.query(sql);
		
		rs.next();
		
		Coarse.max_g_num = rs.getInt("max(g)");
		Coarse.extended = new ArrayList<Sequence>();
		
//		ArrayList<Sequence> display_extended = new ArrayList<Sequence>();
		
		ArrayList<ArrayList<Integer>> new_G_sequence_ids = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> old_G_sequence_ids = new ArrayList<ArrayList<Integer>>();
		
		int counter = 0;
		for(int i = 0; i<=Coarse.max_g_num; i++) {
			for(int j = 0; j<=Coarse.max_g_num; j++) {
				if(i == j)
					continue;
				
			//	System.out.println(i + "," + j + ":");
				
				ArrayList<Sequence> extended = Coarse.extend(origin, i);
				
				
//				for(int k = 0; k<extended.size(); k++) {
//					System.out.print(extended.get(k).objectID + " ");
//					extended.get(k).show();
//				}
//				
			//	System.out.println("extended!");
				
			//	if(extended.size() == 0)
		//			System.out.println("ex size = 0");
				
				
				Coarse_pattern cp = Coarse.testCoarse(extended, i, j, DT, SIGMA);
				if(cp != null) {
					cp.meanShiftClustering();
					cp.show();
					cp.insertCoarse();
					counter++;
					System.out.println("====");
					
					old_G_sequence_ids.add(cp.G_sequence_ids);
					
//					if(i == 0 && j == 1)
//					{
//						display_extended = Coarse.extendBySequence(origin, cp.G_sequence_ids, 3600000);
//					}
				}	
			}
		}
		
		System.out.println(counter);
		
//		for(int i = 0; i<display_extended.size(); i++) {
//			display_extended.get(i).show();
//		}
//		
		for(int k = 3; k<5; k++) {
			
			System.out.println("============== length = " + k + " ====================");
			counter = 0;
			for(int i = 0; i<old_G_sequence_ids.size(); i++) {
				for(int j = 0; j<Coarse.max_g_num; j++) {
					Coarse.extended = new ArrayList<Sequence>();
					ArrayList<Sequence> extended = Coarse.extendBySequence(origin, old_G_sequence_ids.get(i), DT);
					
//					if(k == 4) {
//						for(int q = 0; q<extended.size(); q++) {
//							System.out.print(extended.get(q).objectID + " ");
//							extended.get(q).show();
//							System.out.println();
//						}
//						break;
//					}
//					
					if(extended.size() == 0) {
					//	System.out.println("extend size = 0");
						continue;
					}
					Coarse_pattern cp = Coarse.testCoarseExtendLength(extended, k, old_G_sequence_ids.get(i).get(k-2), j, DT, SIGMA);
					if(cp != null) {
						cp.meanShiftClustering();
						
						cp.show();
						cp.insertCoarse();
						counter++;
						System.out.println("====");
						new_G_sequence_ids.add(cp.G_sequence_ids);
						
				//		MeanShift.meanShiftClustering(cp);
					}				
				}
			}
			System.out.println("counter = " + counter);
			
			//  ============== ending initialize ===============
			old_G_sequence_ids.clear();
			old_G_sequence_ids = new ArrayList<ArrayList<Integer>>();
			for(int i = 0; i<new_G_sequence_ids.size(); i++) {
				ArrayList<Integer> tempGseq = new ArrayList<Integer>();
				
				for(int j = 0; j<new_G_sequence_ids.get(i).size(); j++) {
					int temp = new_G_sequence_ids.get(i).get(j);
					tempGseq.add(temp);
				}
				
				old_G_sequence_ids.add(tempGseq);
			}
			new_G_sequence_ids.clear();
			new_G_sequence_ids = new ArrayList<ArrayList<Integer>>();
		}
		
	}
	
	public static void create_sequence30(String DB_name) throws SQLException, ParseException
	{
		Sequence all = new Sequence();
		
		JDBC jdbc = new JDBC(DB_name);
		
		String sql = "select * from raw2 where same != -1";
		
		ResultSet rs = jdbc.query(sql);
		
		rs.next();
		double lat = rs.getDouble("lat");
		double lng = rs.getDouble("lng");
		String time = rs.getString("date");
		
		Date d = parseDate(time);
		NewPoint np = new NewPoint(d, lat, lng);
		Point startPoint = new Point(0, np);
		startPoint.same = Integer.parseInt(rs.getString("same"));
		
		int idCounter = 0;
		
		System.out.println(sdFormat.format(startPoint.ckTime));
		
		Point previous = startPoint;
		
		while(rs.next()) {
			lat = Double.parseDouble(rs.getString("lat"));
			lng = Double.parseDouble(rs.getString("lng"));
			time = rs.getString("date");
			d = parseDate(time);
			np = new NewPoint(d, lat, lng);
			Point p = new Point(0, np);
			p.same = Integer.parseInt(rs.getString("same"));		
			
			if(p.same != startPoint.same) { // ���P same ������I�A�n�����I(e.g. p1)�������I�Atime = startPoint.time�A�é�J Sequence
			
				NewPoint newP = Coarse.getSameCenterById(startPoint.same, DB_name);
				newP.setTime(startPoint.ckTime);
				Point temp = new Point(idCounter, newP);
				temp.endTime = previous.ckTime;
				all.points.add(temp);
				idCounter++;
				startPoint = p;
			}
			else // ���O����I 
			{
				
			}
			previous = p;
		}
		
		
		for(int i = 0; i<all.points.size(); i++) {
			Point p = all.points.get(i);
			System.out.println(p.id + ": " + p.lat + "," + p.lng + " " + sdFormat.format(p.ckTime));
			sql = "select * from same where lat = " + p.lat + " and lng = " + p.lng;

			rs = jdbc.query(sql);
			rs.next();
			
			sql = "insert into sequence30 values(" + p.id + "," + rs.getInt("sameid") + "," + p.lat + "," + p.lng + ",'" + rs.getString("G") + "','" +  rs.getString("cate") + "','" + sdFormat.format(p.ckTime) + "','" + sdFormat.format(p.endTime) + "', -1)";
			
			jdbc.insertQuery(sql);
			
		}
	}
	
	
	public static Date parseDate(String time) throws ParseException
	{
		sdFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		Date d = sdFormat.parse(time);
		
		return d;
	}
	
	@SuppressWarnings({ "deprecation" })
	public static ArrayList<Sequence> cut() throws SQLException, ParseException 
	{
		ArrayList<Sequence> origin = new ArrayList<Sequence>();
		
		JDBC jdbc = new JDBC("han");
		String sql = "select * from sequence30";
		
		ResultSet rs = jdbc.query(sql);
		
		ArrayList<Point> temp = new ArrayList<Point>();
		
		rs.next();
		
		NewPoint np = new NewPoint(Main.parseDate(rs.getString("time")), rs.getDouble("lat"), rs.getDouble("lng"), rs.getString("cate"));
		Point p = new Point((long)rs.getInt("sameid"), np);
		p.Gid = rs.getInt("G");
		temp.add(p);
		
		//System.out.println(p.ckTime.getDay());
		
		int previous = p.ckTime.getDay();
		int objectCounter = 0;
		
		while(rs.next()) {
			np = new NewPoint(Main.parseDate(rs.getString("time")), rs.getDouble("lat"), rs.getDouble("lng"), rs.getString("cate"));
			p = new Point((long)rs.getInt("sameid"), np);
			p.Gid = rs.getInt("G");
			
	//		p.show();
			
			if(previous == p.ckTime.getDay()) {   // same day
				temp.add(p);
			}
			else // move day 
			{
				Sequence seq = new Sequence(temp);
				seq.objectID = objectCounter;
				origin.add(seq);
				objectCounter++;
								
//				System.out.println(objectCounter + ":");
//				for(int i = 0; i<temp.size(); i++)	
//					temp.get(i).show();
				
				temp.clear();
				temp = new ArrayList<Point>();
				temp.add(p);
			
//				System.out.println(objectCounter + ":");
//				for(int i = 0; i<temp.size(); i++)	
//					temp.get(i).show();
//				System.out.println("");				
			}
			previous = p.ckTime.getDay();
		}
		
		return origin;
	}
	
	public static void timeDistribution() throws SQLException, ParseException
	{
		distribution = new int[MAX_POINT_NUM][24];
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from sequence30";
		
		ResultSet rs = jdbc.query(sql);
		
		while(rs.next()) {
			int same = rs.getInt("sameid");
			Date startTime = sdFormat.parse(rs.getString("time"));
			Date endTime = sdFormat.parse(rs.getString("endtime"));
			
//			System.out.println(startTime.getHours());
//			System.out.println(endTime.getHours());

			for(int i = startTime.getHours(); i<=endTime.getHours(); i++) {
				distribution[same][i]++;
			}
		}
		
		sql = "select max(sameid) from same";
		
		rs = jdbc.query(sql);
		
		rs.next();
		
		int max_point_num = rs.getInt("max(sameid)");
		
		for(int i = 0; i<=max_point_num; i++) {
			System.out.println(i + ":");
			StringBuilder SB = new StringBuilder();
			for(int j = 0; j<24; j++) {
				System.out.println(j + ": " + distribution[i][j]);
				if(j == 0)
					SB.append(j + ":" + distribution[i][j]);
				else 
					SB.append("\n" + j + ":" + distribution[i][j] );
			}
			sql = "insert into timedistribution values(" + i + ",'" + SB.toString() + "')";
			jdbc.insertQuery(sql);
			System.out.println("=======");
		}
	}
	
	public static void mergeTimeDistribution() throws SQLException
	{
		JDBC jdbc = new JDBC("han");
		
		String sql = "select max(gid) from gcenter";
		
		ResultSet rs = jdbc.query(sql);
		
		rs.next();
		
		int max_g_num = rs.getInt("max(gid)");
		
		for(int i = 0; i<=max_g_num; i++) {
			
			sql = "select sameid from same where g = " + i;
			
			rs = jdbc.query(sql);
			
			int [] timeCounter = new int[24];
			
			System.out.println("g = " + i);
			
			while(rs.next()) 
			{
				int sameid = rs.getInt("sameid");
				
				System.out.println("samdid = " + sameid);
				
				sql = "select * from timeDistribution where sameid = " + sameid;
				ResultSet rs2 = jdbc.query(sql);
				
				rs2.next();
				
				String timeDistribution = rs2.getString("distribution");
				
				String [] SP = timeDistribution.split("\n");
				
				for(int j = 0; j<24; j++) {
					String [] SP2 = SP[j].split(":");
					// 0 = index , 1 = counter;
					timeCounter[j] += Integer.parseInt(SP2[1]);
					
				}
			}
			
			StringBuilder SB = new StringBuilder();
			
			for(int j = 0; j<24; j++) {
				System.out.println(j + ": " + timeCounter[j]);
				if(j == 0)
					SB.append(j + ":" + timeCounter[j]);
				else 
					SB.append("\n" + j + ":" + timeCounter[j] );
			}
			
			sql = "insert into g_timedistribution values(" + i + ",'" + SB.toString() + "')";
			jdbc.insertQuery(sql);			
			
		}
	}
	
}
