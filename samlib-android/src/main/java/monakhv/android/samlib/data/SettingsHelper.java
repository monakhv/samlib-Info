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
package monakhv.android.samlib.data;

import android.app.AlarmManager;

import android.app.PendingIntent;
import android.app.backup.BackupManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Environment;
import android.text.TextUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;


import java.io.File;
import java.util.*;


import monakhv.android.samlib.R;
import monakhv.android.samlib.SamlibApplication;
import monakhv.android.samlib.receiver.UpdateReceiver;
import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.http.ProxyData;
import monakhv.samlib.log.Log;



/**
 * @author monakhv
 */
public class SettingsHelper extends AbstractSettings implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String DATA_DIR = "/SamLib-Info/";

    public final static String PREFS_NAME = "samlib_prefs";
    private Context mContext = null;
    private static final String DEBUG_TAG = "SettingsHelper";
    private boolean updateService = false;
    private final SharedPreferences prefs;
    private static final String DARK = "DARK";
    private static final String LIGHT = "LIGHT";
    static final String DEBUG_FILE = SQLController.DB_NAME + ".log";




    public SettingsHelper(Context context) {
        this.mContext = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, 0);

    }

    public Context getContext(){
        return mContext;
    }



    /**
     * unconditionally request backup data
     */
    public void requestBackup() {
        BackupManager bmr = new BackupManager(mContext);
        bmr.dataChanged();
    }

    /**
     * Request backup the first time after upgrade only
     * Call from update manager
     */
    public void requestFirstBackup() {
        String str = prefs.getString(mContext.getString(R.string.pref_key_version_name), null);


        if (str == null || !str.equals(getVersionName())) {

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(mContext.getString(R.string.pref_key_version_name), getVersionName());
            editor.commit();
            Log.d(DEBUG_TAG, "SettingsHelper: requestBackup");
            requestBackup();
        } else {
            Log.d(DEBUG_TAG, "SettingsHelper: IGNORE first time requestBackup");
        }
    }

    public String getVersionName() {
        String res = "";
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            res = pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void registerListener() {
        Log.d(DEBUG_TAG, "Register Listener");
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void unRegisterListener() {
        Log.d(DEBUG_TAG, "Unregister Listener");
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        requestBackup();
        Log.d(DEBUG_TAG, "Preference change, key is " + key);
        if (key.equals(mContext.getString(R.string.pref_key_flag_background_update))
                || key.equals(mContext.getString(R.string.pref_key_update_Period))) {
            updateService = true;

        }
        if (key.equals(mContext.getString(R.string.pref_key_mirror))) {
            String mirror = sharedPreferences.getString(mContext.getString(R.string.pref_key_mirror), null);
            Log.i(DEBUG_TAG, "Set the first mirror to: " + mirror);
            SamLibConfig sc = SamLibConfig.getInstance(this);
            sc.refreshData();
        }
        if (key.equals(mContext.getString(R.string.pref_key_debug_options))){
            SamlibApplication.initLogger();

        }
    }

    /**
     * Set or cancel Recurring Task using updated preferences into Alarm Manager
     */
    public void updateService() {
        if (!updateService) {
            return;
        }
        updateServiceForce();
    }

    public void updateServiceForce() {

        if (getBackgroundUpdateFlag()) {

            setRecurringAlarm();
        } else {

            cancelRecurringAlarm();
        }
    }

    /**
     * Cancel recurring Task
     */
    private void cancelRecurringAlarm() {
        Log.i(DEBUG_TAG, "Cancel Updater service call");

        /**
         * We are using receiver here to avoid possible interference with the Pending intend in ProgressNotification
         */

        Intent intent = new Intent(mContext, UpdateReceiver.class);
        PendingIntent recurringUpdate = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarms.cancel(recurringUpdate);
    }

    /**
     * Set recurring Task in Alarm Manager
     */
    private void setRecurringAlarm() {
        Log.i(DEBUG_TAG, "Update Updater service call");

        //the fist time will be only after updatePeriod
        long updatePeriod = getUpdatePeriod();
        //temp
        //long startTime = Calendar.getInstance().getTimeInMillis() + 10000;
        long startTime = Calendar.getInstance().getTimeInMillis() + updatePeriod;


        /**
         * We are using receiver here to avoid possible interference with the Pending intend in ProgressNotification
         */
        Intent intent = new Intent(mContext, UpdateReceiver.class);
        PendingIntent recurringUpdate = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, startTime, updatePeriod, recurringUpdate);
    }

    private boolean getBackgroundUpdateFlag() {

        return prefs.getBoolean(
                mContext.getString(R.string.pref_key_flag_background_update),
                false);
    }

    /**
     * Get whether the application in debug mode or not
     *
     * @return true if application in debug mode
     */
    public boolean getDebugFlag() {

        return prefs.getBoolean(
                mContext.getString(R.string.pref_key_debug_options),
                false);
    }

    public String getGoogleAccount() {
        return prefs.getString(mContext.getString(R.string.pref_key_google_account), null);
    }

    public void setGoogleAccount(String account) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(mContext.getString(R.string.pref_key_google_account), account);
        edit.commit();
    }


    /**
     * Get Ringtone URI used for notification constructions
     *
     * @return URI selected by user or default notification ringtone URI
     */
    public Uri getNotificationRingToneURI() {

        String sUri = prefs.getString(mContext.getString(R.string.pref_key_notification_ringtone), mContext.getString(R.string.pref_default_notification_ringtone));

        return Uri.parse(sUri);
    }

    /**
     * Get auto-mark Flag - whether clean new Mark or not on open bookmark
     *
     * @return auto-mark Flag
     */
    public boolean getAutoMarkFlag() {

        return prefs.getBoolean(
                mContext.getString(R.string.pref_key_flag_automark),
                true);
    }

    /**
     * get whether we ignore connection error notification or not
     *
     * @return true if we do not provide connection error notification
     */
    public boolean getIgnoreErrorFlag() {

        return prefs.getBoolean(
                mContext.getString(R.string.pref_key_ignore_connect_error),
                true);
    }

    /**
     * get whether we make automatic update using wifi only
     *
     * @return true if we do update using wifi
     */
    public boolean getWifiOnlyFlag() {

        return prefs.getBoolean(
                mContext.getString(R.string.pref_key_flag_wihi_only),
                false);
    }

    @Override
    public boolean getAutoLoadFlag() {

        return prefs.getBoolean(
                mContext.getString(R.string.pref_key_flag_background_update_autoload),
                false) ;
    }

    @Override
    public boolean getLimitBookLifeTimeFlag() {

        return prefs.getBoolean(
                mContext.getString(R.string.pref_key_flag_limit_booke_lifetime),
                true) && getBackgroundUpdateFlag();
    }

    public String getUpdatePeriodString() {

        return prefs.getString(mContext.getString(R.string.pref_key_update_Period), mContext.getString(R.string.pref_default_update_Period));
    }


    public String getAuthorSortOrderString() {
        String str = prefs.getString(
                mContext.getString(R.string.pref_key_author_order),
                mContext.getString(R.string.pref_default_author_order));
        return str;
    }


    public String getBookSortOrderString() {
        String str = prefs.getString(
                mContext.getString(R.string.pref_key_book_order),
                mContext.getString(R.string.pref_default_book_order));
        return str;
    }

    public FileType getFileType() {
        String str = prefs.getString(
                mContext.getString(R.string.pref_key_file_format),
                mContext.getString(R.string.pref_default_file_format)
        );
        return FileType.valueOf(str);
    }

    private long getUpdatePeriod() {


        String str = getUpdatePeriodString();

        Log.i(DEBUG_TAG, "Update interval: " + str);

        if (str.equals("1HOUR")) {
            return AlarmManager.INTERVAL_HOUR;
        }
        if (str.equals("3HOUR")) {
            return 3 * AlarmManager.INTERVAL_HOUR;
        }
        if (str.equals("6HOUR")) {
            return 6 * AlarmManager.INTERVAL_HOUR;
        }
        if (str.equals("12HOUR")) {
            return AlarmManager.INTERVAL_HALF_DAY;
        }
        if (str.equals("DAY")) {
            return AlarmManager.INTERVAL_DAY;
        }
        if (str.equals("15MINUTES")) {
            return AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        }
        Log.e(DEBUG_TAG, "Period Format error us default one");


        return 3 * AlarmManager.INTERVAL_HOUR;

    }




    public ProxyData getProxy(){

        boolean useProxy = prefs.getBoolean(mContext.getString(R.string.pref_key_use_proxy_flag), false);
        if (! useProxy){
            return null;
        }
        boolean wifiProxyFlag = prefs.getBoolean(mContext.getString(R.string.pref_key_use_proxy_wifi_flag), false);
        if (wifiProxyFlag && !isWiFi()){
            return null;//we have active flag but have not wifi, so do not use proxy
        }
        if (prefs.getBoolean(mContext.getString(R.string.pref_key_use_google_proxy_flag),false)){
            return ProxyData.GOOGLE_HTTP;
        }
        String user = prefs.getString(mContext.getString(R.string.pref_key_proxy_user), "");
        String password = prefs.getString(mContext.getString(R.string.pref_key_proxy_password), "");
        String host = prefs.getString(mContext.getString(R.string.pref_key_proxy_host), "localhost");
        String proxyPort = prefs.getString(mContext.getString(R.string.pref_key_proxy_port), "3128");

        int pp;
        try {
            pp = Integer.parseInt(proxyPort);
        } catch (NumberFormatException ex) {
            Log.e(DEBUG_TAG, "Parse proxy port exception: " + proxyPort);
            pp = 3128;
        }
        ProxyData proxy=new ProxyData(host,pp,user,password);

        return proxy;
    }


    /**
     * Checks if we have a valid Internet Connection on the device.
     *
     *
     * @return True if device has internet
     * <p/>
     * Code from: http://www.androidsnippets.org/snippets/131/
     */
    public boolean haveInternetWIFI() {

        NetworkInfo info = ((ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            return false;
        }
        boolean workRoaming = prefs.getBoolean(mContext.getString(R.string.pref_key_flag_roaming_work), false);

        if (info.isRoaming() && !workRoaming) {
            // here is the roaming option you can change it if you want to
            // disable internet while roaming if user check do not work in Roaming
            return false;
        }


        if (getWifiOnlyFlag()) {
            return isWiFi();
        }
        return true;
    }

    /**
     * Check if we have WIFI active or not
     *
     * @return
     */
    private  boolean isWiFi(){
        ConnectivityManager conMan = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo inf = conMan.getNetworkInfo(1);
        State wifi;
        if (inf != null) {
            wifi = inf.getState();
        } else {
            return false;
        }

        return wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING;
    }

    /**
     * Test whether we have internet or not
     * Use of manual update from Activity. Here we do not care about roaming
     *
     * @param ctx Context
     * @return true if we have internet connection
     */
    public static boolean haveInternet(Context ctx) {

        NetworkInfo info = ((ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return !(info == null || !info.isConnected());
    }

    /**
     * Test whether we need download book during update procedure True if we are
     * inside size limit
     *
     * @param book Book object
     * @return true if we need load the book
     */
    public boolean testAutoLoadLimit(Book book) {


        String str = prefs.getString(mContext.getString(R.string.pref_key_update_autoload_limit), mContext.getString(R.string.pref_default_update_atutoload_limit));

        int limit;
        try {
            limit = Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            Log.e(DEBUG_TAG, "Error parse Auto-load limit: " + str, ex);
            return false;
        }

        if (limit == 0) {
            return true;//download in any way
        }
        long lLimit = (long) limit;
        return book.getSize() < lLimit;
    }

    public int getTheme() {
        String str = prefs.getString(mContext.getString(R.string.pref_key_theme),
                mContext.getString(R.string.pref_default_theme));

        if (str.equals(LIGHT)) {
            return R.style.MyThemeLight;
        }
        return R.style.MyTheme;

    }

    public int getSelectedIcon() {

        TypedArray a = mContext.getTheme().obtainStyledAttributes(getTheme(), new int[]{R.attr.iconSelected});
        return a.getResourceId(0, 0);

    }

    public int getLockIcon(){
        TypedArray a = mContext.getTheme().obtainStyledAttributes(getTheme(), new int[]{R.attr.iconLock});
        return a.getResourceId(0, 0);
    }

    public int getSortIcon() {
        String str = prefs.getString(mContext.getString(R.string.pref_key_theme),
                mContext.getString(R.string.pref_default_theme));
        if (str.equals(LIGHT)) {
            return R.drawable.collections_sort_by_size_l;
        }
        return R.drawable.collections_sort_by_size;
    }

    @Override
    public String getFirstMirror() {
        return prefs.getString(mContext.getString(R.string.pref_key_mirror), mContext.getString(R.string.pref_default_mirror));
    }

    @Override
    public String getBookLifeTime(){
        return prefs.getString(mContext.getString(R.string.pref_key_book_lifetime), mContext.getString(R.string.pref_default_book_lifetime));
    }


    /**
     * Prepare for  backup
     * Copy all preferences into external object
     *
     * @param tot Special SharedPreferences to copy settings to
     */
    public void backup(SharedPreferences tot) {
        Log.d(DEBUG_TAG, "begin settings backup");
        SharedPreferences.Editor editor = tot.edit();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {

            Object v = entry.getValue();
            String key = entry.getKey();
            Log.d(DEBUG_TAG, "copy key: " + key + " value: " + v);

            if (v instanceof Boolean)
                editor.putBoolean(key, (Boolean) v);
            else if (v instanceof Float)
                editor.putFloat(key, (Float) v);
            else if (v instanceof Integer)
                editor.putInt(key, (Integer) v);
            else if (v instanceof Long)
                editor.putLong(key, (Long) v);
            else if (v instanceof String)
                editor.putString(key, ((String) v));

        }
        editor.commit();

    }

    /**
     * Restore settings from external object
     *
     * @param map external data to restore data from
     */
    public void restore(Map<String, ?> map) {
        Log.d(DEBUG_TAG, "begin setting restore");

        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, ?> entry : map.entrySet()) {


            String key = entry.getKey();
            Object v = entry.getValue();

            if (key.startsWith("pref_key_")) {
                Log.d(DEBUG_TAG, "restore key: " + key + " with value: " + v);

                if (v instanceof Boolean)
                    editor.putBoolean(key, (Boolean) v);
                else if (v instanceof Float)
                    editor.putFloat(key, (Float) v);
                else if (v instanceof Integer)
                    editor.putInt(key, (Integer) v);
                else if (v instanceof Long)
                    editor.putLong(key, (Long) v);
                else if (v instanceof String)
                    editor.putString(key, ((String) v));
            }


        }
        editor.commit();
        updateServiceForce();
        Log.d(DEBUG_TAG, "end setting restore");

    }

    public boolean isDirectoryWritable(String newValue) {
        File ff = new File(newValue + DATA_DIR);
        ff.mkdirs();
        return ff.canWrite();
//        if (ff.canWrite()){
//            Log.d(DEBUG_TAG,"Directory is Valid!!");
//            return true;
//
//        }
//        else {
//            Log.d(DEBUG_TAG,"WRONG Directory!!");
//            return false;
//        }

    }



    public File getDataDirectory() {
        String SdPath = prefs.getString(mContext.getString(R.string.pref_key_directory), null);
        if (TextUtils.isEmpty(SdPath)) {
            SdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d(DEBUG_TAG, "Data dir default set to: " + SdPath);

            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(mContext.getString(R.string.pref_key_directory), SdPath);
            edit.commit();

        }
        File ff = new File(SdPath + DATA_DIR);
        if (ff.isDirectory()) {
            return ff;
        }
        ff.mkdirs();
        return ff;
    }

    @Override
    public String getCollationRule() {
        return SamLibConfig.COLLATION_RULES_NEW;
    }

    public String getUpdateTag() {
        return prefs.getString(mContext.getString(R.string.pref_key_update_tag),
                Integer.toString(SamLibConfig.TAG_AUTHOR_ALL));
    }

    public boolean isAnimation() {
        return prefs.getBoolean(mContext.getString(R.string.pref_key_flag_anim), false);
    }

    public boolean isNotifyTickerEnable() {
        return prefs.getBoolean(mContext.getString(R.string.pref_key_flag_update_ticker), true);
    }

    public void setGoogleAuto(boolean googleAuto) {
        prefs.edit().putBoolean(mContext.getString(R.string.pref_key_google_auto), googleAuto).commit();

    }

    public boolean isGoogleAuto() {
        return prefs.getBoolean(mContext.getString(R.string.pref_key_google_auto), false);
    }

    public boolean isGoogleAutoEnable() {
        return prefs.getBoolean(mContext.getString(R.string.pref_key_google_auto_enable), false);
    }

    public void setGoogleAutoEnable(boolean enable) {
        prefs.edit().putBoolean(mContext.getString(R.string.pref_key_google_auto_enable), enable).commit();
        if (!enable) {
            setGoogleAuto(false);
        }
    }

    @Override
    public boolean isUpdateDelay(){
        return prefs.getBoolean(mContext.getString(R.string.pref_key_update_delay), false);
    }

    public boolean isShowBookDate(){
        return prefs.getBoolean(mContext.getString(R.string.pref_key_book_date),false);
    }
}
