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

    private final JPanel panel = new JPanel();
    private final JPanel apan = new JPanel();
    private final JLabel name = new JLabel();
    private final JLabel newIcon = new JLabel();
    private final JLabel updated = new JLabel();
    private final JLabel url = new JLabel();
    private final JLabel tgnames = new JLabel();
    private Font font;
    private final Font boldfont;
    private Font smallFont;
    private SimpleDateFormat df;

    public AuthorRenderer() {
        df = new SimpleDateFormat(DATE_FORMAT);

        font = name.getFont();
//        Font ff =font.deriveFont(Font.TYPE1_FONT);
//        font = ff;
        Log.i(DEBUG_TAG, "Normal Font size: " + font.getSize2D());

        boldfont = font.deriveFont(Font.BOLD);
        float ss = ((float) (font.getSize2D() * 3. / 4.));
        Log.i(DEBUG_TAG, "Small Font size: " + ss);
        smallFont = font.deriveFont(ss);



        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        panel.setLayout(new BorderLayout());
        panel.setOpaque(true);


        GroupLayout layout = new GroupLayout(apan);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        apan.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(name)
                                .addComponent(url)
                                .addComponent(updated))
                .addComponent(tgnames)

        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()

                        .addComponent(name)
                        .addComponent(url)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(updated)
                                .addComponent(tgnames))


        );
        apan.setOpaque(true);
        panel.add(apan,BorderLayout.CENTER);
        panel.add(newIcon,BorderLayout.EAST);

    }

    @Override
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean hasFocus) {
        if (value != null) {
            if (value instanceof Author) {
                final Author a = (Author) value;
                name.setText(a.getName());
                Date d = new Date(a.getUpdateDate());
                updated.setText(df.format(d));
                updated.setFont(smallFont);
                url.setText(a.getUrl());
                url.setFont(smallFont);
                String tags = a.getAll_tags_name();
                if (tags != null) {
                    tgnames.setText(tags.replaceAll(",", ", "));
                } else {
                    tgnames.setText("");
                }
                tgnames.setFont(smallFont);

                if (a.isIsNew()) {
                    newIcon.setIcon(GREEN_ICON);
                    name.setFont(boldfont);
                } else {
                    newIcon.setIcon(BLACK_ICON);
                    name.setFont(font);
                }
            }

        }

        if (isSelected) {
            name.setForeground(Color.WHITE);
            updated.setForeground(Color.WHITE);
            url.setForeground(Color.WHITE);
            panel.setBackground(list.getSelectionBackground());
            panel.setForeground(list.getSelectionForeground());
            apan.setBackground(list.getSelectionBackground());
            apan.setForeground(list.getSelectionForeground());

        } else {
            name.setForeground(Color.BLACK);
            updated.setForeground(Color.BLACK);
            url.setForeground(Color.BLACK);
            panel.setBackground(list.getBackground());
            panel.setForeground(list.getForeground());
            apan.setBackground(list.getBackground());
            apan.setForeground(list.getForeground());
        }
        panel.setEnabled(list.isEnabled());
        return panel;


    }
}
