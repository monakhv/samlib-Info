package monakhv.samlib.data;

import monakhv.samlib.db.entity.Book;
import monakhv.samlib.http.Proxy;

import java.io.File;

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
 * 2/12/15.
 */
public interface SettingsHelper {
    String getFirstMirror();
    Proxy getProxy();
    void log(String debugTag, String s);
    void log(String debugTag, String s, Exception e);
    public File getBookFile(Book book,FileType fileType);


    public static enum FileType {
        HTML(".html","text/html"),
        FB2(".fb2",null);
        public final String ext;
        public final String mime;

        private FileType(String ext,String mime) {
            this.ext=ext;
            this.mime = mime;
        }
    }
}
