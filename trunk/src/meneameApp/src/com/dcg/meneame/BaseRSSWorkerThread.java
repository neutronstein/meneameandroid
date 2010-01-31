package com.dcg.meneame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.Semaphore;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

abstract public class BaseRSSWorkerThread extends Thread {
	
	/** Definitions of a completed message */
	public static String COMPLETED_KEY = "completed";
	public static int COMPLETED_OK = 0;
	public static int COMPLETED_FAILED = -1;
	
	/** Global Application */
	private ApplicationMNM mApp = null;
	
	/** log tag for this class */
	private static final String TAG = "RssWorkerThread";
	
	/** if true we requested a stop, so do not handle any request stuff */
	private boolean mbStopRequested = false;
	
	/** Semaphore used when the thread starts working*/
	private Semaphore mSemaphore = null;
	
	/** Feed URL */
	private String mFeedURL = "";
	
	/** Handler used to send messages to the activity that will handle our work */
	private Handler mHandler;
	
	/** Read buffer used to catch our content */
	private BufferedReader in = null;
	
	/**
	 * 
	 * @param Activity ParentActivity, holds the semaphore to make this thread save
	 */
	public BaseRSSWorkerThread() {
		super();
		
		
	}
	
	/**
	 * Setup all data this class needs, we can not use it's constructor because we invoke this class!
	 * @param globalApp
	 * @param handler
	 * @param feedURL
	 * @param threadSemaphore
	 */
	public void setupWorker( ApplicationMNM globalApp, Handler handler, String feedURL, Semaphore threadSemaphore ) {
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
					Log.d(TAG, "Aquirering semaphore " + mSemaphore.toString());
					mSemaphore.acquire();
				} catch (InterruptedException e) {
						return;
				}
				guardedRun();
			} catch (InterruptedException e) {
				// fall thru and exit normally
			} finally {
				Log.d(TAG, "Releasing semaphore " + mSemaphore.toString());
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
			
			request.setURI(new URI(mFeedURL));
			Log.d(TAG, "Starting request " + request.toString());
			
			HttpResponse response = client.execute(request);
			
			// We have stopped!
			if ( mbStopRequested ) return;
			
			// Build message body
			Message msg = mHandler.obtainMessage();
			Bundle data = new Bundle();
			
			if ( response != null )
			{
				in = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));
				
				StringBuffer sb = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");
				
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				
				in.close();
				String page = sb.toString();
				
				// Start parsing the feed and 
				data.putAll( parseResult(page) );
				
				//System.out.println(page);
				
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
		} catch (Exception e) {
			e.printStackTrace();
			
			// Build and send failed message
			Message msg = mHandler.obtainMessage();
			Bundle data = new Bundle();
			data.putInt(COMPLETED_KEY, COMPLETED_FAILED);
			msg.setData(data);
			mHandler.sendMessage(msg);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();					
				}
			}
		}
	}
	
	// TODO: Make all SDCard access globally and configurable using always:
	//  - Environment.getExternalStorageDirectory()
	//  - File.separator
	
	/**
	 * Prepares the SDCard with all we need
	 */
	private void prepareSDCard() {
		// Create app dir in SDCard if possible
		File path = new File("/sdcard/com.dcg.meneame/cache/");
		if(! path.isDirectory()) {
			if ( path.mkdirs() )
			{
				Log.d(TAG,"Directory created: /sdcard/com.dcg.meneame/cache/");
			}
			else
			{
				Log.w(TAG,"Failed to create directory: /sdcard/com.dcg.meneame/cache/");
			}
		}
	}
	
	/**
	 * Will parse the incomming data and save it into a Bundle
	 * @param page
	 * @return
	 */
	protected Bundle parseResult( String page )
	{
		Bundle data = new Bundle();
		
		// By default this is just empty
		try {
			// Prepare process
			prepareSDCard();
			
		    File root = Environment.getExternalStorageDirectory();
		    if (root.canWrite()){
		        File gpxfile = new File("/sdcard/com.dcg.meneame/cache/feed.rss");
		        FileWriter gpxwriter = new FileWriter(gpxfile);
		        BufferedWriter out = new BufferedWriter(gpxwriter);
		        out.write(page);
		        out.close();
		        Log.d(TAG,"Feed written to: /sdcard/com.dcg.meneame/cache/feed.rss");
		    }
		    else
		    {
		    	Log.w(TAG, "Could not write file, SD Card not writeable.");
		    }
		} catch (IOException e) {
		    Log.w(TAG, "Could not write file " + e.getMessage());
		}
		
		return data;
	}
}
