package monakhv.samlib.desk.gui;

import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;

import javax.swing.*;
import java.awt.*;

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
 * 2/17/15.
 */
public class BookRenderer extends DefaultListCellRenderer {

    private final BookRow panel = new BookRow();



    public BookRenderer(){

    }
    @Override
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean hasFocus) {

        if (value != null){
            if (value instanceof Book){
                final Book book = (Book) value;
                panel.load(book);
            }
        }


        panel.setEnabled(list.isEnabled());
        return panel;

    }
}
