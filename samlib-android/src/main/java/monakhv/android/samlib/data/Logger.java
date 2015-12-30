package monakhv.android.samlib.data;

import android.util.Log;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import monakhv.samlib.log.AbstractLogger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;

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
    private static HashMap<String,org.slf4j.Logger> logger;
    private SettingsHelper mSettingsHelper;


    Logger(SettingsHelper settingsHelper){
        mSettingsHelper=settingsHelper;

        logger = new HashMap<>();
        initLogger();
    }

    private void initLogger() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);


        // setup LogcatAppender
        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
        encoder2.setContext(lc);
        encoder2.setPattern("[%thread] %msg%n");
        encoder2.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setEncoder(encoder2);
        logcatAppender.start();


        if (mSettingsHelper.getDebugFlag()){
            // setup FileAppender
            File logFile = new File(mSettingsHelper.getDataDirectoryPath(),SettingsHelper.DEBUG_FILE);
            PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
            encoder1.setContext(lc);
            encoder1.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
            encoder1.start();
            FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
            fileAppender.setContext(lc);
            File save = new File(logFile.getAbsolutePath());
            fileAppender.setFile(save.getAbsolutePath());
            fileAppender.setEncoder(encoder1);
            fileAppender.setPrudent(true);
            fileAppender.setLazy(false);
            fileAppender.start();

            root.addAppender(fileAppender);
        }
        root.addAppender(logcatAppender);

    }


    private org.slf4j.Logger getLogger(String tag){
        if (logger.containsKey(tag)){
            return logger.get(tag);
        }
        else {
            org.slf4j.Logger log = LoggerFactory.getLogger(tag);
            logger.put(tag,log);
            return log;
        }
    }



    @Override
    public void verbose(String tag, String msg) {
        getLogger(tag).trace(msg);
    }

    @Override
    public void debug(String tag, String msg) {
        getLogger(tag).debug(msg);
    }

    @Override
    public void info(String tag, String msg) {
        getLogger(tag).info(msg);
    }

    @Override
    public void warn(String tag, String msg) {
        getLogger(tag).warn(msg);
    }

    @Override
    public void error(String tag, String msg) {
        getLogger(tag).error(msg);
    }

    @Override
    public void verbose(String tag, String msg, Throwable ex) {
        getLogger(tag).trace(msg,ex);
    }

    @Override
    public void debug(String tag, String msg, Throwable ex) {
        getLogger(tag).debug(msg,ex);
    }

    @Override
    public void info(String tag, String msg, Throwable ex) {
        getLogger(tag).info(msg,ex);
    }

    @Override
    public void warn(String tag, String msg, Throwable ex) {
        getLogger(tag).warn(msg,ex);
    }

    @Override
    public void error(String tag, String msg, Throwable ex) {
        getLogger(tag).error(msg,ex);
    }
}
