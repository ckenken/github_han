package com.ckenken.implement.run;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.ckenken.implement.storage.DataPoint;
import com.ckenken.io.JDBC;

public class IndexSymbol {
	
	final public static int NAIVE2 = 1;
	final public static int SS = 2;
	
	public static HashMap<Integer, Integer> symbolizeByHotRegion(ArrayList<DataPoint> datas, int method) throws SQLException
	{
		JDBC jdbc = new JDBC("han");
		
		String sql = "select max(g) from sequence30_training";
		
		ResultSet rs = jdbc.query(sql);
		
		rs.next();
		
		int max_g_num = rs.getInt("max(g)");
		
		ArrayList<ArrayList<DataPoint>> tree = new ArrayList<ArrayList<DataPoint>>();
		
		// Create Linked list (tree) prepare to input all DataPoints, index by hot reigion id (G)
		for(int i = 0; i<=max_g_num; i++) {	
			ArrayList<DataPoint> temp = new ArrayList<DataPoint>();
			tree.add(temp);
		}
		
		// Put all DataPoints into its own linked list
		for(int i = 0; i<datas.size(); i++) {
			DataPoint a = DataPoint.copy(datas.get(i));
			tree.get(datas.get(i).G).add(a);
		}
		
		// Begin to symbolize
		if (method == SS) {
			
			int mina = -1;
			int minb = -1;
			
			for(int i = 0; i<tree.size(); i++) {
				
				System.out.println("i = " + i);
				
				ArrayList<DataPoint> list = tree.get(i);
					
				boolean modified = false;
				do
				{
					modified = false;
					double min = 2000000;
					for(int k = 0; k<list.size(); k++) {
						for(int q = 0; q<list.size(); q++) {
							if(k != q) {
								DataPoint a = list.get(k);
								DataPoint b = list.get(q);
								
								if(a.symbol == b.symbol && a.symbol != -1) {
									continue;
								}
								
								double score = DataPoint.similarity_cos(a, b);
								
//								System.out.println("score = " + score);
								
								if(score != IM_Main.NOT_SIM && score < IM_Main.SIM_THRESHOLD) {
									
									score = 1.0-score;
									
									if(score < min) {
										min = score;
										mina = k;
										minb = q;
										modified = true;
									}
								}
								
							}
						}
					}
					if(modified) {
						DataPoint temp = DataPoint.merge(list.get(mina), list.get(minb));
						DataPoint temp2 = DataPoint.copy(temp);
						
						temp.seqid = list.get(mina).seqid;
						temp2.seqid = list.get(minb).seqid;
						
						temp.sameid = list.get(mina).sameid;
						temp2.sameid = list.get(minb).sameid;
						
						temp.G = list.get(mina).G;
						temp2.G = list.get(minb).G;
						
						temp.cate = list.get(mina).cate;
						temp2.cate = list.get(minb).cate;
						
						temp.startTime = list.get(mina).startTime;
						temp2.startTime = list.get(minb).startTime;
			
						temp.endTime = list.get(mina).endTime;
						temp2.endTime = list.get(minb).endTime;
						
						list.set(mina, temp);
						list.set(minb, temp2);
					
						int symbol = temp.symbol;
						for(int k = 0; k<list.size(); k++) {
							if(list.get(k).symbol == symbol) {
								list.get(k).extendTheSame(temp);
							}
						}
					}			
				}while(modified);
				
				for(int k = 0; k<list.size(); k++) {
					if(list.get(k).symbol == -1) {
						list.get(k).symbol = DataPoint.max_symbol++;
					}
				}				
			}
		}
		else if (method == NAIVE2) {
			
			int mina = -1;
			int minb = -1;
			
			for(int i = 0; i<tree.size(); i++) {
				ArrayList<DataPoint> list = tree.get(i);
				for(int j = 0; j<list.size(); j++) {
					
					DataPoint a = DataPoint.copy(list.get(j));
					
					boolean modified = false;
					double min = 2000000;
					
					for(int k = 0; k<j; k++) {
						DataPoint b = DataPoint.copy(list.get(k));
						
						double score = DataPoint.similarity_cos(a, b);
						
//						System.out.println("score = " + score);
						
						if(score != IM_Main.NOT_SIM && score < IM_Main.SIM_THRESHOLD) {
							
							score = 1.0-score;
							
							if(score < min) {
								min = score;
								mina = j;
								minb = k;
								modified = true;
							}
						}
						
					}
					
					if(modified) {
						DataPoint temp = DataPoint.merge(list.get(mina), list.get(minb));
						DataPoint temp2 = DataPoint.copy(temp);
						
						temp.seqid = list.get(mina).seqid;
						temp2.seqid = list.get(minb).seqid;
						
						temp.sameid = list.get(mina).sameid;
						temp2.sameid = list.get(minb).sameid;
						
						temp.G = list.get(mina).G;
						temp2.G = list.get(minb).G;
						
						temp.cate = list.get(mina).cate;
						temp2.cate = list.get(minb).cate;
						
						temp.startTime = list.get(mina).startTime;
						temp2.startTime = list.get(minb).startTime;
			
						temp.endTime = list.get(mina).endTime;
						temp2.endTime = list.get(minb).endTime;
						
						list.set(mina, temp);
						list.set(minb, temp2);
					
						int symbol = temp.symbol;
						for(int k = 0; k<list.size(); k++) {
							if(list.get(k).symbol == symbol) {
								list.get(k).extendTheSame(temp);
							}
						}
					}			
					else {
						list.get(j).symbol = DataPoint.max_symbol++;
					} 
				}
			}
		}
		
		HashMap<Integer, Integer> map = new HashMap<Integer,Integer>();
		
		for(int i = 0; i<tree.size(); i++) {
			ArrayList<DataPoint> list = tree.get(i);
			for(int j = 0; j<list.size(); j++) {
				map.put(list.get(j).seqid, list.get(j).symbol);
			}	
		}
		
		return map;
	}
}
