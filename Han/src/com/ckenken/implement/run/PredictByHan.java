package com.ckenken.implement.run;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import com.ckenken.Main.Main_v2;
import com.ckenken.implement.storage.DataPoint;
import com.ckenken.io.JDBC;

public class PredictByHan {

	private static final String OUTPUT = "predict_han_output.txt";  
	
	public static void main(String[] args) throws SQLException, ParseException, IOException {
//		PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
//		System.setOut(outstream);		
//		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from sequence30_training";
		
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
			
			temp.symbol = rs.getInt("symbol");
			
			datas.add(temp);
		}
		
		sql = "select * from sequence30 where seqid > 904";
		
		rs = jdbc.query(sql);
		
		int correct = 0;
		int fault = 0;
		
		double sum = 0;
		
		while(rs.next()) 
		{
			int seqid = rs.getInt("seqid");
			int sameid = rs.getInt("sameid");
			double lat = rs.getDouble("lat");
			double lng = rs.getDouble("lng");
			int G = rs.getInt("G");
			String cate = rs.getString("cate");
			String startTime = rs.getString("time");
			String endTime = rs.getString("endtime");
			
			DataPoint temp = new DataPoint(seqid,sameid, lat, lng, G, cate, startTime, endTime);			
			
	//		System.out.println(temp.seqid + "'s most sim point = " + datas.get(minI).seqid + ", symbol = "+ datas.get(minI).symbol + ", sim = " + min);
			
			System.gc();
			
			sql = "select * from coarse order by wightsum desc";
			
			ResultSet rs2 = jdbc.query(sql);
			
			int next = -1;
			
			while(rs2.next()) {
				String pattern = rs2.getString("coarsepattern");
				
				String [] SP = pattern.split(",");
				
				for(int j = 0; j<SP.length; j++) {
					if (Integer.parseInt(SP[j]) == temp.G && j != (SP.length-1)) { // find "most next frequent point" and not final point in sequence
						next = Integer.parseInt(SP[j+1]);
						break;
					}
				}
				if(next != -1)
					break;
			}
			rs2.close();

			
			if(next != -1) {
				System.out.println("predict " + temp.seqid + "'s next = " + next);
			
				sql = "select * from sequence30_training where g = " + next;
				
				ResultSet rs3 = jdbc.query(sql);
				
				rs3.next();
//				int G_number = rs3.getInt("G");c
//				System.out.println("G = " + G_number);
				
				seqid = rs3.getInt("seqid");
				sameid = rs3.getInt("sameid");
				lat = rs3.getDouble("lat");
				lng = rs3.getDouble("lng");
				G = rs3.getInt("G");
				cate = rs3.getString("cate");
				startTime = rs3.getString("time");
				endTime = rs3.getString("endtime");
				
				DataPoint comp = new DataPoint(seqid,sameid, lat, lng, G, cate, startTime, endTime);
				
				comp.symbol = rs.getInt("symbol");							
				
				if(temp.seqid != 994) {
					sql = "select * from sequence30 where seqid > " + (temp.seqid);
					System.out.println(sql);
					ResultSet rs4 = jdbc.query(sql);
					
					boolean flag = false;
					
					while(rs4.next()) {
						
						seqid = rs4.getInt("seqid");
						sameid = rs4.getInt("sameid");
						lat = rs4.getDouble("lat");
						lng = rs4.getDouble("lng");
						G = rs4.getInt("G");
						cate = rs4.getString("cate");
						startTime = rs4.getString("time");
						endTime = rs4.getString("endtime");
						
						DataPoint matched_point = new DataPoint(seqid,sameid, lat, lng, G, cate, startTime, endTime);
						
						Date time = Main_v2.sdFormat.parse(rs4.getString("time"));
						
						if ((time.getTime() - temp.endTime.getTime()) > IM_Main.DT)
							break;
						
						sql = "select * from g_timedistribution where gid = " + comp.G;
						
						ResultSet rs5 = jdbc.query(sql);
						
						rs5.next();
						
						String input = rs5.getString("distribution");
						
						comp.setTimeDistribution(input);
						if(matched_point.seqid == 542) {
							System.out.println();
							
						}
						if (DataPoint.similarity_cos(comp, matched_point) <= IM_Main.SIM_THRESHOLD) {
							correct++;
							flag = true;
							sum += DataPoint.similarity_cos(comp, matched_point);
							break;
						}
						
//						if (comp.G == matched_point.G) {
//							correct++;
//							flag = true;
//							break;
//						} 
					}
					if(!flag) {
						fault++;
					}
				}
				
			}
			else 
				System.out.println(temp.seqid + " cannot predict! (Not in frequent pattern)");
		}
		
		System.out.println("avg = " + (sum / (double)correct));
		
		System.out.println("correct = " + correct);
		System.out.println("fault = " + fault);		
		
		
	}
}
