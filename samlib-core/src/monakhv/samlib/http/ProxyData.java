package monakhv.samlib.http;




import monakhv.samlib.log.Log;
import okhttp3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;


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

//    public Authenticator getAuthenticator() {
//        return new Authenticator() {
//            @Override
//            public PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(user, password.toCharArray());
//            }
//        };
//    }

    public void applyProxy(OkHttpClient.Builder builder) {

        SocketAddress addr = new InetSocketAddress(host, port);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);

        builder.proxy(proxy);

        if (user == null || user.equalsIgnoreCase("")) {
            return ;//do not make credentials for empty users
        }

        final String credential = Credentials.basic(user, password);

        builder.authenticator(new okhttp3.Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                Log.d("ProxyData","authenticate: "+user+":"+password);
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            }

        });


    }

}
