package com.ckenken.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.naming.spi.DirStateFactory.Result;

import lab.adsl.object.Point;
import lab.adsl.optics.OPTICS;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ckenken.Main.Main;
import com.ckenken.implement.storage.Google_Cate_List;
import com.ckenken.storage.Category;
import com.ckenken.storage.NewPoint;

public class Same {
	public static ArrayList<Category> categories = new ArrayList<Category>(); 
	
	public static void main(String [] args) throws IOException, SQLException, ParseException, JSONException 
	{

		updateSameIntoRaw("same20-5-April.txt");
		
		
//		File f2 = new File("same_all0_20-5.txt");
//		
//		FileWriter FW = new FileWriter(f2);
//		
//		String sql2 = "select * from raw2 where same = 0";
//		
//		JDBC jdbc2 = new JDBC();
//		
//		ResultSet rs = jdbc2.query(sql2);
//		
//		try {
//			while(rs.next())
//			{
//				String lat = rs.getString("lat");
//				String lng = rs.getString("lng");
//				System.out.println(lat + "," + lng);
//				FW.append(lat + "," + lng + "\n");
//			}
//			FW.close();
//
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
	
//		insertSames();	
		
//		write_G_center();
//		
//		clusterSameNoCate();
//		updateSameG_NoCate();
		
	}
	
	public static void updateSameIntoRaw(String fileName) throws IOException
	{
		File f = new File(fileName);
		
		FileInputStream FIS = new FileInputStream(f);
		
		InputStreamReader ISR = new InputStreamReader(FIS);
		
		BufferedReader BR = new BufferedReader(ISR);
		
		while(BR.ready())
		{
			String line = BR.readLine();
			
			line = line.replace("output:", "");
			
			String [] SP = line.split(",");
			// SP[0] = id, SP[1] = clusterid (sameid)	
			
			JDBC jdbc = new JDBC("han");
			
			String sql = "update raw2 set same = " + SP[1] + " where id = " + SP[0];
			
			jdbc.insertQuery(sql);
			
			System.out.println(line);
		}
		BR.close();		
		
	}
	
	public static ArrayList<NewPoint> getSameById(int id) throws SQLException, ParseException {
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from raw2 where same = " + id;
		
		ArrayList<NewPoint> result = new ArrayList<NewPoint>();
		
		ResultSet rs = jdbc.query(sql);
		
		while(rs.next())
		{
			double lat = rs.getDouble("lat");
			double lng = rs.getDouble("lng");
			
			String dateStr = rs.getString("date");
			
			Date d = Main.sdFormat.parse(dateStr);
			
			NewPoint np = new NewPoint(d, lat, lng);
			
			result.add(np);
		}		
		return result;
	}
	
	public static void insertSames() throws SQLException, ParseException, JSONException, IOException
	{
		JDBC jdbc = new JDBC("han");
		
		String sql = "select Count(distinct same) from raw2";
		
		ResultSet rs = jdbc.query(sql);
		
		while(rs.next()) {
			int n = rs.getInt("Count(distinct same)");
			System.out.println("n = " + n);
			for(int i = 0; i<n-1; i++) {
				ArrayList<NewPoint> p1 = getSameById(i);
				
				double latSum = 0.0;
				double lngSum = 0.0;
				
				if(p1.size() == 0) {
					System.out.println(i + ", error");
					continue;
				}
				
				Date d = p1.get(0).getTime();
				
				for(int j = 0; j<p1.size(); j++) {
					NewPoint p = p1.get(j);
					
					latSum += p.getLat();
					lngSum += p.getLng();
				}
				
				double centerLat = latSum/(double)p1.size();
				double centerLng = lngSum/(double)p1.size();
				
				NewPoint c = new NewPoint(d, centerLat, centerLng);
				
				System.out.println("same = " + i + ": lat,lng: " + centerLat + "," + centerLng);
				
				insertSameSQL_google(i, c);
			}
		}
			
		
	}
	
