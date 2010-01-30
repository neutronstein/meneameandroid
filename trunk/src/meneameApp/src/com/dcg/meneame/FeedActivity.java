package com.dcg.meneame;

import java.util.concurrent.Semaphore;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

abstract public class FeedActivity extends ListActivity {
	
	/** Log tag */
	private static final String TAG = "FeedActivity";
	
	/** Our RssWorkerThread class so subclasses will be able to call another one */
	protected static final String mRssWorkerThreadClass = "com.dcg.meneame.RssWorkerThread";

	/** Global Application */
	protected ApplicationMNM mApp = null;
	
	/** Feed URL */
	protected String mFeedURL = "";
	
	/** Semaphore used by the activities feed worker thread */
	private Semaphore mSemaphore = new Semaphore(1);
	
	/** Worker thread which will do the async operations */
	private BaseRssWorkerThread mRssThread = null;
	
	/** Handler used to communicate with our worker thread*/
	protected Handler mHandler = null;
	
	public FeedActivity() {
		super();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 // Cache app
		try {
			mApp = (ApplicationMNM)getApplication();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				handleThreadMessage( msg );
			}
		};
	}
	
	protected void handleThreadMessage(Message msg) {
		Bundle data = msg.getData();
		
		// Check if it completed ok or not
		if ( data.getInt( BaseRssWorkerThread.COMPLETED_KEY) == BaseRssWorkerThread.COMPLETED_OK )
		{
			ShowToast("Completed");
			Log.d(TAG,"Worker thread posted a completed message: OK");
		}
		else
		{
			ShowToast("Failed!");
			Log.d(TAG,"Worker thread posted a completed message: FAILED");
		}
	}
	
	/**
	 * Shows a toast message, will hide any already shown message
	 * @param msg
	 */
	protected void ShowToast( String msg ) {
		if ( mApp != null ) mApp.ShowToast(msg);
	}
	
	/**
	 * Returns the URL this feed points too
	 * @return String - FeedURL
	 */
	public String getFeedURL() {
		return mFeedURL;
	}
	
	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * @return String - TabTag
	 */
	public static String getTabActivityTag() {
		return "";
	}
	
	/**
	 * String id used for the tab indicator
	 * @return
	 */
	public static int getIndicatorStringID() {
		return -1;
	}
	
	/**
	 * Will refresh the current feed
	 */
	public void RefreshFeed() {		
		// Start thread if not started or not alive
		if ( mRssThread == null || !mRssThread.isAlive() )
		{
			String Error = "";
			try {
				Log.d(TAG, "Staring worker thread");
				ShowToast("Refreshing: " + getFeedURL());
				mRssThread = (BaseRssWorkerThread) Class.forName( mRssWorkerThreadClass ).newInstance();
				mRssThread.setupWorker( mApp, mHandler, getFeedURL(), mSemaphore );
				mRssThread.start();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Error = e.toString();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Error = e.toString();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Error = e.toString();
			}
			if ( Error.length() > 0 ) ShowToast("Ups... failed to refresh: " + Error);
		}
		else
		{
			Log.d(TAG, "Worker thread already alive");
			ShowToast("Already refreshing... please wait...");
		}
	}
}
