package com.ckenken.implement.run;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import com.ckenken.Main.Main_v2;
import com.ckenken.implement.storage.DataPoint;
import com.ckenken.io.JDBC;

public class CurrentPredict {

	private static final String OUTPUT = "pre_DEBUG.txt";  
	
	private static ArrayList<DataPoint> datas;
	
	private static int [] correct;
	private static int [] fault;
	
	public static int [] exist;
	
	public static void main(String [] args) throws SQLException, ParseException, IOException
	{
//		PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
//		System.setOut(outstream);				
		
		MakeCanPredict c = new MakeCanPredict();
		
		correct = new int [5];
		fault = new int [5];
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from merged_sequence30_training";
		
		ResultSet rs = jdbc.query(sql);
		
		datas = new ArrayList<DataPoint>();
		
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
		rs.close();
		
		exist = new int[2000];
		
		sql = "select * from datapattern_training";
		
		rs = jdbc.query(sql);
		
		while(rs.next()) {
			String pattern = rs.getString("datapattern");
			
			String [] SP = pattern.split(",");
			
			for(int i = 0; i<SP.length; i++) {
				exist[Integer.parseInt(SP[i])] = 1;
			}
		}
		rs.close();
		
		sql = "select * from sequence30 where seqid > 900";
		
		rs = jdbc.query(sql);
		
		int q2correct = 0;
		int q2fault = 0;		

		int q3correct = 0;
		int q3fault = 0;		

		int q4correct = 0;
		int q4fault = 0;
		
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
			
//			if (MakeCanPredict.canPredict[temp.seqid] != 1) {
//				continue;
//			}
			
/////////////////////////////////////////////				
//////////////// Q2: find current Location ///////////////////
			
			IM_Main.S_THRESHOLD = 0;
			IM_Main.SEMANTIC_THRESHOLD = 0.7;
			IM_Main.TIME_THRESHOLD = 0.3;
			
			IM_Main.ALPHA = 0.0;
			IM_Main.BETA = 0.5;
			IM_Main.GAMA = 0.5;
			
			int minSymbol = findMostSim_v2(temp);
			
			if (minSymbol != -1) { 
				
//				System.out.println(temp.seqid + "->" + minSymbol);
				
				DataPoint testPoint = createTestPoint(minSymbol);
				
				if (testPoint.G == temp.G) {
					q2correct++;
				}
				else {
					q2fault++;
				}
			}
			
			IM_Main.S_THRESHOLD = 1;
			IM_Main.SEMANTIC_THRESHOLD = 0.7;
			IM_Main.TIME_THRESHOLD = 0.7;

			IM_Main.ALPHA = 0.4;
			IM_Main.BETA = 0.3;
			IM_Main.GAMA = 0.3;		
			
			nextItem(temp, minSymbol, 2);
			
			
//////////////// Q3: find current Semantic ///////////////////			
			
			IM_Main.S_THRESHOLD = 1;
			IM_Main.SEMANTIC_THRESHOLD = 0.0;
			IM_Main.TIME_THRESHOLD = 0.3;

			IM_Main.ALPHA = 0.5;
			IM_Main.BETA = 0.5;
			IM_Main.GAMA = 0.0;			
			
			minSymbol = findMostSim_v2(temp);
			
			if (minSymbol != -1) {
				DataPoint testPoint = createTestPoint(minSymbol);
				
//				System.out.println(temp.seqid + "->" + minSymbol);
				
				if (DataPoint.semantic_cos(temp, testPoint) >= 0.7) {
					q3correct++;	
				}
				else {
					q3fault++;
				}				
			}
			
			IM_Main.S_THRESHOLD = 1;
			IM_Main.SEMANTIC_THRESHOLD = 0.7;
			IM_Main.TIME_THRESHOLD = 0.7;

			IM_Main.ALPHA = 0.4;
			IM_Main.BETA = 0.3;
			IM_Main.GAMA = 0.3;		
			
			nextItem(temp, minSymbol, 3);
			
////////////////   Q4: find current Time   ///////////////////					

			IM_Main.S_THRESHOLD = 1;
			IM_Main.SEMANTIC_THRESHOLD = 0.7;
			IM_Main.TIME_THRESHOLD = 0.0;

			IM_Main.ALPHA = 0.5;
			IM_Main.BETA = 0.0;
			IM_Main.GAMA = 0.5;			
			
			minSymbol = findMostSim_v2(temp);
			
			if (minSymbol != -1) {
				DataPoint testPoint = createTestPoint(minSymbol);				

//				System.out.println(temp.seqid + "->" + minSymbol);			
				
				if (DataPoint.time_cos(temp, testPoint) >= 0.7) {
					q4correct++;
				}
				else {
					q4fault++;
				}
			}

			IM_Main.S_THRESHOLD = 1;
			IM_Main.SEMANTIC_THRESHOLD = 0.7;
			IM_Main.TIME_THRESHOLD = 0.7;

			IM_Main.ALPHA = 0.4;
			IM_Main.BETA = 0.3;
			IM_Main.GAMA = 0.3;		
			
			nextItem(temp, minSymbol, 4);
			
/////////////////////////////////////////////
/////////////////  Q1 ///////////////////////		
			
			IM_Main.S_THRESHOLD = 1;
			IM_Main.SEMANTIC_THRESHOLD = 0.7;
			IM_Main.TIME_THRESHOLD = 0.3;

			IM_Main.ALPHA = 0.4;
			IM_Main.BETA = 0.3;
			IM_Main.GAMA = 0.3;					
			
			minSymbol = findMostSim_v2(temp);
			
			if (minSymbol == -1)
				continue;
			
			IM_Main.S_THRESHOLD = 1;
			IM_Main.SEMANTIC_THRESHOLD = 0.7;
			IM_Main.TIME_THRESHOLD = 0.7;

			IM_Main.ALPHA = 0.4;
			IM_Main.BETA = 0.3;
			IM_Main.GAMA = 0.3;		
			
			nextItem(temp, minSymbol, 1);
			
		}
		
		NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits( 3 );    // after dot 2
		
		System.out.println("Q1:");
		System.out.println("correct = " + correct[1]);
		System.out.println("fault = " + fault[1]);
		System.out.println(nf.format((double)correct[1]/(double)(correct[1]+fault[1])));
		System.out.println("(" + correct[1] + "/" + (correct[1]+fault[1]) + ")");
		
		System.out.println("Q2:");
//		System.out.println("correct = " + correct[2]);
//		System.out.println("fault = " + fault[2]);
		System.out.println(nf.format((double)correct[2]/(double)(correct[2]+fault[2])));
		System.out.println("(" + correct[2] + "/" + (correct[2]+fault[2]) + ")");
		
//		System.out.println("Q2 current correct = " + q2correct);
//		System.out.println("Q2 current fault = " + q2fault);
		System.out.println("current:");
		System.out.println(nf.format((double)q2correct / (double)(q2correct + q2fault)));
		System.out.println("(" + q2correct + "/" + (q2correct + q2fault) + ")");		
		
		
		System.out.println("Q3:");
//		System.out.println("correct = " + correct[3]);
//		System.out.println("fault = " + fault[3]);
		System.out.println(nf.format((double)correct[3]/(double)(correct[3]+fault[3])));
		System.out.println("(" + correct[3] + "/" + (correct[3]+fault[3]) + ")");		
		
		
//		System.out.println("Q3 current correct = " + q3correct);
//		System.out.println("Q3 current fault = " + q3fault);
		System.out.println("current:");
		System.out.println(nf.format((double)q3correct / (double)(q3correct + q3fault)));
		System.out.println("(" + q3correct + "/" + (q3correct + q3fault) + ")");			
		
		System.out.println("Q4:");
//		System.out.println("correct = " + correct[4]);
//		System.out.println("fault = " + fault[4]);
//		System.out.println("Q4 current correct = " + q4correct);
//		System.out.println("Q4 current fault = " + q4fault);
		System.out.println(nf.format((double)correct[4]/(double)(correct[4]+fault[4])));
		System.out.println("(" + correct[4] + "/" + (correct[4]+fault[4]) + ")");
		
