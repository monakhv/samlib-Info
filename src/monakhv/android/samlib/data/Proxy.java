package monakhv.android.samlib.data;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;

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
     String host;
    int port;
    String user;
    String password;

    Proxy(){

    }
    public HttpHost getHttpHost() {
        return new HttpHost(host, port);
    }
    public AuthScope getAuthScope(){

        return  new AuthScope(host, port);
    }
    public UsernamePasswordCredentials getPasswordCredentials(){
        return new UsernamePasswordCredentials(user, password);
    }

    public Authenticator getAuthenticator(){
        Authenticator a = new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password.toCharArray());
            }
        };
        return a;
    }

}
