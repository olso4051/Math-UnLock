package com.olyware.mathlock.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.model.GraphUser;
import com.olyware.mathlock.R;
import com.olyware.mathlock.utils.ContactHelper;

public class GetContacts extends AsyncTask<String, CustomContactData, Integer> {
	private List<String> userPhoneHashes = new ArrayList<String>();
	private List<String> userIDHashes = new ArrayList<String>();
	private String baseURL;
	private Context ctx;

	public GetContacts(Context ctx) {
		this.ctx = ctx;
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
		List<CustomContactData> allContacts = new ArrayList<CustomContactData>();
		List<String> phoneNumbers = new ArrayList<String>();
		List<String> allPhoneNumbers = new ArrayList<String>();
		List<String> allEncryptedPhoneNumbers = new ArrayList<String>();
		List<String> emails = new ArrayList<String>();
		List<String> allEmails = new ArrayList<String>();
		boolean isPerson = false;
		String name, id;
		List<String> allNames = new ArrayList<String>();
		int replaceID = -1;
		final Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			if (session.getPermissions().contains("user_friends")) {
				// Get the user's list of friends
				Request friendsRequest = new Request(session, "/me/friends");
				Response response = friendsRequest.executeAndWait();
				GraphObjectList<GraphObject> list = response.getGraphObjectList();
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						GraphUser friend = (GraphUser) list.get(i);
						if (friend != null) {
							name = friend.getName();
							String nameLo = name.toLowerCase(Locale.ENGLISH);
							friend.getId();
							allNames.add(nameLo);
							allContacts.add(new CustomContactData(name, friend.getId()));
							publishProgress(new CustomContactData(name, friend.getId()));
						}
					}
				}
			}
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
					if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
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
						if (isPerson) {
							if (replaceID == -1) {
								allNames.add(nameLo);
								allContacts.add(new CustomContactData(name, emails, phoneNumbers));
							}
							emails.add(String.valueOf(replaceID));
							publishProgress(new CustomContactData(name, emails, phoneNumbers));
						}
						replaceID = -1;
						isPerson = false;
						emails.clear();
						phoneNumbers.clear();
					}
				}
			}
		}
		allEncryptedPhoneNumbers.addAll(ContactHelper.getPhoneHashes(allPhoneNumbers));
		cur.close();

		// PUT to API with user_id
		/*DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		HttpPut httpput = new HttpPut(baseURL + "friend");
		HttpEntity entity;
		String fullResult;
		JSONArray jsonResponse;
		try {
			JSONArray data = new JSONArray();
			for (String encryptedPhoneNumber : allEncryptedPhoneNumbers) {
				data.put(encryptedPhoneNumber);
			}
			httpput.setEntity(new StringEntity(data.toString()));
			httpput.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httpput);
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
		for (int i = 0; i < 5; i++) {
			userPhoneHashes.add(allEncryptedPhoneNumbers.get(i));
			userIDHashes.add("test" + i);
		}
		Collections.sort(allContacts);
		Log.d("test", "findContact");
		ContactHelper.findContact(ContactHelper.FindType.PHONEHASH, allContacts, userPhoneHashes, new ContactHelper.friendDataListener() {
			@Override
			public void onFriendContactFound(int contact, int id) {
				Log.d("test", "new friend contact found contact = " + contact + " id = " + id);
				publishProgress(new CustomContactData(contact, userIDHashes.get(id)));
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
				e.printStackTrace();
				return 1;
			}
		}
		return 1;
	}
}
