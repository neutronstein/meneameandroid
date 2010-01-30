package com.dcg.meneame;

import java.util.concurrent.Semaphore;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

abstract public class FeedActivity extends ListActivity {
	
	/** Global Application */
	protected ApplicationMNM mApp = null;
	
	/** Feed URL */
	protected String mFeedURL = "";
	
	/** Semaphore used by the activities feed worker thread */
	private Semaphore mSemaphore = new Semaphore(1);
	
	/** Worker thread which will do the async operations */
	private RssWorkerThread mRssThread = null;
	
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
		Toast toast = null;
		if ( data.getInt( RssWorkerThread.COMPLETED_KEY) == RssWorkerThread.COMPLETED_OK )
		{
			toast = Toast.makeText(getBaseContext(), "Finished", Toast.LENGTH_SHORT);
		}
		else
		{
			toast = Toast.makeText(getBaseContext(), "Failed!", Toast.LENGTH_SHORT);
		}
		
		// Show
		toast.show();
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
		Toast toast = null;
		
		
		// Start thread if not started or not alive
		if ( mRssThread == null || !mRssThread.isAlive() )
		{
			toast = Toast.makeText(getBaseContext(), "Refreshing: " + getFeedURL(), Toast.LENGTH_SHORT);
			mRssThread = new RssWorkerThread(mApp, mHandler, getFeedURL(), mSemaphore );
			mRssThread.start();
		}
		else
		{
			toast = Toast.makeText(getBaseContext(), "Already refreshing... please wait...", Toast.LENGTH_SHORT);
		}
		toast.show();
	}
}
