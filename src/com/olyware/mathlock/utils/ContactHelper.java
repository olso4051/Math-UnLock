package com.olyware.mathlock.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.ContactHashes;
import com.olyware.mathlock.service.CustomContactData;
import com.olyware.mathlock.service.RefreshContacts;

public class ContactHelper {
	final public static String CONTACT_PREFS = "contact_prefs";
	final public static String USER_PHONE = "phone_number";
	final public static String USER_PHONE_HASH = "phone_hash";
	final public static String CONTACTS = "contacts";
	final public static String CONTACT_IS_FRIEND = "is_friend";
	final public static String CONTACT_HIQ_USER_ID = "hiq_user_id";
	final public static String CONTACT_HIQ_NAME = "hiq_name";
	final public static String CONTACT_FACEBOOK_USER_ID = "facebook_user_id";
	final public static String CONTACT_FACEBOOK_NAME = "facebook_name";
	final public static String CONTACT_NAME = "name";
	final public static String CONTACT_PHONE = "phone";
	final public static String CONTACT_EMAIL = "email";

	public interface contactDataListener {
		public void onNewContactFound(int replaceID, CustomContactData contactData);

		public void onFriendContactFound(int id, String userID, String userName);

		public void onDoneFindingContacts(List<ContactHashes> hashes);
	}

	public interface friendDataListener {
		public void onFriendContactFound(int contact, int id);
	}

	public static List<ContactHashes> getUniqueContactHashes(List<ContactHashes> list) {
		Set<ContactHashes> unique = new LinkedHashSet<ContactHashes>(list);
		return new ArrayList<ContactHashes>(unique);
	}

	public static RefreshContacts getCustomContactDataAsync(final Context ctx, List<CustomContactData> contacts, boolean refreshPhonebook,
			final contactDataListener listener) {
		// TODO user phone number instead of ""
		RefreshContacts c = new RefreshContacts(ctx, contacts, refreshPhonebook) {
			@Override
			protected void onProgressUpdate(CustomContactData... values) {
				if (!isCancelled()) {
					if (values[0].isContact()) {
						List<String> emailsTemp = new ArrayList<String>();
						emailsTemp.addAll(values[0].getEmails());
						int replaceID = Integer.parseInt(values[0].getEmails().get(emailsTemp.size() - 1));
						values[0].getEmails().remove(emailsTemp.size() - 1);
						listener.onNewContactFound(replaceID, values[0]);
					} else {
						Loggy.d("test",
								"new Friend found, contactID = " + values[0].getContacts().toString() + " userID = "
										+ values[0].getHiqUserID() + " userName = " + values[0].getDisplayName());
						listener.onFriendContactFound(values[0].getContact(), values[0].getHiqUserID(), values[0].getHiqUserName());
					}
				}
			}

			@Override
			protected void onPostExecute(Integer result) {
				if (!isCancelled())
					listener.onDoneFindingContacts(getUserHashes());
			}

		};
		c.execute();
		return c;
	}

