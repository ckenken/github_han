package com.ckenken.implement.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MakeCanPredict {
	public static int [] canPredict = new int [2000];
	
	public MakeCanPredict() throws FileNotFoundException
	{
		canPredict = new int[2000];
		
		File f = new File("canPredict.txt");
		
		FileInputStream FIS = new FileInputStream(f);
		
		Scanner scanner = new Scanner(FIS);
		
		while(scanner.hasNext()) {
			String line = scanner.nextLine();
			
			int id = Integer.parseInt(line);
			
			canPredict[id] = 1;
		}
	}
	
}
