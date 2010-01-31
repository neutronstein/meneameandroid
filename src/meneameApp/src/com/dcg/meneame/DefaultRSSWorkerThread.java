package com.dcg.meneame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
		        Log.d(TAG,"Feed written to: /sdcard/com.dcg.meneame/cache/feed.rss");
		    }
		    else
		    {
		    	Log.w(TAG, "Could not write file, SD Card not writeable.");
		    }		
		} catch (IOException e) {
			Log.w(TAG, "Could not write file " + e.getMessage());
		}
	}
}
