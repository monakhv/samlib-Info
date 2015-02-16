package monakhv.samlib.desk.data;

import monakhv.samlib.data.SettingsHelper;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.http.Proxy;
import monakhv.samlib.log.Log;

import java.io.File;
import java.io.FileInputStream;
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
public class Settings implements SettingsHelper  {
    private static final String DEBUG_TAG="SettingsHelper";
    private static final String CONFIG_DIR=".samlib-info";
    private static final String BOOKS_DIR="Books";
    private static final String PROPERTY_FILE="samlib-info.properties";
    private static final String os   = System.getProperty("os.name");
    private static final String home = System.getProperty("user.home");
    private static final String sep  = System.getProperty("file.separator");

    public static final String ZERO="0";
    public static final String ONE="1";


    private static final String PROP_KEY_DATA_DIR="PROP_KEY_DATA_DIR";
    private static final String PROP_VALUE_DATA_DIR=home+sep+CONFIG_DIR;
    private static final String PROP_KEY_FIRST_MIRROR="PROP_KEY_FIRST_MIRROR";
    private static final String PROP_VALUE_FIRST_MIRROR="SamLib";

    private static final String PROP_KEY_USE_PROXY="PROP_KEY_USE_PROXY";
    private static final String PROP_VALUE_USE_PROXY="0";

    private static final String PROP_KEY_PROXY_HOST="PROP_KEY_PROXY_HOST";
    private static final String PROP_VALUE_PROXY_HOST="";
    private static final String PROP_KEY_PROXY_PORT="PROP_KEY_PROXY_PORT";
    private static final String PROP_VALUE_PROXY_PORT="3128";
    private static final String PROP_KEY_PROXY_USER="PROP_KEY_PROXY_USER";
    private static final String PROP_VALUE_PROXY_USER="";
    private static final String PROP_KEY_PROXY_PASSWORD="PROP_KEY_PROXY_PASSWORD";
    private static final String PROP_VALUE_PROXY_PASSWORD="";


    private static final Properties defaultProperty;

    static {
        defaultProperty = new Properties();
        defaultProperty.setProperty(PROP_KEY_DATA_DIR,PROP_VALUE_DATA_DIR);
        defaultProperty.setProperty(PROP_KEY_FIRST_MIRROR,PROP_VALUE_FIRST_MIRROR);
        defaultProperty.setProperty(PROP_KEY_USE_PROXY,PROP_VALUE_USE_PROXY);
        defaultProperty.setProperty(PROP_KEY_PROXY_HOST,PROP_VALUE_PROXY_HOST);
        defaultProperty.setProperty(PROP_KEY_PROXY_PORT,PROP_VALUE_PROXY_PORT);
        defaultProperty.setProperty(PROP_KEY_PROXY_USER,PROP_VALUE_PROXY_USER);
        defaultProperty.setProperty(PROP_KEY_PROXY_PASSWORD,PROP_VALUE_PROXY_PASSWORD);
    }

    private File config_dir;
    private Properties props ;
    private static Settings instance;


    public static Settings getInstance(){
        if (instance==null){
            instance=new Settings();
        }
        return instance ;
    }
    private Settings(){
        Log.checkInit(new Logger());

        config_dir=new File(home+sep+CONFIG_DIR);
        if (! config_dir.isDirectory()){
            config_dir.mkdir();
        }

        File propFile= new File(config_dir,PROPERTY_FILE);
        props = new Properties( defaultProperty);

        if (propFile.exists()){//load properties from the file
            try {
                FileInputStream in = new FileInputStream( propFile);
                props.load(in);
            } catch (java.io.IOException e) {
                Log.e(DEBUG_TAG,"read properties error using default");
            }
        }


    }
    @Override
    public String getFirstMirror() {
        return props.getProperty(PROP_KEY_FIRST_MIRROR);
    }

    @Override
    public Proxy getProxy() {
        if (props.getProperty(PROP_KEY_USE_PROXY).equalsIgnoreCase(ZERO)){
            return null;
        }
        String sPort = props.getProperty(PROP_KEY_PROXY_PORT,"3128");
        int port =Integer.parseInt(sPort);
        String host = props.getProperty(PROP_KEY_PROXY_HOST);
        String user = props.getProperty(PROP_KEY_PROXY_USER);
        String password = props.getProperty(PROP_KEY_PROXY_PASSWORD);
        return new Proxy( host,port,user,password);
    }

    @Override
    public void log(String debugTag, String s) {

    }

    @Override
    public void log(String debugTag, String s, Exception e) {

    }

    @Override
    public File getBookFile(Book book, FileType fileType) {
        String ff = BOOKS_DIR + sep + book.getUri() + fileType.ext;

        File ss = new File(getDataDirectory(), ff);
        File pp = ss.getParentFile();
        boolean res = pp.mkdirs();
        Log.d(DEBUG_TAG, "Path: " + pp.getAbsolutePath() + " result is: " + res);
        return ss;
    }

    @Override
    public String getDataDirectoryPath() {
        return getDataDirectory().getAbsolutePath();
    }

    public File getDataDirectory() {
        String path=props.getProperty(PROP_KEY_DATA_DIR,CONFIG_DIR);
        File dataDirectory=new File(path);
        if (dataDirectory.isDirectory()){
            return dataDirectory;
        }
        dataDirectory.mkdirs();
        return dataDirectory;
    }
}
