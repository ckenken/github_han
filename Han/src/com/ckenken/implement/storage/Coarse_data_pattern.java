package com.ckenken.implement.storage;

import java.util.ArrayList;

import com.ckenken.io.JDBC;

public class Coarse_data_pattern {
	public ArrayList<Integer> symbol_sequence;
	public int frequent;
	
	public Coarse_data_pattern()
	{
		symbol_sequence = new ArrayList<Integer>();
	}
	
	public void show()
	{
		for(int i = 0; i<symbol_sequence.size(); i++) {
			if(i == 0)
				System.out.print(symbol_sequence.get(i));
			else 
				System.out.print("->" + symbol_sequence.get(i));
		}
		System.out.println();
	}
	
	public void insertDataPattern(String tableName)
	{
		JDBC jdbc = new JDBC("han");
	
		StringBuilder SB = new StringBuilder();
		for(int i = 0; i<this.symbol_sequence.size(); i++) {
			if(i == 0)
				SB.append(Integer.toString(this.symbol_sequence.get(i)));
			else 
				SB.append("," + this.symbol_sequence.get(i));
		}
		
		String sql = "insert into " + tableName + " values (NULL,'" + SB.toString() + "'," + this.symbol_sequence.size() + "," + this.frequent + ")";
		
		jdbc.insertQuery(sql);
		
	}
	
}
