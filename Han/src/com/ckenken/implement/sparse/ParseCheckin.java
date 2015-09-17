package com.ckenken.implement.sparse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lab.adsl.object.Point;
import lab.adsl.optics.Haversine;
import lab.adsl.optics.OPTICS;

import com.ckenken.Main.Main;
import com.ckenken.Main.Main_v2;
import com.ckenken.implement.storage.Google_Cate_List;
import com.ckenken.io.JDBC;
import com.ckenken.io.Same;
import com.ckenken.storage.Category;
import com.ckenken.storage.NewPoint;

public class ParseCheckin {
	public static ArrayList<Category> categories = new ArrayList<Category>(); 
	final private static String OUTPUT = "near_raw.txt";  
	
	public static int max_gcenter_number = 0;
	
	public static int NEAR_WINDOW = 7200;
	
	public static void readCheckin() throws FileNotFoundException
	{
		JDBC jdbc = new JDBC("han");
		
		File f = new File("checkins.dat");
		
		FileInputStream FIS = new FileInputStream(f);
		
		Scanner scanner = new Scanner(FIS);
		
		if (scanner.hasNext())
			scanner.nextLine();
		
		if (scanner.hasNext())
			scanner.nextLine();
		
		int k = 0;
		
		while(scanner.hasNext()) {
			String line = scanner.nextLine();
			
			if (k < 810514) {
				k++;
				continue;
			}
			
			line = line.replace("|", ",");
			
			String [] SP = line.split(",");
	
			for(int i = 0; i<=4; i++) {
				SP[i] = SP[i].replace(" ", "");
			}
		
			String [] SP2 = SP[5].split(" ");
			
			String time = SP2[1] + "T" + SP2[2];
			
			int id = Integer.parseInt(SP[0]);
			int userId = Integer.parseInt(SP[1]);
			int venueId = Integer.parseInt(SP[2]);
			
			String sql = "insert into raw values(" + id + "," + userId + "," + venueId + ",0,0,0,0,'" +  time + "','')";
			
			jdbc.insertQuery(sql);
			
//			break;
		}
		scanner.close();
		
	}
	
	public static void readVenues() throws FileNotFoundException
	{
		JDBC jdbc = new JDBC("han");
		
		File f = new File("venues.dat");
		
		FileInputStream FIS = new FileInputStream(f);
		
		Scanner scanner = new Scanner(FIS);
		
		if (scanner.hasNext())
			scanner.nextLine();
		
		if (scanner.hasNext())
			scanner.nextLine();
		
		int k = 0;
		int counter = 0;
		while(scanner.hasNext()) {
			String line = scanner.nextLine();
			
//			if (k < 1078359) {
//				k++;
//				continue;
//			}
			
			line = line.replace("|", ",");
			
			String [] SP = line.split(",");
	
			for(int i = 0; i<SP.length; i++) {
				SP[i] = SP[i].replace(" ", "");
			}
			
			int id = Integer.parseInt(SP[0]);
			
			if (SP[1].equals("")) {
				counter++;
				System.out.println("counter = " + counter);
				continue;
			}
//				
//			double lat = Double.parseDouble(SP[1]);
//			double lng = Double.parseDouble(SP[2]);
//			
//
//			
//			String sql = "insert into venues values(" + id + "," + lat + "," + lng + ")";
//			
//			jdbc.insertQuery(sql);
//			
//			break;
		}
		scanner.close();
		
		System.out.println("counter = " + counter);
		
	}
	
	public static void updateLatLngIntoRaw() throws SQLException
	{
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from venues";
		
		ResultSet rs = jdbc.query(sql);
		
		while(rs.next()) {
			
			int venueId = rs.getInt("id");
			
			sql = "update raw set lat = " + rs.getDouble("lat") + " , lng = " + rs.getDouble("lng") + " where venue_id = " + venueId; 
			
			jdbc.insertQuery(sql);
		}
		rs.close();
	}
	
	public static void splitByTime() throws SQLException, ParseException
	{
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from raw2";
		
		ResultSet rs = jdbc.query(sql);
		while(rs.next()) {
			double lat = rs.getDouble("lat");
			double lng = rs.getDouble("lng");
			String time = rs.getString("date");
			
			Date d = Main_v2.parseDate(time);
			
			sql = "update raw2 set timestamp = " + d.getHours() + " where id = " + rs.getInt("id");
			jdbc.insertQuery(sql);
		}
		rs.close();
	}
	
