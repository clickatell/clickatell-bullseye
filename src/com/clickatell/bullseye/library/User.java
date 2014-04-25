package com.clickatell.bullseye.library;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.PhoneLookup;

public class User {
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String API_ID = "api_id";
	public static final String USER_NUMBER = "user_number";
	public static final String DEFAULT_GROUP = "default_group";
	public static final String TEST_MODE = "test_mode";

	public static boolean isLoggedIn(Context context) {
		return get(context, USERNAME).length() > 0;
	}

	public static String get(Context context, String name) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getString(name, "");
	}

	public static void set(Context context, String name, String value) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = settings.edit();
		edit.putString(name, value);
		edit.apply();
	}

	public static Contact[] getGroups(Context context) {
		ArrayList<Contact> groups = new ArrayList<Contact>();
		groups.add(new Contact(-1, "Please Select One..."));
		Cursor tempCur = context.getContentResolver().query(Groups.CONTENT_URI,
				new String[] { Groups._ID, Groups.TITLE }, null, null, null);

		tempCur.moveToFirst();
		while (tempCur.moveToNext()) {
			groups.add(new Contact(tempCur.getLong(0), tempCur.getString(1)));
		}
		tempCur.close();
		return groups.toArray(new Contact[0]);
	}

	public static Contact[] getContacts(Context context, long group_id) {

		ArrayList<Contact> contacts = new ArrayList<Contact>();

		String[] cProjection = { Contacts.DISPLAY_NAME,
				GroupMembership.CONTACT_ID };

		Cursor groupCursor = context
				.getContentResolver()
				.query(Data.CONTENT_URI,
						cProjection,
						CommonDataKinds.GroupMembership.GROUP_ROW_ID
								+ "= ?"
								+ " AND "
								+ ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE
								+ "='"
								+ ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
								+ "'",
						new String[] { String.valueOf(group_id) }, null);
		if (groupCursor.moveToFirst()) {
			do {

				int nameCoumnIndex = groupCursor
						.getColumnIndex(Phone.DISPLAY_NAME);
				int idCoumnIndex = groupCursor.getColumnIndex(Phone.CONTACT_ID);

				String name = groupCursor.getString(nameCoumnIndex);
				long id = groupCursor.getLong(idCoumnIndex);
				Contact contact = new Contact(id, name);

				long contactId = groupCursor.getLong(groupCursor
						.getColumnIndex(GroupMembership.CONTACT_ID));

				Cursor numberCursor = context.getContentResolver().query(
						Phone.CONTENT_URI,
						new String[] { Phone.NUMBER, Phone.NORMALIZED_NUMBER,
								Phone.TYPE },
						Phone.CONTACT_ID + "=" + contactId, null, null);

				if (numberCursor.moveToFirst()) {
					int i1 = numberCursor.getColumnIndex(Phone.NUMBER);
					int i2 = numberCursor
							.getColumnIndex(Phone.NORMALIZED_NUMBER);
					int i3 = numberCursor.getColumnIndex(Phone.TYPE);
					do {
						String phone = numberCursor.getString(i1);
						String normalized = numberCursor.getString(i2);
						long type = numberCursor.getLong(i3);
						if (type == 2)
							contact.addNumber(new Contact(type, normalized,
									phone));
					} while (numberCursor.moveToNext());
					
				}
				numberCursor.close();
				contacts.add(contact);
			} while (groupCursor.moveToNext());
		}
		groupCursor.close();

		HashMap<String, Contact> hm_contacts = new HashMap<String, Contact>();

		for (Contact c : contacts) {
			if (!hm_contacts.containsKey(c.name) && c.numbers.size() > 0) {
				hm_contacts.put(c.numbers.get(0).name, c);
			}
		}
		return hm_contacts.values().toArray(new Contact[0]);
	}

	public static String normalizeNumber(Context context, String number) {
		String ret = "";
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String[] projection = new String[] { PhoneLookup.NUMBER,
				PhoneLookup.NORMALIZED_NUMBER };
		Cursor c = context.getContentResolver().query(uri, projection, null,
				null, null);
		if (c.moveToFirst()) {
			ret = c.getString(c
					.getColumnIndexOrThrow(PhoneLookup.NORMALIZED_NUMBER));
		}
		c.close();
		return ret;
	}

	public static boolean isValidEmail(String u) {
		if (u == null) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(u).matches();
		}
	}

	public static int testValidPassword(CharSequence target) {
		if (target == null) {
			return 0;
		}
		if (target.length() < 5) {
			return 1;
		}
		if (!target.toString().matches(".*[0-9].*")) {
			return 2;
		}
		if (!target.toString().matches(".*[a-zA-Z].*")) {
			return 3;
		}

		return -1;
	}
}
