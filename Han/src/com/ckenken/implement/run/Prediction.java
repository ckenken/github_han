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

public class Prediction {
	
	private static final String OUTPUT = "new_predict_output_han_im.txt";  
	
	public static void main(String[] args) throws SQLException, ParseException, IOException {
		
//		PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
//		System.setOut(outstream);		
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from merged_sequence30_training";
		
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
		
		sql = "select * from sequence30 where seqid > 536";
		
		rs = jdbc.query(sql);
		
		int correct = 0;
		int fault = 0;
		
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
				System.out.println(temp.seqid + " no sim point!");
				continue;
			}
			int minSymbol = datas.get(minI).symbol;
			
	//		System.out.println(temp.seqid + "'s most sim point = " + datas.get(minI).seqid + ", symbol = "+ datas.get(minI).symbol + ", sim = " + min);
			
			System.gc();
			
			sql = "select * from datapattern_training order by frequent desc limit 5000";
			
			ResultSet rs2 = jdbc.query(sql);
			
			int next = -1;
			
			while(rs2.next()) {
				String pattern = rs2.getString("datapattern");
				
				String [] SP = pattern.split(",");
				
				for(int j = 0; j<SP.length; j++) {
					if (Integer.parseInt(SP[j]) == minSymbol && j != (SP.length-1)) { // find "most next frequent point" and not final point in sequence
						next = Integer.parseInt(SP[j+1]);	
						break;
					}
				}
				if(next != -1)
					break;
			}
			
			if(next != -1) {
//				System.out.println("predict " + temp.seqid + "'s next = " + next);
			
				sql = "select * from merged_sequence30_training where symbol = " + next;
				
				ResultSet rs3 = jdbc.query(sql);
				
				rs3.next();
//				int G_number = rs3.getInt("G");
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
				
				comp.symbol = rs3.getInt("symbol");							
				
				sql = "select * from prefixcenter_training where symbolid = " + comp.symbol;
				
	//			System.out.println(sql);
				
				ResultSet rs6 = jdbc.query(sql);
				
				rs6.next();
				
				comp.setDistributions(rs6.getString("gDistribution"), rs6.getString("timeDistribution"));
				
				if(temp.seqid != 670) {
					sql = "select * from sequence30 where seqid > " + (temp.seqid);
//					System.out.println(sql);
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
						
		//				System.out.println(comp.symbol + ": " + comp.seqid + "<->" + matched_point.seqid + " : " + DataPoint.similarity(comp, matched_point));
						
						
						if (DataPoint.similarity_cos(comp, matched_point) <= IM_Main.SIM_THRESHOLD) {
							System.out.println("success, "+ temp.seqid + "<->" + matched_point.seqid);
							correct++;
							flag = true;	
							break;
						} 
					}
					if(!flag) {
						System.out.println("fail, "+ comp.seqid);
						fault++;
					}
				}
				
			}
			else  {
				System.out.print("seqid = " + temp.seqid + " , ");
				System.out.println("minsymbol = " + minSymbol);
				System.out.println("cannot predict! (Not in frequent pattern)");
			}
		}
		
		System.out.println("correct = " + correct);
		System.out.println("fault = " + fault);
		
	}
}
