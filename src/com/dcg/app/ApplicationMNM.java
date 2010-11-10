package com.dcg.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
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
import com.dcg.meneame.TabActivityRecord;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * General app object used by android life cycle
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class ApplicationMNM extends Application {
	
	/** log tag for this class */
	private static final String TAG = "ApplicationMNM";
	
	/** The actual record of activity records */
	private List<TabActivityRecord> mTabActivityRecord = new ArrayList<TabActivityRecord>();

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
	
	private static Context mAppContext = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Cache contentxt
		mAppContext = getBaseContext();
		
		// Create log ignore list!
		// Note: To use a log just comment it :D
		addIgnoreCat(""); // Yep... empty too ;P
		addIgnoreCat("MeneameMainActivity");
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

		// Create shared HttpClient
		mHttpClient = createHttpClient();
		
		ApplicationMNM.addLogCat(TAG);
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
	
	/**
	 * Add a new activity we use in our main tab to the global registry
	 * @param String - tag
	 * @param Activity - activity
	 */
	public void addTabActivity( String  tag, Activity activity ) {
		if ( !isTabActivityRegistered( tag ) )
		{
			mTabActivityRecord.add(new TabActivityRecord( tag, activity ));
		}
	}
	
	/**
	 * Removes the activity linked to a tag
	 * @param tag
	 */
	public void removeTabActivity( String tag ) {
		int index = getTabActivityIndex(tag);
		if ( index != -1 )
		{
			mTabActivityRecord.remove( index );
		}
	}
	
	/**
	 * Returns an iterator to navigate through the list of registered tab activities
	 * @return Iterator of TabActivityRecords
	 */
	public Iterator<TabActivityRecord> getTabActivityRecordIterator() {
		return (Iterator<TabActivityRecord>)mTabActivityRecord.iterator();
	}
	
	/**
	 * Will return the Activity linked to a tag
	 * @param tag
	 * @return Activity
	 */
	public Activity getTabActivity( String tag ) {
		if ( isTabActivityRegistered( tag ) )
		{
			return mTabActivityRecord.get( getTabActivityIndex( tag ) ).getTabActivity();
		}
		return null;
	}
	
	/**
	 * Returns the index of the tab activity registered with a sepcifc tag
	 * @param tag
	 * @return int - index
	 */
	public int getTabActivityIndex( String tag ) {
		// Iterate through the list
		Iterator<TabActivityRecord> it = getTabActivityRecordIterator();
		int i = 0;
		
		while(it.hasNext())
		{
			TabActivityRecord tabRecord = it.next();
			if ( tabRecord.getTabTag().compareTo( tag ) == 0 )
			{
				return i;
			}
			i++;
		}
		return -1;
	}
	
	/**
	 * 
	 * @param tag
	 * @return true if an activity with the same tag is already registred
	 */
	public boolean isTabActivityRegistered( String tag ) {
		return getTabActivityIndex( tag ) != -1;
	}
	
	/**
	 * Clears all registered activities
	 */
	public void clearTabActivityRecord() {
		mTabActivityRecord.clear();
		ApplicationMNM.logCat(TAG, "Clearing TabActivityRecord...");
	}
	
	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
		shutdownHttpClient();
	}
	
	@Override
	public void onTerminate()
	{
		super.onTerminate();
		shutdownHttpClient();
		clearTabActivityRecord();
	}
	
	private HttpClient createHttpClient()
	{
		ApplicationMNM.logCat(TAG,"createHttpClient()");
		
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);
		
		// Register http/s shemas!
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params,schReg);
		
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
}
