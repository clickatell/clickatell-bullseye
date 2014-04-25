package com.clickatell.bullseye.library;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParser extends DefaultHandler {
	List<NameValuePair> list = null;

	StringBuilder builder;

	BasicNameValuePair jobsValues = null;

	@Override
	public void startDocument() throws SAXException {
		list = new ArrayList<NameValuePair>();
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		System.out.println("se: " + localName);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if (!localName.equalsIgnoreCase("CLICKATELLSDK")) {
			list.add(new BasicNameValuePair(localName, builder.toString()));
			builder = new StringBuilder();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		/****** Read the characters and append them to the buffer ******/
		String tempString = new String(ch, start, length);
		if (builder == null) {
			builder = new StringBuilder();
		}
		builder.append(tempString);
	}
}
