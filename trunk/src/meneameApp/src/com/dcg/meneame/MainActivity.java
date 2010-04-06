package com.dcg.meneame;

import com.dcg.app.ApplicationMNM;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

/**
 * Main activity, basically holds the main tab widget
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class MainActivity extends TabActivity  {
	
	/** Class tag used for it's logs */
	private static final String TAG = "MeneameMainActivity";
	
	/** Main app TabHost*/
	private TabHost mTabHost;
    
    /** Main animation */
    private Animation mMainAnimation = null;
    
    /** Global Application */
	private ApplicationMNM mApp = null;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ApplicationMNM.addLogCat(TAG);
        
        ApplicationMNM.logCat(TAG, "Starting...");
        
        setContentView(R.layout.main);
        
        // Cache app
		try {
			mApp = (ApplicationMNM)getApplication();
		} catch(Exception e){
			e.printStackTrace();
		}
        
        // Get some global stuff
        mTabHost = getTabHost();
        
        // Add news tab
        TabSpec newsTab = mTabHost.newTabSpec(NewsActivity.static_getTabActivityTag()); 
        newsTab.setContent(new Intent(this, NewsActivity.class)); 
        newsTab.setIndicator( getResources().getString(NewsActivity.static_getIndicatorStringID()) ); 
        mTabHost.addTab(newsTab);
        
        // Add queue tab
        TabSpec queueTab = mTabHost.newTabSpec(QueueActivity.static_getTabActivityTag()); 
        queueTab.setContent(new Intent(this, QueueActivity.class)); 
        queueTab.setIndicator( getResources().getString(QueueActivity.static_getIndicatorStringID()) ); 
        mTabHost.addTab(queueTab);

        
        // Add comments tab
        TabSpec commentsTab = mTabHost.newTabSpec(CommentsActivity.static_getTabActivityTag()); 
        commentsTab.setContent(new Intent(this, CommentsActivity.class)); 
        commentsTab.setIndicator( getResources().getString(CommentsActivity.static_getIndicatorStringID()) ); 
        mTabHost.addTab(commentsTab);
        
        // Set news tab as visible one
        mTabHost.setCurrentTab(0);
    }
    
    /** Refresh the animation we will use for the tab page */
    private void initAnim() {
    	mMainAnimation = null;
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
        String mainAnim = prefs.getString("pref_style_mainanimation", "None");
        if ( mainAnim.compareTo("Fade-in") == 0 )
        {
        	mMainAnimation = AnimationUtils.loadAnimation( this, R.anim.fadein );
        }
        else if ( mainAnim.compareTo("Slide-in") == 0 )
        {
        	mMainAnimation = AnimationUtils.loadAnimation( this, R.anim.slide_bottom );
        }
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    /** After the activity get's visible to the user */
    protected void onResume() {
    	super.onResume();
    	
    	// Start animation stuff
    	initAnim();
        
    	if ( mMainAnimation != null )
    	{
    		mTabHost.startAnimation(mMainAnimation);
    	}
    }
}