	public static void storeContacts(Context ctx, List<CustomContactData> contacts) {
		SharedPreferences.Editor editorPrefsContacts = ctx.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE).edit();
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
		Loggy.d("contacts to store = " + contactsJSON);
		editorPrefsContacts.putString(CONTACTS, contactsJSON).commit();
	}

	public static List<CustomContactData> getStoredContacts(Context ctx) {
		SharedPreferences sharedPrefsContacts = ctx.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE);
		String contactsJSON = sharedPrefsContacts.getString(CONTACTS, "");
		if (!contactsJSON.equals("")) {
			try {
				List<CustomContactData> contacts = new ArrayList<CustomContactData>();
				Loggy.d("contacts to get = " + contactsJSON);
				JSONArray contactsJSONArray = new JSONArray(contactsJSON);
				for (int i = 0; i < contactsJSONArray.length(); i++) {
					JSONObject contactJSONObject = contactsJSONArray.getJSONObject(i);
					boolean isFriend = contactJSONObject.getBoolean(CONTACT_IS_FRIEND);
					String hiqUserID = contactJSONObject.getString(CONTACT_HIQ_USER_ID);
					String hiqName = contactJSONObject.getString(CONTACT_HIQ_NAME);
					String facebookUserID = contactJSONObject.getString(CONTACT_FACEBOOK_USER_ID);
					String facebookName = contactJSONObject.getString(CONTACT_FACEBOOK_NAME);
					String name = contactJSONObject.getString(CONTACT_NAME);
					JSONArray contactPhoneNumbers = contactJSONObject.getJSONArray(CONTACT_PHONE);
					JSONArray contactEmails = contactJSONObject.getJSONArray(CONTACT_EMAIL);
					String challengeID = PreferenceHelper.getChallengeIDFromHiqUserID(ctx, hiqUserID);
					CustomContactData.ChallengeState state = PreferenceHelper.getChallengeStateFromID(ctx, challengeID);
					contacts.add(new CustomContactData(hiqUserID, hiqName, facebookUserID, facebookName, name,
							getStringListFromJSONArray(contactEmails), getStringListFromJSONArray(contactPhoneNumbers), isFriend, state,
							challengeID));
				}
				return contacts;
			} catch (JSONException e) {
				return new ArrayList<CustomContactData>();
			}
		} else
			return new ArrayList<CustomContactData>();
	}

	public static int getNumberOfChallenges(Context ctx) {
		SharedPreferences sharedPrefsContacts = ctx.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE);
		String contactsJSON = sharedPrefsContacts.getString(CONTACTS, "");
		if (!contactsJSON.equals("")) {
			try {
				int newChallenges = 0;
				JSONArray contactsJSONArray = new JSONArray(contactsJSON);
				for (int i = 0; i < contactsJSONArray.length(); i++) {
					JSONObject contactJSONObject = contactsJSONArray.getJSONObject(i);
					String hiqUserID = contactJSONObject.getString(CONTACT_HIQ_USER_ID);
					String challengeID = PreferenceHelper.getChallengeIDFromHiqUserID(ctx, hiqUserID);
					CustomContactData.ChallengeState state = PreferenceHelper.getChallengeStateFromID(ctx, challengeID);
					if (state.equals(CustomContactData.ChallengeState.New) || state.equals(CustomContactData.ChallengeState.Active)) {
						newChallenges++;
					}
				}
				return newChallenges;
			} catch (JSONException e) {
				return 0;
			}
		} else
			return 0;
	}

	public static void removeStoredContacts(Context ctx) {
		SharedPreferences.Editor editorPrefsContacts = ctx.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE).edit();
		editorPrefsContacts.putString(CONTACTS, "").commit();
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

	public static List<String> getFirstPhoneNumbersFromContacts(List<CustomContactData> contacts) {
		List<String> phones = new ArrayList<String>(contacts.size());
		for (CustomContactData contact : contacts) {
			phones.add(contact.getPhoneNumber());
		}
		return phones;
	}

	public static List<String> getFirstEmailsFromContacts(List<CustomContactData> contacts) {
		List<String> emails = new ArrayList<String>(contacts.size());
		for (CustomContactData contact : contacts) {
			emails.add(contact.getEmail());
		}
		return emails;
	}

	public static List<String> getFirstFacebookHashesFromContacts(List<CustomContactData> contacts) {
		List<String> hashes = new ArrayList<String>(contacts.size());
		for (CustomContactData contact : contacts) {
			hashes.add(contact.getFacebookHash());
		}
		return hashes;
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

	public static List<String> getPhoneNumbersFromStrings(List<String> phoneNumbers) {
		List<String> phoneNumbersEdited = new ArrayList<String>(phoneNumbers.size());
		for (String phoneNumber : phoneNumbers) {
			phoneNumbersEdited.add(getPhoneNumberFromString(phoneNumber));
		}
		return phoneNumbersEdited;
	}

	public static String getPhoneHashFromString(String phoneNumber) {
		return EncryptionHelper.encryptForURL(getPhoneNumberFromString(phoneNumber));
	}

	public static List<String> getPhoneHashes(List<String> phoneNumbers) {
		List<String> phoneNumberEncrypted = new ArrayList<String>(phoneNumbers.size());
		if (phoneNumbers.size() > 0) {
			for (int i = 0; i < phoneNumbers.size(); i++) {
				phoneNumberEncrypted.add(EncryptionHelper.encryptForURL(phoneNumbers.get(i)));
			}
		}
		return phoneNumberEncrypted;
	}

	public static void findContact(FindType findType, List<CustomContactData> contacts, List<ContactHashes> searches,
			final friendDataListener listener) {

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
					String facebookHash = contact.getFacebookHash();
					if (facebookHash != null && search != null) {
						if (!facebookHash.equals("") && !search.equals("") && facebookHash.equals(search)) {
							Loggy.d("contact facebook = " + contact.getFacebookHash() + " |search = " + search);
							// List<Integer> ints = findContacts(findType, i, contacts, searches.get(location));
							Loggy.d("test", "sending facebook friend to listener");
							listener.onFriendContactFound(i, location);
							found = true;
							break;
						}
					}
				}

				// Search contacts phone hashes
				if (!found) {
					List<String> phoneHashes = new ArrayList<String>();
					phoneHashes.addAll(contact.getPhoneHashs());
					for (String phoneHash : phoneHashes) {
						for (int location = 0; location < searches.size(); location++) {
							String search = searches.get(location).getPhoneHash();
							if (phoneHash != null && search != null) {
								if (!phoneHash.equals("") && !search.equals("") && phoneHash.equals(search)) {
									// List<Integer> ints = findContacts(findType, i, contacts, searches.get(location));
									Loggy.d("test", "sending contacts friend to listener");
									listener.onFriendContactFound(i, location);
									found = true;
									break;
								}
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

	public static List<Integer> findContacts(FindType findType, int start, List<CustomContactData> contacts, ContactHashes search) {
		List<Integer> indexes = new ArrayList<Integer>();
		if (start < contacts.size()) {
			switch (findType) {
			case NAME:
				break;
			case PhoneAndFacebookHASH:
				Loggy.d("test", "searching contacts");
				Loggy.d("test", "contacts.size() = " + contacts.size());
				Loggy.d("test", "start = " + start);
				for (int i = start; i < contacts.size(); i++) {
					CustomContactData contact = contacts.get(i);
					// Search contacts facebook hashes
					if (!search.getFacebookHash().equals("")) {
						if (contact.getFacebookHash() != null && contact.getFacebookHash().equals(search.getFacebookHash())) {
							if (!indexes.contains(i))
								indexes.add(i);
						}
					}
					// Search contacts phone hashes
					if (!search.getPhoneHash().equals("")) {
						List<String> phoneHashes = new ArrayList<String>();
						phoneHashes.addAll(contact.getPhoneHashs());
						for (String phoneHash : phoneHashes) {
							if (phoneHash.equals(search.getPhoneHash())) {
								if (!indexes.contains(i)) {
									indexes.add(i);
									break;
								}
							}
						}
					}
				}
				break;
			case EMAIL:
				break;
			}
		}
		return indexes;
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

	public static String storeUserPhoneNumber(Context ctx, String number) {
		String numberEncrypted = getPhoneHashFromString(number);
		SharedPreferences.Editor editorPrefsContacts = ctx.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE).edit();
		editorPrefsContacts.putString(USER_PHONE, number);
		editorPrefsContacts.putString(USER_PHONE_HASH, numberEncrypted).commit();
		return numberEncrypted;
	}

	public static String getUserPhoneNumber(Context ctx) {
		SharedPreferences sharedPrefsContacts = ctx.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE);
		String number = sharedPrefsContacts.getString(USER_PHONE, "");
		if (number.equals("")) {
			TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
			number = telephonyManager.getLine1Number();
			if (number == null || number.length() <= 0) {
				return "";
			} else {
				storeUserPhoneNumber(ctx, number);
			}
		}
		return number;
	}

	public static String getUserPhoneHash(Context ctx) {
		SharedPreferences sharedPrefsContacts = ctx.getSharedPreferences(CONTACT_PREFS, Context.MODE_PRIVATE);
		return sharedPrefsContacts.getString(USER_PHONE_HASH, "");
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

	public static String getFacebookUserName(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_facebook_name), "");
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
		String hiqUserName = sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_username), "");
		String facebookUserName = sharedPrefsUserInfo.getString(ctx.getString(R.string.pref_user_facebook_name), "");
		return CustomContactData.getDisplayName(facebookUserName, hiqUserName);
	}

	public static boolean isUserConfirmed(Context ctx) {
		SharedPreferences sharedPrefsUserInfo = ctx.getSharedPreferences(ctx.getString(R.string.pref_user_info), Context.MODE_PRIVATE);
		return sharedPrefsUserInfo.getBoolean(ctx.getString(R.string.pref_user_confirmed), false);
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
