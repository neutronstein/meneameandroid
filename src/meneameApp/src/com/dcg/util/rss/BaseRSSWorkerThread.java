package com.dcg.util.rss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.Semaphore;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.dcg.app.ApplicationMNM;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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
	private String mFeedParserClassName = "";
	
	/** Default handler we use to parse the current feed */
	private RSSParser mFeedParser = null;
	
	protected Bundle mDdata = new Bundle();
	
	/**
	 * 
	 * @param Activity ParentActivity, holds the semaphore to make this thread save
	 */
	public BaseRSSWorkerThread() {
		super();
		
		ApplicationMNM.AddLogCat(TAG);
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
	
	public String getmFeedParserClassName() {
		return mFeedParserClassName;
	}

	public void setmFeedParserClassName(String mFeedParserClassName) {
		this.mFeedParserClassName = mFeedParserClassName;
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
					ApplicationMNM.LogCat(TAG, "Aquirering semaphore " + mSemaphore.toString());
					mSemaphore.acquire();
				} catch (InterruptedException e) {
						return;
				}
				guardedRun();
			} catch (InterruptedException e) {
				// fall thru and exit normally
			} finally {
				ApplicationMNM.LogCat(TAG, "Releasing semaphore " + mSemaphore.toString());
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
			ApplicationMNM.LogCat(TAG, "Starting request " + request.toString());
			
			HttpResponse response = client.execute(request);
			
			// We have stopped!
			if ( mbStopRequested ) return;
			
			// Build message body
			Message msg = mHandler.obtainMessage();
			mDdata = new Bundle();
			
			if ( response != null )
			{				
				// Start processing the RSS file
				processResult( msg, new InputStreamReader(response.getEntity().getContent()) );				
				
				// look for any error
				if ( isDataValid() )
				{
					// All fine
					ApplicationMNM.LogCat(TAG, "Finished!");				
					mDdata.putInt(COMPLETED_KEY, COMPLETED_OK);
				}
				else
				{
					mDdata.putInt(COMPLETED_KEY, COMPLETED_FAILED);
				}		
			}
			else
			{
				Log.d(TAG, "Failed!");
				mDdata.putInt(COMPLETED_KEY, COMPLETED_FAILED);	
			}
			
			// Send final message
			msg.setData(mDdata);
			sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
			
			// Build and send failed message
			Message msg = mHandler.obtainMessage();
			Bundle data = new Bundle();
			data.putInt(COMPLETED_KEY, COMPLETED_FAILED);
			msg.setData(data);
			sendMessage(msg);
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
	 * Send a message to our handler
	 * @param msg
	 */
	private void sendMessage( Message msg ) {
		mHandler.sendMessage(msg);
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
	
	private void createRSSHandler() {
		// Try to create the RSS handler
		try {
			mFeedParser = (RSSParser)Class.forName(this.mFeedParserClassName).newInstance();
			ApplicationMNM.LogCat(TAG, "RSS-Parser created: " + mFeedParser.toString());
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
	 * @param msg 
	 * @param page
	 * @return 
	 * @return
	 */
	private void processResult( Message msg, InputStreamReader inputStreamReader )
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
		
		// Get the max number of items to be shown from our preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApp.getBaseContext());        
        int maxItems = -1;        
        try
        {
        	maxItems = Integer.parseInt(prefs.getString("pref_app_maxarticles", "-1"));
        }
        catch( Exception e)
        {
        	// Nothing to do here :P
        }                
		mFeedParser.setMaxItems(maxItems);
		mFeedParser.parse();
		
		// We finished to inform subclasses
		feedParsingFinished(mFeedParser.getFeed());
		
		msg.obj = mFeedParser.getFeed();
		
		ApplicationMNM.LogCat(TAG,"Feed: " + mFeedParser.toString());
	}
	
	/**
	 * Will be called once the feed has been parsed
	 * @param parsedFeed
	 */
	protected void feedParsingFinished( Feed parsedFeed ) {
		// The feed we just resolved
	}
}
