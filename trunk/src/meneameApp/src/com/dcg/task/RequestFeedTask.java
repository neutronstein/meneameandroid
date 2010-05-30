package com.dcg.task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.SQLException;

import com.dcg.app.ApplicationMNM;
import com.dcg.meneame.FeedActivity;
import com.dcg.provider.FeedItemElement;
import com.dcg.rss.RSSParser;
import com.dcg.rss.RSSParser.AddFeedItemListener;
import com.dcg.util.HttpManager;
import com.dcg.util.UserTask;

/**
 * Will download a feed from the net and cache it to the database
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class RequestFeedTask extends UserTask<RequestFeedTaskParams, Void, Integer> implements AddFeedItemListener {
	private static final String TAG = "RequestFeedTask";
	
	/** Error keys used as return types by the task */
	public static final int ERROR_COULD_NOT_CREATE_RSS_HANDLER = ApplicationMNM.ERROR_FAILED+1;
	public static final int ERROR_RSS_UNKOWN = ERROR_COULD_NOT_CREATE_RSS_HANDLER+1;
	public static final int ERROR_INVALID_RSS_DATA = ERROR_RSS_UNKOWN+1;
	public static final int ERROR_NO_INPUT_STREAM = ERROR_INVALID_RSS_DATA+1;
	public static final int ERROR_NO_INPUT_STREAM_EXCEPTION = ERROR_NO_INPUT_STREAM+1;
	public static final int ERROR_NO_INPUT_STREAM_FILE_NOT_FOUND = ERROR_NO_INPUT_STREAM_EXCEPTION+1;
	public static final int ERROR_NO_INPUT_STREAM_UNKOWN_HOST = ERROR_NO_INPUT_STREAM_FILE_NOT_FOUND+1;
	public static final int ERROR_NO_INPUT_STREAM_ILLEGAL_STATE = ERROR_NO_INPUT_STREAM_UNKOWN_HOST+1;
	public static final int ERROR_CREATE_FEEDITEM_ACCESS = ERROR_NO_INPUT_STREAM_ILLEGAL_STATE+1;
	public static final int ERROR_CREATE_FEEDITEM_INSTANCE = ERROR_CREATE_FEEDITEM_ACCESS+1;
	public static final int ERROR_CREATE_FEEDITEM_CLASS_NOT_FOUND = ERROR_CREATE_FEEDITEM_INSTANCE+1;
	public static final int ERROR_RSS_SAX = ERROR_CREATE_FEEDITEM_CLASS_NOT_FOUND+1;
	public static final int ERROR_RSS_IO_EXCEPTION = ERROR_RSS_SAX+1;
	public static final int ERROR_RSS_PARSE_CONFIG = ERROR_RSS_IO_EXCEPTION+1;
	public static final int ERROR_NOT_A_FEED_ACTIVITY = ERROR_RSS_PARSE_CONFIG+1;
	public static final int ERROR_NO_PARAMS = ERROR_NOT_A_FEED_ACTIVITY+1;
	
	/** Parser types */
	public static final int PARSER_TYPE_DEFAULT = 0;
	
	/** data params for the task */
	private RequestFeedTaskParams mMyParams;
	
	/** argument list */
	private static String[] sArguments1 = new String[1];
	
	/** Feed parser */
	private RSSParser mFeedParser = null;
	
	/**
     * Constructor
     */
    public RequestFeedTask(Activity activity) {
		super(activity);
		ApplicationMNM.addLogCat(TAG);
	}
    
	@Override
	public Integer doInBackground(RequestFeedTaskParams... params) {
		Integer bResult = ApplicationMNM.ERROR_SUCCESSFULL;

		// We need params to run
		if ( params[0] != null )
		{
			// Get params
			mMyParams = params[0];
			try {
				// Get stream from the net
				InputStreamReader streamReader = getInputStreamReader(mMyParams.mURL);
				try {
					ApplicationMNM.logCat(TAG, "Requesting feed: "+mMyParams.mFeedID);
					// Delete cache before staring the parsing process
					if ( deleteFeedCache(mMyParams.mFeedID) )
						ApplicationMNM.logCat(TAG, " Cache deleted");
					else
						ApplicationMNM.logCat(TAG, " Unable to delete cache");
					
					// Create the parser
					mFeedParser = getRSSParser(PARSER_TYPE_DEFAULT);
					mFeedParser.setInputStream(streamReader);
					mFeedParser.setmFeedItemClassName(mMyParams.mItemClass);
					mFeedParser.setMaxItems(mMyParams.mMaxItems);
					mFeedParser.onAddFeedItemListener(this);
					mFeedParser.parse();
				
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					bResult = ERROR_RSS_UNKOWN;
				} catch (InstantiationException e) {
					e.printStackTrace();
					bResult = ERROR_RSS_UNKOWN;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					bResult = ERROR_RSS_UNKOWN;
				} catch (ClassCastException e) {
					e.printStackTrace();
					bResult = ERROR_NOT_A_FEED_ACTIVITY;
				}
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
				bResult = ERROR_RSS_UNKOWN;
			} catch (IOException e1) {
				e1.printStackTrace();
				bResult = ERROR_RSS_UNKOWN;
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
				bResult = ERROR_RSS_UNKOWN;
			}
		}
		else
		{
			bResult = ERROR_NO_PARAMS;
		}
		
		// Cleanup!
		mFeedParser = null;
		
		return bResult;
	}
	
	/**
	 * Request this thread to stop what it's doing
	 */
	public void requestStop( boolean mayInterruptIfRunning) {
		if ( mFeedParser != null )
			mFeedParser.requestStop();
		cancel(mayInterruptIfRunning);
	}
	
	/**
	 * Will get the input stream reader the feed reader will use to parse it
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected InputStreamReader getInputStreamReader( String streamURL ) throws ClientProtocolException, IOException, URISyntaxException 
	{
		HttpGet request = new HttpGet();
		
		request.setURI(new URI(streamURL));
		ApplicationMNM.logCat(TAG, "Starting request " + request.toString());
		
		HttpResponse response = HttpManager.execute(request);
		
		if ( response != null )
		{
			return new InputStreamReader(response.getEntity().getContent());
		}
		return null;
	}
	
	/**
	 * Create a new feed parser
	 * @param parserClass
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 */
	private RSSParser getRSSParser(int parserType) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		return new RSSParser();
	}
	
	@Override
	public void onPostExecute(Integer result) {
		//ApplicationMNM.showToast("Finished with result: " + result);
		if ( mMyParams != null )
		{
			mMyParams.mFeedListener.onFeedFinished(result);
		}
	}
	
	/**
	 * Access the current content provider
	 * @return
	 */
	public ContentResolver getContentResolver() {
		return ((FeedActivity)mActivity).getContentResolver();
	}
	
	/**
	 * Delete an entire feed cache refernced by a feed ID
	 * @param feedID
	 */
	public boolean deleteFeedCache( int feedID ) {
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(feedID);
		final String where = FeedItemElement.FEEDID + "=?";
		int count = getContentResolver().delete(FeedItemElement.CONTENT_URI, where, arguments1);
		return count > 0;
	}
	
	/**
	 * Called from the RSS parser when a feed item has been parsed
	 */
	public void onFeedAdded(FeedItemElement feedItem) {
		// Set some feed specific values
		feedItem.setFeedID(mMyParams.mFeedID);
		feedItem.setType( ((FeedActivity)mActivity).getFeedItemType() );
		
		// Print it out
		ApplicationMNM.logCat(TAG, "FeedParsed: "+feedItem.getLinkID());
		
		try {
			// Insert the feed item
			getContentResolver().insert(FeedItemElement.CONTENT_URI, feedItem.getContentValues());
		} catch ( SQLException e ) {
			// Something went wrong!
			ApplicationMNM.warnCat(TAG, String.valueOf(e.getStackTrace()));
		}
	}
	
	/**
	 * Listener invoked by
	 * {@link com.dcg.task.RequestFeedTask#doInBackground(RequestFeedTaskParams...)}
	 * Once a feed has been finished processing.
	 */
	public static interface RequestFeedListener {
		
		/**
		 * Invoked when we finished a feed request. If the result code is !0 from
		 * 0 an error occurred.
		 * @param resultCode
		 * @param feed
		 */
		void onFeedFinished(Integer resultCode);
	}
}
