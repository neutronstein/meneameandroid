package com.dcg.app;

import android.content.Context;

/**
 * The RESTful manager handles and updates any REST methods we are executing.</br>
 * By now it handles states of already executed methods or methods that are </br>
 * a transaction state.</br>
 * </br>
 * The manager it self is a singleton so you can have only one instance at time.</br>
 * The context will be set by ApplicationMNM so you do not need to worry about it.</br>
 * </br>
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class RESTfulManager {
	private static final String TAG = "RESTfulManager";
	
	/** Context in which we operate */
	private Context mContext = null;
	
	/** Our static instance */
	private static RESTfulManager mInstance = null;
	
	private RESTfulManager() {
		ApplicationMNM.addLogCat(TAG);
	}
	
	/**
	 * Will return a RESTfulManager instance.
	 * @return RESTfulManager
	 */
	public static synchronized RESTfulManager getInstance() {
		if ( mInstance == null )
		{
			mInstance = new RESTfulManager();
		}
		return mInstance;
	}
	
	/**
	 * Register a context that will be used by the manager
	 * @param context
	 */
	public void setContext( Context context  ) {
		mContext = context;
	}
}
