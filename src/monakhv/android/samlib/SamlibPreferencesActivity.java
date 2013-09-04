/*
 * Copyright 2013 Dmitry Monakhov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monakhv.android.samlib;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import java.util.Arrays;
import java.util.List;
import monakhv.android.samlib.data.SettingsHelper;

/**
 *
 * @author monakhv
 */
public class SamlibPreferencesActivity extends PreferenceActivity
        implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener {

    private static String DEBUG_TAG = "SamlibPreferencesActivity";
    private SettingsHelper helper;
    private final String[] autoSummaryFields = {"pref_key_update_Period", "pref_key_proxy_host",
        "pref_key_proxy_port", "pref_key_proxy_user", "pref_key_update_autoload_limit", "pref_key_book_lifetime"};
    private List<String> autoSumKeys;
    private RingtonePreference ringtonPref;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        getPreferenceManager().setSharedPreferencesName(
                SettingsHelper.PREFS_NAME);
        addPreferencesFromResource(R.xml.prefs);

        helper = new SettingsHelper(this);
        autoSumKeys = Arrays.asList(autoSummaryFields);
        ringtonPref = (RingtonePreference) findPreference(getString(R.string.pref_key_notification_ringtone));
    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.registerListener();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        Log.d(DEBUG_TAG, "onResume");

        for (String key : autoSumKeys) {
            updateSummary(key);
        }

        // A patch to overcome OnSharedPreferenceChange not being called by RingtonePreference bug 

        ringtonPref.setOnPreferenceChangeListener(this);
        updateRingtoneSummary(ringtonPref, helper.getNotificationRingToneURI());

    }

    /**
     * Override onPause to set or cancel task
     */
    @Override
    protected void onPause() {
        super.onPause();
        helper.updateService();
        helper.unRegisterListener();
        SettingsHelper.addAuthenticator(this.getApplicationContext());
        Log.d(DEBUG_TAG, "onPause");
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

    private void updateSummary(String key) {
        if (autoSumKeys.contains(key)) {
            Preference pr = getPreferenceScreen().findPreference(key);
            if (pr instanceof ListPreference) {
                final ListPreference currentPreference = (ListPreference) pr;
                currentPreference.setSummary(currentPreference.getEntry());
            } else if (pr instanceof EditTextPreference) {
                final EditTextPreference currentPreference = (EditTextPreference) pr;
                currentPreference.setSummary(currentPreference.getText());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary(key);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        updateRingtoneSummary((RingtonePreference) preference, Uri.parse((String) newValue));
        return true;
    }

    private void updateRingtoneSummary(RingtonePreference preference, Uri ringtoneUri) {
        Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        if (ringtone != null) {
            preference.setSummary(ringtone.getTitle(this));
        } else {
            preference.setSummary(getString(R.string.pref_no_sound));
        }


    }
}
