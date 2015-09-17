package com.ckenken.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TwitterParser {
	public TwitterParser(String fileName) throws FileNotFoundException
	{
		File f = new File(fileName);
		
		FileInputStream FIS = new FileInputStream(f);
		
		Scanner scanner = new Scanner(FIS);
		
		while(scanner.hasNext())
		{
			String line = scanner.nextLine();
			
			String [] SP = line.split("\t");
			
			for(int i = 0; i<SP.length; i++) {
				System.out.println(SP[i]);
			}
			break;
		}
		
	}
	public static void main(String[] args) throws FileNotFoundException {
		TwitterParser TP = new TwitterParser("train.txt");
		
		
		
	}
	
}
