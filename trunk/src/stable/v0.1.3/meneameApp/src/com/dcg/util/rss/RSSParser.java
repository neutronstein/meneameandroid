package com.dcg.util.rss;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.dcg.app.ApplicationMNM;

/**
 * Class that does the parsing of an RSS file
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
abstract public class RSSParser extends DefaultHandler {
	
	/** log tag for this class */
	private static final String TAG = "RSSParser";
	
	/** The stream from where to get our data */
	private InputStreamReader mInputStreamReader = null;
	
	/** Our feed object where we store our parsed data */
	private Feed mFeed = null;
	
	/** Current feed item  */
	private FeedItem mFeedItem = null;
	
	/** class name used to create a new feed item */
	private String mFeedItemClassName;
	
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
	
	/** did we received a stop request? */
	private boolean mbStopRequested;
	
	/**
	 * Create a RSSParser passing along a RSS RawData
	 * @param RawFeed
	 */
	public RSSParser()
    {
        this.mText = new StringBuilder();
        this.mFeed = new Feed();
        this.mItemCount = 0;
        
        // Add our tag to the category log (so it will be printed out)
        ApplicationMNM.addLogCat(TAG);
    }
	
	/**
	 * Request this thread to stop what it's doing
	 */
	public void requestStop() {
		mbStopRequested = true;
	}
	
	public String getmFeedItemClassName() {
		return mFeedItemClassName;
	}

	public void setmFeedItemClassName(String mFeedItemClassName) {
		this.mFeedItemClassName = mFeedItemClassName;
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
	public void setError( int errorID ) {
		if ( mParentThread != null )
		{
			mParentThread.setError(errorID);
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
			ApplicationMNM.logCat(TAG, "FeedItem created: " + this.mFeedItem.toString());
		} catch (IllegalAccessException e) {
			ApplicationMNM.warnCat(TAG, "Failed to create feed item: "+e.toString());
			setError(BaseRSSWorkerThread.ERROR_CREATE_FEEDITEM_ACCESS);
		} catch (InstantiationException e) {
			ApplicationMNM.warnCat(TAG, "Failed to create feed item: "+e.toString());
			setError(BaseRSSWorkerThread.ERROR_CREATE_FEEDITEM_INSTANCE);
		} catch (ClassNotFoundException e) {
			ApplicationMNM.warnCat(TAG, "Failed to create feed item: "+e.toString());
			setError(BaseRSSWorkerThread.ERROR_CREATE_FEEDITEM_CLASS_NOT_FOUND);
		}
	}
	
	/**
	 * Sets new data to the current feed item
	 * @param key
	 * @param value
	 */
	private boolean setItemValue( String key, Object value )
	{
		if ( this.mFeedItem != null )
		{
			return this.mFeedItem.setValue(key, value);
		}
		return false;
	}
	
	/**
	 * Sets new data to the current feed
	 * @param key
	 * @param value
	 */
	private boolean setFeedValue( String key, Object value )
	{
		if ( this.mFeed != null )
		{
			return this.mFeed.setValue(key, value);
		}
		return false;
	}
	
	/**
	 * This must be called after we parse and got the needed data,
	 * so we avoid any memory leak we could get
	 */
	public void clearReferences()
	{
		this.mInputStreamReader = null;		
		this.mFeed = null;
		this.mFeedItem = null;
		this.mText = null;
		this.mParentThread =null;
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
			}
			
			// This is all right, so get rid of the current feed item
			this.mFeedItem = null;
		} catch (RSSParserMaxElementsException e) {
			// Not a real 'error' heheh
			ApplicationMNM.logCat(TAG, "Finished: " + e.toString());
		} catch (RSSParserStopRequestException e) {
			// Not a real 'error' heheh
			ApplicationMNM.logCat(TAG, "Finished: " + e.toString());
		} catch (SAXException e) {
			setError(BaseRSSWorkerThread.ERROR_RSS_SAX);
		} catch (IOException e) {
			setError(BaseRSSWorkerThread.ERROR_RSS_IO_EXCEPTION);
		} catch (ParserConfigurationException e) {
			setError(BaseRSSWorkerThread.ERROR_RSS_PARSE_CONFIG);
		} catch (Exception e) {
			setError(BaseRSSWorkerThread.ERROR_RSS_UNKOWN);
		} finally {
			// We should add the last article to the feed (if it exists)
			_addArticle();
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
			this.mFeed.addArticle(this.mFeedItem);
		this.mFeedItem = null;
	}
	
	/**
	 * [XML-PARSING] We found a start element
	 */
	public void startElement(String uri, String name, String qName, Attributes atts) throws RSSParserMaxElementsException, RSSParserStopRequestException {
		if ( name.length() > 0 )
		{
			//ApplicationMNM.LogCat(TAG, "[START] localName: " + name.toString());
			
			// Set some global states
			if (name.trim().equals("channel")) 
			{
				ApplicationMNM.logCat(TAG, "Getting channel data...");
				this.mbParsingChannel = true;
			}
			else if (name.trim().equals("item")) 
			{
				this.mItemCount++;
				ApplicationMNM.logCat(TAG, "Item Found ["+this.mItemCount+"]");
				this.mbParsingChannel = false;
				
				// Check if we reached the max articles permitted
				// NOTE: The last article will be added by the parser 'always!'
				if ( this.mMaxItems > 0 && this.mItemCount >= this.mMaxItems )
				{
					throw new RSSParserMaxElementsException("MAX ELEMENTS REACHED: " + mItemCount, null);
				}
				
				// Did we got a stop request?
				if ( mbStopRequested )
				{
					throw new RSSParserStopRequestException("Received stop request from parent thread!", null);
				}
				
				// Add previus article in case we got a previus one
				if ( this.mItemCount > 1 )
				{
					_addArticle();
				}
				
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
				if ( setFeedValue(mCurrentTag.trim(), mText.toString()) )
				{
					ApplicationMNM.logCat(TAG, " [feed] " + mCurrentTag + ": " + mText.toString());
				}
			}
			else
			{				
				if ( setItemValue(mCurrentTag.trim(), mText.toString()) )
				{
					ApplicationMNM.logCat(TAG, " - " + mCurrentTag + ": " + mText.toString());
				}
			}
		}
	}
}
