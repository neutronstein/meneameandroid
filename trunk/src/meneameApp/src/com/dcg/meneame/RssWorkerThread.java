package com.dcg.meneame;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.util.Log;

public class RssWorkerThread extends Thread {
	
	/** activity used to access global application data */
	private ApplicationMNM mApp;
	
	/** log tag for this class */
	private static final String TAG = "ApplicationMNM";
	
	/**
	 * 
	 * @param Activity ParentActivity, holds the semaphore to make this thread save
	 */
	public RssWorkerThread( Activity PrentActivity ) {
		super();
		
		// Cache app
		try {
			mApp = (ApplicationMNM)PrentActivity.getApplication();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		if ( mApp != null ) {
			try {
				try {
					mApp.acquireRssSemaphore();
				} catch (InterruptedException e) {
						return;
				}
				guardedRun();
			} catch (InterruptedException e) {
				// fall thru and exit normally
			} finally {
				mApp.releaseRssSemaphore();
			}
		}
		else {
			Log.w(TAG, "No application object found!");
		}
	}
	
	private void guardedRun() throws InterruptedException {
		// At this point we are thread safe
		try {
			HttpClient client = mApp.getHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI("http://www.google.com/"));
			HttpResponse response = client.execute(request);
			String page=EntityUtils.toString(response.getEntity());
			System.out.println(page);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
