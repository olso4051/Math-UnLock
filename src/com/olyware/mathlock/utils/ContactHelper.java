package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.olyware.mathlock.service.CustomContactData;
import com.olyware.mathlock.service.GetContacts;

public class ContactHelper {
	final public static String CONTACT_PREFS = "contact_prefs";
	final public static String CONTACTS = "contacts";

	public interface contactDataListener {
		public void onNewContactFound(int replaceID, CustomContactData contactData);

		public void onFriendContactFound(int id, String userID);

		public void onDoneFindingContacts(List<String> userPhoneHashes, List<String> userIDHashes);
	}

	public interface friendDataListener {
		public void onFriendContactFound(int contact, int id);
	}

	public static void getCustomContactDataAsync(final Context ctx, List<CustomContactData> contacts, final contactDataListener listener) {
		new GetContacts(ctx, contacts) {
			@Override
			protected void onProgressUpdate(CustomContactData... values) {
				if (values[0].isContact()) {
					List<String> emailsTemp = new ArrayList<String>();
					emailsTemp.addAll(values[0].getEmails());
					int replaceID = Integer.parseInt(values[0].getEmails().get(emailsTemp.size() - 1));
					values[0].getEmails().remove(emailsTemp.size() - 1);
					listener.onNewContactFound(replaceID, values[0]);
				} else {
					Log.d("test", "new Friend found, contactID = " + values[0].getContact() + " userID = " + values[0].getHiqUserID());
					listener.onFriendContactFound(values[0].getContact(), values[0].getHiqUserID());
				}
			}

			@Override
			protected void onPostExecute(Integer result) {
				listener.onDoneFindingContacts(getUserPhoneHashes(), getUserIDHashes());
			}

		}.execute();
	}

	public static void storeContacts(Context ctx, List<CustomContactData> contacts) {
		SharedPreferences.Editor editSharedPrefs = ctx.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE).edit();
		String contactsJSON = "[";
		boolean first = true;
		for (CustomContactData contact : contacts) {
			if (first) {
				contactsJSON += contact.getJSON();
				first = false;
			} else
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
					boolean isFriend = contactJSONObject.getBoolean("is_friend");
					String name = contactJSONObject.getString("name");
					JSONArray contactPhoneNumbers = contactJSONObject.getJSONArray("phone");
					JSONArray contactEmails = contactJSONObject.getJSONArray("email");
					contacts.add(new CustomContactData(name, getStringListFromJSONArray(contactEmails),
							getStringListFromJSONArray(contactPhoneNumbers), isFriend));
				}
				return contacts;
			} catch (JSONException e) {
				return new ArrayList<CustomContactData>();
			}
		} else
			return new ArrayList<CustomContactData>();
	}

	public static List<String> getNamesFromContacts(List<CustomContactData> contacts) {
		List<String> names = new ArrayList<String>(contacts.size());
		for (CustomContactData contact : contacts) {
			names.add(contact.getName());
		}
		return names;
	}

	public static List<String> getNamesLowercaseFromContacts(List<CustomContactData> contacts) {
		List<String> names = new ArrayList<String>(contacts.size());
		for (CustomContactData contact : contacts) {
			names.add(contact.getName().toLowerCase(Locale.ENGLISH));
		}
		return names;
	}

	public static int getNumberOfFriendsFromContacts(List<CustomContactData> contacts) {
		int friends = 0;
		for (CustomContactData contact : contacts) {
			friends += (contact.isFriend() ? 1 : 0);
		}
		return friends;
	}

	public static List<String> getPhoneHashes(List<String> phoneNumbers) {
		List<String> phoneNumberEncrypted = new ArrayList<String>(phoneNumbers.size());
		if (phoneNumbers.size() > 0) {
			for (int i = 0; i < phoneNumbers.size(); i++) {
				phoneNumberEncrypted.add(new EncryptionHelper().encryptForURL(phoneNumbers.get(i)));
			}
		}
		return phoneNumberEncrypted;
	}

	public static void findContact(FindType findType, List<CustomContactData> contacts, List<String> searches,
			final friendDataListener listener) {
		// List<ArrayList<Integer>> matchingIndex = new ArrayList<ArrayList<Integer>>(searches.size());
		switch (findType) {
		case NAME:
			break;
		case PHONEHASH:
			Log.d("test", "searching contacts");
			Log.d("test", "contacts.size() = " + contacts.size());
			Log.d("test", "searches.size() = " + searches.size());
			for (int i = 0; i < contacts.size(); i++) {
				CustomContactData contact = contacts.get(i);
				List<String> phoneHashes = new ArrayList<String>();
				phoneHashes.addAll(contact.getPhoneHashs());
				for (String phoneHash : phoneHashes) {
					for (int location = 0; location < searches.size(); location++) {
						String search = searches.get(location);
						if (phoneHash != null && phoneHash.equals(search)) {
							// matchingIndex.get(location).add(i);
							Log.d("test", "sending friend to listener");
							listener.onFriendContactFound(i, location);
						}
					}
				}
			}
			break;
		case EMAIL:
			break;
		}
		// return matchingIndex;
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

	public static enum FindType {
		NAME, PHONEHASH, EMAIL;
	}
}
