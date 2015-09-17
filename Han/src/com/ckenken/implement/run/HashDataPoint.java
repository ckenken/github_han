package com.ckenken.implement.run;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;

import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.CityBlockHash;
import be.tarsos.lsh.families.CosineDistance;
import be.tarsos.lsh.families.CosineHash;
import be.tarsos.lsh.families.HashFunction;

import com.ckenken.implement.storage.DataPoint;
import com.ckenken.io.JDBC;

import info.debatty.java.lsh.LSHSuperBit;
import info.debatty.java.lsh.SuperBit;

public class HashDataPoint {

	final public static int MAX_BUCKETS = 2; 
	final private static int TIME = 0;
	final private static int SEM = 1;
	
//	public static int hash(DataPoint a, int type, int max_bucket, int n)
//	{
//		int stages = 2;
//		
//		LSHSuperBit lsh = new LSHSuperBit(stages, max_bucket, n);
//		
//		int [] output;
//		
//		if (type == TIME)
//			output = lsh.hash(a.timeDistribution);
//		else 
//			output = lsh.hash(a.gDistribution);
//		
//		return output[0];
//	}
//	
//	public static int hash_v2(DataPoint a, int type) 
//	{
//		HashFunction cosh = new CosineHash(2);
//		
//		int output = 0;
//		
//		if (type == TIME) {	
//			Vector v = new Vector(2);	
//			v.setKey(Integer.toString(a.seqid));
//			
//			v.set(0, 0.5);
//			v.set(1, 0.5);
//			
////			for(int i = 0; i<24; i++) {
////				v.set(i, a.timeDistribution[i]);
////			}
//			output = cosh.hash(v);
//		}
//		else {
//			Vector v = new Vector(2);	
//			v.setKey(Integer.toString(a.seqid));
//			
//			v.set(0, 0.5);
//			v.set(1, 0.5);
//			
////			for(int i = 0; i<DataPoint.MAX_PARENT_G_NUMBER; i++) {
////				v.set(i, a.gDistribution[i]);
////			}
//			output = cosh.hash(v);			
//		} 
//		
//		return output;
//	}
	
	public static int hashTime(DataPoint a, int frag) {
	
		int [] bits = new int[frag+1];
		
		double step = (double)((double)a.timeDistribution.length/(double)frag);
		
		step = Math.floor(step);
		
		int start = 0;
		int round = 0;
		do
		{
			boolean flag = false;
			for(int i = start; i<start+step && i<a.timeDistribution.length; i++) {
				if (a.timeDistribution[i] >= 0.15) {
					flag = true;
					break;
				}
			}
			if (flag) {
				bits[round] = 1;
			}
			else {
				bits[round] = 0;
			}
			round++;
			start += step;
		} while(start < a.timeDistribution.length);
	
		
	////
		System.out.println("seqid = " + a.seqid);
		for(int i = 0; i<bits.length; i++) {
			System.out.print(bits[i]);
		}
		System.out.println();
	////	
		
		int hash = bitsToInt(bits);
		
		return hash;
	}

	public static int hashSemantic(DataPoint a, int frag) {
		
		int [] bits = new int[frag+1];
		
		double stepDouble = (double)((double)a.gDistribution.length/(double)frag);
		
		int step = (int)stepDouble;
		
		System.out.println(a.gDistribution.length + "," + frag + "," + step);
		
		int start = 0;
		int round = 0;
		do
		{
			boolean flag = false;
			for(int i = start; i<start+step && i<a.gDistribution.length; i++) {
				if (a.gDistribution[i] >= 0.2) {
					flag = true;
					break;
				}
			}
			if (flag) {
				bits[round] = 1;
			}
			else {
				bits[round] = 0;
			}
			round++;
			start += step;
		} while(start < a.gDistribution.length);
	
		int hash = bitsToInt(bits);
		
		return hash;
	}
	
	public static int bitsToInt(int [] bits) 
	{
		int sum = 0;
		for(int i = 0; i<bits.length; i++) {
			sum += bits[i] * Math.pow(2,i);
		}
		
		return sum;
	}
	
