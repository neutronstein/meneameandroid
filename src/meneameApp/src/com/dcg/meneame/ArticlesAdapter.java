package com.dcg.meneame;

import java.util.ArrayList;
import java.util.List;

import com.dcg.app.ApplicationMNM;
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
	private List<FeedItem> mData=new ArrayList<FeedItem>();
	private LayoutInflater mInflater;
	
	public ArticlesAdapter(Context context, List<FeedItem> data ) {
		// Cache the LayoutInflate to avoid asking for a new one each time.
		mInflater = LayoutInflater.from(context);
		
		// Save data, this will just copy the items so that once we use this adapter we
		// can get rid of the actual feed objects we generated when parsing the feed
		mData.addAll(data);
	}
	
	/**
	 * The number of items in the list is determined by the number of speeches
	 * in our array.
	 *
	 * @see android.widget.ListAdapter#getCount()
	 */
	public int getCount() {
		return mData.size();
	}
	
	/**
	 * Return the feed object at a specific list index
	 * 
	 * @see android.widget.ListAdapter#getItem(int)
	 */
	public Object getItem(int position) {
		return mData.get(position);
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
	 * Make a view to hold each row.
	 *
	 * @see android.widget.ListAdapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unnecessary calls
		// to findViewById() on each row.
		ViewHolder holder;
		
		// When convertView is not null, we can reuse it directly, there is no need
		// to reinflate it. We only inflate a new View when the convertView supplied
		// by ListView is null.
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.meneo_listitem, null);
			
			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.description = (TextView) convertView.findViewById(R.id.description);
			
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}
		
		FeedItem feedItem = null;
		String title = "";
		String description = "";
		try {
			// Get item
			feedItem = (FeedItem)getItem(position);
			
			// Now get data
			// TODO: Control null return values!
			title = (String)feedItem.getKeyData("title");
			description = (String)feedItem.getKeyData("description");
		} catch ( ClassCastException  e )
		{
			// What the hell!
			ApplicationMNM.LogCat(TAG, "Failed to ceate view for item at position ["+position+"]");
			e.printStackTrace();
		}
		
		if ( feedItem != null )
		{
			// Bind the data efficiently with the holder.
			holder.title.setText(title);
			holder.description.setText(description);
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
    }
}
