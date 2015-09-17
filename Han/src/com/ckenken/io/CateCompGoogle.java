package com.ckenken.io;

import java.util.Comparator;

import com.ckenken.storage.Category;

public class CateCompGoogle implements Comparator<Category>{
	@Override
	public int compare(Category a, Category b) {
		
		if(a.rating > b.rating)
			return 1;
		else if (a.rating == b.rating)
			return 0;
		else 
			return -1;
		
	}	
}
