package monakhv.samlib.desk.gui;

import monakhv.samlib.db.entity.Book;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
 * 2/19/15.
 */
public class BookList {
    private JPanel panel;

    private List<Component> panels;

    public BookList(JPanel p){
        panels = new ArrayList<>();
        this.panel=p;
    }

    public void load(List<Book> books){

        panel.removeAll();
        panels.clear();

        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));


        for (Book book: books){
            JPanel row = new BookRow(book);

            Dimension preferredSize = row.getPreferredSize();
            Dimension maxSize = row.getMaximumSize();
            row.setMaximumSize(new Dimension( maxSize.width,preferredSize.height+10));

            panel.add(row);
            panels.add(row);
        }

        Component comp = Box.createVerticalGlue();
        panel.add(comp);
        panels.add(comp);
        panel.revalidate();


    }


}
