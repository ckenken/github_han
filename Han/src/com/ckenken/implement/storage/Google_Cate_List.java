package com.ckenken.implement.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.ckenken.io.JDBC;

public class Google_Cate_List {
	
	public HashMap<String, Integer> cateMap = new HashMap<String, Integer>();
	
	public HashMap<String, String> cateParent = new HashMap<String, String>();
	
	public HashMap<String, Integer> parentMap = new HashMap<String, Integer>();
	
	public Google_Cate_List() throws IOException
	{
		File f = new File("google_cates.txt");
		
		FileInputStream FIS = new FileInputStream(f);
		
		Scanner scanner = new Scanner(FIS);
		int index = 0;
		while(scanner.hasNext()) 
		{
			String line = scanner.nextLine();
			
			cateMap.put(line, index);
			index++;
		}
		scanner.close();
		
		parentMap.put("Entertainment", 0);
		parentMap.put("Outdoor", 1);
		parentMap.put("Food", 2);
		parentMap.put("Working", 3);
		parentMap.put("Shopping", 4);
		parentMap.put("Residence", 5);
		parentMap.put("Transport", 6);
		parentMap.put("Non-Important", 7);
		parentMap.put("Others", 8);
		
		insertParent();
	}
	            
	public void insertParent() throws IOException 
	{
//		JDBC jdbc = new JDBC("han");
		
		File subFile = new File("sub_cate.txt");
		
		FileInputStream FIS = new FileInputStream(subFile);
		
		Scanner scanner = new Scanner(FIS);
		
		ArrayList<String> sub = new ArrayList<String>();
		
		while(scanner.hasNext()) 
		{
			String line = scanner.nextLine();
			sub.add(line);
		}
		scanner.close();
		FIS.close();
		
		File parentFile = new File("parent_cate.txt");
		
		FIS = new FileInputStream(parentFile);
		
		scanner = new Scanner(FIS);
		
		ArrayList<String> parent = new ArrayList<String>();
		
		while(scanner.hasNext())
		{
			String line = scanner.nextLine();
			
			parent.add(line);
		}
		
		for(int i = 0; i<sub.size(); i++) {
			cateParent.put(sub.get(i), parent.get(i));
		}
	}
	
	
	public static void main(String[] args) throws IOException {
	
//		JDBC jdbc = new JDBC("han");
//		
//		String sql = "select * from sequence30 where symbol = -22";
//		
//		ResultSet rs = jdbc.query(sql);
//		
//		try {
//			
//			
//			if (rs.next()) {
//				System.out.println("12345");
//			}
//			else 
//			{
//				System.out.println("23456");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		Google_Cate_List g = new Google_Cate_List();
		
		g.insertParent();
		
		for (Object key : g.cateParent.keySet()) {
			System.out.println(key + " : " + g.cateParent.get(key));
		}		
		
//		Google_Cate_List g = new Google_Cate_List();
//		
//		for (Object key : g.cateMap.keySet()) {
//            System.out.println(key + " : " + g.cateMap.get(key));
//        }
//		
	}
}
