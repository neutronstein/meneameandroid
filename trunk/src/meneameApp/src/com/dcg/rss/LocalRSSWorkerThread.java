package com.dcg.rss;

import com.dcg.app.ApplicationMNM;

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
		this.setmFeedParserClassName("com.dcg.rss.FeedParser");
	}
}
