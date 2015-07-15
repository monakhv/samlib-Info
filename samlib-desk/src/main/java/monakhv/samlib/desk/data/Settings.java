package monakhv.samlib.desk.data;

import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.http.Proxy;
import monakhv.samlib.log.Log;

import java.io.*;
import java.util.Properties;

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
public class Settings extends AbstractSettings {
    private static final String DEBUG_TAG = "SettingsHelper";
    private static final String CONFIG_DIR = ".samlib-info";

    private static final String PROPERTY_FILE = "samlib-info.properties";
    //private static final String os = System.getProperty("os.name");
    private static final String home = System.getProperty("user.home");


    public static final String ZERO = "0";
    public static final String ONE = "1";


    private static final String PROP_KEY_DATA_DIR = "PROP_KEY_DATA_DIR";
    private static final String PROP_VALUE_DATA_DIR = home + sep + CONFIG_DIR;
    private static final String PROP_KEY_FIRST_MIRROR = "PROP_KEY_FIRST_MIRROR";
    private static final String PROP_VALUE_FIRST_MIRROR = "SamLib";

    private static final String PROP_KEY_USE_PROXY = "PROP_KEY_USE_PROXY";
    private static final String PROP_VALUE_USE_PROXY = ZERO;

    private static final String PROP_KEY_PROXY_HOST = "PROP_KEY_PROXY_HOST";
    private static final String PROP_VALUE_PROXY_HOST = "";
    private static final String PROP_KEY_PROXY_PORT = "PROP_KEY_PROXY_PORT";
    private static final String PROP_VALUE_PROXY_PORT = "3128";
    private static final String PROP_KEY_PROXY_USER = "PROP_KEY_PROXY_USER";
    private static final String PROP_VALUE_PROXY_USER = "";
    private static final String PROP_KEY_PROXY_PASSWORD = "PROP_KEY_PROXY_PASSWORD";
    private static final String PROP_VALUE_PROXY_PASSWORD = "";


    private static final String PROP_KEY_AUTO_LOAD_FLAG="PROP_KEY_AUTO_LOAD_FLAG";
    private static final String PROP_VALUE_AUTO_LOAD_FLAG=ONE;
    private static final String PROP_KEY_LIMIT_BOOK_LIFE_TIME_FLAG="PROP_KEY_LIMIT_BOOK_LIFE_TIME_FLAG";
    private static final String PROP_VALUE_LIMIT_BOOK_LIFE_TIME_FLAG=ZERO;
    private static final String PROP_KEY_BOOK_LIFE_TIME="PROP_KEY_BOOK_LIFE_TIME";
    private static final String PROP_VALUE_BOOK_LIFE_TIME="365";
    private static final String PROP_KEY_BOOK_FILE_TYPE="PROP_KEY_BOOK_FILE_TYPE";
    private static final String PROP_VALUE_BOOK_FILE_TYPE="HTML";



    private static final Properties defaultProperty;

    static {
        defaultProperty = new Properties();
        defaultProperty.setProperty(PROP_KEY_DATA_DIR, PROP_VALUE_DATA_DIR);
        defaultProperty.setProperty(PROP_KEY_FIRST_MIRROR, PROP_VALUE_FIRST_MIRROR);
        defaultProperty.setProperty(PROP_KEY_USE_PROXY, PROP_VALUE_USE_PROXY);
        defaultProperty.setProperty(PROP_KEY_PROXY_HOST, PROP_VALUE_PROXY_HOST);
        defaultProperty.setProperty(PROP_KEY_PROXY_PORT, PROP_VALUE_PROXY_PORT);
        defaultProperty.setProperty(PROP_KEY_PROXY_USER, PROP_VALUE_PROXY_USER);
        defaultProperty.setProperty(PROP_KEY_PROXY_PASSWORD, PROP_VALUE_PROXY_PASSWORD);
        defaultProperty.setProperty(PROP_KEY_AUTO_LOAD_FLAG, PROP_VALUE_AUTO_LOAD_FLAG);
        defaultProperty.setProperty(PROP_KEY_LIMIT_BOOK_LIFE_TIME_FLAG, PROP_VALUE_LIMIT_BOOK_LIFE_TIME_FLAG);
        defaultProperty.setProperty(PROP_KEY_BOOK_LIFE_TIME, PROP_VALUE_BOOK_LIFE_TIME);
        defaultProperty.setProperty(PROP_KEY_BOOK_FILE_TYPE, PROP_VALUE_BOOK_FILE_TYPE);
    }

    private String proxyStrPort;
    private String proxyHost;
    private String proxyUser;
    private String proxyPassword;
    private String bookLifeTime;
    private boolean proxyUse;
    private boolean autoLoadFlag;
    private boolean limitBookLifeTimeFlag;
    private FileType fileType;

