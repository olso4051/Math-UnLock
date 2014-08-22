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
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.impl.client.HttpClientBuilder;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.olyware.mathlock.R;
import com.olyware.mathlock.adapter.ContactHashes;
import com.olyware.mathlock.utils.ContactHelper;
import com.olyware.mathlock.utils.JSONHelper;
import com.olyware.mathlock.utils.Loggy;
import com.olyware.mathlock.utils.Toaster;

public class RefreshContacts extends AsyncTask<Void, CustomContactData, Integer> {
	final private static String FriendsEndpoint = "friends";
	private List<ContactHashes> userHashes = new ArrayList<ContactHashes>();
	private List<String> allNames = new ArrayList<String>();
	private List<String> allPhoneNumbers = new ArrayList<String>();
	private List<String> allEmails = new ArrayList<String>();
	private List<String> allFacebookHashes = new ArrayList<String>();
	private List<CustomContactData> allContacts = new ArrayList<CustomContactData>();
	private String notNumber;
	private boolean phonebookRefresh;
	private String baseURL;
	private Context ctx;
	private HttpPost httppost;

	public RefreshContacts(Context ctx, List<CustomContactData> contacts, boolean phonebookRefresh) {
		this.ctx = ctx;
		allContacts.clear();
		allContacts.addAll(contacts);
		this.notNumber = ContactHelper.getUserPhoneNumber(ctx);
		this.phonebookRefresh = phonebookRefresh;
		baseURL = ctx.getString(R.string.service_base_url);
	}

	public List<ContactHashes> getUserHashes() {
		if (userHashes != null)
			return userHashes;
		else
			return new ArrayList<ContactHashes>();
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
				try {
					Request friendsRequest = new Request(session, "/me/friends");
					Response response = friendsRequest.executeAndWait();
					String rawResponse = "";
					if (response != null)
						rawResponse = response.getRawResponse();
					if (rawResponse == null)
						rawResponse = "";
					JSONObject responseJSON = new JSONObject(rawResponse);
					JSONArray data = responseJSON.getJSONArray("data");
					if (data.length() > 0) {
						for (int i = 0; i < data.length(); i++) {
							if (isCancelled())
								break;
							String facebookName = ((JSONObject) data.get(i)).getString("name");
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
					// Do nothing
				} catch (FacebookException e) {
					// Do nothing
				} catch (IllegalArgumentException e) {
					// Do nothing
				}
			} else {
				Toaster.toastAllowFacebookFriends(ctx, true);
			}
		} else {
			Toaster.toastLoginWithFacebook(ctx, true);
		}
		allNames.clear();
		allNames.addAll(ContactHelper.getNamesLowercaseFromContacts(allContacts));
		allPhoneNumbers.clear();
		allPhoneNumbers.addAll(ContactHelper.getFirstPhoneNumbersFromContacts(allContacts));
		allEmails.clear();
		allEmails.addAll(ContactHelper.getFirstEmailsFromContacts(allContacts));
		// Get contacts from user's contacts
		if (phonebookRefresh) {
			ContentResolver cr = ctx.getContentResolver();
			Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			if (cur.getCount() > 0) {
				// next stored contact
				while (cur.moveToNext()) {
					if (isCancelled())
						break;
					boolean quit = false;
					id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));				// store ID for getting the email and phone
					// number
					name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));	// name of the contact
					if (name != null && id != null) {
						String nameLo = name.toLowerCase(Locale.ENGLISH);								// lower case name for comparing other contacts
						if (name.charAt(0) != '#') {													// don't add contacts that start with #
							// add info to names that we've already found
							if (allNames.contains(nameLo)) {
								replaceID = allNames.indexOf(nameLo);
							}
							// only add a contact if they have a phone number
							String hasNumber = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
							if (hasNumber != null) {
								if (Integer.parseInt(hasNumber) > 0 || replaceID >= 0) {
									Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
											ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
									while (pCur.moveToNext() && !quit) {
										if (isCancelled())
											break;
										// could use the next line to restrict types of numbers
										// int phoneType = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
										String fullNumber = pCur.getString(pCur
												.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
										if (fullNumber != null) {
											String number = ContactHelper.getPhoneNumberFromString(fullNumber);
											// only add phone numbers we haven't found yet
											if (number.equals(notNumber))
												quit = true;
											if (!number.equals(notNumber) && number.length() >= 7 && !allPhoneNumbers.contains(number)) {
												isPerson = true;
												phoneNumbers.add(number);
												allPhoneNumbers.add(number);
											}
										}
									}
									pCur.close();
									Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
											ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { id }, null);
									while (emailCur.moveToNext() && !quit) {
										if (isCancelled())
											break;
										String email = emailCur.getString(emailCur
												.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
										// only add emails we haven't found yet
										if (email != null) {
											if (email.length() >= 5 && !allEmails.contains(email)) {
												isPerson = true;
												emails.add(email);
												allEmails.add(email);
											}
										}
									}
									emailCur.close();
									if (!quit && (isPerson || replaceID >= 0)) {
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
						}
					}
					replaceID = -1;
					isPerson = false;
					emails.clear();
					phoneNumbers.clear();
				}
			}
			cur.close();
		}
		allEncryptedPhoneNumbers.addAll(ContactHelper.getPhoneHashes(allPhoneNumbers));

		if (!isCancelled()) {
			// POST to API to get user_ids of contacts and facebook friends
			HttpClient httpClient = HttpClientBuilder.create().build();
			httppost = new HttpPost(baseURL + FriendsEndpoint);
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
				// facebookHashes.put("13959212");
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
				httppost.setEntity(new StringEntity(data.toString(), "UTF-8"));
				httppost.setHeader("Content-Type", "application/json");
				HttpResponse response = httpClient.execute(httppost);
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

			Collections.sort(allContacts);
			userHashes = ContactHelper.getUniqueContactHashes(userHashes);
			if (userHashes.size() > 0) {
				ContactHelper.findContact(ContactHelper.FindType.PhoneAndFacebookHASH, allContacts, userHashes,
						new ContactHelper.friendDataListener() {
							@Override
							public void onFriendContactFound(int contact, int id) {
								if (!isCancelled()) {
									CustomContactData contactData = new CustomContactData(contact, userHashes.get(id).getHiqUserHash(),
											userHashes.get(id).getHiqUserName());
									publishProgress(contactData);
								}
							}
						});
			}
		}
		return 0;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		if (httppost != null)
			httppost.abort();
	}

	private int getFriendsFromJSON(JSONArray json) {
		userHashes.clear();
		if (json.length() > 0) {
			try {
				for (int i = 0; i < json.length(); i++) {
					JSONObject jsonFriend = json.getJSONObject(i);
					String phoneHash = JSONHelper.getStringFromJSON(jsonFriend, "phone_hash");
					String facebookHash = JSONHelper.getStringFromJSON(jsonFriend, "facebook_hash");
					String hiqUserHash = JSONHelper.getStringFromJSON(jsonFriend, "user_id");
					String hiqUserName = JSONHelper.getStringFromJSON(jsonFriend, "username");
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
}