	public static void removeNoise() throws SQLException, ParseException, IOException
	{
		Map<Long, Point> result = new HashMap<Long, Point>();
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "";
		
		ResultSet rs;
		
		for(int i = 0; i<24; i++) {
			sql = "select * from raw2 where timestamp = " + i;
			rs = jdbc.query(sql);
			
			while(rs.next()) {
				long id = rs.getLong("id");
				
				double lat = rs.getDouble("lat");
				double lng = rs.getDouble("lng");
				
				Date d = Main_v2.parseDate(rs.getString("date"));
				
				NewPoint np = new NewPoint(d, lat, lng);
				
				Point p = new Point(id, np);
				
				result.put(id, p);
			}
			
			OPTICS k = new OPTICS();
			
			k.setParameter(0, 20, 5);
				
			k.pts = result;
			
		//	k.pts = k.getSyntheticData();
		
			k.runOptics();
			
			k.extractCluster();
			
			k.displayCluster("same20-5-newyork-gowalla" + i + ".txt");
			
			result.clear();
		}
		
	}
	
	public static void updateSame() throws IOException
	{
		File folder = new File("newyork-gowalla");
		
		JDBC jdbc = new JDBC("han");
		
		for(int i = 0; i<folder.list().length; i++) {
			File f = new File(folder.getName() + "/" + folder.list()[i]);
			
			System.out.println(folder.list()[i]);
			
			FileInputStream FIS = new FileInputStream(f);
			
			InputStreamReader ISR = new InputStreamReader(FIS);
			
			BufferedReader BR = new BufferedReader(ISR);
			
			while(BR.ready())
			{
				String line = BR.readLine();
				
				line = line.replace("output:", "");
				
				String [] SP = line.split(",");
				// SP[0] = id, SP[1] = clusterid (sameid)	
				
				String sql = "update raw2 set same = " + SP[1] + " where id = " + SP[0];
				
				jdbc.insertQuery(sql);
				
//				System.out.println(line);
			}
			BR.close();					
		}
	}
	
	public static ArrayList<NewPoint> getSameByIdAndTime(int id, int timestamp) throws SQLException, ParseException {
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from raw2 where same = " + id + " and timestamp = " + timestamp;
		
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
		
		for(int q = 0; q<24; q++) {
			String sql = "select Count(distinct same) from raw2 where timestamp = " + q;
			
			ResultSet rs = jdbc.query(sql);
			
			if(rs.next()) {
				int n = rs.getInt("Count(distinct same)");
				System.out.println("n = " + n);
				for(int i = 0; i<n-1; i++) { // except -1
					ArrayList<NewPoint> p1 = getSameByIdAndTime(i, q);
					
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
					
					insertSameSQL_googleTime(i, c, q);
				}
			}	
		}
	}
	
