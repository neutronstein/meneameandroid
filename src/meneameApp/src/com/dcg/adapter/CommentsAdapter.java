package com.dcg.adapter;

import android.content.Context;

import com.dcg.app.ApplicationMNM;
import com.dcg.meneame.R;
import com.dcg.rss.Feed;

/**
 * Special adapter used by any comments activity
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class CommentsAdapter extends ArticlesAdapter {
	private static final String TAG = "CommentsAdapter";

	public CommentsAdapter() {
		super();
		ApplicationMNM.addLogCat(TAG);
		
		// Set default layout
		mItenLayoutID = R.layout.meneo_listitem_comments;
	}
	
	public CommentsAdapter(Context context, Feed feed) {
		super(context,feed);
		ApplicationMNM.addLogCat(TAG);
		
		// Set default layout
		mItenLayoutID = R.layout.meneo_listitem_comments;
	}

}
