package com.ckenken.implement.storage;

import java.util.ArrayList;

public class DataSequence {
	
	public ArrayList<DataPoint> dataPoints;
	
	public DataSequence()
	{
		dataPoints = new ArrayList<DataPoint>();
	}
	
	public DataSequence(ArrayList<DataPoint> input)
	{
		dataPoints = new ArrayList<DataPoint>();
		
		for(int i = 0; i<input.size(); i++) {
			dataPoints.add(input.get(i));
		}
	}
	
	public ArrayList<DataPoint> getSubDataSequence(int index) 
	{
		ArrayList<DataPoint> temp = new ArrayList<DataPoint>();
		
		for(int i = index; i<dataPoints.size(); i++) {
			temp.add(dataPoints.get(i));
		}
		return temp;
	}	
	
	public static double similarity() 
	{
		double score = 0.0;
		
		
		
		return score;
	}
	
}
