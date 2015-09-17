package com.ckenken.implement.sparse;

public class EndPoint {
	
	final public static int PLUS = 0;
	final public static int MINUS = 1;
	
	public int symbolid;
	public int type; // 0 == +, 1 == -
	public int timestamp;
	
	public EndPoint(int inputSymbol, int inputType, int inputTimeStamp)
	{
		this.symbolid = inputSymbol;
		this.type = inputType;
		this.timestamp = inputTimeStamp;
	}
	
	public EndPoint copy()
	{
		EndPoint t = new EndPoint(this.symbolid, this.type, this.timestamp);
		return t;
	}
	
	public EndPoint()
	{
		this.type = PLUS;
		this.timestamp = -1;
	}
	
	public String getStringKey()
	{
		String str = new String();
		
		str = Integer.toString(this.symbolid) + "#" + (this.type==EndPoint.PLUS?"+":"-") +  "#" + Integer.toString(this.timestamp);
		
		return str;
	}
	
	public String getStringKeyShow()
	{
		String str = new String();
		
		str = Integer.toString(this.symbolid) + (this.type==EndPoint.PLUS?"+":"-") +  "#" + Integer.toString(this.timestamp);
		
		return str;
	}
	
	public static EndPoint keyToEndPoint(String key) 
	{
		String [] SP = key.split("#");
		
		EndPoint e = new EndPoint(Integer.parseInt(SP[0]), (SP[1].equals("+")?EndPoint.PLUS:EndPoint.MINUS),Integer.parseInt(SP[2]));
		
		return e;
	} 
	
}
