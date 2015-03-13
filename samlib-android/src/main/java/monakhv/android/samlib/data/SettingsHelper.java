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
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;


import monakhv.android.samlib.R;
import monakhv.android.samlib.receiver.UpdateReceiver;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.http.Proxy;

/**
 * @author monakhv
 */
public class SettingsHelper implements monakhv.samlib.data.SettingsHelper, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String DATA_DIR = "/SamLib-Info/";

    public final static String PREFS_NAME = "samlib_prefs";
    private Context context = null;
    private static final String DEBUG_TAG = "SettingsHelper";
    private boolean updateService = false;
    private final SharedPreferences prefs;
    private static final String DARK = "DARK";
    private static final String LIGHT = "LIGHT";
    private static final String DATE_FORMAT_DEBUG = "dd-MM-yyyy HH:mm:ss";
    private static final String DATE_FORMAT_BOOK_FILE = "dd-MM-yyyy_HH-mm-ss";
    private static final String DEBUG_FILE = SQLController.DB_NAME + ".log";

    public SettingsHelper(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, 0);
        monakhv.samlib.log.Log.checkInit(new Logger());

    }

    /**
     * unconditionally request backup data
     */
    public void requestBackup() {
        BackupManager bmr = new BackupManager(context);
        bmr.dataChanged();
    }

    /**
     * Request backup the first time after upgrade only
     * Call from update manager
     */
    public void requestFirstBackup() {
        String str = prefs.getString(context.getString(R.string.pref_key_version_name), null);


        if (str == null || !str.equals(getVersionName())) {

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(context.getString(R.string.pref_key_version_name), getVersionName());
            editor.commit();
            Log.d(DEBUG_TAG, "SettingsHelper: requestBackup");
            requestBackup();
        } else {
            Log.d(DEBUG_TAG, "SettingsHelper: IGNORE requestBackup");
        }
    }

    public String getVersionName() {
        String res = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            res = pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void registerListener() {
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void unRegisterListener() {

        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        requestBackup();

        if (key.equals(context.getString(R.string.pref_key_flag_background_update))
                || key.equals(context.getString(R.string.pref_key_update_Period))) {
            updateService = true;

        }
        if (key.equals(context.getString(R.string.pref_key_mirror))) {
            String mirror = sharedPreferences.getString(context.getString(R.string.pref_key_mirror), null);
            Log.i(DEBUG_TAG, "Set the first mirror to: " + mirror);
            SamLibConfig sc = SamLibConfig.getInstance(this);
            sc.refreshData();
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
     */
    private void cancelRecurringAlarm() {
        Log.d(DEBUG_TAG, "Cancel Updater service call");
        if (getDebugFlag()) {
            log(DEBUG_TAG, "Cancel Updater service call");

        }
        Intent downloader = new Intent(context, UpdateReceiver.class);
        PendingIntent recurringDownload = PendingIntent.getBroadcast(context,
                0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarms.cancel(recurringDownload);
    }

    /**
     * Set recurring Task
     */
    private void setRecurringAlarm() {
        Log.d(DEBUG_TAG, "Update Updater service call");
        if (getDebugFlag()) {
            log(DEBUG_TAG, "Update Updater service call");

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

    public String getGoogleAccount() {
        return prefs.getString(context.getString(R.string.pref_key_google_account), null);
    }

    public void setGoogleAccount(String account) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(context.getString(R.string.pref_key_google_account), account);
        edit.commit();
    }


    /**
     * Get Ringtone URI used for notification constructions
     *
     * @return URI selected by user or default notification ringtone URI
     */
    public Uri getNotificationRingToneURI() {

        String sUri = prefs.getString(context.getString(R.string.pref_key_notification_ringtone), context.getString(R.string.pref_default_notification_ringtone));

        return Uri.parse(sUri);
    }

    /**
     * Get auto-mark Flag - whether clean new Mark or not on open bookmark
     *
     * @return auto-mark Flag
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


    public String getAuthorSortOrderString() {
        String str = prefs.getString(
                context.getString(R.string.pref_key_author_order),
                context.getString(R.string.pref_default_author_order));
        return str;
    }


    public String getBookSortOrderString() {
        String str = prefs.getString(
                context.getString(R.string.pref_key_book_order),
                context.getString(R.string.pref_default_book_order));
        return str;
    }

    public FileType getFileType() {
        String str = prefs.getString(
                context.getString(R.string.pref_key_file_format),
                context.getString(R.string.pref_default_file_format)
        );
        return FileType.valueOf(str);
    }

    private long getUpdatePeriod() {


        String str = getUpdatePeriodString();
        if (getDebugFlag()) {
            //TODO: remove this hack
            //str = "15MINUTES";
            log(DEBUG_TAG, "Update interval set to: " + str);

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
            log(DEBUG_TAG, "Period Format error us default one");

        }

        return 3 * AlarmManager.INTERVAL_HOUR;

    }

    /**
     * Log output
     *
     * @param tag debug tag
     * @param msg message
     * @param ex  Exception
     */
    public void log(String tag, String msg, Exception ex) {
        if (getDebugFlag()) {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_DEBUG);
            File save = new File(getDataDirectory(), DEBUG_FILE);
            if (! save.exists()){
                try {
                    save.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream dst;
            Date date = Calendar.getInstance().getTime();

            try {
                dst = new FileOutputStream(save, true);
                PrintStream ps = new PrintStream(dst);
                ps.println(df.format(date) + "  " + tag + " " + msg);
                if (ex != null) {
                    ex.printStackTrace(ps);
                }
                ps.flush();
                dst.flush();
                ps.close();
                dst.close();
            } catch (Exception ex1) {
                Log.e(DEBUG_TAG, "Log save error", ex1);
            }

        }
    }
    /**
     * Setting file to store book content
     * making parent directories if need
     *
     * @param book Book object to get File for
     * @return  File object to sore book to
     */
    @Override
    public File getBookFile(Book book, FileType fileType) {
        String ff;
        if (book.isPreserve()){
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_BOOK_FILE);
            ff=  DataExportImport.BOOKS_DIR +"/"+    book.getUri()    + "/" +   df.format(Calendar.getInstance().getTime())+ fileType.ext;
        }
        else {
            ff=  DataExportImport.BOOKS_DIR +"/"+    book.getUri()    +      fileType.ext;
        }


        File ss = new File(getDataDirectory(), ff);
        File pp = ss.getParentFile();
        boolean res =pp.mkdirs();
        Log.d(DEBUG_TAG, "Path: " + pp.getAbsolutePath() + " result is: " + res);
        return ss;

    }

    /**
     * Create directory to store many versions for the book
     * Move existing version into the directory
     * @param book
     */
    public void makePreserved(Book book){
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_BOOK_FILE);

        File dir =  new File(getDataDirectory(), DataExportImport.BOOKS_DIR +"/"+    book.getUri()    );
        dir.mkdir();

        File old = getBookFile(book,book.getFileType());
        if (old.exists()){
            Date lm = Calendar.getInstance().getTime();
            lm.setTime(old.lastModified());
            old.renameTo(new File(dir,df.format(lm)+book.getFileType().ext));
        }

    }

    /**
     * get All version for book files for read selection
     * @param book
     * @return
     */
    public  String[] getBookFileVersions(Book book){
        File dir =  new File(getDataDirectory(), DataExportImport.BOOKS_DIR +"/"+    book.getUri()    );
        List<String> files = new ArrayList<String>();
        for (String fn : dir.list()){
            if (fn.endsWith(book.getFileType().ext)){
                files.add(fn);
            }
        }
//        if (files.isEmpty()){
//            return null;
//        }
        return files.toArray(new String[files.size()]);
    }

    /**
     * get Book file to read it
     * @param book
     * @param fileType
     * @return
     */
    public File getBookFile4Read(Book book,FileType fileType){
        if (book.isPreserve()){//choose latest version to read
            File dir =  new File(getDataDirectory(), DataExportImport.BOOKS_DIR +"/"+    book.getUri()    );
            File res = null;
            long lastmod=0L;
            for (String fn : dir.list()){
                if (fn.endsWith(fileType.ext)){
                    File file = new File(dir,fn);
                    Log.i(DEBUG_TAG,"test file "+fn+" - "+file.getAbsolutePath());
                    if (file.lastModified()>lastmod){
                        lastmod=file.lastModified();
                        res=file;
                    }
                }
            }//file cycle
            return res;
        }
        else {
            return getBookFile(book,fileType);//we have the only version just open it
        }

    }
    /**
     * Get URL to open book for offline reading
     * @return construct URL to start external program for offline reading
     */
    public String getBookFileURL(Book book) {
        return "file://" + getBookFile4Read(book, book.getFileType()).getAbsolutePath();
    }

    /**
     * Get URL to open book for offline reading
     * To read  particular version of file
     * @param book Book object
     * @param file version file name
     * @return file URL to READ
     */
    public String getBookFileURL(Book book,String file) {
        File dir =  new File(getDataDirectory(), DataExportImport.BOOKS_DIR +"/"+    book.getUri()    );
        File f=new File(dir,file);
        return "file://" +f.getAbsolutePath();
    }
    /**
     * Clean downloaded files of any types
     * Find all book for read and delete them
     *
     * @param book  Book object
     */
    public void cleanBookFile(Book book){
        for (monakhv.samlib.data.SettingsHelper.FileType ft : monakhv.samlib.data.SettingsHelper.FileType.values()){
            File ff = getBookFile4Read(book, ft);

            if (ff!=null && ff.exists()) {
                ff.delete();
            }
        }
    }

    public void log(String tag, String msg) {
        if (getDebugFlag()) {
            log(tag, msg, null);
        }
    }

    public Proxy getProxy(){

        boolean useProxy = prefs.getBoolean(context.getString(R.string.pref_key_use_proxy_flag), false);
        if (! useProxy){
            return null;
        }
        boolean wifiProxyFlag = prefs.getBoolean(context.getString(R.string.pref_key_use_proxy_wifi_flag), false);
        if (wifiProxyFlag && !isWiFi(context)){
            return null;//we have active flag but have not wifi, so do not use proxy
        }
        String user = prefs.getString(context.getString(R.string.pref_key_proxy_user), "");
        String password = prefs.getString(context.getString(R.string.pref_key_proxy_password), "");
        String host = prefs.getString(context.getString(R.string.pref_key_proxy_host), "localhost");
        String proxyPort = prefs.getString(context.getString(R.string.pref_key_proxy_port), "3128");

        int pp;
        try {
            pp = Integer.parseInt(proxyPort);
        } catch (NumberFormatException ex) {
            Log.e(DEBUG_TAG, "Parse proxy port exception: " + proxyPort);
            pp = 3128;
        }
        Proxy proxy=new Proxy(host,pp,user,password);

        return proxy;
    }


    /**
     * Checks if we have a valid Internet Connection on the device.
     *
     * @param ctx Context
     * @return True if device has internet
     * <p/>
     * Code from: http://www.androidsnippets.org/snippets/131/
     */
    public static boolean haveInternetWIFI(Context ctx) {

        NetworkInfo info = ((ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            return false;
        }
        SettingsHelper helper = new SettingsHelper(ctx);
        boolean workRoaming = helper.prefs.getBoolean(ctx.getString(R.string.pref_key_flag_roaming_work), false);
        if (info.isRoaming() && !workRoaming) {
            // here is the roaming option you can change it if you want to
            // disable internet while roaming if user check do not work in Roaming
            return false;
        }


        if (helper.getWifiOnlyFlag()) {
            return isWiFi(ctx);
        }
        return true;
    }

    /**
     * Check if we have WIFI active or not
     * @param ctx
     * @return
     */
    private static boolean isWiFi(Context ctx){
        ConnectivityManager conMan = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
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


        String str = prefs.getString(context.getString(R.string.pref_key_update_autoload_limit), context.getString(R.string.pref_default_update_atutoload_limit));

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
        String str = prefs.getString(context.getString(R.string.pref_key_theme),
                context.getString(R.string.pref_default_theme));

        if (str.equals(LIGHT)) {
            return R.style.MyThemeLight;
        }
        return R.style.MyTheme;

    }

    public int getSelectedIcon() {

        TypedArray a = context.getTheme().obtainStyledAttributes(getTheme(), new int[]{R.attr.iconSelected});
        return a.getResourceId(0, 0);

    }

    public int getLockIcon(){
        TypedArray a = context.getTheme().obtainStyledAttributes(getTheme(), new int[]{R.attr.iconLock});
        return a.getResourceId(0, 0);
    }

    public int getSortIcon() {
        String str = prefs.getString(context.getString(R.string.pref_key_theme),
                context.getString(R.string.pref_default_theme));
        if (str.equals(LIGHT)) {
            return R.drawable.collections_sort_by_size_l;
        }
        return R.drawable.collections_sort_by_size;
    }


    public String getFirstMirror() {
        return prefs.getString(context.getString(R.string.pref_key_mirror), context.getString(R.string.pref_default_mirror));
    }

    void checkDeleteBook(File file) {

        String str = prefs.getString(context.getString(R.string.pref_key_book_lifetime), context.getString(R.string.pref_default_book_lifetime));

        int limit;
        try {
            limit = Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            Log.e(DEBUG_TAG, "Error parse Auto-load limit: " + str, ex);
            return;
        }


        long interval = AlarmManager.INTERVAL_DAY * limit;
        long curTime = Calendar.getInstance().getTimeInMillis();

        //Log.d("checkDeleteBook", file.getAbsolutePath());
        if ((curTime - file.lastModified()) > interval) {
            Log.i("checkDeleteBook", "delete book: " + file.getAbsolutePath());
            if (!file.delete()) {
                Log.e(DEBUG_TAG, "Can not delete the book: " + file.getAbsolutePath());
            }
        }

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

    /**
     * Return absolute path data directory preference
     * get Default directory as SD-path + Samlib-Info
     * <p/>
     * Create if need
     *
     * @return Absolute path to the data directory
     */
    public String getDataDirectoryPath() {

        return getDataDirectory().getAbsolutePath();
    }

    public File getDataDirectory() {
        String SdPath = prefs.getString(context.getString(R.string.pref_key_directory), null);
        if (TextUtils.isEmpty(SdPath)) {
            SdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d(DEBUG_TAG, "Data dir default set to: " + SdPath);

            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(context.getString(R.string.pref_key_directory), SdPath);
            edit.commit();

        }
        File ff = new File(SdPath + DATA_DIR);
        if (ff.isDirectory()) {
            return ff;
        }
        ff.mkdirs();
        return ff;
    }

    public String getUpdateTag() {
        return prefs.getString(context.getString(R.string.pref_key_update_tag),
                Integer.toString(SamLibConfig.TAG_AUTHOR_ALL));
    }
}
