package monakhv.android.samlib.data;

import android.util.Log;
import monakhv.samlib.log.AbstractLogger;

/*
 * Copyright 2015  Dmitry Monakhov
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
 * 2/13/15.
 */
public class Logger implements AbstractLogger  {
    @Override
    public void verbose(String tag, String msg) {
        Log.v(tag,msg);
    }

    @Override
    public void debug(String tag, String msg) {
        Log.d(tag,msg);
    }

    @Override
    public void info(String tag, String msg) {
        Log.i(tag,msg);
    }

    @Override
    public void warn(String tag, String msg) {
        Log.w(tag,msg);
    }

    @Override
    public void error(String tag, String msg) {
        Log.e(tag,msg);
    }

    @Override
    public void verbose(String tag, String msg, Throwable ex) {
        Log.v(tag,msg,ex);
    }

    @Override
    public void debug(String tag, String msg, Throwable ex) {
        Log.d(tag,msg,ex);
    }

    @Override
    public void info(String tag, String msg, Throwable ex) {
        Log.i(tag,msg,ex);
    }

    @Override
    public void warn(String tag, String msg, Throwable ex) {
        Log.w(tag,msg,ex);
    }

    @Override
    public void error(String tag, String msg, Throwable ex) {
        Log.e(tag,msg,ex);
    }
}
