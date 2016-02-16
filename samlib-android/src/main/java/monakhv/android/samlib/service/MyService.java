/*
 *   Copyright 2015 Dmitry Monakhov.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *
 */

package monakhv.android.samlib.service;

import android.app.Service;
import android.content.Context;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import monakhv.android.samlib.SamlibApplication;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sql.DatabaseHelper;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.log.Log;
import monakhv.samlib.service.GuiEventBus;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;


/**
 * General Service which contains ORMLite DBHelper methods
 * Created by monakhv on 04.12.15.
 */
public abstract class MyService extends Service {
    private volatile DatabaseHelper helper;
    private volatile boolean created = false;
    private volatile boolean destroyed = false;
    private CompositeSubscription mCompositeSubscription;

    private SamlibApplication mSamlibApplication;


    @Override
    public void onCreate() {
        if (helper == null) {
            helper = getHelperInternal(this);
            created = true;
        }
        super.onCreate();

        mSamlibApplication = (SamlibApplication) getApplication();
        mCompositeSubscription = new CompositeSubscription();

    }

    public void addSubscription(Subscription subscription){
        mCompositeSubscription.add(subscription);
    }

    public SettingsHelper getSettingsHelper(){
        return mSamlibApplication.getSettingsHelper();
    }

    protected GuiEventBus getBus(){
        return mSamlibApplication.getApplicationComponent().getGuiEventBus();
    }

    public HttpClientController getHttpClientController(){
        return mSamlibApplication.getApplicationComponent().getHttpClientController();
    }

    public SpecialSamlibService getSpecialSamlibService(){
        return mSamlibApplication.getDatabaseComponent(getHelper()).getSpecialSamlibService();
    }

    public AuthorController getAuthorController(){
        return mSamlibApplication.getDatabaseComponent(getHelper()).getAuthorController();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyService","onDestroy");
        releaseHelper();
        destroyed = true;
        mSamlibApplication.releaseDatabaseComponent();
        mSamlibApplication.releaseServiceComponent();
        if (mCompositeSubscription != null){
            mCompositeSubscription.unsubscribe();
        }
    }

    public DatabaseHelper getHelper() {
        if (helper == null) {
            if (!created) {
                throw new IllegalStateException("A call has not been made to onCreate() yet so the helper is null");
            } else if (destroyed) {
                throw new IllegalStateException(
                        "A call to onDestroy has already been made and the helper cannot be used after that point");
            } else {
                throw new IllegalStateException("Helper is null for some unknown reason");
            }
        } else {
            return helper;
        }
    }

    /**
     * This is called internally by the class to populate the helper object instance. This should not be called directly
     * by client code unless you know what you are doing. Use {@link #getHelper()} to get a helper instance. If you are
     * managing your own helper creation, override this method to supply this activity with a helper instance.
     * <p/>
     * <p/>
     * <b> NOTE: </b> I
     */
    protected DatabaseHelper getHelperInternal(Context context) {
        @SuppressWarnings({"unchecked", "deprecation"})
        DatabaseHelper newHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        return newHelper;
    }

    /**
     * Release the helper instance created in {@link #getHelperInternal(Context)}. You most likely will not need to call
     * this directly since {@link #onDestroy()} does it for you.
     * <p/>
     * <p>
     * <b> NOTE: </b> If you override this method, you most likely will need to override the
     * {@link #getHelperInternal(Context)} method as well.
     * </p>
     */
    protected void releaseHelper() {
        OpenHelperManager.releaseHelper();
        this.helper = null;
    }
}
