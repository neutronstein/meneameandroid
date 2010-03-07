package com.dcg.meneame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class DefaultRSSWorkerThread extends BaseRSSWorkerThread {
	
	/** log tag for this class */
	private static final String TAG = "DefaultRSSWorkerThread";
	
	// TODO: Make all SDCard access globally and configurable using always:
	//  - Environment.getExternalStorageDirectory()
	//  - File.separator
	// TODO: Caching stuff must be moved to it's own classes
	// TODO: Feed parsing must be moved to it's own classes
	
	/**
	 * 
	 * @param Activity ParentActivity, holds the semaphore to make this thread save
	 */
	public DefaultRSSWorkerThread() {
		super();	
		
		ApplicationMNM.AddLogCat(TAG);
	}
	
	// TODO: Make all SDCard access globally and configurable using always:
	//  - Environment.getExternalStorageDirectory()
	//  - File.separator
	
	/**
	 * Prepares the SDCard with all we need
	 */
	protected void prepareSDCard( Bundle data ) {
		// Create app dir in SDCard if possible
		File path = new File("/sdcard/com.dcg.meneame/cache/");
		if(! path.isDirectory()) {
			if ( path.mkdirs() )
			{
				ApplicationMNM.LogCat(TAG,"Directory created: /sdcard/com.dcg.meneame/cache/");
			}
			else
			{
				Log.w(TAG,"Failed to create directory: /sdcard/com.dcg.meneame/cache/");
			}
		}
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
		        ApplicationMNM.LogCat(TAG,"Feed written to: /sdcard/com.dcg.meneame/cache/feed.rss");
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
		ApplicationMNM.LogCat(TAG,"Feed Parsed: " + parsedFeed.getKeyData("title"));
		ApplicationMNM.LogCat(TAG,"  Articles: " + articleList.size());
	}
}
