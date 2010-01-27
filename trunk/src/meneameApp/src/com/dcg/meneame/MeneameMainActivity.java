package com.dcg.meneame;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MeneameMainActivity extends TabActivity  {
	
	/** Main app TabHost*/
	private TabHost mTabHost;
	
	/** Refresh menu item id */
	private static final int MENU_REFRESH = 0;
	
	/** Nótame menu item id */
    private static final int MENU_NOTAME = 1;
    
    /** Settings menu item id */
    private static final int MENU_SETTINGS = 2;
    
    /** Main animation */
    private Animation mMainAnimation = null;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);

        mTabHost = getTabHost();
        
        // Todo, make custom tabs: http://ezmobile.wordpress.com/2009/02/02/customized-android-tabs/
        
        // Add news tab
        TabSpec newsTab = mTabHost.newTabSpec("last_news_tab"); 
        newsTab.setContent(new Intent(this, MeneameNewsActivity.class)); 
        newsTab.setIndicator( getResources().getString(R.string.main_tab_news) ); 
        mTabHost.addTab(newsTab);
        
        // Add queue tab
        TabSpec queueTab = mTabHost.newTabSpec("in_que_tab"); 
        queueTab.setContent(new Intent(this, MeneameQueueActivity.class)); 
        queueTab.setIndicator( getResources().getString(R.string.main_tab_queue) ); 
        mTabHost.addTab(queueTab);
        
        // Add comments tab
        TabSpec commentsTab = mTabHost.newTabSpec("comments_tab"); 
        commentsTab.setContent(new Intent(this, MeneameCommentsActivity.class)); 
        commentsTab.setIndicator( getResources().getString(R.string.main_tab_comments) ); 
        mTabHost.addTab(commentsTab);
        
        // Set news tab as visible one
        mTabHost.setCurrentTab(0);
    }
    
    /** Refreshs the animation we will use for the tab page */
    private void initAnim() {
    	// By default we do not use any animation
    	mMainAnimation = null;
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
        String mainAnim = prefs.getString("pref_app_mainanimation", "None");
        if ( mainAnim.compareTo("Fade-in") == 0 )
        {
        	mMainAnimation = AnimationUtils.loadAnimation( this, R.anim.fadein );
        }
        else if ( mainAnim.compareTo("Slide-in") == 0 )
        {
        	mMainAnimation = AnimationUtils.loadAnimation( this, R.anim.slide_bottom );
        }
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
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_REFRESH, 0, R.string.main_menu_refresh).setIcon(R.drawable.ic_menu_refresh);
        menu.add(0, MENU_NOTAME, 0, R.string.main_menu_notame).setIcon(android.R.drawable.ic_menu_send);
        menu.add(0, MENU_SETTINGS, 0, R.string.main_menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }
    
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_REFRESH:
            // Refresh currently selected tab content
            return true;
        case MENU_NOTAME:
            // Open Notame Activity
            return true;
        case MENU_SETTINGS:
            // Open settitngs screen
        	openSettingsScreen();
            return true;
        }
        return false;
    }
    
    /* Open settings screen */
    public void openSettingsScreen() {
    	Intent settingsActivity = new Intent( this, MeneamePreferences.class);
    	startActivity(settingsActivity);
    }
}