	public static void insertSameSQL(int id, NewPoint p) throws JSONException, SQLException {
		
		@SuppressWarnings({ "resource", "deprecation" })
		DefaultHttpClient client = new DefaultHttpClient();
		
		System.out.println(p.getLat() + "," + p.getLng());
		
		String url = "https://api.foursquare.com/v2/venues/search?limit=30&ll=" + p.getLat() + "," + p.getLng() + "&client_id=BXTCY4HGTLWINDPRLFXCOWRUEDAJC12ZHEGDTGX4A5DX413K&client_secret=X20DAZW4CXKKC2V1O4QXYYHEQ1T5BMIBHUYD5ZJOVUKGFD3V&v=20140728";

		HttpGet request = new HttpGet(url);
		HttpResponse response = null;
		
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String responseString = null;
		
		try {
			responseString = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(responseString);
		
		JSONObject json = new JSONObject(responseString);
		
		JSONArray venues = json.getJSONObject("response").getJSONArray("venues");
		
		for(int i = 0; i<venues.length(); i++) {
			Category c = new Category();
			
			if(venues.getJSONObject(i).getJSONArray("categories").length() <= 0)
				continue;
			
			c.category = venues.getJSONObject(i).getJSONArray("categories").getJSONObject(0).getString("name");
			c.distance = venues.getJSONObject(i).getJSONObject("location").getInt("distance");
			
			JDBC jdbc = new JDBC();
			
		//	System.out.println(c.category);
			
			String sql = "select * from categories where name = '" + c.category + "'";
			
			if(c.category.contains("'"))
				continue;
			
			System.out.println(sql);
			
			ResultSet rs = jdbc.query(sql);
			
			while(rs.next()){
				c.parent = rs.getString("parent");		
			}
			categories.add(c);
		}	
		
		Collections.sort(categories, new CateComparable());
		
//		for(int i = 0; i<categories.size(); i++) {
//			System.out.println("cate = " + categories.get(i).category + ", parent = " + categories.get(i).parent +", distance = " + categories.get(i).distance);
//		}
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "insert into same values(" + id + "," + p.getLat() + "," + p.getLng() + "," + 0 + ",'" + categories.get(0).category + "," + categories.get(1).category + "')";
		
		jdbc.insertQuery(sql);
		
	}
	
	public static void find_G_center() throws SQLException
	{
		JDBC jdbc = new JDBC("han");
		
		String sql = "select max(G) from same";
		
		ResultSet rs = jdbc.query(sql);
		
		rs.next();
		
		int max_G_num = rs.getInt("max(G)");
		
		for(int i = 0; i<=max_G_num; i++) {
			sql = "select AVG(lat) from same where G = " + i; 
			rs = jdbc.query(sql);
			rs.next();
			double lat = rs.getDouble("AVG(lat)");
			
			sql = "select AVG(lng) from same where G = " + i; 
			rs = jdbc.query(sql);
			rs.next();
			double lng = rs.getDouble("AVG(lng)");
			
			sql = "insert into gcenter values(" + i + "," + lat + "," + lng + ",'', 0)";
			jdbc.insertQuery(sql);
		}
	}
	
	public static void write_G_center() throws SQLException, IOException
	{
		File f = new File("gcenter.txt");
		FileWriter FW = new FileWriter(f);
		
		JDBC jdbc = new JDBC("history5");
		
		String sql = "select * from gcenter";
		
		ResultSet rs = jdbc.query(sql);
		
		while(rs.next()) {
//			if(rs.getInt("lat") == 0) {
//				continue;
//			}
//			
			String lat = rs.getString("lat");
			String lng = rs.getString("lng");
			
			FW.append(lat + "," + lng + "\n");
		}
		FW.close();
	}

	public static void insertSameSQL_google(int id, NewPoint p) throws SQLException, JSONException, IOException {
		
		@SuppressWarnings({ "resource", "deprecation" })
		DefaultHttpClient client = new DefaultHttpClient();
		
		System.out.println(p.getLat() + "," + p.getLng());
		
		String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + p.getLat() + "," + p.getLng() + "&radius=200&sensor=false&key=AIzaSyDk43SMQqkeCkvsJYfwZs-FI36I2nnBYQY";
		
	//	String url = "https://api.foursquare.com/v2/venues/search?limit=30&ll=" + p.getLat() + "," + p.getLng() + "&client_id=BXTCY4HGTLWINDPRLFXCOWRUEDAJC12ZHEGDTGX4A5DX413K&client_secret=X20DAZW4CXKKC2V1O4QXYYHEQ1T5BMIBHUYD5ZJOVUKGFD3V&v=20140728";

		HttpGet request = new HttpGet(url);
		HttpResponse response = null;
		
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String responseString = null;
		
		try {
			responseString = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println(responseString);
		
		JSONObject json = new JSONObject(responseString);
		
		JSONArray results = json.getJSONArray("results");
		
		categories = new ArrayList<Category>();
		
		for(int i = 0; i<results.length(); i++) {
			
			Category c = new Category();
			
	//		if(results.getJSONObject(i).getJSONArray("categories").length() <= 0)
	//			continue;
			
			c.category = results.getJSONObject(i).getJSONArray("types").getString(0);
	//		c.rating = results.getJSONObject(i).getDouble("rating");
			
		//	System.out.println(c.category);
			
			if (c.category.equals("colloquial_area")) {
				continue;
			}
			
			Google_Cate_List google = new Google_Cate_List();
			
			c.category = google.cateParent.get(c.category);
			
			categories.add(c);
		}	
		
//		Collections.sort(categories, new CateCompGoogle());
		
//		for(int i = 0; i<categories.size(); i++) {
//			System.out.println("cate = " + categories.get(i).category + ", parent = " + categories.get(i).parent +", distance = " + categories.get(i).distance);
//		}
		
		JDBC jdbc = new JDBC("han");
		
		if(categories.size() >=2) {
			StringBuilder SB = new StringBuilder();
			
			for(int i = 0; i<categories.size() && i<5; i++) {
				if (i == 0) {
					SB.append(categories.get(i).category);
				}
				else {
					SB.append("," + categories.get(i).category);
				} 
			}
			
			String sql = "insert into same values(" + id + "," + p.getLat() + "," + p.getLng() + "," + 0 + ",'" + SB.toString() + "',0,0)";
		
			jdbc.insertQuery(sql);
			
			System.out.println(sql);
		}
		else if (categories.size() <= 1) {
			
			String sql = "insert into same values(" + id + "," + p.getLat() + "," + p.getLng() + "," + 0 + ",'Others,Others',0,0)";
			jdbc.insertQuery(sql);
			
			System.out.println(sql);
		}
	}
	
	public static void clusterSameNoCate() throws SQLException, IOException 
	{
		Map<Long, Point> result = new HashMap<Long, Point>();
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "Select * from same";
		
		ResultSet rs = jdbc.query(sql);
		
		while(rs.next()) 
		{
			int sameid = rs.getInt("sameid");
			double lat = rs.getDouble("lat");
			double lng = rs.getDouble("lng");
			
			NewPoint newtemp = new NewPoint(new Date(), lat, lng);
			
			Point temp = new Point(sameid, newtemp);
			
			result.put((long)sameid, temp);
		}
		
		
		OPTICS k = new OPTICS();
		
		k.setParameter(0, 200, 1);
			
		k.pts = result;
		
	//	k.pts = k.getSyntheticData();
	
		k.runOptics();
		
		k.extractCluster();
		
		k.displayCluster("poly_im_-G-cluster_im.txt");
		
	}
	
	public static void updateSameG_NoCate() throws FileNotFoundException 
	{
		JDBC jdbc = new JDBC("han");
		
		File f = new File("poly_im_-G-cluster_im.txt");
		
		FileInputStream FIS = new FileInputStream(f);
		
		Scanner scanner = new Scanner(FIS);
		
		String first = scanner.nextLine();
		
		first = first.replace("output:", "");
		
		String [] SP = first.split(",");
		
		int id = Integer.parseInt(SP[0]);
		int previousG = Integer.parseInt(SP[1]);
		
		String sql = "update same set G = 0 where sameid = " + id;
		
		jdbc.insertQuery(sql);
		
		int Gid = 0;
		while(scanner.hasNext())
		{
			String line = scanner.nextLine();
			
			line = line.replace("output:", "");
			
			SP = line.split(",");
			id = Integer.parseInt(SP[0]);
			int G = Integer.parseInt(SP[1]);
			
			if (G == previousG && G != -1) {
				sql = "update same set G = " + Gid + " where sameid = " + id;
			}
			else {
				Gid++;
				sql = "update same set G = " + Gid + " where sameid = " + id;
			} 
			
			jdbc.insertQuery(sql);
			
			previousG = G;
		}
	}
	
	
}
	
	
