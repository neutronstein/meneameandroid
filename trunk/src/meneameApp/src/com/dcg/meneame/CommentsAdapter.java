package com.dcg.meneame;

import android.content.Context;
import android.view.LayoutInflater;

import com.dcg.app.ApplicationMNM;
import com.dcg.util.rss.Feed;

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
