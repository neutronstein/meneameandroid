package com.dcg.meneame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class DefaultRSSWorkerThread extends BaseRSSWorkerThread {
	
	/** log tag for this class */
	private static final String TAG = "DefaultRSSWorkerThread";
	
	/** class that handles the caching process */
	public final String mFeedCacher = "";
	
	/** class we use to parse the feed */
	public final String mFeedParser = "";
	
	// TODO: Make all SDCard access globally and configurable using always:
	//  - Environment.getExternalStorageDirectory()
	//  - File.separator
	// TODO: Caching stuff must be moved to it's own classes
	// TODO: Feed parsing must be moved to it's own classes
	
	/**
	 * Will parse the incomming data and save it into a Bundle
	 * @param page
	 * @return
	 */
	protected Bundle parseResult( String page )
	{
		Bundle data = new Bundle();
		
		data = super.parseResult( page );
		
		// By default this is just empty
		try {
			// Prepare process
			prepareSDCard( data );
			
			// Check if any error occurred
			if ( data.getInt(CACHE_FEED_RESULT) == 0 ) 
			{
			    File root = Environment.getExternalStorageDirectory();
			    if (root.canWrite()){
			        File gpxfile = new File("/sdcard/com.dcg.meneame/cache/feed.rss");
			        FileWriter gpxwriter = new FileWriter(gpxfile);
			        BufferedWriter out = new BufferedWriter(gpxwriter);
			        out.write(page);
			        out.close();
			        data.putInt(CACHE_FEED_RESULT, CACHE_FEED_OK);
			        Log.d(TAG,"Feed written to: /sdcard/com.dcg.meneame/cache/feed.rss");
			    }
			    else
			    {
			    	data.putInt(CACHE_FEED_RESULT, CACHE_FEED_RESULT_RSS_FAILED_SD_NOTWRITEABLE);
			    	Log.w(TAG, "Could not write file, SD Card not writeable.");
			    }
			}
		} catch (IOException e) {
			data.putInt(CACHE_FEED_RESULT, CACHE_FEED_RESULT_RSS_FAILED_SD_CANNOTCREATEDRECTORY);
		    Log.w(TAG, "Could not write file " + e.getMessage());
		}
		
		return data;
	}
}
