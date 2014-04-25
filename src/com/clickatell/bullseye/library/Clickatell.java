package com.clickatell.bullseye.library;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.net.Uri;

public class Clickatell {
	public static final int STATUS_FAILURE = -1;
	public static final int STATUS_SUCCESS = 1;
	public static final int STATUS_ALREADY_EMAILACTIVATED = 2;

	private final String API_URL = "https://api.clickatell.com/http/";
	private final String CONNECT_URL = "https://connect.clickatell.com/";
	private final String CONNECT_API_ID = "8d579a8654c093143142080cf9c85775";

	public Clickatell() {
	}

	public NameValuePair[] connectCall(String data) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(CONNECT_URL + CONNECT_API_ID);
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("XML", data));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String XMLData = httpclient.execute(httppost, responseHandler);
			BufferedReader br = new BufferedReader(new StringReader(XMLData));
			InputSource is = new InputSource(br);
			XMLParser parser = new XMLParser();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser sp = factory.newSAXParser();
			XMLReader reader = sp.getXMLReader();
			reader.setContentHandler(parser);
			reader.parse(is);
			NameValuePair[] ret = parser.list.toArray(new NameValuePair[0]);
			return ret;
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String apiCall(String data) {
		try {
			URL url = null;
			String response = null;
			String url_string = API_URL + data;
			url = new URL(url_string);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			String line = "";
			InputStreamReader isr = new InputStreamReader(
					connection.getInputStream());
			BufferedReader reader = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			response = sb.toString();
			isr.close();
			reader.close();
			return response;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public NameValuePair[] getCaptcha() {
		return this
				.connectCall("<clickatellsdk><action>get_captcha</action></clickatellsdk>");
	}

	public NameValuePair[] register(String firstname, String surname,
			String mobile, String email, String password, String company,
			int country, String coupon, String captcha_id, String captcha_text) {

		String s = "<clickatellsdk><action>register</action>";

		s += "<user>" + email + "</user>";
		s += "<fname>" + firstname + "</fname>";
		s += "<sname>" + surname + "</sname>";
		s += "<password>" + password + "</password>";
		s += "<email_address>" + email + "</email_address>";
		s += "<country_id>" + country + "</country_id>";
		s += "<mobile_number>" + mobile + "</mobile_number>";
		s += "<accept_terms>" + 1 + "</accept_terms>";
		s += "<captcha_id>" + captcha_id + "</captcha_id>";
		s += "<captcha_code>" + captcha_text + "</captcha_code>";
		s += "<force_create>1</force_create>";
		if (coupon.length() > 3) {
			s += "<coupon_code>" + coupon + "</coupon_code>";
		}
		s += "<activation_redirect>http://www.ourwebsite.co.za/bullseye/activationconfirmation.html</activation_redirect></clickatellsdk>";

		return this.connectCall(s);
	}

	public int connectAuth(String username, String password, String captcha_id,
			String captcha_value) {
		NameValuePair[] s = this
				.connectCall("<clickatellsdk><action>authenticate_user</action><user>"
						+ username
						+ "</user><password>"
						+ password
						+ "</password>"

						+ "<captcha_id>"
						+ captcha_id
						+ "</captcha_id>"
						+ "<captcha_code>"
						+ captcha_value
						+ "</captcha_code></clickatellsdk>");
		for (NameValuePair n : s) {
			if (n.getName().equalsIgnoreCase("Result")
					&& !n.getValue().equalsIgnoreCase("Success")) {
				return -1;
			}
			if (n.getName().equalsIgnoreCase("Usernumber")) {
				return Integer.parseInt(n.getValue());
			}
		}
		return STATUS_FAILURE;
	}

	public int resendEmailActivation(String username, String password) {

		NameValuePair[] s = this
				.connectCall("<clickatellsdk><action>resend_email_activation</action><user>"
						+ username
						+ "</user><password>"
						+ password
						+ "</password><connection_id>2</connection_id> </clickatellsdk>");

		for (NameValuePair n : s) {
			if (n.getName().equalsIgnoreCase("Error")
					&& n.getValue().equalsIgnoreCase("432")) {
				return STATUS_FAILURE;
			}
			if (n.getName().equalsIgnoreCase("Result")
					&& n.getValue().equalsIgnoreCase("Success")) {
				return STATUS_SUCCESS;
			}
		}

		return -1;
	}

	public String sendMsg(String api_id, String username, String password,
			String to, String text, int time_offset) {

		return apiCall("sendmsg?user=" + Uri.encode(username) + "&password="
				+ Uri.encode(password) + "&api_id=" + api_id + "&to="
				+ Uri.encode(to) + "&text=" + Uri.encode(text) + "&deliv_time=" + time_offset);
	}

	public String getBalance(String username, String password, String api_id) {
		return apiCall("getbalance?api_id=" + api_id + "&user="
				+ Uri.encode(username) + "&password=" + Uri.encode(password));
	}

	public int createApi(String username, String password) {
		NameValuePair[] s = this
				.connectCall("<clickatellsdk><action>create_connection</action><user>"
						+ username
						+ "</user><password>"
						+ password
						+ "</password><connection_id>2</connection_id> </clickatellsdk>");
		for (NameValuePair n : s) {
			if (n.getName().equalsIgnoreCase("Result")
					&& !n.getValue().equalsIgnoreCase("Success")) {
				return -1;
			}
			if (n.getName().equalsIgnoreCase("api_id")) {
				return Integer.parseInt(n.getValue());
			}
		}
		return STATUS_FAILURE;
	}

	public ArrayList<com.clickatell.bullseye.library.CountryItem> getCountries() {
		ArrayList<CountryItem> country_list = new ArrayList<CountryItem>();
		NameValuePair[] data = this
				.connectCall("<clickatellsdk><action>get_list_country</action></clickatellsdk>");
		while (data == null) {
			data = this
					.connectCall("<clickatellsdk><action>get_list_country</action></clickatellsdk>");
		}
		String country_id = "";
		String name = "";
		for (NameValuePair n : data) {
			if (n.getName().equalsIgnoreCase("country_id")) {
				country_id = n.getValue();
			} else if (n.getName().equalsIgnoreCase("name")) {
				name = n.getValue();
			} else if (n.getName().equalsIgnoreCase("Value")) {
				country_list.add(new CountryItem(name, Integer
						.parseInt(country_id)));
			}
		}
		return country_list;
	}

	public NameValuePair[] getAccountTypes() {
		return null;
	}

	public String getBuyCreditUrl(String username, String password) {
		NameValuePair[] s = this
				.connectCall("<clickatellsdk><action>buy_credits_url</action><user>"
						+ username
						+ "</user><password>"
						+ password
						+ "</password></clickatellsdk>");
		for (NameValuePair n : s) {
			if (n.getName().equalsIgnoreCase("Result")
					&& !n.getValue().equalsIgnoreCase("Success")) {
				return "";
			}
			if (n.getName().equalsIgnoreCase("buy_url")) {
				return n.getValue();
			}
		}
		return "";
	}

	public NameValuePair[] resetPassword(String email, String captcha_id,
			String captcha_value) {
		String data = "<CLICKATELLSDK>" + "<action>forgot_password</action>"
				+ "<user>" + email + "</user>" + "<email_address>" + email
				+ "</email_address>" + "<captcha_id>" + captcha_id
				+ "</captcha_id>" + "<captcha_code>" + captcha_value
				+ "</captcha_code>" + "</CLICKATELLSDK>";
		return this.connectCall(data);

	}
	


	static final String HEX_DIGITS = "0123456789ABCDEF";

	public static byte[] urlDecode(char[] cs)
			throws UnsupportedEncodingException, IllegalArgumentException {
		if (cs == null) {
			return null;
		}

		byte[] decodeBytes = new byte[cs.length];
		int decodedByteCount = 0;

		try {
			for (int count = 0; count < cs.length; count++) {
				switch (cs[count]) {
				case '+':
					decodeBytes[decodedByteCount++] = (byte) ' ';
					break;

				case '%':
					decodeBytes[decodedByteCount++] = (byte) ((HEX_DIGITS
							.indexOf(cs[++count]) << 4) + (HEX_DIGITS
							.indexOf(cs[++count])));

					break;

				default:
					decodeBytes[decodedByteCount++] = (byte) cs[count];
				}
			}

		} catch (IndexOutOfBoundsException ae) {
			throw new IllegalArgumentException("Malformed encoding");
		}

		return decodeBytes;
	}

}
