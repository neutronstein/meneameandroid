package com.dcg.adapter;

import com.dcg.app.ApplicationMNM;
import com.dcg.meneame.FeedActivity;
import com.dcg.provider.FeedItemElement;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.SimpleCursorAdapter;

public class FeedItemAdapter extends CursorAdapter implements FilterQueryProvider {	
	private static final String[] PROJECTION_IDS_AND_TITLE = new String[] {
		FeedItemElement._ID,
		FeedItemElement.LINK_ID,
		FeedItemElement.FEEDID,
		FeedItemElement.COMMENT_RSS,
		FeedItemElement.TITLE,
		FeedItemElement.VOTES,
		FeedItemElement.LINK,
		FeedItemElement.DESCRIPTION,
		FeedItemElement.CATEGORY,
		FeedItemElement.URL,
	};
	
	private static final String TAG = "FeedItemAdapter";
	
	private final LayoutInflater mInflater;
	private final FeedActivity mActivity;
	
	/**
	 * Constructor
	 * @param activity
	 */
	public FeedItemAdapter(FeedActivity activity ) {
		super(activity, activity.managedQuery(FeedItemElement.CONTENT_URI,
                PROJECTION_IDS_AND_TITLE,
                null, null, FeedItemElement.DEFAULT_SORT_ORDER), true);
		
		ApplicationMNM.addLogCat(TAG);
		
		final Cursor c = getCursor();
		
		mActivity = activity;
		mInflater = LayoutInflater.from(activity);
	}
	
	@Override
    public void changeCursor(Cursor cursor) {
        final Cursor oldCursor = getCursor();
        if (oldCursor != null) mActivity.stopManagingCursor(oldCursor);
        super.changeCursor(cursor);
    }

	public Cursor runQuery(CharSequence arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
