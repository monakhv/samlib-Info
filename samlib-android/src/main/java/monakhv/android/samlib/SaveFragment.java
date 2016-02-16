/*
 *  Copyright 2016 Dmitry Monakhov.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  16.02.16 10:51
 *
 */

package monakhv.android.samlib;

import android.os.Bundle;
import android.support.annotation.Nullable;
import monakhv.samlib.service.GuiUpdateObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Special Retain Fragment to store cached Observable
 * Created by monakhv on 16.02.16.
 */
public class SaveFragment extends MyBaseAbstractFragment {
    public static final String TAG="SaveFragment";

    private Observable<GuiUpdateObject> mObjectObservable;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mObjectObservable=getBus()
                .getObservable()
                .cache()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread());
    }
    public Observable<GuiUpdateObject> getObjectObservable(){
        return mObjectObservable;
    }

}
