package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.Toast;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.ContactHashes;
import com.olyware.mathlock.utils.ContactHelper;
import com.olyware.mathlock.utils.Loggy;

public class GetContacts extends AsyncTask<Void, CustomContactData, Integer> {
	final private static String FriendsEndpoint = "friends";
	private List<ContactHashes> userHashes = new ArrayList<ContactHashes>();
	private List<String> allNames = new ArrayList<String>();
	private List<String> allPhoneNumbers = new ArrayList<String>();
	private List<String> allEmails = new ArrayList<String>();
	private List<String> allFacebookHashes = new ArrayList<String>();
	private List<CustomContactData> allContacts = new ArrayList<CustomContactData>();
	private String notNumber;
	private String baseURL;
	private Context ctx;

	public GetContacts(Context ctx, List<CustomContactData> contacts) {
		this.ctx = ctx;
		allContacts.clear();
		allContacts.addAll(contacts);
		this.notNumber = ContactHelper.getUserPhoneNumber(ctx);
		baseURL = ctx.getString(R.string.service_base_url);
	}

	@Override
	protected Integer doInBackground(Void... v) {
		allFacebookHashes.clear();
		allFacebookHashes.addAll(ContactHelper.getFirstFacebookHashesFromContacts(allContacts));
		// Get Contacts from user's facebook
		List<String> phoneNumbers = new ArrayList<String>();
		List<String> allEncryptedPhoneNumbers = new ArrayList<String>();
		List<String> emails = new ArrayList<String>();
		boolean isPerson = false;
		String name, id;
		// List<String> allNames = new ArrayList<String>();
		int replaceID = -1;
		final Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			if (session.getPermissions().contains("user_friends")) {
				// Get the user's list of friends
				Request friendsRequest = new Request(session, "/me/friends");
				Response response = friendsRequest.executeAndWait();
				try {
					JSONObject responseJSON = new JSONObject(response.getRawResponse());
					JSONArray data = responseJSON.getJSONArray("data");
					if (data.length() > 0) {
						for (int i = 0; i < data.length(); i++) {
							String facebookName = ((JSONObject) data.get(i)).getString("name");
							/*String nameLo = name.toLowerCase(Locale.ENGLISH);
							if (allNames.contains(nameLo)) {
								replaceID = allNames.indexOf(nameLo);
							}*/
							String facebookID = ((JSONObject) data.get(i)).getString("id");
							if (allFacebookHashes.contains(facebookID)) {
								replaceID = allFacebookHashes.indexOf(facebookID);
							}

							CustomContactData contact = new CustomContactData(facebookName, facebookID);
							if (replaceID == -1) {
								allContacts.add(contact);
								Collections.sort(allContacts);
								allFacebookHashes.clear();
								allFacebookHashes.addAll(ContactHelper.getFirstFacebookHashesFromContacts(allContacts));
							}
							contact.getEmails().add(String.valueOf(replaceID));
							publishProgress(contact);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else {
			Toast.makeText(ctx, ctx.getString(R.string.fragment_challenge_facebook_prompt), Toast.LENGTH_LONG).show();
		}
		allNames.clear();
		allNames.addAll(ContactHelper.getNamesLowercaseFromContacts(allContacts));
		allPhoneNumbers.clear();
		allPhoneNumbers.addAll(ContactHelper.getFirstPhoneNumbersFromContacts(allContacts));
		allEmails.clear();
		allEmails.addAll(ContactHelper.getFirstEmailsFromContacts(allContacts));
		// Get contacts from user's contacts
		ContentResolver cr = ctx.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
			// next stored contact
			while (cur.moveToNext()) {
				id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));				// store ID for getting the email and phone number
				name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));	// name of the contact
				String nameLo = name.toLowerCase(Locale.ENGLISH);									// lower case name for comparing other contacts
				if (name.charAt(0) != '#') {														// don't add contacts that start with #
					// add info to names that we've already found
					if (allNames.contains(nameLo)) {
						replaceID = allNames.indexOf(nameLo);
					}
					// only add a contact if they have a phone number
					if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0
							|| replaceID >= 0) {
						Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
						while (pCur.moveToNext()) {
							// could use the next line to restrict types of numbers
							// int phoneType = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
							String number = ContactHelper.getPhoneNumberFromString(pCur.getString(pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
							// only add phone numbers we haven't found yet
							if (!number.equals(notNumber) && number.length() >= 7 && !allPhoneNumbers.contains(number)) {
								isPerson = true;
								phoneNumbers.add(number);
								allPhoneNumbers.add(number);
							}
						}
						pCur.close();
						Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
								ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { id }, null);
						while (emailCur.moveToNext()) {
							String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
							// only add emails we haven't found yet
							if (email.length() >= 5 && !allEmails.contains(email)) {
								isPerson = true;
								emails.add(email);
								allEmails.add(email);
							}
						}
						emailCur.close();
						if (isPerson || replaceID >= 0) {
							CustomContactData contact = new CustomContactData(name, emails, phoneNumbers);
							if (replaceID == -1) {
								allContacts.add(contact);
								Collections.sort(allContacts);
								allNames.clear();
								allNames.addAll(ContactHelper.getNamesLowercaseFromContacts(allContacts));
							}
							contact.addEmail(String.valueOf(replaceID));
							publishProgress(contact);
						}
					}
				}
				replaceID = -1;
				isPerson = false;
				emails.clear();
				phoneNumbers.clear();
			}
		}
		allEncryptedPhoneNumbers.addAll(ContactHelper.getPhoneHashes(allPhoneNumbers));
		cur.close();

