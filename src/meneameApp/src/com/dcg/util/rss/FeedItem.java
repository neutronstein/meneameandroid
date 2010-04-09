package com.dcg.util.rss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import com.dcg.app.ApplicationMNM;

/**
 * Base item our feed parser will return, must be subclassed to own types.
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class FeedItem extends Object {
	
	/** log tag for this class */
	private static final String TAG = "FeedItem";
	
	/** Our map that holds the internal data */
	protected Map<String, String> mItemData = null;
	
	/** Character used to separate list items if the map type is a list */
	public static final String LIST_SEPARATOR = ";";
	
	/** our semaphore to make this class thread safe! */
	private Semaphore mSemaphore = new Semaphore(1);
	
	/** List of keys permitted */
	protected static List<String> mPermittedList = new ArrayList<String>();
	
	/** List of keys restricted */
	protected static List<String> mRestrictedList = new ArrayList<String>();

	/**
	 * Empty constructor
	 */
	public FeedItem() {	
		ApplicationMNM.addLogCat(TAG);		
		mItemData = new HashMap<String, String>();
	}
    
	/**
	 * Will acquire internal semaphore
	 * @throws InterruptedException
	 */
	private void acquireSemaphore() throws InterruptedException	{
		mSemaphore.acquire();
	}
	
	/**
	 * release internal semaphore
	 */
	private void releaseSemaphore()	{
		mSemaphore.release();
	}
	
	/**
	 * Looks if this is a valid key or not
	 * @param key
	 * @return
	 */
	public boolean isKeyValid( String key ) {
		return isKeyPermitted(key) && !isKeyRestricted(key);
	}
	
	/**
	 * Decides if a key can be added to the feed item, by default returns true
	 * @param key
	 * @return
	 */
	protected boolean isKeyPermitted( String key ) {
		return mPermittedList.contains(key);
	}
	
	/**
	 * Is this a restricted key?
	 * @param key
	 * @return
	 */
	protected boolean isKeyRestricted( String key ) {
		return mRestrictedList.contains(key);
	}
	
	/**
	 * Looks if the key should be a list or not
	 * @param key
	 * @return
	 */
	protected boolean isKeyListValue( String key ) {
		return false;
	}
	
	/**
	 * Will set the object value for a key.
	 * @param key
	 * @param value
	 * @return true if added and valid or false if not valid or adding failed
	 */
	public boolean setValue( String key, Object value ) {
		try {
			return (isKeyListValue(key))?setListItemValue(key, (String) value):setStringValue(key, (String) value);
		} catch ( Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * tranform the data from a raw value into a valid value
	 * @param key
	 * @param rawValue
	 * @return
	 */
	protected String tranformRAWValue( String key, String rawValue ) {
		String value = rawValue;
		return value;		
	}
	
	/**
	 * tranform the data from a raw value into a valid value
	 * @param key
	 * @param rawValue
	 * @return
	 */
	protected String tranformRAWListValue( String key, String rawValue ) {
		String value = rawValue;
		return value;		
	}
	
	/**
	 * Set a string value
	 * @param key
	 * @param value
	 * @return true/false
	 */
	protected boolean setStringValue( String key, String value ) {
		boolean bResult;
		String finalValue = value;
		bResult = false;		
		try {
			// Make us tread safe!
			acquireSemaphore();
			ApplicationMNM.logCat(TAG,"setStringValue::("+ key +") value("+ value +")");
			finalValue = tranformRAWValue(key,value);
			setKeyValue(key, finalValue);
			bResult = true;
		} catch( Exception e) {
			// fall thru and exit normally
			ApplicationMNM.warnCat(TAG,"(setStringValue) Can not set key("+ key +") value("+ finalValue +")");
			e.printStackTrace();
		} finally {
			// release our semaphore
			releaseSemaphore();
		}
		return bResult;
	}

	/**
	 * Add a value as an item of a list. A list is also just a string
	 * but each item is separated by a special character, in this case 
	 * LIST_SEPARATOR.
	 * @param key
	 * @param value
	 * @return true/false
	 */
	protected boolean setListItemValue( String key, String value ) {
		boolean bResult;
		String finalValue = value;
		bResult = false;		
		try {
			// Make us tread safe!
			acquireSemaphore();
			
			// Before we let any child class transform our value
			// we need to get rid of any LIST_SEPARATOR character
			// value could contain! It' a drawback but thats life
			// hehehe.
			value = value.replaceAll(LIST_SEPARATOR, "");
			
			// now apply any pre-add transformation
			finalValue = tranformRAWListValue(key,value);
			
			// Create or add a new item
			String itemList = "";
			if ( !containsKey(key) )
			{			
				// Add the new value
				itemList = finalValue;
			}
			else
			{
				itemList = getRawKeyData(key);
				itemList += LIST_SEPARATOR + finalValue;
			}
			
			// Update item
			if ( itemList != "" )
			{
				ApplicationMNM.logCat(TAG,"setListItemValue::("+ key +") value("+ finalValue +")");
				setKeyValue(key,itemList);
				bResult = true;
			}
		} catch( Exception e) {
			// fall thru and exit normally
			ApplicationMNM.warnCat(TAG,"(setListItemValue) Can not set key("+ key +") value("+ finalValue +")");
		} finally {
			// release our semaphore
			releaseSemaphore();
		}
		return bResult;
	}
	
	/**
	 * Add a new data set
	 * @param key
	 * @param value
	 */
	private void setKeyValue( String key, String value ) {
		mItemData.put(key, value);
	}
	
	public void removeKey( String key ) {
		mItemData.remove(key);
	}
	
	/**
	 * 
	 * @param key
	 * @return true/false if the key is included
	 */
	public boolean containsKey( String key ) {
		return mItemData.containsKey(key);
	}
	
	/**
	 * Will just return the raw data of a field, this means that if the
	 * field is a list type you will get a string and not the parsed list!
	 * @param key
	 * @return
	 */
	public String getRawKeyData( String key ) {
		String rawData = mItemData.get(key);
		if ( rawData != null )
		{
			return rawData;
		}
		return "";
	}
	
	/**
	 * Return the data value object for a specific key
	 * @param key
	 * @return value
	 */
	public Object getKeyData( String key ) {
		String rawData = mItemData.get(key);
		if ( rawData != null )
		{
			// Get the raw value or parse it into a
			if ( !isKeyListValue(key) )
			{
				return rawData;
			}
			return new ArrayList<String>(Arrays.asList(rawData.trim().split(LIST_SEPARATOR)));
		}
		if ( !isKeyListValue(key) )
		{
			return "";
		}
		return new ArrayList<String>();
	}
	
	/**
	 * Return the map of data
	 * @return Map of Key/Value
	 */
	public Map<String, String> getAllData() {		
		return mItemData;
	}
	
	/**
	 * Returns the number of data fields this item has
	 * @return
	 */
	public int size() {
		return mItemData.size();
	}
}
