package com.ckenken.implement.run;

import com.ckenken.io.JDBC;

public class ResetPattern {
	public static void main(String [] args) {
		JDBC jdbc = new JDBC("han");
		
		String sql = "delete from datapattern";
		
		jdbc.insertQuery(sql);
		
		sql = "delete from prefixcenter";
		
		jdbc.insertQuery(sql);
		
		sql = "update sequence30 set symbol = -1";
		
		jdbc.insertQuery(sql);
		
		sql = "delete from merged_sequence30";
		
		jdbc.insertQuery(sql);
		
		
		sql = "delete from datapattern_training";
		
		jdbc.insertQuery(sql);
		
		sql = "delete from prefixcenter_training";
		
		jdbc.insertQuery(sql);
		
		sql = "delete from merged_sequence30_training";
		
		jdbc.insertQuery(sql);
		
		sql = "update sequence30_training set symbol = -1";
		
		jdbc.insertQuery(sql);
		
		
		sql = "delete from coarse";
		
		jdbc.insertQuery(sql);
		
		sql = "delete from fine";
		
		jdbc.insertQuery(sql);
		
		
//		sql = "update sequence22 set symbol = -1";
//		
//		jdbc.insertQuery(sql);
	}
}
