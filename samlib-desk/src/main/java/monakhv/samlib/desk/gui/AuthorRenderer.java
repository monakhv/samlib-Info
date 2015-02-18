package monakhv.samlib.desk.gui;

import monakhv.samlib.db.entity.Author;
import monakhv.samlib.log.Log;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

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
 * 2/16/15.
 */
public class AuthorRenderer extends DefaultListCellRenderer {
    private static final String DEBUG_TAG = "AuthorRenderer";
    public static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private final static ImageIcon GREEN_ICON = new ImageIcon(AuthorRenderer.class.getResource("/pics/16x16/bullet_green.png"));
    private final static ImageIcon BLACK_ICON = new ImageIcon(AuthorRenderer.class.getResource("/pics/16x16/bullet_black.png"));

    private final AuthorRow pan = new AuthorRow();
    private SimpleDateFormat df;

    public AuthorRenderer() {
        df = new SimpleDateFormat(DATE_FORMAT);

    }

    @Override
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean hasFocus) {
        JLabel name = pan.getJName();
        JLabel updated=pan.getUpdated();
        JLabel url = pan.getUrl();
        JLabel tgnames = pan.getTgnames();
        JLabel newIcon = pan.getNewIcon();
        if (value != null) {
            if (value instanceof Author) {
                final Author a = (Author) value;
                name.setText(a.getName());
                Date d = new Date(a.getUpdateDate());
                updated.setText(df.format(d));

                url.setText(a.getUrl());

                String tags = a.getAll_tags_name();
                if (tags != null) {
                    tgnames.setText(tags.replaceAll(",", ", "));
                } else {
                    tgnames.setText("");
                }


                if (a.isIsNew()) {
                    newIcon.setIcon(GREEN_ICON);
                    //name.setFont(boldfont);
                } else {
                    newIcon.setIcon(BLACK_ICON);
                    //name.setFont(font);
                }
            }

        }

        pan.setSelected(isSelected,list);

        pan.setEnabled(list.isEnabled());
        return pan;


    }
}
