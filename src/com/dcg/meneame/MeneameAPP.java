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

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
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

	// Get some global stuff

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ApplicationMNM.addLogCat(TAG);
		ApplicationMNM.logCat(TAG, "onCreate()");

		this.createContent();

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

	private void createContent() {
		if (isTiny())
			setContentView(R.layout.main_tiny);
		else
			setContentView(R.layout.main);

		mTabHost = getTabHost();

		// Load the colour lists.
		XmlResourceParser[] parser = new XmlResourceParser[3];
		ColorStateList text = null;

		Drawable[] background = new StateListDrawable[3];

		this.createTabs(mTabHost, parser, text, background, 15.0f);

		// Set news tab as visible one
		mTabHost.setCurrentTab(0);
	}

	private void createTabs(TabHost mTabHost, XmlResourceParser[] parser,
			ColorStateList text, Drawable[] background, float textSize) {
		try {
			parser[0] = getResources().getXml(
					R.color.color_state_definition_tab);
			text = ColorStateList.createFromXml(getResources(), parser[0]);
			parser[0] = getResources().getXml(R.drawable.tab_indicator);
			parser[1] = getResources().getXml(R.drawable.tab_indicator);
			parser[2] = getResources().getXml(R.drawable.tab_indicator);
			background[0] = StateListDrawable.createFromXml(getResources(),
					parser[0]);
			background[1] = StateListDrawable.createFromXml(getResources(),
					parser[1]);
			background[2] = StateListDrawable.createFromXml(getResources(),
					parser[2]);
		} catch (XmlPullParserException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		// Add news tab

		TabSpec newsTab = mTabHost.newTabSpec(NewsActivity
				.static_getTabActivityTag());

		configureTab(text, background[0], textSize, newsTab,
				NewsActivity.class, getResources().getString(
						NewsActivity.static_getIndicatorStringID()));

		mTabHost.addTab(newsTab);

		// Add queue tab
		TabSpec queueTab = mTabHost.newTabSpec(QueueActivity
				.static_getTabActivityTag());

		configureTab(text, background[1], textSize, queueTab,
				QueueActivity.class, getResources().getString(
						QueueActivity.static_getIndicatorStringID()));

		mTabHost.addTab(queueTab);

		// Add comments tab
		TabSpec commentsTab = mTabHost.newTabSpec(CommentsActivity
				.static_getTabActivityTag());

		configureTab(text, background[2], textSize, commentsTab,
				CommentsActivity.class, getResources().getString(
						CommentsActivity.static_getIndicatorStringID()));
		mTabHost.addTab(commentsTab);
	}

	private void configureTab(ColorStateList text, Drawable background,
			float textSize, TabSpec tab, Class<? extends FeedActivity> clazz,
			String indicatorStringId) {
		tab.setContent(new Intent(this, clazz));
		TextView textView = new TextView(this);

		textView.setText(indicatorStringId);
		textView.setTextSize(textSize);
		textView.setTextColor(text);
		textView.setBackgroundDrawable(background);
		tab.setIndicator(textView);
	}

	@Override
	protected void onStart() {
		ApplicationMNM.logCat(TAG, "onStart()");

		super.onStart();
	}

	@Override
	protected void onRestart() {
		ApplicationMNM.logCat(TAG, "onRestart()");
		mTabHost.clearAllTabs();
		this.createContent();
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

	/**
	 * Refresh the animation we will use
	 * 
	 * 
	 * 
	 * 
	 * for the tab page
	 */
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

	private boolean isTiny() {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String value = prefs.getString("pref_style_size", "Default");
		return value.compareTo("Tiny") == 0;

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
		ApplicationMNM.logCat(TAG, "onResume()");
		ApplicationMNM.setCachedContext(getBaseContext());

		// Start animation stuff
		initAnim();

		if (mMainAnimation != null) {
			mTabHost.startAnimation(mMainAnimation);
		}
	}
}