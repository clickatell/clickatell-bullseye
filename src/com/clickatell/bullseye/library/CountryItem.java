package com.clickatell.bullseye.library;

public class CountryItem {
	public String name;
	public int id;
	public CountryItem(){
		
	}
	public CountryItem(String name, int id){
		this.name = name;
		this.id = id;
	}
	
	public String toString(){
		return name;
	}
}