	public static void insertSameSQL_googleTime(int id, NewPoint p, int timestamp) throws SQLException, JSONException, IOException {
		
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
			
			String sql = "insert into same values(" + id + "," + p.getLat() + "," + p.getLng() + "," + 0 + ",'" + SB.toString() + "',0,0," + timestamp + ")";
		
			jdbc.insertQuery(sql);
			
			System.out.println(sql);
		}
		else if (categories.size() <= 1) {
			
			String sql = "insert into same values(" + id + "," + p.getLat() + "," + p.getLng() + "," + 0 + ",'Others,Others',0,0," + timestamp + ")";
			jdbc.insertQuery(sql);
			
			System.out.println(sql);
		}
	}
	
	public static void clusterSameNoCate(int timestamp) throws SQLException, IOException 
	{
		Map<Long, Point> result = new HashMap<Long, Point>();
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "Select * from same where timestamp = " + timestamp;
		
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
		
		k.displayCluster("G-cluster_im" + timestamp +".txt");
		
	}
	
	public static void updateSameG_NoCate(int timestamp) throws FileNotFoundException 
	{
		JDBC jdbc = new JDBC("han");
		
		File f = new File("G-cluster_im" + timestamp +  ".txt");
		
		FileInputStream FIS = new FileInputStream(f);
		
		Scanner scanner = new Scanner(FIS);
		
		String first = scanner.nextLine();
		
		first = first.replace("output:", "");
		
		String [] SP = first.split(",");
		
		int id = Integer.parseInt(SP[0]);
		int previousG = Integer.parseInt(SP[1]);
		
		String sql = "update same set G = 0 where sameid = " + id + " and timestamp = " + timestamp;
		
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
				sql = "update same set G = " + Gid + " where sameid = " + id + " and timestamp  = " + timestamp;
			}
			else {
				Gid++;
				sql = "update same set G = " + Gid + " where sameid = " + id + " and timestamp  = " + timestamp;
			} 
			
			jdbc.insertQuery(sql);
			
			previousG = G;
		}
	}
	
	public static String getDateById(int id) throws SQLException, ParseException
	{
		JDBC jdbc = new JDBC("han");
		
		String sql = "Select * from raw2 where id = " + id;
		
		ResultSet rs = jdbc.query(sql);
		
		if(rs.next()) {
		
			Date d = Main_v2.parseDate(rs.getString("date"));
//			System.out.println((d.getMonth()+1) + "/" + d.getDate());
			rs.close();
			jdbc.con.close();
			return ((d.getMonth()+1) + "-" + d.getDate());
		}
		else 
			return "-1#-1";
	}
	
	public static void insertDayIntoRaw() throws SQLException, ParseException
	{
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from raw2";
		
		ResultSet rs = jdbc.query(sql);
		
		while(rs.next()) {
			int id = rs.getInt("id");
			
			String day = getDateById(id);
			
			sql = "update raw2 set day = '" + day + "' where id = " + id;
			
			jdbc.insertQuery(sql);
		}
		rs.close();
	}
	
	public static void find_G_center(int timestamp) throws SQLException
	{
		JDBC jdbc = new JDBC("han");
		
		String sql = "select max(G) from same where timestamp = " + timestamp;
		
		ResultSet rs = jdbc.query(sql);
		
		rs.next();
		
		int max_G_num = rs.getInt("max(G)");
		
		for(int i = 0; i<=max_G_num; i++) {
			sql = "select AVG(lat) from same where G = " + i + " and timestamp = " + timestamp; 
			rs = jdbc.query(sql);
			rs.next();
			double lat = rs.getDouble("AVG(lat)");
			
			sql = "select AVG(lng) from same where G = " + i + " and timestamp = " + timestamp; 
			rs = jdbc.query(sql);
			rs.next();
			double lng = rs.getDouble("AVG(lng)");
			
			sql = "insert into temp_gcenter values(" + i + "," + timestamp + "," + lat + "," + lng + ")";
//			System.out.println(sql);
			jdbc.insertQuery(sql);
		}
	}
	
	public static void insertGcenterMap() throws SQLException 
	{
		double min = 21000000;
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from temp_gcenter";
		
		ResultSet rs = jdbc.query(sql);
		
		ArrayList<Gcenter> gcenters = new ArrayList<Gcenter>();
		
		while(rs.next()) {
			Gcenter c = new Gcenter(rs.getInt("gid"), rs.getInt("timestamp"), rs.getDouble("lat") , rs.getDouble("lng"));
			gcenters.add(c);
		}
		rs.close();
		
		System.out.println(gcenters.size());
		
		for(int i = 0; i<gcenters.size(); i++) {
			for(int j = 0; j<gcenters.size(); j++) {
				if (i == j)
					continue;
				
				if (Gcenter.distance(gcenters.get(i), gcenters.get(j)) <= 200.0) {
					System.out.println(gcenters.get(i).gid + "." + gcenters.get(i).timestamp +"<->" + gcenters.get(j).gid + "." + gcenters.get(j).timestamp +  " : " +Gcenter.distance(gcenters.get(i),gcenters.get(j)));
					sql = "insert into gcenter_map values(NULL," + gcenters.get(i).gid + "," + gcenters.get(i).timestamp + "," + gcenters.get(j).gid + "," + gcenters.get(j).timestamp + ")";
					//System.out.println(sql);
					jdbc.insertQuery(sql);
				}
			}
		}
		jdbc.con.close();
	}
	
	public static void updateTimeNumber() throws ParseException, SQLException
	{
		JDBC jdbc = new JDBC("han");
		
		String sql = "Select * from raw2";
		
		ResultSet rs = jdbc.query(sql);
		
		while(rs.next()) {
			int id = rs.getInt("id");
			
			Date d = Main_v2.parseDate(rs.getString("date"));
			
			sql = "update raw2 set timenumber = " + (int)(d.getTime()/1000) + " where id = " + id;
			jdbc.insertQuery(sql);
		}
		rs.close();
		jdbc.con.close();
	}
	
	public static ArrayList<ArrayList<Integer>> findNearRaw() throws SQLException
	{
		ArrayList<ArrayList<Integer>> output = new ArrayList<ArrayList<Integer>>();
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from raw2 where same != -1 order by user_id, timenumber";
		
		ResultSet rs = jdbc.query(sql);
		
		long previous;
		int preUserId = -1;
		int preId = -1;
		
		if (rs.next()) {
			previous = rs.getLong("timenumber");
			preUserId = rs.getInt("user_id");
			preId = rs.getInt("id");
		}
		else {
			System.out.println("error");
			return null;
		}
		
		while(rs.next()) {
			long now = rs.getLong("timenumber");			
			int nowUserId = rs.getInt("user_id");
			int nowId = rs.getInt("id");
			if ((now-previous) < NEAR_WINDOW && nowUserId == preUserId) {
	//			System.out.println(preId + "->" + nowId + ", " + nowUserId);
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.add(preId);
				temp.add(nowId);
				output.add(temp);
			}
			preId = nowId;
			preUserId = nowUserId;
			previous = now;
		}
		return output;
	}
	
	public static void printNearDistance() throws SQLException
	{
		if (Gcenter.jdbc == null || Gcenter.jdbc.con.isClosed()) {
			Gcenter.jdbc = new JDBC("han");
		}
		
		ArrayList<ArrayList<Integer>> list = findNearRaw();
		
		for(int i = 0; i<list.size(); i++) {
			int id1 = list.get(i).get(0);
			int id2 = list.get(i).get(1);

			double lat1 = 0.0;
			double lng1 = 0.0;
			double lat2 = 0.0;
			double lng2 = 0.0;
			
			String sql = "select * from raw2 where id = " + id1;
			
			ResultSet rs = Gcenter.jdbc.query(sql);
			
			if (rs.next()) {
				lat1 = rs.getDouble("lat");
				lng1 = rs.getDouble("lng");
			}
			else {
				System.out.println("Error!");
				System.exit(1);
			}
			rs.close();
			
			sql = "select * from raw2 where id = " + id2;
			
			rs = Gcenter.jdbc.query(sql);
			
			if (rs.next()) {
				lat2 = rs.getDouble("lat");
				lng2 = rs.getDouble("lng");
			}
			else {
				System.out.println("Error!");
				System.exit(1);
			}
			rs.close();
			
			System.out.println(id1 + "->" + id2 + ": " + Haversine.getDistanceDouble(lat1, lng1, lat2, lng2));
			
		}
	}
	
	public static void readGowalla() throws FileNotFoundException
	{
		JDBC jdbc = new JDBC("han");
		
		File f = new File("loc-gowalla_totalCheckins.txt");
		
		FileInputStream FIS = new FileInputStream(f);
		
		Scanner scanner = new Scanner(FIS);
		
		if (scanner.hasNext())
			scanner.nextLine();
		
		if (scanner.hasNext())
			scanner.nextLine();
		
		int k = 0;
		
		while(scanner.hasNext()) {
			String line = scanner.nextLine();
			
			String [] SP = line.split("\t");
			
			System.out.println(SP[2]);
			
			int userId = Integer.parseInt(SP[0]);
			String time = SP[1].replace("Z", "");
			double lat = Double.parseDouble(SP[2]);
			double lng = Double.parseDouble(SP[3]);
			int venueId = Integer.parseInt(SP[4]);
			
			SP[1] = SP[1].replace("Z", "");
			
			String sql = "insert into raw values(" + k + "," + userId + "," + venueId + "," + lat + "," + lng +",-1,-1,'" + time + "','',-1,'0-0',-1)";
			
			System.out.println(sql);
			
			jdbc.insertQuery(sql);
			
//			if (k < 810514) {
//				k++;
//				continue;
//			}
//			
//			line = line.replace("|", ",");
//			
//			String [] SP = line.split(",");
//	
//			for(int i = 0; i<=4; i++) {
//				SP[i] = SP[i].replace(" ", "");
//			}
//		
//			String [] SP2 = SP[5].split(" ");
//			
//			String time = SP2[1] + "T" + SP2[2];
//			
//			int id = Integer.parseInt(SP[0]);
//			int userId = Integer.parseInt(SP[1]);
//			int venueId = Integer.parseInt(SP[2]);
//			
//			String sql = "insert into raw values(" + id + "," + userId + "," + venueId + ",0,0,0,0,'" +  time + "','')";
//			
//			jdbc.insertQuery(sql);
//			

			k++;
	//		break;
		}
		scanner.close();		
		
		
	}
	
	
	public static void main(String[] args) throws SQLException, ParseException, IOException, JSONException {
		
//		PrintStream outstream = new PrintStream(new FileOutputStream(OUTPUT));  
//		System.setOut(outstream);		
//		
//		readCheckin();
//		readVenues();
//		updateLatLngIntoRaw();
//		splitByTime();
		
//		removeNoise();
		
//		updateSame();
		
//		insertSames();
		
//		for(int i = 0; i<24; i++) {
//			clusterSameNoCate(i);
//			updateSameG_NoCate(i);
//		}
		
//		insertDayIntoRaw();
		
//		for(int i = 0; i<24; i++) {
//			find_G_center(i);
//		}

//		updateTimeNumber();

//		findNearRaw();
		
		insertGcenterMap();
	
//		max_gcenter_number = 0;
		
//		printNearDistance();
		
		
		
// =============
//		readGowalla();
		
		
	}
}
