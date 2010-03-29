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
import android.util.Log;
import android.widget.Toast;

public class ApplicationMNM extends Application {
	
	/** log tag for this class */
	private static final String TAG = "ApplicationMNM";
	
	/** The actual record of activity records */
	private List<TabActivityRecord> mTabActivityRecord = new ArrayList<TabActivityRecord>();

	/** Shared HttpClient used by our application */
	private HttpClient mHttpClient = null;
	
	/** Toast message handler */
	protected Toast mToast = null;
	
	/** Category used to filter the category list */
	private static List<String> mLogCatList = new ArrayList<String>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Create shared HttpClient
		mHttpClient = createHttpClient();
		
		//ApplicationMNM.AddLogCat(TAG);
	}
	
	/**
	 * Add a new category to the category log
	 * @param cat
	 */
	public static void AddLogCat( String cat ) {
		if ( !mLogCatList.contains(cat) )
		{
			mLogCatList.add(cat);
		}
	}
	
	/**
	 * Remove a category from the log
	 * @param cat
	 */
	public static void RemoveLogCat( String cat ) {
		if ( mLogCatList.contains(cat) )
		{
			mLogCatList.remove(cat);
		}
	}
	
	/**
	 * Print a log with a specific category
	 * @param msg
	 * @param cat
	 */
	public static void LogCat( String cat, String msg ) {
		if ( mLogCatList.contains(cat) )
		{
			Log.d(cat, msg);
		}
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
		ApplicationMNM.LogCat(TAG, "Clearing TabActivityRecord...");
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
		ApplicationMNM.LogCat(TAG,"createHttpClient()");
		
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
			ApplicationMNM.LogCat(TAG, "Shutting current HttpClient down");
			mHttpClient.getConnectionManager().shutdown();
		}
	}
	
	/**
	 * Shows a toast message, will hide any already shown message
	 * @param msg
	 */
	public void ShowToast( String msg ) {
		if ( mToast == null )
		{
			mToast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT);
		}
		else
		{
			mToast.cancel();
			mToast.setText( msg );
		}
		mToast.show();
	}
}
