/*
 * Created by JFormDesigner on Mon Jul 20 17:19:00 MSK 2015
 */

package monakhv.samlib.desk.gui;

import javax.swing.border.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import monakhv.samlib.db.entity.AuthorCard;




import javax.swing.*;


/**
 * @author Dmitry Monakhov
 */
public class AuthorCardRow extends JPanel {
    public AuthorCardRow() {
        initComponents();
        scrollPane1.removeMouseWheelListener(scrollPane1.getMouseWheelListeners()[0]);
     }
    public AuthorCardRow(AuthorCard authorCard) {
        this();
        load(authorCard);
    }
    public void load(AuthorCard authorCard){
        name.setText("<html>"+authorCard.getName()+"</html>");
        title.setText(authorCard.getTitle());
        url.setText(authorCard.getUrl());

        //description.setEditorKit(new WrapEditorKit());
        description.setText(authorCard.getDescription());


        String ss = Integer.toString(authorCard.getSize()) + "K/" + Integer.toString(authorCard.getCount());
        sizeCount.setText(ss);

    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        name = new JLabel();
        sizeCount = new JLabel();
        url = new JLabel();
        title = new JLabel();
        scrollPane1 = new JScrollPane();
        description = new JTextPane();

        //======== this ========
        setBorder(new EtchedBorder());
        setLayout(new FormLayout(
            "[pref,150dlu]:grow, $lcgap, pref",
            "3*(fill:pref, $lgap), fill:[30px,pref]:grow"));

        //---- name ----
        name.setText("text");
        name.setFont(name.getFont().deriveFont(name.getFont().getSize() + 6f));
        add(name, CC.xy(1, 1));

        //---- sizeCount ----
        sizeCount.setText("text");
        add(sizeCount, CC.xywh(3, 1, 1, 5));

        //---- url ----
        url.setText("text");
        add(url, CC.xy(1, 3));

        //---- title ----
        title.setText("text");
        add(title, CC.xy(1, 5));

        //======== scrollPane1 ========
        {
            scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scrollPane1.setBorder(null);
            scrollPane1.setViewportView(description);
        }
        add(scrollPane1, CC.xy(1, 7));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel name;
    private JLabel sizeCount;
    private JLabel url;
    private JLabel title;
    private JScrollPane scrollPane1;
    private JTextPane description;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
