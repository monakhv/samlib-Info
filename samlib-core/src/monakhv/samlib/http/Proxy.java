package monakhv.samlib.http;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;

import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

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
public class Proxy {
    private String host;
    private int port;
    private String user;
    private String password;

    public Proxy(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public Authenticator getAuthenticator(){
        return new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        };
    }

    public void applyProxy(DefaultHttpClient httpclient){
          HttpHost proxy = new HttpHost(host, port);
          AuthScope scope = new AuthScope(host, port);
          UsernamePasswordCredentials pwd = new UsernamePasswordCredentials(user, password);


        if (pwd != null && scope != null) {
            httpclient.getCredentialsProvider().setCredentials(scope, pwd);
        }

        if (proxy != null) {
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
    }

}
