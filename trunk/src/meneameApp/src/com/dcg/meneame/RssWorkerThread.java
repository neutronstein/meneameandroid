package com.dcg.meneame;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class RssWorkerThread extends Thread {
	
	/** Global Application */
	private ApplicationMNM mApp = null;
	
	/** log tag for this class */
	private static final String TAG = "ApplicationMNM";
	
	/** if true we requested a stop, so do not handle any request stuff */
	private boolean mbStopRequested = false;
	
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
