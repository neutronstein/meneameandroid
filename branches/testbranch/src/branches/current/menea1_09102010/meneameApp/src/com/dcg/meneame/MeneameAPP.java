package com.dcg.meneame;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

import com.dcg.app.ApplicationMNM;
import com.dcg.dialog.VersionChangesDialog;

/**
 * Main activity, basically holds the main tab widget
 * 
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class MeneameAPP extends TabActivity {

	/** Class tag used for it's logs */
	private static final String TAG = "MeneameAPP";

	/** Main app TabHost */
	private TabHost mTabHost;

	/** Main animation */
	private Animation mMainAnimation = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ApplicationMNM.addLogCat(TAG);
		ApplicationMNM.logCat(TAG, "onCreate()");

		setContentView(R.layout.main);

		// Get some global stuff
		mTabHost = getTabHost();

		XmlResourceParser[]   parser     = new XmlResourceParser[3];
		 ColorStateList      text       = null;
	
		 Drawable[] background  = new StateListDrawable[3];
		 int[]               selected   = {},
		                     unselected = {0};
		 int               selectedColor = Color.GRAY,
		                     defaultColor  = Color.DKGRAY;
		
		// Load the colour lists.
		
		 try {
			parser[0] = getResources().getXml(R.color.color_state_definition_tab);
			text   = ColorStateList.createFromXml(getResources(), parser[0]);
			parser[0] = getResources().getXml(R.drawable.tab_indicator);
			parser[1] = getResources().getXml(R.drawable.tab_indicator);
			parser[2] = getResources().getXml(R.drawable.tab_indicator);
			background[0]   = StateListDrawable.createFromXml(getResources(), parser[0]);
			background[1]   = StateListDrawable.createFromXml(getResources(), parser[1]);
			background[2]   = StateListDrawable.createFromXml(getResources(), parser[2]);
		} catch (XmlPullParserException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		// Add news tab
		TabSpec newsTab = mTabHost.newTabSpec(NewsActivity
				.static_getTabActivityTag());
		newsTab.setContent(new Intent(this, NewsActivity.class));
		
		
		TextView newsTabView = new TextView(this);
		newsTabView.setText(getResources().getString(
		NewsActivity.static_getIndicatorStringID()));
		newsTabView.setTextSize(15.0f);		
		newsTabView.setTextColor(text);
		newsTabView.setBackgroundDrawable(background[0]);
		newsTab.setIndicator(newsTabView);
		mTabHost.addTab(newsTab);
	
		// Add queue tab
		TabSpec queueTab = mTabHost.newTabSpec(QueueActivity
				.static_getTabActivityTag());
		queueTab.setContent(new Intent(this, QueueActivity.class));

		
		
		TextView queueTabView = new TextView(this);
		queueTabView.setText(getResources().getString(
				QueueActivity.static_getIndicatorStringID()));
		queueTabView.setTextSize(15.0f);		
		queueTabView.setTextColor(text);
		queueTabView.setBackgroundDrawable(background[1]);
		queueTab.setIndicator(queueTabView);
		mTabHost.addTab(queueTab);

		// Add comments tab
		TabSpec commentsTab = mTabHost.newTabSpec(CommentsActivity
				.static_getTabActivityTag());
		commentsTab.setContent(new Intent(this, CommentsActivity.class));
		
		TextView commentsTabView = new TextView(this);
		commentsTabView.setText(getResources().getString(
				CommentsActivity.static_getIndicatorStringID()));
		commentsTabView.setTextSize(15.0f);		
		commentsTabView.setTextColor(text);
		commentsTabView.setBackgroundDrawable(background[2]);
		commentsTab.setIndicator(commentsTabView);
		mTabHost.addTab(commentsTab);

		// Set news tab as visible one
		mTabHost.setCurrentTab(0);

		// Check version number and if we change the version show a nice dialog
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		int savedVersion = prefs.getInt("pref_app_version_number", 0);
		ApplicationMNM.logCat(TAG, "Current version: "
				+ ApplicationMNM.getVersionNumber() + " ("
				+ ApplicationMNM.getVersionLable() + ")");
		ApplicationMNM.logCat(TAG, "Saved version: " + savedVersion);
		// Did we made any update?
		if (ApplicationMNM.getVersionNumber() > savedVersion) {
			VersionChangesDialog versionDialog = new VersionChangesDialog(this);
			versionDialog.show();

			// Save the version
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("pref_app_version_number", ApplicationMNM
					.getVersionNumber());

			// Don't forget to commit your edits!!!
			editor.commit();
		}
		// Did we got a crash last time?
		else if (!hasExitedSuccessfully()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.crash_report_question).setCancelable(
					false).setPositiveButton(R.string.generic_yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							sendCrashReport();
						}
					}).setNegativeButton(R.string.generic_no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
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
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		ApplicationMNM.logCat(TAG, "onDestroy()");
		ApplicationMNM.clearCachedContext();

		// Before we destroy the app we need to save our system value to
		// say that we closed the app properly!
		setExitSuccessfull();

		// Destroy app
		super.onDestroy();
	}

	/** Marks tghe exit as ok */
	private void setExitSuccessfull() {
		if (ApplicationMNM.mbAllowCrashReport) {

		}
	}

	/** Looks if we exited successfully the app last time */
	public boolean hasExitedSuccessfully() {
		if (ApplicationMNM.mbAllowCrashReport) {
			boolean bResult = false;

			return bResult;
		}
		return true;
	}

	/** Sends a crash report to us */
	public void sendCrashReport() {
		// TODO: Send the log file to us!
	}

	/** Refresh the animation we will use for the tab page */
	private void initAnim() {
		mMainAnimation = null;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String mainAnim = prefs.getString("pref_style_mainanimation", "None");
		if (mainAnim.compareTo("Fade-in") == 0) {
			mMainAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
		} else if (mainAnim.compareTo("Slide-in") == 0) {
			mMainAnimation = AnimationUtils.loadAnimation(this,
					R.anim.slide_bottom);
		}
	}

	@Override
	protected void onStop() {
		ApplicationMNM.logCat(TAG, "onStop()");

		super.onStop();
	}

	/** After the activity get's visible to the user */
	@Override
	protected void onResume() {
		super.onResume();
		ApplicationMNM.logCat(TAG, "onStop()");
		ApplicationMNM.setCachedContext(getBaseContext());

		// Start animation stuff
		initAnim();

		if (mMainAnimation != null) {
			mTabHost.startAnimation(mMainAnimation);
		}
	}
}