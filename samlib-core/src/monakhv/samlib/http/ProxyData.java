package monakhv.samlib.http;

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


import java.io.IOException;
import java.net.*;

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
    private String host;
    private int port;
    private String user;
    private String password;

    public ProxyData(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public Authenticator getAuthenticator() {
        return new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        };
    }

    public void applyProxy(OkHttpClient httpclient) {

        SocketAddress addr = new InetSocketAddress(host, port);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);

        httpclient.setProxy(proxy);

        final String credential = Credentials.basic(user, password);


        httpclient.setAuthenticator(new com.squareup.okhttp.Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                return null;
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            }
        });

    }

}
