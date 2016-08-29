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
 *  15.02.16 13:27
 *
 */

package monakhv.samlib.service;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Simple application event bus
 * Created by monakhv on 15.02.16.
 */
public class GuiEventBus {
    private final Subject<GuiUpdateObject, GuiUpdateObject> mSubject
            = new SerializedSubject<>(PublishSubject.create());
    public GuiEventBus(){

    }
    public Observable<GuiUpdateObject> getObservable() {
        return mSubject;
    }

    void post(GuiUpdateObject guiUpdateObject){
        mSubject.onNext(guiUpdateObject);
    }
}
