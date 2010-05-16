package com.dcg.task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;

import com.dcg.app.ApplicationMNM;
import com.dcg.rss.Feed;
import com.dcg.rss.RSSParser;
import com.dcg.util.HttpManager;
import com.dcg.util.UserTask;

/**
 * Will download a feed from the net and cache it to the database
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class RequestFeedTask extends UserTask<RequestFeedTaskParams, Void, Integer> {
	private static final String TAG = "RequestFeedTask";
	
	/** Error keys used as return types by the task */
	public static final int ERROR_COULD_NOT_CREATE_RSS_HANDLER = ApplicationMNM.ERROR_FAILED+1;
	public static final int ERROR_INVALID_RSS_DATA = ERROR_COULD_NOT_CREATE_RSS_HANDLER+1;
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
	public static final int ERROR_RSS_UNKOWN = ERROR_RSS_PARSE_CONFIG+1;
	
	/** data params for the task */
	private RequestFeedTaskParams mMyParams;
	
	/** Parsed feed */
	private Feed mFeed;
	
	/**
     * Constructor
     */
    public RequestFeedTask(Activity activity) {
		super(activity);
	}
    
	@Override
	public Integer doInBackground(RequestFeedTaskParams... params) {
		Integer bResult = ApplicationMNM.ERROR_SUCCESSFULL;
		
		// Get params
		mMyParams = params[0];
		
		// Get feed from the net
		try {
			InputStreamReader streamReader = getInputStreamReader(mMyParams.mURL);
			try {
				// Create the parser
				RSSParser feedParser = getRSSParser(mMyParams.mParserClass);
				feedParser.setInputStream(streamReader);
				feedParser.setmFeedItemClassName(mMyParams.mItemClass);
				feedParser.setMaxItems(mMyParams.mMaxItems);
				feedParser.parse();
				mFeed = feedParser.getFeed();
				
				// Parsed so start caching process!
			
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				bResult = ERROR_RSS_UNKOWN;
			} catch (InstantiationException e) {
				e.printStackTrace();
				bResult = ERROR_RSS_UNKOWN;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				bResult = ERROR_RSS_UNKOWN;
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
		
		return bResult;
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
	private RSSParser getRSSParser(String parserClass) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		return (RSSParser)Class.forName(parserClass).newInstance();
	}
	
	@Override
	public void onPostExecute(Integer result) {
		//ApplicationMNM.showToast("Finished with result: " + result);
		if ( mMyParams != null )
		{
			mMyParams.mFeedListener.onFeedFinished(result, mFeed);
			
			// Clear reference to feed object
			mFeed = null;
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
		void onFeedFinished(Integer resultCode, Feed feed);
	}

}
