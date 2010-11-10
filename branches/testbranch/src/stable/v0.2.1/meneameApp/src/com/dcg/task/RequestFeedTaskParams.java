package com.dcg.task;

import com.dcg.task.RequestFeedTask.RequestFeedListener;

/**
 * Params used by our request feed task
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class RequestFeedTaskParams {
	public String 				mURL;
	public String 				mParserClass;
	public String 				mItemClass;
	public int 					mMaxItems;
	public RequestFeedListener 	mFeedListener;
}
