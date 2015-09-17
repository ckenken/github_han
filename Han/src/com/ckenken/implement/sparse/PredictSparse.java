package com.ckenken.implement.sparse;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ckenken.Main.Main_v2;
import com.ckenken.implement.run.IM_Main;
import com.ckenken.implement.storage.DataPoint;
import com.ckenken.implement.storage.Google_Cate_List;
import com.ckenken.io.JDBC;
import com.ckenken.storage.Category;
import com.ckenken.storage.NewPoint;

public class PredictSparse {
	
	public static ArrayList<DataPoint> datas;
	
	public static ArrayList<Category> categories = new ArrayList<Category>(); 

	private static int [] correct;
	private static int [] fault;
	
	public static void insertCateIntoNear() throws SQLException, JSONException, IOException
	{
		if (Gcenter.jdbc == null || Gcenter.jdbc.con.isClosed()) {
			Gcenter.jdbc = new JDBC("han");
		}
		
		ArrayList<ArrayList<Integer>> list = ParseCheckin.findNearRaw();
		
		for(int i = 0; i<list.size(); i++) {
			for(int j = 0; j<list.get(i).size(); j++) {
				String sql = "select * from raw2 where id = " + list.get(i).get(j); 
				
				ResultSet rs = Gcenter.jdbc.query(sql);
				
				while(rs.next()) {
					String cate = rs.getString("cate");
					
					if (cate.length() > 0) {
						continue;
					}
					int id = rs.getInt("id");
					double lat = rs.getDouble("lat");
					double lng = rs.getDouble("lng");
					
					NewPoint np = new NewPoint(new Date(), lat, lng);
					
					insertNearSQL_googleTime(id, np);
				}
				rs.close();
			}
		}
	}
	
