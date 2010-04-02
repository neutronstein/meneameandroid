package com.dcg.meneame;

import com.dcg.util.rss.FeedItem;

public class ArticleFeedItem extends FeedItem {
	
	/**
	 * tranform the data from a raw value into a valid value
	 * @param key
	 * @param rawValue
	 * @return
	 */
	protected String tranformRAWValue( String key, String rawValue )
	{
		String value = super.tranformRAWValue(key,rawValue);
		if( key.equalsIgnoreCase("description") )
		{
			int startIdx = rawValue.indexOf("<p>")+3;
			int endIdx = rawValue.indexOf("</p>");
			value = value.substring(startIdx, endIdx);
		}
		return value;		
	}
	
	/**
	 * tranform the data from a raw value into a valid value
	 * @param key
	 * @param rawValue
	 * @return
	 */
	protected String tranformRAWListValue( String key, String rawValue )
	{
		String value = super.tranformRAWListValue(key,rawValue);
		return value;		
	}
	
	/**
	 * Decides if a key can be added to the feed item, by default returns true
	 * @param key
	 * @return
	 */
	protected boolean isKeyPermitted( String key )
	{
		return super.isKeyPermitted(key);
	}
	
	/**
	 * Is this a restricted key?
	 * @param key
	 * @return
	 */
	protected boolean isKeyRestricted( String key )
	{
		boolean bResult = false;
		if ( key.trim().equals("thumbnail") )
		{
			bResult = true;
		}
		return bResult || super.isKeyRestricted(key);
	}
	
	/**
	 * Looks if the key should be a list or not
	 * @param key
	 * @return
	 */
	protected boolean isKeyListValue( String key )
	{
		if ( key.trim().equals("category") )
		{
			return true;
		}
		return super.isKeyListValue(key);
	}
}
