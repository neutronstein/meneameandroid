package com.dcg.auth;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.dcg.app.ApplicationMNM;
import com.dcg.util.HttpManager;

public class ClientFormLogin {
	private static final String TAG = "ClientFormLogin";
	
	static {
		ApplicationMNM.addLogCat(TAG);
	}
	
	/**
	 * Method used to send a login request to a normal form based login system
	 * @param user Username
	 * @param pass Users password
	 * @param loginURL Should be the URL which will handle authentication
	 * @param realmURL Should be the URL used to initialize client cookies
	 * @throws Exception
	 */
	public static void login(String user, String userToken, String pass, String passToken, String loginURL, String realmURL ) throws Exception {	
		HttpGet httpGet = new HttpGet(realmURL);
		HttpResponse response = HttpManager.execute(httpGet);
		HttpEntity entity = response.getEntity();
		
		ApplicationMNM.logCat(TAG,"Login form get: " + response.getStatusLine());
		if (entity != null) {
			entity.consumeContent();
		}
		ApplicationMNM.logCat(TAG,"Initial set of cookies:");
		List<Cookie> cookies = HttpManager.getCookieStore().getCookies();
		if (cookies.isEmpty()) {
			ApplicationMNM.logCat(TAG,"None");
		} else {
			for (int i = 0; i < cookies.size(); i++) {
				ApplicationMNM.logCat(TAG,"- " + cookies.get(i).toString());
			}
		}
		
		HttpPost httPost = new HttpPost(realmURL);
		
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair(userToken, user));
		nvps.add(new BasicNameValuePair(passToken, pass));
		
		httPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		response = HttpManager.execute(httPost);
		entity = response.getEntity();
		
		ApplicationMNM.logCat(TAG,"Login form get: " + response.getStatusLine());
		if (entity != null) {
			entity.consumeContent();
		}
		
		ApplicationMNM.logCat(TAG,"Post logon cookies:");
		cookies = HttpManager.getCookieStore().getCookies();
		if (cookies.isEmpty()) {
			ApplicationMNM.logCat(TAG,"None");
		} else {
			for (int i = 0; i < cookies.size(); i++) {
				ApplicationMNM.logCat(TAG,"- " + cookies.get(i).toString());
			}
		}       
	}
}
