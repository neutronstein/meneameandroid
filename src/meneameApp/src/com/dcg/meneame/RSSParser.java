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
	
	/** Global Application */
	private ApplicationMNM mApp = null;
	
	/** Text we are parsing right now */
	private StringBuilder mText = new StringBuilder();
	
	/** number of items we want to parse as max */
	private int mMaxItems;
	
	/** RSS worker thread that invoked us */
	private BaseRSSWorkerThread mParentThread;
	
	/**
	 * Create a RSSParser passing along a RSS RawData
	 * @param RawFeed
	 */
	public RSSParser()
    {
        this.mText = new StringBuilder();
        this.mFeed = new Feed();
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
	 * Start RSS parsing
	 */
	public void parse() {
		SAXParserFactory spf = null;
		SAXParser sp = null;
		
		try {
		
			spf = SAXParserFactory.newInstance();
			if (spf != null)
			{
				sp = spf.newSAXParser();				
				sp.parse( new InputSource(this.mInputStreamReader), this);				
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
		}
	}
	
	/**
	 * Called while we read the input stream and parse all items
	 */
	public void characters(char[] ch, int start, int length) {
		this.mText.append(ch, start, length);
    }
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if ( localName.length() > 0 )
		{		
			Log.d(TAG, "[RSS] startElement -----------------------------------");
			Log.d(TAG, "URI: " + uri.toString());
			Log.d(TAG, "localName: " + localName.toString());
			Log.d(TAG, "qName: " + qName.toString());
			Log.d(TAG, "body: " + this.mText.toString());
			
			Log.d(TAG, "attributes:");
			int numAttribs = attributes.getLength();
			for ( int i = 0; i < numAttribs; i++ )
			{
				Log.d(TAG, " [" + i + "] " + attributes.getQName(i));
			}
		}
		
		// Reset string builder
		this.mText.setLength(0);
	}
	
	public void endElement(String uri, String localName, String qName) {
		if ( localName.length() <= 0) return;
		
		Log.d(TAG, "[RSS] endElement -----------------------------------");
		Log.d(TAG, "URI: " + uri.toString());
		Log.d(TAG, "localName: " + localName.toString());
		Log.d(TAG, "qName: " + qName.toString());
	}
}
