package monakhv.android.samlib.data.backup;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.Map;


import monakhv.android.samlib.data.SettingsHelper;

import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;
import monakhv.android.samlib.tasks.AddAuthorRestore;


import javax.inject.Inject;

/*
 * Copyright 2014  Dmitry Monakhov
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
 *
 * 4/28/14.
 */


/**
 * List of authors and tags put into preferences for backup and restore usage
 */
public class AuthorStatePrefs {
    private static final String DEBUG_TAG = "AuthorStatePrefs";
    public static final String PREF_NAME = "AuthorStatePrefs";

    private final Context context;
    private SharedPreferences prefs;
    private final SettingsHelper settings;
    private final AuthorController mAuthorController;
    private final AddAuthorRestore mAddAuthorRestore;


    @Inject
    public AuthorStatePrefs(SettingsHelper settings , AddAuthorRestore addAuthorRestore, AuthorController authorController) {

        mAuthorController = authorController;
        this.context = settings.getContext();
        mAddAuthorRestore=addAuthorRestore;

        this.prefs = getPrefs();
        this.settings = settings;

    }

    /**
     * Prepare for backup
     * Save Author list and setting to special SharedPreferences object
     */
    public void load() {
        SharedPreferences.Editor editor;
        editor = prefs.edit();
        editor.clear();
        editor.commit();
        settings.backup(prefs);
        editor = prefs.edit();

        for (Author a : mAuthorController.getAll()) {
            editor.putString(a.getUrlForBrowser(settings), a.getAll_tags_name());

            Log.d(DEBUG_TAG, "url: " + a.getUrlForBrowser(settings) + " - " + a.getAll_tags_name());
        }
        editor.commit();

    }

    /**
     * Restore actual data from  special SharedPreferences after BackupManager restore
     * 1 Add author to data bases
     * 2 Restore Settings
     */
    public void restore() {
        this.prefs = getPrefs();

        Map<String, ?> map = prefs.getAll();
        settings.restore(map);



        for (String u : map.keySet().toArray(new String[1])) {
            Log.d(DEBUG_TAG, "get: " + u + " - " + map.get(u).toString());
        }
        mAddAuthorRestore.execute(map.keySet().toArray(new String[1]));


    }

    public SharedPreferences getPrefs() {
        return getSharedPreferences(context, PREF_NAME);
    }

    private static SharedPreferences getSharedPreferences(Context context, String name) {
        int sdk = android.os.Build.VERSION.SDK_INT;


        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            return getSharedPreferencesLegacy(context, name);
        } else {
            return getSharedPreferencesModern(context, name);
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static SharedPreferences getSharedPreferencesModern(Context context, String fn) {

        return context.getSharedPreferences(fn, Context.MODE_MULTI_PROCESS);
    }

    private static SharedPreferences getSharedPreferencesLegacy(Context context, String fn) {

        return context.getSharedPreferences(fn, Context.MODE_PRIVATE);
    }


}
