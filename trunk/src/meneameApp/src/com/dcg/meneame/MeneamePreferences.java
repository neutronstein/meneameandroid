package com.dcg.meneame;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MeneamePreferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Add prefs from xml
		addPreferencesFromResource(R.xml.preferences);
	}

}