    private File config_dir;
    private Properties props;
    private static Settings instance;


    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    private Settings() {
        Log.checkInit(new Logger(), this);

        config_dir = new File(home + sep + CONFIG_DIR);
        if (!config_dir.isDirectory()) {
            config_dir.mkdir();
        }

        File propFile = new File(config_dir, PROPERTY_FILE);
        props = new Properties(defaultProperty);

        if (propFile.exists()) {//load properties from the file
            try {
                FileInputStream in = new FileInputStream(propFile);
                props.load(in);
                in.close();
            } catch (java.io.IOException e) {
                Log.e(DEBUG_TAG, "read properties error using default");
            }

        }


        proxyUse = props.getProperty(PROP_KEY_USE_PROXY).equalsIgnoreCase(ONE);
        proxyStrPort = props.getProperty(PROP_KEY_PROXY_PORT, "3128");

        proxyHost = props.getProperty(PROP_KEY_PROXY_HOST);
        proxyUser = props.getProperty(PROP_KEY_PROXY_USER);
        proxyPassword = props.getProperty(PROP_KEY_PROXY_PASSWORD);


        autoLoadFlag=props.getProperty(PROP_KEY_AUTO_LOAD_FLAG).equalsIgnoreCase(ONE);
        limitBookLifeTimeFlag=props.getProperty(PROP_KEY_LIMIT_BOOK_LIFE_TIME_FLAG).equalsIgnoreCase(ONE);
        bookLifeTime=props.getProperty(PROP_KEY_BOOK_LIFE_TIME);
        fileType=FileType.valueOf(props.getProperty(PROP_KEY_BOOK_FILE_TYPE));
    }

    private void saveBoolean(boolean value, String key){
        if (value){
            props.setProperty(key,ONE);
        }
        else {
            props.setProperty(key,ZERO);

        }
    }
    public void saveProperties() {

        saveBoolean(proxyUse,PROP_KEY_USE_PROXY);
        saveBoolean(autoLoadFlag,PROP_KEY_AUTO_LOAD_FLAG);
        saveBoolean(limitBookLifeTimeFlag,PROP_KEY_LIMIT_BOOK_LIFE_TIME_FLAG);

        props.setProperty(PROP_KEY_PROXY_PORT, proxyStrPort);
        props.setProperty(PROP_KEY_PROXY_HOST,proxyHost);
        props.setProperty(PROP_KEY_PROXY_USER,proxyUser);
        props.setProperty(PROP_KEY_PROXY_PASSWORD,proxyPassword);

        props.setProperty(PROP_KEY_BOOK_LIFE_TIME, bookLifeTime);
        props.setProperty(PROP_KEY_BOOK_FILE_TYPE,fileType.name());


        File propFile = new File(config_dir, PROPERTY_FILE);
        try {
            FileOutputStream out = new FileOutputStream(propFile);
            props.store(out, "save");
            out.close();
        } catch (FileNotFoundException e) {

            Log.e(DEBUG_TAG, "save properties error - open file");
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "save properties error - sore file");
        }


    }

    @Override
    public String getFirstMirror() {
        return props.getProperty(PROP_KEY_FIRST_MIRROR);
    }

    @Override
    public Proxy getProxy() {
        if (!proxyUse) {
            return null;
        }
        int proxyPort = Integer.parseInt(proxyStrPort);

        return new Proxy(proxyHost, proxyPort, proxyUser, proxyPassword);
    }

    @Override
    public void log(String debugTag, String s) {

    }

    @Override
    public void log(String debugTag, String s, Exception e) {

    }


    @Override
    public File getDataDirectory() {
        String path = props.getProperty(PROP_KEY_DATA_DIR, CONFIG_DIR);
        File dataDirectory = new File(path);
        if (dataDirectory.isDirectory()) {
            return dataDirectory;
        }
        dataDirectory.mkdirs();
        return dataDirectory;
    }

    @Override
    public String getBookLifeTime() {

        return bookLifeTime;
    }

    public void setBookLifeTime(String bookLifeTime) {
        this.bookLifeTime = bookLifeTime;
    }

    @Override
    public FileType getFileType() {

        return fileType;
    }

    public void setFileType(String fileTypeS) {
        this.fileType = FileType.valueOf(fileTypeS);
    }

    @Override
    public boolean getAutoLoadFlag() {

        return autoLoadFlag;
    }

    public void setAutoLoadFlag(boolean autoLoadFlag) {
        this.autoLoadFlag = autoLoadFlag;
    }

    @Override
    public boolean getLimitBookLifeTimeFlag() {

        return limitBookLifeTimeFlag;
    }

    public void setLimitBookLifeTimeFlag(boolean limitBookLifeTimeFlag) {
        this.limitBookLifeTimeFlag = limitBookLifeTimeFlag;
    }

    public String getProxyStrPort() {
        return proxyStrPort;
    }

    public void setProxyStrPort(String proxyStrPort) {
        this.proxyStrPort = proxyStrPort;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public boolean isProxyUse() {
        return proxyUse;
    }

    public void setProxyUse(boolean proxyUse) {
        this.proxyUse = proxyUse;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }
}

