	public static void insertNearSQL_googleTime(int id, NewPoint p) throws SQLException, JSONException, IOException {
		
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
			
			//String sql = "insert into raw2 values(" + id + "," + p.getLat() + "," + p.getLng() + "," + 0 + ",'" + SB.toString() + "',0,0," + timestamp + ")";
		
			String sql = "update raw2 set cate = '" + SB.toString() + "' where id = " + id;
			
			jdbc.insertQuery(sql);
			
			System.out.println(sql);
		}
		else if (categories.size() <= 1) {
			
			//String sql = "insert into raw2 values(" + id + "," + p.getLat() + "," + p.getLng() + "," + 0 + ",'Others,Others',0,0," + timestamp + ")";
			
			String sql = "update raw2 set cate = 'Others,Others' where id = " + id; 
			
			jdbc.insertQuery(sql);
			
			System.out.println(sql);
		}
	}
	
	public static DataPoint createTestPoint(int minSymbol) throws SQLException, ParseException, IOException
	{
		if (Gcenter.jdbc == null || Gcenter.jdbc.con.isClosed()) {
			Gcenter.jdbc = new JDBC("han");
		}
		
		String sql = "select * from sequence22 where symbol = " + minSymbol;
		
		ResultSet rs99 = Gcenter.jdbc.query(sql);
		
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
		
		rs99.close();
		
		sql = "select * from prefixcenter where symbolid = " + testPoint.symbol;
		
//		System.out.println(sql);
		
		ResultSet rs66 = Gcenter.jdbc.query(sql);
		rs66.next();
		testPoint.setDistributions(rs66.getString("gDistribution"), rs66.getString("timeDistribution"));				
		rs66.close();
		return testPoint;
	}
	
	public static boolean isInTimeWindow(DataPoint pre, DataPoint next)
	{
		if (pre.endTime.getTime() >= next.startTime.getTime() && pre.endTime.getTime() <= next.endTime.getTime()) {  // end in next during time
			return true;
		}
		else if ((next.startTime.getTime() - pre.endTime.getTime()) <= IM_Main.DT) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void nextItem(DataPoint temp, int minSymbol, int qNumber, DataPoint tempNext) throws NumberFormatException, SQLException, ParseException, IOException
	{	
		//JDBC jdbc = new JDBC("han");
		
		if (Gcenter.jdbc == null || Gcenter.jdbc.con.isClosed()) {
			Gcenter.jdbc = new JDBC("han");
		}
		
		System.gc();
		
		String sql = "select * from datapattern order by length,frequent desc";
//		String sql = "select * from datapattern";
		
		ResultSet rs2 = Gcenter.jdbc.query(sql);
		
//		int next = -1;
		
		ArrayList <Integer> next = new ArrayList<Integer>();
		
		while(rs2.next()) {
			String pattern = rs2.getString("datapattern");
			
			String [] SP = pattern.split(",");
			
			for(int j = 0; j<SP.length; j++) {
				if (Integer.parseInt(SP[j]) == minSymbol && j != (SP.length-1)) { // find "most next frequent point" and not final point in sequence
					for(int k = j+1; k<SP.length; k++) {
						next.add(Integer.parseInt(SP[k]));
					}
					
					//next.add(Integer.parseInt(SP[j+1]));	
			//		break;
				}
			}
//			if(next != -1)
//				break;
		}
		rs2.close();
		
		if(next.size() > 0) {
			boolean flag = false;
			
			for(int i = 0; i<next.size(); i++) {
				sql = "select * from sequence22 where symbol = " + next.get(i);
				
				ResultSet rs3 = Gcenter.jdbc.query(sql);
				
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
				
				rs3.close();
				
				sql = "select * from prefixcenter where symbolid = " + comp.symbol;

				ResultSet rs6 = Gcenter.jdbc.query(sql);
				
				rs6.next();
				
				comp.setDistributions(rs6.getString("gDistribution"), rs6.getString("timeDistribution"));
				rs6.close();
				
		//		if ((stime.getTime() - temp.endTime.getTime()) > IM_Main.DT)
		//			break;
				
				if (DataPoint.similarity_cos(comp, tempNext) <= IM_Main.SIM_THRESHOLD) {
					
		//				System.out.println("success, "+ temp.seqid + "<->" + matched_point.seqid);
					correct[qNumber]++;
					System.out.println(temp.seqid + " coorrect!");
					flag = true;
					break;
				} 
			}
			if (!flag && next.size() > 0) {
				fault[qNumber]++;
				System.out.print(temp.seqid + "->" + tempNext.seqid + ": ");
				System.out.println(temp.symbol + "->" + tempNext.symbol);
			}
			
////			if(temp.seqid != 670) {
//			sql = "select * from sequence22";
//
//			ResultSet rs4 = Gcenter.jdbc.query(sql);
//			
//			boolean flag = false;
//			
//			while(rs4.next()) {
//				
//				seqid = rs4.getInt("seqid");
//				sameid = rs4.getInt("sameid");
//				lat = rs4.getDouble("lat");
//				lng = rs4.getDouble("lng");
//				G = rs4.getInt("G");
//				cate = rs4.getString("cate");
//				startTime = rs4.getString("time");
//				endTime = rs4.getString("endtime");
//				
//				DataPoint matched_point = new DataPoint(seqid,sameid, lat, lng, G, cate, startTime, endTime);
//				
////				Date stime = Main_v2.sdFormat.parse(rs4.getString("time"));
////				Date etime = Main_v2.sdFormat.parse(rs4.getString("endtime"));
////				
//				if (!isInTimeWindow(matched_point, temp)) {
//					break;
//				}
//				
////				if ((stime.getTime() - temp.endTime.getTime()) > IM_Main.DT)
////					break;
//				
//				if (DataPoint.similarity_cos(comp, matched_point) <= IM_Main.SIM_THRESHOLD) {
//					
////						System.out.println("success, "+ temp.seqid + "<->" + matched_point.seqid);
//					correct[qNumber]++;
//					System.out.println(temp.seqid);
//					flag = true;	
//					break;
//				} 
//			}
//			if(!flag) {
////					System.out.println("fail, "+ comp.seqid);
//				fault[qNumber]++;
//				System.out.println(temp.seqid);
//			}
//			rs4.close();
//			}  // temp.seqid != 670
			
		}
		else  {
//			System.out.print("seqid = " + temp.seqid + " , ");
//			System.out.println("minsymbol = " + minSymbol);
//			System.out.println("cannot predict! (Not in frequent pattern)");
		}		
	}
	
	public static int findMostSim(DataPoint temp) 
	{
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
//			System.out.println(temp.seqid + " no sim point!");
			return -1;
		}
		int minSymbol = datas.get(minI).symbol;
		
		return minSymbol;
	}
	
	public static int [] patternSymbols() throws SQLException
	{
		if (Gcenter.jdbc == null || Gcenter.jdbc.con.isClosed()) {
			Gcenter.jdbc = new JDBC("han");
		}
		int [] appear = new int[1500];
		
		String sql = "select * from datapattern";
		
		ResultSet rs = Gcenter.jdbc.query(sql);
		
		while(rs.next()) {
			String datapattern = rs.getString("datapattern");
			
			String [] SP = datapattern.split(",");
			
			for(int i = 0; i<SP.length; i++) {
				appear[Integer.parseInt(SP[i])] = 1;
			}
		}
		
		return appear;
	}
	
	public static void main(String[] args) throws SQLException, JSONException, IOException, ParseException {
		
		insertCateIntoNear();
		
//		if (Gcenter.jdbc == null || Gcenter.jdbc.con.isClosed()) {
//			Gcenter.jdbc = new JDBC("han");
//		}
//		
//		correct = new int [5];
//		fault = new int [5];
//		
//		String sql = "select * from sequence22";
//		
//		ResultSet rs = Gcenter.jdbc.query(sql);
//		
//		PredictSparse.datas = new ArrayList<DataPoint>();
//		
//		while(rs.next()) {
//			int seqid = rs.getInt("seqid");
//			int sameid = rs.getInt("sameid");
//			double lat = rs.getDouble("lat");
//			double lng = rs.getDouble("lng");
//			int G = rs.getInt("G");
//			String cate = rs.getString("cate");
//			String startTime = rs.getString("time");
//			String endTime = rs.getString("endtime");
//			
//			DataPoint temp = new DataPoint(seqid,sameid, lat, lng, G, cate, startTime, endTime);
//			
//			temp.symbol = rs.getInt("symbol");
//			
//			datas.add(temp);
//		}		
//		rs.close();
//		
//		int q2correct = 0;
//		int q2fault = 0;		
//
//		int q3correct = 0;
//		int q3fault = 0;		
//
//		int q4correct = 0;
//		int q4fault = 0;
//		
//		ArrayList<ArrayList<Integer>> realTrajectory = ParseCheckin.findNearRaw();
//		
//		for(int i = 0; i<realTrajectory.size(); i++) {
//			int preId = realTrajectory.get(i).get(0);
//			int nextId = realTrajectory.get(i).get(1);
//			
//			sql = "select * from raw2 where id = " + preId;
//			
//			rs = Gcenter.jdbc.query(sql);
//			
//			if (!rs.next()) {
//				System.out.println("Error!!");
//				System.exit(1);
//			}
//			
//			int id = rs.getInt("id");
//			int same = rs.getInt("same");
//			double lat = rs.getDouble("lat");
//			double lng = rs.getDouble("lng");
//			int G = rs.getInt("G");
//			String cate = rs.getString("cate");
//			String startTime = rs.getString("date");
//			String endTime = rs.getString("date");
//			
//			DataPoint temp = new DataPoint(id,same, lat, lng, G, cate, startTime, endTime);		
//			
//			rs.close();
//			
//			sql = "select * from raw2 where id = " + nextId;
//			
//			rs = Gcenter.jdbc.query(sql);
//			
//			if (!rs.next()) {
//				System.out.println("Error!!");
//				System.exit(1);
//			}
//			
//			id = rs.getInt("id");
//			same = rs.getInt("same");
//			lat = rs.getDouble("lat");
//			lng = rs.getDouble("lng");
//			G = rs.getInt("G");
//			cate = rs.getString("cate");
//			startTime = rs.getString("date");
//			endTime = rs.getString("date");
//			
//			DataPoint tempNext = new DataPoint(id,same, lat, lng, G, cate, startTime, endTime);		
//			
//			rs.close();
//			
//			int symbol1 = PredictSparse.findMostSim(temp);
//			int symbol2 = PredictSparse.findMostSim(tempNext);
//			
//			if (symbol1 == symbol2 || symbol1 == -1 || symbol2 == -1)
//				continue;
//			
//			
//			/////////////////////////////////////////////				
//			////////////////Q2: find current Location ///////////////////
//			
//			IM_Main.S_THRESHOLD = 0;
//			IM_Main.SEMANTIC_THRESHOLD = 0.7;
//			IM_Main.TIME_THRESHOLD = 0.7;
//			
//			IM_Main.ALPHA = 0.0;
//			IM_Main.BETA = 0.5;
//			IM_Main.GAMA = 0.5;
//			
//			int minSymbol = findMostSim(temp);
//			
//			if (minSymbol != -1) { 
//				//System.out.println(temp.seqid + "->" + minSymbol);
//				DataPoint testPoint = createTestPoint(minSymbol);
//				
//				if (testPoint.G == temp.G) {
//					q2correct++;
//				}
//				else {
//					q2fault++;
//				}
//			}
//			
//			IM_Main.S_THRESHOLD = 1;
//			IM_Main.SEMANTIC_THRESHOLD = 0.7;
//			IM_Main.TIME_THRESHOLD = 0.7;
//			
//			IM_Main.ALPHA = 0.4;
//			IM_Main.BETA = 0.3;
//			IM_Main.GAMA = 0.3;		
//			
//			nextItem(temp, minSymbol, 2, tempNext);
//			
//			////////////////Q3: find current Semantic ///////////////////			
//			
//			IM_Main.S_THRESHOLD = 1;
//			IM_Main.SEMANTIC_THRESHOLD = 0.0;
//			IM_Main.TIME_THRESHOLD = 0.7;
//			
//			IM_Main.ALPHA = 0.5;
//			IM_Main.BETA = 0.5;
//			IM_Main.GAMA = 0.0;			
//			
//			minSymbol = findMostSim(temp);
//			
//			if (minSymbol != -1) {
//			DataPoint testPoint = createTestPoint(minSymbol);
//			
//			//System.out.println(temp.seqid + "->" + minSymbol);
//			
//			if (DataPoint.semantic_cos(temp, testPoint) >= 0.7) {
//					q3correct++;	
//				}
//				else {
//					q3fault++;
//				}				
//			}
//			
//			IM_Main.S_THRESHOLD = 1;
//			IM_Main.SEMANTIC_THRESHOLD = 0.7;
//			IM_Main.TIME_THRESHOLD = 0.7;
//			
//			IM_Main.ALPHA = 0.4;
//			IM_Main.BETA = 0.3;
//			IM_Main.GAMA = 0.3;		
//			
//			nextItem(temp, minSymbol, 3, tempNext);
//			
//			////////////////Q4: find current Time   ///////////////////					
//			
//			IM_Main.S_THRESHOLD = 1;
//			IM_Main.SEMANTIC_THRESHOLD = 0.7;
//			IM_Main.TIME_THRESHOLD = 0.0;
//			
//			IM_Main.ALPHA = 0.5;
//			IM_Main.BETA = 0.0;
//			IM_Main.GAMA = 0.5;			
//			
//			minSymbol = findMostSim(temp);
//			
//			if (minSymbol != -1) {
//				DataPoint testPoint = createTestPoint(minSymbol);				
//				
//				//System.out.println(temp.seqid + "->" + minSymbol);			
//				
//				if (DataPoint.time_cos(temp, testPoint) >= 0.7) {
//					q4correct++;
//				}
//				else {
//					q4fault++;
//				}
//			}
//			
//			IM_Main.S_THRESHOLD = 1;
//			IM_Main.SEMANTIC_THRESHOLD = 0.7;
//			IM_Main.TIME_THRESHOLD = 0.7;
//			
//			IM_Main.ALPHA = 0.4;
//			IM_Main.BETA = 0.3;
//			IM_Main.GAMA = 0.3;		
//			
//			nextItem(temp, minSymbol, 4, tempNext);
//			
//			/////////////////////////////////////////////
//			/////////////////  Q1 ///////////////////////		
//			
//			IM_Main.S_THRESHOLD = 1;
//			IM_Main.SEMANTIC_THRESHOLD = 0.7;
//			IM_Main.TIME_THRESHOLD = 0.7;
//			
//			IM_Main.ALPHA = 0.4;
//			IM_Main.BETA = 0.3;
//			IM_Main.GAMA = 0.3;					
//			
//			minSymbol = findMostSim(temp);
//			
//			if (minSymbol == -1)
//			continue;
//			
//			IM_Main.S_THRESHOLD = 1;
//			IM_Main.SEMANTIC_THRESHOLD = 0.7;
//			IM_Main.TIME_THRESHOLD = 0.7;
//			
//			IM_Main.ALPHA = 0.4;
//			IM_Main.BETA = 0.3;
//			IM_Main.GAMA = 0.3;		
//			
//			nextItem(temp, minSymbol, 1, tempNext);		
//			
//		}
//		
//		NumberFormat nf = NumberFormat.getInstance();
//        nf.setMaximumFractionDigits( 3 );    // after dot 2
//		
//		System.out.println("Q1:");
//		System.out.println("correct = " + correct[1]);
//		System.out.println("fault = " + fault[1]);
//		System.out.println(nf.format((double)correct[1]/(double)(correct[1]+fault[1])));
//		System.out.println("(" + correct[1] + "/" + (correct[1]+fault[1]) + ")");
//		
//		System.out.println("Q2:");
////		System.out.println("correct = " + correct[2]);
////		System.out.println("fault = " + fault[2]);
//		System.out.println(nf.format((double)correct[2]/(double)(correct[2]+fault[2])));
//		System.out.println("(" + correct[2] + "/" + (correct[2]+fault[2]) + ")");
//		
////		System.out.println("Q2 current correct = " + q2correct);
////		System.out.println("Q2 current fault = " + q2fault);
//		System.out.println("current:");
//		System.out.println(nf.format((double)q2correct / (double)(q2correct + q2fault)));
//		System.out.println("(" + q2correct + "/" + (q2correct + q2fault) + ")");		
//		
//		
//		System.out.println("Q3:");
////		System.out.println("correct = " + correct[3]);
////		System.out.println("fault = " + fault[3]);
//		System.out.println(nf.format((double)correct[3]/(double)(correct[3]+fault[3])));
//		System.out.println("(" + correct[3] + "/" + (correct[3]+fault[3]) + ")");		
//		
//		
////		System.out.println("Q3 current correct = " + q3correct);
////		System.out.println("Q3 current fault = " + q3fault);
//		System.out.println("current:");
//		System.out.println(nf.format((double)q3correct / (double)(q3correct + q3fault)));
//		System.out.println("(" + q3correct + "/" + (q3correct + q3fault) + ")");			
//		
//		System.out.println("Q4:");
////		System.out.println("correct = " + correct[4]);
////		System.out.println("fault = " + fault[4]);
////		System.out.println("Q4 current correct = " + q4correct);
////		System.out.println("Q4 current fault = " + q4fault);
//		System.out.println(nf.format((double)correct[4]/(double)(correct[4]+fault[4])));
//		System.out.println("(" + correct[4] + "/" + (correct[4]+fault[4]) + ")");
//		
//		System.out.println("current:");
//		System.out.println(nf.format((double)q4correct / (double)(q4correct + q4fault)));
//		System.out.println("(" + q4correct + "/" + (q4correct + q4fault) + ")");				
//		
		
	}
}
