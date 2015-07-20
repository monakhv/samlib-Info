package monakhv.samlib.desk.workers;

import monakhv.samlib.service.AuthorService;

import javax.swing.*;
import java.util.ArrayList;

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
 * 20.07.15.
 */
public class AddAuthorWorker extends SwingWorker<Void,Void> {
    private AuthorService service;
    private ArrayList<String> authors;

    public AddAuthorWorker(AuthorService service, ArrayList<String> authors) {
        this.service = service;
        this.authors = authors;
    }

    @Override
    protected Void doInBackground() throws Exception {
        service.makeAuthorAdd(authors);
        return null;
    }
}