		System.out.println("current:");
		System.out.println(nf.format((double)q4correct / (double)(q4correct + q4fault)));
		System.out.println("(" + q4correct + "/" + (q4correct + q4fault) + ")");				
		
	}
	
	public static int findMostSim_v2(DataPoint temp) 
	{
		double min = 20000000.0;
		int minI = -1;
		for(int i = 0; i<datas.size(); i++) {
			double score = DataPoint.similarity_cos(datas.get(i), temp);
			
			if (score != IM_Main.NOT_SIM)
				score = 1-score;
			
			if(score != IM_Main.NOT_SIM && score < min && exist[datas.get(i).symbol] == 1) {				
				minI = i;
				min = score;
			}
		}
		
		if(minI == -1) {
			return -1;
		}
		int minSymbol = datas.get(minI).symbol;
		
		return minSymbol;		
	}
	
	public static int findMostSim(DataPoint temp) 
	{
		double min = 20000000.0;
		int minI = -1;
		for(int i = 0; i<datas.size(); i++) {
			double score = DataPoint.similarity_cos(datas.get(i), temp);
			
//			System.out.println(temp.seqid + "<->" +  datas.get(i).seqid);
//			
//			System.out.println("SEMD: " + DataPoint.semantic_cos(temp, datas.get(i)));
//			System.out.println("TD: " + DataPoint.time_cos(temp, datas.get(i)));
//			if (temp.G == datas.get(i).G)
//				System.out.println("SD = 1");
//			else 
//				System.out.println("DS = 0");
			
			if (score != IM_Main.NOT_SIM)
				score = 1-score;
			
			if(score != IM_Main.NOT_SIM && score < min) {
				
//				System.out.println(temp.seqid + "<->" +  datas.get(i).seqid);
//				
//				System.out.println("SEMD: " + DataPoint.semantic_cos(temp, datas.get(i)));
//				System.out.println("TD: " + DataPoint.time_cos(temp, datas.get(i)));
//				if (temp.G == datas.get(i).G)
//					System.out.println("SD = 1");
//				else 
//					System.out.println("SD = 0");
				
				minI = i;
				min = score;
				
			}
		}
		
		if(minI == -1) {
//			System.out.println(temp.seqid + " no sim point!");
			return -1;
		}
		int minSymbol = datas.get(minI).symbol;
		
		return minSymbol;
	}
	
	public static DataPoint createTestPoint(int minSymbol) throws SQLException, ParseException, IOException
	{
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from merged_sequence30_training where symbol = " + minSymbol;
		
		ResultSet rs99 = jdbc.query(sql);
		
		rs99.next();
		
		int seqid = rs99.getInt("seqid");
		int sameid = rs99.getInt("sameid");
		double lat = rs99.getDouble("lat");
		double lng = rs99.getDouble("lng");
		int G = rs99.getInt("G");
		String cate = rs99.getString("cate");
		String startTime = rs99.getString("time");
		String endTime = rs99.getString("endtime");
		
		DataPoint testPoint = new DataPoint(seqid,sameid, lat, lng, G, cate, startTime, endTime);
		
		testPoint.symbol = rs99.getInt("symbol");							
		
		sql = "select * from prefixcenter_training where symbolid = " + testPoint.symbol;
		
//		System.out.println(sql);
		
		ResultSet rs66 = jdbc.query(sql);
		rs66.next();
		testPoint.setDistributions(rs66.getString("gDistribution"), rs66.getString("timeDistribution"));				
		
		return testPoint;
	}
	
	public static void nextItem(DataPoint temp, int minSymbol, int qNumber) throws NumberFormatException, SQLException, ParseException, IOException
	{	
		JDBC jdbc = new JDBC("han");
		
		System.gc();
		
		String sql = "select * from datapattern_training order by frequent desc";
		
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
		rs2.close();
		
		if(next != -1) {
		
			sql = "select * from merged_sequence30_training where symbol = " + next;
			
			ResultSet rs3 = jdbc.query(sql);
			
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
			
			sql = "select * from prefixcenter_training where symbolid = " + comp.symbol;

			ResultSet rs6 = jdbc.query(sql);
			
			rs6.next();
			
			comp.setDistributions(rs6.getString("gDistribution"), rs6.getString("timeDistribution"));
			
			if(temp.seqid != 670) {
				sql = "select * from sequence30 where seqid > " + (temp.seqid);

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
					
					if (DataPoint.similarity_cos(comp, matched_point) <= IM_Main.SIM_THRESHOLD) {
						
//						System.out.println("success, "+ temp.seqid + "<->" + matched_point.seqid);
						correct[qNumber]++;
						System.out.println(temp.seqid);
						flag = true;	
						break;
					} 
				}
				if(!flag) {
//					System.out.println("fail, "+ comp.seqid);
					fault[qNumber]++;
					System.out.println(temp.seqid);
				}
			}
			
		}
		else  {
//			System.out.print("seqid = " + temp.seqid + " , ");
//			System.out.println("minsymbol = " + minSymbol);
//			System.out.println("cannot predict! (Not in frequent pattern)");
		}		
		
	}
	
}
