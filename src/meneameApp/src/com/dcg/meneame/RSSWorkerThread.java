package com.dcg.meneame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import com.dcg.app.ApplicationMNM;
import com.dcg.util.rss.BaseRSSWorkerThread;
import com.dcg.util.rss.Feed;
import com.dcg.util.rss.FeedItem;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

/**
 * This thread dose all the hard parsing work
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class RSSWorkerThread extends BaseRSSWorkerThread {
	
	/** log tag for this class */
	private static final String TAG = "RSSWorkerThread";
	
	// TODO: Make all SDCard access globally and configurable using always:
	//  - Environment.getExternalStorageDirectory()
	//  - File.separator
	// TODO: Caching stuff must be moved to it's own classes
	// TODO: Feed parsing must be moved to it's own classes
	
	/**
	 * 
	 * @param Activity ParentActivity, holds the semaphore to make this thread save
	 */
	public RSSWorkerThread() {
		super();
		ApplicationMNM.addLogCat(TAG);
		this.setmFeedParserClassName("com.dcg.meneame.FeedParser");
	}
	
	// TODO: Make all SDCard access globally and configurable using always:
	//  - Environment.getExternalStorageDirectory()
	//  - File.separator
	
	/**
	 * Prepares the SDCard with all we need
	 */
	protected void prepareSDCard() {
		// Create app dir in SDCard if possible
		File path = new File("/sdcard/com.dcg.meneame/cache/");
		if(! path.isDirectory()) {
			if ( path.mkdirs() )
			{
				ApplicationMNM.logCat(TAG,"Directory created: /sdcard/com.dcg.meneame/cache/");
			}
			else
			{
				ApplicationMNM.warnCat(TAG,"Failed to create directory: /sdcard/com.dcg.meneame/cache/");
			}
		}
	}
	
	/**
	 * Called once we parsed the file, the input reader has been reset before.
	 */
	protected void postProcessResult( Message msg, Feed parsedFeed ) {
		// Start with caching process
	}
	
	/**
	 * Called before we start requesting the feed source (file or online)
	 */
	protected void preInputStream() {
		MeneameDbAdapter dBHelper = new MeneameDbAdapter(ApplicationMNM.getCachedContext());
		dBHelper.open();		
		dBHelper.deleteFeedCache(mFeedID);	
		dBHelper.close();
	}
	
	/**
	 * Will parse the incomming data and save it into a Bundle
	 * @param page
	 * @return
	 */
	protected void parseResult( String page )
	{
		
		// By default this is just empty
		try {
			// Prepare process
			//prepareSDCard( data );
			
			// Check if any error occurred
		    File root = Environment.getExternalStorageDirectory();
		    if (root.canWrite()){
		        File gpxfile = new File("/sdcard/com.dcg.meneame/cache/feed.rss");
		        FileWriter gpxwriter = new FileWriter(gpxfile);
		        BufferedWriter out = new BufferedWriter(gpxwriter);
		        out.write(page);
		        out.close();
		        ApplicationMNM.logCat(TAG,"Feed written to: /sdcard/com.dcg.meneame/cache/feed.rss");
		    }
		    else
		    {
		    	Log.w(TAG, "Could not write file, SD Card not writeable.");
		    }		
		} catch (IOException e) {
			Log.w(TAG, "Could not write file " + e.getMessage());
		}
	}
	
	/**
	 * Will be called once the feed has been parsed
	 * @param parsedFeed
	 */
	protected void feedParsingFinished( Feed parsedFeed ) {
		
		List<FeedItem> articleList = parsedFeed.getArticleList();
		
		// The feed we just resolved
		ApplicationMNM.logCat(TAG,"Feed Parsed: " + parsedFeed.getKeyData("title"));
		ApplicationMNM.logCat(TAG,"  Articles: " + articleList.size());
	}
}
