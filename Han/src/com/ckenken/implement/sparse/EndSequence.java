package com.ckenken.implement.sparse;

import java.util.ArrayList;

public class EndSequence {
	public ArrayList<EndPoint> endPoints;
	public int counter;
	
	public EndSequence()
	{
		endPoints = new ArrayList<EndPoint>();
	}
	
	public EndSequence(ArrayList<EndPoint> input)
	{
		endPoints = new ArrayList<EndPoint>();
		
		for(int i = 0; i<input.size(); i++) {
			endPoints.add(input.get(i));
		}
	}
	
	public EndSequence copy()
	{
		EndSequence seq = new EndSequence();
		
		for(int i = 0; i<this.endPoints.size(); i++) {
			seq.endPoints.add(this.endPoints.get(i).copy());
		}
		
		return seq;
	}
	
	public void release()
	{
		this.endPoints.clear();
	}
	
	public ArrayList<EndPoint> getSubSequence(int index)
	{
		ArrayList<EndPoint> temp = new ArrayList<EndPoint>();
		
		for(int i = index; i<endPoints.size(); i++) {
			temp.add(endPoints.get(i));
		}
		
//		EndSequence output = new EndSequence(temp);
		
		return temp;
	}
	
	public static boolean AincludeB(EndSequence a, EndSequence b) 
	{
		boolean flag = false;
		int counter = 0;
		for(int i = 0; i<b.endPoints.size(); i++) {
			for(int j = 0; j<a.endPoints.size(); j++) {
				if (b.endPoints.get(i).getStringKey().equals(a.endPoints.get(j).getStringKey())) {
					counter++;
					if (counter == b.endPoints.size()-1) {
						flag = true;
					}
					break;
				}
			}
			if (flag)
				break;
		}
		return flag;
	}
	
}
