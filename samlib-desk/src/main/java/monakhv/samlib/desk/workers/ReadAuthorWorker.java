package monakhv.samlib.desk.workers;

import monakhv.samlib.db.entity.Author;
import monakhv.samlib.service.AuthorService;

import javax.swing.*;

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
 * 16.07.15.
 */
public class ReadAuthorWorker extends SwingWorker<Void,Void> {
    private AuthorService service;
    private Author authors;

    public ReadAuthorWorker(AuthorService service, Author authors) {
        this.service = service;
        this.authors = authors;
    }

    @Override
    protected Void doInBackground() throws Exception {
        service.makeAuthorRead(authors.getId());
        return null;
    }
}
