package com.dcg.meneame;

import java.net.URI;
import java.util.concurrent.Semaphore;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import android.app.Activity;
import android.util.Log;

public class RssWorkerThread extends Thread {
	
	/** Global Application */
	private ApplicationMNM mApp = null;
	
	/** log tag for this class */
	private static final String TAG = "ApplicationMNM";
	
	/** if true we requested a stop, so do not handle any request stuff */
	private boolean mbStopRequested = false;
	
	/** Semaphore used when the thread starts working*/
	private Semaphore mSemaphore = null;
	
	/** Feed URL */
	private String mFeedURL = "";
	
	/**
	 * 
	 * @param Activity ParentActivity, holds the semaphore to make this thread save
	 */
	public RssWorkerThread( ApplicationMNM globalApp, String feedURL, Semaphore threadSemaphore ) {
		super();
		
		mFeedURL = feedURL;
		mSemaphore = threadSemaphore;
		mApp = globalApp;
	}
	
	/**
	 * Request this thread to stop what it's doing
	 */
	public void requestStop() {
		mbStopRequested = true;
	}
	
	@Override
	public void run() {
		mbStopRequested = false;
		if ( mApp != null ) {
			try {
				try {
					mSemaphore.acquire();
				} catch (InterruptedException e) {
						return;
				}
				guardedRun();
			} catch (InterruptedException e) {
				// fall thru and exit normally
			} finally {
				mSemaphore.release();
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
			request.setURI(new URI("http://feeds.feedburner.com/MeneamePublicadas?format=xml"));
			HttpResponse response = client.execute(request);
			
			if ( !mbStopRequested )
			{			
				String page=EntityUtils.toString(response.getEntity());
				System.out.println(page.toString());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
