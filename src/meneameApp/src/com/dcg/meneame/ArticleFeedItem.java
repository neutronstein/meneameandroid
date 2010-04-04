package com.dcg.meneame;

import com.dcg.util.rss.FeedItem;

public class ArticleFeedItem extends FeedItem {
	
	public ArticleFeedItem()
	{
		super();
		
		mPermittedList.add("title");
		mPermittedList.add("description");
		mPermittedList.add("votes");
		mPermittedList.add("url");
		mPermittedList.add("category");
		mPermittedList.add("link");
	}
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
