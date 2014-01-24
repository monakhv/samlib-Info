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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Calendar;
import monakhv.android.samlib.AuthorListFragment;
import monakhv.android.samlib.BookListFragment;
import monakhv.android.samlib.R;
import monakhv.android.samlib.receiver.UpdateReceiver;
import monakhv.android.samlib.sql.entity.Book;
import monakhv.samlib.http.HttpClientController;

/**
 *
 * @author monakhv
 */
public class SettingsHelper implements SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String PREFS_NAME = "samlib_prefs";
    private Context context = null;
    private static final String DEBUG_TAG = "SettingsHelper";
    private boolean updateService = false;
    private final SharedPreferences prefs;

    public SettingsHelper(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, 0);
    }

    public void registerListener() {
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void unRegisterListener() {

        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(context.getText(R.string.pref_key_flag_background_update).toString())
                || key.equals(context.getText(R.string.pref_key_update_Period).toString())) {
            updateService = true;

        }
    }

    /**
     * Set or cancel Recurring Task using updated preferences
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
     *
     *
     */
    private void cancelRecurringAlarm() {
        Log.d(DEBUG_TAG, "Cancel Updater service call");
        if (getDebugFlag()) {
            DataExportImport.log(DEBUG_TAG, "Cancel Updater service call");

        }
        Intent downloader = new Intent(context, UpdateReceiver.class);
        PendingIntent recurringDownload = PendingIntent.getBroadcast(context,
                0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarms.cancel(recurringDownload);
    }

    /**
     * Set recurring Task
     *
     *
     */
    private void setRecurringAlarm() {
        Log.d(DEBUG_TAG, "Update Updater service call");
        if (getDebugFlag()) {
            DataExportImport.log(DEBUG_TAG, "Update Updater service call");

        }
        //the fist time will be only after updatePeriod
        long updatePeriod = getUpdatePeriod();
        //temp
        //long startTime = Calendar.getInstance().getTimeInMillis() + 10000;
        long startTime = Calendar.getInstance().getTimeInMillis() + updatePeriod;


        Intent downloader = new Intent(context, UpdateReceiver.class);
        PendingIntent recurringDownload = PendingIntent.getBroadcast(context,
                0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);



        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                startTime, updatePeriod, recurringDownload);
    }

    private boolean getBackgroundUpdateFlag() {

        return prefs.getBoolean(
                context.getString(R.string.pref_key_flag_background_update),
                false);
    }

    /**
     * Get whether the application in debug mode or not
     *
     * @return true if application in debug mode
     */
    public boolean getDebugFlag() {

        return prefs.getBoolean(
                context.getString(R.string.pref_key_debug_options),
                false);
    }



    /**
     * Get Ringtone URI used for notification constructions
     *
     * @return URI selected by user or default notification ringtone URI
     */
    public Uri getNotificationRingToneURI() {

        String suri = prefs.getString(context.getString(R.string.pref_key_notification_ringtone), context.getString(R.string.pref_default_notification_ringtone));

        return Uri.parse(suri);
    }

    /**
     * Get automark Flag - whether clean new Mark or not on open bookmark
     *
     * @return automark Flag
     */
    public boolean getAutoMarkFlag() {

        return prefs.getBoolean(
                context.getString(R.string.pref_key_flag_automark),
                true);
    }

    /**
     * get whether we ignore connection error notification or not
     *
     * @return true if we do not provide connection error notification
     */
    public boolean getIgnoreErrorFlag() {

        return prefs.getBoolean(
                context.getString(R.string.pref_key_ignore_connect_error),
                true);
    }

    /**
     * get whether we make automatic update using wifi only
     *
     * @return true if we do update using wifi
     */
    public boolean getWifiOnlyFlag() {

        return prefs.getBoolean(
                context.getString(R.string.pref_key_flag_wihi_only),
                false);
    }

    public boolean getAutoLoadFlag() {

        return prefs.getBoolean(
                context.getString(R.string.pref_key_flag_background_update_autoload),
                false) && getBackgroundUpdateFlag();
    }

    public boolean getLimitBookLifeTimeFlag() {

        return prefs.getBoolean(
                context.getString(R.string.pref_key_flag_limit_booke_lifetime),
                true) && getBackgroundUpdateFlag();
    }

    public String getUpdatePeriodString() {

        return prefs.getString(context.getString(R.string.pref_key_update_Period), context.getString(R.string.pref_default_update_Period));
    }


    public AuthorListFragment.SortOrder getAuthorSortOrder(){
        String str = prefs.getString(
                context.getString(R.string.pref_key_author_order), 
                context.getString(R.string.pref_default_author_order));
        return AuthorListFragment.SortOrder.valueOf(str);
    }

    public BookListFragment.SortOrder getBookSortOrder(){
        String str = prefs.getString(
                context.getString(R.string.pref_key_book_order),
                context.getString(R.string.pref_default_book_order));
        return BookListFragment.SortOrder.valueOf(str);
    }

    private long getUpdatePeriod() {


        String str = getUpdatePeriodString();
        if (getDebugFlag()) {
            //TODO: remove this hack
            //str = "15MINUTES";
            DataExportImport.log(DEBUG_TAG, "Update interval set to: " + str);

        }
        Log.d(DEBUG_TAG, "Update interval: " + str);

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
        if (getDebugFlag()) {
            DataExportImport.log(DEBUG_TAG, "Period Format error us default one");

        }

        return 3 * AlarmManager.INTERVAL_HOUR;

    }

    public void log(String tag, String msg, Exception ex) {
        if (getDebugFlag()) {
            DataExportImport.log(tag, msg, ex);
        }
    }

    public void log(String tag, String msg) {
        if (getDebugFlag()) {
            DataExportImport.log(tag, msg);
        }
    }

    /**
     * Read preferences and add default Authenticator for proxy auth
     *
     * @param context Context
     */
    public static void addAuthenticator(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        boolean useProxy = prefs.getBoolean(context.getString(R.string.pref_key_use_proxy_flag), false);

        if (useProxy) {
            final String proxyUser = prefs.getString(context.getString(R.string.pref_key_proxy_user), "");
            final String proxyPass = prefs.getString(context.getString(R.string.pref_key_proxy_password), "");
            Authenticator.setDefault(
                    new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
                }
            });


            String proxyHost = prefs.getString(context.getString(R.string.pref_key_proxy_host), "localhost");
            String proxyPort = prefs.getString(context.getString(R.string.pref_key_proxy_port), "3128");


            int pp;
            try {
                pp = Integer.parseInt(proxyPort);
            } catch (NumberFormatException ex) {
                Log.e(DEBUG_TAG, "Parse proxy port exception: " + proxyPort);
                pp = 3128;
            }

            HttpClientController.setProxy(proxyHost, pp, proxyUser, proxyPass);


        } else {
            Authenticator.setDefault(null);
            HttpClientController.cleanProxy();
        }

    }

    /**
     * Checks if we have a valid Internet Connection on the device.
     *
     * @param ctx Context
     * @return True if device has internet
     *
     * Code from: http://www.androidsnippets.org/snippets/131/
     */
    public static boolean haveInternetWIFI(Context ctx) {

        NetworkInfo info = ((ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            return false;
        }
        if (info.isRoaming()) {
            // here is the roaming option you can change it if you want to
            // disable internet while roaming, just return false
            return false;
        }
        SettingsHelper helper = new SettingsHelper(ctx);
        if (helper.getWifiOnlyFlag()) {
            ConnectivityManager conMan = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo inf = conMan.getNetworkInfo(1);
            State wifi;
            if (inf != null){
                wifi = inf.getState();
            }
            else {
                return false;
            }

            return wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING;
        }
        return true;
    }

    public static boolean haveInternet(Context ctx) {

        NetworkInfo info = ((ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        return !(info == null || !info.isConnected()) && !info.isRoaming();
    }

    /**
     * Test whether we need download book during update procedure True if we are
     * inside size limit
     *
     * @param book Book object
     * @return true if we need load the book
     */
    public boolean testAutoLoadLimit(Book book) {



        String str = prefs.getString(context.getString(R.string.pref_key_update_autoload_limit), context.getString(R.string.pref_default_update_atutoload_limit));

        int limit;
        try {
            limit = Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            Log.e(DEBUG_TAG, "Error parse Autoload limit: " + str, ex);
            return false;
        }

        if (limit == 0) {
            return true;//download in any way
        }
        long Llimit = (long) limit;
        return book.getSize() < Llimit;
    }

    void checkDeleteBook(File file) {

        String str = prefs.getString(context.getString(R.string.pref_key_book_lifetime), context.getString(R.string.pref_default_book_lifetime));

        int limit;
        try {
            limit = Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            Log.e(DEBUG_TAG, "Error parse Autoload limit: " + str, ex);
            return;
        }



        long interval = AlarmManager.INTERVAL_DAY * limit;
        long curTime = Calendar.getInstance().getTimeInMillis();

        //Log.d("checkDeleteBook", file.getAbsolutePath());
        if ((curTime - file.lastModified()) > interval) {
            Log.i("checkDeleteBook", "delete book: " + file.getAbsolutePath());
            if (! file.delete()){
                Log.e(DEBUG_TAG, "Can not delete the book: "  + file.getAbsolutePath());
            }
        }

    }
}
