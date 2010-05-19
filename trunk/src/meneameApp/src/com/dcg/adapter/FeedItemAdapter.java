package com.dcg.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

import com.dcg.app.ApplicationMNM;
import com.dcg.meneame.FeedActivity;
import com.dcg.meneame.R;
import com.dcg.provider.FeedItemElement;

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
	
	/** Column indexes */
	private final int[] mColumnIndexArray = new int[9];
	
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
		
		// Setup column index array
		mColumnIndexArray[FeedItemElement.LINK_ID_FIELD] = 
			c.getColumnIndexOrThrow(FeedItemElement.LINK_ID);
		mColumnIndexArray[FeedItemElement.FEEDID_FIELD] = 
			c.getColumnIndexOrThrow(FeedItemElement.FEEDID);
		mColumnIndexArray[FeedItemElement.COMMENT_RSS_FIELD] = 
			c.getColumnIndexOrThrow(FeedItemElement.COMMENT_RSS);
		mColumnIndexArray[FeedItemElement.TITLE_FIELD] = 
			c.getColumnIndexOrThrow(FeedItemElement.TITLE);
		mColumnIndexArray[FeedItemElement.VOTES_FIELD] = 
			c.getColumnIndexOrThrow(FeedItemElement.VOTES);
		mColumnIndexArray[FeedItemElement.LINK_FIELD] = 
			c.getColumnIndexOrThrow(FeedItemElement.LINK);
		mColumnIndexArray[FeedItemElement.DESCRIPTION_FIELD] = 
			c.getColumnIndexOrThrow(FeedItemElement.DESCRIPTION);
		mColumnIndexArray[FeedItemElement.CATEGORY_FIELD] = 
			c.getColumnIndexOrThrow(FeedItemElement.CATEGORY);
		mColumnIndexArray[FeedItemElement.URL_FIELD] = 
			c.getColumnIndexOrThrow(FeedItemElement.URL);
	}
	
	@Override
    public void changeCursor(Cursor cursor) {
        final Cursor oldCursor = getCursor();
        if (oldCursor != null) mActivity.stopManagingCursor(oldCursor);
        super.changeCursor(cursor);
    }

	public Cursor runQuery(CharSequence constraint) {
		// We really do not want any constraint to be used in this adapter,
		// all feed items should be shown up!
        return mActivity.managedQuery(FeedItemElement.CONTENT_URI, PROJECTION_IDS_AND_TITLE,
                null, null, FeedItemElement.DEFAULT_SORT_ORDER);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		FeedItemViewHolder holder = (FeedItemViewHolder) view.getTag();
		
		// Set meta-data
		holder.link_id = cursor.getInt(mColumnIndexArray[FeedItemElement.LINK_ID_FIELD]);
		holder.feedid = cursor.getInt(mColumnIndexArray[FeedItemElement.FEEDID_FIELD]);
		holder.comment_rss = cursor.getString(mColumnIndexArray[FeedItemElement.COMMENT_RSS_FIELD]);
		
		// Bind the data efficiently with the holder.
		if ( holder.title != null )
			holder.title.setText(cursor.getString(mColumnIndexArray[FeedItemElement.TITLE_FIELD]));
		if ( holder.description != null )
			holder.description.setText(cursor.getString(mColumnIndexArray[FeedItemElement.DESCRIPTION_FIELD]));
		if ( holder.votes != null )
			holder.votes.setText(cursor.getInt(mColumnIndexArray[FeedItemElement.VOTES_FIELD]));
		if ( holder.url != null )
			holder.url.setText(cursor.getString(mColumnIndexArray[FeedItemElement.URL_FIELD]));
		if ( holder.category != null )
			holder.category.setText(cursor.getString(mColumnIndexArray[FeedItemElement.CATEGORY_FIELD]));
	}
	
	/**
	 * Returns the layout used for the item.
	 * @return
	 */
	public int getItemLayout(Context context, Cursor cursor, ViewGroup parent)
	{
		return R.layout.meneo_listitem;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = (TextView) mInflater.inflate(getItemLayout(context,cursor,parent), parent, false);
		
		// Create view holder
		FeedItemViewHolder holder = new FeedItemViewHolder();
		Object viewObj = null;
		viewObj = view.findViewById(R.id.title);
		if ( viewObj != null )
			holder.title = (TextView)viewObj;
		viewObj = view.findViewById(R.id.description);
		if ( viewObj != null )
			holder.description = (TextView)viewObj;
		viewObj = view.findViewById(R.id.votes);
		if ( viewObj != null )
			holder.votes = (TextView)viewObj;
		viewObj = view.findViewById(R.id.source);
		if ( viewObj != null )
			holder.url = (TextView)viewObj;
		viewObj = view.findViewById(R.id.tags_content);
		if ( viewObj != null )
			holder.category = (TextView)viewObj;
		
		// Use the view holder
		view.setTag(holder);		
        return view;
	}

}