	public static void main(String[] args) throws SQLException, ParseException, IOException {
		
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
			
			datas.add(temp);
		}	
		
//		DataPoint a = DataPoint.copy(datas.get(115));
//
//		DataPoint b = DataPoint.copy(datas.get(116));
//		
//		for(int i = 0; i<a.gDistribution.length; i++) {
//			System.out.print(a.gDistribution[i] + " ");
//		}
//		System.out.println();
//		
//		a.buckets[0] = LSHash.hashSemantic(a, 4);
//		
//		System.out.println(a.buckets[0]);
//		
//		for(int i = 0; i<b.gDistribution.length; i++) {
//			System.out.print(b.gDistribution[i] + " ");
//		}
//		System.out.println();
//
//		b.buckets[0] = LSHash.hashSemantic(b, 4);
//		
//		System.out.println(b.buckets[0]);
        
		// time
		for(int i = 0; i<datas.size(); i++) {
			datas.get(i).buckets[TIME] = HashDataPoint.hashTime(datas.get(i), 24);
		}
		System.out.println("time:");
		for(int i = 0; i<datas.size(); i++) {
			System.out.println(datas.get(i).seqid + ": " + datas.get(i).buckets[TIME]);
		}
		
		// SEM
		for(int i = 0; i<datas.size(); i++) {
			datas.get(i).buckets[SEM] = HashDataPoint.hashSemantic(datas.get(i), 9);
		}
		System.out.println();
		System.out.println("sem:");
		for(int i = 0; i<datas.size(); i++) {
//			System.out.println(datas.get(i).seqid + ": " + datas.get(i).buckets[SEM]);
		}		
		
		ArrayList<Integer> symbols = new ArrayList<Integer>();
		
		DataPoint.max_symbol = 0;
		
		
		
		for(int i = 0; i<datas.size(); i++) {
			int symbolid = -1;
			DataPoint a = DataPoint.copy(datas.get(i));
			for(int j = 0; j<i; j++) {
				DataPoint b = DataPoint.copy(datas.get(j));
				
				if (a.G == b.G && a.buckets[TIME] == b.buckets[TIME] && a.buckets[SEM] == b.buckets[SEM]) {
					symbolid = b.symbol;
					break;
				}
			}
			if (symbolid != -1) {
				datas.get(i).symbol = symbolid;
			}
			else {
				datas.get(i).symbol = DataPoint.max_symbol;
				
				symbols.add(i);
				
				DataPoint.max_symbol++;
			}
		}
		
		
		
		
//		for(int i = 0; i<datas.size(); i++) {
//			int symbolid = -1;
//			
//			DataPoint a = DataPoint.copy(datas.get(i));
//			for(int j = 0; j<symbols.size(); j++) {
//				boolean flag = false;
//				for(int k = 0; k<i; k++) {
//					DataPoint b = DataPoint.copy(datas.get(k));
//					
//					if (a.seqid == 313 && b.seqid == 363) {
//						System.out.print("");
//					}
//					
//					if (b.symbol == j && a.G == b.G && a.buckets[TIME] == b.buckets[TIME] && a.buckets[SEM] == b.buckets[SEM]) {
//						symbolid = j;
//						flag = true;
//						break;
//					}
//					if (flag)
//						break;
//				}
//							}
//			if (symbolid != -1) {
//				datas.get(i).symbol = symbolid;
//			}
//			else {
//				datas.get(i).symbol = DataPoint.max_symbol;
//				
//				symbols.add(i);
//				
//				DataPoint.max_symbol++;
//			}
//		}
		
		for(int i = 0; i<datas.size(); i++) {
		//	System.out.println(datas.get(i).seqid + ": " + datas.get(i).symbol);
			
			sql = "update sequence30_training set symbol = " + datas.get(i).symbol + " where seqid = " + datas.get(i).seqid;
			jdbc.insertQuery(sql);
			
		}
		
		RunEndTime = System.currentTimeMillis();
		
		totTime = RunEndTime - RunStartTime;		
		
		System.out.println("time = " + totTime + "ms");		
		
	}
	
	
}
