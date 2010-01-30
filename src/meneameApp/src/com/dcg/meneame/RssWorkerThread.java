package com.dcg.meneame;

import java.net.URI;
import java.util.concurrent.Semaphore;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class RssWorkerThread extends Thread {
	
	/** Definitions of a completed message */
	public static String COMPLETED_KEY = "completed";
	public static int COMPLETED_OK = 0;
	public static int COMPLETED_FAILED = -1;
	
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
	
	/** Handler used to send messages to the activity that will handle our work */
	private Handler mHandler;
	
	/**
	 * 
	 * @param Activity ParentActivity, holds the semaphore to make this thread save
	 */
	public RssWorkerThread( ApplicationMNM globalApp, Handler handler, String feedURL, Semaphore threadSemaphore ) {
		super();
		
		mFeedURL = feedURL;
		mSemaphore = threadSemaphore;
		mApp = globalApp;
		mHandler = handler;
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
			
			// We stopped!
			if ( mbStopRequested ) return;
			
			// Build message body
			Message msg = mHandler.obtainMessage();
			Bundle data = new Bundle();
			
			if ( response != null )
			{
				Log.d(TAG, "Finished!");				
				data.putInt(COMPLETED_KEY, COMPLETED_OK);
			}
			else
			{
				Log.d(TAG, "Failed!");
				data.putInt(COMPLETED_KEY, COMPLETED_FAILED);				
			}
			
			// Send final message
			msg.setData(data);
			mHandler.sendMessage(msg);
			
//			if ( !mbStopRequested )
//			{			
//				String page=EntityUtils.toString(response.getEntity());
//				System.out.println(page.toString());
//			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			// Build and send failed message
			Message msg = mHandler.obtainMessage();
			Bundle data = new Bundle();
			data.putInt(COMPLETED_KEY, COMPLETED_FAILED);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	}
}
