package com.dcg.meneame;

import java.util.concurrent.Semaphore;

import com.dcg.app.ApplicationMNM;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Thread which is responsible for the feed caching process
 * in our database. Right now it only handles the recovering
 * process which takes much more time than saving.
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class ArticleDBCacheThread extends Thread {
	/** log tag for this class */
	private static final String TAG = "ArticleDBCacheThread";
	
	/** Semaphore used when the thread starts working */
	private Semaphore mSemaphore = null;
	
	/** Feed ID */
	protected String mFeedID = "";
	
	/** Handler used to send messages to the activity that will handle our work */
	private Handler mHandler;
	
	/** thread msg data */
	protected Bundle mData = new Bundle();
	
	/** if true we requested a stop, so do not handle any request stuff */
	private boolean mbStopRequested = false;
	
	/** Key which defines specific errors while parsing */
	public static final int ERROR_COULD_NOT_CREATE_RSS_HANDLER = ApplicationMNM.ERROR_FAILED+1;
	
	/** database access helper */
	private MeneameDbAdapter mDBHelper = null;
	
	/**
	 * 
	 * @param Activity ParentActivity, holds the semaphore to make this thread save
	 */
	public ArticleDBCacheThread() {
		super();
		
		ApplicationMNM.addLogCat(TAG);
	}
	
	/**
	 * Setup the thread with all data it needs to work with
	 * @param handler
	 * @param feedID
	 * @param threadSemaphore
	 */
	public void setupWorker( MeneameDbAdapter dBHelper, Handler handler, String feedID, Semaphore threadSemaphore ) {
		mFeedID = feedID;
		mSemaphore = threadSemaphore;
		mHandler = handler;
		mDBHelper = dBHelper;
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
		try {
			try {
				ApplicationMNM.logCat(TAG, "Aquirering semaphore " + mSemaphore.toString());
				mSemaphore.acquire();
			} catch (InterruptedException e) {
					return;
			}
			guardedRun();
		} catch (InterruptedException e) {
			// fall thru and exit normally
		} finally {
			ApplicationMNM.logCat(TAG, "Releasing semaphore " + mSemaphore.toString());
			mSemaphore.release();
		}
		
		// Release last refs
		mSemaphore = null;
		mHandler = null;
	}
	
	private void guardedRun() throws InterruptedException {
		// At this point we are thread safe
		if ( mHandler != null && !mbStopRequested )
		{
			Message msg = mHandler.obtainMessage();
			mData = new Bundle();
			mData.putInt(ApplicationMNM.COMPLETED_KEY, ApplicationMNM.COMPLETED_OK);
			mData.putInt(ApplicationMNM.ERROR_KEY, ApplicationMNM.ERROR_SUCCESSFULL);
			try {
				// Save feed
				msg.obj = mDBHelper.getFeed(mFeedID);
			} catch (Exception e) {
				ApplicationMNM.warnCat(TAG, "(Exception) Failed recover thread from database " + e.toString());
				setError(ApplicationMNM.ERROR_FAILED);
				e.printStackTrace();
			} finally {				
				// Send final message
				sendMessage(msg);
			}
		}
	}
	
	/**
	 * Send a message to our handler
	 * @param msg
	 */
	private void sendMessage( Message msg) {
		if ( mHandler != null && !mbStopRequested )
		{
			mData.putInt(ApplicationMNM.MSG_ID_KEY, ApplicationMNM.MSG_ID_DB_PARSER);
			msg.setData(mData);
			mHandler.sendMessage(msg);
		}
	}
	
	/**
	 * Set a new error message into our current data Bundle
	 * @param error
	 */
	protected void setError( int errorID ) {
		mData.putInt(ApplicationMNM.COMPLETED_KEY, ApplicationMNM.COMPLETED_FAILED);
		mData.putInt(ApplicationMNM.ERROR_KEY, errorID);
	}
}