		// POST to API to get user_ids of contacts and facebook friends
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost httppost = new HttpPost(baseURL + FriendsEndpoint);
		HttpEntity entity;
		String fullResult;
		JSONArray jsonResponse;
		try {
			JSONObject data = new JSONObject();
			JSONArray phoneHashes = new JSONArray();
			// for (int i = 0; i < Math.min(allEncryptedPhoneNumbers.size(), 5); i++) {
			// String encryptedPhoneNumber = allEncryptedPhoneNumbers.get(i);
			for (String encryptedPhoneNumber : allEncryptedPhoneNumbers) {
				phoneHashes.put(encryptedPhoneNumber);
			}
			data.put("phone_hashes", phoneHashes);

			JSONArray facebookHashes = new JSONArray();
			// for (int i = 0; i < Math.min(allFacebookHashes.size(), 5); i++) {
			// String facebookID = allFacebookHashes.get(i);
			for (String facebookID : allFacebookHashes) {
				facebookHashes.put(facebookID);
			}
			data.put("facebook_hashes", facebookHashes);

			String st = data.toString();
			int end = st.length();
			int output = 1000;
			int endSub = Math.min(output, end);
			int start = 0;
			String line = data.toString().substring(start, endSub);
			Loggy.d(line);
			while (endSub < end) {
				start = endSub;
				endSub = Math.min(endSub + output, end);
				line = data.toString().substring(start, endSub);
				Loggy.d(line);
			}
			// Loggy.d("JSON to get friends = " + data.toString());
			httppost.setEntity(new StringEntity(data.toString(), ContentType.create("text/plain", "UTF-8")));
			httppost.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httppost);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			Loggy.d("fullResult = " + fullResult);
			jsonResponse = new JSONArray(fullResult);
			if (entity != null && fullResult != null && jsonResponse != null) {
				getFriendsFromJSON(jsonResponse);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// for testing we'll assume we got 5 random contacts back from the service
		// for (int i = 2; i < 10; i++) {
		// userHashes.add(new ContactHashes(allEncryptedPhoneNumbers.get(6), "262217307306251",
		// "34d90771d25aabbe6bfd39bb9514d3c3","Kyle Tester"));
		// userHashes.add(new ContactHashes(allEncryptedPhoneNumbers.get(20), "13959212", "5f14e3e2e9738b9d50ca3334a438c954",
		// "Kyle Olson"));
		// }

		Collections.sort(allContacts);
		if (userHashes.size() > 0) {
			ContactHelper.findContact(ContactHelper.FindType.PhoneAndFacebookHASH, allContacts, userHashes,
					new ContactHelper.friendDataListener() {
						@Override
						public void onFriendContactFound(int contact, int id) {
							CustomContactData contactData = new CustomContactData(contact, userHashes.get(id).getHiqUserHash(), userHashes
									.get(id).getHiqUserName());
							publishProgress(contactData);
						}
					});
		}
		return 0;
	}

	private int getFriendsFromJSON(JSONArray json) {
		userHashes.clear();
		if (json.length() > 0) {
			try {
				for (int i = 0; i < json.length(); i++) {
					JSONObject jsonFriend = json.getJSONObject(i);
					String phoneHash = getStringFromJSON(jsonFriend, "phone_hash");
					String facebookHash = getStringFromJSON(jsonFriend, "facebook_hash");
					String hiqUserHash = getStringFromJSON(jsonFriend, "user_id");
					String hiqUserName = getStringFromJSON(jsonFriend, "username");
					userHashes.add(new ContactHashes(phoneHash, facebookHash, hiqUserHash, hiqUserName));
				}
				return 0;
			} catch (JSONException e) {
				userHashes.clear();
				return 1;
			}
		}
		return 1;
	}

	private String getStringFromJSON(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException j) {
			return "";
		}
	}
}
