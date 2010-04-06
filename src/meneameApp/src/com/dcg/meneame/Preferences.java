package com.dcg.meneame;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Our preference activity
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Add prefs from xml
		addPreferencesFromResource(R.xml.preferences);
	}
	
	public void  onContentChanged()
	{
		super.onContentChanged();
	}

}
