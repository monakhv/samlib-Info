/*
 * Created by JFormDesigner on Tue Feb 17 17:46:34 MSK 2015
 */

package monakhv.samlib.desk.gui;

import javax.swing.*;
import javax.swing.border.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import monakhv.samlib.db.entity.Author;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Dmitry Monakhov
 */
public class AuthorRow extends JPanel {
    public static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private final static ImageIcon GREEN_ICON = new ImageIcon(AuthorRenderer.class.getResource("/pics/16x16/bullet_green.png"));
    private final static ImageIcon BLACK_ICON = new ImageIcon(AuthorRenderer.class.getResource("/pics/16x16/bullet_black.png"));

    private SimpleDateFormat df;
    public AuthorRow() {
        df = new SimpleDateFormat(DATE_FORMAT);

        initComponents();
    }


    public void load(Author a){
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
    public void setSelected(boolean isSelected, JList list) {
        if (isSelected) {
            name.setForeground(Color.WHITE);
            updated.setForeground(Color.WHITE);
            url.setForeground(Color.WHITE);
            tgnames.setForeground(Color.WHITE);
            panel1.setBackground(list.getSelectionBackground());
            panel1.setForeground(list.getSelectionForeground());
            this.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());


        } else {
            name.setForeground(Color.BLACK);
            updated.setForeground(Color.BLACK);
            url.setForeground(Color.BLACK);
            tgnames.setForeground(Color.BLACK);
            panel1.setBackground(list.getBackground());
            panel1.setForeground(list.getForeground());
            this.setBackground(list.getBackground());
            this.setForeground(list.getForeground());

        }
    }
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel1 = new JPanel();
        name = new JLabel();
        url = new JLabel();
        updated = new JLabel();
        tgnames = new JLabel();
        newIcon = new JLabel();

        //======== this ========
        setBorder(new EtchedBorder(EtchedBorder.RAISED));
        setLayout(new FormLayout(
            "left:[pref,160dlu]:grow, [5px,pref], pref",
            "pref"));

        //======== panel1 ========
        {
            panel1.setLayout(new GridBagLayout());
            ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 5, 0, 0};
            ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 5, 0, 5, 0, 0};
            ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- name ----
            name.setText("text");
            name.setFont(name.getFont().deriveFont(name.getFont().getSize() + 6f));
            panel1.add(name, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));

            //---- url ----
            url.setText("text");
            panel1.add(url, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));

            //---- updated ----
            updated.setText("text");
            panel1.add(updated, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));

            //---- tgnames ----
            tgnames.setText("text");
            panel1.add(tgnames, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        add(panel1, CC.xy(1, 1));
        add(newIcon, CC.xy(3, 1));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel1;
    private JLabel name;
    private JLabel url;
    private JLabel updated;
    private JLabel tgnames;
    private JLabel newIcon;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
