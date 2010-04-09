package com.dcg.util.rss;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.dcg.app.ApplicationMNM;

/**
 * This worker theard uses a local file 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
abstract public class BaseLocalRSSWorkerThread extends BaseRSSWorkerThread {
	/** log tag for this class */
	private static final String TAG = "BaseLocalRSSWorkerThread";
	
	/**
	 * 
	 * @param Activity ParentActivity, holds the semaphore to make this thread save
	 */
	public BaseLocalRSSWorkerThread() {
		super();
		
		ApplicationMNM.addLogCat(TAG);
	}

	/**
	 * Will get the input stream reader the feed reader will use to parse it
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected InputStreamReader getInputStreamReader() throws ClientProtocolException, IOException, URISyntaxException 
	{
		return new InputStreamReader(new FileInputStream(mFeedURL), "UTF-8");
	}
}
