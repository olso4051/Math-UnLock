package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.ContactHashes;
import com.olyware.mathlock.service.CustomContactData;
import com.olyware.mathlock.service.GetContacts;

public class ContactHelper {
	final public static String CONTACT_PREFS = "contact_prefs";
	final public static String CONTACTS = "contacts";
	final public static String CONTACT_IS_FRIEND = "is_friend";
	final public static String CONTACT_USER_ID = "user_id";
	final public static String CONTACT_NAME = "name";
	final public static String CONTACT_PHONE = "phone";
	final public static String CONTACT_EMAIL = "email";

	public interface contactDataListener {
		public void onNewContactFound(int replaceID, CustomContactData contactData);

		public void onFriendContactFound(int id, String userID, String userName);

		public void onDoneFindingContacts();
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
					Loggy.d("test", "new Friend found, contactID = " + values[0].getContact() + " userID = " + values[0].getHiqUserID());
					listener.onFriendContactFound(values[0].getContact(), values[0].getHiqUserID(), values[0].getName());
				}
			}

			@Override
			protected void onPostExecute(Integer result) {
				listener.onDoneFindingContacts();
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
					boolean isFriend = contactJSONObject.getBoolean(CONTACT_IS_FRIEND);
					String userID = contactJSONObject.getString(CONTACT_USER_ID);
					String name = contactJSONObject.getString(CONTACT_NAME);
					JSONArray contactPhoneNumbers = contactJSONObject.getJSONArray(CONTACT_PHONE);
					JSONArray contactEmails = contactJSONObject.getJSONArray(CONTACT_EMAIL);
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

	public static List<String> getPhoneNumbersFromContacts(List<CustomContactData> contacts) {
		List<String> phones = new ArrayList<String>(contacts.size());
		for (CustomContactData contact : contacts) {
			phones.add(contact.getPhone());
		}
		return phones;
	}

	public static List<String> getEmailsFromContacts(List<CustomContactData> contacts) {
		List<String> emails = new ArrayList<String>(contacts.size());
		for (CustomContactData contact : contacts) {
			emails.add(contact.getEmail());
		}
		return emails;
	}

	public static int getNumberOfFriendsFromContacts(List<CustomContactData> contacts) {
		int friends = 0;
		for (CustomContactData contact : contacts) {
			friends += (contact.isFriend() ? 1 : 0);
		}
		return friends;
	}

	public static String getPhoneNumberFromString(String phoneNumber) {
		phoneNumber = phoneNumber.replaceAll("[^\\d]", "");
		phoneNumber = (phoneNumber.charAt(0) == '1') ? phoneNumber.substring(1) : phoneNumber;
		return phoneNumber;
	}

	public static String getPhoneHashFromString(String phoneNumber) {
		return new EncryptionHelper().encryptForURL(getPhoneNumberFromString(phoneNumber));
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

	public static void findContact(FindType findType, List<CustomContactData> contacts, List<ContactHashes> searches,
			final friendDataListener listener) {
		// List<ArrayList<Integer>> matchingIndex = new ArrayList<ArrayList<Integer>>(searches.size());
		switch (findType) {
		case NAME:
			break;
		case PhoneAndFacebookHASH:
			Loggy.d("test", "searching contacts");
			Loggy.d("test", "contacts.size() = " + contacts.size());
			Loggy.d("test", "searches.size() = " + searches.size());
			for (int i = 0; i < contacts.size(); i++) {
				boolean found = false;
				CustomContactData contact = contacts.get(i);
				// Search contacts facebook hashes
				for (int location = 0; location < searches.size(); location++) {
					String search = searches.get(location).getFacebookHash();
					if (contact.getFacebookHash() != null && contact.getFacebookHash().equals(search)) {
						// matchingIndex.get(location).add(i);
						Loggy.d("test", "sending facebook friend to listener");
						listener.onFriendContactFound(i, location);
						found = true;
						break;
					}
				}

				// Search contacts phone hashes
				if (!found) {
					List<String> phoneHashes = new ArrayList<String>();
					phoneHashes.addAll(contact.getPhoneHashs());
					for (String phoneHash : phoneHashes) {
						for (int location = 0; location < searches.size(); location++) {
							String search = searches.get(location).getPhoneHash();
							if (phoneHash != null && phoneHash.equals(search)) {
								// matchingIndex.get(location).add(i);
								Loggy.d("test", "sending contacts friend to listener");
								listener.onFriendContactFound(i, location);
								found = true;
								break;
							}
						}
						if (found)
							break;
					}
				}
			}
			break;
		case EMAIL:
			break;
		}
		// return matchingIndex;
	}

	public static CustomContactData findContact(Context ctx, FindType findType, ContactHashes search) {
		List<CustomContactData> contacts = new ArrayList<CustomContactData>();
		contacts.addAll(getStoredContacts(ctx));
		switch (findType) {
		case NAME:
			break;
		case PhoneAndFacebookHASH:
			for (int i = 0; i < contacts.size(); i++) {
				CustomContactData contact = contacts.get(i);
				// Search facebook hashes
				if (contact.getFacebookHash() != null && !contact.getFacebookHash().equals("") && search.getFacebookHash() != null
						&& !search.getFacebookHash().equals("")) {
					if (contact.getFacebookHash().equals(search.getFacebookHash())) {
						// matchingIndex.get(location).add(i);
						return contact;
					}
				}

				// Search phone hashes
				List<String> phoneHashes = new ArrayList<String>();
				phoneHashes.addAll(contact.getPhoneHashs());
				if (search.getPhoneHash() != null && !search.getPhoneHash().equals("")) {
					for (String phoneHash : phoneHashes) {
						if (phoneHash != null && phoneHash.equals(search.getPhoneHash())) {
							return contact;
						}
					}
				}
			}
			break;
		case EMAIL:
			break;
		}
		return new CustomContactData();
	}

	public static String getGCMID(Context ctx) {
		return GCMHelper.getRegistrationId(ctx.getApplicationContext());
	}

	public static String getUserID(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_userid), "");
	}

	public static String getFaceID(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_facebook_id), "");
	}

	public static String getReferrer(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_referrer), "");
	}

	public static String getBirthday(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_facebook_birth), "");
	}

	public static String getGender(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_facebook_gender), "");
	}

	public static String getLocation(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_facebook_location), "");
	}

	public static String getEmail(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_facebook_email), "");
	}

	public static String getUserName(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_username), "");
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
		NAME, PhoneAndFacebookHASH, EMAIL;
	}
}
