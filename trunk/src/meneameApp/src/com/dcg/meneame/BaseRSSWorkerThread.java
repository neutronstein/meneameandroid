package com.dcg.meneame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.Semaphore;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

abstract public class BaseRSSWorkerThread extends Thread {
	
	/** Definitions of a completed message */
	public static String COMPLETED_KEY = "completed";
	public static int COMPLETED_OK = 0;
	public static int COMPLETED_FAILED = -1;
	
	/** Key which defines any state when caching a feed */
	public static String ERROR_KEY = "error";
	public static int ERROR_SUCCESSFULL = 0;
	public static int ERROR_FAILED = -1;
	public static String ERROR_MESSAGE_KEY = "error_msg";
	
	/** Global Application */
	private ApplicationMNM mApp = null;
	
	/** log tag for this class */
	private static final String TAG = "BaseRSSWorkerThread";
	
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
	
	/** class that handles the caching process */
	protected String mFeedCacherClassName = "";
	
	/** class we use to parse the feed */
	protected String mFeedParserClassName = "com.dcg.meneame.RSSParser";
	
	/** Default handler we use to parse the current feed */
	private RSSParser mFeedParser = null;
	
	protected Bundle mDdata = new Bundle();
	
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
			mDdata = new Bundle();
			
			if ( response != null )
			{
//				in = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));
//				
//				StringBuffer sb = new StringBuffer("");
//				String line = "";
//				String NL = System.getProperty("line.separator");
//				
//				while ((line = in.readLine()) != null) {
//					sb.append(line + NL);
//				}
//				
//				in.close();
//				String page = sb.toString();
//				
//				// Start parsing and caching the feed
//				processResult(page);
				
				// Start processing the RSS file
				processResult( new InputStreamReader(response.getEntity().getContent()) );
				
				
				// look for any error
				if ( isDataValid() )
				{
					// All fine
					Log.d(TAG, "Finished!");				
					mDdata.putInt(COMPLETED_KEY, COMPLETED_OK);
				}
				else
				{
					mDdata.putInt(COMPLETED_KEY, COMPLETED_FAILED);
				}
				
				//System.out.println(page);				
			}
			else
			{
				Log.d(TAG, "Failed!");
				mDdata.putInt(COMPLETED_KEY, COMPLETED_FAILED);	
			}
			
			// Send final message
			msg.setData(mDdata);
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
	
	/**
	 * Checks if the Bundle has any error set or not
	 * @return 
	 */
	protected boolean isDataValid() {
		return mDdata.getInt(ERROR_KEY) == ERROR_SUCCESSFULL;
	}
	
	/**
	 * Returns the current error message registered with the Bundle
	 * @return
	 */
	protected String getErrorMessage() {
		return mDdata.getString(ERROR_MESSAGE_KEY);
	}
	
	/**
	 * Set a new error message into our current data Bundle
	 * @param error
	 */
	protected void setErrorMessage( String error ) {
		mDdata.putInt(ERROR_KEY, ERROR_FAILED);
		mDdata.putString(ERROR_MESSAGE_KEY, error);
	}
	
	// TODO: Make all SDCard access globally and configurable using always:
	//  - Environment.getExternalStorageDirectory()
	//  - File.separator
	
//	/**
//	 * Prepares the SDCard with all we need
//	 */
//	protected void prepareSDCard( Bundle data ) {
//		// Create app dir in SDCard if possible
//		File path = new File("/sdcard/com.dcg.meneame/cache/");
//		if(! path.isDirectory()) {
//			if ( path.mkdirs() )
//			{
//				Log.d(TAG,"Directory created: /sdcard/com.dcg.meneame/cache/");
//			}
//			else
//			{
//				Log.w(TAG,"Failed to create directory: /sdcard/com.dcg.meneame/cache/");
//			}
//		}
//	}
	
	private void createRSSHandler() {
		// Try to create the RSS handler
		try {
			mFeedParser = (RSSParser)Class.forName(this.mFeedParserClassName).newInstance();
			Log.d(TAG, "RSS-Parser created: " + mFeedParser.toString());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrorMessage(e.toString());
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrorMessage(e.toString());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrorMessage(e.toString());
		}
	}
	
	/**
	 * Will parse the incoming data and save it into a Bundle
	 * @param page
	 * @return 
	 * @return
	 */
	private void processResult( InputStreamReader inputStreamReader )
	{
		// Create rss handler
		createRSSHandler();
		
		// Check if we got a valid parser
		if ( mFeedParser == null )
		{
			// Set error message and return
			setErrorMessage("Could not create parser of class " + mFeedParserClassName);
			return;
		}
		
		// Setup parser and parse!
		mFeedParser.setInputStream( inputStreamReader );
		mFeedParser.setWorkerThread(this);
		// TODO: Get config value for max items!
		mFeedParser.setMaxItems(10);
		mFeedParser.parse();
		
		Log.d(TAG,"Feed: " + mFeedParser.toString());
	}
}
