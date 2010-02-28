package com.dcg.meneame;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class RSSParser extends DefaultHandler {
	
	/** log tag for this class */
	private static final String TAG = "BaseRSSWorkerThread";
	
	/** The stream from where to get our data */
	private InputStreamReader mInputStreamReader = null;
	
	/** Our feed object where we store our parsed data */
	private Feed mFeed = null;
	
	/** Current feed item  */
	private FeedItem mFeedItem = null;
	
	/** class name used to create a new feed item */
	protected String mFeedItemClassName;
	
	/** Text we are parsing right now */
	private StringBuilder mText = new StringBuilder();
	
	/** number of items we want to parse as max */
	private int mMaxItems;
	
	/** Count items */
	private int mItemCount;
	
	/** RSS worker thread that invoked us */
	private BaseRSSWorkerThread mParentThread;
	
	/** If true we are parsing channel data and we have not reached any item */
	private boolean mbParsingChannel;
	
	/** Current tag we are in */
	private String mCurrentTag;
	
	/**
	 * Create a RSSParser passing along a RSS RawData
	 * @param RawFeed
	 */
	public RSSParser()
    {
        this.mText = new StringBuilder();
        this.mFeed = new Feed();
        this.mItemCount = 0;
        
        // Set our default feed item class name, sublcasses can change it at will
        this.mFeedItemClassName = "com.dcg.meneame.ArticleFeedItem";
    }
	
	/**
	 * Set the RSS InputStream
	 */
	public void setInputStream( InputStreamReader inputStreamReader ) {
		this.mInputStreamReader = inputStreamReader;
	}
	
	/**
	 * Register a worker thread for this parse
	 * @param mParentThread
	 */
	public void setWorkerThread( BaseRSSWorkerThread mParentThread ) {
		this.mParentThread = mParentThread;
	}
	
	/**
	 * Set max items we will parse
	 * @param mMaxItems
	 */
	public void setMaxItems( int mMaxItems ) {
		this.mMaxItems = mMaxItems;
	}
	
	/**
	 * Return processed feed
	 * @return
	 */
	public Feed getFeed() {
		return mFeed;
	}
	
	/**
	 * Set error in worker thread
	 */
	public void setErrorMessage( String error ) {
		if ( mParentThread != null )
		{
			mParentThread.setErrorMessage(error);
		}
	}
	
	/**
	 * Creates the feed item to be used by the parser
	 */
	private void createFeedItem()
	{
		// Try to create the RSS handler
		try {
			this.mFeedItem = (FeedItem)Class.forName(this.mFeedItemClassName).newInstance();
			Log.d(TAG, "FeedItem created: " + this.mFeedItem.toString());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrorMessage(e.toString());
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrorMessage(e.toString());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrorMessage(e.toString());
		}
	}
	
	/**
	 * Sets new data to the current feed item
	 * @param key
	 * @param value
	 */
	private boolean setValue( String key, Object value )
	{
		if ( this.mFeedItem != null )
		{
			return this.mFeedItem.setValue(key, value);
		}
		return false;
	}
	
	/**
	 * Start RSS parsing
	 */
	public void parse() {
		SAXParserFactory spf = null;
		SAXParser sp = null;
		
		try {
			
			spf = SAXParserFactory.newInstance();
			if (spf != null)
			{
				// Create the fist feed item we will use
				createFeedItem();
				
				sp = spf.newSAXParser();				
				sp.parse( new InputSource(this.mInputStreamReader), this);
				
				// We should add the last article to the feed
				_addArticle();
			}
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrorMessage(e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrorMessage(e.toString());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrorMessage(e.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setErrorMessage(e.toString());
		} finally {
			// Nothing
		}
	}
	
	/**
	 * Called while we read the input stream and parse all items
	 */
	public void characters(char[] ch, int start, int length) {
		this.mText.append(ch, start, length);
    }
	
	/** Adds the current article to the feed and clears the refernce 
	 * @return */
	private void _addArticle() {
		if ( this.mFeedItem != null )
			this.mFeed.addItem(this.mFeedItem);
		this.mFeedItem = null;
	}
	
	/**
	 * [XML-PARSING] We found a start element
	 */
	public void startElement(String uri, String name, String qName, Attributes atts) {
		if ( name.length() > 0 )
		{
			//Log.d(TAG, "[START] localName: " + name.toString());
			
			// Set some global states
			if (name.trim().equals("channel")) 
			{
				Log.d(TAG, "Getting channel data...");
				this.mbParsingChannel = true;
			}
			else if (name.trim().equals("item")) 
			{
				mItemCount++;
				Log.d(TAG, "Item Found ["+this.mItemCount+"]");
				this.mbParsingChannel = false;
				
				// Add previus article
				_addArticle();
				
				// Create new article to be hold
				createFeedItem();
			}
			
			// Register current tag
			mCurrentTag = name;
		}
		
		// Reset string builder
		this.mText.setLength(0);
	}
	
	/**
	 * [XML-PARSING] We found an end element. Will fillup the item with data.
	 */
	public void endElement(String uri, String name, String qName) {
		if ( name.length() > 0 )
		{
			if ( this.mbParsingChannel )
			{
				if ( mCurrentTag.trim().equals("pubDate") )
				{
					this.mFeed.mPubDate = mText.toString();
				}
			}
			else
			{				
				if ( setValue(mCurrentTag.trim(), mText.toString()) )
				{
					Log.d(TAG, " - " + mCurrentTag + ": " + mText.toString());
				}
			}
		}
	}
}
