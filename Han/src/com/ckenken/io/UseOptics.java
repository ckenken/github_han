package com.ckenken.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import lab.adsl.object.Point;
import lab.adsl.optics.OPTICS;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ckenken.Main.Main_v2;
import com.ckenken.storage.KML;
import com.ckenken.storage.NewPoint;

public class UseOptics {
	public static void main(String [] args) throws IOException
	{
		JDBC jdbc2 = new JDBC("han");

		KML kml = new KML();
		
		File folder = new File("April");
		
		Map<Long, Point> result = new HashMap<Long, Point>();
		int sum = 0;
		int id = 0;
		
		for(int i = 0; i<folder.list().length; i++) {
			
		//	System.out.println(folder.list()[i]);
		
			if(folder.list()[i].charAt(0) == '.')
				continue;
			
			File f = new File(folder.getName() + "/" + folder.list()[i]);
			
			FileInputStream FIS = new FileInputStream(f);
			
			@SuppressWarnings("resource")
			InputStreamReader ISR = new InputStreamReader(FIS);
			
			StringBuilder SB = new StringBuilder();
			
			while(ISR.ready()) 
			{
				char a = (char)ISR.read();
				SB.append(a);
			}
			
			kml.raw = SB.toString();
			
			//System.out.print(SB.toString());
			
			Document doc = Jsoup.parse(SB.toString());
			
			Elements whens = doc.getElementsByTag("when");
			Elements coords = doc.getElementsByTag("gx:coord");
			System.out.println(whens.get(0).text());
			//System.out.println(coords.get(0).text());
			
			for(int j = 0; j<whens.size(); j++) { //whens.size();
				
				String [] SP = coords.get(j).text().split(" ");
				
				String lat = SP[1];
				String lng = SP[0];
				
				if (j < 10)
					System.out.println("lat = " + lat + ", " + "lng ="  + lng);
				
				NewPoint newtemp = new NewPoint(whens.get(j).text(), lat, lng);
				//System.out.println(newtemp.getLat());
				Point temp = new Point((long)id, newtemp);				
				
				if(j < 10) {
					System.out.println(Main_v2.sdFormat.format(newtemp.getTime()));
				}
				
				String sql = "insert into raw2 values(" + id + "," + lat +  "," + lng + ",0,0,'" + Main_v2.sdFormat.format(newtemp.getTime()) +"','')";
				
				jdbc2.insertQuery(sql);
				
				kml.points.add(temp);
				result.put((long)id++, temp);
			}
			
			sum += whens.size();
			
			System.out.println(whens.size());			
			
			System.out.println(result.size());
//			
//			for(int i = 0; i<10; i++) {
//				System.out.println(result.get((long)i).lat + ", " + result.get((long)i).lng);
//				
//			}			
//			
		}
		
		System.out.println("total point = " + sum);
		System.out.println("result size = " + result.size());
		
//		Map<Long, Point> result = new HashMap<Long, Point>();
//		Point p0 = new Point(0, 53.7456517542, -0.4875719547);
//		result.put(0L, p0);
//		
		
		OPTICS k = new OPTICS();
		
		k.setParameter(0, 20, 5);
			
		k.pts = result;
		
	//	k.pts = k.getSyntheticData();
	
		k.runOptics();
		
		k.extractCluster();
		
		k.displayCluster("same20-5-newyork.txt");
	//	System.out.println(k.clusterOrder.get(0).ckTime);
		
		for(int i = 0;i<k.clusterOrder.size(); i++) {
			kml.points.get((int)k.clusterOrder.get(i).id).clusterId = k.clusterOrder.get(i).clusterId;
		}		
		
	}
}
