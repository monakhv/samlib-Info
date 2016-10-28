package monakhv.samlib.http;


import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import monakhv.samlib.log.Log;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;


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
 * 2/5/15.
 */
public class ProxyData {
    private static final String DEBUG_TAG = "ProxyData";
    private static final String GOOGLE_HTTP_HOST = "compress.googlezip.net";
    private static final String AUTH_KEY = "ac4500dd3b7579186c1b0620614fdb1f7d61f944";
    private static final String[] CHROME_VERSION = {"49", "0", "2623", "87"};
    public static final ProxyData GOOGLE_HTTP;

    static {
        GOOGLE_HTTP = new ProxyData(GOOGLE_HTTP_HOST, 80, true);
    }

    private String host;
    private int port;
    private String user;
    private String password;
    private boolean isGoogle = false;

    private ProxyData(String host, int port, boolean isGoogle) {
        this.host = host;
        this.port = port;
        this.isGoogle = isGoogle;
    }

    public ProxyData(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

//    public Authenticator getAuthenticator() {
//        return new Authenticator() {
//            @Override
//            public PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(user, password.toCharArray());
//            }
//        };
//    }

    void applyProxy(OkHttpClient.Builder builder, Request.Builder requestBuilder) {


        SocketAddress addr = new InetSocketAddress(host, port);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);

        builder.proxy(proxy);
        if (isGoogle) {
            java.net.Authenticator.setDefault(null);
            requestBuilder.header("Chrome-Proxy", getAuthString());
            return;//for google proxy we do not need credentials
        }

        if (user == null || user.equalsIgnoreCase("")) {
            return;//do not make credentials for empty users
        }

        final String credential = Credentials.basic(user, password);

        builder.proxyAuthenticator((route, response) -> {
            Log.d("ProxyData", "authenticate: " + user + ":" + password);
            return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        });


    }

    private String getAuthString() {
        String timestamp = Long.toString(System.currentTimeMillis()).substring(0, 10);

        String sid = (timestamp + AUTH_KEY + timestamp);
        sid = md5(sid);
        return "ps=" + timestamp + "-" + randomNumber() + "-" + randomNumber() + "-" + randomNumber() + ", sid=" + sid + ", b=" +
                CHROME_VERSION[2] + ", p=" + CHROME_VERSION[3] + ", c=win";

    }

    private String randomNumber() {
        return Integer.toString((int) (Math.random() * 1000000000));
    }

    private String md5(final String str) {
        final String MD5 = "MD5";
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(MD5);
        } catch (NoSuchAlgorithmException e) {
            Log.e(DEBUG_TAG, "md5: Algorithm error", e);
            return null;
        }
        digest.update(str.getBytes());
        byte messageDigest[] = digest.digest();

        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : messageDigest) {
            String h = Integer.toHexString(0xFF & aMessageDigest);
            while (h.length() < 2)
                h = "0" + h;
            hexString.append(h);
        }
        return hexString.toString();

    }

}
