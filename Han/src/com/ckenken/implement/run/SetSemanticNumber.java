package com.ckenken.implement.run;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.ckenken.io.JDBC;

public class SetSemanticNumber {
	
	public static void set(int n) throws SQLException {
		
		JDBC jdbc = new JDBC("han");
		
		String sql = "select * from same";
		
		ResultSet rs = jdbc.query(sql);
		
		
		while(rs.next()) 
		{
			String sems = rs.getString("cate");
			
			String [] SP = sems.split(",");
					
			StringBuilder SB = new StringBuilder();
			for(int i = 0; i<n && i<SP.length; i++) {
				if (i == 0)
					SB.append(SP[i]);
				else 
					SB.append("," + SP[i]);
			}
			
			sql = "update same set cate = '" + SB.toString() + "' where sameid = " + rs.getInt("sameid"); 
			jdbc.insertQuery(sql);
		}
		rs.close();

		
	}
	
	public static void main(String[] args) throws SQLException {
		SetSemanticNumber.set(3);
	}
}
