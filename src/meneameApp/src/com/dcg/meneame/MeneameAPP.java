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
public class MeneameAPP extends TabActivity  {
	
	/** Class tag used for it's logs */
	private static final String TAG = "MeneameAPP";
	
	/** Main app TabHost*/
	private TabHost mTabHost;
    
    /** Main animation */
    private Animation mMainAnimation = null;
    
    /** Database helper */
    private MeneameDbAdapter mDBHelper = null;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ApplicationMNM.addLogCat(TAG);        
        ApplicationMNM.logCat(TAG, "onCreate()");
        
        setContentView(R.layout.main);
        
        // Create databse helper
		mDBHelper = new MeneameDbAdapter(this);

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
        
        // Check version number and if we change the version show a nice dialog
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
        int savedVersion = prefs.getInt("pref_app_version_number", 0);
        ApplicationMNM.logCat(TAG, ""+savedVersion);
        if ( ApplicationMNM.getVersionNumber() > savedVersion )
        {
        	VersionChangesDialog versionDialog = new VersionChangesDialog(this);
        	versionDialog.show();
        	
        	// Save the version
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());  
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("pref_app_version_number", ApplicationMNM.getVersionNumber());

            // Don't forget to commit your edits!!!
            editor.commit();
        }
    }
    
    @Override
	protected void onStart() {
		ApplicationMNM.logCat(TAG, "onStart()");
		super.onStart();
	}
	
	@Override
	protected void onRestart() {
		ApplicationMNM.logCat(TAG, "onRestart()");
		super.onRestart();
	}
	
	@Override
	protected void onPause() {
		ApplicationMNM.logCat(TAG, "onPause()");
		
		// Close database
		mDBHelper.close();
		ApplicationMNM.setDBHelper(null);
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		ApplicationMNM.logCat(TAG, "onDestroy()");
		ApplicationMNM.clearCachedContext();
		super.onDestroy();
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
    	ApplicationMNM.logCat(TAG, "onStop()");
    	super.onStop();    	
    }
    
    /** After the activity get's visible to the user */
    protected void onResume() {
    	super.onResume();
    	ApplicationMNM.logCat(TAG, "onStop()");
    	ApplicationMNM.setCachedContext(getBaseContext());
    	
    	// Open db connection
    	mDBHelper.open();
		ApplicationMNM.setDBHelper(mDBHelper);
    	
    	// Start animation stuff
    	initAnim();
        
    	if ( mMainAnimation != null )
    	{
    		mTabHost.startAnimation(mMainAnimation);
    	}
    }
}