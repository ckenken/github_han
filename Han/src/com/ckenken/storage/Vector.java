package com.ckenken.storage;

import java.util.ArrayList;

public class Vector {
	public ArrayList<Double> v;
	
	public Vector()
	{
		v = new ArrayList<Double>();
	}
	
	public Vector(Snippet sni)
	{
		v = new ArrayList<Double>();
		
		for(int i = 0; i<sni.s.points.size(); i++) {
			double lat = sni.s.points.get(i).lat;
			double lng = sni.s.points.get(i).lng;
			v.add(lat);
			v.add(lng);
		}
	}
	
	public static double vectorDistance(Vector from, Vector to) {
		
		if(from.v.size() != to.v.size()) {
			System.out.println("Size of vectors are not fitting!");
			return -1;
		}
		
		Vector temp = new Vector();	
		
		for(int i = 0; i<from.v.size(); i++) {	
			temp.v.add(Math.pow(from.v.get(i) - to.v.get(i), 2));
		}
		
		double sum = 0;
		for(int i = 0; i<temp.v.size(); i++) {
			sum += temp.v.get(i);
		}
		
		return Math.sqrt(sum);
	}
	
	public static Vector addVector(Vector a, Vector b) {
		Vector temp = new Vector();
		for(int i = 0; i<a.v.size(); i++) {
			temp.v.add(a.v.get(i) + b.v.get(i));	
		}
		return temp;
	}
	
	public void show()
	{
		for(int i = 0; i<this.v.size(); i++) {
			if(i == 0)
				System.out.print(this.v.get(i));
			else 
				System.out.print(", " + this.v.get(i));
		}
		System.out.println();
	}
	
//	public static void main(String [] args) 
//	{
//		Vector v = new Vector();
//		
//		v.v.add(1.0);
//		v.v.add(1.0);
//		v.v.add(1.0);
//		
//		Vector v2 = new Vector();
//		
//		v2.v.add(0.0);
//		v2.v.add(3.0);
//		v2.v.add(4.0);
//		
//		//System.out.println(Vector.vectorDistance(v, v2));
//	
//		Vector temp = Vector.addVector(v, v2);
//		for(int i = 0; i<temp.v.size(); i++) 	
//			System.out.print(temp.v.get(i) + ", ");
//		
//	}
//	
}
