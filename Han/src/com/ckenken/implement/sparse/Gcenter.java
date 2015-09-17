package com.ckenken.implement.sparse;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ckenken.implement.storage.Google_Cate_List;
import com.ckenken.io.JDBC;
import com.ckenken.storage.Category;
import com.ckenken.storage.NewPoint;

import lab.adsl.optics.Haversine;

public class Gcenter {
	public int gid;
	public int timestamp;	
	public double lat;
	public double lng;
	
	final private static double SAME_THRESHOLD = 1.0; 
	
	private static ArrayList<Gcenter> sameGcenters;
	
	private static double [][] after = new double[150][24]; 

	public static JDBC jdbc;
	
	private static int max_gcenter_number = 0;
	
	public Gcenter(int inputId, int inputTimestamp, double inputLat, double inputLng)
	{
		this.gid = inputId;
		this.timestamp = inputTimestamp;
		this.lat = inputLat;
		this.lng = inputLng;
	}
	
	public static double squ(double a)
	{
		return (a*a);
	}
	
	public static double distance(Gcenter a, Gcenter b)
	{
		return Haversine.getDistanceDouble(a.lat, a.lng, b.lat, b.lng);
	}
	
	public static boolean compareSame(Gcenter a, Gcenter b) 
	{
		if (Gcenter.distance(a, b) <= SAME_THRESHOLD)
			return true;
		else 
			return false;
	}
	
	public static void recursiveSameGcenterAndInsert(Gcenter g) throws SQLException
	{	
//		Gcenter.jdbc = new JDBC("han");
		
		String sql = "select * from gcenter_map where gid1 = " + g.gid + " and timestamp1 = " + g.timestamp;
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		while(rs.next()) {
			int gid2 = rs.getInt("gid2");
			int timestamp2 = rs.getInt("timestamp2");
			
			sql = "select * from temp_gcenter where gid = " + gid2 + " and timestamp = " + timestamp2;
			
			ResultSet rs2 = jdbc.query(sql);
			
			if (rs2.next()) {
				Gcenter temp = new Gcenter(rs2.getInt("gid"), rs2.getInt("timestamp"), rs2.getDouble("lat"), rs2.getDouble("lng"));
				
				if (after[temp.gid][temp.timestamp] == 1) {
					continue;
				}
				
				after[temp.gid][temp.timestamp] = 1;
				recursiveSameGcenterAndInsert(temp);		
			}
			else {
				System.out.println("Error!");
				System.exit(1);
			}
			rs2.close();
		}
		rs.close();
		sameGcenters.add(g);
		
	}
	
	public static void insertRealGcenter()
	{
		double latSum = 0.0;
		double lngSum = 0.0;
		
		String sql = new String();
		
		for(int i = 0; i<sameGcenters.size(); i++) {
			latSum += sameGcenters.get(i).lat;
			lngSum += sameGcenters.get(i).lng;
			
			after[sameGcenters.get(i).gid][sameGcenters.get(i).timestamp] = 1;
			
			sql = "insert into gcenter_focus values(NULL," + sameGcenters.get(i).gid + "," + sameGcenters.get(i).timestamp + "," + max_gcenter_number +")";
			
			System.out.println(sql);
			
			Gcenter.jdbc.insertQuery(sql);
		}
		
		double lat = (double)((double)latSum / (double)sameGcenters.size());
		double lng = (double)((double)lngSum / (double)sameGcenters.size());
		
		sql = "insert into gcenter values(" + max_gcenter_number + "," + lat + "," + lng  +  ",'',0)";
		
		Gcenter.jdbc.insertQuery(sql);
	
		max_gcenter_number++;
	}
	
	public static void fillGfocusIntoRaw() throws SQLException
	{
		String sql = "select * from same";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		while(rs.next()) {
			int same = rs.getInt("sameid");
			int timestamp = rs.getInt("timestamp");
			int gid = rs.getInt("g");
							
			sql = "select * from gcenter_focus where gid1 = " + gid + " and timestamp1 = " + timestamp;
			
			ResultSet rs2 = Gcenter.jdbc.query(sql);
			
			if (rs2.next()) {
				int focus = rs2.getInt("focusgid");
				sql = "update raw2 set g = " + focus + " where same = " + same + " and timestamp = " + timestamp;
				jdbc.insertQuery(sql);
			}
			else {
				System.out.println("Fill g focus Error 2 !");
				System.exit(1);
			}
			rs2.close();
		}
		rs.close();
	}
	
	public static void fillCateIntoGcenter(int id, NewPoint p) throws JSONException, IOException
	{
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
		
		ArrayList<Category> categories = new ArrayList<Category>();
		
		for(int i = 0; i<results.length(); i++) {
			
			Category c = new Category();
			
			c.category = results.getJSONObject(i).getJSONArray("types").getString(0);
			
			if (c.category.equals("colloquial_area")) {
				continue;
			}
			
			Google_Cate_List google = new Google_Cate_List();
			
			c.category = google.cateParent.get(c.category);
			
			if (categories.size() == 0 && c.category.equals("Others"))
				continue;
			
			categories.add(c);
		}	
		
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
			
//			String sql = "insert into gcenter values(" + id + "," + p.getLat() + "," + p.getLng() + "," + 0 + ",'" + SB.toString() + "',0,0)";
		
			String sql = "update gcenter set cate = '" + SB.toString() + "' where Gid = " + id ;
			
			jdbc.insertQuery(sql);
			
			System.out.println(sql);
		}
		else if (categories.size() <= 1) {
			
//			String sql = "insert into gcenter values(" + id + "," + p.getLat() + "," + p.getLng() + "," + 0 + ",'Others,Others',0,0)";
			
			String sql = "update gcenter set cate = 'Others,Others' where Gid = " + id;
			
			jdbc.insertQuery(sql);
			
			System.out.println(sql);
		}
	}
	
	public static void insertCategories() throws SQLException, JSONException, IOException
	{
		String sql = "select * from gcenter";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		while(rs.next()) {
			double lat = rs.getDouble("lat");
			double lng = rs.getDouble("lng");
			int gid = rs.getInt("Gid");		
			
			NewPoint np = new NewPoint(new Date(), lat, lng);
			
			fillCateIntoGcenter(gid, np);
				
		}
		rs.close();
	}
	
	public static void main(String[] args) throws SQLException, JSONException, IOException {
	
		sameGcenters = new ArrayList<Gcenter>();
		
		Gcenter.jdbc = new JDBC("han");
		
		String sql = "select * from temp_gcenter";
		
		ResultSet rs = jdbc.query(sql);
		
		max_gcenter_number = 0;
		
//		while(rs.next()) {
//			Gcenter g = new Gcenter(rs.getInt("gid"), rs.getInt("timestamp"), rs.getDouble("lat"), rs.getDouble("lng"));
//			
//			
//			if (after[g.gid][g.timestamp] == 1)  //already assigned 
//				continue;
//			
//			after[g.gid][g.timestamp] = 1;
//			
//			recursiveSameGcenterAndInsert(g);
//
//			insertRealGcenter();
//			
//			sameGcenters.clear();
//			sameGcenters = new ArrayList<Gcenter>();
//		}
//		rs.close();
		
		
//		fillGfocusIntoRaw();
		
		insertCategories();
		
	}
	
}
