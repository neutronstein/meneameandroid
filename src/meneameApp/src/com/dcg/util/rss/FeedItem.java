package com.dcg.util.rss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import com.dcg.app.ApplicationMNM;

/**
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 *
 */
abstract public class FeedItem extends Object {
	
	/** log tag for this class */
	private static final String TAG = "FeedItem";
	
	/** Our map that holds the internal data */
	private Map<String, Object> mItemData = new HashMap<String, Object>();;
	
	/** our semaphore to make this class thread safe! */
	private Semaphore mSemaphore = new Semaphore(1);

	/**
	 * Empty constructor
	 */
	public FeedItem() {
		super();
		
		ApplicationMNM.AddLogCat(TAG);
	}
	
	/**
	 * Will acquire internal semaphore
	 * @throws InterruptedException
	 */
	private void acquireSemaphore() throws InterruptedException
	{
		mSemaphore.acquire();
	}
	
	/**
	 * release internal semaphore
	 */
	private void releaseSemaphore()
	{
		mSemaphore.release();
	}
	
	/**
	 * Looks if this is a valid key or not
	 * @param key
	 * @return
	 */
	public boolean isKeyValid( String key )
	{
		return isKeyPermitted(key) && !isKeyRestricted(key);
	}
	
	/**
	 * Decides if a key can be added to the feed item, by default returns true
	 * @param key
	 * @return
	 */
	protected boolean isKeyPermitted( String key )
	{
		return true;
	}
	
	/**
	 * Is this a restricted key?
	 * @param key
	 * @return
	 */
	protected boolean isKeyRestricted( String key )
	{
		return false;
	}
	
	/**
	 * Looks if the key should be a list or not
	 * @param key
	 * @return
	 */
	protected boolean isKeyListValue( String key )
	{
		return false;
	}
	
	/**
	 * Will set the object value for a key.
	 * @param key
	 * @param value
	 * @return true if added and valid or false if not valid or adding failed
	 */
	public boolean setValue( String key, Object value )
	{
		if ( !isKeyValid(key) ) return false;
		try {
			return (isKeyListValue(key))?setListItemValue(key, (String) value):setStringValue(key, (String) value);
		} catch ( Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Set a string value
	 * @param key
	 * @param value
	 * @return true/false
	 */
	protected boolean setStringValue( String key, String value )
	{
		boolean bResult;		
		bResult = false;		
		try {
			// Make us tread safe!
			acquireSemaphore();
			ApplicationMNM.LogCat(TAG,"setStringValue::("+ key +") value("+ value +")");
			setKeyValue(key, value);
			bResult = true;
		} catch( Exception e) {
			// fall thru and exit normally
			ApplicationMNM.LogCat(TAG,"(setStringValue) Can not set key("+ key +") value("+ value +")");
		} finally {
			// release our semaphore
			releaseSemaphore();
		}
		return bResult;
	}

	/**
	 * Add a value as an item of a list
	 * @param key
	 * @param value
	 * @return true/false
	 */
	@SuppressWarnings("unchecked")
	protected boolean setListItemValue( String key, String value )
	{
		boolean bResult;		
		bResult = false;		
		try {
			// Make us tread safe!
			acquireSemaphore();
			
			// Create or add a new item
			List<String> itemList = null;
			if ( !containsKey(key) )
			{			
				// No item there so build the list item
				itemList = new ArrayList<String>();
				
				// Add the new value
				itemList.add(value);
			}
			else
			{
				Object rawValue = getKeyData(key);
				itemList = (rawValue != null)?(List<String>) rawValue:null;
				if ( itemList != null )
				{
					itemList.add(value);
				}
			}
			
			// Update item
			if ( itemList != null )
			{
				ApplicationMNM.LogCat(TAG,"setListItemValue::("+ key +") value("+ value +")");
				setKeyValue(key,itemList);
				bResult = true;
			}
		} catch( Exception e) {
			// fall thru and exit normally
			ApplicationMNM.LogCat(TAG,"(setListItemValue) Can not set key("+ key +") value("+ value +")");
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
	private void setKeyValue( String key, Object value ) {
		mItemData.put(key, value);
	}
	
	public void removeKey( String key )
	{
		mItemData.remove(key);
	}
	
	/**
	 * 
	 * @param key
	 * @return true/false if the key is included
	 */
	public boolean containsKey( String key )
	{
		return mItemData.containsKey(key);
	}
	
	/**
	 * Return the data value object for a specifc key
	 * @param key
	 * @return value
	 */
	public Object getKeyData( String key )
	{
		return mItemData.get(key);
	}
	
	/**
	 * Return the map of data
	 * @return Map of Key/Value
	 */
	public Map<String, Object> getAllData()
	{		
		return mItemData;
	}
}
