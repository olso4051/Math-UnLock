package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.olyware.mathlock.service.CustomContactData;

public class ContactHelper {
	final public static String CONTACT_PREFS = "contact_prefs";
	final public static String CONTACTS = "contacts";

	public interface contactDataListener {
		public void onReceived(List<CustomContactData> contactData);
	}

	public static void getCustomContactDataAsync(final Context ctx, final contactDataListener listener) {
		new AsyncTask<Void, Void, List<CustomContactData>>() {
			@Override
			protected List<CustomContactData> doInBackground(Void... params) {
				ContentResolver cr = ctx.getContentResolver();
				Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
				List<String> phoneNumbers = new ArrayList<String>();
				List<String> allPhoneNumbers = new ArrayList<String>();
				List<String> emails = new ArrayList<String>();
				List<String> allEmails = new ArrayList<String>();
				boolean isPerson = false;
				String name, id;
				List<String> allNames = new ArrayList<String>();
				int replaceID = 0;
				List<CustomContactData> contacts = new ArrayList<CustomContactData>();
				contacts.clear();
				phoneNumbers.clear();
				emails.clear();
				if (cur.getCount() > 0) {
					while (cur.moveToNext()) {
						id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
						name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						String nameLo = name.toLowerCase(Locale.ENGLISH);
						if (name.charAt(0) != '#') {
							if (allNames.contains(nameLo))
								replaceID = allNames.indexOf(nameLo);
							if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
								Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
								while (pCur.moveToNext()) {
									String number = getPhoneNumberFromString(pCur.getString(pCur
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
									if (number.length() >= 7 && !allPhoneNumbers.contains(number)) {
										isPerson = true;
										if (replaceID < 0)
											phoneNumbers.add(number);
										else
											contacts.get(replaceID).getPhoneNumbers().add(number);
										allPhoneNumbers.add(number);
									}
								}
								pCur.close();
								Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
										ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { id }, null);
								while (emailCur.moveToNext()) {
									String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
									if (email.length() >= 5 && !allEmails.contains(email)) {
										if (replaceID < 0)
											emails.add(email);
										else
											contacts.get(replaceID).getEmails().add(email);
										allEmails.add(email);
										// emailType =
										// emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
									}
								}
								emailCur.close();
								if (isPerson && replaceID < 0) {
									allNames.add(name.toLowerCase(Locale.ENGLISH));
									contacts.add(new CustomContactData(name, emails, phoneNumbers));
								}
								replaceID = -1;
								isPerson = false;
								emails.clear();
								phoneNumbers.clear();
							}
						}
					}
				}
				Collections.sort(contacts);
				storeContacts(ctx, contacts);
				return contacts;
			}

			@Override
			protected void onPostExecute(List<CustomContactData> result) {
				listener.onReceived(result);
			}

		}.execute();
	}

	public static void storeContacts(Context ctx, List<CustomContactData> contacts) {
		SharedPreferences.Editor editSharedPrefs = ctx.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE).edit();
		String contactsJSON = "[";
		boolean first = true;
		for (CustomContactData contact : contacts) {
			if (first)
				contactsJSON += contact.getJSON();
			else
				contactsJSON += "," + contact.getJSON();
		}
		contactsJSON += "]";
		editSharedPrefs.putString(CONTACTS, contactsJSON).commit();
	}

	public static List<CustomContactData> getStoredContacts(Context ctx) {
		SharedPreferences sharedPrefs = ctx.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE);
		String contactsJSON = sharedPrefs.getString(CONTACTS, null);
		if (contactsJSON != null) {
			try {
				List<CustomContactData> contacts = new ArrayList<CustomContactData>();
				JSONArray contactsJSONArray = new JSONArray(contactsJSON);
				for (int i = 0; i < contactsJSONArray.length(); i++) {
					JSONObject contactJSONObject = contactsJSONArray.getJSONObject(i);
					String name = contactJSONObject.getString("name");
					JSONArray contactPhoneNumbers = contactJSONObject.getJSONArray("phone");
					JSONArray contactEmails = contactJSONObject.getJSONArray("email");
					contacts.add(new CustomContactData(name, getStringListFromJSONArray(contactEmails),
							getStringListFromJSONArray(contactPhoneNumbers)));
				}
				return contacts;
			} catch (JSONException e) {
				return new ArrayList<CustomContactData>();
			}
		} else
			return new ArrayList<CustomContactData>();
	}

	public static String getPhoneNumberFromString(String phoneNumber) {
		return phoneNumber.replaceAll("[^\\d]", "");
	}

	private static List<String> getStringListFromJSONArray(JSONArray array) {
		List<String> list = new ArrayList<String>();
		try {
			for (int i = 0; i < array.length(); i++) {
				list.add(array.getString(i));
			}
		} catch (JSONException e) {
			return new ArrayList<String>();
		}
		return list;
	}
}
