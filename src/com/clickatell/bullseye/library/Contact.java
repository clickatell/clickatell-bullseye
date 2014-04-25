package com.clickatell.bullseye.library;

import java.util.ArrayList;

public class Contact {
	public long id;
	public String name;
	public String extra;
	public ArrayList<Contact> numbers;
	public Contact(long id, String name) {
		this(id, name, null);
	}
	public Contact(long id, String name, String extra) {
		this.id = id;
		this.name = name;
		this.extra = extra;
		this.numbers = new ArrayList<Contact>();
	}
	public void addNumber(Contact number) {
		this.numbers.add(number);
	}
	public String toString() {
		return name;
	}
}
