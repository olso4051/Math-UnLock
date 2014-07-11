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

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.ContactHelper;

public class GetContacts extends AsyncTask<String, CustomContactData, Integer> {
	private List<String> userPhoneHashes = new ArrayList<String>();
	private List<String> userIDHashes = new ArrayList<String>();
	private List<String> allNames = new ArrayList<String>();
	private List<String> allPhoneNumbers = new ArrayList<String>();
	private List<String> allEmails = new ArrayList<String>();
	private List<CustomContactData> allContacts = new ArrayList<CustomContactData>();
	private String baseURL;
	private Context ctx;

	public GetContacts(Context ctx, List<CustomContactData> contacts) {
		this.ctx = ctx;
		allContacts.clear();
		allContacts.addAll(contacts);
		allNames.clear();
		allNames.addAll(ContactHelper.getNamesLowercaseFromContacts(allContacts));
		allPhoneNumbers.clear();
		allPhoneNumbers.addAll(ContactHelper.getPhoneNumbersFromContacts(allContacts));
		allEmails.clear();
		allEmails.addAll(ContactHelper.getEmailsFromContacts(allContacts));
		baseURL = ctx.getString(R.string.service_base_url);
	}

	public List<String> getUserIDHashes() {
		return userIDHashes;
	}

	public List<String> getUserPhoneHashes() {
		return userPhoneHashes;
	}

	@Override
	protected Integer doInBackground(String... s) {
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
							name = ((JSONObject) data.get(i)).getString("name");
							String nameLo = name.toLowerCase(Locale.ENGLISH);
							if (allNames.contains(nameLo)) {
								replaceID = allNames.indexOf(nameLo);
							}
							String facebookID = ((JSONObject) data.get(i)).getString("id");
							CustomContactData contact = new CustomContactData(name, facebookID);
							if (replaceID == -1) {
								allContacts.add(contact);
								Collections.sort(allContacts);
								allNames.clear();
								allNames.addAll(ContactHelper.getNamesLowercaseFromContacts(allContacts));
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
			Toast.makeText(ctx, "Login with Facebook", Toast.LENGTH_LONG).show();
		}
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
							if (number.length() >= 7 && !allPhoneNumbers.contains(number)) {
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

		// PUT to API with user_id
		/*DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		HttpPost httppost = new HttpPost(baseURL + "friend");
		HttpEntity entity;
		String fullResult;
		JSONArray jsonResponse;
		try {
			JSONArray data = new JSONArray();
			for (String encryptedPhoneNumber : allEncryptedPhoneNumbers) {
				data.put(encryptedPhoneNumber);
			}
			httppost.setEntity(new StringEntity(data.toString()));
			httppost.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httppost);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			jsonResponse = new JSONArray(fullResult);
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			return getFriendsFromJSON(jsonResponse);
		} else {
			return 1;
		}*/

		// for testing we'll assume we got 5 random contacts back from the service
		for (int i = 2; i < 10; i++) {
			userPhoneHashes.add(allEncryptedPhoneNumbers.get(i));
			userIDHashes.add("test" + i);
		}
		Collections.sort(allContacts);
		ContactHelper.findContact(ContactHelper.FindType.PHONEHASH, allContacts, userPhoneHashes, new ContactHelper.friendDataListener() {
			@Override
			public void onFriendContactFound(int contact, int id) {
				CustomContactData contactData = new CustomContactData(contact, userIDHashes.get(id));
				publishProgress(contactData);
			}
		});
		return 0;
	}

	private int getFriendsFromJSON(JSONArray json) {
		userPhoneHashes.clear();
		userIDHashes.clear();
		if (json.length() > 0) {
			try {
				for (int i = 0; i < json.length(); i++) {
					JSONArray jsonFriend = json.getJSONArray(i);
					userPhoneHashes.add(jsonFriend.getString(0));
					userIDHashes.add(jsonFriend.getString(1));
				}
				return 0;
			} catch (JSONException e) {
				userPhoneHashes.clear();
				userIDHashes.clear();
				return 1;
			}
		}
		return 1;
	}
}
