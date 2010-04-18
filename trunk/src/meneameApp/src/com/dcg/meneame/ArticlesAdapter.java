package com.dcg.meneame;

import java.util.ArrayList;

import com.dcg.app.ApplicationMNM;
import com.dcg.util.rss.Feed;
import com.dcg.util.rss.FeedItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Efficient adapter implementing the ViewHolder pattern used by our article list.
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class ArticlesAdapter extends BaseAdapter {
	
	private static final String TAG = "ArticlesAdapter";
	private Feed mFeed=null;
	private LayoutInflater mInflater;
	
	/** Last position we created a view for */
	private int mLastPosition;
	
	/** layout used to each item */
	protected int mItenLayoutID;
	
	public ArticlesAdapter() {
		ApplicationMNM.addLogCat(TAG);
		
		// Set default layout
		mItenLayoutID = R.layout.meneo_listitem;
	}
	
	public ArticlesAdapter(Context context, Feed feed) {
		ApplicationMNM.addLogCat(TAG);
		// Cache the LayoutInflate to avoid asking for a new one each time.
		mInflater = LayoutInflater.from(context);
		
		// Save feed instance!
		mFeed = feed;
		
		// Set default layout
		mItenLayoutID = R.layout.meneo_listitem;
	}
	
	public void setupAdapter( Context context, Feed feed ) {
		// Cache the LayoutInflate to avoid asking for a new one each time.
		mInflater = LayoutInflater.from(context);
		
		// Save feed instance!
		mFeed = feed;
	}
	
	/**
	 * The number of items in the list is determined by the number of speeches
	 * in our array.
	 *
	 * @see android.widget.ListAdapter#getCount()
	 */
	public int getCount() {
		return mFeed.getArticleCount();
	}
	
	/**
	 * Return the feed object at a specific list index
	 * 
	 * @see android.widget.ListAdapter#getItem(int)
	 */
	public Object getItem(int position) {
		return mFeed.getArticle(position);
	}
	
	/**
	 * Use the array index as a unique id.
	 *
	 * @see android.widget.ListAdapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * Will return the last position we created/refreshed a view for
	 * @return
	 */
	public int getLastPosition() {
		return mLastPosition;
	}
	
	/**
	 * Make a view to hold each row.
	 *
	 * @see android.widget.ListAdapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	@SuppressWarnings("unchecked")
	public View getView(int position, View convertView, ViewGroup parent) {
		try {
			// Cache this position so we can restore the location on the view
			mLastPosition = position;
			
			// A ViewHolder keeps references to children views to avoid unnecessary calls
			// to findViewById() on each row.
			ViewHolder holder;
			
			// When convertView is not null, we can reuse it directly, there is no need
			// to reinflate it. We only inflate a new View when the convertView supplied
			// by ListView is null.
			if (convertView == null) {
				convertView = mInflater.inflate(mItenLayoutID, null);
				
				// Creates a ViewHolder and store references to the two children views
				// we want to bind data to.
				holder = new ViewHolder();
				Object viewObj = null;
				viewObj = convertView.findViewById(R.id.title);
				if ( viewObj != null )
					holder.title = (TextView)viewObj;
				viewObj = convertView.findViewById(R.id.description);
				if ( viewObj != null )
					holder.description = (TextView)viewObj;
				viewObj = convertView.findViewById(R.id.votes);
				if ( viewObj != null )
					holder.votes = (TextView)viewObj;
				viewObj = convertView.findViewById(R.id.source);
				if ( viewObj != null )
					holder.url = (TextView)viewObj;
				viewObj = convertView.findViewById(R.id.tags_content);
				if ( viewObj != null )
					holder.category = (TextView)viewObj;
				
				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
			
			FeedItem feedItem = null;
			String title = "";
			String description = "";
			String votes = "";
			String url = "";
			String category = "";
			try {
				// Get item
				feedItem = (FeedItem)getItem(position);
				
				// Now get data
				title = feedItem.getRawKeyData("title");
				if ( ApplicationMNM.mbShowArticlePositions )
				{
					title = "["+position+"] "+title;
				}
				description = feedItem.getRawKeyData("description");
				votes = feedItem.getRawKeyData("votes");
				url = feedItem.getRawKeyData("url");
				
				ArrayList<String> tags = (ArrayList<String>)feedItem.getKeyData("category");
				if ( tags != null )
				{
					int tagsNum = tags.size();
					for (int i = 0; i < tagsNum; i++ )
					{
						category += tags.get(i);
						// Add separator if needed
						if ( i < tagsNum - 1 )
						{
							category += ", ";
						}
					}
				}
				
			} catch ( ClassCastException  e )
			{
				// What the hell!
				ApplicationMNM.logCat(TAG, "Failed to ceate view for item at position ["+position+"]");
				e.printStackTrace();
			}
			
			if ( feedItem != null )
			{
				// Bind the data efficiently with the holder.
				if ( holder.title != null )
					holder.title.setText(title);
				if ( holder.description != null )
					holder.description.setText(description);
				if ( holder.votes != null )
					holder.votes.setText(votes);
				if ( holder.url != null )
					holder.url.setText(url);
				if ( holder.category != null )
					holder.category.setText(category);
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		} finally {
			// Force GC
			System.gc();
		}
		return convertView;
	}
	
	/**
	 * View holder for our articles, this must match the layout we are using!
	 * @author Moritz Wundke (b.thax.dcg@gmail.com)
	 *
	 */
	static class ViewHolder {
        TextView title;
        TextView description;
        TextView votes;
        TextView url;
        TextView category;
    }
}
