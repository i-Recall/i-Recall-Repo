package com.example.nurhazim.i_recall;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by NurHazim on 05-Jan-15.
 */
public class SettingsActivity extends PreferenceActivity {
    public static final String KEY_PREF_BASE_URL = "pref_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_setting);
    }
}
