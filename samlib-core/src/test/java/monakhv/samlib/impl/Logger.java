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
 *  01.03.16 14:24
 *
 */

package monakhv.samlib.impl;

import monakhv.samlib.log.AbstractLogger;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;

/**
 * Created by monakhv on 01.03.16.
 */
public class Logger implements AbstractLogger {
    private HashMap<String, org.apache.logging.log4j.Logger> logs;

    Logger() {
        logs = new HashMap<>();
    }

    private org.apache.logging.log4j.Logger getLogger(String tag) {
        if (logs.containsKey(tag)) {
            return logs.get(tag);
        }
        logs.put(tag, LogManager.getLogger(tag));
        return logs.get(tag);
    }



    @Override
    public void verbose(String tag, String mes) {
        getLogger(tag).trace(mes);
    }

    @Override
    public void debug(String tag, String mes) {
        getLogger(tag).debug(mes);
    }

    @Override
    public void info(String tag, String mes) {
        getLogger(tag).info(mes);
    }

    @Override
    public void warn(String tag, String mes) {
        getLogger(tag).warn(mes);
    }

    @Override
    public void error(String tag, String mes) {
        getLogger(tag).error(mes);
    }

    @Override
    public void verbose(String tag, String mes, Throwable ex) {
        getLogger(tag).trace(mes,ex);
    }

    @Override
    public void debug(String tag, String mes, Throwable ex) {
        getLogger(tag).debug(mes,ex);
    }

    @Override
    public void info(String tag, String mes, Throwable ex) {
        getLogger(tag).info(mes,ex);
    }

    @Override
    public void warn(String tag, String mes, Throwable ex) {
        getLogger(tag).warn(mes,ex);
    }

    @Override
    public void error(String tag, String mes, Throwable ex) {
        getLogger(tag).error(mes,ex);
    }
}
