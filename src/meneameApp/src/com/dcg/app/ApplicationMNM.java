package com.dcg.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * General app object used by android life cycle
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class ApplicationMNM extends Application {
	
	/** Current version of the app*/
	private static final int mAppVersion = 3;
	
	/** log tag for this class */
	private static final String TAG = "ApplicationMNM";
	
	/** Shared HttpClient used by our application */
	private HttpClient mHttpClient = null;
	
	/** Toast message handler */
	private static Toast mToast = null;
	
	/** Category used to filter the category list */
	private static List<String> mLogCatList = new ArrayList<String>();
	
	/** Ignore category list */
	private static List<String> mIgnoreCatList = new ArrayList<String>();
	
	/** Enable logging or not */
	private static boolean mbEnableLogging = true;
	
	/** Cached context to be able to achieve static access */
	private static Context mAppContext = null;
	
	/** IDs used to handle diffrenet messages comming from different threads */
	public static final String MSG_ID_KEY = "msg_id_key";
	public static final int MSG_ID_ARTICLE_PARSER = 0;
	public static final int MSG_ID_MENEALO = 1;
	
	/** Some global definitions */
	public static final String MENEAME_BASE_URL = "http://www.meneame.net";
	
	@Override
	public void onCreate() {
		super.onCreate();		
		ApplicationMNM.addLogCat(TAG);
		ApplicationMNM.logCat(TAG, "onCreate()");
		
		// Create log ignore list!
		// Note: To use a log just comment it :D
		addIgnoreCat(""); // Yep... empty too ;P
		addIgnoreCat("MeneameAPP");
		addIgnoreCat("ApplicationMNM");
		addIgnoreCat("RSSParser");
		addIgnoreCat("RSSWorkerThread");
		addIgnoreCat("FeedItem");
		addIgnoreCat("Feed");
		addIgnoreCat("BaseRSSWorkerThread");
		addIgnoreCat("FeedParser");
		addIgnoreCat("FeedActivity");
		addIgnoreCat("ArticlesAdapter");
		addIgnoreCat("CommentsAdapter");
		addIgnoreCat("Preferences");
		addIgnoreCat("NotameActivity");
		addIgnoreCat("ArticleFeedItem");

		// Create shared HttpClient
		mHttpClient = createHttpClient();
	}
	
	/**
	 * Clear the cached context, called from the main activity in it's 
	 * onDestroy() call.
	 */
	public void clearCachedContext() {
		mAppContext = null;
	}
	
	/**
	 * Set cached context for the app
	 * @param context
	 */
	public void setCachedContext( Context context ) {
		mAppContext = context;
	}
	
	/**
	 * Returns the version number we are currently in
	 */
	public static int getVersionNumber() {
		return mAppVersion;
	}
	
	/**
	 * Add a new category to the category ignore list
	 * @param cat
	 */
	public static void addIgnoreCat( String cat ) {
		if ( mbEnableLogging && !mIgnoreCatList.contains(cat) )
		{
			mIgnoreCatList.add(cat);
		}
	}
	
	/**
	 * Add a new category to the category log
	 * @param cat
	 */
	public static void addLogCat( String cat ) {
		if ( mbEnableLogging && !mIgnoreCatList.contains(cat) && !mLogCatList.contains(cat) )
		{
			mLogCatList.add(cat);
		}
	}
	
	/**
	 * Remove a category from the log
	 * @param cat
	 */
	public static void removeLogCat( String cat ) {
		if ( mbEnableLogging && mLogCatList.contains(cat) )
		{
			mLogCatList.remove(cat);
		}
	}
	
	/**
	 * Print a log with a specific category
	 * @param msg
	 * @param cat
	 */
	public static void logCat( String cat, String msg ) {
		if ( mbEnableLogging && mLogCatList.contains(cat) )
		{
			Log.d(cat, msg);
		}
	}
	
	/**
	 * Print a warn with a specific category
	 * @param msg
	 * @param cat
	 */
	public static void warnCat( String cat, String msg ) {
		Log.w(cat, msg);
	}
	
	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
		ApplicationMNM.logCat(TAG, "onLowMemory()");
		shutdownHttpClient();
	}
	
	@Override
	public void onTerminate()
	{
		super.onTerminate();
		ApplicationMNM.logCat(TAG, "onLowMemory()");
		shutdownHttpClient();
	}
	
	/**
	 * Create and configura a httpclient
	 * @return
	 */
	private HttpClient createHttpClient()
	{
		ApplicationMNM.logCat(TAG,"createHttpClient()");
		
		// Set basic data
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);
		
		// Make pool
		ConnPerRoute connPerRoute = new ConnPerRouteBean(12); 
		ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
		ConnManagerParams.setMaxTotalConnections(params, 20); 
		
		// Register http/s shemas!
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		ThreadSafeClientConnManager conMgr = new ThreadSafeClientConnManager(params,schReg);
		return new DefaultHttpClient(conMgr, params);
	}
	
	/**
	 * Returns the sharde http client
	 * @return HttpClient
	 */
	public HttpClient getHttpClient() {
		return mHttpClient;
	}
	
	/**
	 * Shutdown current HttpClient's connection
	 */
	private void shutdownHttpClient()
	{
		if(mHttpClient!=null && mHttpClient.getConnectionManager()!=null)
		{
			ApplicationMNM.logCat(TAG, "Shutting current HttpClient down");
			mHttpClient.getConnectionManager().shutdown();
		}
	}
	
	/**
	 * Will shutdown and create a new httpclient
	 */
	public void refreshHttpClient() {
		shutdownHttpClient();
		mHttpClient = createHttpClient();
	}
	
	/**
	 * Clear any pending http connections
	 */
	public void clearHttpClientConnections()
	{
		if(mHttpClient!=null && mHttpClient.getConnectionManager()!=null)
		{
			mHttpClient.getConnectionManager().closeExpiredConnections();
			mHttpClient.getConnectionManager().closeIdleConnections(10, TimeUnit.SECONDS );
		}
	}
	
	/**
	 * Shows a toast message, will hide any already shown message
	 * @param msg
	 */
	public static void showToast( String msg ) {
		if ( mAppContext != null )
		{
			if ( mToast == null )
			{
				mToast = Toast.makeText(mAppContext, msg, Toast.LENGTH_SHORT);
			}
			else
			{
				mToast.cancel();
				mToast.setText( msg );
			}
			mToast.show();
		}
	}
	
	/**
	 * Shows a toast message but referencing a resource id and not directly a string
	 * @param id
	 */
	public static void showToast( int id ) {
		showToast(mAppContext.getResources().getString(id));
	}
	
	/**
	 * Returns the root folder we will use in the SDCard
	 * @return
	 */
	public static String getRootSDcardFolder() {
		return Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"com.dcg.meneame"+File.separator;
	}
	
	/**
	 * Returns the root path to our cache foldet
	 * @return
	 */
	public static String getRootCacheFolder() {
		return getRootSDcardFolder()+"cache"+File.separator;
	}
	
	/**
	 * Clear all cached feeds
	 * @return
	 */
	public static boolean clearFeedCache() {
		try {
			File directory = new File(getRootCacheFolder());
			fileDelete(directory);
			return true;
		} catch (IOException e ) {
			warnCat(TAG, "Failed to clear cache: "+e.toString());
		}
		return false;
	}
	
	/**
	 * Delete a file/directory recursively
	 * @param srcFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void fileDelete(File srcFile) throws FileNotFoundException, IOException {
		if (srcFile.isDirectory()) 
		{
			File[] b = srcFile.listFiles();
			for (int i = 0; i < b.length; i++) 
			{
				fileDelete(b[i]);
			}
			srcFile.delete();
		} 
		else 
		{
			srcFile.delete();
		}
	}
}
