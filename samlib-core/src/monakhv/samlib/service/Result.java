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
 *  12.02.16 11:23
 *
 */

package monakhv.samlib.service;

/**
 * Created by monakhv on 12.02.16.
 */
public class Result {
    boolean mRes;
    int numberOfAdded = 0;
    int numberOfDeleted = 0;
    int doubleAdd = 0;
    String mMessage;

    public Result(boolean res){
        mRes=res;
    }
    public Result(boolean res, String message) {
        mRes = res;
        mMessage = message;
    }
}
