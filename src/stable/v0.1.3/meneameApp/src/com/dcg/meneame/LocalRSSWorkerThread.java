package com.dcg.meneame;

import com.dcg.app.ApplicationMNM;
import com.dcg.util.rss.BaseLocalRSSWorkerThread;

public class LocalRSSWorkerThread extends BaseLocalRSSWorkerThread {
	/** log tag for this class */
	private static final String TAG = "LocalRSSWorkerThread";
	
	/**
	 * 
	 * @param Activity ParentActivity, holds the semaphore to make this thread save
	 */
	public LocalRSSWorkerThread() {
		super();
		ApplicationMNM.addLogCat(TAG);
		this.setmFeedParserClassName("com.dcg.meneame.FeedParser");
	}
}
